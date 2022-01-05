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
 
package org.apache.linkis.datasourcemanager.core.restful;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;
import org.apache.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import org.apache.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping(value = "/data_source",produces = {"application/json"})
public class DataSourceCoreRestfulApi {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceCoreRestfulApi.class);
    @Autowired
    private DataSourceInfoService dataSourceInfoService;

    @Autowired
    private DataSourceRelateService dataSourceRelateService;

    @Autowired
    private ParameterValidator parameterValidator;

    @Autowired
    private Validator beanValidator;

    private MultiPartFormDataTransformer formDataTransformer;


    @PostConstruct
    public void initRestful(){
        this.formDataTransformer = FormDataTransformerFactory.buildCustom();
    }

    @RequestMapping(value = "/info/json" ,method = RequestMethod.POST)
    public Message insertJsonInfo(DataSource dataSource,  HttpServletRequest req) throws ErrorException {
        String userName = SecurityFilter.getLoginUsername(req);
        //Bean validation
        Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
        if(result.size() > 0){
            throw new ConstraintViolationException(result);
        }
        dataSource.setCreateUser(userName);
        insertDataSourceConfig(dataSource);
        return Message.ok().data("insert_id", dataSource.getId());
    }

    @RequestMapping(value = "/info/form",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,method = RequestMethod.POST)
    public Message insertFormConfig(FormDataMultiPart multiPartForm, HttpServletRequest request) throws ErrorException {
            if(null != multiPartForm) {
                String userName = SecurityFilter.getLoginUsername(request);
                DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
                dataSource.setCreateUser(userName);
                insertDataSourceConfig(dataSource);
                return Message.ok().data("insert_id", dataSource.getId());
            }
            return Message.error("Empty request");
    }

    @RequestMapping(value = "/info/{data_source_id}",method = RequestMethod.GET)
    public Message getInfoByDataSourceId(@RequestParam(value = "system",required = false)String createSystem,
                                            @PathVariable("data_source_id")Long dataSourceId,
                                            HttpServletRequest request){
            if(StringUtils.isBlank(createSystem)){
                return Message.error("'create system' is missing[缺少系统名]");
            }
            DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId, createSystem);
            //Decrypt
            if(null != dataSource) {
                RestfulApiHelper.decryptPasswordKey(dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId())
                        , dataSource.getConnectParams());
            }
            return Message.ok().data("info", dataSource);
    }


    @RequestMapping(value = "/info/{data_source_id}",method = RequestMethod.DELETE)
    public Message removeDataSource(@RequestParam(value = "system",required = false)String createSystem,
                                     @PathVariable("data_source_id")Long dataSourceId){
            if(StringUtils.isBlank(createSystem)){
                return Message.error("'create system' is missing[缺少系统名]");
            }
            Long removeId = dataSourceInfoService.removeDataSourceInfo(dataSourceId, createSystem);
            if(removeId < 0){
                return Message.error("Fail to remove data source[删除数据源信息失败], [id:" + dataSourceId +"]");
            }
            return Message.ok().data("remove_id", removeId);
    }

    @RequestMapping(value = "/info/{data_source_id}/json",method = RequestMethod.PUT)
    public Message updateDataSourceInJson(DataSource dataSource,
                                           @PathVariable("data_source_id")Long dataSourceId,
                                            HttpServletRequest req) throws ErrorException {
            String userName = SecurityFilter.getLoginUsername(req);
            //Bean validation
            Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
            if(result.size() > 0){
                throw new ConstraintViolationException(result);
            }
            dataSource.setId(dataSourceId);
            dataSource.setModifyUser(userName);
            dataSource.setModifyTime(Calendar.getInstance().getTime());
            DataSource storedDataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId, dataSource.getCreateSystem());
            if(null == storedDataSource){
                return Message.error("Fail to update data source[更新数据源失败], " + "[Please check the id:'"
                        + dataSourceId +"' and create system: '"+dataSource.getCreateSystem()+" is correct ']");
            }
            dataSource.setCreateUser(storedDataSource.getCreateUser());
            updateDataSourceConfig(dataSource, storedDataSource);
            return Message.ok().data("update_id", dataSourceId);
    }

    @RequestMapping(value = "/info/{data_source_id}/form",method = RequestMethod.PUT,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Message updateDataSourceInForm(FormDataMultiPart multiPartForm,
                                           @PathVariable("data_source_id") Long dataSourceId,
                                           HttpServletRequest req) throws ErrorException {
            String userName = SecurityFilter.getLoginUsername(req);
            DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
            dataSource.setId(dataSourceId);
            dataSource.setModifyUser(userName);
            dataSource.setModifyTime(Calendar.getInstance().getTime());
            DataSource storedDataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId, dataSource.getCreateSystem());
            if(null == storedDataSource){
                return Message.error("Fail to update data source[更新数据源失败], " +
                        "[Please check the id:'" + dataSourceId +"' and create system: '"+dataSource.getCreateSystem()+" is correct ']");
            }
            dataSource.setCreateUser(storedDataSource.getCreateUser());
            updateDataSourceConfig(dataSource, storedDataSource);
            return Message.ok().data("update_id", dataSourceId);
    }

    @RequestMapping(value = "/info",method = RequestMethod.GET)
    public Message queryDataSource(@RequestParam(value = "system",required = false)String createSystem,
                                    @RequestParam(value = "name",required = false)String dataSourceName,
                                    @RequestParam(value = "typeId",required = false)Long dataSourceTypeId,
                                    @RequestParam(value = "identifies",required = false)String identifies,
                                    @RequestParam(value = "currentPage",required = false)Integer currentPage,
                                    @RequestParam(value = "pageSize",required = false)Integer pageSize){
            if(StringUtils.isBlank(createSystem)){
                return Message.error("'create system' is missing[缺少系统名]");
            }
            DataSourceVo dataSourceVo = new DataSourceVo(dataSourceName, dataSourceTypeId,
                    identifies, createSystem);
            dataSourceVo.setCurrentPage(null != currentPage ? currentPage : 1);
            dataSourceVo.setPageSize(null != pageSize? pageSize : 10);
            List<DataSource> queryList = dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
            return Message.ok().data("query_list", queryList);
    }

    @RequestMapping(value = "/key_define/type/{type_id}",method = RequestMethod.GET)
    public Message getKeyDefinitionsByType(@PathVariable("type_id") Long dataSourceTypeId){
            List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceRelateService.getKeyDefinitionsByType(dataSourceTypeId);
            return Message.ok().data("key_define", keyDefinitions);
    }

    @RequestMapping(value = "/key_define/type/{type_id}/{scope}",method = RequestMethod.GET)
    public Message getKeyDefinitionsByTypeAndScope(@PathVariable("type_id") Long dataSourceTypeId,
                                                    @PathVariable("scope") String scopeValue){
            DataSourceParamKeyDefinition.Scope scope = DataSourceParamKeyDefinition.Scope.valueOf(scopeValue.toUpperCase());
            List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceRelateService
                    .getKeyDefinitionsByType(dataSourceTypeId, scope);
            return Message.ok().data("key_define", keyDefinitions);
    }

    @RequestMapping(value = "/type/all",method = RequestMethod.GET)
    public Message getAllDataSourceTypes(){
            List<DataSourceType> dataSourceTypes = dataSourceRelateService.getAllDataSourceTypes();
            return Message.ok().data("type_list", dataSourceTypes);
    }

    /**
     * Inner method to insert data source
     * @param dataSource data source entity
     * @throws ErrorException
     */
    private void insertDataSourceConfig(DataSource dataSource) throws ErrorException {
        if(null != dataSource.getDataSourceEnvId()){
            //Merge parameters
            dataSourceInfoService.addEnvParamsToDataSource(dataSource.getDataSourceEnvId(), dataSource);
        }
        //Validate connect parameters
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
        dataSource.setKeyDefinitions(keyDefinitionList);
        Map<String,Object> connectParams = dataSource.getConnectParams();
        parameterValidator.validate(keyDefinitionList, connectParams);
        //Encrypt password value type
        RestfulApiHelper.encryptPasswordKey(keyDefinitionList, connectParams);
        dataSourceInfoService.saveDataSourceInfo(dataSource);
    }

    /**
     * Inner method to update data source
     * @param updatedOne new entity
     * @param storedOne old entity
     * @throws ErrorException
     */
    private void updateDataSourceConfig(DataSource updatedOne, DataSource storedOne) throws ErrorException{
        if(null != updatedOne.getDataSourceEnvId()){
            //Merge parameters
            dataSourceInfoService.addEnvParamsToDataSource(updatedOne.getDataSourceEnvId(), updatedOne);
        }
        //Validate connect parameters
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(updatedOne.getDataSourceTypeId());
        updatedOne.setKeyDefinitions(keyDefinitionList);
        Map<String, Object> connectParams = updatedOne.getConnectParams();
        parameterValidator.validate(keyDefinitionList, connectParams);
        RestfulApiHelper.encryptPasswordKey(keyDefinitionList, connectParams);
        dataSourceInfoService.updateDataSourceInfo(updatedOne, storedOne);
    }



}
