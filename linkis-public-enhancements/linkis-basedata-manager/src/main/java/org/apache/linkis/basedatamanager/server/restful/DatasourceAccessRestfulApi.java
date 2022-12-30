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

import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceAccessService;
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

import java.util.Date;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "DatasourceAccessRestfulApi")
@RestController
@RequestMapping(path = "/basedata-manager/datasource-access")
public class DatasourceAccessRestfulApi {

  @Autowired DatasourceAccessService datasourceAccessService;

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request"),
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize")
  })
  @ApiOperation(value = "list", notes = "list Datasource Accesses", httpMethod = "GET")
  @RequestMapping(path = "", method = RequestMethod.GET)
  public Message list(
      HttpServletRequest request, String searchName, Integer currentPage, Integer pageSize) {
    ModuleUserUtils.getOperationUser(
        request, "Query list data of Datasource Access,search name:" + searchName);
    PageInfo pageList = datasourceAccessService.getListByPage(searchName, currentPage, pageSize);
    return Message.ok("").data("list", pageList);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request"),
    @ApiImplicitParam(paramType = "path", dataType = "long", name = "id")
  })
  @ApiOperation(value = "get", notes = "Get a Datasource Access Record", httpMethod = "GET")
  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public Message get(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(request, "Get a Datasource Access Record,id:" + id.toString());
    DatasourceAccessEntity datasourceAccess = datasourceAccessService.getById(id);
    return Message.ok("").data("item", datasourceAccess);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request"),
    @ApiImplicitParam(
        paramType = "body",
        dataType = "DatasourceAccessEntity",
        name = "datasourceAccess")
  })
  @ApiOperation(value = "add", notes = "", httpMethod = "POST")
  @RequestMapping(path = "", method = RequestMethod.POST)
  public Message add(
      HttpServletRequest request, @RequestBody DatasourceAccessEntity datasourceAccess) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Add a Datasource Access Record," + datasourceAccess.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    datasourceAccess.setAccessTime(new Date());
    boolean result = datasourceAccessService.save(datasourceAccess);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request"),
    @ApiImplicitParam(paramType = "path", dataType = "long", name = "id")
  })
  @ApiOperation(
      value = "remove",
      notes = "Remove a Datasource Access Record",
      httpMethod = "DELETE")
  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  public Message remove(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(
        request, "Remove a Datasource Access Record,id:" + id.toString());
    boolean result = datasourceAccessService.removeById(id);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "HttpServletRequest", name = "request"),
    @ApiImplicitParam(
        paramType = "body",
        dataType = "DatasourceAccessEntity",
        name = "datasourceAccess")
  })
  @ApiOperation(value = "update", notes = "Update a Datasource Access Record", httpMethod = "PUT")
  @RequestMapping(path = "", method = RequestMethod.PUT)
  public Message update(
      HttpServletRequest request, @RequestBody DatasourceAccessEntity datasourceAccess) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Update a Datasource Access Record,id:" + datasourceAccess.getId().toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = datasourceAccessService.updateById(datasourceAccess);
    return Message.ok("").data("result", result);
  }
}
