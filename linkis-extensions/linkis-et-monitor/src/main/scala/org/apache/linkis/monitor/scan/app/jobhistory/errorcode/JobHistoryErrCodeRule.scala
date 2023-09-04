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

package org.apache.linkis.monitor.scan.app.jobhistory.errorcode

import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.scan.app.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.scan.app.monitor.until.CacheUtils
import org.apache.linkis.monitor.scan.core.ob.Observer
import org.apache.linkis.monitor.scan.core.pac.{AbstractScanRule, ScannedData}
import scala.collection.JavaConverters._


/**
 * 针对执行任务返回的错误码进行监控，执行脚本任务时，会记录执行的错误码在数据库中，
 * 服务会根据数据库中记录的错误码，来进行告警，如果错误码中包含（11001，11002）即可触发告警
 */
class JobHistoryErrCodeRule(errorCodes: util.Set[String], hitObserver: Observer)
    extends AbstractScanRule(event = new JobHistoryErrCodeHitEvent, observer = hitObserver)
    with Logging {
  private val scanRuleList = CacheUtils.cacheBuilder

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  override def triggerIfMatched(data: util.List[ScannedData]): Boolean = {

    if (!getHitEvent().isRegistered || null == data) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }

    val alertData: util.List[JobHistory] = new util.ArrayList[JobHistory]()
    for (sd <- data.asScala) {
      if (sd != null && sd.getData() != null) {
        for (d <- sd.getData().asScala) {
          d match {
            case history: JobHistory =>
              if (errorCodes.contains(String.valueOf(history.getErrorCode))) {
                alertData.add(history)
              }
              scanRuleList.put("jobHistoryId", history.getId)
            case _ =>
              logger.warn("Ignored wrong input data Type : " + d + ", " + d.getClass.getCanonicalName)
          }
        }
      } else {
        logger.warn("Ignored null scanned data")
      }

    }
    logger.info("hit " + alertData.size() + " data in one iteration")
    if (alertData.size() > 0) {
      getHitEvent().notifyObserver(getHitEvent(), alertData)
       true
    } else {
       false
    }
  }

}
