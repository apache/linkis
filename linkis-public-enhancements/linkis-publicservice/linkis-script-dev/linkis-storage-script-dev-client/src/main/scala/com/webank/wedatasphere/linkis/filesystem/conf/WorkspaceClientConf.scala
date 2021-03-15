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
package com.webank.wedatasphere.linkis.filesystem.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars


/**
  * Created by patinousward
  */
object WorkspaceClientConf {

  val gateway: String =
    CommonVars[String]("wds.linkis.gateway.address", "").getValue

  val prefix: String =
    CommonVars[String]("wds.linkis.filesystem.prefixutl", "/api/rest_j/v1/filesystem").getValue

  val scriptFromBML: String =
    CommonVars[String]("wds.linkis.filesystem.scriptFromBML", "/openScriptFromBML").getValue

  val dwsVersion: String =
    CommonVars[String]("wds.linkis.bml.dws.version", "v1").getValue

  val tokenKey: String = CommonVars[String]("wds.linkis.filesystem.token.key", "Validation-Code").getValue

  val tokenValue: String = CommonVars[String]("wds.linkis.filesystem.token.value", "WS-AUTH").getValue

  val scriptFromBMLUrl: String =prefix + scriptFromBML
}
