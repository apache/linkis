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

package org.apache.linkis.manager.am.manager;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateResponse;

import java.util.List;

public interface EngineNodeManager {

  List<EngineNode> listEngines(String user);

  EngineNode getEngineNode(ServiceInstance serviceInstance);

  EngineNode getEngineNodeInfo(EngineNode engineNode);

  EngineNode getEngineNodeInfo(ServiceInstance serviceInstance);

  EngineNode getEngineNodeInfoByDB(EngineNode engineNode);

  /**
   * Get detailed engine information from the persistence
   *
   * @param scoreServiceInstances
   * @return
   */
  EngineNode[] getEngineNodes(ScoreServiceInstance[] scoreServiceInstances);

  void updateEngineStatus(
      ServiceInstance serviceInstance, NodeStatus fromState, NodeStatus toState);

  void addEngineNode(EngineNode engineNode);

  void updateEngineNode(ServiceInstance serviceInstance, EngineNode engineNode);

  void updateEngine(EngineNode engineNode);

  void deleteEngineNode(EngineNode engineNode);

  EngineNode switchEngine(EngineNode engineNode);

  EngineNode reuseEngine(EngineNode engineNode);

  EngineNode useEngine(EngineNode engineNode, long timeout);

  EngineOperateResponse executeOperation(EngineNode engineNode, EngineOperateRequest request);
}
