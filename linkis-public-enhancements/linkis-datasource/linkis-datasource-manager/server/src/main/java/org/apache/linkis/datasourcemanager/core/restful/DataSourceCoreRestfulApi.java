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

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.utils.AESUtils;
import org.apache.linkis.common.variable.DateTypeUtils;
import org.apache.linkis.datasourcemanager.common.auth.AuthContext;
import org.apache.linkis.datasourcemanager.common.domain.DataSource;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceType;
import org.apache.linkis.datasourcemanager.common.domain.DatasourceVersion;
import org.apache.linkis.datasourcemanager.common.util.CryptoUtils;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.datasourcemanager.core.conf.DatasourceConf;
import org.apache.linkis.datasourcemanager.core.dao.DataSourceVersionDao;
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
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.github.pagehelper.PageInfo;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.datasourcemanager.common.errorcode.LinkisDatasourceManagerErrorCodeSummary.DATASOURCE_NOT_FOUND;

@Api(tags = "data source core restful api")
@RestController
@RequestMapping(
    value = "/data-source-manager",
    produces = {"application/json"})
public class DataSourceCoreRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceCoreRestfulApi.class);

  @Autowired private DataSourceInfoService dataSourceInfoService;

  @Autowired private DataSourceRelateService dataSourceRelateService;

  @Autowired private ParameterValidator parameterValidator;

  @Autowired private Validator beanValidator;

  @Autowired private MetadataOperateService metadataOperateService;
  @Autowired private DataSourceVersionDao dataSourceVersionDao;
  private MultiPartFormDataTransformer formDataTransformer;

  @Autowired private List<DataSourceParamsHook> dataSourceParamsHooks = new ArrayList<>();

  @PostConstruct
  public void initRestful() {
    this.formDataTransformer = FormDataTransformerFactory.buildCustom();
  }

  @ApiOperation(
      value = "getAllDataSourceTypes",
      notes = "get all data source types",
      response = Message.class)
  @RequestMapping(value = "/type/all", method = RequestMethod.GET)
  public Message getAllDataSourceTypes(HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "getAllDataSourceTypes");
          List<DataSourceType> dataSourceTypes =
              dataSourceRelateService.getAllDataSourceTypes(request.getHeader("Content-Language"));
          return Message.ok().data("typeList", dataSourceTypes);
        },
        "Fail to get all types of data source[获取数据源类型列表失败]");
  }

  @ApiOperation(
      value = "getKeyDefinitionsByType",
      notes = "get key definitions by type",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "typeId", required = true, dataType = "Long")})
  @RequestMapping(value = "/key-define/type/{typeId}", method = RequestMethod.GET)
  public Message getKeyDefinitionsByType(
      @PathVariable("typeId") Long dataSourceTypeId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "getKeyDefinitionsByType");
          List<DataSourceParamKeyDefinition> keyDefinitions =
              dataSourceRelateService.getKeyDefinitionsByType(
                  dataSourceTypeId, request.getHeader("Content-Language"));
          return Message.ok().data("keyDefine", keyDefinitions);
        },
        "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
  }

  @ApiOperation(
      value = "getKeyDefinitionsByTypeName",
      notes = "get key definitions by typeName",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "typeName", required = true, dataType = "String")})
  @RequestMapping(value = "/key-define/{typeName}", method = RequestMethod.GET)
  public Message getKeyDefinitionsByTypeName(
      @PathVariable("typeName") String typeName, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "getKeyDefinitionsByType");
          DataSourceType targetDataSourceType = getDatasoutceTypeID(typeName, request);
          if (targetDataSourceType != null) {
            List<DataSourceParamKeyDefinition> keyDefinitions =
                dataSourceRelateService.getKeyDefinitionsByType(
                    Long.valueOf(targetDataSourceType.getId()),
                    request.getHeader("Content-Language"));
            return Message.ok().data("keyDefine", keyDefinitions);
          } else {
            return Message.error("No data source type found with name: " + typeName);
          }
        },
        "Fail to get key definitions of data source type[查询数据源参数键值对失败]");
  }

  @ApiOperation(value = "insertJsonInfo", notes = "insert json info", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"dataSource"})
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "createSystem",
        required = true,
        dataType = "String",
        example = "linkis"),
    @ApiImplicitParam(name = "dataSourceDesc", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceTypeId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "labels", required = true, dataType = "String"),
    @ApiImplicitParam(name = "connectParams", required = true, dataType = "List"),
    @ApiImplicitParam(name = "host", dataType = "String", example = "127.0.0.1"),
    @ApiImplicitParam(name = "password", dataType = "String"),
    @ApiImplicitParam(name = "port", dataType = "String", example = "9523"),
    @ApiImplicitParam(name = "subSystem", dataType = "String"),
    @ApiImplicitParam(name = "username", dataType = "String")
  })
  @RequestMapping(value = "/info/json", method = RequestMethod.POST)
  public Message insertJsonInfo(@RequestBody DataSource dataSource, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "insertJsonInfo");
          if (dataSourceInfoService.existDataSource(dataSource.getDataSourceName())) {
            return Message.error(
                "The data source named: "
                    + dataSource.getDataSourceName()
                    + " has been existed [数据源: "
                    + dataSource.getDataSourceName()
                    + " 已经存在]");
          }
          insertDatasource(dataSource, userName);
          return Message.ok().data("insertId", dataSource.getId());
        },
        "Fail to insert data source[新增数据源失败]");
  }

  @ApiOperation(value = "insertJsonInfo", notes = "insert json info", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"dataSource"})
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "createSystem",
        required = true,
        dataType = "String",
        example = "linkis"),
    @ApiImplicitParam(name = "dataSourceDesc", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceTypeName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "labels", required = true, dataType = "String"),
    @ApiImplicitParam(name = "connectParams", required = true, dataType = "List"),
    @ApiImplicitParam(name = "host", dataType = "String"),
    @ApiImplicitParam(name = "password", dataType = "String"),
    @ApiImplicitParam(name = "port", dataType = "String"),
    @ApiImplicitParam(name = "subSystem", dataType = "String"),
    @ApiImplicitParam(name = "username", dataType = "String")
  })
  @RequestMapping(value = "/info/json/create", method = RequestMethod.POST)
  public Message insertJson(@RequestBody DataSource dataSource, HttpServletRequest request) {
    ModuleUserUtils.getOperationUser(request, "insertJsonCreate");
    String datasourceUser = dataSource.getCreateUser();
    String dataSourceTypeName = dataSource.getDataSourceTypeName();
    // 参数校验
    if (StringUtils.isBlank(datasourceUser)) {
      return Message.error("Parameter createUser cannot be empty （参数 createUser 不能为空）");
    }
    if (StringUtils.isBlank(dataSourceTypeName)) {
      return Message.error(
          "Parameter dataSourceTypeName cannot be empty （参数 dataSourceTypeName 不能为空）");
    }
    Map<String, Object> connectParams = dataSource.getConnectParams();
    if (MapUtils.isEmpty(connectParams)) {
      return Message.error("Parameter connectParams cannot be empty （参数 connectParams 不能为空）");
    }
    // 定义需要校验的参数
    String[] requiredParams = {"host", "port", "driverClassName", "username", "password"};
    for (String param : requiredParams) {
      Object value = connectParams.get(param);
      if (value == null || StringUtils.isEmpty(value.toString())) {
        return Message.error("Parameter " + param + " cannot be empty （参数 " + param + " 不能为空）");
      }
    }
    // 限制仅支持starrocks
    if (!DatasourceConf.INSERT_DATAESOURCE_LIMIT.getValue().contains(dataSourceTypeName)) {
      return Message.error("DataSource Create Only Support starrocks");
    }
    // 参数调整
    dataSource.setDataSourceName(
        String.join(
            "_",
            dataSourceTypeName,
            datasourceUser,
            DateTypeUtils.dateFormatSecondLocal().get().format(new Date())));
    if (dataSourceInfoService.existDataSource(dataSource.getDataSourceName())) {
      return Message.error(
          "The data source named: "
              + dataSource.getDataSourceName()
              + " has been existed [数据源: "
              + dataSource.getDataSourceName()
              + " 已经存在]");
    }
    DataSourceType dataSourceType = getDatasoutceTypeID(dataSourceTypeName, request);
    if (dataSourceType != null)
      dataSource.setDataSourceTypeId(Long.valueOf(dataSourceType.getId()));
    // 创建数据源
    insertDatasource(dataSource, datasourceUser);
    Map<String, Object> stringHashMap = new HashMap<>();
    stringHashMap.put("connectParams", dataSource.getConnectParams());
    stringHashMap.put("comment", "初始化版本");
    // 创建数据源version
    Message message = insertJsonParameter(dataSource.getId(), stringHashMap, request);
    if (message.getStatus() == 1) {
      return message;
    }
    long publishedVersionId = Long.parseLong(message.getData().get("version").toString());
    dataSource.setPublishedVersionId(publishedVersionId);
    // 发布数据源version
    message = publishByDataSourceId(dataSource.getId(), publishedVersionId, request);
    if (message.getStatus() == 1) {
      return message;
    }
    return Message.ok().data("datasource", dataSource);
  }

  @ApiOperation(
      value = "updateDataSourceInJson",
      notes = "update data source in json",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long"),
    @ApiImplicitParam(
        name = "createSystem",
        required = true,
        dataType = "String",
        example = "Linkis"),
    @ApiImplicitParam(
        name = "createTime",
        required = true,
        dataType = "String",
        example = "1650426189000"),
    @ApiImplicitParam(name = "createUser", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceDesc", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "dataSourceTypeId", required = true, dataType = "String"),
    @ApiImplicitParam(name = "labels", required = true, dataType = "String"),
    @ApiImplicitParam(name = "connectParams", required = true, dataType = "List"),
    @ApiImplicitParam(name = "host", dataType = "String"),
    @ApiImplicitParam(name = "password", dataType = "String"),
    @ApiImplicitParam(name = "port", dataType = "String"),
    @ApiImplicitParam(name = "subSystem", dataType = "String"),
    @ApiImplicitParam(name = "username", dataType = "String"),
    @ApiImplicitParam(name = "expire", dataType = "boolean"),
    @ApiImplicitParam(name = "file", dataType = "String"),
    @ApiImplicitParam(name = "modifyTime", dataType = "String"),
    @ApiImplicitParam(name = "modifyUser", dataType = "String"),
    @ApiImplicitParam(name = "versionId", dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"dataSource"})
  @RequestMapping(value = "/info/{dataSourceId}/json", method = RequestMethod.PUT)
  public Message updateDataSourceInJson(
      @RequestBody DataSource dataSource,
      @PathVariable("dataSourceId") Long dataSourceId,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "updateDataSourceInJson");
          // Bean validation
          Set<ConstraintViolation<DataSource>> result =
              beanValidator.validate(dataSource, Default.class);
          if (result.size() > 0) {
            throw new ConstraintViolationException(result);
          }
          dataSource.setId(dataSourceId);
          dataSource.setModifyUser(userName);
          dataSource.setModifyTime(Calendar.getInstance().getTime());
          DataSource storedDataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);
          if (null == storedDataSource) {
            return Message.error("This data source was not found [更新数据源失败]");
          }
          if (!AuthContext.hasPermission(storedDataSource, userName)) {
            return Message.error("Don't have update permission for data source [没有数据源的更新权限]");
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
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          dataSource.setKeyDefinitions(keyDefinitionList);

          Map<String, Object> connectParams = dataSource.getConnectParams();

          if (AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()
              && connectParams.containsKey(AESUtils.PASSWORD)) {
            dataSource
                .getConnectParams()
                .replace(
                    AESUtils.PASSWORD,
                    AESUtils.encrypt(
                        connectParams.get(AESUtils.PASSWORD).toString(),
                        AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue()));
            // 标记密码已经加密
            dataSource.getConnectParams().put(AESUtils.IS_ENCRYPT, AESUtils.ENCRYPT);
          }

          // add default value filed
          keyDefinitionList.forEach(
              keyDefinition -> {
                String key = keyDefinition.getKey();
                if (StringUtils.isNotBlank(keyDefinition.getDefaultValue())
                    && !connectParams.containsKey(key)) {
                  connectParams.put(key, keyDefinition.getDefaultValue());
                }
              });

          for (DataSourceParamsHook hook : dataSourceParamsHooks) {
            hook.beforePersist(connectParams, keyDefinitionList);
          }
          String parameter = Json.toJson(connectParams, null);
          dataSource.setParameter(parameter);
          dataSourceInfoService.updateDataSourceInfo(dataSource);
          return Message.ok().data("updateId", dataSourceId);
        },
        "Fail to update data source[更新数据源失败]");
  }

  /**
   * create or update parameter, save a version of parameter,return version id.
   *
   * @param params
   * @param request
   * @return
   */
  @ApiOperation(
      value = "insertJsonParameter",
      notes = "insert json parameter",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long")})
  @ApiOperationSupport(ignoreParameters = {"params"})
  @RequestMapping(value = "/parameter/{dataSourceId}/json", method = RequestMethod.POST)
  public Message insertJsonParameter(
      @PathVariable("dataSourceId") Long dataSourceId,
      @RequestBody() Map<String, Object> params,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "insertJsonParameter");

          Map<String, Object> connectParams = (Map) params.get("connectParams");
          String comment = params.get("comment").toString();

          DataSource dataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);
          if (null == dataSource) {
            throw new ErrorException(
                DATASOURCE_NOT_FOUND.getErrorCode(), DATASOURCE_NOT_FOUND.getErrorDesc());
          }
          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have update permission for data source [没有数据源的更新权限]");
          }
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());

          formatConnectParams(keyDefinitionList, connectParams);

          parameterValidator.validate(keyDefinitionList, connectParams);
          // Encrypt password value type
          RestfulApiHelper.encryptPasswordKey(keyDefinitionList, connectParams);

          long versionId =
              dataSourceInfoService.insertDataSourceParameter(
                  keyDefinitionList, dataSourceId, connectParams, userName, comment);

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
  @ApiOperation(
      value = "getInfoByDataSourceId",
      notes = "get info by data source id",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long")})
  @RequestMapping(value = "/info/{dataSourceId}", method = RequestMethod.GET)
  public Message getInfoByDataSourceId(
      @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getInfoByDataSourceId dataSourceId:" + dataSourceId);

          DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId);
          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }
          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }

          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          // Decrypt
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, dataSource.getConnectParams());
          }
          return Message.ok().data("info", dataSource);
        },
        "Fail to access data source[获取数据源信息失败]");
  }

  @ApiOperation(
      value = "getInfoByDataSourceName",
      notes = "get info by data source name",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String")
  })
  @RequestMapping(value = "/info/name/{dataSourceName}", method = RequestMethod.GET)
  public Message getInfoByDataSourceName(
      @PathVariable("dataSourceName") String dataSourceName, HttpServletRequest request)
      throws UnsupportedEncodingException {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getInfoByDataSourceName dataSourceName:" + dataSourceName);

          DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceName);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }

          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          // Decrypt
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, dataSource.getConnectParams());
          }
          return Message.ok().data("info", dataSource);
        },
        "Fail to access data source[获取数据源信息失败]");
  }

  @ApiOperation(
      value = "getPublishedInfoByDataSourceName",
      notes = "get published info by data source name",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String")
  })
  @RequestMapping(value = "/publishedInfo/name/{dataSourceName}", method = RequestMethod.GET)
  public Message getPublishedInfoByDataSourceName(
      @PathVariable("dataSourceName") String dataSourceName, HttpServletRequest request)
      throws UnsupportedEncodingException {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getPublishedInfoByDataSourceName dataSourceName:" + dataSourceName);

          DataSource dataSource = dataSourceInfoService.getDataSourcePublishInfo(dataSourceName);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          // Decrypt
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, dataSource.getConnectParams());
          }
          return Message.ok().data("info", dataSource);
        },
        "Fail to access data source[获取数据源信息失败]");
  }

  @ApiOperation(
      value = "Get published info by data source name, IP and port",
      notes = "Retrieve published information of a data source by its type name, IP and port",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "datasourceTypeName", required = true, dataType = "String"),
    @ApiImplicitParam(name = "datasourceUser", required = true, dataType = "String"),
    @ApiImplicitParam(name = "ip", required = true, dataType = "String"),
    @ApiImplicitParam(name = "port", required = true, dataType = "String")
  })
  @RequestMapping(
      value = "/publishedInfo/{datasourceTypeName}/{datasourceUser}/{ip}/{port}",
      method = RequestMethod.GET)
  public Message getPublishedInfoByIpPort(
      @PathVariable("datasourceTypeName") String datasourceTypeName,
      @PathVariable("datasourceUser") String datasourceUser,
      @PathVariable("ip") String ip,
      @PathVariable("port") String port,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String username =
              ModuleUserUtils.getOperationUser(
                  request, "getPublishedInfoByIpPort ip:" + ip + ",port:" + port);
          if (StringUtils.isBlank(datasourceUser)) {
            return Message.error(
                "Parameter datasourceUser cannot be empty （参数 datasourceUser 不能为空）");
          }

          DataSource dataSource =
              dataSourceInfoService.getDataSourcePublishInfo(
                  datasourceTypeName, ip, port, datasourceUser);
          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }
          if (!AuthContext.hasPermission(dataSource, username)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          // Decrypt
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, dataSource.getConnectParams());
          }
          DataSourceType dataSourceType = new DataSourceType();
          dataSourceType.setName(datasourceTypeName);
          dataSource.setDataSourceType(dataSourceType);
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
  @ApiOperation(
      value = "getInfoByDataSourceIdAndVersion",
      notes = "get info by data source id and version",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long"),
    @ApiImplicitParam(name = "version", required = true, dataType = "Long")
  })
  @RequestMapping(value = "/info/{dataSourceId}/{version}", method = RequestMethod.GET)
  public Message getInfoByDataSourceIdAndVersion(
      @PathVariable("dataSourceId") Long dataSourceId,
      @PathVariable("version") Long version,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getInfoByDataSourceIdAndVersion dataSourceId:" + dataSourceId);

          DataSource dataSource = dataSourceInfoService.getDataSourceInfo(dataSourceId, version);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          // Decrypt
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, dataSource.getConnectParams());
          }
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
  @ApiOperation(value = "getVersionList", notes = "get version list", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long")})
  @RequestMapping(value = "/{dataSourceId}/versions", method = RequestMethod.GET)
  public Message getVersionList(
      @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getVersionList dataSourceId:" + dataSourceId);

          DataSource dataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }
          List<DatasourceVersion> versions = dataSourceInfoService.getVersionList(dataSourceId);

          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          // Decrypt
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue() && null != versions) {
            versions.forEach(
                version -> {
                  RestfulApiHelper.decryptPasswordKey(
                      keyDefinitionList, version.getConnectParams());
                });
          }
          return Message.ok().data("versions", versions);
        },
        "Fail to access data source[获取数据源信息失败]");
  }

  @ApiOperation(
      value = "publishByDataSourceId",
      notes = "publish by datasource id",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long"),
    @ApiImplicitParam(name = "version", required = true, dataType = "Long")
  })
  @RequestMapping(value = "/publish/{dataSourceId}/{versionId}", method = RequestMethod.POST)
  public Message publishByDataSourceId(
      @PathVariable("dataSourceId") Long dataSourceId,
      @PathVariable("versionId") Long versionId,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "publishByDataSourceId dataSourceId:" + dataSourceId);

          // Get brief info
          DataSource dataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have publish permission for data source [没有数据源的发布权限]");
          }
          int updateResult = dataSourceInfoService.publishByDataSourceId(dataSourceId, versionId);
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
  @ApiOperation(value = "removeDataSource", notes = "remove datasource", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long")})
  @RequestMapping(value = "/info/delete/{dataSourceId}", method = RequestMethod.DELETE)
  public Message removeDataSource(
      @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "removeDataSource dataSourceId:" + dataSourceId);

          // Get brief info
          DataSource dataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have delete permission for data source [没有数据源的删除权限]");
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

  @ApiOperation(value = "expireDataSource", notes = "expire data source", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long")})
  @RequestMapping(value = "/info/{dataSourceId}/expire", method = RequestMethod.PUT)
  public Message expireDataSource(
      @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "expireDataSource dataSourceId:" + dataSourceId);

          // Get brief info
          DataSource dataSource = dataSourceInfoService.getDataSourceInfoBrief(dataSourceId);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have operation permission for data source [没有数据源的操作权限]");
          }
          Long expireId = dataSourceInfoService.expireDataSource(dataSourceId);
          if (expireId < 0) {
            return Message.error("Fail to expire data source[数据源过期失败], [id:" + dataSourceId + "]");
          }
          return Message.ok().data("expireId", expireId);
        },
        "Fail to expire data source[数据源过期失败]");
  }

  /**
   * get datasource connect params for publish version
   *
   * @param dataSourceId
   * @param request
   * @return
   */
  @ApiOperation(
      value = "getConnectParams(dataSourceId)",
      notes = "get connect params(dataSourceId)",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long")})
  @RequestMapping(value = "/{dataSourceId}/connect-params", method = RequestMethod.GET)
  public Message getConnectParams(
      @PathVariable("dataSourceId") Long dataSourceId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getConnectParams dataSourceId:" + dataSourceId);

          DataSource dataSource = dataSourceInfoService.getDataSourceInfoForConnect(dataSourceId);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }
          Map<String, Object> connectParams = dataSource.getConnectParams();
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, connectParams);
          }
          return Message.ok().data("connectParams", connectParams);
        },
        "Fail to connect data source[连接数据源失败]");
  }

  @ApiOperation(
      value = "getConnectParams(dataSourceName)",
      notes = "get connect params(dataSourceName)",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceName", required = true, dataType = "String")
  })
  @RequestMapping(value = "/name/{dataSourceName}/connect-params", method = RequestMethod.GET)
  public Message getConnectParams(
      @PathVariable("dataSourceName") String dataSourceName, HttpServletRequest request)
      throws UnsupportedEncodingException {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(
                  request, "getConnectParams dataSourceName:" + dataSourceName);

          DataSource dataSource = dataSourceInfoService.getDataSourceInfoForConnect(dataSourceName);

          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, userName)) {
            return Message.error("Don't have query permission for data source [没有数据源的查询权限]");
          }
          Map<String, Object> connectParams = dataSource.getConnectParams();

          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, connectParams);
          }
          return Message.ok().data("connectParams", connectParams);
        },
        "Fail to connect data source[连接数据源失败]");
  }

  @ApiOperation(value = "connectDataSource", notes = "connect datasource", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "dataSourceId", required = true, dataType = "Long"),
    @ApiImplicitParam(name = "version", required = true, dataType = "Long")
  })
  @RequestMapping(value = "/{dataSourceId}/{version}/op/connect", method = RequestMethod.PUT)
  public Message connectDataSource(
      @PathVariable("dataSourceId") Long dataSourceId,
      @PathVariable("version") Long version,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String operator =
              ModuleUserUtils.getOperationUser(
                  request, "connectDataSource dataSourceId:" + dataSourceId);

          DataSource dataSource =
              dataSourceInfoService.getDataSourceInfoForConnect(dataSourceId, version);
          if (dataSource == null) {
            return Message.error("No Exists The DataSource [不存在该数据源]");
          }

          if (!AuthContext.hasPermission(dataSource, operator)) {
            return Message.error("Don't have operation permission for data source [没有数据源的操作权限]");
          }
          String dataSourceTypeName = dataSource.getDataSourceType().getName();
          String mdRemoteServiceName = MdmConfiguration.METADATA_SERVICE_APPLICATION.getValue();
          Map<String, Object> connectParams = dataSource.getConnectParams();

          // Get definitions
          List<DataSourceParamKeyDefinition> keyDefinitionList =
              dataSourceRelateService.getKeyDefinitionsByType(dataSource.getDataSourceTypeId());
          if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
            RestfulApiHelper.decryptPasswordKey(keyDefinitionList, connectParams);
          }
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

  @ApiOperation(
      value = "queryDataSourceByIds",
      notes = "query datasource by ids",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "ids", required = true, dataType = "List"),
  })
  @RequestMapping(value = "/info/ids", method = RequestMethod.GET)
  public Message queryDataSource(
      @RequestParam(value = "ids") String idsJson, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(request, "queryDataSourceByIds ids:" + idsJson);

          List ids = BDPJettyServerHelper.jacksonJson().readValue(idsJson, List.class);
          List<DataSource> dataSourceList = dataSourceInfoService.queryDataSourceInfo(ids);
          return Message.ok()
              .data("queryList", dataSourceList)
              .data("totalPage", dataSourceList.size());
        },
        "Fail to query page of data source[查询数据源失败]");
  }

  @ApiOperation(value = "queryDataSource", notes = "query datasource", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "system", dataType = "String"),
    @ApiImplicitParam(name = "name", dataType = "Long"),
    @ApiImplicitParam(name = "typeId", dataType = "Long"),
    @ApiImplicitParam(name = "identifies", dataType = "String"),
    @ApiImplicitParam(name = "currentPage", dataType = "Integer"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer")
  })
  @RequestMapping(value = "/info", method = RequestMethod.GET)
  public Message queryDataSource(
      @RequestParam(value = "system", required = false) String createSystem,
      @RequestParam(value = "name", required = false) String dataSourceName,
      @RequestParam(value = "typeId", required = false) Long dataSourceTypeId,
      @RequestParam(value = "identifies", required = false) String identifies,
      @RequestParam(value = "currentPage", required = false) Integer currentPage,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String permissionUser = ModuleUserUtils.getOperationUser(request, "queryDataSource");

          DataSourceVo dataSourceVo =
              new DataSourceVo(dataSourceName, dataSourceTypeId, identifies, createSystem);
          dataSourceVo.setCurrentPage(null != currentPage ? currentPage : 1);
          dataSourceVo.setPageSize(null != pageSize ? pageSize : 10);

          if (AuthContext.isAdministrator(permissionUser)) {
            permissionUser = null;
          }
          dataSourceVo.setPermissionUser(permissionUser);
          PageInfo<DataSource> pageInfo =
              dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
          List<DataSource> queryList = pageInfo.getList();
          return Message.ok().data("queryList", queryList).data("totalPage", pageInfo.getTotal());
        },
        "Fail to query page of data source[查询数据源失败]");
  }

  @ApiOperation(
      value = "queryDataSourceWithConnectParms",
      notes = "query datasource",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "system", dataType = "String"),
    @ApiImplicitParam(name = "name", dataType = "Long"),
    @ApiImplicitParam(name = "typeId", dataType = "Long"),
    @ApiImplicitParam(name = "identifies", dataType = "String"),
    @ApiImplicitParam(name = "currentPage", dataType = "Integer"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer")
  })
  @RequestMapping(value = "/info/connect-params", method = RequestMethod.GET)
  public Message queryDataSourceWithConnectParms(
      @RequestParam(value = "system", required = false) String createSystem,
      @RequestParam(value = "name", required = false) String dataSourceName,
      @RequestParam(value = "typeId", required = false) Long dataSourceTypeId,
      @RequestParam(value = "identifies", required = false) String identifies,
      @RequestParam(value = "currentPage", required = false) Integer currentPage,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String permissionUser = ModuleUserUtils.getOperationUser(request, "queryDataSource");

          DataSourceVo dataSourceVo =
              new DataSourceVo(dataSourceName, dataSourceTypeId, identifies, createSystem);
          dataSourceVo.setCurrentPage(null != currentPage ? currentPage : 1);
          dataSourceVo.setPageSize(null != pageSize ? pageSize : 10);

          if (AuthContext.isAdministrator(permissionUser)) {
            permissionUser = null;
          }
          dataSourceVo.setPermissionUser(permissionUser);
          PageInfo<DataSource> pageInfo =
              dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
          List<DataSource> queryList = pageInfo.getList();
          for (DataSource dataSource : queryList) {
            DataSource dataSourceConnect =
                dataSourceInfoService.getDataSourceInfoForConnect(dataSource.getDataSourceName());
            if (dataSourceConnect == null) {
              return Message.error("No Exists The DataSource [不存在该数据源]");
            }
            Map<String, Object> connectParams = dataSourceConnect.getConnectParams();
            List<DataSourceParamKeyDefinition> keyDefinitionList =
                dataSourceRelateService.getKeyDefinitionsByType(
                    dataSourceConnect.getDataSourceTypeId());
            if (!AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()) {
              RestfulApiHelper.decryptPasswordKey(keyDefinitionList, connectParams);
            }
            connectParams.remove(AESUtils.PASSWORD);
            dataSource.setConnectParams(connectParams);
          }
          return Message.ok().data("queryList", queryList).data("totalPage", pageInfo.getTotal());
        },
        "Fail to query page of data source[查询数据源失败]");
  }

  @ApiOperation(
      value = "encryptDatasourcePassword",
      notes = "encrypt datasource password",
      response = Message.class)
  @RequestMapping(value = "/encrypt", method = RequestMethod.GET)
  public Message encryptDatasourcePassword(
      @RequestParam(value = "isEncrypt", required = false) String isEncrypt,
      HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          if (AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()
              && StringUtils.isNotBlank(isEncrypt)) {
            // 处理linkis_ps_dm_datasource表和处理linkis_ps_dm_datasource_version的password字段加密
            String permissionUser = ModuleUserUtils.getOperationUser(request, "encrypt");
            DataSourceVo dataSourceVo = new DataSourceVo();
            dataSourceVo.setCurrentPage(1);
            dataSourceVo.setPageSize(10000);
            if (AuthContext.isAdministrator(permissionUser)) {
              permissionUser = null;
            }
            dataSourceVo.setPermissionUser(permissionUser);
            dataSourceInfoService
                .queryDataSourceInfoPage(dataSourceVo)
                .getList()
                .forEach(s -> dealDatasoueceData(s, isEncrypt));
          }
          return Message.ok();
        },
        "Fail to aes of data source[加密数据源密码失败]");
  }

  @ApiOperation(
      value = "getDataSourceByTypeName",
      notes = "get data source by datasource type name",
      response = Message.class)
  @RequestMapping(value = "/info-by-type", method = RequestMethod.GET)
  public Message getDataSourceListByTypes(
      HttpServletRequest request,
      @RequestParam String typeName,
      @RequestParam(required = false, defaultValue = "1") Integer currentPage,
      @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(request, "getDataSourceByTypeName");
          if (AuthContext.isAdministrator(userName)) {
            userName = null;
          }
          DataSourceType targetDataSourceType = getDatasoutceTypeID(typeName, request);
          if (targetDataSourceType != null) {
            DataSourceVo dataSourceVo = new DataSourceVo();
            dataSourceVo.setDataSourceTypeId(Long.valueOf(targetDataSourceType.getId()));
            dataSourceVo.setPermissionUser(userName);
            dataSourceVo.setCurrentPage(currentPage);
            dataSourceVo.setPageSize(pageSize);
            PageInfo<DataSource> pageInfo =
                dataSourceInfoService.queryDataSourceInfoPage(dataSourceVo);
            List<DataSource> queryList = pageInfo.getList();
            return Message.ok().data("queryList", queryList).data("totalPage", pageInfo.getTotal());
          } else {
            return Message.error("No data source type found with name: " + typeName);
          }
        },
        "Fail to get all types of data source[获取数据源列表失败]");
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

    Map<String, Object> connectParams = dataSource.getConnectParams();
    formatConnectParams(keyDefinitionList, connectParams);

    for (DataSourceParamsHook hook : dataSourceParamsHooks) {
      hook.beforePersist(connectParams, keyDefinitionList);
    }
    String parameter = Json.toJson(connectParams, null);
    dataSource.setParameter(parameter);
    dataSourceInfoService.saveDataSourceInfo(dataSource);
  }

  private void formatConnectParams(
      List<DataSourceParamKeyDefinition> keyDefinitionList, Map<String, Object> connectParams) {
    // add default value filed
    keyDefinitionList.forEach(
        keyDefinition -> {
          String key = keyDefinition.getKey();
          if (StringUtils.isNotBlank(keyDefinition.getDefaultValue())
              && !connectParams.containsKey(key)) {
            connectParams.put(key, keyDefinition.getDefaultValue());
            logger.info(
                "connectParams put key:{} with default value:{}",
                key,
                keyDefinition.getDefaultValue());
          }
        });

    connectParams.forEach(
        (k, v) -> {
          if (v instanceof String) {
            connectParams.put(k, v.toString().trim());
            if (!k.equals(AESUtils.PASSWORD)) {
              logger.info("connectParams put key:{} with value:{}", k, v.toString().trim());
            }
          }
        });
  }

  private DataSourceType getDatasoutceTypeID(
      String dataSourceTypeName, HttpServletRequest request) {
    List<DataSourceType> dataSourceTypes =
        dataSourceRelateService.getAllDataSourceTypes(request.getHeader("Content-Language"));
    return dataSourceTypes.stream()
        .filter(type -> type.getName().equals(dataSourceTypeName))
        .findFirst()
        .orElse(null);
  }

  private DataSource insertDatasource(DataSource dataSource, String userName) {
    // Bean validation
    Set<ConstraintViolation<DataSource>> result = beanValidator.validate(dataSource, Default.class);
    if (result.size() > 0) {
      throw new ConstraintViolationException(result);
    }
    // Escape the data source name
    dataSource.setCreateUser(userName);

    Map<String, Object> connectParams = dataSource.getConnectParams();
    if (AESUtils.LINKIS_DATASOURCE_AES_SWITCH.getValue()
        && connectParams.containsKey(AESUtils.PASSWORD)) {
      dataSource
          .getConnectParams()
          .replace(
              AESUtils.PASSWORD,
              AESUtils.encrypt(
                  connectParams.get(AESUtils.PASSWORD).toString(),
                  AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue()));
      // 标记密码已经加密
      dataSource.getConnectParams().put(AESUtils.IS_ENCRYPT, AESUtils.ENCRYPT);
    }
    insertDataSource(dataSource);
    return dataSource;
  }

  private void dealDatasoueceData(DataSource dataSourceInfo, String isEncrypt) {
    DataSource dataSourceInfoBrief =
        dataSourceInfoService.getDataSourceInfoBrief(dataSourceInfo.getId());
    if (StringUtils.isNotBlank(dataSourceInfoBrief.getParameter())
        && dataSourceInfoBrief.getParameter().contains(AESUtils.PASSWORD)) {
      Map datasourceParmMap =
          BDPJettyServerHelper.gson()
              .fromJson(dataSourceInfoBrief.getParameter().toString(), Map.class);
      if (!datasourceParmMap
              .getOrDefault(AESUtils.IS_ENCRYPT, AESUtils.DECRYPT)
              .equals(AESUtils.ENCRYPT)
          && isEncrypt.equals(AESUtils.ENCRYPT)) {
        datasourceParmMap.put(
            AESUtils.PASSWORD,
            AESUtils.encrypt(
                datasourceParmMap.get(AESUtils.PASSWORD).toString(),
                AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue()));
        datasourceParmMap.put(AESUtils.IS_ENCRYPT, AESUtils.ENCRYPT);
        dataSourceInfoBrief.setParameter(BDPJettyServerHelper.gson().toJson(datasourceParmMap));
        dataSourceInfoService.updateDataSourceInfo(dataSourceInfoBrief);
      }
      if (datasourceParmMap
              .getOrDefault(AESUtils.IS_ENCRYPT, AESUtils.DECRYPT)
              .equals(AESUtils.ENCRYPT)
          && isEncrypt.equals(AESUtils.DECRYPT)) {
        datasourceParmMap.put(
            AESUtils.PASSWORD,
            AESUtils.decrypt(
                datasourceParmMap.get(AESUtils.PASSWORD).toString(),
                AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue()));
        datasourceParmMap.remove(AESUtils.IS_ENCRYPT);
        dataSourceInfoBrief.setParameter(BDPJettyServerHelper.gson().toJson(datasourceParmMap));
        dataSourceInfoService.updateDataSourceInfo(dataSourceInfoBrief);
      }
      dealDatasoueceVersionData(dataSourceInfoBrief, isEncrypt);
    }
  }

  private void dealDatasoueceVersionData(DataSource dataSourceInfo, String isEncrypt) {
    // 处理linkis_ps_dm_datasource_version中的password,解密base64，加密AES
    List<DatasourceVersion> datasourceVersionList =
        dataSourceVersionDao.getVersionsFromDatasourceId(dataSourceInfo.getId());
    datasourceVersionList.forEach(
        datasourceVersion -> {
          // 加密
          if (StringUtils.isNotBlank(datasourceVersion.getParameter())
              && datasourceVersion.getParameter().contains(AESUtils.PASSWORD)) {
            Map datasourceVersionMap =
                BDPJettyServerHelper.gson().fromJson(datasourceVersion.getParameter(), Map.class);
            if (!datasourceVersionMap
                    .getOrDefault(AESUtils.IS_ENCRYPT, AESUtils.DECRYPT)
                    .equals(AESUtils.ENCRYPT)
                && isEncrypt.equals(AESUtils.ENCRYPT)) {
              try {
                Object password =
                    CryptoUtils.string2Object(
                        datasourceVersionMap.get(AESUtils.PASSWORD).toString());
                datasourceVersionMap.put(
                    AESUtils.PASSWORD,
                    AESUtils.encrypt(
                        password.toString(), AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue()));
                datasourceVersionMap.put(AESUtils.IS_ENCRYPT, AESUtils.ENCRYPT);
                datasourceVersion.setParameter(
                    BDPJettyServerHelper.gson().toJson(datasourceVersionMap));
                dataSourceVersionDao.updateByDatasourceVersion(datasourceVersion);
              } catch (Exception e) {
                logger.warn(
                    "error encrypt  linkis_ps_dm_datasource_version id :"
                        + datasourceVersion.getDatasourceId()
                        + " version:"
                        + datasourceVersion.getVersionId());
              }
            }
            // 解密
            if (datasourceVersionMap
                    .getOrDefault(AESUtils.IS_ENCRYPT, AESUtils.DECRYPT)
                    .equals(AESUtils.ENCRYPT)
                && isEncrypt.equals(AESUtils.DECRYPT)) {
              try {
                String password = datasourceVersionMap.get(AESUtils.PASSWORD).toString();
                String decryptPassword =
                    AESUtils.decrypt(password, AESUtils.LINKIS_DATASOURCE_AES_KEY.getValue());
                datasourceVersionMap.put(
                    AESUtils.PASSWORD, CryptoUtils.object2String(decryptPassword));
                datasourceVersionMap.remove(AESUtils.IS_ENCRYPT);
                datasourceVersion.setParameter(
                    BDPJettyServerHelper.gson().toJson(datasourceVersionMap));
                dataSourceVersionDao.updateByDatasourceVersion(datasourceVersion);
              } catch (Exception e) {
                logger.warn(
                    "error encrypt  linkis_ps_dm_datasource_version id :"
                        + datasourceVersion.getDatasourceId()
                        + " version:"
                        + datasourceVersion.getVersionId());
              }
            }
          }
        });
  }
}
