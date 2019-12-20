/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.httpclient

import java.io.{File, InputStream}
import java.util.concurrent.TimeUnit

import com.ning.http.client.Response
import com.ning.http.multipart.{FilePart, PartSource, StringPart}
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
import dispatch._
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpException
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.collection.Iterable
import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext,ExecutionContextExecutorService, Future}

/**
  * Created by enjoyyin on 2019/5/20.
  */
abstract class AbstractHttpClient(clientConfig: ClientConfig, clientName: String) extends Client {

  protected implicit val formats: Formats = DefaultFormats
  protected implicit val executors: ExecutionContext = Utils.newCachedExecutionContext(clientConfig.getMaxConnection, clientName, false)
  protected val httpTimeout: Duration = if (clientConfig.getReadTimeout > 0) Duration(clientConfig.getReadTimeout, TimeUnit.MILLISECONDS)
    else Duration.Inf

  if(clientConfig.getAuthenticationStrategy != null) clientConfig.getAuthenticationStrategy match {
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
        else if(clientConfig.isLoadbalancerEnabled) Some(DefaultLoadbalancerStrategy.createLoadBalancer())
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
    val req = prepareReq(action)
    val response = if(!clientConfig.isRetryEnabled) executeRequest(req, Some(waitTime).filter(_ > 0))
      else clientConfig.getRetryHandler.retry(executeRequest(req, Some(waitTime).filter(_ > 0)), action.getClass.getSimpleName + "HttpRequest")
    responseToResult(response, action)
  }

