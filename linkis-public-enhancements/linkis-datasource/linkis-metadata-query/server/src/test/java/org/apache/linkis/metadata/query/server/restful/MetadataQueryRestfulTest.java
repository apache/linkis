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

package org.apache.linkis.metadata.query.server.restful;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadata.query.server.WebApplicationServer;
import org.apache.linkis.metadata.query.server.service.MetadataQueryService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.isA;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class})
class MetadataQueryRestfulTest {

  @Autowired protected MockMvc mockMvc;

  @MockBean private MetadataQueryService metadataQueryService;

  private static MockedStatic<ModuleUserUtils> moduleUserUtils;

  @BeforeAll
  private static void init() {
    moduleUserUtils = Mockito.mockStatic(ModuleUserUtils.class);
  }

  @AfterAll
  private static void close() {
    moduleUserUtils.close();
  }

  String dataSourceName = "test";
  String database = "hivedb";
  String table = "testtab";
  String system = "linkis";

  @Test
  void testGetDatabases() {
    try {
      String url = "/metadataQuery/getDatabases";
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.set("system", "");
      params.set("dataSourceName", dataSourceName);

      MvcUtils mvcUtils = new MvcUtils(mockMvc);
      moduleUserUtils
          .when(
              () ->
                  ModuleUserUtils.getOperationUser(
                      isA(HttpServletRequest.class), isA(String.class)))
          .thenReturn("hadoop", "hadoop", "hadoop");
      Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("'system' is missing"));
      params.set("system", system);
      Mockito.when(metadataQueryService.getDatabasesByDsName(dataSourceName, system, "hadoop"))
          .thenReturn(new ArrayList<>());
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(MessageStatus.SUCCESS() == res.getStatus());

      Mockito.doThrow(new ErrorException(1, ""))
          .when(metadataQueryService)
          .getDatabasesByDsName(dataSourceName, system, "hadoop");

      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("Fail to get database list"));
    } catch (Exception e) {
      // ignore
    }
  }

  @Test
  void testGetTables() throws Exception {

    String url = "/metadataQuery/getTables";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.set("system", "");
    params.set("dataSourceName", dataSourceName);
    params.set("database", database);
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("hadoop", "hadoop", "hadoop");
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
    Assertions.assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("'system' is missing"));

    params.set("system", system);
    Mockito.when(metadataQueryService.getTablesByDsName(dataSourceName, database, system, "hadoop"))
        .thenReturn(new ArrayList<>());
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
    Assertions.assertTrue(MessageStatus.SUCCESS() == res.getStatus());

    Mockito.doThrow(new ErrorException(1, ""))
        .when(metadataQueryService)
        .getTablesByDsName(dataSourceName, database, system, "hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
    Assertions.assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Fail to get table list"));
  }

  @Test
  void testGetTableProps() {
    try {
      String url = "/metadataQuery/getTableProps";

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.set("dataSourceName", dataSourceName);
      params.set("database", database);
      params.set("table", table);
      params.set("system", "");

      MvcUtils mvcUtils = new MvcUtils(mockMvc);
      moduleUserUtils
          .when(
              () ->
                  ModuleUserUtils.getOperationUser(
                      isA(HttpServletRequest.class), isA(String.class)))
          .thenReturn("hadoop", "hadoop", "hadoop");
      Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("'system' is missing"));

      params.set("system", system);
      Mockito.when(
              metadataQueryService.getTablePropsByDsName(
                  dataSourceName, database, table, system, "hadoop"))
          .thenReturn(new HashMap<>());
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(MessageStatus.SUCCESS() == res.getStatus());

      Mockito.doThrow(new ErrorException(1, ""))
          .when(metadataQueryService)
          .getTablePropsByDsName(dataSourceName, database, table, system, "hadoop");
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("Fail to get table properties"));
    } catch (Exception e) {
      // ignore
    }
  }

  @Test
  void testGetPartitions() {
    try {

      String url = "/metadataQuery/getPartitions";
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.set("dataSourceName", dataSourceName);
      params.set("database", database);
      params.set("table", table);
      params.set("system", "");
      MvcUtils mvcUtils = new MvcUtils(mockMvc);
      moduleUserUtils
          .when(
              () ->
                  ModuleUserUtils.getOperationUser(
                      isA(HttpServletRequest.class), isA(String.class)))
          .thenReturn("hadoop", "hadoop", "hadoop");
      Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("'system' is missing"));

      params.set("system", system);
      Mockito.when(
              metadataQueryService.getPartitionsByDsName(
                  dataSourceName, database, table, system, false, "hadoop"))
          .thenReturn(new MetaPartitionInfo());
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(MessageStatus.SUCCESS() == res.getStatus());

      Mockito.doThrow(new ErrorException(1, ""))
          .when(metadataQueryService)
          .getPartitionsByDsName(dataSourceName, database, table, system, false, "hadoop");
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("Fail to get partitions"));
    } catch (Exception e) {
      // ignore
    }
  }

  @Test
  void testGetPartitionProps() {
    try {

      String partition = "ds";

      String url = "/metadataQuery/getPartitionProps";
      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.set("dataSourceName", dataSourceName);
      params.set("database", database);
      params.set("table", table);
      params.set("partition", partition);
      params.set("system", "");
      MvcUtils mvcUtils = new MvcUtils(mockMvc);
      moduleUserUtils
          .when(
              () ->
                  ModuleUserUtils.getOperationUser(
                      isA(HttpServletRequest.class), isA(String.class)))
          .thenReturn("hadoop", "hadoop", "hadoop");
      Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("'system' is missing"));

      params.set("system", system);
      Mockito.when(
              metadataQueryService.getPartitionPropsByDsName(
                  dataSourceName, database, table, partition, system, "hadoop"))
          .thenReturn(new HashMap<>());
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(MessageStatus.SUCCESS() == res.getStatus());

      Mockito.doThrow(new ErrorException(1, ""))
          .when(metadataQueryService)
          .getPartitionPropsByDsName(dataSourceName, database, table, partition, system, "hadoop");
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("Fail to get partition properties"));
    } catch (Exception e) {
      // ignore
    }
  }

  @Test
  void testGetColumns() {
    try {
      String url = "/metadataQuery/getColumns";

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.set("dataSourceName", dataSourceName);
      params.set("database", database);
      params.set("table", table);
      params.set("system", "");
      MvcUtils mvcUtils = new MvcUtils(mockMvc);
      moduleUserUtils
          .when(
              () ->
                  ModuleUserUtils.getOperationUser(
                      isA(HttpServletRequest.class), isA(String.class)))
          .thenReturn("hadoop", "hadoop", "hadoop");
      Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("'system' is missing"));

      params.set("system", system);
      Mockito.when(
              metadataQueryService.getColumnsByDsName(
                  dataSourceName, database, table, system, "hadoop"))
          .thenReturn(new ArrayList<>());
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(MessageStatus.SUCCESS() == res.getStatus());

      Mockito.doThrow(new ErrorException(1, ""))
          .when(metadataQueryService)
          .getColumnsByDsName(dataSourceName, database, table, system, "hadoop");
      res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
      Assertions.assertTrue(
          MessageStatus.ERROR() == res.getStatus()
              && res.getMessage().contains("Fail to get column list"));
    } catch (Exception e) {
      // ignore
    }
  }
}
