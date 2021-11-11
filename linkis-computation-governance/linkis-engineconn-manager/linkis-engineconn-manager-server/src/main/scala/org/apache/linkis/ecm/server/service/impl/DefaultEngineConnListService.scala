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

import java.util
import java.util.concurrent.ConcurrentHashMap

import com.google.common.collect.Interners
import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.core.engineconn.{EngineConn, YarnEngineConn}
import org.apache.linkis.ecm.core.launch.EngineConnLaunchRunner
import org.apache.linkis.ecm.core.listener.{ECMEvent, ECMEventListener}
import org.apache.linkis.ecm.server.LinkisECMApplication
import org.apache.linkis.ecm.server.converter.ECMEngineConverter
import org.apache.linkis.ecm.server.listener._
import org.apache.linkis.ecm.server.service.EngineConnListService
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.resource.{Resource, ResourceType}
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import scala.collection.JavaConversions._

class DefaultEngineConnListService extends EngineConnListService with ECMEventListener with Logging {
  /**
   * key:tickedId,value :engineConn
   */
  private val engineConnMap = new ConcurrentHashMap[String, EngineConn]

  private var engineConnKillService: DefaultEngineConnKillService = _

  val lock = Interners.newWeakInterner[String]

  override def init(): Unit = {}

  override def getEngineConn(engineConnId: String): Option[EngineConn] = Option(engineConnMap.get(engineConnId))

  override def getEngineConns: util.List[EngineConn] = engineConnMap.values().toList

  override def addEngineConn(engineConn: EngineConn): Unit = {
    if (LinkisECMApplication.isReady)
      engineConnMap.put(engineConn.getTickedId, engineConn)
  }

  override def killEngineConn(engineConnId: String): Unit = {
    val conn = engineConnMap.remove(engineConnId)
    if (conn != null) {
      Utils.tryAndWarn{
        conn.close()
        info(s"engineconn ${conn.getPid} was closed.")
      }
    }
  }

  override def getUsedResources: Resource = engineConnMap.values().map(_.getResource.getMinResource).fold(Resource.initResource(ResourceType.Default))(_ + _)

  override def submit(runner: EngineConnLaunchRunner): Option[EngineConn] = ???

  def updateYarnAppId(event: YarnAppIdCallbackEvent): Unit = {
    updateYarnEngineConn(x => x.setApplicationId(event.protocol.applicationId), event.protocol.nodeId)
  }

  def updateYarnEngineConn(implicit updateFunction: YarnEngineConn => Unit, nodeId: String): Unit = {
    lock.intern(nodeId) synchronized {
      engineConnMap.get(nodeId) match {
        case e: YarnEngineConn => updateFunction(e)
        case e: EngineConn =>
          engineConnMap.put(nodeId, ECMEngineConverter.engineConn2YarnEngineConn(e))
      }
    }
  }

  def updateEngineConn(updateFunction: EngineConn => Unit, nodeId: String): Unit = {
    lock.intern(nodeId) synchronized {
      engineConnMap.get(nodeId) match {
        case e: EngineConn => updateFunction(e)
        case _ =>
      }
    }
  }

  def updateYarnInfo(event: YarnInfoCallbackEvent): Unit = {
    updateYarnEngineConn(x => x.setApplicationURL(event.protocol.uri), event.protocol.nodeId)
  }

  def updatePid(event: EngineConnPidCallbackEvent): Unit = {
    updateEngineConn(x => {
      x.setPid(event.protocol.pid)
      x.setServiceInstance(event.protocol.serviceInstance)
    }, event.protocol.ticketId)
  }

  def updateEngineConnStatus(tickedId: String, updateStatus: NodeStatus): Unit = {
    updateEngineConn(x => x.setStatus(updateStatus), tickedId)
    if (NodeStatus.isCompleted(updateStatus)) {
      info(s" from engineConnMap to remove engineconn ticketId ${tickedId}")
      killEngineConn(tickedId)
    }
  }

  override def onEvent(event: ECMEvent): Unit = {
    info(s"Deal event $event")
    event match {
      case event: ECMClosedEvent => shutdownEngineConns(event)
      case event: YarnAppIdCallbackEvent => updateYarnAppId(event)
      case event: YarnInfoCallbackEvent => updateYarnInfo(event)
      case event: EngineConnPidCallbackEvent => updatePid(event)
      case EngineConnAddEvent(engineConn) => addEngineConn(engineConn)
      case EngineConnStatusChangeEvent(tickedId, updateStatus) => updateEngineConnStatus(tickedId, updateStatus)
      case _ =>
    }
  }

  private def getEngineConnKillService(): DefaultEngineConnKillService = {
    if(engineConnKillService == null){
      val applicationContext = DataWorkCloudApplication.getApplicationContext
      engineConnKillService = applicationContext.getBean(classOf[DefaultEngineConnKillService])
    }
    engineConnKillService
  }

  private def shutdownEngineConns(event: ECMClosedEvent): Unit = {
    info("start to kill all engines belonging the ecm")
    engineConnMap.values().foreach(engineconn => {
      info(s"start to kill engine, pid:${engineconn.getPid}")
      val engineStopRequest = new EngineStopRequest()
      engineStopRequest.setServiceInstance(engineconn.getServiceInstance)
      getEngineConnKillService.dealEngineConnStop(engineStopRequest)
    })
    info("Done! success to kill all engines belonging the ecm")
  }

}
