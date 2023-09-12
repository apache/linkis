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
import org.apache.linkis.monitor.core.ob.Observer
import org.apache.linkis.monitor.core.pac.{AbstractScanRule, ScannedData}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.jobhistory.exception.AnomalyScannerException
import org.apache.linkis.monitor.until.CacheUtils

import java.util
import java.util.Locale

import scala.collection.JavaConverters._

/**
 * Monitor the execution status of tasks, scan data outside 12 hours and within 24 hours, If within
 * the scope of the rule, there is data whose status is one of (Inited, WaitForRetry, Scheduled,
 * Running), an alarm will be triggered.
 */
class JobTimeExceedRule(thresholds: util.Set[String], hitObserver: Observer)
    extends AbstractScanRule(event = new JobTimeExceedHitEvent, observer = hitObserver)
    with Logging {

  private val threshold: Long = {
    if (thresholds == null) {
      throw new AnomalyScannerException(21304, "thresholds should not be null")
    }
    var t = Long.MaxValue
    for (k <- thresholds.asScala) {
      if (k != null) {
        if (t > k.toLong) {
          t = k.toLong
        }
      } else {
        logger.warn("ignored null input")
      }
    }
    t
  }

  private val scanRuleList = CacheUtils.cacheBuilder

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  override def triggerIfMatched(data: util.List[ScannedData]): Boolean = {
    if (!getHitEvent.isRegistered || data == null) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }
    val alertData: util.List[JobHistory] = new util.ArrayList[JobHistory]()
    for (sd <- data.asScala) {
      if (sd != null && sd.getData() != null) {
        for (d <- sd.getData().asScala) {
          if (d.isInstanceOf[JobHistory]) {
            val jobHistory = d.asInstanceOf[JobHistory]
            val status = jobHistory.getStatus.toUpperCase(Locale.getDefault)
            if (Constants.UNFINISHED_JOB_STATUS.contains(status)) {
              val elapse = System.currentTimeMillis() - jobHistory.getCreatedTime.getTime
              if (elapse / 1000 >= threshold) {
                alertData.add(d.asInstanceOf[JobHistory])
              }
            }
            scanRuleList.put("jobhistoryScan", jobHistory.getId)
          } else {
            logger.warn("Ignored wrong input data Type : " + d + ", " + d.getClass.getCanonicalName)
          }
        }
      } else {
        logger.warn("Ignored null scanned data")
      }

    }
    logger.info("hit " + alertData.size() + " data in one iteration")
    if (alertData.size() > 0) {
      getHitEvent.notifyObserver(getHitEvent, alertData)
      true
    } else {
      false
    }
  }

}
