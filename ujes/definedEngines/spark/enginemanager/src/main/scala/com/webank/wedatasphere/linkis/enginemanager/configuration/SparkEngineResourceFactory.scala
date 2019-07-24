/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.enginemanager.configuration

import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils
import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineResourceFactory
import com.webank.wedatasphere.linkis.enginemanager.configuration.SparkResourceConfiguration._
import com.webank.wedatasphere.linkis.resourcemanager.{DriverAndYarnResource, LoadInstanceResource, YarnResource}
import org.springframework.stereotype.Component

/**
  * Created by allenlliu on 2019/4/8.
  */
@Component("engineResourceFactory")
class SparkEngineResourceFactory extends AbstractEngineResourceFactory {

  override protected def getRequestResource(properties: java.util.Map[String, String]): DriverAndYarnResource = {
    val executorNum = DWC_SPARK_EXECUTOR_INSTANCES.getValue(properties)
    new DriverAndYarnResource(
      new LoadInstanceResource(ByteTimeUtils.byteStringAsBytes(DWC_SPARK_DRIVER_MEMORY.getValue(properties) + "G"),
        DWC_SPARK_DRIVER_CORES,
        1),
      new YarnResource(ByteTimeUtils.byteStringAsBytes(DWC_SPARK_EXECUTOR_MEMORY.getValue(properties) * executorNum + "G"),
        DWC_SPARK_EXECUTOR_CORES.getValue(properties) * executorNum,
        0,
        DWC_QUEUE_NAME.getValue(properties))
    )
  }
}
