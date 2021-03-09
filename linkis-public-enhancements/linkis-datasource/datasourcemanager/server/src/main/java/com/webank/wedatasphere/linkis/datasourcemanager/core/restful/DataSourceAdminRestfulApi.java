/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datasourcemanager.core.restful;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceEnv;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceInfoService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceRelateService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.vo.DataSourceEnvVo;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidator;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author liaoyt
 * 2020/02/10
 */
@Path("/data_source")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
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

    @POST
    @Path("/env/json")
    public Response insertJsonEnv(DataSourceEnv dataSourceEnv, @Context HttpServletRequest req){
        return RestfulApiHelper.doAndResponse(()->{
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
        }, "/data_source/env/json","Fail to insert data source environment[新增数据源环境失败]");
    }

    @POST
    @Path("/env/form")
    public Response insertFormEnv(FormDataMultiPart multiPartForm,
                                  @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(()->{
           String userName = SecurityFilter.getLoginUsername(request);
            if(!RestfulApiHelper.isAdminUser(userName)){
                return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
            }
            DataSourceEnv dataSourceEnv = formDataTransformer.transformToObject(multiPartForm, DataSourceEnv.class, beanValidator);
            dataSourceEnv.setCreateUser(userName);
            insertDataSourceEnv(dataSourceEnv);
            return Message.ok().data("insert_id", dataSourceEnv.getId());
        }, "/data_source/env/form","Fail to insert data source environment[新增数据源环境失败]");
    }

    @GET
    @Path("/env_list/all/type/{type_id}")
    public Response getAllEnvListByDataSourceType(@PathParam("type_id")Long typeId){
        return RestfulApiHelper.doAndResponse(()->{
            List<DataSourceEnv> envList = dataSourceInfoService.listDataSourceEnvByType(typeId);
            return Message.ok().data("env_list", envList);
        }, "/data_source/env_list/all/type/" + typeId,"Fail to get data source environment list[获取数据源环境清单失败]");
    }

    @GET
    @Path("/env/{env_id}")
    public Response getEnvEntityById(@PathParam("env_id")Long envId){
        return RestfulApiHelper.doAndResponse(() ->{
            DataSourceEnv dataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
            return Message.ok().data("env", dataSourceEnv);
        }, "/data_source/env/" + envId,"Fail to get data source environment[获取数据源环境信息失败]");
    }

    @DELETE
    @Path("/env/{env_id}")
    public Response removeEnvEntity(@PathParam("env_id")Long envId,
                                    @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(() -> {
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
        }, "/data_source/env/" + envId,"Fail to remove data source environment[删除数据源环境信息失败]");
    }

    @PUT
    @Path("/env/{env_id}/json")
    public Response updateJsonEnv(DataSourceEnv dataSourceEnv,
                                  @PathParam("env_id")Long envId,
                                  @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(() -> {
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
        }, "/data_source/env/" + envId + "/json","Fail to update data source environment[更新数据源环境失败]");
    }

    @PUT
    @Path("/env/{env_id}/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public  Response updateFormEnv(FormDataMultiPart multiPartForm,
                                   @PathParam("env_id")Long envId,
                                   @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(()->{
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
        }, "/data_source/env/" + envId + "/form","Fail to update data source environment[更新数据源环境失败]");
    }

    @GET
    @Path("/env")
    public Response queryDataSourceEnv(@QueryParam("name")String envName,
                                       @QueryParam("typeId")Long dataSourceTypeId,
                                       @QueryParam("currentPage")Integer currentPage,
                                       @QueryParam("pageSize")Integer pageSize){
        return RestfulApiHelper.doAndResponse(() -> {
            DataSourceEnvVo dataSourceEnvVo = new DataSourceEnvVo(envName, dataSourceTypeId);
            dataSourceEnvVo.setCurrentPage(null != currentPage ? currentPage : 1);
            dataSourceEnvVo.setPageSize(null != pageSize? pageSize : 10);
            List<DataSourceEnv> queryList = dataSourceInfoService.queryDataSourceEnvPage(dataSourceEnvVo);
            return Message.ok().data("query_list", queryList);
        }, "/data_source/env","Fail to query page of data source environment[查询数据源环境失败]");
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
