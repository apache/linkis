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

package org.apache.linkis.datasourcemanager.core.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceDao;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceEnvDao;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceParamKeyDao;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceVersionDao;
import org.apache.linkis.datasourcemanager.core.service.impl.DataSourceInfoServiceImpl;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class DataSourceInfoServiceTest {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceInfoServiceTest.class);
  @InjectMocks DataSourceInfoServiceImpl dataSourceInfoService;

  @Mock DataSourceDao dataSourceDao;

  @Mock DataSourceEnvDao dataSourceEnvDao;

  @Mock DataSourceVersionDao dataSourceVersionDao;

  @Mock DataSourceParamKeyDao dataSourceParamKeyDao;

  private DataSource buildDataSource() {
    DataSource dataSource = new DataSource();
    dataSource.setId(1l);
    dataSource.setDataSourceName("unitTest");
    dataSource.setDataSourceTypeId(1l);
    dataSource.setDataSourceDesc("unit test by h2 database");
    dataSource.setCreateIdentify("unitTest");
    dataSource.setCreateSystem("local");
    dataSource.setCreateUser("test");
    dataSource.setParameter("{}");
    dataSource.setModifyUser("test");
    dataSource.setVersionId(1l);
    dataSource.setPublishedVersionId(1l);
    dataSource.setCreateTime(new Date());
    dataSource.setModifyTime(new Date());
    return dataSource;
  }

  private DataSourceEnv buildDataSourceEnv() {
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setId(1L);
    dataSourceEnv.setEnvName("test_env_name");
    dataSourceEnv.setCreateUser("test");
    dataSourceEnv.setParameter("{\"username\":\"test_dev\",\"password\":\"12e12e12e1\"}");
    dataSourceEnv.setModifyUser("test");
    dataSourceEnv.setDataSourceTypeId(1L);
    return dataSourceEnv;
  }

  @Test
  void testSaveDataSourceInfo() throws ErrorException {
    DataSource dataSource = buildDataSource();
    Mockito.doNothing().when(dataSourceDao).insertOne(dataSource);
    dataSourceInfoService.saveDataSourceInfo(dataSource);
    Mockito.verify(dataSourceDao, Mockito.times(1)).insertOne(dataSource);
  }

  @Test
  void testAddEnvParamsToDataSource() {
    DataSource dataSource = buildDataSource();
    Map<String, Object> envParamMap = new HashMap<>();
    envParamMap.put("a", "b");
    dataSource.setConnectParams(envParamMap);
    Long dataSourceEnvId = 1l;
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    Map<String, Object> envAllParamMap = new HashMap<>();
    envAllParamMap.put("c", "d");
    dataSourceEnv.setConnectParams(envAllParamMap);
    envAllParamMap.putAll(envAllParamMap);
    Mockito.when(dataSourceEnvDao.selectOneDetail(dataSourceEnvId)).thenReturn(dataSourceEnv);
    dataSourceInfoService.addEnvParamsToDataSource(dataSourceEnvId, dataSource);
    assertTrue(envAllParamMap.equals(dataSource.getConnectParams()));
  }

  @Test
  void testGetDataSourceInfo() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetail(dataSource.getId())).thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(dataSource.getId(), dataSource.getVersionId()))
        .thenReturn("a:b");
    dataSource.setParameter("a:b");
    DataSource actuallyDataSource = dataSourceInfoService.getDataSourceInfo(dataSource.getId());
    assertThat(actuallyDataSource).usingRecursiveComparison().isEqualTo(dataSource);
  }

  @Test
  void testGetDataSourceInfoByName() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetailByName(dataSource.getDataSourceName()))
        .thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(dataSource.getId(), dataSource.getVersionId()))
        .thenReturn("a:b");
    dataSource.setParameter("a:b");
    DataSource actuallyDataSource =
        dataSourceInfoService.getDataSourceInfo(dataSource.getDataSourceName());
    assertThat(actuallyDataSource).usingRecursiveComparison().isEqualTo(dataSource);
  }

  @Test
  void testGetDataSourceInfoByIdAndVerId() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetail(dataSource.getId())).thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(dataSource.getId(), dataSource.getVersionId()))
        .thenReturn("a:b");
    dataSource.setParameter("a:b");
    DataSource actuallyDataSource =
        dataSourceInfoService.getDataSourceInfo(dataSource.getId(), dataSource.getVersionId());
    assertThat(actuallyDataSource).usingRecursiveComparison().isEqualTo(dataSource);
  }

  @Test
  void testGetDataSourceInfoForConnectById() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetail(dataSource.getId())).thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(
                dataSource.getId(), dataSource.getPublishedVersionId()))
        .thenReturn("{\"a\":\"b\"}");
    String res =
        dataSourceInfoService
            .getDataSourceInfoForConnect(dataSource.getId())
            .getConnectParams()
            .toString();
    assertTrue("{a=b}".equals(res));
    Mockito.verify(dataSourceDao, Mockito.times(1)).selectOneDetail(dataSource.getId());
  }

  @Test
  void testGetDataSourceInfoForConnectByName() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetailByName(dataSource.getDataSourceName()))
        .thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(
                dataSource.getId(), dataSource.getPublishedVersionId()))
        .thenReturn("{\"a\":\"b\"}");
    String res =
        dataSourceInfoService
            .getDataSourceInfoForConnect(dataSource.getDataSourceName())
            .getConnectParams()
            .toString();
    assertTrue("{a=b}".equals(res));
  }

  @Test
  void testGetDataSourceInfoForConnectByNameAndEnvId() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetailByName(dataSource.getDataSourceName()))
        .thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(
                dataSource.getId(), dataSource.getPublishedVersionId()))
        .thenReturn("{\"a\":\"b\"}");
    String res =
        dataSourceInfoService
            .getDataSourceInfoForConnect(dataSource.getDataSourceName(), "1")
            .getConnectParams()
            .toString();
    assertTrue("{a=b}".equals(res));
  }

  @Test
  void testGetDataSourceInfoForConnectByIdAndVerId() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneDetail(dataSource.getId())).thenReturn(dataSource);
    Mockito.when(
            dataSourceVersionDao.selectOneVersion(dataSource.getId(), dataSource.getVersionId()))
        .thenReturn("{\"a\":\"b\"}");
    String res =
        dataSourceInfoService
            .getDataSourceInfoForConnect(dataSource.getId(), dataSource.getVersionId())
            .getConnectParams()
            .toString();
    assertTrue("{a=b}".equals(res));
  }

  @Test
  void testExistDataSource() {
    String dataSourceName = "unitTest";
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOneByName(dataSourceName)).thenReturn(dataSource);
    assertTrue(dataSourceInfoService.existDataSource(dataSourceName));
  }

  @Test
  void testGetDataSourceInfoBrief() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOne(dataSource.getId())).thenReturn(dataSource);
    assertThat(dataSourceInfoService.getDataSourceInfoBrief(dataSource.getId()))
        .usingRecursiveComparison()
        .isEqualTo(dataSource);
  }

  @Test
  void testRemoveDataSourceInfo() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOne(dataSource.getId()))
        .thenReturn(dataSource)
        .thenReturn(null);
    Mockito.when(dataSourceDao.removeOne(dataSource.getId())).thenReturn(1);
    assertTrue(
        dataSourceInfoService.removeDataSourceInfo(dataSource.getId(), dataSource.getCreateSystem())
            > 0);
    assertTrue(
        dataSourceInfoService.removeDataSourceInfo(dataSource.getId(), dataSource.getCreateSystem())
            == -1);
  }

  @Test
  void testUpdateDataSourceInfo() {
    DataSource dataSource = buildDataSource();
    dataSourceInfoService.updateDataSourceInfo(dataSource);
  }

  @Test
  void testQueryDataSourceInfoPage() {
    DataSourceVo dataSourceVo = new DataSourceVo();
    dataSourceVo.setPageSize(10);
    dataSourceVo.setCurrentPage(1);

    List<DataSource> dataSources = new ArrayList<>();
    dataSources.add(buildDataSource());
    dataSources.add(buildDataSource());
    Mockito.when(dataSourceDao.selectByPageVo(dataSourceVo)).thenReturn(dataSources);
    PageInfo<DataSource> dataSourcePageInfo =
        dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
    assertTrue(dataSourcePageInfo.getSize() == 2);
  }

  @Test
  void testSaveDataSourceEnv() throws ErrorException {
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setCreateUser("test");
    dataSourceEnv.setKeyDefinitions(new ArrayList<>());
    dataSourceEnv.setConnectParams(new HashMap<>());
    dataSourceEnv.setDataSourceTypeId(1l);
    dataSourceInfoService.saveDataSourceEnv(dataSourceEnv);
  }

  @Test
  void testListDataSourceEnvByType() {
    List<DataSourceEnv> dataSourceEnvs = new ArrayList<>();
    dataSourceEnvs.add(new DataSourceEnv());
    dataSourceEnvs.add(new DataSourceEnv());
    Mockito.when(dataSourceEnvDao.listByTypeId(1l)).thenReturn(dataSourceEnvs);
    List<DataSourceEnv> actuallyDataSourceEnvs = dataSourceInfoService.listDataSourceEnvByType(1l);
    assertTrue(actuallyDataSourceEnvs.size() == 2);
  }

  @Test
  void testGetDataSourceEnv() {
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    Mockito.when(dataSourceEnvDao.selectOneDetail(1l)).thenReturn(dataSourceEnv);
    DataSourceEnv actuallyDataSourceEnv = dataSourceInfoService.getDataSourceEnv(1l);
    assertThat(actuallyDataSourceEnv).usingRecursiveComparison().isEqualTo(dataSourceEnv);
  }

  @Test
  void testRemoveDataSourceEnv() {
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setId(1l);
    dataSourceEnv.setDataSourceTypeId(1l);
    Map<String, Object> connectParams = new HashMap<>();
    connectParams.put("key", "value");
    dataSourceEnv.setConnectParams(connectParams);
    List<DataSourceParamKeyDefinition> keyDefinitions = new ArrayList<>();
    DataSourceParamKeyDefinition dsParamKeyDefinition = new DataSourceParamKeyDefinition();
    keyDefinitions.add(dsParamKeyDefinition);
    dsParamKeyDefinition.setKey("key");
    dsParamKeyDefinition.setValueType(DataSourceParamKeyDefinition.ValueType.FILE);
    Mockito.when(dataSourceEnvDao.selectOneDetail(dataSourceEnv.getId())).thenReturn(dataSourceEnv);
    Mockito.when(dataSourceEnvDao.removeOne(dataSourceEnv.getId())).thenReturn(1);
    Mockito.when(
            dataSourceParamKeyDao.listByDataSourceTypeAndScope(
                dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV))
        .thenReturn(keyDefinitions);
    Long res = dataSourceInfoService.removeDataSourceEnv(dataSourceEnv.getId());
    assertTrue(res == dataSourceEnv.getId());
  }

  @Test
  void testExpireDataSource() {
    DataSource dataSource = buildDataSource();
    Mockito.when(dataSourceDao.selectOne(dataSource.getId())).thenReturn(dataSource);
    Mockito.when(dataSourceDao.expireOne(dataSource.getId())).thenReturn(1);
    Long res = dataSourceInfoService.expireDataSource(dataSource.getId());
    assertTrue(res == 1);
  }

  @Test
  void testPublishByDataSourceId() {
    Mockito.when(dataSourceVersionDao.getLatestVersion(1l)).thenReturn(3l).thenReturn(1l);
    Mockito.when(dataSourceDao.setPublishedVersionId(1l, 2l)).thenReturn(1);
    int res = dataSourceInfoService.publishByDataSourceId(1l, 2l);
    assertTrue(res == 1);
    res = dataSourceInfoService.publishByDataSourceId(1l, 2l);
    assertTrue(res == 0);
  }

  @Test
  void testInsertDataSourceParameter() throws ErrorException {
    List<DataSourceParamKeyDefinition> keyDefinitionList = new ArrayList<>();
    Long datasourceId = 1l;
    Map<String, Object> connectParams = new HashMap<>();
    String username = "test";
    String comment = "unitTest";
    Long curVersion = 1l;
    Long expectedVersion = curVersion + 1l;
    Mockito.when(dataSourceVersionDao.getLatestVersion(1l)).thenReturn(curVersion);
    Long res =
        dataSourceInfoService.insertDataSourceParameter(
            keyDefinitionList, datasourceId, connectParams, username, comment);
    assertTrue(expectedVersion == res);
  }

  @Test
  void testExistDataSourceEnv() {
    String dataSourceEnvName = "test_env_name";
    DataSourceEnv dataSourceEnv = new DataSourceEnv();
    dataSourceEnv.setEnvName(dataSourceEnvName);
    dataSourceEnv.setId(1L);
    Mockito.when(dataSourceEnvDao.selectOneByName(dataSourceEnvName)).thenReturn(dataSourceEnv);
    Boolean result = dataSourceInfoService.existDataSourceEnv(dataSourceEnvName);
    assertTrue(result);

    Mockito.when(dataSourceEnvDao.selectOneByName(dataSourceEnvName)).thenReturn(null);
    result = dataSourceInfoService.existDataSourceEnv(dataSourceEnvName);
    assertFalse(result);

    assertFalse(dataSourceInfoService.existDataSourceEnv(""));
  }

  @Test
  void testSaveBatchDataSourceEnv() throws ErrorException {
    List<DataSourceEnv> list = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      DataSourceEnv dataSourceEnv = buildDataSourceEnv();
      list.add(dataSourceEnv);
      Mockito.doNothing().when(dataSourceEnvDao).insertOne(dataSourceEnv);
    }

    dataSourceInfoService.saveBatchDataSourceEnv(list);
  }

  @Test
  void testUpdateBatchDataSourceEnv() throws ErrorException {
    List<DataSourceEnv> list = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      DataSourceEnv dataSourceEnv = buildDataSourceEnv();
      list.add(dataSourceEnv);
      Mockito.doNothing().when(dataSourceEnvDao).updateOne(dataSourceEnv);
      Mockito.when(dataSourceEnvDao.selectOneDetail(dataSourceEnv.getId()))
          .thenReturn(dataSourceEnv);
    }

    dataSourceInfoService.updateBatchDataSourceEnv(list);
  }

  @Test
  void testQueryDataSourceInfo() {
    List<DataSource> dataSourceList = new ArrayList<>();
    DataSource dataSource = new DataSource();
    dataSource.setId(1l);
    dataSource.setCreateUser("test");
    dataSourceList.add(dataSource);
    Mockito.when(dataSourceDao.selectByIds(Arrays.asList(1l))).thenReturn(dataSourceList);

    List<DataSource> list = dataSourceInfoService.queryDataSourceInfo(Arrays.asList(1l));
    assertTrue(CollectionUtils.isNotEmpty(list));
    assertEquals(dataSourceList.get(0).getId(), 1l);
  }
}
