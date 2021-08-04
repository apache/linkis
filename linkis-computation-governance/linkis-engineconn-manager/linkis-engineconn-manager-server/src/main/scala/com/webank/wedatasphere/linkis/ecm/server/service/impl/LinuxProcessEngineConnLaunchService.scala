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

package com.webank.wedatasphere.linkis.ecm.server.service.impl

import com.webank.wedatasphere.linkis.ecm.core.launch.{DiscoveryMsgGenerator, EngineConnLaunch, EurekaDiscoveryMsgGenerator}
import com.webank.wedatasphere.linkis.ecm.linux.launch.LinuxProcessEngineConnLaunch
import com.webank.wedatasphere.linkis.ecm.server.conf.ECMConfiguration._
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode
import com.webank.wedatasphere.linkis.manager.engineplugin.common.launch.entity.{EngineConnBuildRequest, EngineConnLaunchRequest}
import com.webank.wedatasphere.linkis.message.annotation.Receiver
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext
import com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf._
import com.webank.wedatasphere.linkis.rpc.Sender

import scala.concurrent.duration.Duration


class LinuxProcessEngineConnLaunchService extends ProcessEngineConnLaunchService {


  @Receiver
  def launchEngineConn(engineConnBuildRequest: EngineConnBuildRequest, smc: ServiceMethodContext): EngineNode = {
    Sender.getSender(ENGINECONN_PLUGIN_SPRING_NAME).ask(engineConnBuildRequest) match {
      case request: EngineConnLaunchRequest if ENGINECONN_CREATE_DURATION._1 != 0L =>
        launchEngineConn(request, ENGINECONN_CREATE_DURATION._1)
      case request: EngineConnLaunchRequest =>
        launchEngineConn(request, smc.getAttribute(DURATION_KEY).asInstanceOf[Duration]._1)
    }
  }

  override def launchEngineConn(engineConnBuildRequest: EngineConnBuildRequest): EngineNode = {
    Sender.getSender(ENGINECONN_PLUGIN_SPRING_NAME).ask(engineConnBuildRequest) match {
      case request: EngineConnLaunchRequest =>
        launchEngineConn(request, ENGINECONN_CREATE_DURATION._1)
    }
  }

  def createDiscoveryMsgGenerator: DiscoveryMsgGenerator = new EurekaDiscoveryMsgGenerator

  override def createEngineConnLaunch: EngineConnLaunch = {
    val launch = new LinuxProcessEngineConnLaunch
    launch.setDiscoveryMsgGenerator(createDiscoveryMsgGenerator)
    launch
  }

}
