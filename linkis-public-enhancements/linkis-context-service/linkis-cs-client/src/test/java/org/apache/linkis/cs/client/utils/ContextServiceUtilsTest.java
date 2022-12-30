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

package org.apache.linkis.cs.client.utils;

import org.apache.linkis.cs.common.entity.source.CombinedNodeIDContextID;
import org.apache.linkis.cs.common.entity.source.ContextID;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContextServiceUtilsTest {

  @Test
  @DisplayName("getContextIDStrByMapTest")
  public void getContextIDStrByMapTest() {

    Map<String, Object> map = new HashMap<>();
    ContextID contextID = new CombinedNodeIDContextID();
    contextID.setContextId("90a6ee94-4bd6-47d9-a536-f92660c4c052");
    contextID.setContextIDType(1);
    map.put("contextID", new Gson().toJson(contextID));
    String contextId = ContextServiceUtils.getContextIDStrByMap(map);
    Assertions.assertNotNull(contextId);
  }

  @Test
  @DisplayName("getNodeNameStrByMapTest")
  public void getNodeNameStrByMapTest() {

    Map<String, Object> map = new HashMap<>();
    ContextID contextID = new CombinedNodeIDContextID();
    contextID.setContextId("90a6ee94-4bd6-47d9-a536-f92660c4c052");
    contextID.setContextIDType(1);
    map.put("contextID", new Gson().toJson(contextID));
    map.put("nodeName", "flow1");

    String nameStrByMap = ContextServiceUtils.getNodeNameStrByMap(map);
    Assertions.assertNotNull(nameStrByMap);
  }

  @Test
  @DisplayName("getContextIDStrByPropertiesTest")
  public void getContextIDStrByPropertiesTest() {

    Properties properties = new Properties();
    ContextID contextID = new CombinedNodeIDContextID();
    contextID.setContextId("90a6ee94-4bd6-47d9-a536-f92660c4c052");
    contextID.setContextIDType(1);
    properties.put("contextID", new Gson().toJson(contextID));

    String idStrByProperties = ContextServiceUtils.getContextIDStrByProperties(properties);
    Assertions.assertNotNull(idStrByProperties);
  }

  @Test
  @DisplayName("getNodeNameStrByPropertiesTest")
  public void getNodeNameStrByPropertiesTest() {

    Properties properties = new Properties();
    ContextID contextID = new CombinedNodeIDContextID();
    contextID.setContextId("90a6ee94-4bd6-47d9-a536-f92660c4c052");
    contextID.setContextIDType(1);
    properties.put("contextID", new Gson().toJson(contextID));
    properties.put("nodeName", "flow1");

    String strByProperties = ContextServiceUtils.getNodeNameStrByProperties(properties);
    Assertions.assertNotNull(strByProperties);
  }

  @Test
  @DisplayName("getNodeNameByCombinedNodeIDContextIDTest")
  public void getNodeNameByCombinedNodeIDContextIDTest() {

    ContextID contextID = new CombinedNodeIDContextID();
    contextID.setContextId("90a6ee94-4bd6-47d9-a536-f92660c4c052");
    contextID.setContextIDType(1);
    String contextIDStr = new Gson().toJson(contextID);

    String nodeIDContextID = ContextServiceUtils.getNodeNameByCombinedNodeIDContextID(contextIDStr);
    Assertions.assertNull(nodeIDContextID);
  }
}
