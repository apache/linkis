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

package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.entity.persistence.PersistencerEcNodeInfo;

import org.apache.ibatis.annotations.*;

import org.springframework.dao.DuplicateKeyException;

import java.util.List;

@Mapper
public interface NodeManagerMapper {

  void addNodeInstance(PersistenceNode node) throws DuplicateKeyException;

  void updateNodeInstance(
      @Param("instance") String instance,
      @Param("persistenceNode") PersistenceNode persistenceNode);

  void removeNodeInstance(@Param("instance") String instance);

  List<PersistenceNode> getNodeInstancesByOwner(@Param("owner") String owner);

  List<PersistenceNode> getAllNodes();

  void updateNodeInstanceByInstance(@Param("persistenceNode") PersistenceNode persistenceNode);

  Integer getNodeInstanceId(@Param("instance") String instance);

  List<Integer> getNodeInstanceIds(@Param("instances") List<String> instances);

  PersistenceNode getNodeInstance(@Param("instance") String instance);

  PersistenceNode getNodeInstanceById(@Param("id") int id);

  PersistenceNode getEMNodeInstanceByEngineNode(@Param("instance") String instance);

  List<PersistenceNode> getNodeInstances(@Param("instance") String instance);

  List<PersistenceNode> getNodesByInstances(@Param("instances") List<String> instances);

  void addEngineNode(
      @Param("engineNodeInstance") String engineNodeInstance,
      @Param("emNodeInstance") String emNodeInstance);

  void deleteEngineNode(
      @Param("engineNodeInstance") String engineNodeInstance,
      @Param("emNodeInstance") String emNodeInstance);

  List<Integer> getNodeInstanceIdsByOwner(@Param("owner") String owner);

  void updateNodeRelation(@Param("tickedId") String tickedId, @Param("instance") String instance);

  void updateNodeLabelRelation(
      @Param("tickedId") String tickedId, @Param("instance") String instance);

  List<PersistenceNode> getNodeInstancesByOwnerList(@Param("owner") List<String> owner);

  List<PersistencerEcNodeInfo> getEMNodeInfoList(
      @Param("creatorUsers") List<String> creatorUsers, @Param("statuss") List<Integer> statuss);
}
