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

package org.apache.linkis.cs.client.test.test_multiuser;

import org.apache.linkis.cs.client.Context;
import org.apache.linkis.cs.client.ContextClient;
import org.apache.linkis.cs.client.builder.ContextClientFactory;
import org.apache.linkis.cs.client.service.DefaultSearchService;
import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.resource.BMLResource;
import org.apache.linkis.cs.common.entity.resource.LinkisBMLResource;
import org.apache.linkis.cs.common.entity.source.*;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import org.apache.linkis.cs.common.serialize.helper.SerializationHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class TestCreateContext {

  public static final String CONTEXTID_PATH = "out/test-store-contextID.txt";

  public static void main(String[] args) throws Exception {

    // 1, create contextid
    ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
    FileWriter fr = null;
    BufferedWriter bw = null;
    try {

      Context context = contextClient.createContext("test_client", "test_client", "alex", null);
      System.out.println(context.getContextID().getContextId());

      // 2, save contxtid
      ContextID contextID = context.getContextID();
      SerializationHelper serializationHelper = ContextSerializationHelper.getInstance();
      String contextIDStr = null;
      if (serializationHelper.accepts(contextID)) {
        contextIDStr = serializationHelper.serialize(contextID);
        File file = new File(CONTEXTID_PATH);
        fr = new FileWriter(file);
        bw = new BufferedWriter(fr);
        bw.write(contextIDStr);
        bw.flush();
        System.out.println("ContextID saved at : " + file.getAbsolutePath());
        // test deserialize
        System.out.println("contextIDStr: " + contextIDStr);
        Object obj = serializationHelper.deserialize(contextIDStr);
        System.out.println("Deserialize jsonstr : " + new Gson().toJson(obj));
      } else {
        System.out.println("ContextID not saved.");
      }

      // 3, test search
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
      LinkisBMLResource linkisBMLResource = (LinkisBMLResource) myValue.getValue();
      System.out.println(linkisBMLResource.getResourceId());
      BMLResource rs =
          DefaultSearchService.getInstance()
              .getContextValue(contextID, contextKey, LinkisBMLResource.class);
      System.out.println(new Gson().toJson(rs));
      List<String> contains = new ArrayList<>();
      contains.add("cooper");
      List<ContextKeyValue> contextKeyValueList =
          contextClient.search(context.getContextID(), null, null, contains, null);
      System.out.println("ContextKVList : " + new Gson().toJson(contextKeyValueList));
    } catch (Exception e) {
      contextClient.close();
      e.printStackTrace();
    } finally {
      if (fr != null) {
        try {
          fr.close();
        } catch (Exception e) {
          // ignore
        }
      }
      if (bw != null) {
        try {
          bw.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
}
