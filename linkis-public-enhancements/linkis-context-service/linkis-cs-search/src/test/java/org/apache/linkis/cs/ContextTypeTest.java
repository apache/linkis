/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs;

import com.google.common.collect.Lists;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

public class ContextTypeTest {

    List<ContextKeyValue> contextKeyValues = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {

        ContextID contextID = new TestContextID();
        contextID.setContextId("id");
        ContextKey contextKey1 = new TestContextKey();
        contextKey1.setKey("key1");
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
        contextKey2.setKey("key2");
        contextKey2.setContextType(ContextType.METADATA);
        contextKey2.setContextScope(ContextScope.PROTECTED);
        contextKey2.setKeywords("keyword1,keyword2,keyword3");
        ContextKeyValue contextKeyValue2 = new TestContextKeyValue();
        contextKeyValue2.setContextKey(contextKey2);
        contextKeyValue2.setContextValue(null);
        //contextKeyValues.add(contextKeyValue2);
    }

    @Test
    public void testSearch() throws ContextSearchFailedException {

        ContextSearch contextSearch = new DefaultContextSearch();
        ContextCacheService contextCacheService = Mockito.mock(ContextCacheService.class);
        Mockito.when(contextCacheService.getAllByType(Mockito.any(ContextID.class), Mockito.any(ContextType.class))).thenReturn(contextKeyValues);

        ConditionBuilder conditionBuilder = ConditionBuilder.newBuilder();
        conditionBuilder.contextTypes(Lists.newArrayList(ContextType.DATA));
        Condition condition = conditionBuilder.build();

        ContextID contextID = new TestContextID();
        contextID.setContextId("id");
        List<ContextKeyValue> list = contextSearch.search(contextCacheService, contextID, condition);
        Assert.assertEquals(1, list.size());
    }
}
