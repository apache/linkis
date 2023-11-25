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

package org.apache.linkis.monitor.jobhistory.jobtime

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.monitor.config.MonitorConfig
import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.utils.alert.AlertDesc
import org.apache.linkis.monitor.utils.alert.ims.{ImsAlertDesc, PooledImsAlertUtils}

import java.text.MessageFormat
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class JobTimeExceedAlertSender(alerts: util.Map[String, AlertDesc]) extends Observer with Logging {

  private val orderedThresholds: Array[Long] = {
    val ret = new ArrayBuffer[Long]()
    if (alerts != null) {
      for (k <- alerts.keySet().asScala) {
        Utils.tryCatch(ret.append(k.toLong)) { t =>
          logger.warn("Ignored illegal threshold: " + k, t)
          false
        }
      }
    }
    ret.toArray
  }

  override def update(e: Event, jobHistoryList: scala.Any): Unit = {
    if (!e.isInstanceOf[JobTimeExceedHitEvent]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong event that triggers JobTimeExceedAlertSender. Input DataType: " + e.getClass.getCanonicalName
      )
    }
    if (null == jobHistoryList || !jobHistoryList.isInstanceOf[util.List[_]]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobTimeExceedAlertSender. Input DataType: " + jobHistoryList.getClass.getCanonicalName
      )
    }
    if (orderedThresholds.length == 0) {
      logger.warn("Found none legal threshold, will not send any alert: " + this)
      return
    }
    val toSend = new util.HashMap[String, ImsAlertDesc]
    for (a <- jobHistoryList.asInstanceOf[util.List[_]].asScala) {
      if (a == null) {
        logger.warn("Ignore null input data")
      } else if (!a.isInstanceOf[JobHistory]) {
        logger.warn("Ignore wrong input data Type : " + a.getClass.getCanonicalName)
      } else {
        val jobHistory = a.asInstanceOf[JobHistory]
        val elapse = System.currentTimeMillis() - jobHistory.getCreatedTime.getTime
        var ts = 0L
        for (t <- orderedThresholds) { // search max threshold that is smaller than elapse
          if (elapse >= t) {
            ts = t
          } else {}
        }
        val name = ts.toString
        val alert = if (!toSend.containsKey(name)) {
          alerts
            .get(name)
            .asInstanceOf[ImsAlertDesc]
        } else {
          toSend.get(name)
        }

        val newInfo = MessageFormat.format(
          MonitorConfig.TASK_RUNTIME_TIMEOUT_DESC.getValue,
          jobHistory.getId,
          (elapse / 1000 / 60 / 60).toString,
          jobHistory.getInstances,
          MonitorConfig.SOLUTION_URL.getValue
        )

        val newNumHit = alert.numHit + 1
        val receiver = new util.HashSet[String]()
        receiver.add(jobHistory.getSubmitUser)
        receiver.add(jobHistory.getExecuteUser)
        receiver.addAll(alert.alertReceivers)
        val ImsAlertDesc =
          alert.copy(alertInfo = newInfo, alertReceivers = receiver, numHit = newNumHit)
        PooledImsAlertUtils.addAlert(ImsAlertDesc)

      }
    }
  }

}
