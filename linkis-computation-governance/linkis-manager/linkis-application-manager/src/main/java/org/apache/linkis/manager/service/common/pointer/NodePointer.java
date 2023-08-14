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

package org.apache.linkis.manager.service.common.pointer;

import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.protocol.node.NodeHealthyRequest;
import org.apache.linkis.manager.common.protocol.node.NodeHeartbeatMsg;
import org.apache.linkis.manager.label.entity.Label;

public interface NodePointer {

  /**
   * 与该远程指针关联的node信息
   *
   * @return
   */
  Node getNode();

  /**
   * 向对应的Node发送请求获取节点状态
   *
   * @return
   */
  NodeStatus getNodeStatus();

  /**
   * 向对应的Node发送请求获取节点心跳信息
   *
   * @return
   */
  NodeHeartbeatMsg getNodeHeartbeatMsg();

  /**
   * 向对应的Node发送Kill 请求
   *
   * @return
   */
  void stopNode();

  /**
   * 向对应的Node发送Label更新 请求
   *
   * @return
   */
  void updateLabels(Label<?>[] labels);

  void updateNodeHealthyRequest(NodeHealthyRequest nodeHealthyRequest);
}
