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

package com.webank.wedatasphere.linkis.enginemanager

import java.net.ServerSocket

import com.webank.wedatasphere.linkis.common.conf.DWCArgumentsParser
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration
import com.webank.wedatasphere.linkis.enginemanager.exception.EngineManagerErrorException
import com.webank.wedatasphere.linkis.enginemanager.impl.UserTimeoutEngineResource
import com.webank.wedatasphere.linkis.enginemanager.process.{CommonProcessEngine, ProcessEngine, ProcessEngineBuilder}
import com.webank.wedatasphere.linkis.protocol.engine.{EngineCallback, RequestEngine}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.server.{JMap, toScalaMap}
import org.apache.commons.io.IOUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by johnnwang on 2018/10/11.
  */
abstract class AbstractEngineCreator extends EngineCreator {

  private val inInitPorts = ArrayBuffer[Int]()

  private def getAvailablePort: Int = synchronized {
    var port = AbstractEngineCreator.getNewPort
    while(inInitPorts.contains(port)) port = AbstractEngineCreator.getNewPort
    inInitPorts += port
    port
  }

  def removePort(port: Int): Unit = inInitPorts -= port

  protected def createProcessEngineBuilder(): ProcessEngineBuilder
  protected def getExtractSpringConfigs(requestEngine: RequestEngine): JMap[String, String] = {
    val springConf = new JMap[String, String]
    requestEngine.properties.keysIterator.filter(_.startsWith("spring.")).foreach(key => springConf.put(key.substring(7), requestEngine.properties.get(key)))
    springConf
  }
  protected def createEngine(processEngineBuilder:ProcessEngineBuilder,parser:DWCArgumentsParser):ProcessEngine={
     processEngineBuilder.getEngineResource match {
      case timeout: UserTimeoutEngineResource =>
        new CommonProcessEngine(processEngineBuilder, parser, timeout.getTimeout)
      case _ =>
        new CommonProcessEngine(processEngineBuilder, parser)
    }
  }

  override def create(ticketId: String, engineRequest: EngineResource, request: RequestEngine): Engine = {
    val port = getAvailablePort
    val processEngineBuilder = createProcessEngineBuilder()
    processEngineBuilder.setPort(port)
    processEngineBuilder.build(engineRequest, request)
    val parser = new DWCArgumentsParser
    var springConf = Map("spring.application.name" -> EngineManagerConfiguration.ENGINE_SPRING_APPLICATION_NAME.getValue,
      "server.port" -> port.toString, "spring.profiles.active" -> "engine",
      "logging.config" -> "classpath:log4j2-engine.xml",
      "eureka.client.serviceUrl.defaultZone" -> EngineManagerReceiver.getSpringConf("eureka.client.serviceUrl.defaultZone"))
    springConf = springConf ++: getExtractSpringConfigs(request).toMap
    parser.setSpringConf(springConf)
    var dwcConf = Map("ticketId" -> ticketId, "creator" -> request.creator, "user" -> request.user) ++:
      EngineCallback.callbackToMap(EngineCallback(Sender.getThisServiceInstance.getApplicationName, Sender.getThisServiceInstance.getInstance))
    if(request.properties.exists{case (k, v) => k.contains(" ") || (v != null && v.contains(" "))})
      throw new EngineManagerErrorException(30000, "Startup parameters contain spaces!(启动参数中包含空格！)")
    dwcConf = dwcConf ++: request.properties.toMap
    parser.setDWCConf(dwcConf)
    val engine = createEngine(processEngineBuilder,parser)
    engine.setTicketId(ticketId)
    engine.setPort(port)
    engine match {
      case commonEngine: CommonProcessEngine => commonEngine.setUser(request.user)
      case _ =>
    }
    engine
  }
}
object AbstractEngineCreator {
  private[enginemanager] def getNewPort: Int = {
    val socket = new ServerSocket(0)
    Utils.tryFinally(socket.getLocalPort)(IOUtils.closeQuietly(socket))
  }
}