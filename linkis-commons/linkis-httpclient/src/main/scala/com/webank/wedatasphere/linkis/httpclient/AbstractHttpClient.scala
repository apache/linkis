/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.httpclient

import java.util
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.httpclient.authentication.{AbstractAuthenticationStrategy, AuthenticationAction, HttpAuthentication}
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig
import com.webank.wedatasphere.linkis.httpclient.discovery.{AbstractDiscovery, Discovery, HeartbeatAction}
import com.webank.wedatasphere.linkis.httpclient.exception.{HttpClientResultException, HttpMessageParseException}
import com.webank.wedatasphere.linkis.httpclient.loadbalancer.{AbstractLoadBalancer, DefaultLoadbalancerStrategy, LoadBalancer}
import com.webank.wedatasphere.linkis.httpclient.request._
import com.webank.wedatasphere.linkis.httpclient.response._
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpException, HttpResponse, _}
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.collection.Iterable
import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}


/**
  * Created by enjoyyin on 2019/5/20.
  */
abstract class AbstractHttpClient(clientConfig: ClientConfig, clientName: String) extends Client {

  protected implicit val formats: Formats = DefaultFormats
  protected implicit val executors: ExecutionContext = Utils.newCachedExecutionContext(clientConfig.getMaxConnection, clientName, false)
  protected val httpTimeout: Duration = if (clientConfig.getReadTimeout > 0) Duration(clientConfig.getReadTimeout, TimeUnit.MILLISECONDS)
  else Duration.Inf

  protected val httpClient = HttpClients.createDefault()

  if (clientConfig.getAuthenticationStrategy != null) clientConfig.getAuthenticationStrategy match {
    case auth: AbstractAuthenticationStrategy => auth.setClient(this)
    case _ =>
  }
  protected val (discovery, loadBalancer): (Option[Discovery], Option[LoadBalancer]) =
    if (this.clientConfig.isDiscoveryEnabled) {
      val discovery = Some(createDiscovery())
      discovery.foreach {
        case d: AbstractDiscovery =>
          d.setServerUrl(clientConfig.getServerUrl)
          d.setClient(this)
          d.setSchedule(clientConfig.getDiscoveryPeriod, clientConfig.getDiscoveryTimeUnit)
        case d => d.setServerUrl(clientConfig.getServerUrl)
      }
      //如果discovery没有启用，那么启用loadBalancer是没有意义的
      val loadBalancer = if (clientConfig.isLoadbalancerEnabled && this.clientConfig.getLoadbalancerStrategy != null)
        Some(this.clientConfig.getLoadbalancerStrategy.createLoadBalancer())
      else if (clientConfig.isLoadbalancerEnabled) Some(DefaultLoadbalancerStrategy.createLoadBalancer())
      else None
      loadBalancer match {
        case Some(lb: AbstractLoadBalancer) =>
          discovery.foreach(_.addDiscoveryListener(lb))
        case _ =>
      }
      (discovery, loadBalancer)
    } else (None, None)

  discovery.foreach(_.start())

  protected def createDiscovery(): Discovery

  override def execute(requestAction: Action): Result = execute(requestAction, -1)

  override def execute(requestAction: Action, waitTime: Long): Result = {
    if(!requestAction.isInstanceOf[HttpAction])
      throw new UnsupportedOperationException("only HttpAction supported, but the fact is " + requestAction.getClass)
    val action = prepareAction(requestAction.asInstanceOf[HttpAction])
    val response: CloseableHttpResponse = executeHttpAction(action)
    responseToResult(response, action)
  }

  override def execute(requestAction: Action, resultListener: ResultListener): Unit = {
    if (!requestAction.isInstanceOf[HttpAction]) {
      throw new UnsupportedOperationException("only HttpAction supported, but the fact is " + requestAction.getClass)
    }
    val action = prepareAction(requestAction.asInstanceOf[HttpAction])
    val response: CloseableHttpResponse = executeHttpAction(action)
    //response.onSuccess{case r => resultListener.onSuccess(responseToResult(r, action))}
    //response.onFailure{case t => resultListener.onFailure(t)}
  }

  protected def getRequestUrl(suffixUrl: String, requestBody: String): String = {
    val urlPrefix = loadBalancer.map(_.chooseServerUrl(requestBody)).getOrElse(clientConfig.getServerUrl)
    connectUrl(urlPrefix, suffixUrl)
  }

  protected def connectUrl(prefix: String, suffix: String): String = {
    val prefixEnd = prefix.endsWith("/")
    val suffixStart = suffix.startsWith("/")
    if(prefixEnd && suffixStart) prefix.substring(0, prefix.length - 1) + suffix
    else if(!prefixEnd && !suffixStart) prefix + "/" + suffix
    else prefix + suffix
  }

  protected def prepareAction(requestAction: HttpAction): HttpAction = requestAction

