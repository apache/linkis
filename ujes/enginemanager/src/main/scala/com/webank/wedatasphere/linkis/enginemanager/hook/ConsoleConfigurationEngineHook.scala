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

package com.webank.wedatasphere.linkis.enginemanager.hook

import com.webank.wedatasphere.linkis.enginemanager.cache.ConfigurationMapCache
import com.webank.wedatasphere.linkis.enginemanager.{Engine, EngineHook}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.server.JMap

/**
  * Function:
 * 1. Request the engine parameter module to get the resources set by the user
 * 2. Compare the resource settings of the user script scope to get the aggregated resources
  * 作用：
  * 1. 请求引擎参数模块，获取用户设置的资源
  * 2. 对比用户脚本作用域的资源设置，获取汇总的资源
  * created by johnnwang
  */
class ConsoleConfigurationEngineHook extends EngineHook {

  override def beforeCreateSession(requestEngine: RequestEngine): RequestEngine = {
    val properties = new JMap[String, String]
    val globalConfig = ConfigurationMapCache.globalMapCache.getCacheMap(requestEngine)
    properties.putAll(globalConfig)
    val engineConfig = ConfigurationMapCache.engineMapCache.getCacheMap(requestEngine)
    properties.putAll(engineConfig)
    properties.putAll(requestEngine.properties)
    //put all properties
    requestEngine.properties.putAll(properties)
    requestEngine
  }

  override def afterCreatedSession(engine: Engine, requestEngine: RequestEngine): Unit = {}
}
