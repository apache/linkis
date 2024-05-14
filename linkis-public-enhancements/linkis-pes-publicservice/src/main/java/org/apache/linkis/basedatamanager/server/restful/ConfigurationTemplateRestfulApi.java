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

import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigKey;
import org.apache.linkis.basedatamanager.server.request.ConfigurationTemplateSaveRequest;
import org.apache.linkis.basedatamanager.server.response.EngineLabelResponse;
import org.apache.linkis.basedatamanager.server.service.ConfigurationTemplateService;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/** This module is designed to manage configuration parameter templates */
@RestController
@RequestMapping(path = "/basedata-manager/configuration-template")
public class ConfigurationTemplateRestfulApi {

  @Autowired ConfigurationTemplateService configurationTemplateService;

  @ApiOperation(value = "save", notes = "save a configuration template", httpMethod = "POST")
  @RequestMapping(path = "/save", method = RequestMethod.POST)
  public Message add(
      HttpServletRequest httpRequest, @RequestBody ConfigurationTemplateSaveRequest request) {
    String username =
        ModuleUserUtils.getOperationUser(httpRequest, "save a configuration template");
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    if (Objects.isNull(request)
        || StringUtils.isEmpty(request.getEngineLabelId())
        || StringUtils.isEmpty(request.getKey())
        || StringUtils.isEmpty(request.getName())
        || StringUtils.isEmpty(request.getTreeName())) {
      throw new InvalidParameterException("please check your parameter.");
    }
    Boolean flag = configurationTemplateService.saveConfigurationTemplate(request);
    return Message.ok("").data("success: ", flag);
  }

  @ApiImplicitParams({
    @ApiImplicitParam(paramType = "path", dataType = "long", name = "keyId", value = "")
  })
  @ApiOperation(value = "delete", notes = "delete a configuration template", httpMethod = "DELETE")
  @RequestMapping(path = "/{keyId}", method = RequestMethod.DELETE)
  public Message delete(HttpServletRequest httpRequest, @PathVariable("keyId") Long keyId) {
    String username =
        ModuleUserUtils.getOperationUser(
            httpRequest, "delete a configuration template, keyId: " + keyId);
    if (!Configuration.isAdmin(username)) {
      return Message.error("User '" + username + "' is not admin user[非管理员用户]");
    }
    Boolean flag = configurationTemplateService.deleteConfigurationTemplate(keyId);
    return Message.ok("").data("success: ", flag);
  }

  @ApiOperation(value = "engin-list", notes = "get all engine list", httpMethod = "GET")
  @RequestMapping(path = "/engin-list", method = RequestMethod.GET)
  public Message getEngineList(HttpServletRequest httpRequest) {
    ModuleUserUtils.getOperationUser(httpRequest, "get all engine list");
    List<EngineLabelResponse> engineList = configurationTemplateService.getEngineList();
    return Message.ok("").data("success: ", engineList);
  }

  @ApiOperation(
      value = "template-list-by-label",
      notes = "get template list by label",
      httpMethod = "GET")
  @RequestMapping(path = "/template-list-by-label", method = RequestMethod.GET)
  public Message getTemplateListByLabelId(
      HttpServletRequest httpRequest, @RequestParam String engineLabelId) {
    ModuleUserUtils.getOperationUser(
        httpRequest, "get template list by label, engineLabelId: " + engineLabelId);
    if (StringUtils.isEmpty(engineLabelId)) {
      throw new InvalidParameterException("please check your parameter.");
    }
    List<ConfigurationConfigKey> configKeyList =
        configurationTemplateService.getTemplateListByLabelId(engineLabelId);
    return Message.ok("").data("success: ", configKeyList);
  }
}
