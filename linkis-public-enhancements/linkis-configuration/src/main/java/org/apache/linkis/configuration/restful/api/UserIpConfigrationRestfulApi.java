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
import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.UserIpConfigService;
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
@RequestMapping(path = "/configuration/user-ip-mapping")
public class UserIpConfigrationRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(UserIpConfigrationRestfulApi.class);

  @Autowired private UserIpConfigService userIpConfigService;

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(
        paramType = "body",
        dataType = "UserIpVo",
        name = "userIpVo",
        value = "userIpVo")
  })
  @ApiOperation(value = "create-user-ip", notes = "create user ip", httpMethod = "POST")
  @RequestMapping(path = "/create-user-ip", method = RequestMethod.POST)
  public Message createUserIp(HttpServletRequest req, @RequestBody UserIpVo userIpVo) {
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "execute createUserIP");
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to create-user-ip,msg: only administrators can configure");
      }
      if (userIpConfigService.userExists(userIpVo.getUser(), userIpVo.getCreator())) {
        throw new ConfigurationException("User-creator is existed");
      }
      parameterVerification(userIpVo);
      userIpConfigService.createUserIP(userIpVo);
    } catch (DuplicateKeyException e) {
      return Message.error("Failed to create-user-ip,msg:create user-creator is existed");
    } catch (ConfigurationException e) {
      return Message.error("Failed to create-user-ip,msg:" + e.getMessage());
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
        dataType = "UserIpVo",
        name = "UserIpVo",
        value = "UserIpVo")
  })
  @ApiOperation(value = "update-user-ip", notes = "update user ip", httpMethod = "POST")
  @RequestMapping(path = "/update-user-ip", method = RequestMethod.POST)
  public Message updateUserIp(HttpServletRequest req, @RequestBody UserIpVo userIpVo) {
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "execute updateUserIP");
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to update-user-ip,msg: only administrators can configure ");
      }
      //      if (!userIpConfigService.checkUserCteator(userIpVo.getUser(), userIpVo.getCreator()))
      // {
      //        throw new ConfigurationException("User-creator is not existed");
      //      }
      parameterVerification(userIpVo);
      userIpConfigService.updateUserIP(userIpVo);
    } catch (ConfigurationException e) {
      return Message.error("Failed to update-user-ip,msg:" + e.getMessage());
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
  @ApiOperation(value = "delete-user-ip", notes = "delete user ip", httpMethod = "GET")
  @RequestMapping(path = "/delete-user-ip", method = RequestMethod.GET)
  public Message deleteUserIp(HttpServletRequest req, @RequestParam(value = "id") Integer id) {
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "execute deleteUserIp,id: " + id);
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to delete-user-ip,msg: only administrators can configure");
      }
      userIpConfigService.deleteUserIP(id);
    } catch (ConfigurationException e) {
      return Message.error("Failed to check-user-creator,msg:" + e.getMessage());
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
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "creator", value = "creator")
  })
  @ApiOperation(value = "query-user-ip-list", notes = "query user ip list", httpMethod = "GET")
  @RequestMapping(path = "/query-user-ip-list", method = RequestMethod.GET)
  public Message queryUserIpList(
      HttpServletRequest req,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "pageNow") Integer pageNow,
      @RequestParam(value = "pageSize") Integer pageSize) {
    String userName = ModuleUserUtils.getOperationUser(req, "queryUserIPList");
    if (!Configuration.isAdmin(userName)) {
      return Message.error("Failed to query-user-ip-list,msg: only administrators can configure");
    }
    if (StringUtils.isBlank(user)) user = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (null == pageNow) pageNow = 1;
    if (null == pageSize) pageSize = 20;
    Map<String, Object> resultMap =
        userIpConfigService.queryUserIPList(user, creator, pageNow, pageSize);
    return Message.ok()
        .data("userIpList", resultMap.get("userIpList"))
        .data(JobRequestConstants.TOTAL_PAGE(), resultMap.get(JobRequestConstants.TOTAL_PAGE()));
  }

  @ApiImplicitParams({
    @ApiImplicitParam(
        paramType = "query",
        dataType = "HttpServletRequest",
        name = "req",
        value = ""),
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "user", value = "user"),
    @ApiImplicitParam(paramType = "query", dataType = "string", name = "creator", value = "creator")
  })
  @ApiOperation(value = "check-user-creator", notes = " check user creator", httpMethod = "GET")
  @RequestMapping(path = "/check-user-creator", method = RequestMethod.GET)
  public Message checkUserCreator(
      HttpServletRequest req,
      @RequestParam(value = "user") String user,
      @RequestParam(value = "creator") String creator) {
    boolean result = false;
    try {
      String userName = ModuleUserUtils.getOperationUser(req, "checkUserCreator");
      if (!Configuration.isAdmin(userName)) {
        return Message.error("Failed to check-user-creator,msg: only administrators can configure");
      }
      // Parameter verification
      if (StringUtils.isBlank(creator)) {
        throw new ConfigurationException("Application Name couldn't be empty ");
      }
      if (StringUtils.isBlank(user)) {
        throw new ConfigurationException("User Name couldn't be empty ");
      }
      if (creator.equals("*")) {
        throw new ConfigurationException("Application Name couldn't be '*' ");
      }
      result = userIpConfigService.userExists(user, creator);
    } catch (ConfigurationException e) {
      return Message.error("Failed to check-user-creator,msg:" + e.getMessage());
    }
    return Message.ok().data("exist", result);
  }

  private void parameterVerification(UserIpVo userIpVo) throws ConfigurationException {
    // Parameter verification
    if (StringUtils.isBlank(userIpVo.getCreator())) {
      throw new ConfigurationException("Application Name couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getUser())) {
      throw new ConfigurationException("User Name couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getBussinessUser())) {
      throw new ConfigurationException("Creat User couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getIpList())) {
      throw new ConfigurationException("Ip List couldn't be empty ");
    }
    if (StringUtils.isBlank(userIpVo.getDesc())) {
      throw new ConfigurationException("Description couldn't be empty ");
    }
  }
}
