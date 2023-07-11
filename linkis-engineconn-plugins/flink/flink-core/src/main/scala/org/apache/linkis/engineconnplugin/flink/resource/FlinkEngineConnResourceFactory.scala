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

package org.apache.linkis.engineconnplugin.flink.resource

import org.apache.linkis.common.utils.ByteTimeUtils
import org.apache.linkis.engineconnplugin.flink.config.FlinkResourceConfiguration._
import org.apache.linkis.manager.common.entity.resource.{
  DriverAndYarnResource,
  LoadInstanceResource,
  Resource,
  YarnResource
}
import org.apache.linkis.manager.engineplugin.common.resource.AbstractEngineResourceFactory

class FlinkEngineConnResourceFactory extends AbstractEngineResourceFactory {

  override def getRequestResource(properties: java.util.Map[String, String]): Resource = {
    val containers = if (properties.containsKey(LINKIS_FLINK_CONTAINERS)) {
      val containers = LINKIS_FLINK_CONTAINERS.getValue(properties)
      properties.put(
        FLINK_APP_DEFAULT_PARALLELISM.key,
        String.valueOf(containers * LINKIS_FLINK_TASK_SLOTS.getValue(properties))
      )
      containers
    } else {
      math.round(
        FLINK_APP_DEFAULT_PARALLELISM.getValue(properties) * 1.0f / LINKIS_FLINK_TASK_SLOTS
          .getValue(properties)
      )
    }
    val yarnMemory = ByteTimeUtils.byteStringAsBytes(
      LINKIS_FLINK_TASK_MANAGER_MEMORY.getValue(properties) * containers + "M"
    ) +
      ByteTimeUtils.byteStringAsBytes(LINKIS_FLINK_JOB_MANAGER_MEMORY.getValue(properties) + "M")
    val yarnCores = LINKIS_FLINK_TASK_MANAGER_CPU_CORES.getValue(properties) * containers + 1
    new DriverAndYarnResource(
      new LoadInstanceResource(
        ByteTimeUtils.byteStringAsBytes(LINKIS_FLINK_CLIENT_MEMORY.getValue(properties) + "M"),
        LINKIS_FLINK_CLIENT_CORES,
        1
      ),
      new YarnResource(yarnMemory, yarnCores, 0, LINKIS_QUEUE_NAME.getValue(properties))
    )
  }

}
