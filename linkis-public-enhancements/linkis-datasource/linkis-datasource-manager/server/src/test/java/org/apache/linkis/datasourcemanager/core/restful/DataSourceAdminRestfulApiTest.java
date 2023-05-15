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

package org.apache.linkis.datasourcemanager.core.restful;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.core.Scan;
import org.apache.linkis.datasourcemanager.core.WebApplicationServer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
class DataSourceAdminRestfulApiTest {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceAdminRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @MockBean private Validator beanValidator;

  @MockBean private ParameterValidator parameterValidator;

  @MockBean private DataSourceInfoService dataSourceInfoService;

  private static MockedStatic<ModuleUserUtils> moduleUserUtils;

  @BeforeAll
  private static void init() {
    moduleUserUtils = Mockito.mockStatic(ModuleUserUtils.class);
  }

  @AfterAll
  private static void close() {
    moduleUserUtils.close();
  }

  @Test
  void insertJsonEnv() throws Exception {
    long dataSourceEnvId = 10l;
    String url = "/data-source-manager/env/json";
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setId(dataSourceEnvId);
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, dataSourceEnv);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser", "hadoop");
    Message mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("is not admin user"));

    Mockito.doNothing().when(parameterValidator).validate(any(), any());
    Mockito.doNothing().when(dataSourceInfoService).saveDataSourceEnv(any());
    mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));

    assertTrue(
        MessageStatus.SUCCESS() == mvcResult.getStatus()
            && "10".equals(mvcResult.getData().get("insertId").toString()));
  }

  @Test
  void insertJsonEnvBatch() throws Exception {
    long dataSourceEnvId = 10l;
    String fromSystem = "WTSS";
    String url = "/data-source-manager/env/json/batch?system=";
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    List<DataSourceEnv> dataSourceEnvList = new ArrayList<>();
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setId(dataSourceEnvId);
    dataSourceEnvList.add(dataSourceEnv);
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, dataSourceEnvList);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser", "hadoop");
    Message mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url + fromSystem, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("is not admin user"));

    fromSystem = "Qualitis";
    mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url + fromSystem, dsJsonWriter.toString()));

    Mockito.doNothing().when(parameterValidator).validate(any(), any());
    Mockito.doNothing().when(dataSourceInfoService).saveDataSourceEnv(any());
    assertTrue(MessageStatus.SUCCESS() == mvcResult.getStatus());
    List<Map<String, Object>> dataSourceEnvMap =
        (List<Map<String, Object>>) mvcResult.getData().get("envs");
    assertTrue(CollectionUtils.isNotEmpty(dataSourceEnvMap));
    assertEquals(10, dataSourceEnvMap.get(0).get("id"));
  }

  @Test
  void updateJsonEnvBatch() throws Exception {
    long dataSourceEnvId = 10l;
    String fromSystem = "WTSS";
    String url = "/data-source-manager/env/json/batch?system=";

    List<DataSourceEnv> dataSourceEnvList = new ArrayList<>();
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setId(dataSourceEnvId);
    dataSourceEnvList.add(dataSourceEnv);
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, dataSourceEnvList);

    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser", "hadoop");
    Message mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url + fromSystem, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("is not admin user"));

    fromSystem = "Qualitis";
    Mockito.when(dataSourceInfoService.getDataSourceEnv(dataSourceEnvId))
        .thenReturn(null)
        .thenReturn(dataSourceEnv);
    mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url + fromSystem, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("Fail to update data source environment"));

    mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(MessageStatus.SUCCESS() == mvcResult.getStatus());
    List<Map<String, Object>> dataSourceEnvMap =
        (List<Map<String, Object>>) mvcResult.getData().get("envs");
    assertTrue(CollectionUtils.isNotEmpty(dataSourceEnvMap));
    assertEquals(10, dataSourceEnvMap.get(0).get("id"));
  }

  @Test
  void getAllEnvListByDataSourceType() throws Exception {
    long dataSourceEnvTypeId = 10l;
    String url = String.format("/data-source-manager/env-list/all/type/%s", dataSourceEnvTypeId);
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    List<DataSourceEnv> envList = new ArrayList<>();
    Mockito.when(dataSourceInfoService.listDataSourceEnvByType(dataSourceEnvTypeId))
        .thenReturn(envList);
    Message mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == mvcResult.getStatus());
    Mockito.verify(dataSourceInfoService, times(1)).listDataSourceEnvByType(dataSourceEnvTypeId);
  }

  @Test
  void getEnvEntityById() throws Exception {
    long dataSourceEnvId = 10l;
    String url = String.format("/data-source-manager/env/%s", dataSourceEnvId);
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    List<DataSourceEnv> envList = new ArrayList<>();
    Mockito.when(dataSourceInfoService.getDataSourceEnv(dataSourceEnvId))
        .thenReturn(new DataSourceEnv());
    Message mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == mvcResult.getStatus());
    Mockito.verify(dataSourceInfoService, times(1)).getDataSourceEnv(dataSourceEnvId);
  }

  @Test
  void removeEnvEntity() throws Exception {
    long dataSourceEnvId = 10l;
    String url = String.format("/data-source-manager/env/%s", dataSourceEnvId);
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser", "hadoop");
    Message mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("is not admin user"));

    Mockito.when(dataSourceInfoService.removeDataSourceEnv(dataSourceEnvId))
        .thenReturn(dataSourceEnvId);
    mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    assertTrue(
        MessageStatus.SUCCESS() == mvcResult.getStatus()
            && "10".equals(mvcResult.getData().get("removeId").toString()));
  }

  @Test
  void updateJsonEnv() throws Exception {
    long dataSourceEnvId = 10l;
    String url = String.format("/data-source-manager/env/%s/json", dataSourceEnvId);

    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setId(dataSourceEnvId);
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, dataSourceEnv);

    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser", "hadoop");
    Message mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("is not admin user"));

    Mockito.when(dataSourceInfoService.getDataSourceEnv(dataSourceEnvId))
        .thenReturn(null)
        .thenReturn(dataSourceEnv);
    mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("Fail to update data source environment"));

    mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(MessageStatus.SUCCESS() == mvcResult.getStatus());
  }

  @Test
  void queryDataSourceEnv() throws Exception {
    String url = String.format("/data-source-manager/env");
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    Mockito.when(dataSourceInfoService.queryDataSourceEnvPage(any())).thenReturn(new ArrayList<>());
    Message mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == mvcResult.getStatus());
    Mockito.verify(dataSourceInfoService, times(1)).queryDataSourceEnvPage(any());
  }
}
