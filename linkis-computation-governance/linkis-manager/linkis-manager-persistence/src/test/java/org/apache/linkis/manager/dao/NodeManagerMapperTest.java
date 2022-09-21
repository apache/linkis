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

import org.apache.ibatis.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NodeManagerMapperTest extends BaseDaoTest {

  @Autowired NodeManagerMapper nodeManagerMapper;

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

  // @Insert({
  //        "insert into
  // linkis_cg_manager_service_instance(instance,name,owner,mark,update_time,create_time,updator,creator)"
  //                +
  // "values(#{instance},#{name},#{owner},#{mark},#{updateTime},#{createTime},#{updator},#{creator})"
  // @Options(useGeneratedKeys = true, keyProperty = "id")
  // void addNodeInstance(PersistenceNode node) throws DuplicateKeyException;

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

  //  @Update({
  //          "update linkis_cg_manager_service_instance set instance = #{persistenceNode.instance},
  // owner = #{persistenceNode.owner},mark = #{persistenceNode.mark},name =
  // #{persistenceNode.name},"
  //                  + "update_time = #{persistenceNode.updateTime},updator =
  // #{persistenceNode.updator},creator = #{persistenceNode.creator} where instance = #{instance}"
  //  })
  //  void updateNodeInstance(
  //          @Param("instance") String instance,
  //          @Param("persistenceNode") PersistenceNode persistenceNode);
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

  //  @Delete("delete from  linkis_cg_manager_service_instance where instance = #{instance}")
  //  void removeNodeInstance(@Param("instance") String instance);
  @Test
  public void testRemoveNodeInstance() {
    PersistenceNode pn = insertOne();
    PersistenceNode persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertNotNull(persistenceNode);
    nodeManagerMapper.removeNodeInstance(instance);
    persistenceNode = nodeManagerMapper.getNodeInstanceById(pn.getId());
    assertNull(persistenceNode);
  }

  //
  //  @Select("select * from  linkis_cg_manager_service_instance where owner = #{owner}")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  List<PersistenceNode> getNodeInstancesByOwner(@Param("owner") String owner);
  @Test
  public void testGetNodeInstancesByOwner() {
    PersistenceNode pn = insertOne();
    List<PersistenceNode> instances = nodeManagerMapper.getNodeInstancesByOwner(owner);
    assertEquals(1, instances.size());
  }

  //
  //  @Select("select * from  linkis_cg_manager_service_instance")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  List<PersistenceNode> getAllNodes();
  @Test
  public void testGetAllNodes() {
    PersistenceNode pn = insertOne();
    nodeManagerMapper.addNodeInstance(pn);
    List<PersistenceNode> instances = nodeManagerMapper.getAllNodes();
    assertEquals(2, instances.size());
  }

  //
  //  @Update({
  //          "update linkis_cg_manager_service_instance set owner = #{persistenceNode.owner},mark =
  // #{persistenceNode.mark},name = #{persistenceNode.name},"
  //                  + "update_time = #{persistenceNode.updateTime},create_time =
  // #{persistenceNode.createTime},updator = #{persistenceNode.updator},creator =
  // #{persistenceNode.creator} where instance = #{instance}"
  //  })
  //  void updateNodeInstanceOverload(PersistenceNode persistenceNode);
  @Test
  public void testUpdateNodeInstanceOverload() {}

  //
  //  @Select("select id from  linkis_cg_manager_service_instance where instance = #{instance}")
  //  int getNodeInstanceId(@Param("instance") String instance);
  @Test
  public void testGetNodeInstanceId() {}

  //
  //  @Select("select id from  linkis_cg_manager_service_instance where instance = #{instance}")
  //  int getIdByInstance(@Param("instance") String instance);
  @Test
  public void testGetIdByInstance() {}

  //
  //  @Select(
  //          "<script>"
  //                  + "select id from linkis_cg_manager_service_instance where instance in("
  //                  + "<foreach collection='instances' separator=',' item='instance'>"
  //                  + "#{instance} "
  //                  + "</foreach> "
  //                  + ")</script>")
  //  List<Integer> getNodeInstanceIds(@Param("serviceInstances") List<String> instances);
  @Test
  public void testGetNodeInstanceIds() {}

  //
  //  @Select("select * from linkis_cg_manager_service_instance where instance = #{instance}")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  PersistenceNode getNodeInstance(@Param("instance") String instance);
  @Test
  public void testGetNodeInstance() {}

  //
  //  @Select("select * from  linkis_cg_manager_service_instance where id = #{id}")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  PersistenceNode getNodeInstanceById(@Param("id") int id);
  @Test
  public void testGetNodeInstanceById() {}

  //
  //  @Select(
  //          "select * from linkis_cg_manager_service_instance where instance in (select
  // em_instance from linkis_cg_manager_engine_em where engine_instance=#{instance})")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  PersistenceNode getEMNodeInstanceByEngineNode(@Param("instance") String instance);
  @Test
  public void testGetEMNodeInstanceByEngineNode() {}

  //
  //  @Select(
  //          "select * from linkis_cg_manager_service_instance where instance in ( select
  // engine_instance from linkis_cg_manager_engine_em where em_instance=#{instance})")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  List<PersistenceNode> getNodeInstances(@Param("instance") String instance);
  @Test
  public void testGetNodeInstances() {}

  //
  //  @Select(
  //          "<script>"
  //                  + "select * from linkis_cg_manager_service_instance where instance in("
  //                  + "<foreach collection='instances' separator=',' item='instance'>"
  //                  + "#{instance} "
  //                  + "</foreach> "
  //                  + ")</script>")
  //  @Results({
  //          @Result(property = "updateTime", column = "update_time"),
  //          @Result(property = "createTime", column = "create_time")
  //  })
  //  List<PersistenceNode> getNodesByInstances(@Param("engineNodeIds") List<String> instances);
  @Test
  public void testGetNodesByInstances() {}

  //
  //  @Insert(
  //          "insert into  linkis_cg_manager_engine_em (engine_instance, em_instance, update_time,
  // create_time)"
  //                  + "values(#{engineNodeInstance}, #{emNodeInstance}, now(), now())")
  //  void addEngineNode(
  //          @Param("engineNodeInstance") String engineNodeInstance,
  //          @Param("emNodeInstance") String emNodeInstance);
  @Test
  public void testAddEngineNode() {}

  //
  //  @Delete(
  //          "delete from  linkis_cg_manager_engine_em where engine_instance =
  // #{engineNodeInstance} and em_instance = #{emNodeInstance}")
  //  void deleteEngineNode(
  //          @Param("engineNodeInstance") String engineNodeInstance,
  //          @Param("emNodeInstance") String emNodeInstance);
  @Test
  public void testDeleteEngineNode() {}

  //
  //  @Select("select engine_id from  linkis_cg_manager_engine_em where em_id = #{emId}")
  //  List<Integer> getEngineNodeIDsByEMId(@Param("emId") int emId);
  @Test
  public void testGetEngineNodeIDsByEMId() {}

  //
  //  @Select("select em_id from  linkis_cg_manager_engine_em where engine_id = #{engineNodeId}")
  //  int getEMIdByEngineId(@Param("engineNodeId") int engineNodeId);
  @Test
  public void testGetEMIdByEngineId() {}

  //
  //  @Select("select id from linkis_cg_manager_service_instance where owner = #{owner}")
  //  List<Integer> getNodeInstanceIdsByOwner(@Param("owner") String owner);
  @Test
  public void testGetNodeInstanceIdsByOwner() {}

  //
  //  void updateNodeRelation(@Param("tickedId") String tickedId, @Param("instance") String
  // instance);
  @Test
  public void testUpdateNodeRelation() {}

  //
  //  void updateNodeLabelRelation(
  //          @Param("tickedId") String tickedId, @Param("instance") String instance);
  @Test
  public void testUpdateNodeLabelRelation() {}
}
