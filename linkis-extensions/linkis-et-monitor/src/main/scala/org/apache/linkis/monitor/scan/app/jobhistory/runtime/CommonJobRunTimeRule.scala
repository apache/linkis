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

import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.scan.app.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.scan.constants.Constants
import org.apache.linkis.monitor.scan.core.ob.Observer
import org.apache.linkis.monitor.scan.core.pac.{AbstractScanRule, ScannedData}

import java.util
import scala.collection.JavaConverters._

/**
 * 对前20分钟内的执行数据进行扫描，
 * 1.数据的ObserveInfo字段进行判断是否为空，
 * 2.任务状态已经完成（Succeed,Failed,Cancelled,Timeout,ALL）
 * 满足条件即可触发告警
 */
class CommonJobRunTimeRule(hitObserver: Observer)
  extends AbstractScanRule(event = new JobHistoryRunTimeHitEvent, observer = hitObserver)
    with Logging {

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  override def triggerIfMatched(data: util.List[ScannedData]): Boolean = {
    if (!getHitEvent.isRegistered || null == data) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }
    val alertData: util.List[JobHistory] = new util.ArrayList[JobHistory]()
    for (sd <- data.asScala) {
      if (sd != null && sd.getData() != null) {
        for (d <- sd.getData().asScala) {
          d match {
            case jobHistory: JobHistory =>
              if (Constants.DIRTY_DATA_FINISHED_JOB_STATUS.contains(jobHistory.getStatus.toUpperCase())
                &&StringUtils.isNotBlank(jobHistory.getObserveInfo)) {
                alertData.add(jobHistory)
              } else {
                logger.warn("jobHistory is not completely  ， taskid :" + d)
              }
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
