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

package com.webank.wedatasphere.linkis.gateway.config

import com.webank.wedatasphere.linkis.common.conf.CommonVars

/**
  * created by cooperyang on 2019/5/22.
  */
object GatewayConfiguration {

  val ENABLE_PROXY_USER = CommonVars("wds.linkis.gateway.conf.enable.proxy.user", false)
  val PROXY_USER_CONFIG = CommonVars("wds.linkis.gateway.conf.proxy.user.config", "proxy.properties")
  val PROXY_USER_SCAN_INTERVAL = CommonVars("wds.linkis.gateway.conf.proxy.user.scan.interval", 1000 * 60 * 10)

  val TOKEN_KEY = "Token-Code"
  val TOKEN_USER_KEY = "Token-User"
  val ENABLE_TOKEN_AUTHENTICATION = CommonVars("wds.linkis.gateway.conf.enable.token.auth", false)
  val TOKEN_AUTHENTICATION_CONFIG = CommonVars("wds.linkis.gateway.conf.token.auth.config", "token.properties")
  val TOKEN_AUTHENTICATION_SCAN_INTERVAL = CommonVars("wds.linkis.gateway.conf.token.auth.scan.interval", 1000 * 60 * 10)

  val PASS_AUTH_REQUEST_URI = CommonVars("wds.linkis.gateway.conf.url.pass.auth", "/dws/").getValue.split(",")

  val ENABLE_SSO_LOGIN = CommonVars("wds.linkis.gateway.conf.enable.sso", false)
  val SSO_INTERCEPTOR_CLASS = CommonVars("wds.linkis.gateway.conf.sso.interceptor", "")

  val ADMIN_USER = CommonVars("wds.linkis.gateway.admin.user", "hadoop")

  val USERCONTROL_SWITCH_ON = CommonVars("wds.linkis.gateway.usercontrol_switch_on", false)

//  val PROXY_USER_LIST = CommonVars("wds.linkis.gateway.conf.proxy.user.list", "").getValue.split(",")

  /**
    * QUERY_PERSISTENCE_SPRING_APPLICATION_NAME is the name of the application that represents the query module in springcloud
    * QUERY_PERSISTENCE_SPRING_APPLICATION_NAME 是表示usercontrol模块在springcloud中的应用名称
    */
  val USERCONTROL_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.query.application.name", "cloud-usercontrol")
}
