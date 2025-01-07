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

package org.apache.linkis.monitor.jobhistory.analyze

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.core.ob.Observer
import org.apache.linkis.monitor.core.pac.{AbstractScanRule, ScannedData}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.until.{CacheUtils, HttpsUntils, ThreadUtils}
import org.apache.linkis.monitor.utils.job.JohistoryUtils

import java.util

class JobHistoryAnalyzeRule(hitObserver: Observer)
    extends AbstractScanRule(event = new JobHistoryAnalyzeHitEvent, observer = hitObserver)
    with Logging {
  private val scanRuleList = CacheUtils.cacheBuilder

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  override def triggerIfMatched(data: util.List[ScannedData]): Boolean = {
    if (!getHitEvent.isRegistered) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }
    for (scanedData <- JohistoryUtils.getJobhistorySanData(data)) {
      scanedData match {
        case jobHistory: JobHistory =>
          val jobStatus = jobHistory.getStatus.toUpperCase()
          if (Constants.FINISHED_JOB_STATUS.contains(jobStatus) && jobStatus.equals("FAILED")) {
            // 执行任务分析
            ThreadUtils.analyzeRun(jobHistory)
          }
        case _ =>
      }
    }
    true
  }

}
