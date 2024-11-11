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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.core.ob.{Event, Observer}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.until.HttpsUntils
import org.apache.linkis.monitor.utils.alert.ims.{MonitorAlertUtils, PooledImsAlertUtils}

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils

import java.util

import scala.collection.JavaConverters.asScalaBufferConverter

class StarrocksTimeExceedAlterSender extends Observer with Logging {

  /**
   * Observer Pattern
   */
  override def update(e: Event, jobHistroyList: scala.Any): Unit = {
    if (!e.isInstanceOf[StarrocksTimeExceedHitEvent]) {
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
        val jobHistory = a.asInstanceOf[JobHistory]
        val timeValue =
          HttpsUntils.getJDBCConf(jobHistory.getSubmitUser, Constants.JDBC_ALERT_TIME)
        val userValue =
          HttpsUntils.getJDBCConf(jobHistory.getSubmitUser, Constants.JDBC_ALERT_USER)
        var levelValue =
          HttpsUntils.getJDBCConf(jobHistory.getSubmitUser, Constants.JDBC_ALERT_LEVEL)
        if (StringUtils.isNotBlank(timeValue) && StringUtils.isNotBlank(userValue)) {
          val replaceParm: util.HashMap[String, String] = new util.HashMap[String, String]
          replaceParm.put("$id", String.valueOf(jobHistory.getId))
          replaceParm.put("$timeoutTime", timeValue)
          replaceParm.put("$alteruser", userValue)
          replaceParm.put("$eccAlertUser", userValue)
          replaceParm.put("$submitUser", jobHistory.getSubmitUser)
          if (StringUtils.isBlank(levelValue)) {
            levelValue = "3";
          }
          replaceParm.put("$alterLevel", levelValue)
          val alters = MonitorAlertUtils.getAlerts(Constants.USER_LABEL_MONITOR, replaceParm)
          PooledImsAlertUtils.addAlert(alters.get("12020"))
        }
      }
    }
  }

}
