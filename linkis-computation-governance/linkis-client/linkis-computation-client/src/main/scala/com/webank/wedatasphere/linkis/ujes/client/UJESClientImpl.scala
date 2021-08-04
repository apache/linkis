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

package com.webank.wedatasphere.linkis.ujes.client
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import com.webank.wedatasphere.linkis.httpclient.request.Action
import com.webank.wedatasphere.linkis.httpclient.response.Result
import com.webank.wedatasphere.linkis.ujes.client.request.UJESJobAction

class UJESClientImpl(clientConfig: DWSClientConfig) extends UJESClient {
  private val dwsHttpClient = new DWSHttpClient(clientConfig, "UJES-Job-Execution-Thread")
  override protected[client] def executeUJESJob(ujesJobAction: UJESJobAction): Result = ujesJobAction match {
    case action: Action => dwsHttpClient.execute(action)
  }
  override def close(): Unit = dwsHttpClient.close()
}