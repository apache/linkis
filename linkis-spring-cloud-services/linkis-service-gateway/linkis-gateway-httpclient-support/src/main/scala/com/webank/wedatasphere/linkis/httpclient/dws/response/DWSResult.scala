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

package com.webank.wedatasphere.linkis.httpclient.dws.response

import java.util

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.exception.HttpClientResultException
import com.webank.wedatasphere.linkis.httpclient.response.HttpResult

trait DWSResult extends Logging with HttpResult {

  private var resultMap: util.Map[String, Object] = _
  private var responseBody: String = _
  private var statusCode: Int = _
  private var url: String = _
  private var contentType: String = _

  private var status: Int = -1
  private var message: String = _
  private var data: util.Map[String, Object] = _

  def getResultMap: util.Map[String, Object] = resultMap

  def getStatus: Int = status

  def getMessage: String = message

  def getData: util.Map[String, Object] = data

  override def getContentType: String = contentType

  override def getUri: String = url

  override def getStatusCode: Int = statusCode

  override def set(responseBody: String, statusCode: Int, url: String, contentType: String): Unit = {
    if(statusCode != 200) throw new HttpClientResultException(s"URL $url request failed! ResponseBody is $responseBody." )
    resultMap = DWSHttpClient.jacksonJson.readValue(responseBody, classOf[util.Map[String, Object]])
    status = resultMap.get("status").asInstanceOf[Int]
    if(status != 0) throw new HttpClientResultException(s"URL $url request failed! ResponseBody is $responseBody." )
    message = getResultMap.get("message").asInstanceOf[String]
    data = getResultMap.get("data").asInstanceOf[util.Map[String, Object]]
    this.responseBody = responseBody
    this.statusCode = statusCode
    this.url = url
    this.contentType = contentType
  }

  override def getResponseBody: String = responseBody
}
