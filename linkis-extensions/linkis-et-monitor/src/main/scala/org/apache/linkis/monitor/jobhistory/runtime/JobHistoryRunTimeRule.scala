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
import org.apache.linkis.monitor.core.ob.Observer
import org.apache.linkis.monitor.core.pac.{AbstractScanRule, ScannedData}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.until.CacheUtils
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.server.BDPJettyServerHelper

import java.util

import scala.collection.JavaConverters._

class JobHistoryRunTimeRule(hitObserver: Observer)
    extends AbstractScanRule(event = new JobHistoryRunTimeHitEvent, observer = hitObserver)
    with Logging {
  private val scanRuleList = CacheUtils.cacheBuilder

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  override def triggerIfMatched(data: util.List[ScannedData]): Boolean = {
    if (null == data || !getHitEvent.isRegistered) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }
    val alertData: util.List[JobHistory] = new util.ArrayList[JobHistory]()
    for (sd <- data.asScala) {
      if (sd != null && sd.getData() != null) {
        for (d <- sd.getData().asScala) {
          d match {
            case jobHistory: JobHistory =>
              if (Constants.FINISHED_JOB_STATUS.contains(jobHistory.getStatus.toUpperCase())) {
                val parmsMap: util.Map[String, scala.AnyRef] = BDPJettyServerHelper.gson.fromJson(
                  jobHistory.getParams,
                  classOf[util.Map[String, scala.AnyRef]]
                )
                val runtimeMap = TaskUtils.getRuntimeMap(parmsMap)
                if (
                    runtimeMap.containsKey("task.notification.conditions") &&
                    Constants.FINISHED_JOB_STATUS.contains(
                      String.valueOf(runtimeMap.get("task.notification.conditions")).toUpperCase()
                    )
                ) {
                  alertData.add(jobHistory)
                }
              } else {
                logger.warn(
                  "Ignored wrong input data Type : " + d + ", " + d.getClass.getCanonicalName
                )
              }
              scanRuleList.put("jobHistoryId", jobHistory.getId)
            case _ =>
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
