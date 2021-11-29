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
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import org.apache.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceEnvVo;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
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
public class DataSourceAdminRestfulApi {

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

    @RequestMapping(value = "/env/json",method = RequestMethod.POST)
    public Message insertJsonEnv(DataSourceEnv dataSourceEnv, HttpServletRequest req) throws ErrorException {
        String userName = SecurityFilter.getLoginUsername(req);
        if(!RestfulApiHelper.isAdminUser(userName)){
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
        }
        //Bean validation
        Set<ConstraintViolation<DataSourceEnv>> result = beanValidator.validate(dataSourceEnv, Default.class);
        if(result.size() > 0){
            throw new ConstraintViolationException(result);
        }
        dataSourceEnv.setCreateUser(userName);
        insertDataSourceEnv(dataSourceEnv);
        return Message.ok().data("insert_id", dataSourceEnv.getId());
    }

    @RequestMapping(value = "/env/form",method = RequestMethod.POST)
    public Message insertFormEnv(FormDataMultiPart multiPartForm, HttpServletRequest request) throws ErrorException {
       String userName = SecurityFilter.getLoginUsername(request);
        if(!RestfulApiHelper.isAdminUser(userName)){
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
        }
        DataSourceEnv dataSourceEnv = formDataTransformer.transformToObject(multiPartForm, DataSourceEnv.class, beanValidator);
        dataSourceEnv.setCreateUser(userName);
        insertDataSourceEnv(dataSourceEnv);
        return Message.ok().data("insert_id", dataSourceEnv.getId());
    }

    @RequestMapping(value = "/env_list/all/type/{type_id}",method = RequestMethod.GET)
    public Message getAllEnvListByDataSourceType(@PathVariable("type_id")Long typeId){
        List<DataSourceEnv> envList = dataSourceInfoService.listDataSourceEnvByType(typeId);
        return Message.ok().data("env_list", envList);
    }

    @RequestMapping (value = "/env/{env_id}",method = RequestMethod.GET)
    public Message getEnvEntityById(@PathVariable("env_id")Long envId){
        DataSourceEnv dataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
        return Message.ok().data("env", dataSourceEnv);
    }

    @RequestMapping(value = "/env/{env_id}",method = RequestMethod.DELETE)
    public Message removeEnvEntity(@PathVariable("env_id")Long envId,
                                    HttpServletRequest request){
        String userName = SecurityFilter.getLoginUsername(request);
        if(!RestfulApiHelper.isAdminUser(userName)){
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
        }
        Long removeId = dataSourceInfoService.removeDataSourceEnv(envId);
        if(removeId < 0){
            return Message.error("Fail to remove data source environment[删除数据源环境信息失败], [id:" +
                    envId + "]");
        }
        return Message.ok().data("remove_id", removeId);
    }

    @RequestMapping(value = "/env/{env_id}/json",method = RequestMethod.PUT)
    public Message updateJsonEnv(DataSourceEnv dataSourceEnv,
                                  @PathVariable("env_id")Long envId,
                                   HttpServletRequest request) throws ErrorException {
        String userName = SecurityFilter.getLoginUsername(request);
        if(!RestfulApiHelper.isAdminUser(userName)){
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
        }
        //Bean validation
        Set<ConstraintViolation<DataSourceEnv>> result = beanValidator.validate(dataSourceEnv, Default.class);
        if(result.size() > 0){
            throw new ConstraintViolationException(result);
        }
        dataSourceEnv.setId(envId);
        dataSourceEnv.setModifyUser(userName);
        dataSourceEnv.setModifyTime(Calendar.getInstance().getTime());
        DataSourceEnv storedDataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
        if(null == storedDataSourceEnv){
            return Message.error("Fail to update data source environment[更新数据源环境失败], " + "[Please check the id:'"
                    + envId + " is correct ']");
        }
        dataSourceEnv.setCreateUser(storedDataSourceEnv.getCreateUser());
        updateDataSourceEnv(dataSourceEnv, storedDataSourceEnv);
        return Message.ok().data("update_id", envId);
    }

    @RequestMapping(value = "/env/{env_id}/form",method = RequestMethod.PUT,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public  Message updateFormEnv(FormDataMultiPart multiPartForm,
                                   @PathVariable("env_id")Long envId,
                                    HttpServletRequest request) throws ErrorException {
        if(null != multiPartForm) {
            String userName = SecurityFilter.getLoginUsername(request);
            if (!RestfulApiHelper.isAdminUser(userName)) {
                return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
            }
            DataSourceEnv dataSourceEnv = formDataTransformer.transformToObject(multiPartForm, DataSourceEnv.class, beanValidator);
            dataSourceEnv.setId(envId);
            dataSourceEnv.setModifyUser(userName);
            dataSourceEnv.setModifyTime(Calendar.getInstance().getTime());
            DataSourceEnv storedDataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
            if (null == storedDataSourceEnv) {
                return Message.error("Fail to update data source environment[更新数据源环境失败], " + "[Please check the id:'"
                        + envId + " is correct ']");
            }
            dataSourceEnv.setCreateUser(storedDataSourceEnv.getCreateUser());
            updateDataSourceEnv(dataSourceEnv, storedDataSourceEnv);
            return Message.ok().data("update_id", envId);
        }
        return Message.error("Empty request");
    }

    @RequestMapping(value = "/env",method = RequestMethod.GET)
    public Message queryDataSourceEnv(@RequestParam(value = "name",required = false)String envName,
                                       @RequestParam(value = "typeId",required = false)Long dataSourceTypeId,
                                       @RequestParam(value = "currentPage",required = false)Integer currentPage,
                                       @RequestParam(value = "pageSize",required = false)Integer pageSize){
        DataSourceEnvVo dataSourceEnvVo = new DataSourceEnvVo(envName, dataSourceTypeId);
        dataSourceEnvVo.setCurrentPage(null != currentPage ? currentPage : 1);
        dataSourceEnvVo.setPageSize(null != pageSize? pageSize : 10);
        List<DataSourceEnv> queryList = dataSourceInfoService.queryDataSourceEnvPage(dataSourceEnvVo);
        return Message.ok().data("query_list", queryList);
    }

    /**
     * Inner method to insert data source environment
     * @param dataSourceEnv data source environment entity
     * @throws ErrorException
     */
    private void insertDataSourceEnv(DataSourceEnv dataSourceEnv) throws ErrorException{
        //Get key definitions in environment scope
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
        dataSourceEnv.setKeyDefinitions(keyDefinitionList);
        Map<String, Object> connectParams = dataSourceEnv.getConnectParams();
        //Validate connect parameters
        parameterValidator.validate(keyDefinitionList, connectParams);
        dataSourceInfoService.saveDataSourceEnv(dataSourceEnv);
    }

    /**
     * Inner method to update data source environment
     * @param updatedOne new entity
     * @param storedOne old entity
     * @throws ErrorException
     */
    private void updateDataSourceEnv(DataSourceEnv updatedOne, DataSourceEnv storedOne) throws ErrorException{
        //Get key definitions in environment scope
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(updatedOne.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
        updatedOne.setKeyDefinitions(keyDefinitionList);
        Map<String, Object> connectParams = updatedOne.getConnectParams();
        //Validate connect parameters
        parameterValidator.validate(keyDefinitionList, connectParams);
        dataSourceInfoService.updateDataSourceEnv(updatedOne, storedOne);
    }
}
