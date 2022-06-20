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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.MessageJava;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.datasourcemanager.common.ServiceErrorCode;
import org.apache.linkis.datasourcemanager.common.auth.AuthContext;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;
import org.apache.linkis.datasourcemanager.common.domain.DatasourceVersion;
import org.apache.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import org.apache.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.service.MetadataOperateService;
import org.apache.linkis.datasourcemanager.core.service.hooks.DataSourceParamsHook;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidateException;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceVo;
import org.apache.linkis.metadata.query.common.MdmConfiguration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;


@Api(tags = "数据源核心")
@RestController
@RequestMapping(
        value = "/data-source-manager",
        produces = {"application/json"})
public class DataSourceCoreRestfulApi {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceCoreRestfulApi.class);
    @Autowired private DataSourceInfoService dataSourceInfoService;

    @Autowired private DataSourceRelateService dataSourceRelateService;

    @Autowired private ParameterValidator parameterValidator;

    @Autowired private Validator beanValidator;

    @Autowired private MetadataOperateService metadataOperateService;

    private MultiPartFormDataTransformer formDataTransformer;

    @Autowired private List<DataSourceParamsHook> dataSourceParamsHooks = new ArrayList<>();

    @PostConstruct
    public void initRestful() {
        this.formDataTransformer = FormDataTransformerFactory.buildCustom();
    }



    @ApiOperation(value="数据源列表",notes="获取数据源列表",response = MessageJava.class)
    @RequestMapping(value = "/type/all", method = RequestMethod.GET)
    public Message getAllDataSourceTypes() {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    List<DataSourceType> dataSourceTypes =
                            dataSourceRelateService.getAllDataSourceTypes();
                    return Message.ok().data("typeList", dataSourceTypes);
                },
                "Fail to get all types of data source[获取数据源类型列表失败]");
    }
    @ApiOperation(value="选择数据源弹窗",notes="根据typeId参数返回不同数据源弹窗",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "typeId", dataType = "Integer", value = "数据源类型枚举值有1，2，3，4，5，6"),
    })
    @RequestMapping(value = "/key-define/type/{typeId}", method = RequestMethod.GET)
    public Message getKeyDefinitionsByType(@PathVariable("typeId") Long dataSourceTypeId) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    List<DataSourceParamKeyDefinition> keyDefinitions =
                            dataSourceRelateService.getKeyDefinitionsByType(dataSourceTypeId);
                    return Message.ok().data("keyDefine", keyDefinitions);
                },
                "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
    }
    @ApiOperation(value="新增数据源",notes="新增数据源",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "connectParams", dataType = "Map", value = "新增数据源的内容"),
            @ApiImplicitParam(name = "host", dataType = "String", required = true, value = "Ip,connectParams中的内容"),
            @ApiImplicitParam(name = "params", dataType = "String", required = true, value = "连接参数,connectParams中的内容"),
            @ApiImplicitParam(name = "password", dataType = "String", required = true, value = "密码,connectParams中的内容"),
            @ApiImplicitParam(name = "port", dataType = "String", required = true, value = "端口,connectParams中的内容"),
            @ApiImplicitParam(name = "subSystem", dataType = "String", required = true, value = "子系统名,connectParams中的内容"),
            @ApiImplicitParam(name = "username", dataType = "String", required = true, value = "用户名,connectParams中的内容"),
            @ApiImplicitParam(name = "createSystem", dataType = "String", required = true, value = "创建系统，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceDesc", dataType = "String", required = true, value = "数据源描述，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceEnvId", dataType = "String",  value = "数据源EnvId，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceName", dataType = "String", required = true, value = "数据源名称，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceTypeId", dataType = "String", required = true, value = "数据源类型ID，跟connectParams一个级别"),
            @ApiImplicitParam(name = "labels", dataType = "String", required = true, value = "标签，跟connectParams一个级别")
    })
    @RequestMapping(value = "/info/json", method = RequestMethod.POST)
    public Message insertJsonInfo(@RequestBody DataSource dataSource, HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    String userName = SecurityFilter.getLoginUsername(req);
                    // Bean validation
                    Set<ConstraintViolation<DataSource>> result =
                            beanValidator.validate(dataSource, Default.class);
                    if (result.size() > 0) {
                        throw new ConstraintViolationException(result);
                    }
                    // Escape the data source name
                    dataSource.setCreateUser(userName);
                    if (dataSourceInfoService.existDataSource(dataSource.getDataSourceName())) {
                        return Message.error(
                                "The data source named: "
                                        + dataSource.getDataSourceName()
                                        + " has been existed [数据源: "
                                        + dataSource.getDataSourceName()
                                        + " 已经存在]");
                    }
                    insertDataSource(dataSource);
                    return Message.ok().data("insertId", dataSource.getId());
                },
                "Fail to insert data source[新增数据源失败]");
    }



    @ApiOperation(value="修改数据源信息",notes="修改数据源信息",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSourceId", dataType = "Long", value = "新增数ID"),
            @ApiImplicitParam(name = "connectParams", dataType = "Map", value = "新增数据源的内容"),
            @ApiImplicitParam(name = "host", dataType = "String", required = true, value = "Ip,connectParams中的内容"),
            @ApiImplicitParam(name = "params", dataType = "String", required = true, value = "连接参数,connectParams中的内容"),
            @ApiImplicitParam(name = "password", dataType = "String", required = true, value = "密码,connectParams中的内容"),
            @ApiImplicitParam(name = "port", dataType = "String", required = true, value = "端口,connectParams中的内容"),
            @ApiImplicitParam(name = "subSystem", dataType = "String", required = true, value = "子系统名,connectParams中的内容"),
            @ApiImplicitParam(name = "username", dataType = "String", required = true, value = "用户名,connectParams中的内容"),
            @ApiImplicitParam(name = "createSystem", dataType = "String", required = true, value = "创建系统，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceDesc", dataType = "String", required = true, value = "数据源描述，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceEnvId", dataType = "String",  value = "数据源EnvId，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceName", dataType = "String", required = true, value = "数据源名称，跟connectParams一个级别"),
            @ApiImplicitParam(name = "dataSourceTypeId", dataType = "String", required = true, value = "数据源类型ID，跟connectParams一个级别"),
            @ApiImplicitParam(name = "labels", dataType = "String", required = true, value = "标签，跟connectParams一个级别")
    })
    @RequestMapping(value = "/info/{dataSourceId}/json", method = RequestMethod.PUT)
    public Message updateDataSourceInJson(
            @RequestBody DataSource dataSource,
            @PathVariable("dataSourceId") Long dataSourceId,
            HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    String userName = SecurityFilter.getLoginUsername(req);
                    // Bean validation
                    Set<ConstraintViolation<DataSource>> result =
                            beanValidator.validate(dataSource, Default.class);
                    if (result.size() > 0) {
                        throw new ConstraintViolationException(result);
                    }
                    dataSource.setId(dataSourceId);
                    dataSource.setModifyUser(userName);
                    dataSource.setModifyTime(Calendar.getInstance().getTime());
                    DataSource storedDataSource =
                            dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);
                    if (null == storedDataSource) {
                        return Message.error("This data source was not found [更新数据源失败]");
                    }
                    if (!AuthContext.hasPermission(storedDataSource, userName)) {
                        return Message.error(
                                "Don't have update permission for data source [没有数据源的更新权限]");
                    }
                    String dataSourceName = dataSource.getDataSourceName();
                    if (!Objects.equals(dataSourceName, storedDataSource.getDataSourceName())
                            && dataSourceInfoService.existDataSource(dataSourceName)) {
                        return Message.error(
                                "The data source named: "
                                        + dataSourceName
                                        + " has been existed [数据源: "
                                        + dataSourceName
                                        + " 已经存在]");
                    }
                    dataSourceInfoService.updateDataSourceInfo(dataSource);
                    return Message.ok().data("updateId", dataSourceId);
                },
                "Fail to update data source[更新数据源失败]");
    }

    /**
     * create or update parameter, save a version of parameter,return version id.
     *
     * @param params
     * @param req
     * @return
     */

    @ApiOperation(value="数据源操作记录",notes="数据源操作记录",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSourceId", dataType = "Long", value = "数据源Id"),
            @ApiImplicitParam(name = "connectParams", dataType = "Map", value = "新增数据源的内容"),
            @ApiImplicitParam(name = "host", dataType = "String", required = true, value = "Ip,connectParams中的内容"),
            @ApiImplicitParam(name = "params", dataType = "String", required = true, value = "连接参数,connectParams中的内容"),
            @ApiImplicitParam(name = "password", dataType = "String", required = true, value = "密码,connectParams中的内容"),
            @ApiImplicitParam(name = "port", dataType = "String", required = true, value = "端口,connectParams中的内容"),
            @ApiImplicitParam(name = "subSystem", dataType = "String", required = true, value = "子系统名,connectParams中的内容"),
            @ApiImplicitParam(name = "username", dataType = "String", required = true, value = "用户名,connectParams中的内容"),
            @ApiImplicitParam(name = "comment", dataType = "String", required = true, value = "描述，跟connectParams一个级别")
    })
    @RequestMapping(value = "/parameter/{dataSourceId}/json", method = RequestMethod.POST)
    public Message insertJsonParameter(
            @PathVariable("dataSourceId") Long dataSourceId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    Map<String, Object> connectParams = (Map) params.get("connectParams");
                    String comment = params.get("comment").toString();
                    String userName = SecurityFilter.getLoginUsername(req);

                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);
                    if (null == dataSource) {
                        throw new ErrorException(
                                ServiceErrorCode.DATASOURCE_NOTFOUND_ERROR.getValue(),
                                "datasource not found ");
                    }
                    if (!AuthContext.hasPermission(dataSource, userName)) {
                        return Message.error(
                                "Don't have update permission for data source [没有数据源的更新权限]");
                    }
                    List<DataSourceParamKeyDefinition> keyDefinitionList =
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId());
                    parameterValidator.validate(keyDefinitionList, connectParams);
                    // Encrypt password value type
                    RestfulApiHelper.encryptPasswordKey(keyDefinitionList, connectParams);

                    long versionId =
                            dataSourceInfoService.insertDataSourceParameter(
                                    keyDefinitionList,
                                    dataSourceId,
                                    connectParams,
                                    userName,
                                    comment);

                    return Message.ok().data("version", versionId);
                },
                "Fail to insert data source parameter [保存数据源参数失败]");
    }

    /**
     * get datasource detail, for current version
     *
     * @param dataSourceId
     * @param request
     * @return
     */
    @RequestMapping(value = "/info/{dataSourceId}", method = RequestMethod.GET)
    public Message getInfoByDataSourceId(
            @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId);
                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }
                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have query permission for data source [没有数据源的查询权限]");
                    }
                    // Decrypt
                    RestfulApiHelper.decryptPasswordKey(
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId()),
                            dataSource.getConnectParams());
                    return Message.ok().data("info", dataSource);
                },
                "Fail to access data source[获取数据源信息失败]");
    }

    @RequestMapping(value = "/info/name/{dataSourceName}", method = RequestMethod.GET)
    public Message getInfoByDataSourceName(
            @PathVariable("dataSourceName") String dataSourceName, HttpServletRequest request)
            throws UnsupportedEncodingException {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceName);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have query permission for data source [没有数据源的查询权限]");
                    }
                    // Decrypt
                    RestfulApiHelper.decryptPasswordKey(
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId()),
                            dataSource.getConnectParams());

                    return Message.ok().data("info", dataSource);
                },
                "Fail to access data source[获取数据源信息失败]");
    }

    /**
     * get datasource detail
     *
     * @param dataSourceId
     * @param version
     * @return
     */
    @ApiOperation(value="编辑数据源",notes="打开编辑数据源",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSourceId", dataType = "Long", value = "数据源ID"),
            @ApiImplicitParam(name = "version", dataType = "Long", value = "数据源弹窗类型，默认1，2，3，4，5，6"),
    })
    @RequestMapping(value = "/info/{dataSourceId}/{version}", method = RequestMethod.GET)
    public Message getInfoByDataSourceIdAndVersion(
            @PathVariable("dataSourceId") Long dataSourceId,
            @PathVariable("version") Long version,
            HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfo(dataSourceId, version);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have query permission for data source [没有数据源的查询权限]");
                    }
                    // Decrypt
                    RestfulApiHelper.decryptPasswordKey(
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId()),
                            dataSource.getConnectParams());
                    return Message.ok().data("info", dataSource);
                },
                "Fail to access data source[获取数据源信息失败]");
    }

    /**
     * get verion list for datasource
     *
     * @param dataSourceId
     * @param request
     * @return
     */
    @RequestMapping(value = "/{dataSourceId}/versions", method = RequestMethod.GET)
    public Message getVersionList(
            @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have query permission for data source [没有数据源的查询权限]");
                    }
                    List<DatasourceVersion> versions =
                            dataSourceInfoService.getVersionList(dataSourceId);
                    // Decrypt
                    if (null != versions) {
                        versions.forEach(
                                version -> {
                                    RestfulApiHelper.decryptPasswordKey(
                                            dataSourceRelateService.getKeyDefinitionsByType(
                                                    dataSource.getDataSourceTypeId()),
                                            version.getConnectParams());
                                });
                    }
                    return Message.ok().data("versions", versions);
                },
                "Fail to access data source[获取数据源信息失败]");
    }

    @RequestMapping(value = "/publish/{dataSourceId}/{versionId}", method = RequestMethod.POST)
    public Message publishByDataSourceId(
            @PathVariable("dataSourceId") Long dataSourceId,
            @PathVariable("versionId") Long versionId,
            HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    // Get brief info
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have publish permission for data source [没有数据源的发布权限]");
                    }
                    int updateResult =
                            dataSourceInfoService.publishByDataSourceId(dataSourceId, versionId);
                    if (0 == updateResult) {
                        return Message.error("publish error");
                    }
                    return Message.ok();
                },
                "Fail to publish datasource[数据源版本发布失败]");
    }

    /**
     * Dangerous operation!
     *
     * @param dataSourceId
     * @return
     */
    @ApiOperation(value="删除数据源",notes="删除数据源",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSourceId", dataType = "Long", value = "数据源ID"),
    })
    @RequestMapping(value = "/info/delete/{dataSourceId}", method = RequestMethod.DELETE)
    public Message removeDataSource(
            @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    // Get brief info
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have delete permission for data source [没有数据源的删除权限]");
                    }
                    Long removeId = dataSourceInfoService.removeDataSourceInfo(dataSourceId, "");
                    if (removeId < 0) {
                        return Message.error(
                                "Fail to remove data source[删除数据源信息失败], [id:" + dataSourceId + "]");
                    }
                    return Message.ok().data("removeId", removeId);
                },
                "Fail to remove data source[删除数据源信息失败]");
    }


    @ApiOperation(value="过期数据源",notes="过期数据源",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSourceId", dataType = "Long", value = "数据源ID"),
    })
    @RequestMapping(value = "/info/{dataSourceId}/expire", method = RequestMethod.PUT)
    public Message expireDataSource(
            @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    // Get brief info
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, request)) {
                        return Message.error(
                                "Don't have operation permission for data source [没有数据源的操作权限]");
                    }
                    Long expireId = dataSourceInfoService.expireDataSource(dataSourceId);
                    if (expireId < 0) {
                        return Message.error(
                                "Fail to expire data source[数据源过期失败], [id:" + dataSourceId + "]");
                    }
                    return Message.ok().data("expireId", expireId);
                },
                "Fail to expire data source[数据源过期失败]");
    }

    /**
     * get datasource connect params for publish version
     *
     * @param dataSourceId
     * @param req
     * @return
     */
    @RequestMapping(value = "/{dataSourceId}/connect-params", method = RequestMethod.GET)
    public Message getConnectParams(
            @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoForConnect(dataSourceId);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, req)) {
                        return Message.error(
                                "Don't have query permission for data source [没有数据源的查询权限]");
                    }
                    Map<String, Object> connectParams = dataSource.getConnectParams();
                    RestfulApiHelper.decryptPasswordKey(
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId()),
                            connectParams);
                    return Message.ok().data("connectParams", connectParams);
                },
                "Fail to connect data source[连接数据源失败]");
    }

    @RequestMapping(value = "/name/{dataSourceName}/connect-params", method = RequestMethod.GET)
    public Message getConnectParams(
            @PathVariable("dataSourceName") String dataSourceName, HttpServletRequest req)
            throws UnsupportedEncodingException {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoForConnect(dataSourceName);

                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, req)) {
                        return Message.error(
                                "Don't have query permission for data source [没有数据源的查询权限]");
                    }
                    Map<String, Object> connectParams = dataSource.getConnectParams();
                    RestfulApiHelper.decryptPasswordKey(
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId()),
                            connectParams);
                    return Message.ok().data("connectParams", connectParams);
                },
                "Fail to connect data source[连接数据源失败]");
    }

    @RequestMapping(value = "/{dataSourceId}/{version}/op/connect", method = RequestMethod.PUT)
    public Message connectDataSource(
            @PathVariable("dataSourceId") Long dataSourceId,
            @PathVariable("version") Long version,
            HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    String operator = SecurityFilter.getLoginUsername(req);
                    DataSource dataSource =
                            dataSourceInfoService.getDataSourceInfoForConnect(
                                    dataSourceId, version);
                    if (dataSource == null) {
                        return Message.error("No Exists The DataSource [不存在该数据源]");
                    }

                    if (!AuthContext.hasPermission(dataSource, req)) {
                        return Message.error(
                                "Don't have operation permission for data source [没有数据源的操作权限]");
                    }
                    String dataSourceTypeName = dataSource.getDataSourceType().getName();
                    String mdRemoteServiceName =
                            MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue();
                    Map<String, Object> connectParams = dataSource.getConnectParams();
                    RestfulApiHelper.decryptPasswordKey(
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId()),
                            connectParams);
                    // Get definitions
                    List<DataSourceParamKeyDefinition> keyDefinitionList =
                            dataSourceRelateService.getKeyDefinitionsByType(
                                    dataSource.getDataSourceTypeId());
                    // For connecting, also need to handle the parameters
                    for (DataSourceParamsHook hook : dataSourceParamsHooks) {
                        hook.beforePersist(connectParams, keyDefinitionList);
                    }
                    metadataOperateService.doRemoteConnect(
                            mdRemoteServiceName,
                            dataSourceTypeName.toLowerCase(),
                            operator,
                            dataSource.getConnectParams());
                    return Message.ok().data("ok", true);
                },
                "Fail to connect data source[连接数据源失败]");
    }

    @ApiOperation(value="数据源列表",notes="获取数据源列表",response = MessageJava.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name="typeId",dataType="Integer",value="数据源列表"),
            @ApiImplicitParam(name="pageSize",dataType="Integer",value="页面大小"),
            @ApiImplicitParam(name="currentPage",dataType="Integer",value="页码"),
            @ApiImplicitParam(name="name",dataType="String",value="数据源名称"),
    })
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Message queryDataSource(
            @RequestParam(value = "system", required = false) String createSystem,
            @RequestParam(value = "name", required = false) String dataSourceName,
            @RequestParam(value = "typeId", required = false) Long dataSourceTypeId,
            @RequestParam(value = "identifies", required = false) String identifies,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest req) {
        return RestfulApiHelper.doAndResponse(
                () -> {
                    DataSourceVo dataSourceVo =
                            new DataSourceVo(
                                    dataSourceName, dataSourceTypeId, identifies, createSystem);
                    dataSourceVo.setCurrentPage(null != currentPage ? currentPage : 1);
                    dataSourceVo.setPageSize(null != pageSize ? pageSize : 10);
                    String permissionUser = SecurityFilter.getLoginUsername(req);
                    if (AuthContext.isAdministrator(permissionUser)) {
                        permissionUser = null;
                    }
                    dataSourceVo.setPermissionUser(permissionUser);
                    PageInfo<DataSource> pageInfo =
                            dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
                    List<DataSource> queryList = pageInfo.getList();
                    return Message.ok()
                            .data("queryList", queryList)
                            .data("totalPage", pageInfo.getTotal());
                },
                "Fail to query page of data source[查询数据源失败]");
    }

    /**
     * Inner method to insert data source
     *
     * @param dataSource data source entity
     * @throws ParameterValidateException
     */
    private void insertDataSource(DataSource dataSource) throws ErrorException {
        List<DataSourceParamKeyDefinition> keyDefinitionList =
                dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
        dataSource.setKeyDefinitions(keyDefinitionList);
        dataSourceInfoService.saveDataSourceInfo(dataSource);
    }
}
