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

import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeKeyEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceTypeKeyService;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(path = "/basedata-manager/datasource-type-key")
public class DatasourceTypeKeyRestfulApi {
  private DatasourceTypeKeyService datasourceTypeKeyService;

  public DatasourceTypeKeyRestfulApi(DatasourceTypeKeyService datasourceTypeKeyService) {
    this.datasourceTypeKeyService = datasourceTypeKeyService;
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize")
  })
  @ApiOperation(value = "list", notes = "list Datasource Type Key", httpMethod = "GET")
  @RequestMapping(path = "", method = RequestMethod.GET)
  public Message list(
      HttpServletRequest request,
      @RequestParam(value = "searchName", required = false) String searchName,
      @RequestParam(value = "dataSourceTypeId", required = false) Integer dataSourceTypeId,
      Integer currentPage,
      Integer pageSize) {

    ModuleUserUtils.getOperationUser(
        request, "Query list data of Datasource Type Key,search name:" + searchName);
    PageInfo pageList =
        datasourceTypeKeyService.getListByPage(searchName, dataSourceTypeId, currentPage, pageSize);
    return Message.ok("").data("list", pageList);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(value = "get", notes = "Get a Datasource Type Key by id", httpMethod = "GET")
  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public Message get(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(
        request, "Get a Datasource Type Key Record,id:" + id.toString());
    DatasourceTypeKeyEntity datasourceType = datasourceTypeKeyService.getById(id);
    return Message.ok("").data("item", datasourceType);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "body",
        dataType = "DatasourceTypeEntity",
        name = "datasourceType")
  })
  @ApiOperation(value = "add", notes = "Add a Datasource Type Key Record", httpMethod = "POST")
  @RequestMapping(path = "", method = RequestMethod.POST)
  public Message add(
      HttpServletRequest request, @RequestBody DatasourceTypeKeyEntity datasourceType) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Add a Datasource Type Key Record," + datasourceType.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = datasourceTypeKeyService.save(datasourceType);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(
      value = "remove",
      notes = "Remove a Datasource Type Key Record by id",
      httpMethod = "DELETE")
  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  public Message remove(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(
        request, "Remove a Datasource Type Key Record,id:" + id.toString());
    boolean result = datasourceTypeKeyService.removeById(id);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "body",
        dataType = "DatasourceTypeEntity",
        name = "datasourceType")
  })
  @ApiOperation(value = "update", notes = "Update a Datasource Type Key Record", httpMethod = "PUT")
  @RequestMapping(path = "", method = RequestMethod.PUT)
  public Message update(
      HttpServletRequest request, @RequestBody DatasourceTypeKeyEntity datasourceType) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Update a Datasource Type Key Record,id:" + datasourceType.getId().toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = datasourceTypeKeyService.updateById(datasourceType);
    return Message.ok("").data("result", result);
  }
}
