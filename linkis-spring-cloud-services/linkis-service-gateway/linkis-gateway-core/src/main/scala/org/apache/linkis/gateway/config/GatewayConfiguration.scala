/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.gateway.config

import org.apache.linkis.common.conf.CommonVars

object GatewayConfiguration {

  val ENABLE_PROXY_USER = CommonVars("wds.linkis.gateway.conf.enable.proxy.user", false)

  val PROXY_USER_CONFIG =
    CommonVars("wds.linkis.gateway.conf.proxy.user.config", "proxy.properties")

  val PROXY_USER_SCAN_INTERVAL =
    CommonVars("wds.linkis.gateway.conf.proxy.user.scan.interval", 1000 * 60 * 10)

  val TOKEN_KEY = "Token-Code"
  val TOKEN_USER_KEY = "Token-User"
  val TOKEN_ALIVE_KEY = "Token-Alive"
  val TOKEN_ALIVE_TRUE = "true"
  val ENABLE_TOKEN_AUTHENTICATION = CommonVars("wds.linkis.gateway.conf.enable.token.auth", false)

  val ENABLE_TOEKN_AUTHENTICATION_ALIVE =
    CommonVars("wds.linkis.gateway.auth.token.alive.enable", false)

  val TOKEN_AUTHENTICATION_SCAN_INTERVAL =
    CommonVars("wds.linkis.gateway.conf.token.auth.scan.interval", 1000 * 60 * 10)

  val PASS_AUTH_REQUEST_URI =
    CommonVars("wds.linkis.gateway.conf.url.pass.auth", "/dws/").getValue.split(",")

  val ENABLE_SSO_LOGIN = CommonVars("wds.linkis.gateway.conf.enable.sso", false)
  val SSO_INTERCEPTOR_CLASS = CommonVars("wds.linkis.gateway.conf.sso.interceptor", "")

  val ADMIN_USER = CommonVars("wds.linkis.admin.user", "hadoop")

  val ADMIN_PASSWORD = CommonVars("wds.linkis.admin.password", ADMIN_USER.getValue)

  val USERCONTROL_SWITCH_ON = CommonVars("wds.linkis.gateway.usercontrol_switch_on", false)

  val PUBLICKEY_GATEWAY_URL =
    CommonVars("wds.linkis.gateway.publickey.url", "http://127.0.0.1:8088/")

  val PROXY_USER_LIST =
    CommonVars("wds.linkis.gateway.conf.proxy.user.list", "").getValue.split(",")

  val USERCONTROL_SPRING_APPLICATION_NAME =
    CommonVars("wds.linkis.usercontrol.application.name", "cloud-usercontrol")

  val LOGIN_ENCRYPT_ENABLE = CommonVars("wds.linkis.login_encrypt.enable", false)

  val ENTRANCE_SPRING_NAME = CommonVars("wds.linkis.entrance.name", "linkis-cg-entrance")

  val ENABLE_GATEWAY_AUTH = CommonVars("wds.linkis.enable.gateway.auth", false)

  val AUTH_IP_FILE = CommonVars("wds.linkis.gateway.auth.file", "auth.txt")
  val DEFAULT_GATEWAY_ACCESS_TOKEN = CommonVars("wds.linkis.gateway.access.token", "WS-AUTH")

  val CONTROL_WORKSPACE_ID_LIST = CommonVars("wds.linkis.gateway.control.workspace.ids", "224")

  val DSS_QUERY_WORKSPACE_SERVICE_NAME =
    CommonVars("wds.dss.query.workspace.service", "dss-framework-project-server")

  val USER_WORKSPACE_REFLESH_TIME = CommonVars("wds.linkis.user.workspace.reflesh.time", 10)

  val GATEWAY_SERVER_REFRESH_ENABLED =
    CommonVars("wds.linkis.gateway.server.reflesh.enabled", false)

  val GATEWAY_HEADER_ALLOW_ORIGIN = CommonVars("wds.linkis.gateway.header.allow.origin", "*")

  val GATEWAY_HEADER_ALLOW_METHOD =
    CommonVars("wds.linkis.gateway.header.allow.methods", "POST, GET, OPTIONS, PUT, HEAD, DELETE")

  val GATEWAY_DOMAIN_LEVEL = CommonVars("wds.linkis.gateway.domain.level", 3)

  val GATEWAY_COOKIE_DOMAIN_SETUP_SWITCH =
    CommonVars("wds.linkis.gateway.cookie.domain.setup.switch", false)

  // Use regex to match against URLs, if matched, let them pass anyway(even if not currently logged in), Use "()" and "|" to match against multiple URLs
  val GATEWAY_NO_AUTH_URL_REGEX =
    CommonVars("wds.linkis.gateway.no.auth.url.regex", ".*visualis.*share.*")

  val THIS_GATEWAY_URL = CommonVars("wds.linkis.gateway.this.url", "")
  val THIS_GATEWAY_SCHEMA = CommonVars("wds.linkis.gateway.this.schema", "")

  val ENABLE_WATER_MARK = CommonVars("wds.linkis.web.enable.water.mark", true)

  val ROUTER_SERVER_LIST = CommonVars(
    "wds.linkis.entrance.name",
    ENTRANCE_SPRING_NAME.getValue,
    "List of supported routing services"
  )

  val IS_DOWNLOAD = CommonVars("linkis.web.result.set.export.enable", true)

  val LINKIS_CLUSTER_NAME = CommonVars("linkis.cluster.name", "")

  val ACCESS_CONTROL_ENABLED = CommonVars("linkis.client.access.control.enable", false)

  val ACCESS_CONTROL_URL = CommonVars("linkis.client.access.control.url", "")

  val ACCESS_CONTROL_IP = CommonVars("linkis.client.access.control.ip", "")

  val ACCESS_CONTROL_USER_ENABLED = CommonVars("linkis.client.access.control.user.enable", false)

}
