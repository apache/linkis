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

package org.apache.linkis.basedatamanager.server.restful;

import org.apache.linkis.basedatamanager.server.domain.RmExternalResourceProviderEntity;
import org.apache.linkis.basedatamanager.server.service.RmExternalResourceProviderService;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "RmExternalResourceProviderRestfulApi")
@RestController
@RequestMapping(path = "/basedata-manager/rm-external-resource-provider")
public class RmExternalResourceProviderRestfulApi {

  @Autowired RmExternalResourceProviderService rmExternalResourceProviderService;

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize")
  })
  @ApiOperation(
      value = "list",
      notes = "list Resource manager External Resource Providers",
      httpMethod = "GET")
  @RequestMapping(path = "", method = RequestMethod.GET)
  public Message list(
      HttpServletRequest request, String searchName, Integer currentPage, Integer pageSize) {
    ModuleUserUtils.getOperationUser(
        request,
        "Query list data of Resource manager External Resource Provider,search name:" + searchName);
    PageInfo pageList =
        rmExternalResourceProviderService.getListByPage(searchName, currentPage, pageSize);
    return Message.ok("").data("list", pageList);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(
      value = "get",
      notes = "Get a Resource manager External Resource Provider Record by id",
      httpMethod = "GET")
  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public Message get(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(
        request, "Get a Resource manager External Resource Provider Record,id:" + id.toString());
    RmExternalResourceProviderEntity rmExternalResourceProvider =
        rmExternalResourceProviderService.getById(id);
    return Message.ok("").data("item", rmExternalResourceProvider);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "body",
        dataType = "RmExternalResourceProviderEntity",
        name = "rmExternalResourceProvider")
  })
  @ApiOperation(
      value = "add",
      notes = "Add a Resource manager External Resource Provider Record",
      httpMethod = "POST")
  @RequestMapping(path = "", method = RequestMethod.POST)
  public Message add(
      HttpServletRequest request,
      @RequestBody RmExternalResourceProviderEntity rmExternalResourceProvider) {
    String username =
        ModuleUserUtils.getOperationUser(
            request,
            "Add a Resource manager External Resource Provider Record,"
                + rmExternalResourceProvider.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = rmExternalResourceProviderService.save(rmExternalResourceProvider);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(
      value = "remove",
      notes = "Remove a Resource manager External Resource Provider Record by id",
      httpMethod = "DELETE")
  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  public Message remove(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(
        request, "Remove a Resource manager External Resource Provider Record,id:" + id.toString());
    boolean result = rmExternalResourceProviderService.removeById(id);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "body",
        dataType = "RmExternalResourceProviderEntity",
        name = "rmExternalResourceProvider")
  })
  @ApiOperation(
      value = "update",
      notes = "Update a Resource manager External Resource Provider Record",
      httpMethod = "PUT")
  @RequestMapping(path = "", method = RequestMethod.PUT)
  public Message update(
      HttpServletRequest request,
      @RequestBody RmExternalResourceProviderEntity rmExternalResourceProvider) {
    String username =
        ModuleUserUtils.getOperationUser(
            request,
            "Update a Resource manager External Resource Provider Record,id:"
                + rmExternalResourceProvider.getId().toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = rmExternalResourceProviderService.updateById(rmExternalResourceProvider);
    return Message.ok("").data("result", result);
  }
}
