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
 
package org.apache.linkis.httpclient.response

import java.util

import org.apache.linkis.common.utils.JsonUtils
import org.apache.linkis.httpclient.exception.HttpClientResultException


class HashMapHttpResult extends HttpResult {

  private var resultMap: util.Map[String, Object] = _
  private var responseBody: String = _
  private var statusCode: Int = _
  private var url: String = _
  private var contentType: String = _

  override def getContentType: String = contentType

  override def getUri: String = url

  override def getStatusCode: Int = statusCode

  def getResultMap: util.Map[String, Object] = resultMap

  override def set(responseBody: String, statusCode: Int, url: String, contentType: String): Unit = {
    if(statusCode != 200) throw new HttpClientResultException(s"URL $url request failed! ResponseBody is $responseBody." )
    resultMap = JsonUtils.jackson.readValue(responseBody, classOf[util.Map[String, Object]])
    this.responseBody = responseBody
    this.statusCode = statusCode
    this.url = url
    this.contentType = contentType
  }

  override def getResponseBody: String = responseBody
}
