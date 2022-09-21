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

package org.apache.linkis.configuration.restful.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.configuration.entity.TenantVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TenantConfigService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api
@RestController
@RequestMapping(path = "/configuration/tenant-mapping")
public class TenantConfigrationRestfulApi {

    private static final Logger logger = LoggerFactory.getLogger(TenantConfigrationRestfulApi.class);

    @Autowired
    private TenantConfigService tenantConfigService;

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "req", value = ""),
            @ApiImplicitParam(paramType = "body", dataType = "TenantVo", name = "tenantVo", value = "tenantVo")
    })
    @ApiOperation(value = "create-tenant", notes = "create tenant", httpMethod = "POST", response = Message.class)
    @RequestMapping(path = "/create-tenant", method = RequestMethod.POST)
    public Message createTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "createTenant");
            if (!Configuration.isAdmin(userName)) {
                return Message.error("Failed to create-tenant,msg: only administrators can configure");
            }
            tenantConfigService.createTenant(tenantVo);
        } catch (DuplicateKeyException e) {
            return Message.error("Failed to create-tenant,msg:create user-creator is existed");
        } catch (ConfigurationException e) {
            return Message.error("Failed to update-tenant,msg:" + e.getMessage());
        }
        return Message.ok();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "req", value = ""),
            @ApiImplicitParam(paramType = "body", dataType = "TenantVo", name = "tenantVo", value = "tenantVo")
    })
    @ApiOperation(value = "update-tenant", notes = "update tenant", httpMethod = "POST", response = Message.class)
    @RequestMapping(path = "/update-tenant", method = RequestMethod.POST)
    public Message updateTenant(HttpServletRequest req, @RequestBody TenantVo tenantVo) {
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "updateTenant");
            if (!Configuration.isAdmin(userName)) {
                return Message.error("Failed to update-tenant,msg: only administrators can configure");
            }
            tenantConfigService.updateTenant(tenantVo);
        } catch (ConfigurationException e) {
            return Message.error("Failed to update-tenant,msg:" + e.getMessage());
        }
        return Message.ok();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "req", value = ""),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "id", value = "id")
    })
    @ApiOperation(value = "delete-tenant", notes = "delete tenant", httpMethod = "GET", response = Message.class)
    @RequestMapping(path = "/delete-tenant", method = RequestMethod.GET)
    public Message deleteTenant(HttpServletRequest req, Integer id) {
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "deleteTenant");
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
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "req", value = ""),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "user", value = "user"),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "creator", value = "creator"),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "tenantValue", value = "tenantValue")
    })
    @ApiOperation(value = "query-tenant-list", notes = "query tenant list", httpMethod = "GET", response = Message.class)
    @RequestMapping(path = "/query-tenant-list", method = RequestMethod.GET)
    public Message queryTenantList(HttpServletRequest req, String user, String creator, String tenantValue) {
        String userName = ModuleUserUtils.getOperationUser(req, "queryTenantList");
        if (!Configuration.isAdmin(userName)) {
            return Message.error("Failed to query-tenant-list,msg: only administrators can configure");
        }
        return Message.ok().data("tenantList", tenantConfigService.queryTenantList(user, creator, tenantValue));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "req", value = ""),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "user", value = "user"),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "creator", value = "creator"),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "tenantValue", value = "tenantValue")
    })
    @ApiOperation(value = "check-user-creator", notes = "check user creator", httpMethod = "GET", response = Message.class)
    @RequestMapping(path = "/check-user-creator", method = RequestMethod.GET)
    public Message checkUserCreator(HttpServletRequest req, String user, String creator, String tenantValue) {
        Boolean result = false;
        try {
            String userName = ModuleUserUtils.getOperationUser(req, "checkUserCreator");
            if (!Configuration.isAdmin(userName)) {
                return Message.error("Failed to check-user-creator,msg: only administrators can configure");
            }
            result = tenantConfigService.checkUserCteator(user, creator, tenantValue);
        } catch (ConfigurationException e) {
            return Message.error("Failed to check-user-creator,msg:" + e.getMessage());
        }
        return Message.ok().data("result", result);
    }
}