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

package org.apache.linkis.monitor.scan.constants

import org.apache.linkis.common.conf.CommonVars


object Constants {

  val ALERT_IMS_URL = CommonVars.properties.getProperty(
    "wds.linkis.alert.url",
    "http://127.0.0.1:10812/ims_data_access/send_alarm.do"
  )

  val ALERT_PROPS_FILE_PATH = CommonVars.properties.getProperty(
    "wds.linkis.alert.ims.file.path",
    "linkis-et-monitor-ims.properties"
  )

  val ALERT_IMS_MAX_LINES = CommonVars[Int]("wds.linkis.alert.ims.max.lines", 8).getValue

  val SCAN_INTERVALS_SECONDS =
    CommonVars[Long]("wds.linkis.errorcode.scanner.interval.seconds", 1 * 60 * 60).getValue

  val MAX_INTERVALS_SECONDS =
    CommonVars[Long]("wds.linkis.errorcode.scanner.max.interval.seconds", 1 * 60 * 60).getValue

  val ALERT_SUB_SYSTEM_ID =
    CommonVars.properties.getProperty("wds.linkis.alert.ims.sub_system_id", "5435")

  val ALERT_DEFAULT_RECEIVERS = CommonVars.properties
    .getProperty("wds.linkis.alert.receiver.default", "")
    .split(",")
    .toSet[String]

  val SCAN_PREFIX_ERRORCODE = "jobhistory.errorcode."
  val SCAN_PREFIX_UNFINISHED_JOBTIME_EXCEED_SEC = "jobhistory.unfinished.time.exceed.sec."

  val SCAN_RULE_UNFINISHED_JOB_STATUS =
    "Inited,WaitForRetry,Scheduled,Running".split(",").map(s => s.toUpperCase())

  val DIRTY_DATA_EUREKA_DELETE_INSTANCE_URL =
    CommonVars.apply("wds.linkis.eureka.defaultZone", "http://localhost:20303").getValue

  val DIRTY_DATA_EUREKA_DELETE_PATH = CommonVars
    .apply("wds.linkis.dirty.data.eureka.delete.path", "/apps/{springName}/{instance}")
    .getValue

  val DIRTY_DATA_UNFINISHED_JOB_STATUS =
    "Inited,WaitForRetry,Scheduled,Running".split(",").map(s => s.toUpperCase())

  val DIRTY_DATA_JOB_TARGET_STATUS = "Cancelled"

  val DIRTY_DATA_ENTRANCE_APPLICATIONNAME =
    CommonVars("wds.linkis.entrance.spring.name", "linkis-cg-entrance").getValue

  val MODIFY_DB_DATA_DAYS = CommonVars("wds.linkis.dirty.data.modify.db.days", 1).getValue
  val ALERT_RESOURCE_MONITOR = "ecm.resource.monitor.im."

  val LINKIS_API_VERSION: CommonVars[String] =
    CommonVars[String]("wds.linkis.bml.api.version", "v1")

  val AUTH_TOKEN_KEY: CommonVars[String] =
    CommonVars[String]("wds.linkis.bml.auth.token.key", "Validation-Code")

  val AUTH_TOKEN_VALUE: CommonVars[String] =
    CommonVars[String]("wds.linkis.bml.auth.token.value", "BML-AUTH")

  val CONNECTION_MAX_SIZE: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.bml.connection.max.size", 10)

  val CONNECTION_TIMEOUT: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.bml.connection.timeout", 5 * 60 * 1000)

  val CONNECTION_READ_TIMEOUT: CommonVars[Int] =
    CommonVars[Int]("wds.linkis.bml.connection.read.timeout", 10 * 60 * 1000)

  val AUTH_TOKEN_KEY_SHORT_NAME = "tokenKey"
  val AUTH_TOKEN_VALUE_SHORT_NAME = "tokenValue"
  val CONNECTION_MAX_SIZE_SHORT_NAME = "maxConnection"
  val CONNECTION_TIMEOUT_SHORT_NAME = "connectTimeout"
  val CONNECTION_READ_TIMEOUT_SHORT_NAME = "readTimeout"
  val CLIENT_NAME_SHORT_NAME = "clientName"
  val USER_LABEL_MONITOR = "jobhistory.label.monitor.im."

  val USER_LABEL_TENANT: CommonVars[String] =
    CommonVars[String]("linkis.monitor.jobhistory.userLabel.tenant", "{}")

  val USER_RESOURCE_MONITOR = "user.mode.monitor.im."
  val BML_CLEAR_IM = "bml.clear.monitor.im."
  val THREAD_TIME_OUT_IM = "thread.monitor.timeout.im."
  val JOB_RESULT_IM = "jobhistory.result.monitor.im."

  val DIRTY_DATA_FINISHED_JOB_STATUS =
    "Succeed,Failed,Cancelled,Timeout,ALL".split(",").map(s => s.toUpperCase())
  val DIRTY_DATA_FINISHED_JOB_STATUS_ARRAY = "Succeed,Failed,Cancelled,Timeout".split(",")

}
