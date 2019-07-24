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

package com.webank.wedatasphere.linkis.enginemanager.process

import com.webank.wedatasphere.linkis.enginemanager.AbstractEngineResourceFactory
import com.webank.wedatasphere.linkis.enginemanager.util.{PythonConfiguration, PythonResourceConfiguration}
import com.webank.wedatasphere.linkis.resourcemanager._
import org.springframework.stereotype.Component

/**
  * Created by allenlliu on 2019/4/8.
  */

@Component("engineResourceFactory")
class PythonEngineResourceFactory  extends AbstractEngineResourceFactory{
  override protected def getRequestResource(properties: java.util.Map[String, String]): Resource = {

    if (properties.containsKey(PythonConfiguration.PYTHON_JAVA_CLIENT_MEMORY.key)){
      val settingClientMemory = properties.get(PythonConfiguration.PYTHON_JAVA_CLIENT_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")){
        properties.put(PythonConfiguration.PYTHON_JAVA_CLIENT_MEMORY.key, settingClientMemory + "g")
      }
    }


    new LoadInstanceResource(PythonConfiguration.PYTHON_JAVA_CLIENT_MEMORY.getValue(properties).toLong,
      PythonResourceConfiguration.PYTHON_ENGINE_REQUEST_CORES.getValue(properties),PythonResourceConfiguration.PYTHON_ENGINE_REQUEST_INSTANCE.getValue)
  }
}
