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

import org.apache.linkis.cs.client.Context;
import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.client.service.DefaultSearchService;
import org.apache.linkis.cs.client.service.SearchService;
import org.apache.linkis.cs.client.utils.SerializeHelper;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.object.CSFlowInfos;
import org.apache.linkis.cs.common.entity.resource.BMLResource;
import org.apache.linkis.cs.common.entity.resource.LinkisBMLResource;
import org.apache.linkis.cs.common.entity.source.*;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;

// import org.apache.linkis.cs.client.service.DefaultSearchService;
// import org.apache.linkis.cs.common.utils.CSCommonUtils;

public class TestInfo {

  public static void main(String[] args) throws Exception {

    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    LinkisHAWorkFlowContextID contextID = new LinkisHAWorkFlowContextID();
    contextID.setFlow("test");
    contextID.setProject("test");
    contextID.setWorkSpace("test1");
    contextID.setVersion("v00001");
    Context context = contextClient.createContext(contextID);
    System.out.println(context.getContextID().getContextId());

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
    System.out.println(CSCommonUtils.gson.toJson(myValue));

    SearchService searchService = DefaultSearchService.getInstance();
    BMLResource bmlResource =
        searchService.searchContext(contextID, "ql", "sql", LinkisBMLResource.class);
    System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(bmlResource));

    contextClient.close();
  }
}
