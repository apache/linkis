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

package org.apache.linkis.udf.service;

import org.apache.linkis.udf.dao.UDFTreeDao;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.service.impl.UDFTreeServiceImpl;

import org.apache.commons.collections.map.HashedMap;

import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class UDFTreeServiceTest {

  private static final Logger LOG = LoggerFactory.getLogger(UDFTreeServiceTest.class);

  @InjectMocks private UDFTreeServiceImpl udfTreeServiceImpl;

  @Mock private UDFTreeDao udfTreeDao;

  @Test
  @DisplayName("initTreeTest")
  public void initTreeTest() {

    UDFTree udfTree = new UDFTree();
    udfTree.setId(13L);
    List<UDFTree> first = new ArrayList<>();
    first.add(udfTree);

    Map<String, Object> params = new HashedMap();
    params.put("parent", -1L);
    params.put("userName", "hadoop");
    params.put("category", "all");

    Mockito.when(udfTreeDao.getTreesByParentId(Mockito.anyMap())).thenReturn(first);
    Assertions.assertAll(
        () -> {
          UDFTree initTree = udfTreeServiceImpl.initTree("hadoop", "all");
          Assertions.assertNotNull(initTree);
        });
  }

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

    Assertions.assertAll(
        () -> {
          udfTreeServiceImpl.addTree(udfTree, "hadoop");
        });
  }

  @Test
  @DisplayName("updateTreeTest")
  public void updateTreeTest() {
    UDFTree udfTree = new UDFTree();
    udfTree.setId(13L);
    udfTree.setParent(10L);
    udfTree.setName("udfTreeUpdate");
    udfTree.setUserName("hadoop");
    udfTree.setDescription("test descs");
    udfTree.setUpdateTime(new Date());

    Assertions.assertAll(
        () -> {
          udfTreeServiceImpl.updateTree(udfTree, "hadoop");
        });
  }

  @Test
  @DisplayName("deleteTreeTest")
  public void deleteTreeTest() {

    Assertions.assertAll(
        () -> {
          udfTreeServiceImpl.deleteTree(13L, "hadoop");
        });
  }

  @Test
  @DisplayName("getTreeByIdTest")
  public void getTreeByIdTest() {

    Assertions.assertAll(
        () -> {
          UDFTree udfTree = udfTreeServiceImpl.getTreeById(13L, "hadoop", "sys", "all");
          Assertions.assertNull(udfTree);
        });
  }
}