  override def execute(requestAction: Action, resultListener: ResultListener): Unit = {
    if(!requestAction.isInstanceOf[HttpAction])
      throw new UnsupportedOperationException("only HttpAction supported, but the fact is " + requestAction.getClass)
    val action = prepareAction(requestAction.asInstanceOf[HttpAction])
    val req = prepareReq(action)
    val response = executeAsyncRequest(req)
    response.onSuccess{case r => resultListener.onSuccess(responseToResult(r, action))}
    response.onFailure{case t => resultListener.onFailure(t)}
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

  protected def prepareReq(requestAction: HttpAction): Req = {
    var realURL = ""
    var req = requestAction match {
      case serverUrlAction: ServerUrlAction =>
        realURL = serverUrlAction.serverUrl
        dispatch.url(connectUrl(serverUrlAction.serverUrl, requestAction.getURL))
      case _ =>
        val url = getRequestUrl(requestAction.getURL, requestAction.getRequestBody)
        realURL = url.replaceAll(requestAction.getURL, "")
        dispatch.url(url)
    }
    var request = requestAction match {
      case upload: UploadAction =>
        req = req.setContentType("multipart/form-data", Configuration.BDP_ENCODING.getValue).POST
        if(upload.files != null && upload.files.nonEmpty) {
          val fs = upload.user.map(getFsByUser(_, new FsPath(upload.files.head._2)))
          upload.files.foreach { case (k, v) =>
            if(StringUtils.isEmpty(v)) throw new HttpException(s"$k 的文件路径不能为空！")
            val filePart = fs.map(f => if(f.exists(new FsPath(v))) new FilePart(k, new FsPartSource(f, v))
              else throw new HttpException(s"File $v 不存在！")).getOrElse{
                val f = new File(v)
                if(!f.exists() || !f.isFile) throw new HttpException(s"File $v 不存在！")
                else new FilePart(k, f)
            }
            req = req.addBodyPart(filePart)
          }
        }
        if(upload.inputStreams != null)
          upload.inputStreams.foreach { case (k, v) =>
            val filePart = new FilePart(k, new PartSource{
              val length = v.available
              override def getLength: Long = length
              override def getFileName: String = upload.inputStreamNames.getOrDefault(k, k)
              override def createInputStream(): InputStream = v
            })
            req = req.addBodyPart(filePart)
          }
        upload match {
          case get: GetAction => get.getParameters.foreach { case (k, v) => req = req.addBodyPart(new StringPart(k, v.toString)) }
          case _ =>
        }
        req
      case post: POSTAction =>
        req = req.POST
        if(!post.getParameters.isEmpty) post.getParameters.foreach{ case (k, v) => req = req.addQueryParameter(k, v.toString)}
        if(post.getFormParams.nonEmpty) {
          req.setContentType("application/x-www-form-urlencoded", Configuration.BDP_ENCODING.getValue) << post.getFormParams
        } else req.setContentType("application/json", Configuration.BDP_ENCODING.getValue) << post.getRequestPayload
      case get: GetAction =>
        if(!get.getParameters.isEmpty) get.getParameters.foreach{ case (k, v) => req = req.addQueryParameter(k, v.toString)}
        req.GET
      case _ =>
        req.POST.setBody(requestAction.getRequestBody)
          .setContentType("application/json", Configuration.BDP_ENCODING.getValue)
    }
    if(clientConfig.getAuthenticationStrategy != null) clientConfig.getAuthenticationStrategy.login(requestAction, realURL) match {
      case authAction: HttpAuthentication =>
        val cookies = authAction.authToCookies
        if(cookies != null && cookies.nonEmpty) cookies.foreach(requestAction.addCookie)
        val headers = authAction.authToHeaders
        if(headers != null && headers.nonEmpty) headers.foreach{case (k, v) => requestAction.addHeader(k, v.toString)}
      case _ =>
    }
    if(requestAction.getHeaders.nonEmpty) request = request <:< requestAction.getHeaders
    if(requestAction.getCookies.nonEmpty) requestAction.getCookies.foreach(c => request = request.addCookie(c))
    request
  }

  protected def getFsByUser(user: String, path: FsPath): Fs

  protected def executeRequest(req: Req, waitTime: Option[Long]): Response = {
    val response = executeAsyncRequest(req)
    Await.result(response, Duration(waitTime.getOrElse(clientConfig.getReadTimeout), TimeUnit.MILLISECONDS))
  }

  protected def executeAsyncRequest(req: Req): Future[Response] = Http(req > as.Response(r => r))

  protected def responseToResult(response: Response, requestAction: Action): Result = {
    val result = requestAction match {
      case download: DownloadAction =>
        val statusCode = response.getStatusCode
        if(statusCode != 200) {
           val responseBody = response.getResponseBody
           val url = response.getUri.toString
           throw new HttpClientResultException(s"URL $url request failed! ResponseBody is $responseBody." )
        }
        download.write(response.getResponseBodyAsStream)
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
        httpResponseToResult(response, httpAction)
          .getOrElse(throw new HttpMessageParseException("cannot parse message: " + response.getResponseBody))
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

  protected def httpResponseToResult(response: Response, requestAction: HttpAction): Option[Result]

  protected def deserializeResponseBody(response: Response): Iterable[_] = {
    val responseBody = response.getResponseBody
    if(responseBody.startsWith("{") && responseBody.endsWith("}"))
      read[Map[String, Object]](responseBody)
    else if(responseBody.startsWith("[") && responseBody.endsWith("}"))
      read[List[Map[String, Object]]](responseBody)
    else if(StringUtils.isEmpty(responseBody)) Map.empty[String, Object]
    else if(responseBody.length > 200) throw new HttpException(responseBody.substring(0, 200))
    else throw new HttpException(responseBody)
  }

  override def close(): Unit = {
    discovery.foreach{
      case d: AbstractDiscovery => IOUtils.closeQuietly(d)
      case _ =>
    }
    dispatch.Http.shutdown()
    executors.asInstanceOf[ExecutionContextExecutorService].shutdown()
  }
}

class FsPartSource (fs: Fs, path: String) extends PartSource {
  val fsPath = fs.get(path)
  override def getLength: Long = fsPath.getLength

  override def createInputStream(): InputStream = fs.read(fsPath)

  override def getFileName: String = fsPath.toFile.getName
}