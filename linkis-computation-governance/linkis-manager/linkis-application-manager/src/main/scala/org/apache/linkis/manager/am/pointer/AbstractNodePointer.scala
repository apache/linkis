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
 
package org.apache.linkis.manager.am.pointer

import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.protocol.node._
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.service.common.pointer.NodePointer
import org.apache.linkis.rpc.Sender


abstract class AbstractNodePointer extends NodePointer with Logging {


  protected def getSender: Sender = {
    Sender.getSender(getNode().getServiceInstance)
  }

  /**
    * 向对应的Node发送请求获取节点状态
    *
    * @return
    */
  override def getNodeStatus(): NodeStatus = {
    val sender = getSender
    sender.ask(new RequestNodeStatus) match {
      case responseStatus: ResponseNodeStatus =>
        responseStatus.getNodeStatus
      case warn: WarnException => throw warn
    }
  }

  /**
    * 向对应的Node发送请求获取节点心跳信息
    *
    * @return
    */
  override def getNodeHeartbeatMsg(): NodeHeartbeatMsg = {
    val sender = getSender
    sender.ask(new NodeHeartbeatRequest) match {
      case heartbeatMsg: NodeHeartbeatMsg =>
        heartbeatMsg
      case warn: WarnException => throw warn
    }
  }

  /**
    * 向对应的Node发送Kill 请求
    *
    * @return
    */
  override def stopNode(): Unit = {
    val sender = getSender
    sender.send(new StopNodeRequest)
  }

  /**
    * 向对应的Node Label 更新请求
    *
    * @return
    */
  override def updateLabels(labels: Array[Label[_]]): Unit = {

  }



}
