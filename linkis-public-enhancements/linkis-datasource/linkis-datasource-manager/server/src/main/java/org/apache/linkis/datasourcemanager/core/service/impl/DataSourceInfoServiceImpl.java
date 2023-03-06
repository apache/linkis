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

package org.apache.linkis.datasourcemanager.core.service.impl;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DatasourceVersion;
import org.apache.linkis.datasourcemanager.common.exception.JsonErrorException;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.datasourcemanager.core.dao.*;
import org.apache.linkis.datasourcemanager.core.formdata.FormStreamContent;
import org.apache.linkis.datasourcemanager.core.service.BmlAppService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.service.hooks.DataSourceParamsHook;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceEnvVo;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DataSourceInfoServiceImpl implements DataSourceInfoService {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceInfoService.class);
  @Autowired private BmlAppService bmlAppService;

  @Autowired private DataSourceDao dataSourceDao;

  @Autowired private DataSourceEnvDao dataSourceEnvDao;

  @Autowired private DataSourceParamKeyDao dataSourceParamKeyDao;

  @Autowired private DataSourceVersionDao dataSourceVersionDao;

  @Autowired private DataSourceRelateService dataSourceRelateService;

  @Autowired private List<DataSourceParamsHook> dataSourceParamsHooks = new ArrayList<>();

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveDataSourceInfo(DataSource dataSource) throws ErrorException {
    dataSourceDao.insertOne(dataSource);
  }

  /**
   * The DataSource parameter takes precedence over the environment parameter
   *
   * @param dataSourceEnvId data source environment
   * @param dataSource data source
   */
  @Override
  public void addEnvParamsToDataSource(Long dataSourceEnvId, DataSource dataSource) {
    DataSourceEnv dataSourceEnv = dataSourceEnvDao.selectOneDetail(dataSourceEnvId);
    if (null != dataSourceEnv) {
      Map<String, Object> envParamMap = dataSourceEnv.getConnectParams();
      envParamMap.putAll(dataSource.getConnectParams());
      dataSource.setConnectParams(envParamMap);
    }
  }

  @Override
  public DataSource getDataSourceInfo(Long dataSourceId) {
    DataSource dataSource = dataSourceDao.selectOneDetail(dataSourceId);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, dataSource.getVersionId());
    }
    return dataSource;
  }

  @Override
  public DataSource getDataSourceInfo(String dataSourceName) {
    DataSource dataSource = dataSourceDao.selectOneDetailByName(dataSourceName);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, dataSource.getVersionId());
    }
    return dataSource;
  }

  @Override
  public DataSource getDataSourcePublishInfo(String dataSourceName) {
    DataSource dataSource = dataSourceDao.selectOneDetailByName(dataSourceName);
    if (Objects.nonNull(dataSource)) {
      Long publishedVersionId = dataSource.getPublishedVersionId();
      if (publishedVersionId == null) {
        LOG.warn("Datasource name:{} is not published. ", dataSourceName);
      } else {
        String parameter =
            dataSourceVersionDao.selectOneVersion(dataSource.getId(), publishedVersionId);
        dataSource.setParameter(parameter);
      }
    }
    return dataSource;
  }

  @Override
  public DataSource getDataSourceInfo(Long dataSourceId, Long version) {
    DataSource dataSource = dataSourceDao.selectOneDetail(dataSourceId);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, version);
    }
    return dataSource;
  }

  /**
   * get datasource info for connect for published version, if there is a dependency environment,
   * merge datasource parameter and environment parameter.
   */
  @Override
  public DataSource getDataSourceInfoForConnect(Long dataSourceId) {
    DataSource dataSource = dataSourceDao.selectOneDetail(dataSourceId);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, dataSource.getPublishedVersionId());
      mergeEnvParamsByDefault(dataSource);
    }
    return dataSource;
  }

  @Override
  public DataSource getDataSourceInfoForConnect(String dataSourceName) {
    DataSource dataSource = dataSourceDao.selectOneDetailByName(dataSourceName);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, dataSource.getPublishedVersionId());
      mergeEnvParamsByDefault(dataSource);
    }
    return dataSource;
  }

  @Override
  public DataSource getDataSourceInfoForConnect(String dataSourceName, String envId) {
    DataSource dataSource = dataSourceDao.selectOneDetailByName(dataSourceName);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, dataSource.getPublishedVersionId());
      mergeEnvParamsByEnvId(dataSource, envId);
    }
    return dataSource;
  }

  private void mergeEnvParamsByEnvId(DataSource dataSource, String specialEnvId) {
    List<String> envIdList = getEnvIdsFrom(dataSource);
    if (envIdList.contains(specialEnvId)) {
      addEnvParamsToDataSource(Long.valueOf(specialEnvId), dataSource);
    }
  }

  private void mergeEnvParamsByDefault(DataSource dataSource) {
    List<String> envIdList = getEnvIdsFrom(dataSource);
    if (CollectionUtils.isNotEmpty(envIdList)) {
      addEnvParamsToDataSource(Long.valueOf(envIdList.get(0)), dataSource);
    }
  }

  private List<String> getEnvIdsFrom(DataSource dataSource) {
    Map<String, Object> connectParams = dataSource.getConnectParams();
    if (connectParams.containsKey("envId")) {
      String envId = connectParams.get("envId").toString();
      // remove envId for connect
      connectParams.remove("envId");
      return Arrays.asList(envId);
    } else if (connectParams.containsKey("envIdArray")) {
      //    if exists multi env
      Object envIdArray = connectParams.get("envIdArray");
      if (envIdArray instanceof List) {
        List<String> envIdList = (List<String>) envIdArray;
        // remove envIdArray for connect
        connectParams.remove("envIdArray");
        return envIdList;
      }
    }
    return Collections.emptyList();
  }

  /**
   * get datasource info for connect, if there is a dependency environment, merge datasource
   * parameter and environment parameter.
   *
   * @param dataSourceId
   * @param version
   * @return
   */
  @Override
  public DataSource getDataSourceInfoForConnect(Long dataSourceId, Long version) {
    DataSource dataSource = dataSourceDao.selectOneDetail(dataSourceId);
    if (Objects.nonNull(dataSource)) {
      mergeVersionParams(dataSource, version);
      mergeEnvParamsByDefault(dataSource);
    }
    return dataSource;
  }

  @Override
  public boolean existDataSource(String dataSourceName) {
    if (StringUtils.isNotBlank(dataSourceName)) {
      DataSource dataSource = dataSourceDao.selectOneByName(dataSourceName);
      return Objects.nonNull(dataSource);
    }
    return false;
  }

  @Override
  public boolean existDataSourceEnv(String dataSourceEnvName) {
    if (StringUtils.isNotBlank(dataSourceEnvName)) {
      DataSourceEnv dataSourceEnv = dataSourceEnvDao.selectOneByName(dataSourceEnvName);
      return Objects.nonNull(dataSourceEnv);
    }
    return false;
  }

  @Override
  public DataSource getDataSourceInfoBrief(Long dataSourceId) {
    return dataSourceDao.selectOne(dataSourceId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long removeDataSourceInfo(Long dataSourceId, String createSystem) {
    DataSource dataSource = dataSourceDao.selectOne(dataSourceId);
    if (null != dataSource) {
      // First to delete record in db
      int affect = dataSourceDao.removeOne(dataSourceId);
      if (affect > 0) {
        // delete parameter version
        int versionNum = dataSourceVersionDao.removeFromDataSourceId(dataSourceId);
        // TODO throws Exception
        return dataSourceId;
      }
    }
    return -1L;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateDataSourceInfo(DataSource updatedOne) {
    dataSourceDao.updateOne(updatedOne);
  }

  @Override
  public PageInfo<DataSource> queryDataSourceInfoPage(DataSourceVo dataSourceVo) {
    PageHelper.startPage(dataSourceVo.getCurrentPage(), dataSourceVo.getPageSize());
    try {
      List<DataSource> queryList = dataSourceDao.selectByPageVo(dataSourceVo);
      return new PageInfo<>(queryList);
    } finally {
      PageHelper.clearPage();
    }
  }

  @Override
  public List<DataSource> queryDataSourceInfo(List ids) {
    List<DataSource> queryList = dataSourceDao.selectByIds(ids);
    return queryList;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveDataSourceEnv(DataSourceEnv dataSourceEnv) throws ErrorException {
    storeConnectParams(
        dataSourceEnv.getCreateUser(),
        dataSourceEnv.getKeyDefinitions(),
        dataSourceEnv.getConnectParams(),
        parameter -> {
          dataSourceEnv.setParameter(parameter);
          // Save environment into database
          dataSourceEnvDao.insertOne(dataSourceEnv);
        });
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveBatchDataSourceEnv(List<DataSourceEnv> dataSourceEnvList) throws ErrorException {
    for (DataSourceEnv dataSourceEnv : dataSourceEnvList) {
      storeConnectParams(
          dataSourceEnv.getCreateUser(),
          dataSourceEnv.getKeyDefinitions(),
          dataSourceEnv.getConnectParams(),
          parameter -> {
            dataSourceEnv.setParameter(parameter);
            // Save environment into database
            dataSourceEnvDao.insertOne(dataSourceEnv);
          });
    }
  }

  @Override
  public List<DataSourceEnv> listDataSourceEnvByType(Long dataSourceTypeId) {
    return dataSourceEnvDao.listByTypeId(dataSourceTypeId);
  }

  @Override
  public DataSourceEnv getDataSourceEnv(Long envId) {
    return dataSourceEnvDao.selectOneDetail(envId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long removeDataSourceEnv(Long envId) {
    DataSourceEnv dataSourceEnv = dataSourceEnvDao.selectOneDetail(envId);
    if (null != dataSourceEnv) {
      // First to delete record in db
      int affect = dataSourceEnvDao.removeOne(envId);
      if (affect > 0) {
        // Remove resource
        Map<String, Object> connectParams = dataSourceEnv.getConnectParams();
        List<DataSourceParamKeyDefinition> keyDefinitions =
            dataSourceParamKeyDao.listByDataSourceTypeAndScope(
                dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
        // TODO throws ERROR Exception
        keyDefinitions.forEach(
            keyDefinition -> {
              if (keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.FILE
                  && connectParams.containsKey(keyDefinition.getKey())) {
                try {
                  // Proxy creator to delete resource
                  bmlAppService.clientRemoveResource(
                      dataSourceEnv.getCreateUser(),
                      String.valueOf(connectParams.get(keyDefinition.getKey())));
                } catch (Exception e) {
                  // Ignore remove error
                  // TODO LOG and throws LinkisRuntimeException
                }
              }
            });
        return envId;
      }
    }
    return -1L;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateDataSourceEnv(DataSourceEnv updatedOne, DataSourceEnv storedOne)
      throws ErrorException {
    updateConnectParams(
        updatedOne.getCreateUser(),
        updatedOne.getKeyDefinitions(),
        updatedOne.getConnectParams(),
        storedOne.getConnectParams(),
        parameter -> {
          updatedOne.setParameter(parameter);
          // Update environment into database
          dataSourceEnvDao.updateOne(updatedOne);
        });
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateBatchDataSourceEnv(List<DataSourceEnv> dataSourceEnvList)
      throws ErrorException {
    for (DataSourceEnv updatedOne : dataSourceEnvList) {
      DataSourceEnv storedOne = getDataSourceEnv(updatedOne.getId());
      updateConnectParams(
          updatedOne.getCreateUser(),
          updatedOne.getKeyDefinitions(),
          updatedOne.getConnectParams(),
          storedOne.getConnectParams(),
          parameter -> {
            updatedOne.setParameter(parameter);
            // Update environment into database
            dataSourceEnvDao.updateOne(updatedOne);
          });
    }
  }

  @Override
  public List<DataSourceEnv> queryDataSourceEnvPage(DataSourceEnvVo dataSourceEnvVo) {
    PageHelper.startPage(dataSourceEnvVo.getCurrentPage(), dataSourceEnvVo.getPageSize());
    List<DataSourceEnv> queryList = dataSourceEnvDao.selectByPageVo(dataSourceEnvVo);
    PageInfo<DataSourceEnv> pageInfo = new PageInfo<>(queryList);
    return pageInfo.getList();
  }

  /**
   * expire data source
   *
   * @param dataSourceId
   * @return
   */
  @Override
  public Long expireDataSource(Long dataSourceId) {
    DataSource dataSource = dataSourceDao.selectOne(dataSourceId);
    if (null != dataSource) {
      // First to delete record in db
      int affect = dataSourceDao.expireOne(dataSourceId);
      if (affect > 0) {
        return dataSourceId;
      }
    }
    return -1L;
  }

  /**
   * publish datasource by id set published_version_id to versionId;
   *
   * @param dataSourceId
   * @return
   */
  @Override
  public int publishByDataSourceId(Long dataSourceId, Long versionId) {
    Long latestVersion = dataSourceVersionDao.getLatestVersion(dataSourceId);
    if (versionId > latestVersion) {
      // can't publish a version that does not exist
      return 0;
    }
    return dataSourceDao.setPublishedVersionId(dataSourceId, versionId);
  }

  /**
   * insert a datasource parameter, return new version, and update current versionId of datasource
   *
   * @param keyDefinitionList
   * @param datasourceId
   * @param connectParams
   * @param username
   * @param comment
   * @return
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public long insertDataSourceParameter(
      List<DataSourceParamKeyDefinition> keyDefinitionList,
      Long datasourceId,
      Map<String, Object> connectParams,
      String username,
      String comment)
      throws ErrorException {

    DatasourceVersion datasourceVersion = new DatasourceVersion();
    datasourceVersion.setCreateUser(username);
    datasourceVersion.setDatasourceId(datasourceId);
    if (null != comment) {
      datasourceVersion.setComment(comment);
    }

    // 1. set version + 1
    Long latestVersion = dataSourceVersionDao.getLatestVersion(datasourceId);
    // TODO NullPoint latestVersion
    long newVersionId = latestVersion + 1;
    datasourceVersion.setVersionId(newVersionId);

    // 2. set parameter, (check connectParams and remove if not in definedKeyNames);
    List<String> definedKeyNames =
        keyDefinitionList.stream()
            .map(DataSourceParamKeyDefinition::getKey)
            .collect(Collectors.toList());
    // Accept the other parameters
    //        connectParams.entrySet().removeIf(entry ->
    // !definedKeyNames.contains(entry.getKey()));
    // 2.1 Invoke the hooks
    for (DataSourceParamsHook hook : dataSourceParamsHooks) {
      hook.beforePersist(connectParams, keyDefinitionList);
    }
    datasourceVersion.setParameter(Json.toJson(connectParams, null));

    // 3. insert to dataSourceVersionDao
    dataSourceVersionDao.insertOne(datasourceVersion);

    // 4. update version id for dataSourceDao
    dataSourceDao.updateVersionId(datasourceId, newVersionId);

    return newVersionId;
  }

  /**
   * get datasource version list
   *
   * @param datasourceId
   * @return
   */
  @Override
  public List<DatasourceVersion> getVersionList(Long datasourceId) {
    List<DatasourceVersion> versionList =
        dataSourceVersionDao.getVersionsFromDatasourceId(datasourceId);
    return versionList;
  }

  /**
   * @param userName
   * @param keyDefinitionList
   * @param updatedParams
   * @param storedParams
   * @param parameterCallback
   * @throws ErrorException
   */
  private void updateConnectParams(
      String userName,
      List<DataSourceParamKeyDefinition> keyDefinitionList,
      Map<String, Object> updatedParams,
      Map<String, Object> storedParams,
      Consumer<String> parameterCallback)
      throws ErrorException {
    List<String> definedKeyNames =
        keyDefinitionList.stream()
            .map(DataSourceParamKeyDefinition::getKey)
            .collect(Collectors.toList());
    List<String> uploadedResources = new ArrayList<>();
    try {
      updatedParams
          .entrySet()
          .removeIf(
              entry -> {
                if (!definedKeyNames.contains(entry.getKey())) {
                  return true;
                }
                Object paramValue = entry.getValue();
                if (paramValue instanceof FormStreamContent) {
                  String resourceId = String.valueOf(storedParams.getOrDefault(entry.getKey(), ""));
                  if (StringUtils.isNotBlank(resourceId)) {
                    uploadFormStream(userName, (FormStreamContent) paramValue, resourceId);
                  } else {
                    resourceId = uploadFormStream(userName, (FormStreamContent) paramValue, "");
                  }
                  if (null == resourceId) {
                    return true;
                  }
                  uploadedResources.add(resourceId);
                  entry.setValue(resourceId);
                }
                storedParams.remove(entry.getKey());
                return false;
              });
      // Found the duplicate File
      List<String> duplicateResources = new ArrayList<>();
      keyDefinitionList.forEach(
          definedKey -> {
            if (definedKey.getValueType() == DataSourceParamKeyDefinition.ValueType.FILE
                && storedParams.containsKey(definedKey.getKey())) {
              duplicateResources.add(String.valueOf(storedParams.get(definedKey.getKey())));
            }
          });
      for (DataSourceParamsHook hook : dataSourceParamsHooks) {
        hook.beforePersist(updatedParams, keyDefinitionList);
      }
      parameterCallback.accept(Json.toJson(updatedParams, null));
      deleteResources(userName, duplicateResources);
    } catch (Exception e) {
      deleteResources(userName, uploadedResources);
      if (e.getCause() instanceof ErrorException) {
        throw (ErrorException) e.getCause();
      }
      throw e;
    }
  }

  /**
   * Upload the form stream context in connect parameters, and serialize parameters
   *
   * @param keyDefinitionList
   * @param connectParams
   * @param parameterCallback
   */
  private void storeConnectParams(
      String userName,
      List<DataSourceParamKeyDefinition> keyDefinitionList,
      Map<String, Object> connectParams,
      Consumer<String> parameterCallback)
      throws ErrorException {
    List<String> definedKeyNames =
        keyDefinitionList.stream()
            .map(DataSourceParamKeyDefinition::getKey)
            .collect(Collectors.toList());
    List<String> uploadedResources = new ArrayList<>();
    try {
      connectParams
          .entrySet()
          .removeIf(
              entry -> {
                if (!definedKeyNames.contains(entry.getKey())) {
                  return true;
                }
                Object paramValue = entry.getValue();
                // Upload stream resource in connection params
                if (paramValue instanceof FormStreamContent) {
                  String resourceId =
                      uploadFormStream(userName, (FormStreamContent) paramValue, "");
                  if (null == resourceId) {
                    return true;
                  }
                  uploadedResources.add(resourceId);
                  entry.setValue(resourceId);
                }
                return false;
              });
      for (DataSourceParamsHook hook : dataSourceParamsHooks) {
        hook.beforePersist(connectParams, keyDefinitionList);
      }
      parameterCallback.accept(Json.toJson(connectParams, null));
    } catch (Exception e) {
      deleteResources(userName, uploadedResources);
      if (e.getCause() instanceof ErrorException) {
        throw (ErrorException) e.getCause();
      }
      // TODO wrapped Exception
      throw e;
    }
  }

  /**
   * Upload form stream
   *
   * @param userName user name
   * @param streamContent stream content
   * @param resourceId resource id
   * @return resource id or version tab
   */
  private String uploadFormStream(
      String userName, FormStreamContent streamContent, String resourceId) {
    String fileName = streamContent.getFileName();
    InputStream inputStream = streamContent.getStream();
    if (null != inputStream) {
      // Proxy creator to upload resource
      try {
        return StringUtils.isBlank(resourceId)
            ? bmlAppService.clientUploadResource(userName, fileName, inputStream)
            : bmlAppService.clientUpdateResource(userName, resourceId, inputStream);
      } catch (Exception e) {
        // Wrap with runtime exception
        //                throw new RuntimeException(e);
        // TODO defined Exception
      }
    }
    return null;
  }

  /**
   * Delete uploaded resources
   *
   * @param userName user name
   * @param uploadedResources resource id list
   */
  private void deleteResources(String userName, List<String> uploadedResources) {
    if (!uploadedResources.isEmpty()) {
      // Remove duplicated resource
      for (String resourceId : uploadedResources) {
        try {
          // Proxy to delete resource
          bmlAppService.clientRemoveResource(userName, resourceId);
        } catch (Exception ie) {
          // ignore
          // TODO throws RPC Exception
        }
      }
    }
  }

  private void mergeVersionParams(DataSource dataSource, Long version) {
    if (Objects.isNull(version)) {
      return;
    }
    Map<String, Object> connectParams = dataSource.getConnectParams();
    String versionParameter = dataSourceVersionDao.selectOneVersion(dataSource.getId(), version);
    if (StringUtils.isNotBlank(versionParameter)) {
      try {
        connectParams.putAll(Objects.requireNonNull(Json.fromJson(versionParameter, Map.class)));
      } catch (JsonErrorException e) {
        LOG.warn("Parameter is not json string");
      }
    }
  }
}
