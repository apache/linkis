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

package com.webank.wedatasphere.linkis.enginemanager.io

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration
import com.webank.wedatasphere.linkis.enginemanager.{AbstractEngineCreator, Engine, EngineResource}
import com.webank.wedatasphere.linkis.enginemanager.process.{JavaProcessEngineBuilder, ProcessEngineBuilder}
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngine, RequestNewEngine, TimeoutRequestNewEngine}
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/10/30.
  */
@Component("executorCreator")
class IODefaultEngineCreator extends AbstractEngineCreator with Logging{

  override protected def createProcessEngineBuilder(): ProcessEngineBuilder = new JavaProcessEngineBuilder {
    override protected def getExtractJavaOpts: String = IOEngineManagerConfiguration.EXTRA_JAVA_OPTS.getValue
    override protected def getAlias(request: RequestEngine): String = EngineManagerConfiguration.ENGINE_SPRING_APPLICATION_NAME.getValue
    override protected def getExtractClasspath: Array[String] = Array.empty
    override protected def classpathCheck(jarOrFiles: Array[String]): Unit = {}
    override protected val addApacheConfigPath: Boolean = true
  }
}
