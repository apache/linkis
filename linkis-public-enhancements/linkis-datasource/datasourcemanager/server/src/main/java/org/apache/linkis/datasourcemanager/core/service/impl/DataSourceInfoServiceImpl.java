/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.datasourcemanager.core.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceParamKeyDao;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceTypeEnvDao;
import org.apache.linkis.datasourcemanager.core.formdata.FormStreamContent;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceDao;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceEnvDao;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.core.service.BmlAppService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceEnvVo;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class DataSourceInfoServiceImpl implements DataSourceInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceInfoService.class);
    @Autowired
    private BmlAppService bmlAppService;

    @Autowired
    private DataSourceTypeEnvDao dataSourceTypeEnvDao;

    @Autowired
    private DataSourceDao dataSourceDao;

    @Autowired
    private DataSourceEnvDao dataSourceEnvDao;

    @Autowired
    private DataSourceParamKeyDao dataSourceParamKeyDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDataSourceInfo(DataSource dataSource) throws ErrorException {
        storeConnectParams(dataSource.getCreateUser(), dataSource.getKeyDefinitions(),
                dataSource.getConnectParams(), parameter ->{
                dataSource.setParameter(parameter);
                //Save information into database
                dataSourceDao.insertOne(dataSource);
        });
    }

    @Override
    public void addEnvParamsToDataSource(Long dataSourceEnvId, DataSource dataSource) {
        DataSourceEnv dataSourceEnv = dataSourceEnvDao.selectOneDetail(dataSourceEnvId);
        if(null != dataSourceEnv){
            Map<String, Object> envParamMap = dataSourceEnv.getConnectParams();
            envParamMap.putAll(dataSource.getConnectParams());
            dataSource.setConnectParams(envParamMap);
        }
    }

    @Override
    public DataSource getDataSourceInfo(Long dataSourceId, String createSystem) {
         return dataSourceDao.selectOneDetail(dataSourceId, createSystem);
    }

    @Override
    public DataSource getDataSourceInfoBrief(Long dataSourceId, String createSystem) {
        return dataSourceDao.selectOne(dataSourceId, createSystem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long removeDataSourceInfo(Long dataSourceId, String createSystem) {
        DataSource dataSource = dataSourceDao.selectOne(dataSourceId, createSystem);
        if(null != dataSource){
            //First to delete record in db
            int affect = dataSourceDao.removeOne(dataSourceId, createSystem);
            if(affect > 0){
                //Remove resource
                Map<String, Object> connectParams = dataSource.getConnectParams();
                List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceParamKeyDao
                        .listByDataSourceType(dataSource.getDataSourceTypeId());
                keyDefinitions.forEach(keyDefinition -> {
                    if(keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.FILE
                            && keyDefinition.getScope() != DataSourceParamKeyDefinition.Scope.ENV
                            && connectParams.containsKey(keyDefinition.getKey())){
                        try {
                            //Proxy creator to delete resource
                            bmlAppService.clientRemoveResource(dataSource.getCreateUser(), String
                                    .valueOf(connectParams.get(keyDefinition.getKey())));
                        }catch(Exception e){
                            //Ignore remove error
                        }
                    }
                });
                return dataSourceId;
            }
        }
        return -1L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataSourceInfo(DataSource updatedOne, DataSource storedOne) throws ErrorException{
        updateConnectParams(updatedOne.getCreateUser(), updatedOne.getKeyDefinitions(),
                updatedOne.getConnectParams(), storedOne.getConnectParams(),
                parameter -> {
            updatedOne.setParameter(parameter);
            //Save information into database
            dataSourceDao.updateOne(updatedOne);
        });
    }

    @Override
    public List<DataSource> queryDataSourceInfoPage(DataSourceVo dataSourceVo) {
        PageHelper.startPage(dataSourceVo.getCurrentPage(), dataSourceVo.getPageSize());
        List<DataSource> queryList = dataSourceDao.selectByPageVo(dataSourceVo);
        PageInfo<DataSource> pageInfo = new PageInfo<>(queryList);
        return pageInfo.getList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDataSourceEnv(DataSourceEnv dataSourceEnv) throws ErrorException {
        storeConnectParams(dataSourceEnv.getCreateUser(), dataSourceEnv.getKeyDefinitions(),
                dataSourceEnv.getConnectParams(),
                parameter ->{
            dataSourceEnv.setParameter(parameter);
            //Save environment into database
            dataSourceEnvDao.insertOne(dataSourceEnv);
            //Store relation
            dataSourceTypeEnvDao.insertRelation(dataSourceEnv.getDataSourceTypeId(), dataSourceEnv.getId());
        });
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
       if(null != dataSourceEnv){
            //First to delete record in db
           int affect = dataSourceEnvDao.removeOne(envId);
           if(affect > 0){
               //Remove relations
               dataSourceTypeEnvDao.removeRelationsByEnvId(envId);
               //Remove resource
               Map<String, Object> connectParams = dataSourceEnv.getConnectParams();
               List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceParamKeyDao
                       .listByDataSourceTypeAndScope(dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
               keyDefinitions.forEach(keyDefinition -> {
                    if(keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.FILE
                            && connectParams.containsKey(keyDefinition.getKey())){
                        try{
                            //Proxy creator to delete resource
                            bmlAppService.clientRemoveResource(dataSourceEnv.getCreateUser(), String
                                .valueOf(connectParams.get(keyDefinition.getKey())));
                        }catch(Exception e){
                            //Ignore remove error
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
    public void updateDataSourceEnv(DataSourceEnv updatedOne, DataSourceEnv storedOne) throws ErrorException {
        updateConnectParams(updatedOne.getCreateUser(), updatedOne.getKeyDefinitions(),
                updatedOne.getConnectParams(), storedOne.getConnectParams(),
                parameter ->{
            updatedOne.setParameter(parameter);
            //Update environment into database
            dataSourceEnvDao.updateOne(updatedOne);
            if(!updatedOne.getDataSourceTypeId().equals(storedOne.getDataSourceTypeId())){
                //Remove old relation and add new relation
                dataSourceTypeEnvDao.removeRelationsByEnvId(updatedOne.getId());
                dataSourceTypeEnvDao.insertRelation(updatedOne.getDataSourceTypeId(), updatedOne.getId());
            }
        });
    }

    @Override
    public List<DataSourceEnv> queryDataSourceEnvPage(DataSourceEnvVo dataSourceEnvVo) {
        PageHelper.startPage(dataSourceEnvVo.getCurrentPage(), dataSourceEnvVo.getPageSize());
        List<DataSourceEnv> queryList = dataSourceEnvDao.selectByPageVo(dataSourceEnvVo);
        PageInfo<DataSourceEnv> pageInfo = new PageInfo<>(queryList);
        return pageInfo.getList();
    }

    /**
     *
     * @param userName
     * @param keyDefinitionList
     * @param updatedParams
     * @param storedParams
     * @param parameterCallback
     * @throws ErrorException
     */
    private void updateConnectParams(String userName, List<DataSourceParamKeyDefinition> keyDefinitionList,
                                     Map<String, Object> updatedParams, Map<String, Object> storedParams,
                                     Consumer<String> parameterCallback) throws ErrorException {
        List<String> definedKeyNames = keyDefinitionList.stream().map(DataSourceParamKeyDefinition::getKey)
                .collect(Collectors.toList());
        List<String> uploadedResources = new ArrayList<>();
        try {
            updatedParams.entrySet().removeIf(entry -> {
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
            //Found the duplicate File
            List<String> duplicateResources = new ArrayList<>();
            keyDefinitionList.forEach( definedKey-> {
               if(definedKey.getValueType() == DataSourceParamKeyDefinition.ValueType.FILE
                    && storedParams.containsKey(definedKey.getKey())){
                    duplicateResources.add(String.valueOf(storedParams.get(definedKey.getKey())));
                }
            });
            parameterCallback.accept(Json.toJson(updatedParams, null));
            deleteResources(userName, duplicateResources);
        }catch(Exception e){
            deleteResources(userName, uploadedResources);
            if(e.getCause() instanceof  ErrorException){
                throw (ErrorException)e.getCause();
            }
            throw e;
        }
    }

    /**
     * Upload the form stream context in connect parameters,
     * and serialize parameters
     * @param keyDefinitionList
     * @param connectParams
     * @param parameterCallback
     */
    private void storeConnectParams(String userName, List<DataSourceParamKeyDefinition> keyDefinitionList,
                                    Map<String, Object> connectParams,
                                    Consumer<String> parameterCallback) throws ErrorException{
        List<String> definedKeyNames = keyDefinitionList.stream().map(DataSourceParamKeyDefinition::getKey)
                .collect(Collectors.toList());
        List<String> uploadedResources = new ArrayList<>();
        try{
            connectParams.entrySet().removeIf(entry -> {
                if(!definedKeyNames.contains(entry.getKey())){
                    return true;
                }
                Object paramValue = entry.getValue();
                //Upload stream resource in connection params
                if (paramValue instanceof FormStreamContent) {
                    String resourceId = uploadFormStream(userName, (FormStreamContent)paramValue, "");
                    if(null == resourceId) {
                        return true;
                    }
                    uploadedResources.add(resourceId);
                    entry.setValue(resourceId);
                }
                return false;
            });
            parameterCallback.accept(Json.toJson(connectParams, null));
        }catch(Exception e){
            deleteResources(userName, uploadedResources);
            if(e.getCause() instanceof ErrorException){
                throw (ErrorException)e.getCause();
            }
            throw e;
        }
    }

    /**
     * Upload form stream
     * @param userName user name
     * @param streamContent stream content
     * @param resourceId resource id
     * @return resource id or version tab
     */
    private String uploadFormStream(String userName, FormStreamContent streamContent, String resourceId){
        String fileName = streamContent.getFileName();
        InputStream inputStream = streamContent.getStream();
        if (null != inputStream){
            //Proxy creator to upload resource
            try{
                return StringUtils.isBlank(resourceId)? bmlAppService.clientUploadResource(userName, fileName, inputStream)
                        : bmlAppService.clientUpdateResource(userName, resourceId, inputStream);
            }catch(Exception e){
                //Wrap with runtime exception
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Delete uploaded resources
     * @param userName user name
     * @param uploadedResources resource id list
     */
    private void deleteResources(String userName, List<String> uploadedResources){
        if(!uploadedResources.isEmpty()){
            //Remove duplicated resource
            for (String resourceId : uploadedResources) {
                try{
                    //Proxy to delete resource
                    bmlAppService.clientRemoveResource(userName, resourceId);
                }catch(Exception ie){
                    //ignore
                }
            }
        }
    }
}
