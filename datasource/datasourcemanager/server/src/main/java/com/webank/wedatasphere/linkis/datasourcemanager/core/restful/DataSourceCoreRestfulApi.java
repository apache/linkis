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
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceType;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceInfoService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceRelateService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.vo.DataSourceVo;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSource;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidateException;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidator;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
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

    @POST
    @Path("/info/json")
    public Response insertJsonInfo(DataSource dataSource, @Context HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            //Bean validation
            Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
            if(result.size() > 0){
                throw new ConstraintViolationException(result);
            }
            dataSource.setCreateUser(userName);
            insertDataSourceConfig(dataSource);
            return Message.ok().data("insert_id", dataSource.getId());
        }, "/data_source/info/json", "Fail to insert data source[新增数据源失败]");
    }

    @POST
    @Path("/info/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response insertFormConfig(FormDataMultiPart multiPartForm,
                                     @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(() -> {
            if(null != multiPartForm) {
                String userName = SecurityFilter.getLoginUsername(request);
                DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
                dataSource.setCreateUser(userName);
                insertDataSourceConfig(dataSource);
                return Message.ok().data("insert_id", dataSource.getId());
            }
            return Message.error("Empty request");
        }, "/data_source/info/form", "Fail to insert data source[新增数据源失败]");
    }

    @GET
    @Path("/info/{data_source_id}")
    public Response getInfoByDataSourceId(@QueryParam("system")String createSystem,
                                            @PathParam("data_source_id")Long dataSourceId,
                                            @Context HttpServletRequest request){
        return RestfulApiHelper.doAndResponse(() -> {
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
        }, "/data_source/info/" + dataSourceId, "Fail to access data source[获取数据源信息失败]");
    }


    @DELETE
    @Path("/info/{data_source_id}")
    public Response removeDataSource(@QueryParam("system")String createSystem,
                                     @PathParam("data_source_id")Long dataSourceId){
        return RestfulApiHelper.doAndResponse(() -> {
            if(StringUtils.isBlank(createSystem)){
                return Message.error("'create system' is missing[缺少系统名]");
            }
            Long removeId = dataSourceInfoService.removeDataSourceInfo(dataSourceId, createSystem);
            if(removeId < 0){
                return Message.error("Fail to remove data source[删除数据源信息失败], [id:" + dataSourceId +"]");
            }
            return Message.ok().data("remove_id", removeId);
        }, "/data_source/info/" + dataSourceId,"Fail to remove data source[删除数据源信息失败]");
    }

    @PUT
    @Path("/info/{data_source_id}/json")
    public Response updateDataSourceInJson(DataSource dataSource,
                                           @PathParam("data_source_id")Long dataSourceId,
                                           @Context HttpServletRequest req){
        return RestfulApiHelper.doAndResponse(() -> {
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
        }, "/data_source/info/"+dataSourceId+"/json","Fail to update data source[更新数据源失败]");
    }

    @PUT
    @Path("/info/{data_source_id}/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDataSourceInForm(FormDataMultiPart multiPartForm,
                                           @PathParam("data_source_id")Long dataSourceId,
                                           @Context HttpServletRequest req){
        return RestfulApiHelper.doAndResponse(() -> {
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
        }, "/data_source/info/" + dataSourceId + "/form", "Fail to update data source[更新数据源失败]");
    }

    @GET
    @Path("/info")
    public Response queryDataSource(@QueryParam("system")String createSystem,
                                    @QueryParam("name")String dataSourceName,
                                    @QueryParam("typeId")Long dataSourceTypeId,
                                    @QueryParam("identifies")String identifies,
                                    @QueryParam("currentPage")Integer currentPage,
                                    @QueryParam("pageSize")Integer pageSize){
        return RestfulApiHelper.doAndResponse(() -> {
            if(StringUtils.isBlank(createSystem)){
                return Message.error("'create system' is missing[缺少系统名]");
            }
            DataSourceVo dataSourceVo = new DataSourceVo(dataSourceName, dataSourceTypeId,
                    identifies, createSystem);
            dataSourceVo.setCurrentPage(null != currentPage ? currentPage : 1);
            dataSourceVo.setPageSize(null != pageSize? pageSize : 10);
            List<DataSource> queryList = dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
            return Message.ok().data("query_list", queryList);
        }, "/data_source/info", "Fail to query page of data source[查询数据源失败]");
    }

    @GET
    @Path("/key_define/type/{type_id}")
    public Response getKeyDefinitionsByType(@PathParam("type_id") Long dataSourceTypeId){
        return RestfulApiHelper.doAndResponse(() -> {
            List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceRelateService.getKeyDefinitionsByType(dataSourceTypeId);
            return Message.ok().data("key_define", keyDefinitions);
        }, "/data_source/key_define/type/" + dataSourceTypeId,
                "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
    }

    @GET
    @Path("/key_define/type/{type_id}/{scope}")
    public Response getKeyDefinitionsByTypeAndScope(@PathParam("type_id") Long dataSourceTypeId,
                                                    @PathParam("scope") String scopeValue){
        return RestfulApiHelper.doAndResponse(() -> {
            DataSourceParamKeyDefinition.Scope scope = DataSourceParamKeyDefinition.Scope.valueOf(scopeValue.toUpperCase());
            List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceRelateService
                    .getKeyDefinitionsByType(dataSourceTypeId, scope);
            return Message.ok().data("key_define", keyDefinitions);
        }, "/data_source/key_define/type/" + dataSourceTypeId + "/" + scopeValue,
                "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
    }

    @GET
    @Path("/type/all")
    public Response getAllDataSourceTypes(){
        return RestfulApiHelper.doAndResponse(() -> {
            List<DataSourceType> dataSourceTypes = dataSourceRelateService.getAllDataSourceTypes();
            return Message.ok().data("type_list", dataSourceTypes);
        }, "/data_source/type/all", "Fail to get all types of data source[获取数据源类型列表失败]");
    }

    /**
     * Inner method to insert data source
     * @param dataSource data source entity
     * @throws ParameterValidateException
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
