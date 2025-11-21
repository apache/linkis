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

package org.apache.linkis.monitor.constants

import org.apache.linkis.common.conf.CommonVars

object Constants {

  val SCAN_PREFIX_ERRORCODE = "jobhistory.errorcode."
  val SCAN_PREFIX_UNFINISHED_JOBTIME_EXCEED_SEC = "jobhistory.unfinished.time.exceed.sec."
  val ALERT_RESOURCE_MONITOR = "ecm.resource.monitor.im."

  val UNFINISHED_JOB_STATUS =
    "Inited,WaitForRetry,Scheduled,Running".split(",").map(s => s.toUpperCase())

  val FINISHED_JOB_STATUS =
    "Succeed,Failed,Cancelled,Timeout".split(",").map(s => s.toUpperCase())

  val DATA_FINISHED_JOB_STATUS_ARRAY = "Succeed,Failed,Cancelled,Timeout".split(",")

  val DATA_UNFINISHED_JOB_STATUS_ARRAY =
    "Inited,WaitForRetry,Scheduled,Running".split(",")

  val ALERT_PROPS_FILE_PATH = CommonVars.properties.getProperty(
    "linkis.alert.conf.file.path",
    "linkis-et-monitor-ims.properties"
  )

  val ALERT_IMS_URL = CommonVars.properties.getProperty(
    "linkis.alert.url",
    "http://127.0.0.1:10812/ims_data_access/send_alarm.do"
  )

  val ALERT_SUB_SYSTEM_ID =
    CommonVars.properties.getProperty("linkis.alert.sub_system_id", "5435")

  val ALERT_DEFAULT_RECEIVERS = CommonVars.properties
    .getProperty("linkis.alert.receiver.default", "")
    .split(",")
    .toSet[String]

  val ECC_DEFAULT_RECEIVERS = CommonVars.properties
    .getProperty("linkis.alert.ecc.receiver.default", "")
    .split(",")
    .toSet[String]

  val ALERT_IMS_MAX_LINES = CommonVars[Int]("linkis.alert.content.max.lines", 8).getValue

  val TIMEOUT_INTERVALS_SECONDS =
    CommonVars[Long]("linkis.monitor.scanner.timeout.interval.seconds", 1 * 60 * 60).getValue

  val ERRORCODE_MAX_INTERVALS_SECONDS =
    CommonVars[Long]("linkis.errorcode.scanner.max.interval.seconds", 1 * 60 * 60).getValue

  val SCAN_RULE_UNFINISHED_JOB_STATUS =
    "Inited,WaitForRetry,Scheduled,Running".split(",").map(s => s.toUpperCase())

  val USER_LABEL_MONITOR = "jobhistory.label.monitor.im."

  val USER_LABEL_TENANT: CommonVars[String] =
    CommonVars[String]("linkis.monitor.jobhistory.userLabel.tenant", "{}")

  val USER_RESOURCE_MONITOR = "user.mode.monitor.im."
  val BML_CLEAR_IM = "bml.clear.monitor.im."
  val THREAD_TIME_OUT_IM = "thread.monitor.timeout.im."
  val JOB_RESULT_IM = "jobhistory.result.monitor.im."
  val DEPARTMENT_USER_IM = "department.user.sync.im."

  val BML_VERSION_MAX_NUM: CommonVars[Int] =
    CommonVars[Int]("linkis.monitor.bml.cleaner.version.max.num", 50)

  val BML_VERSION_KEEP_NUM: CommonVars[Int] =
    CommonVars[Int]("linkis.monitor.bml.cleaner.version.keep.num", 20)

  val BML_PREVIOUS_INTERVAL_TIME_DAYS: CommonVars[Long] =
    CommonVars[Long]("linkis.monitor.bml.cleaner.previous.interval.days", 30)

  val BML_CLEAN_ONCE_RESOURCE_LIMIT_NUM: CommonVars[Int] =
    CommonVars[Int]("linkis.monitor.bml.cleaner.once.limit.num", 100)

  val BML_TRASH_PATH_PREFIX: CommonVars[String] =
    CommonVars[String]("linkis.monitor.bml.trash.prefix.path", "hdfs:///tmp/linkis/trash/bml_trash")

  val LINKIS_CLUSTER_NAME =
    CommonVars.properties.getProperty("linkis.cluster.name", "")

  val ADMIN_USER = "hadoop"

  val SPLIT_DELIMITER = ";"

  val JDBC_ALERT_TIME = "linkis.jdbc.task.timeout.alert.time"

  val JDBC_ALERT_USER = "linkis.jdbc.task.timeout.alert.user"

  val JDBC_ALERT_LEVEL = "linkis.jdbc.task.timeout.alert.level"

  val JOB_DATASOURCE_CONF = "wds.linkis.engine.runtime.datasource"

  val JDBC_ENGINE = "jdbc"

  val TASK_ARCHIVE_SH = "linkis_task_archive.sh"
}
