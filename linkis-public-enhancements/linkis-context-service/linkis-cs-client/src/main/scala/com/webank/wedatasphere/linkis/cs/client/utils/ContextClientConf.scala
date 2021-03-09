/**
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
package com.webank.wedatasphere.linkis.cs.client.utils

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
 * created by cooperyang on 2020/2/18
 * Description:
 */
object ContextClientConf {
  val LINKIS_WEB_VERSION:CommonVars[String] = CommonVars[String]("wds.linkis.web.version", "v1")


  val CONTEXT_CLIENT_AUTH_KEY:CommonVars[String] = CommonVars[String]("wds.linkis.context.client.auth.key", "Token-Code")

  val CONTEXT_CLIENT_AUTH_VALUE:CommonVars[String] = CommonVars[String]("wds.linkis.context.client.auth.value", "BML-AUTH")


  val URL_PREFIX:CommonVars[String] = CommonVars[String]("wds.linkis.cs.url.prefix", "/api/rest_j/v1/contextservice", "cs服务的url前缀")

  val CREATE_CONTEXT_URL:CommonVars[String] = CommonVars[String]("wds.linkis.cs.createcontext.url", "createContextID")

  val SET_VALUE_BY_KEY_URL:CommonVars[String] = CommonVars[String]("wds.linkis.cs.setvaluebykey.url", "setValueByKey")

  val SET_KEYVALUE_URL:CommonVars[String] = CommonVars[String]("wds.linkis.cs.setkeyvalue.url", "setValue")

  val RESET_KEYVALUE_URL:CommonVars[String] = CommonVars[String]("wds.linkis.cs.resetkeyvalue.url", "resetValue")

  val REMOVE_VALUE_URL:CommonVars[String] = CommonVars[String]("wds.linkis.cs.removeValue.url", "removeValue")

  val RESET_CONTEXT_ID_URL:CommonVars[String] = CommonVars[String]("wds.linkis.cs.reset.contextid.url", "removeAllValue")

  val HEART_BEAT_ENABLED:CommonVars[String] = CommonVars[String]("wds.linkis.cs.heartbeat.enabled", "true")

}
