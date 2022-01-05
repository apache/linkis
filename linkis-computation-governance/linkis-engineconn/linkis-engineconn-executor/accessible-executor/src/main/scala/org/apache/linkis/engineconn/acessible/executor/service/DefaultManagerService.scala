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
 
package org.apache.linkis.engineconn.acessible.executor.service

import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.executor.service.ManagerService
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.engine.EngineConnReleaseRequest
import org.apache.linkis.manager.common.protocol.label.LabelReportRequest
import org.apache.linkis.manager.common.protocol.node.{NodeHeartbeatMsg, ResponseNodeStatus}
import org.apache.linkis.manager.common.protocol.resource.ResourceUsedProtocol
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel
import org.apache.linkis.rpc.Sender

import scala.collection.JavaConverters._

class DefaultManagerService extends ManagerService with Logging{


  protected def getManagerSender: Sender = {
    Sender.getSender(GovernanceCommonConf.MANAGER_SPRING_NAME.getValue)
  }

  protected def getEngineConnManagerSender: Sender = {
    Sender.getSender(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue)
  }


  override def labelReport(labels: util.List[Label[_]]): Unit = {
    if (null == labels || labels.isEmpty) {
      info("labels is empty, Not reported")
      return
    }
    val reportLabel = labels.asScala.filter(_.isInstanceOf[EngineTypeLabel])
    if (reportLabel.isEmpty) {
      info("engineType labels is empty, Not reported")
      return
    }
    val labelReportRequest = LabelReportRequest(reportLabel.asJava, Sender.getThisServiceInstance)
    getManagerSender.send(labelReportRequest)
  }


  override def statusReport(status: NodeStatus): Unit = {
    val responseNodeStatus = new ResponseNodeStatus
    responseNodeStatus.setNodeStatus(status)
    getManagerSender.send(responseNodeStatus)
  }

  override def requestReleaseEngineConn(engineConnReleaseRequest: EngineConnReleaseRequest): Unit = {
    getManagerSender.send(engineConnReleaseRequest)
  }

  override def heartbeatReport(nodeHeartbeatMsg: NodeHeartbeatMsg): Unit = {
    getManagerSender.send(nodeHeartbeatMsg)
    info(s"success to send engine heartbeat report to ${Sender.getInstances(GovernanceCommonConf.MANAGER_SPRING_NAME.getValue).map(_.getInstance).mkString(",")},status:${nodeHeartbeatMsg.getStatus},msg:${nodeHeartbeatMsg.getHeartBeatMsg}")
  }

  override def reportUsedResource(resourceUsedProtocol: ResourceUsedProtocol): Unit = {
    getManagerSender.send(resourceUsedProtocol)
  }

}
