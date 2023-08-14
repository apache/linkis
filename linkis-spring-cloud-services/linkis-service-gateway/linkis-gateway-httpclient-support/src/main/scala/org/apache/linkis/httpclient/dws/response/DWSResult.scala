/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.httpclient.dws.response

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.httpclient.dws.DWSHttpClient
import org.apache.linkis.httpclient.dws.response.DWSResult.LOGGEDIN_STR
import org.apache.linkis.httpclient.exception.HttpClientResultException
import org.apache.linkis.httpclient.response.HttpResult

import org.apache.commons.lang3.StringUtils

import java.util

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

  override def set(
      responseBody: String,
      statusCode: Int,
      url: String,
      contentType: String
  ): Unit = {
//    if(statusCode != 200) throw new HttpClientResultException(s"URL $url request failed! ResponseBody is $responseBody." )
    var newStatusCode = statusCode
    Utils.tryCatch {
      resultMap =
        DWSHttpClient.jacksonJson.readValue(responseBody, classOf[util.Map[String, Object]])
      status = resultMap.get("status").asInstanceOf[Int]
      message = getResultMap.get("message").asInstanceOf[String]
      if (status != 0) {
        if (StringUtils.isBlank(message) || !message.contains(LOGGEDIN_STR)) {
          throw new HttpClientResultException(
            s"URL $url request failed! ResponseBody is $responseBody."
          )
        }
        this.status = 0
        newStatusCode = 200
      }
      data = getResultMap.get("data").asInstanceOf[util.Map[String, Object]]
      this.responseBody = responseBody
      this.statusCode = newStatusCode
      this.url = url
      this.contentType = contentType
    } { case e: Exception =>
      throw new HttpClientResultException(
        s"URL $url request failed! ResponseBody is $responseBody. ${e.getMessage}"
      )
    }

  }

  override def getResponseBody: String = responseBody
}

object DWSResult {

  lazy val LOGGEDIN_STR = CommonVars(
    "wds.linkis.httpclient.default.logged_in.str",
    "Already logged in, please log out"
  ).getValue

}
