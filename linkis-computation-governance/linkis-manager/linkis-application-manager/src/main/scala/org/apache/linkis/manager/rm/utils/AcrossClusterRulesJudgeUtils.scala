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
import org.apache.linkis.manager.common.constant.AMConstant
import org.apache.linkis.manager.common.entity.resource.{Resource, ResourceType, YarnResource}
import org.apache.linkis.manager.common.exception.RMWarnException
import org.apache.linkis.manager.common.protocol.engine.EngineCreateRequest
import org.apache.linkis.manager.rm.domain.RMLabelContainer
import org.apache.linkis.manager.rm.exception.RMErrorCode
import org.apache.linkis.manager.rm.external.service.ExternalResourceService
import org.apache.linkis.manager.rm.external.yarn.YarnResourceIdentifier

import org.apache.commons.lang3.StringUtils

object AcrossClusterRulesJudgeUtils extends Logging {

  def targetClusterRuleCheck(
      leftResource: YarnResource,
      usedResource: YarnResource,
      maxResource: YarnResource,
      clusterMaxCapacity: YarnResource,
      clusterUsedCapacity: YarnResource,
      CPUThreshold: Int,
      MemoryThreshold: Int,
      CPUPercentageThreshold: Double,
      MemoryPercentageThreshold: Double,
      clusterCPUPercentageThreshold: Double,
      clusterMemoryPercentageThreshold: Double
  ): Unit = {
    if (
        leftResource != null && usedResource != null && maxResource != null && clusterMaxCapacity != null && clusterUsedCapacity != null
    ) {

      val clusterUsedCPUPercentage = clusterUsedCapacity.getQueueCores
        .asInstanceOf[Double] / clusterMaxCapacity.getQueueCores.asInstanceOf[Double]
      val clusterUsedMemoryPercentage = clusterUsedCapacity.getQueueMemory
        .asInstanceOf[Double] / clusterMaxCapacity.getQueueMemory.asInstanceOf[Double]

      if (
          clusterUsedCPUPercentage >= clusterCPUPercentageThreshold || clusterUsedMemoryPercentage >= clusterMemoryPercentageThreshold
      ) {
        throw new RMWarnException(
          RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
          s"clusterUsedCPUPercentage: $clusterUsedCPUPercentage, CPUPercentageThreshold: $clusterCPUPercentageThreshold" +
            s"clusterUsedMemoryPercentage: $clusterUsedMemoryPercentage, MemoryPercentageThreshold: $clusterMemoryPercentageThreshold"
        )
      }

      val leftQueueMemory = leftResource.getQueueMemory / Math.pow(1024, 3).toLong
      if (leftResource.getQueueCores >= CPUThreshold && leftQueueMemory >= MemoryThreshold) {
        val usedCPUPercentage =
          usedResource.getQueueCores.asInstanceOf[Double] / maxResource.getQueueCores
            .asInstanceOf[Double]
        val usedMemoryPercentage = usedResource.getQueueMemory
          .asInstanceOf[Double] / maxResource.getQueueMemory.asInstanceOf[Double]

        logger.info(
          "cross cluster test in target rule check" + s"usedCPUPercentage: $usedCPUPercentage, CPUPercentageThreshold: $CPUPercentageThreshold" +
            s"usedMemoryPercentage: $usedMemoryPercentage, MemoryPercentageThreshold: $MemoryPercentageThreshold"
        )

        if (
            usedCPUPercentage >= CPUPercentageThreshold || usedMemoryPercentage >= MemoryPercentageThreshold
        ) {
          throw new RMWarnException(
            RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
            s"usedCPUPercentage: $usedCPUPercentage, CPUPercentageThreshold: $CPUPercentageThreshold" +
              s"usedMemoryPercentage: $usedMemoryPercentage, MemoryPercentageThreshold: $MemoryPercentageThreshold"
          )
        }
      } else {
        throw new RMWarnException(
          RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
          s"leftResource.queueCores: ${leftResource.getQueueCores}, CPUThreshold: $CPUThreshold," +
            s"leftQueueMemory: $leftQueueMemory, MemoryThreshold: $MemoryThreshold"
        )
      }
    }
  }

  def originClusterRuleCheck(
      usedResource: YarnResource,
      maxResource: YarnResource,
      CPUPercentageThreshold: Double,
      MemoryPercentageThreshold: Double
  ): Unit = {
    if (usedResource != null && maxResource != null) {

      val usedCPUPercentage =
        usedResource.getQueueCores.asInstanceOf[Double] / maxResource.getQueueCores
          .asInstanceOf[Double]
      val usedMemoryPercentage = usedResource.getQueueMemory
        .asInstanceOf[Double] / maxResource.getQueueMemory.asInstanceOf[Double]

      logger.info(
        "cross cluster test in origin rule check" + s"usedCPUPercentage: $usedCPUPercentage, CPUPercentageThreshold: $CPUPercentageThreshold" +
          s"usedMemoryPercentage: $usedMemoryPercentage, MemoryPercentageThreshold: $MemoryPercentageThreshold"
      )

      if (
          usedCPUPercentage >= CPUPercentageThreshold || usedMemoryPercentage >= MemoryPercentageThreshold
      ) {
        throw new RMWarnException(
          RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
          AMConstant.ORIGIN_CLUSTER_RETRY_DES
        )
      }
    }
  }

