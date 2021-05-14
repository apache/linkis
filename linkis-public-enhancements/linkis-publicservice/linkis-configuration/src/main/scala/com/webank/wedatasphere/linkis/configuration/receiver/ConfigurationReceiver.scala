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

package com.webank.wedatasphere.linkis.configuration.receiver

import com.webank.wedatasphere.linkis.configuration.service.ConfigurationService
import com.webank.wedatasphere.linkis.governance.common.protocol.conf.{RequestConfigByLabel, RequestQueryEngineConfig, RequestQueryEngineTypeDefault, RequestQueryGlobalConfig}
import com.webank.wedatasphere.linkis.rpc.{Receiver, Sender}

import scala.concurrent.duration.Duration

class ConfigurationReceiver extends Receiver{

  private var configurationService:ConfigurationService = _

  def this(configurationService:ConfigurationService) = {
    this()
    this.configurationService = configurationService
  }

  override def receive(message: Any, sender: Sender): Unit = {}

  override def receiveAndReply(message: Any, sender: Sender): Any = message match {
    case RequestQueryGlobalConfig(username) => configurationService.queryGlobalConfig(username)
    case RequestQueryEngineTypeDefault(engineType) => configurationService.queryDefaultEngineConfig(engineType)
    case RequestQueryEngineConfig(userCreatorLabel,engineTypeLabel,filter) => configurationService.queryConfig(userCreatorLabel,engineTypeLabel,filter)
    case RequestConfigByLabel(labelList,isMerge) => configurationService.queryConfigByLabel(labelList, isMerge)
  }

  override def receiveAndReply(message: Any, duration: Duration, sender: Sender): Any = {}
}
