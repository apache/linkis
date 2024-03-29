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

import org.apache.linkis.basedatamanager.server.domain.UdfBaseInfoEntity;
import org.apache.linkis.basedatamanager.server.domain.UdfTreeEntity;
import org.apache.linkis.basedatamanager.server.service.UdfBaseInfoService;
import org.apache.linkis.basedatamanager.server.service.UdfTreeService;
import org.apache.linkis.basedatamanager.server.utils.UdfTreeUtils;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "UdfTreeRestfulApi")
@RestController
@RequestMapping(path = "/basedata-manager/udf-tree")
public class UdfTreeRestfulApi {

  @Autowired UdfTreeService udfTreeService;

  @Autowired UdfBaseInfoService udfBaseinfoService;

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage"),
    @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize")
  })
  @ApiOperation(value = "list", notes = "Query list data of UDF Tree", httpMethod = "GET")
  @RequestMapping(path = "", method = RequestMethod.GET)
  public Message list(
      HttpServletRequest request, String searchName, Integer currentPage, Integer pageSize) {
    ModuleUserUtils.getOperationUser(
        request, "Query list data of UDF Tree,search name:" + searchName);
    PageInfo pageList = udfTreeService.getListByPage(searchName, currentPage, pageSize);
    return Message.ok("").data("list", pageList);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "category")
  })
  @ApiOperation(value = "all", notes = "Query all data of UDF Tree", httpMethod = "GET")
  @RequestMapping(path = "/all", method = RequestMethod.GET)
  public Message all(HttpServletRequest request, String searchName, String category) {
    ModuleUserUtils.getOperationUser(
        request, "Query all data of UDF Tree,search name:" + searchName);
    List<UdfTreeEntity> udfTreeEntityList = new ArrayList<>();
    if (StringUtils.isNotBlank(searchName) && StringUtils.isNotBlank(category)) {
      UdfTreeEntity entity = new UdfTreeEntity();
      entity.setCategory(category);
      entity.setUserName(searchName);
      QueryWrapper<UdfTreeEntity> queryWrapper =
          new QueryWrapper<>(entity)
              .eq("user_name", entity.getUserName())
              .eq("category", entity.getCategory());
      udfTreeEntityList = new UdfTreeUtils(udfTreeService.list(queryWrapper)).buildTree();
    }
    return Message.ok("").data("list", udfTreeEntityList);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(value = "get", notes = "Get a UDF Tree Record by id", httpMethod = "GET")
  @RequestMapping(path = "/{id}", method = RequestMethod.GET)
  public Message get(HttpServletRequest request, @PathVariable("id") Long id) {
    ModuleUserUtils.getOperationUser(request, "Get a UDF Tree Record,id:" + id.toString());
    UdfTreeEntity errorCode = udfTreeService.getById(id);
    return Message.ok("").data("item", errorCode);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "body", dataType = "UdfTreeEntity", name = "udfTreeEntity")
  })
  @ApiOperation(value = "add", notes = "Add a UDF Tree Record", httpMethod = "POST")
  @RequestMapping(path = "", method = RequestMethod.POST)
  public Message add(HttpServletRequest request, @RequestBody UdfTreeEntity udfTreeEntity) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Add a UDF Tree Record," + udfTreeEntity.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = udfTreeService.save(udfTreeEntity);
    return Message.ok("").data("result", result);
  }

  @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
  @ApiOperation(value = "remove", notes = "Remove a UDF Tree Record by id", httpMethod = "DELETE")
  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  public Message remove(HttpServletRequest request, @PathVariable("id") Long id) {
    String username =
        ModuleUserUtils.getOperationUser(request, "Remove a UDF Tree Record,id:" + id.toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    UdfTreeEntity entity = udfTreeService.getById(id);
    if (null != entity && entity.getParent() == -1) {
      return Message.error("The root directory is forbidden to delete[\"根目录禁止删除\"]");
    }
    QueryWrapper<UdfTreeEntity> queryWrapper =
        new QueryWrapper<>(new UdfTreeEntity()).eq("parent", id);
    List<UdfTreeEntity> folderList = udfTreeService.list(queryWrapper);
    QueryWrapper<UdfBaseInfoEntity> udfQueryWrapper =
        new QueryWrapper<>(new UdfBaseInfoEntity()).eq("tree_id", id);
    List<UdfBaseInfoEntity> functoinList = udfBaseinfoService.list(udfQueryWrapper);
    if (CollectionUtils.isEmpty(folderList) && CollectionUtils.isEmpty(functoinList)) {
      boolean result = udfTreeService.removeById(id);
      return Message.ok("").data("result", result);
    } else {
      return Message.error("Please delete the subdirectory first[请先删除子目录]");
    }
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "body", dataType = "UdfTreeEntity", name = "udfTreeEntity")
  })
  @ApiOperation(value = "update", notes = "Update a UDF Tree Record", httpMethod = "PUT")
  @RequestMapping(path = "", method = RequestMethod.PUT)
  public Message update(HttpServletRequest request, @RequestBody UdfTreeEntity udfTreeEntity) {
    String username =
        ModuleUserUtils.getOperationUser(
            request, "Update a UDF Tree Record,id:" + udfTreeEntity.getId().toString());
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    boolean result = udfTreeService.updateById(udfTreeEntity);
    return Message.ok("").data("result", result);
  }
}
