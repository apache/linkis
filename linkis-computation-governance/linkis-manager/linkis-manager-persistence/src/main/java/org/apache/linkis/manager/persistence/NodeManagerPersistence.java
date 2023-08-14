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

package org.apache.linkis.manager.persistence;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.exception.PersistenceErrorException;

import java.util.List;

public interface NodeManagerPersistence {

  /**
   * 保存node
   *
   * @param node
   * @throws PersistenceErrorException
   */
  void addNodeInstance(Node node) throws PersistenceErrorException;

  void updateEngineNode(ServiceInstance serviceInstance, Node node)
      throws PersistenceErrorException, LinkisRetryException;

  /**
   * 移除node
   *
   * @param node
   * @throws PersistenceErrorException
   */
  void removeNodeInstance(Node node);

  /**
   * 根据 owner 获取node列表
   *
   * @param owner
   * @return
   * @throws PersistenceErrorException
   */
  List<Node> getNodes(String owner);

  /**
   * 获取所有node列表
   *
   * @return
   * @throws PersistenceErrorException
   */
  List<Node> getAllNodes();

  /**
   * 更新node信息
   *
   * @param node
   * @throws PersistenceErrorException
   */
  void updateNodeInstance(Node node);

  /**
   * 根据 servericeinstance 获取 Node
   *
   * @param serviceInstance
   * @return
   * @throws PersistenceErrorException
   */
  Node getNode(ServiceInstance serviceInstance);

  /**
   * 1. 插入Engine 2. 插入Engine和EM关系
   *
   * @param engineNode
   * @throws PersistenceErrorException
   */
  void addEngineNode(EngineNode engineNode) throws PersistenceErrorException;

  /**
   * 1. 删除Engine和Em关系，以及清理和Engine相关的metrics信息 2. 删除Engine本身
   *
   * @param engineNode
   * @throws PersistenceErrorException
   */
  void deleteEngineNode(EngineNode engineNode);

  /**
   * 1. 通过Engine的ServiceInstance，获取Engine的信息和EM信息
   *
   * @param serviceInstance
   * @return
   * @throws PersistenceErrorException
   */
  EngineNode getEngineNode(ServiceInstance serviceInstance);

  EngineNode getEngineNodeByTicketId(String ticketId);

  /**
   * 通过Em的ServiceInstance 获取EM下面Engine的列表
   *
   * @param serviceInstance
   * @return
   * @throws PersistenceErrorException
   */
  List<EngineNode> getEngineNodeByEM(ServiceInstance serviceInstance);

  /**
   * Get the information of the Engine and EM information through the ServiceInstance of the Engine
   * (batch query)
   *
   * @param serviceInstances
   * @return
   * @throws PersistenceErrorException
   */
  List<EngineNode> getEngineNodeByServiceInstance(List<ServiceInstance> serviceInstances);

  /**
   * Get the node list according to ownerList
   *
   * @param owner
   * @return
   * @throws PersistenceErrorException
   */
  List<Node> getNodesByOwnerList(List<String> owner);
}
