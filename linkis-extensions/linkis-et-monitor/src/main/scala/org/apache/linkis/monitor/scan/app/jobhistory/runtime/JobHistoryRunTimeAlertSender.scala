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

package org.apache.linkis.monitor.scan.app.jobhistory.runtime

import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.scan.app.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.scan.app.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.scan.constants.Constants
import org.apache.linkis.monitor.scan.core.ob.{Event, Observer}
import org.apache.linkis.monitor.scan.utils.alert.ims.{MonitorAlertUtils, PooledImsAlertUtils}

import scala.collection.JavaConverters._

/**
 * 对前20分钟内的执行数据进行扫描，对已结束的任务进行判断，
 * 1.jobhistory中的parm字段中包含（task.notification.conditions）
 * 2.执行任务的结果是（Succeed,Failed,Cancelled,Timeout,ALL）其中任意一个，则触发告警
 * 3.job的结果是已经结束
 * 同时满足上述三个条件即可触发告警
 */
class JobHistoryRunTimeAlertSender()
  extends Observer
    with Logging {

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
