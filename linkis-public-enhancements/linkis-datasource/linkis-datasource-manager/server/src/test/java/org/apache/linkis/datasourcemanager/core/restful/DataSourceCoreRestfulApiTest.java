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
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;
import org.apache.linkis.datasourcemanager.core.Scan;
import org.apache.linkis.datasourcemanager.core.WebApplicationServer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.service.MetadataOperateService;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;

import java.io.StringWriter;
import java.util.*;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
class DataSourceCoreRestfulApiTest {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceCoreRestfulApiTest.class);

  @Autowired protected MockMvc mockMvc;

  @MockBean private DataSourceRelateService dataSourceRelateService;

  @MockBean private DataSourceInfoService dataSourceInfoService;

  @MockBean private Validator beanValidator;

  @MockBean ParameterValidator parameterValidator;

  @MockBean MetadataOperateService metadataOperateService;

  @MockBean private ServerConfiguration serverConfiguration;

  private static MockedStatic<ModuleUserUtils> moduleUserUtils;

  @BeforeAll
  private static void init() {
    moduleUserUtils = Mockito.mockStatic(ModuleUserUtils.class);
  }

  @AfterAll
  private static void close() {
    moduleUserUtils.close();
  }

  @BeforeEach
  public void setUp() {}

  @Test
  void getAllDataSourceTypes() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    List<DataSourceType> dataSourceTypes = new ArrayList<>();
    Mockito.when(dataSourceRelateService.getAllDataSourceTypes(null)).thenReturn(dataSourceTypes);
    MvcResult mvcResult = mvcUtils.buildMvcResultGet("/data-source-manager/type/all");
    Message res = mvcUtils.getMessage(mvcResult);
    assertThat(dataSourceTypes).usingRecursiveComparison().isEqualTo(res.getData().get("typeList"));
  }

  @Test
  void getKeyDefinitionsByType() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    Long dataSourceTypeId = 1l;
    List<DataSourceParamKeyDefinition> keyDefinitions = new ArrayList<>();
    Mockito.when(dataSourceRelateService.getKeyDefinitionsByType(dataSourceTypeId))
        .thenReturn(keyDefinitions);
    MvcResult mvcResult =
        mvcUtils.buildMvcResultGet(
            String.format("/data-source-manager/key-define/type/%s", dataSourceTypeId));
    Message res = mvcUtils.getMessage(mvcResult);
    assertThat(keyDefinitions).usingRecursiveComparison().isEqualTo(res.getData().get("keyDefine"));
  }

  @Test
  void insertJsonInfo() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    DataSource dataSource = new DataSource();
    dataSource.setDataSourceName("test");
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, dataSource);

    Mockito.doNothing().when(dataSourceInfoService).saveDataSourceInfo(dataSource);
    MvcResult mvcResult =
        mvcUtils.buildMvcResultPost("/data-source-manager/info/json", dsJsonWriter.toString());
    Message res = mvcUtils.getMessage(mvcResult);
    assertEquals(MessageStatus.SUCCESS(), res.getStatus());

    Mockito.when(dataSourceInfoService.existDataSource(dataSource.getDataSourceName()))
        .thenReturn(true);
    mvcResult =
        mvcUtils.buildMvcResultPost("/data-source-manager/info/json", dsJsonWriter.toString());
    ;
    res = mvcUtils.getMessage(mvcResult);
    assertEquals(MessageStatus.ERROR(), res.getStatus());
  }

  @Test
  void updateDataSourceInJson() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    String url = String.format("/data-source-manager/info/%s/json", dataSourceId);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setDataSourceName("ds-hive");
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, dataSource);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser");
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("This data source was not found"));

    DataSource oldDataSource = new DataSource();
    oldDataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoBrief(dataSource.getId()))
        .thenReturn(oldDataSource);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have update permission for data source"));

    oldDataSource.setCreateUser("testUser");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());

    oldDataSource.setDataSourceName("ds-mysql");
    Mockito.when(dataSourceInfoService.existDataSource(dataSource.getDataSourceName()))
        .thenReturn(true);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus() && res.getMessage().contains("has been existed"));
  }

  @Test
  void insertJsonParameter() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    Long datasourceId = 1l;
    String url = String.format("/data-source-manager/parameter/%s/json", datasourceId);
    Map<String, Object> params = new HashMap<>();
    Map<String, Object> connectParams = new HashMap<>();
    params.put("connectParams", connectParams);
    params.put("comment", "comment");

    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, params);

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser");
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Fail to insert data source parameter"));

    DataSource dataSource = new DataSource();
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoBrief(datasourceId)).thenReturn(dataSource);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have update permission"));

    dataSource.setCreateUser("testUser");
    List<DataSourceParamKeyDefinition> keyDefinitionList = new ArrayList<>();
    Mockito.when(dataSourceRelateService.getKeyDefinitionsByType(datasourceId))
        .thenReturn(keyDefinitionList);
    Mockito.when(
            dataSourceInfoService.insertDataSourceParameter(
                keyDefinitionList, datasourceId, connectParams, "testUser", "comment"))
        .thenReturn(10l);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.SUCCESS() == res.getStatus()
            && "10".equals(res.getData().get("version").toString()));
  }

  @Test
  void getInfoByDataSourceId() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    Long dataSourceId = 1l;
    String url = String.format("/data-source-manager/info/%s", dataSourceId);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfo(dataSourceId))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have query permission"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void getInfoByDataSourceName() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    String dataSourceName = "hive-test";
    String url = String.format("/data-source-manager/info/name/%s", dataSourceName);
    DataSource dataSource = new DataSource();
    dataSource.setDataSourceName(dataSourceName);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfo(dataSourceName))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have query permission"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void getInfoByDataSourceIdAndVersion() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    long version = 1001l;
    String url = String.format("/data-source-manager/info/%s/%s", dataSourceId, version);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setVersionId(version);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfo(dataSourceId, version))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have query permission"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void getVersionList() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    String url = String.format("/data-source-manager/%s/versions", dataSourceId);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoBrief(dataSourceId))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have query permission"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void publishByDataSourceId() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    long version = 1001l;
    String url = String.format("/data-source-manager/publish/%s/%s", dataSourceId, version);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setVersionId(version);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoBrief(dataSourceId))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have publish permission"));

    Mockito.when(dataSourceInfoService.publishByDataSourceId(dataSourceId, version))
        .thenReturn(0)
        .thenReturn(1);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus() && res.getMessage().contains("publish error"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void removeDataSource() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    String url = String.format("/data-source-manager/info/delete/%s", dataSourceId);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoBrief(dataSourceId))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have delete permission"));

    Mockito.when(dataSourceInfoService.removeDataSourceInfo(dataSourceId, ""))
        .thenReturn(-1l)
        .thenReturn(1l);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Fail to remove data source"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultDelete(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void expireDataSource() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    String url = String.format("/data-source-manager/info/%s/expire", dataSourceId);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoBrief(dataSourceId))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have operation permission"));

    Mockito.when(dataSourceInfoService.expireDataSource(dataSourceId))
        .thenReturn(-1l)
        .thenReturn(1l);
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Fail to expire data source"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void testGetConnectParamsById() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    String url = String.format("/data-source-manager/%s/connect-params", dataSourceId);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoForConnect(dataSourceId))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have query permission"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void testGetConnectParamsByName() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    String dataSourceName = "hive-test";
    String url = String.format("/data-source-manager/name/%s/connect-params", dataSourceName);
    DataSource dataSource = new DataSource();
    dataSource.setDataSourceName(dataSourceName);
    dataSource.setCreateUser("hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoForConnect(dataSourceName))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser")
        .thenReturn("hadoop");
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have query permission"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void connectDataSource() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    long dataSourceId = 1l;
    long version = 1001l;
    String url = String.format("/data-source-manager/%s/%s/op/connect", dataSourceId, version);
    DataSource dataSource = new DataSource();
    dataSource.setId(dataSourceId);
    dataSource.setVersionId(version);
    dataSource.setCreateUser("hadoop");
    DataSourceType dataSourceType = new DataSourceType();
    dataSourceType.setName("hive");
    dataSource.setDataSourceType(dataSourceType);
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser", "testUser", "hadoop");
    Mockito.when(dataSourceInfoService.getDataSourceInfoForConnect(dataSourceId, version))
        .thenReturn(null)
        .thenReturn(dataSource);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("No Exists The DataSource"));

    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(
        MessageStatus.ERROR() == res.getStatus()
            && res.getMessage().contains("Don't have operation permission"));

    Mockito.doNothing()
        .when(metadataOperateService)
        .doRemoteConnect(
            "metadata-manager", dataSourceType.getName(), "hadoop", new HashMap<String, Object>());
    res = mvcUtils.getMessage(mvcUtils.buildMvcResultPut(url));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());
  }

  @Test
  void queryDataSource() throws Exception {
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    String url = String.format("/data-source-manager/info");
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("currentPage", "10");
    params.add("pageSize", "20");
    moduleUserUtils
        .when(
            () ->
                ModuleUserUtils.getOperationUser(isA(HttpServletRequest.class), isA(String.class)))
        .thenReturn("testUser");

    DataSourceVo dataSourceVo = new DataSourceVo();
    PageInfo<DataSource> pageInfo = new PageInfo<>();
    pageInfo.setTotal(10l);
    Mockito.when(dataSourceInfoService.queryDataSourceInfoPage(any())).thenReturn(pageInfo);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
    assertTrue(
        MessageStatus.SUCCESS() == res.getStatus()
            && "10".equals(res.getData().get("totalPage").toString()));
  }

  @Test
  void queryDataSourceByIds() throws Exception {
    long id = 10l;
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    String url = String.format("/data-source-manager/info/ids");
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, Arrays.asList(id));
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("ids", dsJsonWriter.toString());

    List<DataSource> dataSourceList = new ArrayList<>();
    DataSource dataSource = new DataSource();
    dataSource.setId(id);
    dataSourceList.add(dataSource);
    Mockito.when(dataSourceInfoService.queryDataSourceInfo(any())).thenReturn(dataSourceList);
    Message res = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, params));
    assertTrue(MessageStatus.SUCCESS() == res.getStatus());

    List<Map<String, Object>> queryList =
        (List<Map<String, Object>>) res.getData().get("queryList");
    assertTrue(!CollectionUtils.isEmpty(queryList));
    assertEquals(10, queryList.get(0).get("id"));
  }
}
