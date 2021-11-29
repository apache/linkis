/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.httpclient

import java.util

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.httpclient.authentication.{AbstractAuthenticationStrategy, AuthenticationAction, HttpAuthentication}
import org.apache.linkis.httpclient.config.ClientConfig
import org.apache.linkis.httpclient.discovery.{AbstractDiscovery, Discovery, HeartbeatAction}
import org.apache.linkis.httpclient.exception.{HttpClientResultException, HttpMessageParseException, HttpMethodNotSupportException}
import org.apache.linkis.httpclient.loadbalancer.{AbstractLoadBalancer, DefaultLoadbalancerStrategy, LoadBalancer}
import org.apache.linkis.httpclient.request._
import org.apache.linkis.httpclient.response._
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.http.client.CookieStore
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.{DeflateDecompressingEntity, GzipDecompressingEntity, UrlEncodedFormEntity}
import org.apache.http.client.methods._
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{BasicCookieStore, CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpResponse, _}

import scala.collection.JavaConversions._



abstract class AbstractHttpClient(clientConfig: ClientConfig, clientName: String) extends Client {

  protected val CONNECT_TIME_OUT = 50000

  protected val cookieStore = new BasicCookieStore
  protected val httpClient: CloseableHttpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build

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
    if (!requestAction.isInstanceOf[HttpAction])
      throw new UnsupportedOperationException("only HttpAction supported, but the fact is " + requestAction.getClass)
    val action = prepareAction(requestAction.asInstanceOf[HttpAction])
    val startTime = System.currentTimeMillis
    val req = prepareReq(action)
    val prepareReqTime = System.currentTimeMillis - startTime
    prepareCookie(action)
    val attempts = new util.ArrayList[Long]()
    def addAttempt(): CloseableHttpResponse = {
      val startTime = System.currentTimeMillis
      val response = executeRequest(req, Some(waitTime).filter(_ > 0))
      attempts.add(System.currentTimeMillis - startTime)
      response
    }
    val response = if (!clientConfig.isRetryEnabled) addAttempt()
    else clientConfig.getRetryHandler.retry(addAttempt(), action.getClass.getSimpleName + "HttpRequest")
    val beforeDeserializeTime = System.currentTimeMillis
    responseToResult(response, action) match {
      case metricResult: MetricResult =>
        if(metricResult.getMetric == null) metricResult.setMetric(new HttpMetric)
        metricResult.getMetric.setPrepareReqTime(prepareReqTime)
        metricResult.getMetric.addRetries(attempts)
        metricResult.getMetric.setDeserializeTime(System.currentTimeMillis - beforeDeserializeTime)
        metricResult.getMetric.setExecuteTotalTime(System.currentTimeMillis - startTime)
        metricResult
      case result: Result => result
    }
  }


  override def execute(requestAction: Action, resultListener: ResultListener): Unit = {
    throw new HttpMethodNotSupportException("Not supported client method!")
  }

  protected def getRequestUrl(suffixUrl: String, requestBody: String): String = {
    val urlPrefix = loadBalancer.map(_.chooseServerUrl(requestBody)).getOrElse(clientConfig.getServerUrl)
    if(suffixUrl.contains(urlPrefix)) suffixUrl else connectUrl(urlPrefix, suffixUrl)
  }

  protected def connectUrl(prefix: String, suffix: String): String = {
    val prefixEnd = prefix.endsWith("/")
    val suffixStart = suffix.startsWith("/")
    if (prefixEnd && suffixStart) prefix.substring(0, prefix.length - 1) + suffix
    else if (!prefixEnd && !suffixStart) prefix + "/" + suffix
    else prefix + suffix
  }

  protected def prepareAction(requestAction: HttpAction): HttpAction = requestAction

  protected def prepareCookie(requestAction:HttpAction): Unit = if(requestAction.getCookies.nonEmpty) {
    requestAction.getCookies.foreach(cookieStore.addCookie)
  }


  protected def prepareReq(requestAction: HttpAction): HttpRequestBase = {
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
        if (headers != null && !headers.isEmpty) {
          headers.foreach { case (k, v) => if(k != null && v != null) requestAction.addHeader(k.toString, v.toString) }
        }
      case _ =>
    }

    val request = requestAction match {
      case delete: DeleteAction =>
        val builder = new URIBuilder(realURL)
        if (!delete.getParameters.isEmpty) {
          delete.getParameters.foreach { case (k, v) => if(k != null && v != null) builder.addParameter(k.toString, v.toString) }
        }
        val httpDelete = new HttpDelete(builder.build())
        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => if(k != null && v != null) httpDelete.addHeader(k.toString, v.toString) }
        }
        httpDelete
      case put: PutAction =>
        val httpPut = new HttpPut(realURL)
        if (put.getParameters.nonEmpty || put.getFormParams.nonEmpty) {
          val nameValuePairs = new util.ArrayList[NameValuePair]
          if (put.getParameters.nonEmpty) {
            put.getParameters.foreach { case (k, v) => if(v != null) nameValuePairs.add(new BasicNameValuePair(k, v.toString)) }
          }
          if (put.getFormParams.nonEmpty) {
            put.getFormParams.foreach { case (k, v) => if(v != null) nameValuePairs.add(new BasicNameValuePair(k, v.toString)) }
          }
          httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs))
        }

        if (StringUtils.isNotBlank(put.getRequestPayload)) {
          val stringEntity = new StringEntity(put.getRequestPayload, "UTF-8")
          stringEntity.setContentEncoding(Configuration.BDP_ENCODING.getValue)
          stringEntity.setContentType("application/json")
          httpPut.setEntity(stringEntity)
        }

        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => if(k != null && v != null) httpPut.addHeader(k.toString, v.toString) }
        }
        httpPut
      case upload: UploadAction =>
        val httpPost = new HttpPost(realURL)
        val builder = MultipartEntityBuilder.create()
        if (upload.inputStreams != null)
          upload.inputStreams.foreach { case (k, v) =>
            builder.addBinaryBody(k, v, ContentType.create("multipart/form-data"), k)
          }
        upload.binaryBodies.foreach(binaryBody => builder.addBinaryBody(binaryBody.parameterName, binaryBody.inputStream, binaryBody.contentType, binaryBody.fileName))
        upload match {
          case get: GetAction => get.getParameters.
            retain((k, v) => v != null && k != null).
            foreach { case (k, v) => if(k != null && v != null) builder.addTextBody(k.toString, v.toString) }
          case _ =>
        }
        upload match {
          case get: GetAction => get.getHeaders.
            retain((k, v) => v != null && k != null).
            foreach { case (k, v) => if(k != null && v != null) httpPost.addHeader(k.toString, v.toString) }
          case _ =>
        }
        val httpEntity = builder.build()
        httpPost.setEntity(httpEntity)
        httpPost
      case post: POSTAction =>
        val httpPost = new HttpPost(realURL)
        if (post.getParameters.nonEmpty || post.getFormParams.nonEmpty) {
          val nvps = new util.ArrayList[NameValuePair]
          if (post.getParameters.nonEmpty) {
            post.getParameters.foreach { case (k, v) => if(v != null) nvps.add(new BasicNameValuePair(k, v.toString)) }
          }
          if (post.getFormParams.nonEmpty) {
            post.getFormParams.foreach { case (k, v) => if(v != null) nvps.add(new BasicNameValuePair(k, v.toString)) }
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
          requestAction.getHeaders.foreach { case (k, v) => if(k != null && v != null) httpPost.addHeader(k.toString, v.toString) }
        }
        httpPost
      case get: GetAction =>
        val builder = new URIBuilder(realURL)
        if (!get.getParameters.isEmpty) {
          get.getParameters.foreach { case (k, v) => if(k != null && v != null) builder.addParameter(k.toString, v.toString) }
        }
        val httpGet = new HttpGet(builder.build())
        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => if(k != null && v != null) httpGet.addHeader(k.toString, v.toString) }
        }
        httpGet
      case _ =>
        val httpost = new HttpPost(realURL)
        val stringEntity = new StringEntity(requestAction.getRequestBody, "UTF-8")
        stringEntity.setContentEncoding(Configuration.BDP_ENCODING.getValue)
        stringEntity.setContentType("application/json")
        httpost.setEntity(stringEntity)
        if (requestAction.getHeaders.nonEmpty) {
          requestAction.getHeaders.foreach { case (k, v) => if(k != null && v != null) httpost.addHeader(k.toString, v.toString) }
        }
        httpost
    }
    request
  }

  protected def getFsByUser(user: String, path: FsPath): Fs


  protected def executeRequest(req: HttpRequestBase, waitTime: Option[Long]): CloseableHttpResponse = {
    val readTimeOut = waitTime.getOrElse(clientConfig.getReadTimeout)
    val connectTimeOut = if (clientConfig.getConnectTimeout > 1000 || clientConfig.getConnectTimeout < 0) clientConfig.getConnectTimeout else CONNECT_TIME_OUT
    val requestConfig = RequestConfig.custom
      .setConnectTimeout(connectTimeOut.toInt)
      .setConnectionRequestTimeout(connectTimeOut.toInt)
      .setSocketTimeout(readTimeOut.toInt).build
    req.setConfig(requestConfig)
    val response = httpClient.execute(req)
    response
  }

  protected def executeRequest(req: HttpRequestBase, waitTime: Option[Long], cookieStore: CookieStore): CloseableHttpResponse = {
    val readTimeOut = waitTime.getOrElse(clientConfig.getReadTimeout)
    val connectTimeOut = if (clientConfig.getConnectTimeout > 1000 || clientConfig.getConnectTimeout < 0) clientConfig.getConnectTimeout else CONNECT_TIME_OUT
    val requestConfig = RequestConfig.custom
      .setConnectTimeout(connectTimeOut.toInt)
      .setConnectionRequestTimeout(connectTimeOut.toInt)
      .setSocketTimeout(readTimeOut.toInt).build
    req.setConfig(requestConfig)
    val response = httpClient.execute(req)
    response
  }

  protected def responseToResult(response: HttpResponse, requestAction: Action): Result = {
    val entity = response.getEntity
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
        val inputStream = if(entity.getContentEncoding != null && StringUtils.isNotBlank(entity.getContentEncoding.getValue))
          entity.getContentEncoding.getValue.toLowerCase match {
            case "gzip" => new GzipDecompressingEntity(entity).getContent
            case "deflate" => new DeflateDecompressingEntity(entity).getContent
            case str => throw new HttpClientResultException(s"request failed! Reason: not support decompress type $str.")
          } else entity.getContent
        download.write(inputStream)
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

  override def close(): Unit = {
    discovery.foreach {
      case d: AbstractDiscovery => IOUtils.closeQuietly(d)
      case _ =>
    }
    httpClient.close()
  }
}