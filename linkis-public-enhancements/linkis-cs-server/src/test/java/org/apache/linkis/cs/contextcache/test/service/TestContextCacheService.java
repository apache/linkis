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

package org.apache.linkis.cs.contextcache.test.service;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.apache.linkis.cs.contextcache.test.csid.TestContextID;
import org.apache.linkis.cs.contextcache.test.keyword.TestContextKey;
import org.apache.linkis.cs.contextcache.test.keyword.TestContextKeyValue;
import org.apache.linkis.cs.contextcache.test.keyword.TestContextValue;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;

public class TestContextCacheService {

  AnnotationConfigApplicationContext context;

  private String contextIDStr = "84693";

  private ContextCacheService contextCacheService;

  @BeforeEach
  public void generateData() throws CSErrorException {
    context =
        new AnnotationConfigApplicationContext("org.apache.linkis.cs", "org.apache.linkis.mybatis");
    System.out.println("ioc容器加载完成");
    contextCacheService = context.getBean(ContextCacheService.class);
    /* ContextPersistenceManager persistenceManager = context.getBean(ContextPersistenceManager.class);
    persistenceManager.getContextIDPersistence().deleteContextID(contextIDStr);
    PersistenceContextID persistenceContextID = new PersistenceContextID();
    persistenceContextID.setContextId(String.valueOf(contextIDStr));
    persistenceContextID.setUser("hadoop");
    persistenceContextID.setExpireTime(new Date());
    persistenceContextID.setExpireType(ExpireType.TODAY);
    persistenceContextID.setInstance("updateInstance");
    persistenceContextID.setBackupInstance("updatebackup");
    persistenceContextID.setApplication("hive");
    persistenceManager.getContextIDPersistence().createContextID(persistenceContextID);
    ContextID contextID = persistenceManager.getContextIDPersistence().getContextID(persistenceContextID.getContextId());
    System.out.println(contextID.getContextId());

    TestContextKey contextKey = new TestContextKey();
    contextKey.setContextScope(ContextScope.FRIENDLY);
    contextKey.setContextType(ContextType.OBJECT);
    contextKey.setKey("flow1.node1.test");
    contextKey.setKeywords("flow1,flow2,flow3");
    TestContextValue contextValue = new TestContextValue();
    contextValue.setKeywords("test1,test2,test3");
    contextValue.setValue("test1.flow1");
    TestContextKeyValue testContextKeyValue = new TestContextKeyValue();
    testContextKeyValue.setContextKey(contextKey);
    testContextKeyValue.setContextValue(contextValue);
    persistenceManager.getContextMapPersistence().create(contextID, testContextKeyValue);*/
  }

  // @Test
  public void testGetAll() {
    try {
      ContextID contextID = new TestContextID();
      contextID.setContextId(contextIDStr);
      List<ContextKeyValue> all = contextCacheService.getAll(contextID);
      if (null != all) {
        all.stream()
            .forEach(
                contextKeyValue -> {
                  System.out.println(contextKeyValue.getContextKey().getKey());
                  System.out.println(contextKeyValue.getContextValue().getValue());
                });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // @Test
  public void testGet() {
    ContextID contextID = new TestContextID();
    contextID.setContextId(contextIDStr);
    ContextKey contextKey1 = new TestContextKey();
    contextKey1.setKey("flow1.node1.test");
    ContextKeyValue contextKeyValue1 = contextCacheService.get(contextID, contextKey1);
    System.out.println(contextKeyValue1.getContextValue().getValue());
  }

  // @Test
  public void testGetValues() {
    ContextID contextID = new TestContextID();
    contextID.setContextId(contextIDStr);
    List<ContextKeyValue> contextKeyValueList =
        contextCacheService.getValues(contextID, "flow2", ContextType.METADATA);
    if (null != contextKeyValueList) {
      contextKeyValueList.stream()
          .forEach(
              contextKeyValue -> {
                System.out.println(contextKeyValue.getContextKey().getKey());
              });
    }
  }

  // @Test
  public void testLike() {
    ContextID contextID = new TestContextID();
    contextID.setContextId(contextIDStr);
    List<ContextKeyValue> contextKeyValueList =
        contextCacheService.getAllLikes(contextID, ".*node1.*", ContextType.METADATA);
    if (null != contextKeyValueList) {
      contextKeyValueList.stream()
          .forEach(
              contextKeyValue -> {
                System.out.println(contextKeyValue.getContextKey().getKey());
              });
    }
  }

  // @BeforeEach
  public void testPut() throws CSErrorException {

    ContextID contextID = new TestContextID();
    contextID.setContextId(contextIDStr);
    ContextKey contextKey1 = new TestContextKey();
    contextKey1.setKey("key1");
    contextKey1.setKeywords("keyword1,keyword2,keyword3");
    ContextKeyValue contextKeyValue1 = contextCacheService.get(contextID, contextKey1);

    ContextKey contextKey3 = new TestContextKey();
    contextKey3.setKey("key3");
    contextKey3.setKeywords("keyworddd1,keyworddd2,keyworddd3");

    ContextValue contextValue3 = new TestContextValue();
    contextValue3.setKeywords("keyworddd4-keyworddd5-keyworddd6");
    contextValue3.setValue("hello,hello3");
    ContextKeyValue contextKeyValue3 = new TestContextKeyValue();
    contextKeyValue3.setContextKey(contextKey3);
    contextKeyValue3.setContextValue(contextValue3);
    contextCacheService.put(contextID, contextKeyValue3);

    ContextKeyValue contextKeyValue4 = contextCacheService.get(contextID, contextKey3);
    System.out.println(contextKeyValue4.getContextKey().getKey());
  }
}
