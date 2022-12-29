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

package org.apache.linkis.basedatamanager.server.service;

import org.apache.linkis.basedatamanager.server.dao.UdfTreeMapper;
import org.apache.linkis.basedatamanager.server.domain.UdfTreeEntity;
import org.apache.linkis.basedatamanager.server.service.impl.UdfTreeServiceImpl;

import org.springframework.util.Assert;

import java.util.ArrayList;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class UdfTreeServiceTest {
  private Logger logger = LoggerFactory.getLogger(UdfTreeServiceTest.class);
  @InjectMocks private UdfTreeServiceImpl udfTreeService;
  @Mock private UdfTreeMapper udfTreeMapper;

  @Test
  public void getListByPageTest() {
    ArrayList<UdfTreeEntity> t = new ArrayList<>();
    UdfTreeEntity udfTreeEntity = new UdfTreeEntity();
    udfTreeEntity.setId(3L);
    udfTreeEntity.setParent(1l);
    udfTreeEntity.setDescription("test");
    udfTreeEntity.setName("test");
    udfTreeEntity.setCategory("test");
    udfTreeEntity.setUserName("test");
    t.add(udfTreeEntity);
    Mockito.when(udfTreeMapper.getListByPage("")).thenReturn(t);
    PageInfo listByPage = udfTreeService.getListByPage("", 1, 10);
    Assert.isTrue(listByPage.getSize() > 0);
    logger.info(listByPage.toString());
  }
}
