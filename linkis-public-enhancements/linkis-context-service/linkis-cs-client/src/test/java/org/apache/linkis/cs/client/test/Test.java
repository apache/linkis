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

package org.apache.linkis.cs.client.test;

import org.apache.linkis.cs.client.Context;
import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.object.CSFlowInfos;
import org.apache.linkis.cs.common.entity.source.*;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
  public static void main(String[] args) throws Exception {
    /*ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    ContextID contextID = new LinkisHAWorkFlowContextID();
    Context context = contextClient.createContext(contextID);
    System.out.println(context.getContextID().getContextId());
    ContextKey contextKey = new CommonContextKey();
    contextKey.setKey("cooper.txt");
    contextKey.setKeywords("xddd");
    contextKey.setContextScope(ContextScope.PUBLIC);
    contextKey.setContextType(ContextType.RESOURCE);
    ContextValue contextValue = new CommonContextValue();
    LinkisBMLResource resource = new LinkisBMLResource();
    resource.setResourceId("456789");
    resource.setVersion("v00001");
    contextValue.setValue(resource);
    ContextKeyValue contextKeyValue = new CommonContextKeyValue();
    contextKeyValue.setContextValue(contextValue);
    contextKeyValue.setContextKey(contextKey);
    context.setContextKeyAndValue(contextKeyValue);
    ContextValue myValue = context.getContextValue(contextKey);
    LinkisBMLResource linkisBMLResource = (LinkisBMLResource)myValue.getValue();
    System.out.println(linkisBMLResource.getResourceId());
    ContextClientListenerBus<ContextClientListener, Event> bus = ContextClientListenerManager.getContextClientListenerBus();
    ContextKeyListener listener = new CommonContextKeyListener();
    listener.setContextKey(contextKey);
    listener.setContext(context);
    context.onBind(contextKey, listener);
    bus.addListener(listener);
    List<String>  contains = new ArrayList<>();
    contains.add("cooper");
    contextClient.search(context.getContextID(), null, null, contains, null);
    List<ContextKeyValue> list = context.searchContext(null, null, contains, null);
    System.out.println("ret list is " + list);
    Thread.sleep(5000);
    contextClient.close();*/
    testCSFlowInfos();
  }

  public static void testCSFlowInfos() throws Exception {
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    LinkisHAWorkFlowContextID contextID = new LinkisHAWorkFlowContextID();
    contextID.setFlow("test");
    contextID.setProject("test");
    contextID.setWorkSpace("test1");
    contextID.setVersion("v00001");
    Context context = contextClient.createContext(contextID);
    System.out.println(context.getContextID().getContextId());
    String csid = context.getContextID().getContextId();

    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();
    CommonContextValue contextValue = new CommonContextValue();
    Map<String, Object> infos = new HashMap<>();
    List<Map<String, String>> edges = new ArrayList<>();
    Map<String, String> edge = new HashMap<>();
    edge.put("source", "sql ");
    edge.put("target", "hql");
    edge.put("sourceLocation", "bottom");
    edge.put("targetLocation", "top");
    edges.add(edge);
    infos.put("edges", edges);
    infos.put("parent", "flow2");
    Map<String, String> idNodeName = new HashMap<>();
    idNodeName.put("90a6ee94-4bd6-47d9-a536-f92660c4c051", "sql");
    idNodeName.put("90a6ee94-4bd6-47d9-a536-f92660c4c052", "hql");
    infos.put("id_nodeName", idNodeName);
    CSFlowInfos csFlowInfos = new CSFlowInfos();
    csFlowInfos.setInfos(infos);
    contextValue.setValue(csFlowInfos);

    ContextKey contextKey = new CommonContextKey();
    contextKey.setKey("flow.infos");
    contextKey.setContextScope(ContextScope.PUBLIC);
    contextKey.setContextType(ContextType.RESOURCE);

    ContextKeyValue contextKeyValue = new CommonContextKeyValue();
    contextKeyValue.setContextValue(contextValue);
    contextKeyValue.setContextKey(contextKey);
    context.setContextKeyAndValue(contextKeyValue);
    ContextValue myValue = context.getContextValue(contextKey);
    System.out.println(SerializeHelper.serializeContextID(context.getContextID()));
    contextClient.removeAllValueByKeyAndContextType(
        context.getContextID(), contextKey.getContextType(), contextKey.getKey());
    ContextValue delValue = context.getContextValue(contextKey);
    if (null != delValue) {
      System.err.println(
          "contextValue : "
              + myValue.getValue()
              + " with key : "
              + contextKey.getKey()
              + " was not deleted.");
    }

    List<String> idList = new ArrayList<>();
    idList.add(csid);
    //        contextClient.

  }
}
