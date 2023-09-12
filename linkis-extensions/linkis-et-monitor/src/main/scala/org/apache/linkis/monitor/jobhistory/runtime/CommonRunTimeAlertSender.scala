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
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.collections.MapUtils

import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util
import java.util.Date

import scala.collection.JavaConverters._

class CommonRunTimeAlertSender() extends Observer with Logging {
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  override def update(e: Event, jobHistoryList: scala.Any): Unit = {
    if (!e.isInstanceOf[JobHistoryRunTimeHitEvent]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong event that triggers JobHistoryErrorCodeAlertSender. Input DataType: " + e.getClass.getCanonicalName
      )
    }
    if (!jobHistoryList.isInstanceOf[util.List[_]] || null == jobHistoryList) {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobHistoryErrorCodeAlertSender. Input DataType: " + jobHistoryList.getClass.getCanonicalName
      )
    }
    for (a <- jobHistoryList.asInstanceOf[util.List[_]].asScala) {
      if (a == null) {
        logger.warn("Ignore null input data")
      } else if (!a.isInstanceOf[JobHistory]) {
        logger.warn("Ignore wrong input data Type : " + a.getClass.getCanonicalName)
      } else {
        val jobHistory = a.asInstanceOf[JobHistory]
        val observeInfoMap = BDPJettyServerHelper.gson.fromJson(
          jobHistory.getObserveInfo,
          classOf[java.util.Map[String, String]]
        )
        val extraMap = MapUtils.getMap(observeInfoMap, "extra")
        observeInfoMap.put(
          "title",
          extraMap
            .get("title")
            .toString + ",任务id：" + jobHistory.getId + ",执行结果 :" + jobHistory.getStatus
        )
        observeInfoMap.put(
          "$detail",
          extraMap.get("detail").toString + ",执行结果 :" + jobHistory.getStatus
        )
        observeInfoMap.put("$submitUser", jobHistory.getSubmitUser)
        observeInfoMap.put("$status", jobHistory.getStatus)
        observeInfoMap.put("$id", jobHistory.getId.toString)
        observeInfoMap.put("$date", dateFormat.format(new Date()))
        var alterSysInfo = ""
        if (null != extraMap.get("alterSysInfo")) {
          alterSysInfo = extraMap.get("alterSysInfo").toString
        }
        observeInfoMap.put("$sysid", alterSysInfo)
        var alterObject = ""
        if (null != extraMap.get("alterObject")) {
          alterObject = extraMap.get("alterObject").toString
        }
        observeInfoMap.put("$object", alterObject)
        observeInfoMap.put("$ip", InetAddress.getLocalHost.getHostAddress)
        observeInfoMap.remove("taskId")
        observeInfoMap.remove("extra")
        val alters = MonitorAlertUtils.getAlerts(Constants.JOB_RESULT_IM, observeInfoMap)
        PooledImsAlertUtils.addAlert(alters.get("12016"))
      }
    }
  }

}
