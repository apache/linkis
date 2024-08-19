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

package org.apache.linkis.configuration.restful.api;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.configuration.entity.DepartmentTenantVo;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api
@RestController
@RequestMapping(path = "/configuration/tenant-mapping")
public class TenantConfigrationRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(TenantConfigrationRestfulApi.class);

  @Autowired private TenantConfigService tenantConfigService;

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(
        paramType = "body",
        dataType = "TenantVo",
        name = "tenantVo",
        value = "tenantVo")
  })
  @ApiOperation(
      value = "create-tenant",
      notes = "create tenant",
      httpMethod = "POST",
      response = Message.class)
  @RequestMapping(path = "/create-tenant", method = RequestMethod.POST)
  public Message createTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "execute createTenant");
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to create-tenant,msg: only administrators can configure");
      }
      parameterVerification(tenantVo);
      if (tenantConfigService.isExist(tenantVo.getUser(), tenantVo.getCreator())) {
        throw new ConfigurationException("User-creator is existed");
      }
      tenantConfigService.createTenant(tenantVo);
    } catch (DuplicateKeyException e) {
      return Message.error("Failed to create-tenant,msg:create user-creator is existed");
    } catch (ConfigurationException e) {
      return Message.error("Failed to create-tenant,msg:" + e.getMessage());
    }
    return Message.ok();
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(
        paramType = "body",
        dataType = "TenantVo",
        name = "tenantVo",
        value = "tenantVo")
  })
  @ApiOperation(
      value = "update-tenant",
      notes = "update tenant",
      httpMethod = "POST",
      response = Message.class)
  @RequestMapping(path = "/update-tenant", method = RequestMethod.POST)
  public Message updateTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "execute updateTenant");
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to update-tenant,msg: only administrators can configure");
      }
      parameterVerification(tenantVo);
      tenantConfigService.updateTenant(tenantVo);
    } catch (ConfigurationException e) {
      return Message.error("Failed to update-tenant,msg:" + e.getMessage());
    }
    return Message.ok();
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "id", value = "id")
  })
  @ApiOperation(
      value = "delete-tenant",
      notes = "delete tenant",
      httpMethod = "GET",
      response = Message.class)
  @RequestMapping(path = "/delete-tenant", method = RequestMethod.GET)
  public Message deleteTenant(HttpServletRequest req, @RequestParam(value = "id") Integer id) {
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "execute deleteTenant,id: " + id);
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to delete-tenant,msg: only administrators can configure");
      }
      tenantConfigService.deleteTenant(id);
    } catch (ConfigurationException e) {
      return Message.error("Failed to delete-tenant,msg:" + e.getMessage());
    }
    return Message.ok();
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "user", value = "user"),
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "creator",
        value = "creator"),
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "tenantValue",
        value = "tenantValue")
  })
  @ApiOperation(
      value = "query-tenant-list",
      notes = "query tenant list",
      httpMethod = "GET",
      response = Message.class)
  @RequestMapping(path = "/query-tenant-list", method = RequestMethod.GET)
  public Message queryTenantList(
      HttpServletRequest req,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "tenantValue", required = false) String tenantValue,
      @RequestParam(value = "pageNow", required = false) Integer pageNow,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    String userName = ModuleUserUtils.getOperationUser(req, "execute queryTenantList");
    if (!Configuration.isAdmin(userName)) {
      return Message.error("Failed to query-tenant-list,msg: only administrators can configure");
    }
    if (StringUtils.isBlank(user)) user = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (StringUtils.isBlank(tenantValue)) tenantValue = null;
    if (null == pageNow) pageNow = 1;
    if (null == pageSize) pageSize = 20;
    Map<String, Object> resultMap =
        tenantConfigService.queryTenantList(user, creator, tenantValue, pageNow, pageSize);
    return Message.ok()
        .data("tenantList", resultMap.get("tenantList"))
        .data(JobRequestConstants.TOTAL_PAGE(), resultMap.get(JobRequestConstants.TOTAL_PAGE()));
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "user", value = "user"),
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "creator",
        value = "creator"),
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "tenantValue",
        value = "tenantValue")
  })
  @ApiOperation(
      value = "check-user-creator",
      notes = "check user creator",
      httpMethod = "GET",
      response = Message.class)
  @RequestMapping(path = "/check-user-creator", method = RequestMethod.GET)
  public Message checkUserCreator(
      HttpServletRequest req,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "creator", required = false) String creator) {
    Boolean result = false;
    try {
      // Parameter verification
      if (StringUtils.isBlank(creator)) {
        throw new ConfigurationException("Application Name can't be empty ");
      }
      if (StringUtils.isBlank(user)) {
        throw new ConfigurationException("User Name can't be empty ");
      }
      String userName = ModuleUserUtils.getOperationUser(req, "checkUserCreator");
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to check-user-creator,msg: only administrators can configure");
      }
      result = tenantConfigService.isExist(user, creator);
    } catch (ConfigurationException e) {
      return Message.error("Failed to check-user-creator,msg:" + e.getMessage());
    }
    return Message.ok().data("exist", result);
  }

  private void parameterVerification(TenantVo tenantVo) throws ConfigurationException {
    // Parameter verification
    if (StringUtils.isBlank(tenantVo.getCreator())) {
      throw new ConfigurationException("Application name can't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getUser())) {
      throw new ConfigurationException("User name can't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getBussinessUser())) {
      throw new ConfigurationException("Creat user can't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getDesc())) {
      throw new ConfigurationException("Description can't be empty ");
    }
    if (StringUtils.isBlank(tenantVo.getTenantValue())) {
      throw new ConfigurationException("Tenant tag can't be empty ");
    }
    if (tenantVo.getCreator().equals("*") && tenantVo.getUser().equals("*")) {
      throw new ConfigurationException("User && Creator cannot be both *");
    }
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "body",
        dataType = "DepartmentTenantVo",
        name = "departmentTenantVo",
        value = "departmentTenantVo")
  })
  @ApiOperation(
      value = "save-department-tenant",
      notes = "save department tenant",
      httpMethod = "POST",
      response = Message.class)
  @RequestMapping(path = "/save-department-tenant", method = RequestMethod.POST)
  public Message saveDepartmentTenant(
      HttpServletRequest req, @RequestBody DepartmentTenantVo departmentTenantVo)
      throws ConfigurationException {
    String userName = ModuleUserUtils.getOperationUser(req, "execute saveDepartmentTenant");
    if (!Configuration.isAdmin(userName)) {
      return Message.error("Failed to save-department-tenant,msg: only administrator users to use");
    }
    if (StringUtils.isBlank(departmentTenantVo.getTenantValue())) {
      return Message.error("tenant can not be empty");
    }
    if (StringUtils.isBlank(departmentTenantVo.getCreator())) {
      return Message.error("creator can not be empty");
    }
    if (StringUtils.isBlank(departmentTenantVo.getDepartmentId())) {
      return Message.error("departmentId can not be empty");
    }
    // Query whether the data exists before executing the update
    if (StringUtils.isBlank(departmentTenantVo.getId())) {
      DepartmentTenantVo departTenant =
          tenantConfigService.queryDepartTenant(
              departmentTenantVo.getCreator(), departmentTenantVo.getDepartmentId());
      if (null != departTenant) {
        return Message.error("department creator is exist");
      }
    }
    tenantConfigService.saveDepartmentTenant(departmentTenantVo);
    return Message.ok();
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "department",
        value = "department"),
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "creator",
        value = "creator"),
    @ApiImplicitParam(
        paramType = "query",
        dataType = "string",
        name = "tenantValue",
        value = "tenantValue"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageNow", value = "pageNow"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "pageSize")
  })
  @ApiOperation(
      value = "query-department-tenant",
      notes = "query department tenant list",
      httpMethod = "GET",
      response = Message.class)
  @RequestMapping(path = "/query-department-tenant", method = RequestMethod.GET)
  public Message queryDepartmentTenant(
      HttpServletRequest req,
      @RequestParam(value = "departmentId", required = false) String departmentId,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "tenantValue", required = false) String tenantValue,
      @RequestParam(value = "pageNow", required = false, defaultValue = "1") Integer pageNow,
      @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
    String userName = ModuleUserUtils.getOperationUser(req, "execute queryDepartmentTenantList");
    if (!Configuration.isAdmin(userName)) {
      return Message.error("Failed to query-tenant-list,msg: only administrator users to use");
    }
    if (StringUtils.isBlank(departmentId)) departmentId = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (StringUtils.isBlank(tenantValue)) tenantValue = null;
    Map<String, Object> resultMap =
        tenantConfigService.queryDepartmentTenant(
            departmentId, creator, tenantValue, pageNow, pageSize);
    return Message.ok()
        .data("tenantList", resultMap.get("tenantList"))
        .data(JobRequestConstants.TOTAL_PAGE(), resultMap.get(JobRequestConstants.TOTAL_PAGE()));
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "id", value = "id")
  })
  @ApiOperation(
      value = "delete-department-tenant",
      notes = "delete department tenant",
      httpMethod = "GET",
      response = Message.class)
  @RequestMapping(path = "/delete-department-tenant", method = RequestMethod.GET)
  public Message deleteDepartmentTenant(
      HttpServletRequest req, @RequestParam(value = "id") Integer id) {
    String userName =
        ModuleUserUtils.getOperationUser(req, "execute deleteDepartmentTenant,id: " + id);
    if (!Configuration.isAdmin(userName)) {
      return Message.error(
          "Failed to delete-department-tenant,msg: only administrator users to use");
    }
    if (StringUtils.isBlank(id.toString())) {
      return Message.error("id can not be empty");
    }
    tenantConfigService.deleteDepartmentTenant(id);
    return Message.ok();
  }

  @ApiOperation(
      value = "query department",
      notes = "query department",
      httpMethod = "GET",
      response = Message.class)
  @RequestMapping(path = "/query-department", method = RequestMethod.GET)
  public Message queryDepartmentList() {
    return Message.ok().data("departmentList", tenantConfigService.queryDepartmentList());
  }
}
