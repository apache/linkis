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

package org.apache.linkis.monitor.jobhistory.runtime

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.utils.alert.ims.{MonitorAlertUtils, PooledImsAlertUtils}

import java.util

import scala.collection.JavaConverters._

/**
 * Scan the execution data within the first 20 minutes, judge the completed tasks,
 *   1. The parm field in jobhistory contains (task.notification.conditions) 2. If the result of
 *      executing the task is any one of (Succeed, Failed, Canceled, Timeout, ALL), an alarm will be
 *      triggered 3.The result of the job is that it has ended The alarm can be triggered if the
 *      above three conditions are met at the same time
 */
class JobHistoryRunTimeAlertSender() extends Observer with Logging {

  override def update(e: Event, jobHistroyList: scala.Any): Unit = {
    if (!e.isInstanceOf[JobHistoryRunTimeHitEvent]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong event that triggers JobHistoryErrorCodeAlertSender. Input DataType: " + e.getClass.getCanonicalName
      )
    }
    if (null == jobHistroyList || !jobHistroyList.isInstanceOf[util.List[_]]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobHistoryErrorCodeAlertSender. Input DataType: " + jobHistroyList.getClass.getCanonicalName
      )
    }
    for (a <- jobHistroyList.asInstanceOf[util.List[_]].asScala) {
      if (a == null) {
        logger.warn("Ignore null input data")
      } else if (!a.isInstanceOf[JobHistory]) {
        logger.warn("Ignore wrong input data Type : " + a.getClass.getCanonicalName)
      } else {
        // 您的任务ID 1234 执行完成，最终状态为：成功、失败、取消
        val jobHistory = a.asInstanceOf[JobHistory]
        val status = jobHistory.getStatus
        val replaceParm: util.HashMap[String, String] = new util.HashMap[String, String]
        replaceParm.put("$id", String.valueOf(jobHistory.getId))
        replaceParm.put("$status", status)
        replaceParm.put("$alteruser", jobHistory.getSubmitUser)
        val alters = MonitorAlertUtils.getAlerts(Constants.JOB_RESULT_IM, replaceParm)
        PooledImsAlertUtils.addAlert(alters.get("12015"))
      }
    }
  }

}
