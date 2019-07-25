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

package com.webank.wedatasphere.linkis.enginemanager.hive.conf

import java.util

import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineResourceFactory
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, Resource}
import org.springframework.stereotype.Component

/**
  * created by cooperyang on 2019/1/30
  * Description:
  */
@Component("engineResourceFactory")
class HiveEngineResourceFactory extends AbstractEngineResourceFactory{
  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {

    if (properties.containsKey(HiveResourceConfiguration.HIVE_ENGINE_REQUEST_MEMORY.key)){
      val settingClientMemory = properties.get(HiveResourceConfiguration.HIVE_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")){
        properties.put(HiveResourceConfiguration.HIVE_ENGINE_REQUEST_MEMORY.key, settingClientMemory + "g")
      }
    }

    new LoadInstanceResource(HiveResourceConfiguration.HIVE_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
      HiveResourceConfiguration.HIVE_ENGINE_REQUEST_CORES.getValue(properties), HiveResourceConfiguration.HIVE_ENGINE_REQUEST_INSTANCE.getValue(properties))
  }
}
