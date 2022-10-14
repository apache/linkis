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

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NodeManagerMapperTest extends BaseDaoTest {

  @Autowired NodeManagerMapper nodeManagerMapper;
  @Autowired LabelManagerMapper labelManagerMapper;

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  private String instance = "testInstance";
  private String name = "testName";
  private String owner = "testOwner";
  private String mark = "testMark";
  private String creator = "testCreator";
  private String updator = "testUpdator";

  public PersistenceNode insertOne() {
    PersistenceNode persistenceNode = new PersistenceNode();
    persistenceNode.setInstance(instance);
    persistenceNode.setName(name);
    persistenceNode.setOwner(owner);
    persistenceNode.setMark(mark);
    persistenceNode.setCreateTime(new Date());
    persistenceNode.setUpdateTime(new Date());
    persistenceNode.setCreator(creator);
    persistenceNode.setUpdator(updator);
    nodeManagerMapper.addNodeInstance(persistenceNode);
    return persistenceNode;
  }

  @Test
  public void testUpdateNodeInstance() {
    PersistenceNode pn = insertOne();
    PersistenceNode persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertEquals(instance, persistenceNode.getInstance());
    pn.setInstance("ttt");
    nodeManagerMapper.updateNodeInstance(instance, pn);
    persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertEquals("ttt", persistenceNode.getInstance());
  }

  @Test
  public void testRemoveNodeInstance() {
    PersistenceNode pn = insertOne();
    PersistenceNode persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertNotNull(persistenceNode);
    nodeManagerMapper.removeNodeInstance(instance);
    persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertNull(persistenceNode);
  }

  @Test
  public void testGetNodeInstancesByOwner() {
    PersistenceNode pn = insertOne();
    List<PersistenceNode> instances = nodeManagerMapper.getNodeInstancesByOwner(owner);
    assertEquals(1, instances.size());
  }

  @Test
  public void testGetAllNodes() {
    PersistenceNode pn = insertOne();
    nodeManagerMapper.addNodeInstance(pn);
    List<PersistenceNode> instances = nodeManagerMapper.getAllNodes();
    assertEquals(2, instances.size());
  }

  @Test
  public void testUpdateNodeInstanceOverload() {
    PersistenceNode pn = insertOne();
    pn.setUpdator("testUp");
    nodeManagerMapper.updateNodeInstanceOverload(pn);
    PersistenceNode persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertEquals("testUp", persistenceNode.getUpdator());
  }

  @Test
  public void testGetNodeInstanceId() {
    PersistenceNode pn = insertOne();
    int id = nodeManagerMapper.getNodeInstanceId(instance);
    assertEquals(pn.getId(), id);
  }

  @Test
  public void testGetIdByInstance() {
    PersistenceNode pn = insertOne();
    int id = nodeManagerMapper.getIdByInstance(instance);
    assertEquals(pn.getId(), id);
  }

  @Test
  public void testGetNodeInstanceIds() {
    PersistenceNode pn = insertOne();
    List<Integer> list = nodeManagerMapper.getNodeInstanceIds(Arrays.asList(instance, "test"));
    assertEquals(pn.getId(), list.get(0));
  }

  @Test
  public void testGetNodeInstance() {
    insertOne();
    PersistenceNode node = nodeManagerMapper.getNodeInstance(instance);
    assertEquals(mark, node.getMark());
  }

  @Test
  public void testGetEMNodeInstanceByEngineNode() {
    insertOne();
    nodeManagerMapper.addEngineNode(instance, instance);
    PersistenceNode node = nodeManagerMapper.getEMNodeInstanceByEngineNode(instance);
    assertEquals(mark, node.getMark());
  }

  @Test
  public void testGetNodeInstances() {
    insertOne();
    nodeManagerMapper.addEngineNode(instance, instance);
    List<PersistenceNode> instances = nodeManagerMapper.getNodeInstances(instance);
    assertEquals(1, instances.size());
  }

  @Test
  public void testGetNodesByInstances() {
    insertOne();
    List<PersistenceNode> nodes = nodeManagerMapper.getNodesByInstances(Arrays.asList(instance));
    assertEquals(1, nodes.size());
  }

  @Test
  public void testDeleteEngineNode() {
    insertOne();
    nodeManagerMapper.addEngineNode(instance, instance);
    List<PersistenceNode> instances = nodeManagerMapper.getNodeInstances(instance);
    assertEquals(1, instances.size());
    nodeManagerMapper.deleteEngineNode(instance, instance);
    instances = nodeManagerMapper.getNodeInstances(instance);
    assertEquals(0, instances.size());
  }

  @Test
  public void testUpdateNodeRelation() {
    insertOne();
    nodeManagerMapper.addEngineNode(instance, instance);
    List<PersistenceNode> instances = nodeManagerMapper.getNodeInstances(instance);
    assertEquals(1, instances.size());

    nodeManagerMapper.updateNodeRelation(instance, "test");
  }

  @Test
  public void testUpdateNodeLabelRelation() {
    labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(8888));
    nodeManagerMapper.updateNodeLabelRelation(instance, instance);
  }

  @Test
  void addNodeInstance() {
    PersistenceNode persistenceNode = new PersistenceNode();
    persistenceNode.setInstance("instance2");
    persistenceNode.setName("testname2");
    persistenceNode.setOwner("testowner2");
    persistenceNode.setMark("testmark2");
    persistenceNode.setUpdator("testupdator2");
    persistenceNode.setCreator("testcreator2");
    nodeManagerMapper.addNodeInstance(persistenceNode);
    PersistenceNode persistenceNodes = nodeManagerMapper.getNodeInstance("instance2");
    assertTrue(persistenceNodes != null);
  }

  @Test
  void updateNodeInstance() {
    addNodeInstance();
    PersistenceNode persistenceNode = new PersistenceNode();
    persistenceNode.setInstance("instance3");
    persistenceNode.setName("testname3");
    persistenceNode.setOwner("testowner3");
    persistenceNode.setMark("testmark3");
    persistenceNode.setUpdator("testupdator3");
    persistenceNode.setCreator("testcreator3");
    nodeManagerMapper.updateNodeInstance("instance2", persistenceNode);
    PersistenceNode persistenceNodes = nodeManagerMapper.getNodeInstance("instance3");
    assertTrue(persistenceNodes != null);
  }

  @Test
  void removeNodeInstance() {
    addNodeInstance();
    nodeManagerMapper.removeNodeInstance("instance2");
    PersistenceNode persistenceNodes = nodeManagerMapper.getNodeInstance("instance2");
    assertTrue(persistenceNodes == null);
  }

  @Test
  void getNodeInstancesByOwner() {
    addNodeInstance();
    List<PersistenceNode> list = nodeManagerMapper.getNodeInstancesByOwner("testowner2");
    assertTrue(list.size() >= 1);
  }

  @Test
  void getAllNodes() {
    addNodeInstance();
    List<PersistenceNode> list = nodeManagerMapper.getAllNodes();
    assertTrue(list.size() >= 1);
  }

  @Test
  void updateNodeInstanceOverload() {
    addNodeInstance();
    PersistenceNode persistenceNode = new PersistenceNode();
    persistenceNode.setInstance("instance2");
    persistenceNode.setName("testname3");
    persistenceNode.setOwner("testowner3");
    persistenceNode.setMark("testmark3");
    persistenceNode.setUpdator("testupdator3");
    persistenceNode.setCreator("testcreator3");
    nodeManagerMapper.updateNodeInstanceOverload(persistenceNode);
    PersistenceNode persistenceNodes = nodeManagerMapper.getNodeInstance("instance2");
    assertTrue(persistenceNode.getName().equals(persistenceNodes.getName()));
  }

  @Test
  void getNodeInstanceId() {
    addNodeInstance();
    int i = nodeManagerMapper.getNodeInstanceId("instance2");
    assertTrue(i >= 1);
  }

  @Test
  void getIdByInstance() {}

  @Test
  void getNodeInstanceIds() {
    addNodeInstance();
    List<String> stringList = new ArrayList<>();
    stringList.add("instance1");
    stringList.add("instance2");
    List<Integer> list = nodeManagerMapper.getNodeInstanceIds(stringList);
    assertTrue(list.size() >= 1);
  }

  @Test
  void getNodeInstance() {
    PersistenceNode persistenceNode = nodeManagerMapper.getNodeInstance("instance1");
    assertTrue(persistenceNode != null);
  }

  @Test
  void getNodeInstanceById() {
    PersistenceNode persistenceNode = nodeManagerMapper.getNodeInstanceById(1);
    assertTrue(persistenceNode != null);
  }

  @Test
  void getEMNodeInstanceByEngineNode() {
    PersistenceNode persistenceNode = nodeManagerMapper.getEMNodeInstanceByEngineNode("instance1");
    assertTrue(persistenceNode != null);
  }

  @Test
  void getNodeInstances() {
    List<PersistenceNode> list = nodeManagerMapper.getNodeInstances("instance1");
    assertTrue(list.size() >= 1);
  }

  @Test
  void getNodesByInstances() {
    addNodeInstance();
    List<String> stringList = new ArrayList<>();
    stringList.add("instance1");
    stringList.add("instance2");
    List<PersistenceNode> list = nodeManagerMapper.getNodesByInstances(stringList);
    assertTrue(list.size() >= 1);
  }

  @Test
  void addEngineNode() {
    nodeManagerMapper.addEngineNode("instance1", "instance1");
  }

  @Test
  void deleteEngineNode() {
    nodeManagerMapper.deleteEngineNode("instance1", "instance1");
  }

  @Test
  void getNodeInstanceIdsByOwner() {
    addNodeInstance();
    List<Integer> list = nodeManagerMapper.getNodeInstanceIdsByOwner("testowner2");
    assertTrue(list.size() >= 1);
  }

  @Test
  void updateNodeRelation() {
    nodeManagerMapper.updateNodeRelation("instance1", "instance2");
  }

  @Test
  void updateNodeLabelRelation() {
    nodeManagerMapper.updateNodeLabelRelation("instance1", "instance2");
  }
}
