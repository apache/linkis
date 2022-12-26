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
import org.apache.linkis.datasourcemanager.common.domain.DataSourceEnv;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.core.formdata.FormDataTransformerFactory;
import org.apache.linkis.datasourcemanager.core.formdata.MultiPartFormDataTransformer;
import org.apache.linkis.datasourcemanager.core.service.DataSourceInfoService;
import org.apache.linkis.datasourcemanager.core.service.DataSourceRelateService;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidator;
import org.apache.linkis.datasourcemanager.core.vo.DataSourceEnvVo;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import java.util.*;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "data source admin restful api")
@RestController
@RequestMapping(
    value = "/data-source-manager",
    produces = {"application/json"})
public class DataSourceAdminRestfulApi {

  private final List<String> permitSystemList = Arrays.asList("Qualitis");
  @Autowired private DataSourceInfoService dataSourceInfoService;

  @Autowired private DataSourceRelateService dataSourceRelateService;

  @Autowired private ParameterValidator parameterValidator;

  @Autowired private Validator beanValidator;

  private MultiPartFormDataTransformer formDataTransformer;

  @PostConstruct
  public void initRestful() {
    this.formDataTransformer = FormDataTransformerFactory.buildCustom();
  }

  @ApiOperation(value = "insertJsonEnv", notes = "insert json env", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"dataSourceEnv"})
  @RequestMapping(value = "/env/json", method = RequestMethod.POST)
  public Message insertJsonEnv(@RequestBody DataSourceEnv dataSourceEnv, HttpServletRequest req)
      throws ErrorException {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName = ModuleUserUtils.getOperationUser(req, "insertJsonEnv");
          if (RestfulApiHelper.isNotAdminUser(userName)) {
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
          }
          // Bean validation
          Set<ConstraintViolation<DataSourceEnv>> result =
              beanValidator.validate(dataSourceEnv, Default.class);
          if (result.size() > 0) {
            throw new ConstraintViolationException(result);
          }
          if (dataSourceInfoService.existDataSourceEnv(dataSourceEnv.getEnvName())) {
            return Message.error(
                "The data source env named: "
                    + dataSourceEnv.getEnvName()
                    + " has been existed [数据源环境: "
                    + dataSourceEnv.getEnvName()
                    + " 已经存在]");
          }
          dataSourceEnv.setCreateUser(userName);
          insertDataSourceEnv(dataSourceEnv);
          return Message.ok().data("insertId", dataSourceEnv.getId());
        },
        "Fail to insert data source environment[新增数据源环境失败]");
  }

  @ApiOperation(
      value = "insertJsonEnvBatch",
      notes = "insert batch json env",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"dataSourceEnvList", "system"})
  @RequestMapping(value = "/env/json/batch", method = RequestMethod.POST)
  public Message insertJsonEnvBatch(
      @RequestBody List<DataSourceEnv> dataSourceEnvList,
      @RequestParam("system") String system,
      HttpServletRequest req)
      throws ErrorException {
    String userName = ModuleUserUtils.getOperationUser(req, "insertJsonEnvBatch");
    if (RestfulApiHelper.isNotAdminUser(userName) && !permitSystemList.contains(system)) {
      return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
    }
    for (DataSourceEnv dataSourceEnv : dataSourceEnvList) {
      // Bean validation
      Set<ConstraintViolation<DataSourceEnv>> result =
          beanValidator.validate(dataSourceEnv, Default.class);
      if (result.size() > 0) {
        throw new ConstraintViolationException(result);
      }
      if (dataSourceInfoService.existDataSourceEnv(dataSourceEnv.getEnvName())) {
        return Message.error(
            "The data source env named: "
                + dataSourceEnv.getEnvName()
                + " has been existed [数据源环境: "
                + dataSourceEnv.getEnvName()
                + " 已经存在]");
      }
      dataSourceEnv.setCreateUser(userName);
      // Get key definitions in environment scope
      List<DataSourceParamKeyDefinition> keyDefinitionList =
          dataSourceRelateService.getKeyDefinitionsByType(
              dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
      dataSourceEnv.setKeyDefinitions(keyDefinitionList);
      Map<String, Object> connectParams = dataSourceEnv.getConnectParams();
      // Validate connect parameters
      parameterValidator.validate(keyDefinitionList, connectParams);
    }
    dataSourceInfoService.saveBatchDataSourceEnv(dataSourceEnvList);
    return RestfulApiHelper.doAndResponse(
        () -> Message.ok().data("envs", dataSourceEnvList),
        "Fail to insert data source environment[新增数据源环境失败]");
  }

  @ApiOperation(
      value = "updateJsonEnvBatch",
      notes = "update batch json env",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"dataSourceEnvList", "system"})
  @RequestMapping(value = "/env/json/batch", method = RequestMethod.PUT)
  public Message updateEnvBatch(
      @RequestBody List<DataSourceEnv> dataSourceEnvList,
      @RequestParam("system") String system,
      HttpServletRequest request)
      throws ErrorException {
    String userName = ModuleUserUtils.getOperationUser(request, "updateJsonEnvBatch");
    if (RestfulApiHelper.isNotAdminUser(userName) && !permitSystemList.contains(system)) {
      return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
    }
    for (DataSourceEnv dataSourceEnv : dataSourceEnvList) {
      if (Objects.isNull(dataSourceEnv.getId())) {
        return Message.error(
            "Fail to update data source environment[更新数据源环境失败], "
                + "[Please check the id if exists']");
      }
      // Bean validation
      Set<ConstraintViolation<DataSourceEnv>> result =
          beanValidator.validate(dataSourceEnv, Default.class);
      if (result.size() > 0) {
        throw new ConstraintViolationException(result);
      }
      Long envId = dataSourceEnv.getId();
      dataSourceEnv.setModifyUser(userName);
      dataSourceEnv.setModifyTime(Calendar.getInstance().getTime());
      DataSourceEnv storedDataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
      if (null == storedDataSourceEnv) {
        return Message.error(
            "Fail to update data source environment[更新数据源环境失败], "
                + "[Please check the id:'"
                + envId
                + " is correct ']");
      }
      if (!Objects.equals(dataSourceEnv.getEnvName(), storedDataSourceEnv.getEnvName())
          && dataSourceInfoService.existDataSourceEnv(dataSourceEnv.getEnvName())) {
        return Message.error(
            "The data source env named: "
                + dataSourceEnv.getEnvName()
                + " has been existed [数据源环境: "
                + dataSourceEnv.getEnvName()
                + " 已经存在]");
      }
      dataSourceEnv.setCreateUser(storedDataSourceEnv.getCreateUser());
      // Get key definitions in environment scope
      List<DataSourceParamKeyDefinition> keyDefinitionList =
          dataSourceRelateService.getKeyDefinitionsByType(
              dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
      dataSourceEnv.setKeyDefinitions(keyDefinitionList);
      Map<String, Object> connectParams = dataSourceEnv.getConnectParams();
      // Validate connect parameters
      parameterValidator.validate(keyDefinitionList, connectParams);
    }
    dataSourceInfoService.updateBatchDataSourceEnv(dataSourceEnvList);
    return RestfulApiHelper.doAndResponse(
        () -> Message.ok().data("envs", dataSourceEnvList),
        "Fail to update data source environment[更新数据源环境失败]");
  }

  @ApiOperation(
      value = "getAllEnvListByDataSourceType",
      notes = "get all env list by data source type",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "typeId", required = true, dataType = "Long", value = "type id")
  })
  @RequestMapping(value = "/env-list/all/type/{typeId}", method = RequestMethod.GET)
  public Message getAllEnvListByDataSourceType(@PathVariable("typeId") Long typeId) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          List<DataSourceEnv> envList = dataSourceInfoService.listDataSourceEnvByType(typeId);
          return Message.ok().data("envList", envList);
        },
        "Fail to get data source environment list[获取数据源环境清单失败]");
  }

  @ApiOperation(
      value = "getEnvEntityById",
      notes = "get env entity by id",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "envId", required = true, dataType = "Long", value = "env id")
  })
  @RequestMapping(value = "/env/{envId}", method = RequestMethod.GET)
  public Message getEnvEntityById(@PathVariable("envId") Long envId) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          DataSourceEnv dataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
          return Message.ok().data("env", dataSourceEnv);
        },
        "Fail to get data source environment[获取数据源环境信息失败]");
  }

  @ApiOperation(value = "removeEnvEntity", notes = "remove env entity", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "envId", required = true, dataType = "Long", value = "env id")
  })
  @RequestMapping(value = "/env/{envId}", method = RequestMethod.DELETE)
  public Message removeEnvEntity(@PathVariable("envId") Long envId, HttpServletRequest request) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(request, "removeEnvEntity,envId:" + envId);
          if (RestfulApiHelper.isNotAdminUser(userName)) {
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
          }
          Long removeId = dataSourceInfoService.removeDataSourceEnv(envId);
          if (removeId < 0) {
            return Message.error(
                "Fail to remove data source environment[删除数据源环境信息失败], [id:" + envId + "]");
          }
          return Message.ok().data("removeId", removeId);
        },
        "Fail to remove data source environment[删除数据源环境信息失败]");
  }

  @ApiOperation(value = "updateJsonEnv", notes = "update json env", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "envId", required = true, dataType = "Long", value = "env id")
  })
  @ApiOperationSupport(includeParameters = {"dataSourceEnv"})
  @RequestMapping(value = "/env/{envId}/json", method = RequestMethod.PUT)
  public Message updateJsonEnv(
      @RequestBody DataSourceEnv dataSourceEnv,
      @PathVariable("envId") Long envId,
      HttpServletRequest request)
      throws ErrorException {
    return RestfulApiHelper.doAndResponse(
        () -> {
          String userName =
              ModuleUserUtils.getOperationUser(request, "updateJsonEnv,envId:" + envId);
          if (RestfulApiHelper.isNotAdminUser(userName)) {
            return Message.error("User '" + userName + "' is not admin user[非管理员用户]");
          }
          // Bean validation
          Set<ConstraintViolation<DataSourceEnv>> result =
              beanValidator.validate(dataSourceEnv, Default.class);
          if (result.size() > 0) {
            throw new ConstraintViolationException(result);
          }
          dataSourceEnv.setId(envId);
          dataSourceEnv.setModifyUser(userName);
          dataSourceEnv.setModifyTime(Calendar.getInstance().getTime());
          DataSourceEnv storedDataSourceEnv = dataSourceInfoService.getDataSourceEnv(envId);
          if (null == storedDataSourceEnv) {
            return Message.error(
                "Fail to update data source environment[更新数据源环境失败], "
                    + "[Please check the id:'"
                    + envId
                    + " is correct ']");
          }
          if (!Objects.equals(dataSourceEnv.getEnvName(), storedDataSourceEnv.getEnvName())
              && dataSourceInfoService.existDataSourceEnv(dataSourceEnv.getEnvName())) {
            return Message.error(
                "The data source env named: "
                    + dataSourceEnv.getEnvName()
                    + " has been existed [数据源环境: "
                    + dataSourceEnv.getEnvName()
                    + " 已经存在]");
          }
          dataSourceEnv.setCreateUser(storedDataSourceEnv.getCreateUser());
          updateDataSourceEnv(dataSourceEnv, storedDataSourceEnv);
          return Message.ok().data("updateId", envId);
        },
        "Fail to update data source environment[更新数据源环境失败]");
  }

  @ApiOperation(
      value = "queryDataSourceEnv",
      notes = "query data source env",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "name", required = true, dataType = "Long", value = "name"),
    @ApiImplicitParam(name = "typeId", required = true, dataType = "Long", value = "type id"),
    @ApiImplicitParam(name = "currentPage", required = true, dataType = "Long"),
    @ApiImplicitParam(name = "pageSize", required = true, dataType = "Long", value = "page size")
  })
  @RequestMapping(value = "/env", method = RequestMethod.GET)
  public Message queryDataSourceEnv(
      @RequestParam(value = "name", required = false) String envName,
      @RequestParam(value = "typeId", required = false) Long dataSourceTypeId,
      @RequestParam(value = "currentPage", required = false) Integer currentPage,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    return RestfulApiHelper.doAndResponse(
        () -> {
          DataSourceEnvVo dataSourceEnvVo = new DataSourceEnvVo(envName, dataSourceTypeId);
          dataSourceEnvVo.setCurrentPage(null != currentPage ? currentPage : 1);
          dataSourceEnvVo.setPageSize(null != pageSize ? pageSize : 10);
          List<DataSourceEnv> queryList =
              dataSourceInfoService.queryDataSourceEnvPage(dataSourceEnvVo);
          return Message.ok().data("queryList", queryList);
        },
        "Fail to query page of data source environment[查询数据源环境失败]");
  }

  /**
   * Inner method to insert data source environment
   *
   * @param dataSourceEnv data source environment entity
   * @throws ErrorException
   */
  private void insertDataSourceEnv(DataSourceEnv dataSourceEnv) throws ErrorException {
    // Get key definitions in environment scope
    List<DataSourceParamKeyDefinition> keyDefinitionList =
        dataSourceRelateService.getKeyDefinitionsByType(
            dataSourceEnv.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
    dataSourceEnv.setKeyDefinitions(keyDefinitionList);
    Map<String, Object> connectParams = dataSourceEnv.getConnectParams();
    // Validate connect parameters
    parameterValidator.validate(keyDefinitionList, connectParams);
    dataSourceInfoService.saveDataSourceEnv(dataSourceEnv);
  }

  /**
   * Inner method to update data source environment
   *
   * @param updatedOne new entity
   * @param storedOne old entity
   * @throws ErrorException
   */
  private void updateDataSourceEnv(DataSourceEnv updatedOne, DataSourceEnv storedOne)
      throws ErrorException {
    // Get key definitions in environment scope
    List<DataSourceParamKeyDefinition> keyDefinitionList =
        dataSourceRelateService.getKeyDefinitionsByType(
            updatedOne.getDataSourceTypeId(), DataSourceParamKeyDefinition.Scope.ENV);
    updatedOne.setKeyDefinitions(keyDefinitionList);
    Map<String, Object> connectParams = updatedOne.getConnectParams();
    // Validate connect parameters
    parameterValidator.validate(keyDefinitionList, connectParams);
    dataSourceInfoService.updateDataSourceEnv(updatedOne, storedOne);
  }
}
