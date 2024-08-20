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

package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.UDFTree;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UDFTreeDaoTest extends BaseDaoTest {

  @Autowired private UDFTreeDao udfTreeDao;

  @Test
  @DisplayName("addTreeTest")
  public void addTreeTest() {

    UDFTree udfTree = new UDFTree();
    udfTree.setId(15L);
    udfTree.setParent(10L);
    udfTree.setName("jarTest");
    udfTree.setUserName("hadoop");
    udfTree.setDescription("test descs");
    udfTree.setCreateTime(new Date());
    udfTree.setUpdateTime(new Date());
    udfTree.setCategory("function");
    udfTreeDao.addTree(udfTree);

    UDFTree tree = udfTreeDao.getTreeById(15L);
    Assertions.assertNotNull(tree);
  }

  @Test
  @DisplayName("updateTreeTest")
  public void updateTreeTest() {

    UDFTree udfTree = new UDFTree();
    udfTree.setId(13L);
    udfTree.setParent(10L);
    udfTree.setName("udfTreeUpdates");
    udfTree.setUserName("hadoop");
    udfTree.setDescription("test descs");
    udfTree.setUpdateTime(new Date());
    udfTreeDao.updateTree(udfTree);

    UDFTree tree = udfTreeDao.getTreeById(13L);
    Assertions.assertEquals(udfTree.getName(), tree.getName());
  }

  @Test
  @DisplayName("deleteTreeTest")
  public void deleteTreeTest() {

    udfTreeDao.deleteTree(13L, "hadoop");
    UDFTree tree = udfTreeDao.getTreeById(13L);
    Assertions.assertNull(tree);
  }

  @Test
  @DisplayName("getTreeByIdAndCategoryTest")
  public void getTreeByIdAndCategoryTest() {
    UDFTree udfTree = udfTreeDao.getTreeByIdAndCategory(13L, "function");
    Assertions.assertNotNull(udfTree);
  }

  @Test
  @DisplayName("getTreeByIdAndCategoryAndUserNameTest")
  public void getTreeByIdAndCategoryAndUserNameTest() {
    UDFTree udfTree = udfTreeDao.getTreeByIdAndCategoryAndUserName(13L, "function", "hadoop");
    Assertions.assertNotNull(udfTree);
  }

  @Test
  @DisplayName("getTreeByIdTest")
  public void getTreeByIdTest() {
    UDFTree udfTree = udfTreeDao.getTreeById(13L);
    Assertions.assertNotNull(udfTree);
  }

  @Test
  @DisplayName("getTreesByParentIdTest")
  public void getTreesByParentIdTest() {
    Map<String, Object> params = new HashMap<>();
    params.put("parent", 10L);
    params.put("userName", "hadoop");
    params.put("category", "function");
    List<UDFTree> udfTreeList = udfTreeDao.getTreesByParentId(params);
    Assertions.assertTrue(udfTreeList.size() > 0);
  }

  @Test
  @DisplayName("getTreeByNameAndUserTest")
  public void getTreeByNameAndUserTest() {
    UDFTree udfTree = udfTreeDao.getTreeByNameAndUser("baoyang", "hadoop", "function");
    Assertions.assertNotNull(udfTree);
  }

  @Test
  @DisplayName("getUserDirectoryTest")
  public void getUserDirectoryTest() {
    List<String> userDirectoryList = udfTreeDao.getUserDirectory("hadoop", "function");
    Assertions.assertTrue(userDirectoryList.size() == 1);
  }
}
