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

package org.apache.linkis.monitor.jobhistory.errorcode

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.utils.alert.AlertDesc
import org.apache.linkis.monitor.utils.alert.ims.{ImsAlertDesc, PooledImsAlertUtils}

import java.util

import scala.collection.JavaConverters._

class JobHistoryErrorCodeAlertSender(alerts: util.Map[String, AlertDesc])
    extends Observer
    with Logging {

  override def update(e: Event, jobHistoryList: scala.Any): Unit = {
    if (!e.isInstanceOf[JobHistoryErrCodeHitEvent]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong event that triggers JobHistoryErrorCodeAlertSender. Input DataType: " + e.getClass.getCanonicalName
      )
    }
    if (null == jobHistoryList || !jobHistoryList.isInstanceOf[util.List[_]]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobHistoryErrorCodeAlertSender. Input DataType: " + jobHistoryList.getClass.getCanonicalName
      )
    }
    val toSend = new util.HashMap[String, ImsAlertDesc]
    for (a <- jobHistoryList.asInstanceOf[util.List[_]].asScala) {
      if (a == null) {
        logger.warn("Ignore null input data")
      } else if (!a.isInstanceOf[JobHistory]) {
        logger.warn("Ignore wrong input data Type : " + a.getClass.getCanonicalName)
      } else {
        val jobHistory = a.asInstanceOf[JobHistory]
        val errorCode = String.valueOf(jobHistory.getErrorCode)
        if (alerts.containsKey(errorCode) && alerts.get(errorCode).isInstanceOf[ImsAlertDesc]) {
          val alert = if (!toSend.containsKey(errorCode)) {
            alerts.get(errorCode).asInstanceOf[ImsAlertDesc]
          } else {
            toSend.get(errorCode)
          }

          var newInfo = if (!toSend.containsKey(errorCode)) {
            alert.alertInfo + "\n" +
              "[error_code] " + jobHistory.getErrorCode + ", " + jobHistory.getErrorDesc + "\n"
          } else {
            alert.alertInfo
          }
          newInfo = newInfo +
            "[job-info] " +
            "submit-user: " + jobHistory.getSubmitUser + ", " +
            "execute-user: " + jobHistory.getExecuteUser + ", " +
            "engine_type: " + jobHistory.getEngineType + ", " +
            "create_time: " + jobHistory.getCreatedTime + ", " +
            "instance: " + jobHistory.getInstances + ". \n"
          val newNumHit = alert.numHit + 1
          toSend.put(errorCode, alert.copy(alertInfo = newInfo, numHit = newNumHit))
        } else if (!alerts.containsKey(errorCode)) {
          logger.warn("Ignored unregistered error code: " + errorCode)
        } else if (!alerts.get(errorCode).isInstanceOf[ImsAlertDesc]) {
          logger.warn(
            "Ignored invalid alertDesc. DataType: " + alerts
              .get(errorCode)
              .getClass
              .getCanonicalName
          )
        }
      }
    }
    for ((_, alert) <- toSend.asScala) {
      PooledImsAlertUtils.addAlert(alert)
    }
  }

}
