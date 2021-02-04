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
import com.webank.wedatasphere.linkis.enginemanager.{Engine, EngineHook}
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngine, RequestNewEngine, TimeoutRequestNewEngine}

/**
  * Created by johnnwang on 2018/11/8.
  */

class IOEngineHook extends EngineHook with Logging{

  override def beforeCreateSession(requestEngine: RequestEngine): RequestEngine = {
    val newUser = IOEngineManagerConfiguration.IO_USER.getValue(requestEngine.properties)
    info(s"switch user and new User: $newUser")
    val newRequest = requestEngine match{
      case RequestNewEngine(creator, user, properties) => RequestNewEngine(creator, newUser, properties)
      case TimeoutRequestNewEngine(timeout,user, creator, properties) => TimeoutRequestNewEngine(timeout, newUser, creator, properties)
      case _ =>  throw new IOErrorException(53001, "Request to create engine failed, engine request type is incorrect(请求创建引擎失败，引擎请求类型有误)")
    }
    newRequest
  }

  override def afterCreatedSession(engine: Engine, requestEngine: RequestEngine): Unit = {

  }
}
