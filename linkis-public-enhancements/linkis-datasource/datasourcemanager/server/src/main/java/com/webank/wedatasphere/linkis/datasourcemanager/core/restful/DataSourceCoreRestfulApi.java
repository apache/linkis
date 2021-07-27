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

import com.github.pagehelper.PageInfo;
import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.datasourcemanager.common.ServiceErrorCode;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceType;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DatasourceVersion;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import com.webank.wedatasphere.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceInfoService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.DataSourceRelateService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.service.MetadataOperateService;
import com.webank.wedatasphere.linkis.datasourcemanager.core.vo.DataSourceVo;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSource;
import com.webank.wedatasphere.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidateException;
import com.webank.wedatasphere.linkis.datasourcemanager.core.validate.ParameterValidator;
import com.webank.wedatasphere.linkis.metadatamanager.common.Json;
import com.webank.wedatasphere.linkis.metadatamanager.common.MdmConfiguration;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private MetadataOperateService metadataOperateService;

    private MultiPartFormDataTransformer formDataTransformer;


    @PostConstruct
    public void initRestful() {
        this.formDataTransformer = FormDataTransformerFactory.buildCustom();
    }


    @GET
    @Path("/type/all")
    public Response getAllDataSourceTypes() {
        return RestfulApiHelper.doAndResponse(() -> {
            List<DataSourceType> dataSourceTypes = dataSourceRelateService.getAllDataSourceTypes();
            return Message.ok().data("type_list", dataSourceTypes);
        }, "/data_source/type/all", "Fail to get all types of data source[获取数据源类型列表失败]");
    }


    @GET
    @Path("/key_define/type/{type_id}")
    public Response getKeyDefinitionsByType(@PathParam("type_id") Long dataSourceTypeId) {
        return RestfulApiHelper.doAndResponse(() -> {
                    List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceRelateService.getKeyDefinitionsByType(dataSourceTypeId);
                    return Message.ok().data("key_define", keyDefinitions);
                }, "/data_source/key_define/type/" + dataSourceTypeId,
                "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
    }

    @GET
    @Path("/key_define/type/{type_id}/{scope}")
    public Response getKeyDefinitionsByTypeAndScope(@PathParam("type_id") Long dataSourceTypeId,
                                                    @PathParam("scope") String scopeValue) {
        return RestfulApiHelper.doAndResponse(() -> {
                    DataSourceParamKeyDefinition.Scope scope = DataSourceParamKeyDefinition.Scope.valueOf(scopeValue.toUpperCase());
                    List<DataSourceParamKeyDefinition> keyDefinitions = dataSourceRelateService
                            .getKeyDefinitionsByType(dataSourceTypeId, scope);
                    return Message.ok().data("key_define", keyDefinitions);
                }, "/data_source/key_define/type/" + dataSourceTypeId + "/" + scopeValue,
                "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
    }

    @POST
    @Path("/info/json")
    public Response insertJsonInfo(DataSource dataSource, @Context HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            //Bean validation
            Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
            if (result.size() > 0) {
                throw new ConstraintViolationException(result);
            }
            dataSource.setCreateUser(userName);
            insertDataSourceConfig(dataSource);
            return Message.ok().data("insert_id", dataSource.getId());
        }, "/data_source/info/json", "Fail to insert data source[新增数据源失败]");
    }

    /**
     * create or update parameter, save a version of parameter,return version number.
     * @param params
     * @param req
     * @return
     */
    @POST
    @Path("/parameter/{datasource_id}/json")
    public Response insertJsonParameter(
            @PathParam("datasource_id") Long datasourceId,
            @RequestParam("params") Map<String, Object> params,
            @Context HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(() -> {
            Map<String, Object> connectParams = (Map) params.get("connectParams");
            String comment = params.get("comment").toString();
            String userName = SecurityFilter.getLoginUsername(req);

            DataSource dataSource = dataSourceInfoService.getDataSourceInfoBrief(datasourceId);
            if(null == dataSource) {
                // todo DatasourceException
                throw new ErrorException(ServiceErrorCode.DATASOURCE_NOTFOUND_ERROR.getValue(), "datasource not found " );
            }
            List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                    .getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
            parameterValidator.validate(keyDefinitionList, connectParams);
            //Encrypt password value type
            RestfulApiHelper.encryptPasswordKey(keyDefinitionList, connectParams);

            long versionId = dataSourceInfoService.insertDataSourceParameter(datasourceId, connectParams, userName, comment);
            return Message.ok().data("version", versionId);
        }, "/data_source/parameter/" + datasourceId + "/json", "Fail to insert data source parameter [保存数据源参数失败]");
    }

    @POST
    @Path("/info/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response insertFormConfig(FormDataMultiPart multiPartForm,
                                     @Context HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(() -> {
            if (null != multiPartForm) {
                String userName = SecurityFilter.getLoginUsername(request);
                DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
                dataSource.setCreateUser(userName);
                insertDataSourceConfig(dataSource);
                return Message.ok().data("insert_id", dataSource.getId());
            }
            return Message.error("Empty request");
        }, "/data_source/info/form", "Fail to insert data source[新增数据源失败]");
    }

    /**
     * get datasource detail, for current version
     * @param dataSourceId
     * @param request
     * @return
     */
    @GET
    @Path("/info/{data_source_id}")
    public Response getInfoByDataSourceId(@PathParam("data_source_id") Long dataSourceId,
                                          @Context HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(() -> {
            DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId);
            // Decrypt
            if (null != dataSource) {
                RestfulApiHelper.decryptPasswordKey(dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId())
                        , dataSource.getConnectParams());
            }
            return Message.ok().data("info", dataSource);
        }, "/data_source/info/" + dataSourceId, "Fail to access data source[获取数据源信息失败]");
    }

    /**
     * get datasource detail
     * @param dataSourceId
     * @param version
     * @return
     */
    @GET
    @Path("/info/{data_source_id}/{version}")
    public Response getInfoByDataSourceIdAndVersion(@PathParam("data_source_id") Long dataSourceId,
                                                    @PathParam("version") Long version) {
        return RestfulApiHelper.doAndResponse(() -> {
            DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId, version);
            // Decrypt
            if (null != dataSource) {
                RestfulApiHelper.decryptPasswordKey(dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId())
                        , dataSource.getConnectParams());
            }
            return Message.ok().data("info", dataSource);
        }, "/data_source/info/" + dataSourceId + "/" + version, "Fail to access data source[获取数据源信息失败]");
    }

    /**
     * get verion list for datasource
     * @param datasourceId
     * @param request
     * @return
     */
    @GET
    @Path("/{data_source_id}/versions")
    public Response getVersionList(@PathParam("data_source_id") Long datasourceId,
                                          @Context HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(() -> {
            List<DatasourceVersion> versions = dataSourceInfoService.getVersionList(datasourceId);
            // Decrypt
//            if (null != versions) {
//                RestfulApiHelper.decryptPasswordKey(dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId())
//                        , dataSource.getConnectParams());
//            }
            return Message.ok().data("versions", versions);
        }, "/data_source/" + datasourceId + "/versions", "Fail to access data source[获取数据源信息失败]");
    }


    @POST
    @Path("/publish/{datasource_id}/{version_id}")
    public Response publishByDataSourceId(@PathParam("datasource_id") Long dataSourceId,
                                          @PathParam("version_id") Long versionId,
                                          @Context HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(() -> {
            int updateResult = dataSourceInfoService.publishByDataSourceId(dataSourceId, versionId);
            return Message.ok().data("updateResult", updateResult);
        }, "/data_source/publish/" + dataSourceId + "/" + versionId, "Fail to publish datasource[数据源版本发布失败]");
    }


    @DELETE
    @Path("/info/{data_source_id}")
    public Response removeDataSource(@PathParam("data_source_id") Long dataSourceId) {
        return RestfulApiHelper.doAndResponse(() -> {
            Long removeId = dataSourceInfoService.removeDataSourceInfo(dataSourceId, "");
            if (removeId < 0) {
                return Message.error("Fail to remove data source[删除数据源信息失败], [id:" + dataSourceId + "]");
            }
            return Message.ok().data("remove_id", removeId);
        }, "/data_source/info/" + dataSourceId, "Fail to remove data source[删除数据源信息失败]");
    }

    @PUT
    @Path("/info/{data_source_id}/expire")
    public Response expireDataSource(@PathParam("data_source_id") Long dataSourceId) {
        return RestfulApiHelper.doAndResponse(() -> {
            Long expireId = dataSourceInfoService.expireDataSource(dataSourceId);
            if (expireId < 0) {
                return Message.error("Fail to expire data source[数据源过期失败], [id:" + dataSourceId + "]");
            }
            return Message.ok().data("expire_id", expireId);
        }, "/data_source/info/" + dataSourceId + "/expire", "Fail to expire data source[数据源过期失败]");
    }

    @PUT
    @Path("/info/{data_source_id}/json")
    public Response updateDataSourceInJson(DataSource dataSource,
                                           @PathParam("data_source_id") Long dataSourceId,
                                           @Context HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            //Bean validation
            Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
            if (result.size() > 0) {
                throw new ConstraintViolationException(result);
            }
            dataSource.setId(dataSourceId);
            dataSource.setModifyUser(userName);
            dataSource.setModifyTime(Calendar.getInstance().getTime());
            DataSource storedDataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);
            if (null == storedDataSource) {
                return Message.error("Fail to update data source[更新数据源失败], " + "[Please check the id:'"
                        + dataSourceId + "' and create system: '" + dataSource.getCreateSystem() + " is correct ']");
            }
            dataSource.setCreateUser(storedDataSource.getCreateUser());
            updateDataSourceConfig(dataSource, storedDataSource);
            return Message.ok().data("update_id", dataSourceId);
        }, "/data_source/info/" + dataSourceId + "/json", "Fail to update data source[更新数据源失败]");
    }

    @PUT
    @Path("/{data_source_id}/{version}/op/connect")
    public Response connectDataSource(@PathParam("data_source_id") Long dataSourceId,
                                      @PathParam("version") Long version,
                                           @Context HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(() -> {
            String operator = SecurityFilter.getLoginUsername(req);
            DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId, version);
            metadataOperateService.doRemoteConnect(MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue()
                            + (StringUtils.isNotBlank(dataSource.getDataSourceType().getName())?("-" +dataSource.getDataSourceType().getName().toLowerCase()) : ""),
                    operator, dataSource.getConnectParams());;
            return Message.ok().data("ok", true);
        }, "/data_source/info/" + dataSourceId + "/" + version + "/op/connect", "Fail to connect data source[连接数据源失败]");
    }

    @PUT
    @Path("/info/{data_source_id}/form")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDataSourceInForm(FormDataMultiPart multiPartForm,
                                           @PathParam("data_source_id") Long dataSourceId,
                                           @Context HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(() -> {
            String userName = SecurityFilter.getLoginUsername(req);
            DataSource dataSource = formDataTransformer.transformToObject(multiPartForm, DataSource.class, beanValidator);
            dataSource.setId(dataSourceId);
            dataSource.setModifyUser(userName);
            dataSource.setModifyTime(Calendar.getInstance().getTime());
            DataSource storedDataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);
            if (null == storedDataSource) {
                return Message.error("Fail to update data source[更新数据源失败], " +
                        "[Please check the id:'" + dataSourceId + "' and create system: '" + dataSource.getCreateSystem() + " is correct ']");
            }
            dataSource.setCreateUser(storedDataSource.getCreateUser());
            updateDataSourceConfig(dataSource, storedDataSource);
            return Message.ok().data("update_id", dataSourceId);
        }, "/data_source/info/" + dataSourceId + "/form", "Fail to update data source[更新数据源失败]");
    }

    @GET
    @Path("/info")
    public Response queryDataSource(@QueryParam("system") String createSystem,
                                    @QueryParam("name") String dataSourceName,
                                    @QueryParam("typeId") Long dataSourceTypeId,
                                    @QueryParam("identifies") String identifies,
                                    @QueryParam("currentPage") Integer currentPage,
                                    @QueryParam("pageSize") Integer pageSize) {
        return RestfulApiHelper.doAndResponse(() -> {
            DataSourceVo dataSourceVo = new DataSourceVo(dataSourceName, dataSourceTypeId,
                    identifies, createSystem);
            dataSourceVo.setCurrentPage(null != currentPage ? currentPage : 1);
            dataSourceVo.setPageSize(null != pageSize ? pageSize : 10);
            PageInfo<DataSource> pageInfo = dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
            List<DataSource> queryList = pageInfo.getList();
            return Message.ok().data("query_list", queryList).data("totalPage", pageInfo.getTotal());
        }, "/data_source/info", "Fail to query page of data source[查询数据源失败]");
    }

    /**
     * Inner method to insert data source
     *
     * @param dataSource data source entity
     * @throws ParameterValidateException
     */
    private void insertDataSourceConfig(DataSource dataSource) throws ErrorException {
        if (null != dataSource.getDataSourceEnvId()) {
            //Merge parameters
            dataSourceInfoService.addEnvParamsToDataSource(dataSource.getDataSourceEnvId(), dataSource);
        }
        //Validate connect parameters
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
        dataSource.setKeyDefinitions(keyDefinitionList);
        dataSourceInfoService.saveDataSourceInfo(dataSource);
    }

    /**
     * Inner method to update data source
     *
     * @param updatedOne new entity
     * @param storedOne  old entity
     * @throws ErrorException
     */
    private void updateDataSourceConfig(DataSource updatedOne, DataSource storedOne) throws ErrorException {
        if (null != updatedOne.getDataSourceEnvId()) {
            //Merge parameters
            dataSourceInfoService.addEnvParamsToDataSource(updatedOne.getDataSourceEnvId(), updatedOne);
        }
        //Validate connect parameters
        List<DataSourceParamKeyDefinition> keyDefinitionList = dataSourceRelateService
                .getKeyDefinitionsByType(updatedOne.getDataSourceTypeId());
        updatedOne.setKeyDefinitions(keyDefinitionList);
//        Map<String, Object> connectParams = updatedOne.getConnectParams();
//        parameterValidator.validate(keyDefinitionList, connectParams);
//        RestfulApiHelper.encryptPasswordKey(keyDefinitionList, connectParams);
        dataSourceInfoService.updateDataSourceInfo(updatedOne, storedOne);
    }


}
