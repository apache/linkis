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

package org.apache.linkis.common.conf

import org.apache.linkis.common.utils.Logging

import org.apache.commons.lang3.StringUtils

object Configuration extends Logging {

  val BDP_ENCODING = CommonVars("wds.linkis.encoding", "utf-8")

  val DEFAULT_DATE_PATTERN = CommonVars("wds.linkis.date.pattern", "yyyy-MM-dd'T'HH:mm:ssZ")

  val FIELD_SPLIT = CommonVars("wds.linkis.field.split", "hadoop")

  val IS_TEST_MODE = CommonVars("wds.linkis.test.mode", false)

  val IS_PROMETHEUS_ENABLE = CommonVars("wds.linkis.prometheus.enable", false)

  val IS_MULTIPLE_YARN_CLUSTER = CommonVars("linkis.multiple.yarn.cluster", false)

  val PROMETHEUS_ENDPOINT = CommonVars("wds.linkis.prometheus.endpoint", "/actuator/prometheus")

  val LINKIS_HOME = CommonVars("wds.linkis.home", CommonVars("LINKIS_HOME", "/tmp").getValue)

  val GATEWAY_URL: CommonVars[String] =
    CommonVars[String]("wds.linkis.gateway.url", "http://127.0.0.1:9001/")

  val LINKIS_WEB_VERSION: CommonVars[String] = CommonVars[String]("wds.linkis.web.version", "v1")

  val REFLECT_SCAN_PACKAGE: Array[String] = CommonVars
    .apply("wds.linkis.reflect.scan.package", "org.apache.linkis,com.webank.wedatasphere")
    .getValue
    .split(",")

  val CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME =
    CommonVars("wds.linkis.console.configuration.application.name", "linkis-ps-configuration")

  val CLOUD_CONSOLE_VARIABLE_SPRING_APPLICATION_NAME =
    CommonVars("wds.linkis.console.variable.application.name", "linkis-ps-publicservice")

  // read from env
  val PREFER_IP_ADDRESS: Boolean = CommonVars(
    "linkis.discovery.prefer-ip-address",
    CommonVars("EUREKA_PREFER_IP", false).getValue
  ).getValue

  val GOVERNANCE_STATION_ADMIN = CommonVars("wds.linkis.governance.station.admin", "hadoop")

  val JOB_HISTORY_ADMIN = CommonVars("wds.linkis.jobhistory.admin", "hadoop")

  val JOB_HISTORY_DEPARTMENT_ADMIN = CommonVars("wds.linkis.jobhistory.department.admin", "hadoop")

  // Only the specified token has permission to call some api
  val GOVERNANCE_STATION_ADMIN_TOKEN_STARTWITH = "ADMIN-"

  val VARIABLE_OPERATION: Boolean = CommonVars("wds.linkis.variable.operation", false).getValue

  val ERROR_MSG_TIP =
    CommonVars(
      "linkis.jobhistory.error.msg.tip",
      "The request interface %s is abnormal. You can try to troubleshoot common problems in the knowledge base document"
    )

  def isAdminToken(token: String): Boolean = {
    if (StringUtils.isBlank(token)) {
      false
    } else {
      token.toUpperCase().startsWith(GOVERNANCE_STATION_ADMIN_TOKEN_STARTWITH)
    }
  }

  def getGateWayURL(): String = {
    val url = GATEWAY_URL.getValue.trim
    val gatewayUr = if (url.endsWith("/")) {
      url.substring(0, url.length - 1)
    } else {
      url
    }
    logger.info(s"gatewayUrl is $gatewayUr")
    gatewayUr
  }

  def getLinkisHome(): String = {
    val home = LINKIS_HOME.getValue.trim
    val linkisHome = if (home.endsWith("/")) {
      home.substring(0, home.length - 1)
    } else {
      home
    }
    logger.info(s"linkisHome is $linkisHome")
    linkisHome
  }

  def isAdmin(username: String): Boolean = {
    val adminUsers = GOVERNANCE_STATION_ADMIN.getHotValue.split(",")
    adminUsers.exists(username.equalsIgnoreCase)
  }

  def isNotAdmin(username: String): Boolean = {
    !isAdmin(username)
  }

  def isNotJobHistoryAdmin(username: String): Boolean = {
    !isJobHistoryAdmin(username)
  }

  def isJobHistoryAdmin(username: String): Boolean = {
    getJobHistoryAdmin()
      .exists(username.equalsIgnoreCase)
  }

  def isDepartmentAdmin(username: String): Boolean = {
    val departmentAdminUsers = JOB_HISTORY_DEPARTMENT_ADMIN.getHotValue.split(",")
    departmentAdminUsers.exists(username.equalsIgnoreCase)
  }

  def getJobHistoryAdmin(): Array[String] = {
    val adminUsers = GOVERNANCE_STATION_ADMIN.getHotValue.split(",")
    val historyAdminUsers = JOB_HISTORY_ADMIN.getHotValue.split(",")
    (adminUsers ++ historyAdminUsers).distinct
  }

}
