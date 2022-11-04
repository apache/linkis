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

import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetrics;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeMetricsEntity;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeMetricManagerMapperTest extends BaseDaoTest {

  @Autowired NodeMetricManagerMapper nodeMetricManagerMapper;

  @Test
  void addNodeMetrics() {
    PersistenceNodeMetrics nodeMetrics = new PersistenceNodeMetrics();
    nodeMetrics.setInstance("instance1");
    nodeMetrics.setStatus(1);
    nodeMetrics.setOverLoad("testoverload");
    nodeMetrics.setHeartBeatMsg("testheartbeat_msg");
    nodeMetrics.setHealthy("2");
    nodeMetricManagerMapper.addNodeMetrics(nodeMetrics);
    PersistenceNodeMetrics persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstance("instance1");
    assertTrue(persistenceNodeMetrics != null);
  }

  @Test
  void checkInstanceExist() {
    addNodeMetrics();
    int i = nodeMetricManagerMapper.checkInstanceExist("instance1");
    assertTrue(i >= 1);
  }

  @Test
  void getNodeMetricsByInstances() {
    addNodeMetrics();
    List<String> list = new ArrayList<>();
    list.add("instance1");
    List<PersistenceNodeMetrics> persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstances(list);
    assertTrue(persistenceNodeMetrics.size() >= 1);
  }

  @Test
  void getNodeMetricsByInstance() {
    addNodeMetrics();
    PersistenceNodeMetrics persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstance("instance1");
    assertTrue(persistenceNodeMetrics != null);
  }

  @Test
  void updateNodeMetrics() {
    addNodeMetrics();
    PersistenceNodeMetrics nodeMetrics = new PersistenceNodeMetrics();
    nodeMetrics.setStatus(2);
    nodeMetrics.setOverLoad("testoverloads");
    nodeMetrics.setHeartBeatMsg("testheartbeat_msgs");
    nodeMetrics.setHealthy("2s");
    nodeMetricManagerMapper.updateNodeMetrics(nodeMetrics, "instance1");
    PersistenceNodeMetrics persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstance("instance1");
    assertTrue(persistenceNodeMetrics.getOverLoad().equals("testoverloads"));
  }

  @Test
  void deleteNodeMetrics() {
    addNodeMetrics();
    nodeMetricManagerMapper.deleteNodeMetrics("instance1");
    PersistenceNodeMetrics persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstance("instance1");
    assertTrue(persistenceNodeMetrics == null);
  }

  @Test
  void deleteNodeMetricsByInstance() {
    addNodeMetrics();
    nodeMetricManagerMapper.deleteNodeMetricsByInstance("instance1");
    PersistenceNodeMetrics persistenceNodeMetrics =
        nodeMetricManagerMapper.getNodeMetricsByInstance("instance1");
    assertTrue(persistenceNodeMetrics == null);
  }

  @Test
  void getAllNodeMetrics() {
    addNodeMetrics();
    List<PersistenceNodeMetricsEntity> list = nodeMetricManagerMapper.getAllNodeMetrics();
    assertTrue(list.size() >= 1);
  }
}
