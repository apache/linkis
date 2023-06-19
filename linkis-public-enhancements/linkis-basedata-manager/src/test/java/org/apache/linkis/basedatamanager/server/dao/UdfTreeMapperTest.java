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

package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.domain.UdfTreeEntity;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UdfTreeMapperTest extends BaseDaoTest {

  @Autowired UdfTreeMapper udfTreeMapper;

  UdfTreeEntity insert() {
    UdfTreeEntity udfTreeEntity = new UdfTreeEntity();
    udfTreeEntity.setCreateTime(new Date());
    udfTreeEntity.setUpdateTime(new Date());
    udfTreeEntity.setCategory("category");
    udfTreeEntity.setDescription("desc");
    udfTreeEntity.setName("name");
    udfTreeEntity.setParent(1L);
    udfTreeEntity.setUserName("userName");
    udfTreeMapper.insert(udfTreeEntity);
    return udfTreeEntity;
  }

  @Test
  void getListByPage() {
    insert();
    List<UdfTreeEntity> list = udfTreeMapper.getListByPage("name");
    assertTrue(list.size() > 0);
  }
}
