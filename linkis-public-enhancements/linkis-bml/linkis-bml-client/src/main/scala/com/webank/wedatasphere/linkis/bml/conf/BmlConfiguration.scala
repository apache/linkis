/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.conf

import com.webank.wedatasphere.linkis.common.conf.CommonVars

object BmlConfiguration {


  val LINKIS_API_VERSION:CommonVars[String] = CommonVars[String]("wds.linkis.bml.api.version", "v1")

  val URL_PREFIX:CommonVars[String] =
    CommonVars[String]("wds.linkis.bml.url.prefix", "/api/rest_j/v1/bml", "bml服务的url前缀")

  val AUTH_TOKEN_KEY:CommonVars[String] = CommonVars[String]("wds.linkis.bml.auth.token.key", "Validation-Code")

  val AUTH_TOKEN_VALUE:CommonVars[String] = CommonVars[String]("wds.linkis.bml.auth.token.value", "BML-AUTH")


}
