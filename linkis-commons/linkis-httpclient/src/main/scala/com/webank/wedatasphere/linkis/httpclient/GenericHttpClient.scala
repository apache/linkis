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

import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath}
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig
import com.webank.wedatasphere.linkis.httpclient.discovery.Discovery
import com.webank.wedatasphere.linkis.httpclient.exception.HttpMethodNotSupportException
import com.webank.wedatasphere.linkis.httpclient.request.HttpAction
import com.webank.wedatasphere.linkis.httpclient.response.{HashMapHttpResult, Result}
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