  def originClusterResourceCheck(
      engineCreateRequest: EngineCreateRequest,
      maxCapacity: Resource,
      usedCapacity: Resource
  ): Unit = {
    val acrossClusterTask =
      engineCreateRequest.getProperties.getOrDefault(AMConfiguration.ACROSS_CLUSTER_TASK, "false")
    val priorityCluster = engineCreateRequest.getProperties.get(AMConfiguration.PRIORITY_CLUSTER)
    if (
        StringUtils.isNotBlank(acrossClusterTask) && acrossClusterTask.toBoolean && StringUtils
          .isNotBlank(priorityCluster) && priorityCluster.equals(
          AMConfiguration.PRIORITY_CLUSTER_ORIGIN
        )
    ) {
      // get origin cluster resource threshold
      val originCPUPercentageThreshold =
        engineCreateRequest.getProperties.get(AMConfiguration.ORIGIN_CPU_PERCENTAGE_THRESHOLD)
      val originMemoryPercentageThreshold =
        engineCreateRequest.getProperties.get(AMConfiguration.ORIGIN_MEMORY_PERCENTAGE_THRESHOLD)
      if (
          StringUtils.isNotBlank(originCPUPercentageThreshold) && StringUtils.isNotBlank(
            originMemoryPercentageThreshold
          )
      ) {
        // judge origin cluster resource in origin threshold
        try {
          AcrossClusterRulesJudgeUtils.originClusterRuleCheck(
            usedCapacity.asInstanceOf[YarnResource],
            maxCapacity.asInstanceOf[YarnResource],
            originCPUPercentageThreshold.toDouble,
            originMemoryPercentageThreshold.toDouble
          )
        } catch {
          // if origin cluster resource gt threshold, throw origin retry exception and change to target cluster next retry;
          case ex: Exception =>
            throw new RMWarnException(
              RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
              ex.getMessage
            )
        }
      }
    }
  }

  def targetClusterResourceCheck(
      labelContainer: RMLabelContainer,
      engineCreateRequest: EngineCreateRequest,
      maxCapacity: Resource,
      usedCapacity: Resource,
      externalResourceService: ExternalResourceService
  ): Unit = {
    val acrossClusterTask =
      engineCreateRequest.getProperties.getOrDefault(AMConfiguration.ACROSS_CLUSTER_TASK, "false")
    val priorityCluster = engineCreateRequest.getProperties.get(AMConfiguration.PRIORITY_CLUSTER)
    if (
        StringUtils.isNotBlank(acrossClusterTask) && acrossClusterTask.toBoolean && StringUtils
          .isNotBlank(priorityCluster) && priorityCluster.equals(
          AMConfiguration.PRIORITY_CLUSTER_TARGET
        )
    ) {
      val leftResource = maxCapacity.minus(usedCapacity)
      // get target cluster resource threshold
      val targetCPUThreshold =
        engineCreateRequest.getProperties.get(AMConfiguration.TARGET_CPU_THRESHOLD)
      val targetMemoryThreshold =
        engineCreateRequest.getProperties.get(AMConfiguration.TARGET_MEMORY_THRESHOLD)
      val targetCPUPercentageThreshold =
        engineCreateRequest.getProperties.get(AMConfiguration.TARGET_CPU_PERCENTAGE_THRESHOLD)
      val targetMemoryPercentageThreshold =
        engineCreateRequest.getProperties.get(AMConfiguration.TARGET_MEMORY_PERCENTAGE_THRESHOLD)
      val clusterCPUPercentageThreshold =
        AMConfiguration.ACROSS_CLUSTER_TOTAL_CPU_PERCENTAGE_THRESHOLD
      val clusterMemoryPercentageThreshold =
        AMConfiguration.ACROSS_CLUSTER_TOTAL_MEMORY_PERCENTAGE_THRESHOLD
      if (
          StringUtils
            .isNotBlank(targetCPUThreshold) && StringUtils.isNotBlank(targetMemoryThreshold)
          && StringUtils.isNotBlank(targetCPUPercentageThreshold) && StringUtils.isNotBlank(
            targetMemoryPercentageThreshold
          )
      ) {
        // judge total target cluster resources in target threshold
        val clusterYarnResource =
          externalResourceService.getResource(
            ResourceType.Yarn,
            labelContainer,
            new YarnResourceIdentifier("root")
          )
        val (clusterMaxCapacity, clusterUsedCapacity) =
          (clusterYarnResource.getMaxResource, clusterYarnResource.getUsedResource)
        // judge target cluster resource in target threshold
        try {
          AcrossClusterRulesJudgeUtils.targetClusterRuleCheck(
            leftResource.asInstanceOf[YarnResource],
            usedCapacity.asInstanceOf[YarnResource],
            maxCapacity.asInstanceOf[YarnResource],
            clusterMaxCapacity.asInstanceOf[YarnResource],
            clusterUsedCapacity.asInstanceOf[YarnResource],
            targetCPUThreshold.toInt,
            targetMemoryThreshold.toInt,
            targetCPUPercentageThreshold.toDouble,
            targetMemoryPercentageThreshold.toDouble,
            clusterCPUPercentageThreshold,
            clusterMemoryPercentageThreshold
          )
        } catch {
          // if target cluster resource gt threshold, throw target retry exception and change to normal task next retry;
          case ex: Exception =>
            throw new RMWarnException(
              RMErrorCode.ACROSS_CLUSTER_RULE_FAILED.getErrorCode,
              ex.getMessage
            )
        }
      }
    }
  }

}
