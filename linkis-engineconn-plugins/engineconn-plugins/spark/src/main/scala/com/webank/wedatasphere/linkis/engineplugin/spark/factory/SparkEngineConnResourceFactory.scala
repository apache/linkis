/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineplugin.spark.factory

import java.util

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging}
import com.webank.wedatasphere.linkis.engineplugin.spark.config.SparkResourceConfiguration._
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{DriverAndYarnResource, LoadInstanceResource, Resource, YarnResource}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.resource.AbstractEngineResourceFactory

/**
  *
  * @date 2020/12/17 16:44
  */
class SparkEngineConnResourceFactory extends AbstractEngineResourceFactory with Logging {

  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {
    val executorNum = LINKIS_SPARK_EXECUTOR_INSTANCES.getValue(properties)
    new DriverAndYarnResource(
      new LoadInstanceResource(ByteTimeUtils.byteStringAsBytes(LINKIS_SPARK_DRIVER_MEMORY.getValue(properties) + "G"),
        LINKIS_SPARK_DRIVER_CORES,
        1),
      new YarnResource(ByteTimeUtils.byteStringAsBytes(LINKIS_SPARK_EXECUTOR_MEMORY.getValue(properties) * executorNum + "G"),
        LINKIS_SPARK_EXECUTOR_CORES.getValue(properties) * executorNum,
        0,
        LINKIS_QUEUE_NAME.getValue(properties))
    )
  }
}
