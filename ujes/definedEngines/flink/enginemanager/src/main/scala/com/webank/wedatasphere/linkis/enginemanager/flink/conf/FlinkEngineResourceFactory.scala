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

package com.webank.wedatasphere.linkis.enginemanager.flink.conf

import java.util

import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineResourceFactory
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, Resource}
import org.springframework.stereotype.Component

/**
 *
 * Created by liangqilang on 2019-11-01 zhuhui@kanzhun.com
 * 
 */
@Component("engineResourceFactory")
class FlinkEngineResourceFactory extends AbstractEngineResourceFactory{
  
  override protected def getRequestResource(properties: util.Map[String, String]): Resource = {
    new LoadInstanceResource(
      FlinkResourceConfiguration.FLINK_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
      FlinkResourceConfiguration.FLINK_ENGINE_REQUEST_CORES.getValue(properties),
      FlinkResourceConfiguration.FLINK_ENGINE_REQUEST_INSTANCE.getValue(properties)
      )
  }
}
