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
 
package org.apache.linkis.httpclient.response.impl

import org.apache.linkis.httpclient.response.HttpResult

class DefaultHttpResult extends HttpResult {

  var responseBody: String = _
  var statusCode: Int = _
  var uri: String = _
  var contentType: String = _

  override def getContentType: String = contentType

  override def getUri: String = uri

  override def getStatusCode: Int = statusCode

  override def set(responseBody: String, statusCode: Int, url: String, contentType: String): Unit = {
    this.responseBody = responseBody
    this.statusCode = statusCode
    this.uri = url
    this.contentType = contentType
  }

  override def getResponseBody: String = responseBody
}
