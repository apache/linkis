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

/*
 * created by cooperyang on 2019/07/24.
 */

package com.webank.wedatasphere.linkis.httpclient.dws

import java.text.SimpleDateFormat
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.httpclient.AbstractHttpClient
import com.webank.wedatasphere.linkis.httpclient.discovery.Discovery
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import com.webank.wedatasphere.linkis.httpclient.dws.discovery.DWSGatewayDiscovery
import com.webank.wedatasphere.linkis.httpclient.dws.request.DWSHttpAction
import com.webank.wedatasphere.linkis.httpclient.dws.response.{DWSHttpMessageFactory, DWSHttpMessageResultInfo, DWSResult}
import com.webank.wedatasphere.linkis.httpclient.request.HttpAction
import com.webank.wedatasphere.linkis.httpclient.response.{HttpResult, ListResult, Result}
import com.webank.wedatasphere.linkis.storage.FSFactory
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.lang.ClassUtils
import org.apache.http.HttpResponse

import scala.collection.JavaConversions.mapAsJavaMap

/**
  * created by cooperyang on 2019/5/20.
  */
class DWSHttpClient(clientConfig: DWSClientConfig, clientName: String)
  extends AbstractHttpClient(clientConfig, clientName) {

  override protected def createDiscovery(): Discovery = new DWSGatewayDiscovery


  override protected def prepareAction(requestAction: HttpAction): HttpAction = {
    requestAction match {
      case dwsAction: DWSHttpAction => dwsAction.setDWSVersion(clientConfig.getDWSVersion)
      case _ =>
    }
    requestAction
  }

  override protected def httpResponseToResult(response: HttpResponse, requestAction: HttpAction, responseBody: String): Option[Result] = {
    var entity = response.getEntity
    val statusCode: Int = response.getStatusLine.getStatusCode
    val url: String = requestAction.getURL
    val contentType: String = entity.getContentType.getValue
    DWSHttpMessageFactory.getDWSHttpMessageResult(url).map { case DWSHttpMessageResultInfo(_, clazz) =>
      clazz match {
        case c if ClassUtils.isAssignable(c, classOf[DWSResult]) =>
          val dwsResult = clazz.getConstructor().newInstance().asInstanceOf[DWSResult]
          dwsResult.set(responseBody, statusCode, url, contentType)
          BeanUtils.populate(dwsResult, dwsResult.getData)
          return Some(dwsResult)
        case _ =>
      }

      def transfer(value: Result, map: Map[String, Object]): Unit = {
        value match {
          case httpResult: HttpResult =>
            httpResult.set(responseBody, statusCode, url, contentType)
          case _ =>
        }
        val javaMap = mapAsJavaMap(map)
        BeanUtils.populate(value, javaMap)
        fillResultFields(javaMap, value)
      }
      deserializeResponseBody(response) match {
        case map: Map[String, Object] =>
          val value = clazz.getConstructor().newInstance().asInstanceOf[Result]
          transfer(value, map)
          value
        case list: List[Map[String, Object]] =>
          val results = list.map { map =>
            val value = clazz.getConstructor().newInstance().asInstanceOf[Result]
            transfer(value, map)
            value
          }.toArray
          new ListResult(responseBody, results)
      }
    }.orElse(nonDWSResponseToResult(response, requestAction))
  }

  protected def nonDWSResponseToResult(response: HttpResponse, requestAction: HttpAction): Option[Result] = None

  protected def fillResultFields(responseMap: util.Map[String, Object], value: Result): Unit = {}

  //TODO Consistent with workspace, plus expiration time(与workspace保持一致，加上过期时间)
  override protected def getFsByUser(user: String, path: FsPath): Fs = FSFactory.getFsByProxyUser(path, user)

}
object DWSHttpClient {
  val jacksonJson = new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))
}