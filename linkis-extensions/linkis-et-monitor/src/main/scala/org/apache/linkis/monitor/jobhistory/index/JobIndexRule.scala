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

package org.apache.linkis.monitor.jobhistory.index

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.core.ob.Observer
import org.apache.linkis.monitor.core.pac.{AbstractScanRule, ScannedData}
import org.apache.linkis.monitor.entity.IndexEntity
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.until.HttpsUntils

import java.util

import scala.collection.JavaConverters._

/**
 * 部门任务指标上报，每隔20分钟统计部门任务数量，上报至IMS
 */
class JobIndexRule(hitObserver: Observer)
    extends AbstractScanRule(event = new JobIndexHitEvent, observer = hitObserver)
    with Logging {

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   * @param scanData
   * @return
   */
  override def triggerIfMatched(scanData: util.List[ScannedData]): Boolean = {
    if (scanData == null) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }
    for (sd <- scanData.asScala) {
      if (sd != null && sd.getData() != null) {
        // 收集部门任务数量
        val deptTaskNumMap = sd
          .getData()
          .asScala
          .map(_.asInstanceOf[JobHistory])
          .groupBy(_.getOrgName)
          .mapValues(_.size)
        val sendList = new util.ArrayList[IndexEntity]
        // 收集部门信息
        val deptList =
          sd.getData().asScala.map(_.asInstanceOf[JobHistory]).map(_.getOrgName).distinct.toList
        deptList.foreach(departName => {
          val count = deptTaskNumMap.get(departName)
          sendList.add(
            new IndexEntity(
              departName,
              "job",
              "job_dept_task",
              HttpsUntils.localHost,
              String.valueOf(count.get)
            )
          )
        })
        // 上报IMS
        HttpsUntils.sendIndex(sendList)
      } else {
        logger.warn("Ignored null scanned data")
      }
    }
    true
  }

}
