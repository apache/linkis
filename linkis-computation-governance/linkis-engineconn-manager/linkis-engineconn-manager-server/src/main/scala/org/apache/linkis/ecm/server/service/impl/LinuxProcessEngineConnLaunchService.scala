/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.ecm.server.service.impl

import org.apache.linkis.ecm.core.launch.{DiscoveryMsgGenerator, EngineConnLaunch, EurekaDiscoveryMsgGenerator}
import org.apache.linkis.ecm.linux.launch.LinuxProcessEngineConnLaunch
import org.apache.linkis.ecm.server.conf.ECMConfiguration._
import org.apache.linkis.manager.common.entity.node.EngineNode
import org.apache.linkis.manager.engineplugin.common.launch.entity.{EngineConnBuildRequest, EngineConnLaunchRequest}
import org.apache.linkis.message.annotation.Receiver
import org.apache.linkis.message.builder.ServiceMethodContext
import org.apache.linkis.message.conf.MessageSchedulerConf._
import org.apache.linkis.rpc.Sender

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
