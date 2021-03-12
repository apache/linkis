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
package com.webank.wedatasphere.linkis.filesystem.request

import com.webank.wedatasphere.linkis.httpclient.authentication.AuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.config.ClientConfig
import com.webank.wedatasphere.linkis.httpclient.dws.DWSHttpClient
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by patinousward
  */
trait WorkspaceHttpConf {

  protected var user: String

  protected var token: String

  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  protected var authenticationStrategy: AuthenticationStrategy

  protected var clientConfig: ClientConfig

  protected var dwsClientConfig: DWSClientConfig

  protected var dwsClientName: String

  protected var dwsClient: DWSHttpClient

}
