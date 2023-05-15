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

import org.apache.linkis.basedatamanager.server.domain.UdfManagerEntity;
import org.apache.linkis.basedatamanager.server.service.UdfManagerService;
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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "UdfManagerRestfulApi")
@RestController
@RequestMapping(path = "/basedata-manager/udf-manager")
public class UdfManagerRestfulApi {

  @Autowired UdfManagerService udfManagerService;

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize")
  })
  @ApiOperation(value = "list", notes = "Query list data of UDF Manager", httpMethod = "GET")
  @RequestMapping(path = "", method = RequestMethod.GET)
  public Message list(
      HttpServletRequest request, String searchName, Integer currentPage, Integer pageSize) {
    ModuleUserUtils.getOperationUser(
        request, "Query list data of UDF Manager,search name:" + searchName);
    PageInfo pageList = udfManagerService.getListByPage(searchName, currentPage, pageSize);
    return Message.ok("").data("list", pageList);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(
      value = "Get a Datasource UDF Manager",
      notes = "get data by id",
      httpMethod = "GET")
  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public Message get(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(request, "Get a Datasource UDF Manager,id:" + id.toString());
    UdfManagerEntity errorCode = udfManagerService.getById(id);
    return Message.ok("").data("item", errorCode);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "body", dataType = "UdfManagerEntity", name = "udfManagerEntity")
  })
  @ApiOperation(value = "add", notes = "Add a UDF Manager Record", httpMethod = "POST")
  @RequestMapping(path = "", method = RequestMethod.POST)
  public Message add(HttpServletRequest request, @RequestBody UdfManagerEntity udfManagerEntity) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Add a UDF Manager Record," + udfManagerEntity.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    QueryWrapper<UdfManagerEntity> queryWrapper =
        new QueryWrapper<>(udfManagerEntity).eq("user_name", udfManagerEntity.getUserName());
    UdfManagerEntity udfManager = udfManagerService.getOne(queryWrapper);
    if (udfManager == null) {
      udfManagerEntity.setCreateTime(new Date());
      udfManagerEntity.setUpdateTime(new Date());
      boolean result = udfManagerService.save(udfManagerEntity);
      return Message.ok("").data("result", result);
    } else {
      return Message.error("The " + udfManager.getUserName() + " already exists,Please add again!");
    }
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(
      value = "remove",
      notes = "Remove a UDF Manager Record by id",
      httpMethod = "DELETE")
  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  public Message remove(HttpServletRequest request, @PathVariable("id") Long id) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Remove a UDF Manager Record,id:" + id.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = udfManagerService.removeById(id);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "body", dataType = "UdfManagerEntity", name = "udfManagerEntity")
  })
  @ApiOperation(value = "update", notes = "Update a Datasource Access Record", httpMethod = "PUT")
  @RequestMapping(path = "", method = RequestMethod.PUT)
  public Message update(
      HttpServletRequest request, @RequestBody UdfManagerEntity udfManagerEntity) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Update a Datasource Access Record,id:" + udfManagerEntity.getId().toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    QueryWrapper<UdfManagerEntity> queryWrapper = new QueryWrapper();
    queryWrapper.eq("user_name", udfManagerEntity.getUserName());
    UdfManagerEntity udfManager = udfManagerService.getOne(queryWrapper);
    if (udfManager == null) {
      udfManagerEntity.setUpdateTime(new Date());
      boolean result = udfManagerService.updateById(udfManagerEntity);
      return Message.ok("").data("result", result);
    } else {
      return Message.error(
          "The " + udfManager.getUserName() + " already exists,Please update again!");
    }
  }
}
