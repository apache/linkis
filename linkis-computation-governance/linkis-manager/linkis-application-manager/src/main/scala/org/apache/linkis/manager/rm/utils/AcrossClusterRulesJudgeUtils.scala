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

package org.apache.linkis.manager.rm.utils

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.resource.YarnResource

object AcrossClusterRulesJudgeUtils extends Logging {

  def acrossClusterRuleJudge(
      leftResource: YarnResource,
      usedResource: YarnResource,
      maxResource: YarnResource,
      leftCPUThreshold: Int,
      leftMemoryThreshold: Int,
      UsedCPUPercentageThreshold: Double,
      UsedMemoryPercentageThreshold: Double
  ): Boolean = {
    if (leftResource != null && usedResource != null && maxResource != null) {
      val leftQueueMemory = leftResource.queueMemory / Math.pow(1024, 3).toLong
      logger.info(
        s"leftResource.queueCores: ${leftResource.queueCores}, leftCPUThreshold: $leftCPUThreshold," +
          s"leftQueueMemory: $leftQueueMemory, leftMemoryThreshold: $leftMemoryThreshold"
      )

      if (leftResource.queueCores > leftCPUThreshold && leftQueueMemory > leftMemoryThreshold) {

        val usedCPUPercentage = usedResource.queueCores.asInstanceOf[Double] / maxResource.queueCores
          .asInstanceOf[Double]
        val usedMemoryPercentage = usedResource.queueMemory
          .asInstanceOf[Double] / maxResource.queueMemory.asInstanceOf[Double]

        logger.info(
          s"usedCPUPercentage: $usedCPUPercentage, UsedCPUPercentageThreshold: $UsedCPUPercentageThreshold" +
            s"usedMemoryPercentage: $usedMemoryPercentage, UsedMemoryPercentageThreshold: $UsedMemoryPercentageThreshold"
        )

        if (
            usedCPUPercentage < UsedCPUPercentageThreshold && usedMemoryPercentage < UsedMemoryPercentageThreshold
        ) {
          return true
        }
      }
    }

    false
  }

}
