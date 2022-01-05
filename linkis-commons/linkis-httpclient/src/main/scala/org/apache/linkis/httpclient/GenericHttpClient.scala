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

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.httpclient.config.ClientConfig
import org.apache.linkis.httpclient.discovery.Discovery
import org.apache.linkis.httpclient.exception.HttpMethodNotSupportException
import org.apache.linkis.httpclient.request.HttpAction
import org.apache.linkis.httpclient.response.{HashMapHttpResult, Result}
import org.apache.http.HttpResponse


class GenericHttpClient(clientConfig: ClientConfig, clientName: String) extends AbstractHttpClient(clientConfig, clientName) {

  override protected def createDiscovery(): Discovery = throw new HttpMethodNotSupportException("GenericHttpClient not support discovery.")

  override protected def httpResponseToResult(response: HttpResponse, requestAction: HttpAction, responseBody: String): Option[Result] = {
    val result = new HashMapHttpResult
    result.set(responseBody, response.getStatusLine.getStatusCode, requestAction.getURL, response.getEntity.getContentType.getValue)
    Some(result)
  }

  override protected def getFsByUser(user: String, path: FsPath): Fs = {
    null
  }
}
