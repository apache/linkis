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

package com.webank.wedatasphere.linkis.manager.am.conf

import java.util

import com.webank.wedatasphere.linkis.common.conf.Configuration
import com.webank.wedatasphere.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, RequestQueryGlobalConfig, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import com.webank.wedatasphere.linkis.protocol.CacheableProtocol
import com.webank.wedatasphere.linkis.rpc.RPCMapCache



object ConfigurationMapCache {

  val globalMapCache = new RPCMapCache[UserCreatorLabel, String, String](
    Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue) {
    override protected def createRequest(userCreatorLabel: UserCreatorLabel): CacheableProtocol = RequestQueryGlobalConfig(userCreatorLabel.getUser)

    override protected def createMap(any: Any): util.Map[String, String] = any match {
      case response: ResponseQueryConfig => response.getKeyAndValue
    }
  }

  val engineMapCache = new RPCMapCache[(UserCreatorLabel, EngineTypeLabel), String, String](
    Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue) {
    override protected def createRequest(labelTuple: (UserCreatorLabel, EngineTypeLabel)): CacheableProtocol =
      RequestQueryEngineConfig(labelTuple._1, labelTuple._2)

    override protected def createMap(any: Any): util.Map[String, String] = any match {
      case response: ResponseQueryConfig => response.getKeyAndValue
    }
  }
}
