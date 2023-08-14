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

package org.apache.linkis.cs.client.test.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.client.Context;
import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.client.service.CSWorkService;
import org.apache.linkis.cs.client.service.CSWorkServiceImpl;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.enumeration.WorkType;
import org.apache.linkis.cs.common.entity.resource.LinkisBMLResource;
import org.apache.linkis.cs.common.entity.source.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.jupiter.api.Assertions.*;

public class TestRemove {

  private ContextClient contextClient;
  private ContextID contextId;
  private ContextKey contextKey;
  private ContextValue contextValue;
  private CSWorkService csWorkService;
  private Gson gson;

  public static void main(String[] args) throws Exception {
    TestRemove test = new TestRemove();
    test.init();
    test.testGetBeforeSet();
    test.testReset();
    test.testGetAfterSet();
    test.testClose();
  }

  public void init() throws Exception {
    contextClient = ContextClientFactory.getOrCreateContextClient();
    csWorkService = CSWorkServiceImpl.getInstance();
    gson = new GsonBuilder().setPrettyPrinting().create();
    LinkisHAWorkFlowContextID haContextID = new LinkisHAWorkFlowContextID();
    haContextID.setWorkSpace("wp1");
    haContextID.setProject("p1");
    haContextID.setFlow("f1");
    haContextID.setVersion("v1");
    if (null == contextId) {
      Context context = contextClient.createContext(haContextID);
      contextId = context.getContextID();
    }

    contextKey = new CommonContextKey();
    contextKey.setContextScope(ContextScope.FRIENDLY);
    contextKey.setContextType(ContextType.ENV);
    contextKey.setKey("project.flow1.node1.key2");

    contextValue = new CommonContextValue();
    LinkisBMLResource resource = new LinkisBMLResource();
    resource.setResourceId("dfasdfsr2456wertg");
    resource.setVersion("v000002");
    contextValue.setValue(resource);

    ContextKeyValue keyValue = new CommonContextKeyValue();
    keyValue.setContextKey(contextKey);
    keyValue.setContextValue(contextValue);

    contextClient.setContextKeyValue(haContextID, keyValue);
  }

  public void testGetBeforeSet() throws ErrorException {
    ContextValue value = contextClient.getContextValue(contextId, contextKey);
    LinkisBMLResource resourceOri = (LinkisBMLResource) contextValue.getValue();
    LinkisBMLResource resourceRs = (LinkisBMLResource) value.getValue();
    assertEquals(resourceOri.getResourceId(), resourceRs.getResourceId());
    assertEquals(resourceOri.getVersion(), resourceRs.getVersion());
    System.out.println(gson.toJson(resourceOri));
    System.out.println(gson.toJson(resourceRs));
  }

  public void testReset() throws ErrorException {
    csWorkService.initContextServiceInfo(
        SerializeHelper.serializeContextID(contextId), WorkType.PROJECT);
  }

  public void testGetAfterSet() throws ErrorException {
    ContextValue value = contextClient.getContextValue(contextId, contextKey);
    assertEquals(null, value);
    System.out.println(gson.toJson(contextValue));
    System.out.println(gson.toJson(value));
  }

  public void testClose() throws Exception {
    contextClient.close();
  }
}
