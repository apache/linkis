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

package org.apache.linkis.monitor.jobhistory.labels

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.utils.alert.AlertDesc
import org.apache.linkis.monitor.utils.alert.ims.{PooledImsAlertUtils, UserLabelAlertUtils}
import org.apache.linkis.server.BDPJettyServerHelper

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class JobHistoryLabelsAlertSender() extends Observer with Logging {

  override def update(e: Event, jobHistoryList: scala.Any): Unit = {
    if (!e.isInstanceOf[JobHistoryLabelsHitEvent]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong event that triggers JobHistoryLabelsAlertSender. Input DataType: " + e.getClass.getCanonicalName
      )
    }
    if (null == jobHistoryList || !jobHistoryList.isInstanceOf[util.List[_]]) {
      throw new AnomalyScannerException(
        21304,
        "Wrong input for JobHistoryLabelsAlertSender. Input DataType: " + jobHistoryList.getClass.getCanonicalName
      )
    }
    val toSend = new ArrayBuffer[String]
    for (a <- jobHistoryList.asInstanceOf[util.List[_]].asScala) {
      if (a == null) {
        logger.warn("Ignore null input data")
      } else if (!a.isInstanceOf[JobHistory]) {
        logger.warn("Ignore wrong input data Type : " + a.getClass.getCanonicalName)
      } else {
        val jobHistory = a.asInstanceOf[JobHistory]
        toSend.append(jobHistory.getLabels)
      }
    }
    for (str <- toSend.distinct) {
      val labelsMap: util.Map[String, String] =
        BDPJettyServerHelper.gson.fromJson(str, classOf[java.util.Map[String, String]])
      val alerts: util.Map[String, AlertDesc] =
        UserLabelAlertUtils.getAlerts(Constants.USER_LABEL_MONITOR, labelsMap.get("userCreator"))
      PooledImsAlertUtils.addAlert(alerts.get("12010"));
    }
  }

}
