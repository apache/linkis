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

package com.webank.wedatasphere.linkis.common.conf

import com.webank.wedatasphere.linkis.common.utils.Logging


object Configuration extends Logging {

  val BDP_ENCODING = CommonVars("wds.linkis.encoding", "utf-8")

  val DEFAULT_DATE_PATTERN = CommonVars("wds.linkis.date.pattern", "yyyy-MM-dd'T'HH:mm:ssZ")

  val FIELD_SPLIT = CommonVars("wds.linkis.field.split", "hadoop")

  val IS_TEST_MODE = CommonVars("wds.linkis.test.mode", false)

  val LINKIS_HOME = CommonVars("wds.linkis.home", CommonVars("LINKIS_HOME", "/tmp").getValue)

  val GATEWAY_URL: CommonVars[String] = CommonVars[String]("wds.linkis.gateway.url", "http://127.0.0.1:9001/")

  val LINKIS_WEB_VERSION:CommonVars[String] = CommonVars[String]("wds.linkis.web.version", "v1")

  val REFLECT_SCAN_PACKAGE = CommonVars.apply("wds.linkis.reflect.scan.package", "com.webank.wedatasphere")

  val CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.console.configuration.application.name", "linkis-ps-publicservice")

  val CLOUD_CONSOLE_VARIABLE_SPRING_APPLICATION_NAME = CommonVars("wds.linkis.console.variable.application.name", "linkis-ps-publicservice")


  def getGateWayURL(): String = {
    val url = GATEWAY_URL.getValue.trim
    val gatewayUr = if (url.endsWith("/")) {
      url.substring(0, url.length - 1)
    } else {
      url
    }
    info(s"gatewayUrl is $gatewayUr")
    gatewayUr
  }

  def getLinkisHome(): String = {
    val home = LINKIS_HOME.getValue.trim
    val linkisHome = if (home.endsWith("/")) {
      home.substring(0, home.length - 1)
    } else {
      home
    }
    info(s"linkisHome is $linkisHome")
    linkisHome
  }

}
