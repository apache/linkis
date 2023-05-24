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

package org.apache.linkis.cs.persistence.dao;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.persistence.AContextID;
import org.apache.linkis.cs.persistence.AContextKey;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import org.apache.linkis.cs.persistence.entity.PersistenceContextValue;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContextMapMapperTest extends BaseDaoTest {

  @Autowired ContextMapMapper contextMapMapper;

  PersistenceContextKeyValue insert() {
    PersistenceContextKeyValue pCVK = new PersistenceContextKeyValue();
    AContextKey aContextKey = new AContextKey();
    ContextValue aContextValue = new PersistenceContextValue();
    aContextValue.setValue("value");
    ((PersistenceContextValue) aContextValue).setValueStr("valuestr");
    aContextValue.setKeywords("keywords");
    aContextKey.setKey("key");
    aContextKey.setContextType(ContextType.ENV);
    aContextKey.setContextScope(ContextScope.FRIENDLY);
    pCVK.setContextKey(aContextKey);
    pCVK.setContextValue(aContextValue);
    pCVK.setProps("props");
    pCVK.setContextId("1");
    pCVK.setCreateTime(new Date());
    pCVK.setUpdateTime(new Date());
    pCVK.setAccessTime(new Date());

    contextMapMapper.createMap(pCVK);
    return pCVK;
  }

  @Test
  void createMap() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap != null);
  }

  @Test
  void updateMap() {
    PersistenceContextKeyValue pCVK = insert();
    PersistenceContextKeyValue pCVK1 = new PersistenceContextKeyValue();
    AContextKey aContextKey = new AContextKey();
    ContextValue aContextValue = new PersistenceContextValue();
    aContextValue.setKeywords("keywords1");
    pCVK1.setContextKey(aContextKey);
    pCVK1.setContextValue(aContextValue);
    pCVK1.setContextId("1");
    pCVK1.setCreateTime(new Date());
    pCVK1.setUpdateTime(new Date());
    pCVK1.setAccessTime(new Date());
    contextMapMapper.updateMap(pCVK1);
    PersistenceContextKeyValue pCVK2 = new PersistenceContextKeyValue();
    pCVK2.setContextId("1");
    List<PersistenceContextKeyValue> list = contextMapMapper.getAllContextMap(pCVK2);

    Assertions.assertTrue(list.get(0).getContextKey().getKeywords().equals("keywords1"));
  }

  @Test
  void getContextMap() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap != null);
  }

  @Test
  void getAllContextMapByKey() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    contextID.setContextId(pCVK.getContextId());
    List<PersistenceContextKeyValue> list =
        contextMapMapper.getAllContextMapByKey(contextID, "key");
    Assertions.assertTrue(list.size() > 0);
  }

  @Test
  void getAllContextMapByContextID() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    contextID.setContextId(pCVK.getContextId());
    List<PersistenceContextKeyValue> list = contextMapMapper.getAllContextMapByContextID(contextID);
    Assertions.assertTrue(list.size() > 0);
  }

  @Test
  void getAllContextMapByScope() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    List<PersistenceContextKeyValue> list =
        contextMapMapper.getAllContextMapByScope(contextID, ContextScope.FRIENDLY);
    Assertions.assertTrue(list.size() > 0);
  }

  @Test
  void getAllContextMapByType() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    contextID.setContextId(pCVK.getContextId());
    List<PersistenceContextKeyValue> list =
        contextMapMapper.getAllContextMapByType(contextID, ContextType.ENV);
    Assertions.assertTrue(list.size() > 0);
  }

  @Test
  void removeContextMap() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeContextMap(contextID, contextKey);
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
    //        assertThat(pCVK.getId(),greaterThan(0));
  }

  @Test
  void removeAllContextMapByContextID() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeAllContextMapByContextID(contextID);
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
  }

  @Test
  void removeAllContextMapByType() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeAllContextMapByType(contextID, ContextType.ENV);
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
  }

  @Test
  void removeAllContextMapByScope() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeAllContextMapByScope(contextID, ContextScope.FRIENDLY);
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
  }

  @Test
  void removeByKeyPrefixAndContextType() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeByKeyPrefixAndContextType(contextID, ContextType.ENV, "key");
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
  }

  @Test
  void removeByKeyAndContextType() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeByKeyAndContextType(contextID, ContextType.ENV, "key");
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
  }

  @Test
  void removeByKeyPrefix() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);
    contextMapMapper.removeByKeyPrefix(contextID, "key");
    PersistenceContextKeyValue contextMap = contextMapMapper.getContextMap(contextID, contextKey);
    Assertions.assertTrue(contextMap == null);
  }

  @Test
  void getAllContextMap() {
    PersistenceContextKeyValue pCVK = insert();
    ContextID contextID = new AContextID();
    ContextKey contextKey = new AContextKey();
    contextID.setContextId(pCVK.getContextId());
    contextKey.setKey(pCVK.getContextKey().getKey());
    contextKey.setContextType(ContextType.ENV);

    PersistenceContextKeyValue pCVK1 = new PersistenceContextKeyValue();
    pCVK1.setContextId("1");
    List<PersistenceContextKeyValue> list = contextMapMapper.getAllContextMap(pCVK1);
    Assertions.assertTrue(list.size() > 0);
  }

  @Test
  void getAllContextMapByTime() {
    PersistenceContextKeyValue pCVK = insert();
    Date createTimeStart = new Date(System.currentTimeMillis() - 1000 * 60 * 60);
    Date createTimeEnd = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
    List<PersistenceContextKeyValue> list =
        contextMapMapper.getAllContextMapByTime(
            createTimeStart, createTimeEnd, null, null, null, null);
    Assertions.assertTrue(list.size() > 0);
  }
}
