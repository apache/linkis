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

package org.apache.linkis.engineplugin.spark.factory

import org.apache.linkis.common.utils.{ByteTimeUtils, Logging}
import org.apache.linkis.engineplugin.spark.config.SparkResourceConfiguration._
import org.apache.linkis.manager.common.entity.resource.{
  DriverAndKubernetesResource,
  DriverAndYarnResource,
  KubernetesResource,
  LoadInstanceResource,
  Resource,
  YarnResource
}
import org.apache.linkis.manager.engineplugin.common.resource.{
  AbstractEngineResourceFactory,
  EngineResourceRequest
}
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel
import org.apache.linkis.manager.label.utils.LabelUtil

import org.apache.commons.lang3.StringUtils

import java.util

import io.fabric8.kubernetes.api.model.Quantity

class SparkEngineConnResourceFactory extends AbstractEngineResourceFactory with Logging {

  override protected def getMinRequestResource(
      engineResourceRequest: EngineResourceRequest
  ): Resource = {
    val clusterLabel = LabelUtil.getLabelFromList[ClusterLabel](engineResourceRequest.labels)
    if (clusterLabel != null && clusterLabel.getClusterType == "K8S") {
      getRequestKubernetesResource(engineResourceRequest.properties)
    } else {
      getRequestResource(engineResourceRequest.properties)
    }
  }

  override protected def getMaxRequestResource(
      engineResourceRequest: EngineResourceRequest
  ): Resource = {
    val clusterLabel = LabelUtil.getLabelFromList[ClusterLabel](engineResourceRequest.labels)
    if (clusterLabel != null && clusterLabel.getStringValue.startsWith("K8S")) {
      getRequestKubernetesResource(engineResourceRequest.properties)
    } else {
      getRequestResource(engineResourceRequest.properties)
    }
  }

  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {
    val executorNum = LINKIS_SPARK_EXECUTOR_INSTANCES.getValue(properties)
    val executorMemory = LINKIS_SPARK_EXECUTOR_MEMORY.getValue(properties)
    val executorMemoryWithUnit = if (StringUtils.isNumeric(executorMemory)) {
      executorMemory + "g"
    } else {
      executorMemory
    }
    val driverMemory = LINKIS_SPARK_DRIVER_MEMORY.getValue(properties)
    val driverMemoryWithUnit = if (StringUtils.isNumeric(driverMemory)) {
      driverMemory + "g"
    } else {
      driverMemory
    }
    val driverCores = LINKIS_SPARK_DRIVER_CORES.getValue(properties)
    new DriverAndYarnResource(
      new LoadInstanceResource(
        ByteTimeUtils.byteStringAsBytes(driverMemoryWithUnit),
        driverCores,
        1
      ),
      new YarnResource(
        ByteTimeUtils.byteStringAsBytes(executorMemoryWithUnit) * executorNum,
        LINKIS_SPARK_EXECUTOR_CORES.getValue(properties) * executorNum,
        0,
        LINKIS_QUEUE_NAME.getValue(properties)
      )
    )
  }

  def getRequestKubernetesResource(properties: util.Map[String, String]): Resource = {
    val executorNum = LINKIS_SPARK_EXECUTOR_INSTANCES.getValue(properties)
    val executorCores = if (properties.containsKey(LINKIS_SPARK_KUBERNETES_EXECUTOR_CORES)) {
      val executorCoresQuantity =
        Quantity.parse(LINKIS_SPARK_KUBERNETES_EXECUTOR_CORES.getValue(properties))
      (Quantity.getAmountInBytes(executorCoresQuantity).doubleValue() * 1000).toLong
    } else {
      LINKIS_SPARK_EXECUTOR_CORES.getValue(properties) * 1000L
    }
    val executorMemory = LINKIS_SPARK_EXECUTOR_MEMORY.getValue(properties)
    val executorMemoryWithUnit = if (StringUtils.isNumeric(executorMemory)) {
      executorMemory + "g"
    } else {
      executorMemory
    }
    val driverCores = if (properties.containsKey(LINKIS_SPARK_KUBERNETES_DRIVER_CORES)) {
      val executorCoresQuantity =
        Quantity.parse(LINKIS_SPARK_KUBERNETES_DRIVER_CORES.getValue(properties))
      (Quantity.getAmountInBytes(executorCoresQuantity).doubleValue() * 1000).toLong
    } else {
      LINKIS_SPARK_DRIVER_CORES.getValue(properties) * 1000L
    }
    val driverMemory = LINKIS_SPARK_DRIVER_MEMORY.getValue(properties)
    val driverMemoryWithUnit = if (StringUtils.isNumeric(driverMemory)) {
      driverMemory + "g"
    } else {
      driverMemory
    }
    val totalExecutorMemory = ByteTimeUtils.byteStringAsBytes(
      executorMemoryWithUnit
    ) * executorNum + ByteTimeUtils.byteStringAsBytes(driverMemoryWithUnit)
    val totalExecutorCores = executorCores * executorNum + driverCores
    // logger.info(s"总需求内存:$totalExecutorMemory, 总需求核数$totalExecutorCores")
    new DriverAndKubernetesResource(
      new LoadInstanceResource(0, 0, 0),
      new KubernetesResource(totalExecutorMemory, totalExecutorCores)
    )
  }

}
