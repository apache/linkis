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

package org.apache.linkis.cs.common.serialize.test;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.history.CommonResourceHistory;
import org.apache.linkis.cs.common.entity.history.metadata.CSTableLineageHistory;
import org.apache.linkis.cs.common.entity.history.metadata.CSTableMetadataContextHistory;
import org.apache.linkis.cs.common.entity.history.metadata.TableOperationType;
import org.apache.linkis.cs.common.entity.metadata.CSTable;
import org.apache.linkis.cs.common.entity.metadata.Table;
import org.apache.linkis.cs.common.entity.object.CSFlowInfos;
import org.apache.linkis.cs.common.entity.resource.LinkisBMLResource;
import org.apache.linkis.cs.common.entity.source.*;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextSerializationHelperTest {

  public static void main(String[] args) throws Exception {
    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();

    CSTable csTable = new CSTable();
    csTable.setCreator("hadoop");
    csTable.setName("table1");

    CSTable csTable1 = new CSTable();
    csTable1.setCreator("hadoop");
    csTable1.setName("table2");
    CSTableMetadataContextHistory csTableMetadataContextHistory =
        new CSTableMetadataContextHistory();
    csTableMetadataContextHistory.setTable(csTable);
    csTableMetadataContextHistory.setOperationType(TableOperationType.ACCESS);
    csTableMetadataContextHistory.setSource("SparkEngine");
    String serialize = contextSerializationHelper.serialize(csTableMetadataContextHistory);
    Object deserialize = contextSerializationHelper.deserialize(serialize);
    System.out.println("test");
    testLineageHistory();
  }

  public static void testLineageHistory() throws Exception {
    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();
    List<Table> tables = new ArrayList<>();
    CSTable csTable = new CSTable();
    csTable.setCreator("hadoop");
    csTable.setName("table1");

    CSTable csTable1 = new CSTable();
    csTable1.setCreator("hadoop");
    csTable1.setName("table2");
    tables.add(csTable);
    tables.add(csTable1);
    CSTableLineageHistory history = new CSTableLineageHistory();
    history.setSourceTables(tables);
    history.setTable(csTable1);
    history.setSource("test");
    String serialize = contextSerializationHelper.serialize(history);
    Object deserialize = contextSerializationHelper.deserialize(serialize);
    CSTableLineageHistory deserialize1 = (CSTableLineageHistory) deserialize;
    List<Table> sourceTables = deserialize1.getSourceTables();
    System.out.println(sourceTables.get(0).getName());
    System.out.println(serialize);
  }

  public static void testCommonResourceHistory() throws Exception {
    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();
    LinkisBMLResource resource = new LinkisBMLResource();
    resource.setResourceId("dfasdfsr2456wertg");
    resource.setVersion("v000002");

    resource.setResourceId("edtr44356-34563456");
    resource.setVersion("v000004");

    CommonResourceHistory history = new CommonResourceHistory();
    history.setSource("history source");
    history.setResource(resource);
    String serialize = contextSerializationHelper.serialize(history);
    Object deserialize = contextSerializationHelper.deserialize(serialize);
    System.out.println("test");
  }

  public static void testHAContextID() throws Exception {
    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();
    LinkisHAWorkFlowContextID haWorkFlowContextID = new LinkisHAWorkFlowContextID();
    haWorkFlowContextID.setBackupInstance("test123");
    haWorkFlowContextID.setInstance("test1234");
    haWorkFlowContextID.setUser("hadoop");
    haWorkFlowContextID.setContextId("hello");
    haWorkFlowContextID.setFlow("hellof");
    String json = contextSerializationHelper.serialize(haWorkFlowContextID);
    String objstr =
        "{\"type\": \"HAWorkFlowContextID\",\"value\": \"{\\n  \\\"instance\\\": null,\\n  \\\"backupInstance\\\": null,\\n  \\\"user\\\": \\\"hadoop\\\",\\n  \\\"workspaceID\\\": null,\\n  \\\"project\\\": \\\"test01_neiljianliu\\\",\\n  \\\"flow\\\": \\\"dedede\\\",\\n  \\\"contextId\\\": \\\"24-24--YmRwZHdzMTEwMDAxOjkxMTY\\\\u003dYmRwZHdzMTEwMDAxOjkxMTY\\\\u003d85197\\\",\\n  \\\"version\\\": \\\"v000001\\\",\\n  \\\"env\\\": null\\n}\"}\n";
    Object deserialize = contextSerializationHelper.deserialize(objstr);
    System.out.println(json);
  }

  public static void testCSFlowInfos() throws Exception {
    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();
    CommonContextValue contextValue = new CommonContextValue();
    contextValue.setKeywords("test.123.test");

    Map<String, Object> infos = new HashMap<>();
    List<Map<String, String>> edges = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Map<String, String> edge = new HashMap<>();
      edge.put("source", "job_export_tb1_crdtloan " + i);
      edge.put("target", "job_export_tb1_crdtloan");
      edge.put("sourceLocation", "top");
      edge.put("targetLocation", "left");
      edges.add(edge);
    }
    infos.put("edges", edges);
    infos.put("parent", "flow2");
    Map<String, String> idNodeName = new HashMap<>();
    idNodeName.put("12345", "job_export_tb1_crdtloan");
    infos.put("id_nodeName", idNodeName);
    CSFlowInfos csFlowInfos = new CSFlowInfos();
    csFlowInfos.setInfos(infos);
    contextValue.setValue(csFlowInfos);
    System.out.println(contextSerializationHelper.serialize(contextValue));
    Object deserialize =
        contextSerializationHelper.deserialize(contextSerializationHelper.serialize(contextValue));
    ContextValue contextValue1 = (ContextValue) deserialize;
    CSFlowInfos value1 = (CSFlowInfos) contextValue1.getValue();
    System.out.println(value1.getInfos());
  }

  private static void testSrialzer() throws Exception {
    ContextSerializationHelper contextSerializationHelper =
        ContextSerializationHelper.getInstance();
    /* LinkisHAWorkFlowContextID haWorkFlowContextID = new LinkisHAWorkFlowContextID();
    haWorkFlowContextID.setBackupInstance("test123");
    haWorkFlowContextID.setInstance("test1234");
    haWorkFlowContextID.setUser("hadoop");
    haWorkFlowContextID.setContextId("hello");
    haWorkFlowContextID.setFlow("hellof");
    String json = contextSerializationHelper.serialize(haWorkFlowContextID);
    */
    CommonContextKey contextKey = new CommonContextKey();
    contextKey.setContextScope(ContextScope.FRIENDLY);
    contextKey.setContextType(ContextType.OBJECT);
    contextKey.setKey("test");

    LinkisBMLResource linkisBMLResource = new LinkisBMLResource();
    linkisBMLResource.setResourceId("12345");
    linkisBMLResource.setVersion("v01");

    CommonContextValue contextValue = new CommonContextValue();
    contextValue.setKeywords("test.123.test");
    contextValue.setValue(linkisBMLResource);

    CommonContextKeyValue contextKeyValue = new CommonContextKeyValue();

    contextKeyValue.setContextKey(contextKey);
    contextKeyValue.setContextValue(contextValue);

    String json = contextSerializationHelper.serialize(contextKeyValue);
    System.out.println(json);
    Object obj = contextSerializationHelper.deserialize(json);
    System.out.println("hello");
  }
}