  protected def executeHttpAction(requestAction: HttpAction): CloseableHttpResponse = {
    var realURL = ""
    requestAction match {
      case serverUrlAction: ServerUrlAction =>
        realURL = connectUrl(serverUrlAction.serverUrl, requestAction.getURL)
      case _ =>
        realURL = getRequestUrl(requestAction.getURL, requestAction.getRequestBody)
    }

    if (clientConfig.getAuthenticationStrategy != null) clientConfig.getAuthenticationStrategy.login(requestAction, realURL.replaceAll(requestAction.getURL, "")) match {
      case authAction: HttpAuthentication =>
        val cookies = authAction.authToCookies
        if (cookies != null && cookies.nonEmpty) cookies.foreach(requestAction.addCookie)
        val headers = authAction.authToHeaders
        if (headers != null && !headers.isEmpty()) {
          headers.foreach { case (k, v) => requestAction.addHeader(k.toString(), v.toString()) }
        }
      case _ =>
    }

    var response: CloseableHttpResponse = null
    requestAction match {
      case upload: UploadAction =>
        val httpPost = new HttpPost(realURL)
        val builder = MultipartEntityBuilder.create()
        if(upload.inputStreams != null)
          upload.inputStreams.foreach { case (k, v) =>
            builder.addBinaryBody(k, v, ContentType.create("multipart/form-data"), k)
          }
        upload match {
          case get: GetAction => get.getParameters.
            retain((k, v) => v != null && k != null).
            foreach { case (k, v) => builder.addTextBody(k.toString, v.toString) }
          case _ =>
        }
        upload match {
          case get: GetAction => get.getHeaders.
            retain((k, v) => v != null && k != null).
            foreach { case (k, v) => httpPost.addHeader(k.toString, v.toString) }
          case _ =>
        }
        val httpEntity = builder.build()
        httpPost.setEntity(httpEntity)
        response = httpClient.execute(httpPost)
      case post: POSTAction =>
        val httpPost = new HttpPost(realURL)
        if (post.getParameters.nonEmpty || post.getFormParams.nonEmpty) {
          val nvps = new util.ArrayList[NameValuePair]
          if (post.getParameters.nonEmpty) {
            post.getParameters.foreach { case (k, v) => nvps.add(new BasicNameValuePair(k, v.toString())) }
          }
          if (post.getFormParams.nonEmpty) {
            post.getFormParams.foreach { case (k, v) => nvps.add(new BasicNameValuePair(k, v.toString())) }
          }
          httpPost.setEntity(new UrlEncodedFormEntity(nvps))
        }

        if (StringUtils.isNotBlank(post.getRequestPayload)) {
          val stringEntity = new StringEntity(post.getRequestPayload, "UTF-8")
          stringEntity.setContentEncoding(Configuration.BDP_ENCODING.getValue)
          stringEntity.setContentType("application/json")
          httpPost.setEntity(stringEntity)
        }

        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => httpPost.addHeader(k.toString(), v.toString()) }
        }
        response = httpClient.execute(httpPost)
      case get: GetAction =>
        val builder = new URIBuilder(realURL)
        if (!get.getParameters.isEmpty) {
          get.getParameters.foreach { case (k, v) => builder.addParameter(k.toString(), v.toString()) }
        }
        val httpGet = new HttpGet(builder.build())
        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => httpGet.addHeader(k.toString(), v.toString()) }
        }
        response = httpClient.execute(httpGet);
      case _ =>
        val httpost = new HttpPost(realURL)
        val stringEntity = new StringEntity(requestAction.getRequestBody, "UTF-8")
        stringEntity.setContentEncoding(Configuration.BDP_ENCODING.getValue)
        stringEntity.setContentType("application/json")
        httpost.setEntity(stringEntity)
        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => httpost.addHeader(k.toString(), v.toString()) }
        }
        response = httpClient.execute(httpost)
    }
    response
  }

  protected def getFsByUser(user: String, path: FsPath): Fs

  protected def responseToResult(response: HttpResponse, requestAction: Action): Result = {
    var entity = response.getEntity
    val result = requestAction match {
      case download: DownloadAction =>
        val statusCode = response.getStatusLine.getStatusCode
        if (statusCode != 200) {
          var responseBody: String = null
          if (entity != null) {
            responseBody = EntityUtils.toString(entity, "UTF-8")
          }
          throw new HttpClientResultException(s"request failed! ResponseBody is $responseBody.")
        }
        download.write(entity.getContent)
        Result()
      case heartbeat: HeartbeatAction =>
        discovery.map {
          case d: AbstractDiscovery => d.getHeartbeatResult(response, heartbeat)
        }.getOrElse(throw new HttpMessageParseException("Discovery is not enable, HeartbeatAction is not needed!"))
      case auth: AuthenticationAction =>
        clientConfig.getAuthenticationStrategy match {
          case a: AbstractAuthenticationStrategy => a.getAuthenticationResult(response, auth)
          case _ => throw new HttpMessageParseException("AuthenticationStrategy is not enable, login is not needed!")
        }
      case httpAction: HttpAction =>
        var responseBody: String = null
        if (entity != null) {
          responseBody = EntityUtils.toString(entity, "UTF-8")
        }
        httpResponseToResult(response, httpAction, responseBody)
          .getOrElse(throw new HttpMessageParseException("cannot parse message: " + responseBody))
    }
    result match {
      case userAction: UserAction => requestAction match {
        case _userAction: UserAction => userAction.setUser(_userAction.getUser)
        case _ =>
      }
      case _ =>
    }
    result
  }

  protected def httpResponseToResult(response: HttpResponse, requestAction: HttpAction, responseBody: String): Option[Result]

  protected def deserializeResponseBody(response: HttpResponse): Iterable[_] = {
    var entity = response.getEntity
    var responseBody: String = null
    if (entity != null) {
      responseBody = EntityUtils.toString(entity, "UTF-8")
    }
    if (responseBody.startsWith("{") && responseBody.endsWith("}"))
      read[Map[String, Object]](responseBody)
    else if (responseBody.startsWith("[") && responseBody.endsWith("}"))
      read[List[Map[String, Object]]](responseBody)
    else if (StringUtils.isEmpty(responseBody)) Map.empty[String, Object]
    else if (responseBody.length > 200) throw new HttpException(responseBody.substring(0, 200))
    else throw new HttpException(responseBody)
  }

  override def close(): Unit = {
    discovery.foreach {
      case d: AbstractDiscovery => IOUtils.closeQuietly(d)
      case _ =>
    }
    httpClient.close()
    executors.asInstanceOf[ExecutionContextExecutorService].shutdown()
  }
}