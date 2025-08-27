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

package org.apache.linkis.cs;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.construction.ConditionBuilder;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.apache.linkis.cs.csid.TestContextID;
import org.apache.linkis.cs.exception.ContextSearchFailedException;
import org.apache.linkis.cs.keyword.TestContextKey;
import org.apache.linkis.cs.keyword.TestContextKeyValue;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainsTest {
  List<ContextKeyValue> contextKeyValues = Lists.newArrayList();

  @BeforeEach
  public void setUp() throws Exception {

    ContextID contextID = new TestContextID();
    contextID.setContextId("id");
    ContextKey contextKey1 = new TestContextKey();
    contextKey1.setKey("abc345efgabcab");
    contextKey1.setContextType(ContextType.DATA);
    contextKey1.setContextScope(ContextScope.PRIVATE);
    contextKey1.setKeywords("keyword1,keyword2,keyword3");
    ContextKeyValue contextKeyValue1 = new TestContextKeyValue();
    contextKeyValue1.setContextKey(contextKey1);
    contextKeyValue1.setContextValue(null);
    contextKeyValues.add(contextKeyValue1);

    ContextID contextID2 = new TestContextID();
    contextID2.setContextId("id");
    ContextKey contextKey2 = new TestContextKey();
    contextKey2.setKey("2342342342342");
    contextKey2.setContextType(ContextType.METADATA);
    contextKey2.setContextScope(ContextScope.PROTECTED);
    contextKey2.setKeywords("keyword1,keyword2,keyword3");
    ContextKeyValue contextKeyValue2 = new TestContextKeyValue();
    contextKeyValue2.setContextKey(contextKey2);
    contextKeyValue2.setContextValue(null);
    contextKeyValues.add(contextKeyValue2);

    ContextID contextID3 = new TestContextID();
    contextID3.setContextId("id");
    ContextKey contextKey3 = new TestContextKey();
    contextKey3.setKey("34646456e");
    contextKey3.setContextType(ContextType.COST);
    contextKey3.setContextScope(ContextScope.PROTECTED);
    contextKey3.setKeywords("keyword1,keyword2,keyword3");
    ContextKeyValue contextKeyValue3 = new TestContextKeyValue();
    contextKeyValue3.setContextKey(contextKey3);
    contextKeyValue3.setContextValue(null);
    contextKeyValues.add(contextKeyValue3);
  }

  @Test
  public void testSearch() throws ContextSearchFailedException {

    ContextSearch contextSearch = new DefaultContextSearch();
    ContextCacheService contextCacheService = Mockito.mock(ContextCacheService.class);
    Mockito.when(contextCacheService.getAll(Mockito.any(ContextID.class)))
        .thenReturn(contextKeyValues);

    ConditionBuilder conditionBuilder = ConditionBuilder.newBuilder();
    conditionBuilder.contains("abc");
    Condition condition = conditionBuilder.build();

    ContextID contextID = new TestContextID();
    contextID.setContextId("id");
    List<ContextKeyValue> list = contextSearch.search(contextCacheService, contextID, condition);
    assertEquals(1, list.size());
  }

  @Test
  public void testSearchNegate() throws ContextSearchFailedException {

    ContextSearch contextSearch = new DefaultContextSearch();
    ContextCacheService contextCacheService = Mockito.mock(ContextCacheService.class);
    Mockito.when(contextCacheService.getAll(Mockito.any(ContextID.class)))
        .thenReturn(contextKeyValues);

    ConditionBuilder conditionBuilder = ConditionBuilder.newBuilder();
    conditionBuilder.contains("abc");
    Condition condition = conditionBuilder.build().not();

    ContextID contextID = new TestContextID();
    contextID.setContextId("id");
    List<ContextKeyValue> list = contextSearch.search(contextCacheService, contextID, condition);
    assertEquals(2, list.size());
  }
}
