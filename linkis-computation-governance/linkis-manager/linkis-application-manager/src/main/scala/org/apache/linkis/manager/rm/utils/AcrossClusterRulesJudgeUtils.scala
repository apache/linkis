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
import org.apache.linkis.manager.am.conf.AMConfiguration
import org.apache.linkis.manager.common.entity.resource.YarnResource
import org.apache.linkis.manager.common.exception.RMWarnException
import org.apache.linkis.manager.rm.exception.RMErrorCode

object AcrossClusterRulesJudgeUtils extends Logging {

  def acrossClusterRuleCheck(
      leftResource: YarnResource,
      usedResource: YarnResource,
      maxResource: YarnResource,
      clusterMaxCapacity: YarnResource,
      clusterUsedCapacity: YarnResource,
      leftCPUThreshold: Int,
      leftMemoryThreshold: Int,
      CPUPercentageThreshold: Double,
      MemoryPercentageThreshold: Double,
      clusterCPUPercentageThreshold: Double,
      clusterMemoryPercentageThreshold: Double
  ): Unit = {
    if (
        leftResource != null && usedResource != null && maxResource != null && clusterMaxCapacity != null && clusterUsedCapacity != null
    ) {

      val clusterUsedCPUPercentage = clusterUsedCapacity.queueCores
        .asInstanceOf[Double] / clusterMaxCapacity.queueCores.asInstanceOf[Double]
      val clusterUsedMemoryPercentage = clusterUsedCapacity.queueMemory
        .asInstanceOf[Double] / clusterMaxCapacity.queueMemory.asInstanceOf[Double]

      if (
          clusterUsedCPUPercentage > clusterCPUPercentageThreshold || clusterUsedMemoryPercentage > clusterMemoryPercentageThreshold
      ) {
        throw new RMWarnException(
          RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
          s"clusterUsedCPUPercentage: $clusterUsedCPUPercentage, CPUPercentageThreshold: $clusterCPUPercentageThreshold" +
            s"clusterUsedMemoryPercentage: $clusterUsedMemoryPercentage, MemoryPercentageThreshold: $clusterMemoryPercentageThreshold"
        )
      }

      val leftQueueMemory = leftResource.queueMemory / Math.pow(1024, 3).toLong
      if (leftResource.queueCores > leftCPUThreshold && leftQueueMemory > leftMemoryThreshold) {
        val usedCPUPercentage =
          usedResource.queueCores.asInstanceOf[Double] / maxResource.queueCores
            .asInstanceOf[Double]
        val usedMemoryPercentage = usedResource.queueMemory
          .asInstanceOf[Double] / maxResource.queueMemory.asInstanceOf[Double]

        if (
            usedCPUPercentage < CPUPercentageThreshold && usedMemoryPercentage < MemoryPercentageThreshold
        ) {
          return
        } else {
          throw new RMWarnException(
            RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
            s"usedCPUPercentage: $usedCPUPercentage, CPUPercentageThreshold: $CPUPercentageThreshold" +
              s"usedMemoryPercentage: $usedMemoryPercentage, MemoryPercentageThreshold: $MemoryPercentageThreshold"
          )
        }
      } else {
        throw new RMWarnException(
          RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
          s"leftResource.queueCores: ${leftResource.queueCores}, leftCPUThreshold: $leftCPUThreshold," +
            s"leftQueueMemory: $leftQueueMemory, leftMemoryThreshold: $leftMemoryThreshold"
        )
      }
    }
  }

}
