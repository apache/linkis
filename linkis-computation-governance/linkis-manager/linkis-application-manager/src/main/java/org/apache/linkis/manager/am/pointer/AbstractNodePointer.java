/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.am.pointer;

import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.protocol.node.*;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.rpc.Sender;

public abstract class AbstractNodePointer implements NodePointer {

  protected Sender getSender() {
    return Sender.getSender(getNode().getServiceInstance());
  }

  /**
   * 向对应的Node发送请求获取节点状态
   *
   * @return
   */
  @Override
  public NodeStatus getNodeStatus() {
    Sender sender = getSender();
    ResponseNodeStatus responseStatus = (ResponseNodeStatus) sender.ask(new RequestNodeStatus());
    return responseStatus.getNodeStatus();
  }

  /**
   * 向对应的Node发送请求获取节点心跳信息
   *
   * @return
   */
  @Override
  public NodeHeartbeatMsg getNodeHeartbeatMsg() {
    Sender sender = getSender();
    NodeHeartbeatMsg heartbeatMsg = (NodeHeartbeatMsg) sender.ask(new NodeHeartbeatRequest());
    return heartbeatMsg;
  }

  /**
   * 向对应的Node发送Kill 请求
   *
   * @return
   */
  @Override
  public void stopNode() {
    Sender sender = getSender();
    sender.send(new StopNodeRequest());
  }

  /**
   * 向对应的Node Label 更新请求
   *
   * @return
   */
  @Override
  public void updateLabels(Label[] labels) {}

  @Override
  public void updateNodeHealthyRequest(NodeHealthyRequest nodeHealthyRequest) {
    getSender().send(nodeHealthyRequest);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Node) {
      Node nodeB = (Node) obj;
      return getNode().getServiceInstance().equals(nodeB.getServiceInstance());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getNode().getServiceInstance().hashCode();
  }
}
