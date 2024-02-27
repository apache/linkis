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
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.configuration.entity.ConfigKeyLimitVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.TemplateConfigKeyService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "configuration template")
@RestController
@RequestMapping(path = "/configuration/template")
public class TemplateRestfulApi {

  private static final Logger logger = LoggerFactory.getLogger(TemplateRestfulApi.class);

  @Autowired private TemplateConfigKeyService templateConfigKeyService;

  @ApiOperation(
      value = "updateKeyMapping",
      notes = "query engineconn info list",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "templateUid",
        dataType = "String",
        required = true,
        value = "templateUid"),
    @ApiImplicitParam(
        name = "templateName",
        dataType = "String",
        required = true,
        value = "engine type"),
    @ApiImplicitParam(name = "engineType", dataType = "String", required = true, value = "String"),
    @ApiImplicitParam(name = "operator", dataType = "String", value = "operator"),
    @ApiImplicitParam(name = "isFullMode", dataType = "Boolbean", value = "isFullMode"),
    @ApiImplicitParam(name = "itemList", dataType = "Array", value = "itemList"),
  })
  @RequestMapping(path = "/updateKeyMapping", method = RequestMethod.POST)
  public Message updateKeyMapping(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws ConfigurationException {
    String username = ModuleUserUtils.getOperationUser(req, "updateKeyMapping");
    String token = ModuleUserUtils.getToken(req);
    // check special admin token
    if (StringUtils.isNotBlank(token)) {
      if (!Configuration.isAdminToken(token)) {
        logger.warn("Token:{} has no permission to updateKeyMapping.", token);
        return Message.error("Token:" + token + " has no permission to updateKeyMapping.");
      }
    } else if (!Configuration.isAdmin(username)) {
      logger.warn("User:{} has no permission to updateKeyMapping.", username);
      return Message.error("User:" + username + " has no permission to updateKeyMapping.");
    }

    String templateUid = jsonNode.get("templateUid").asText();
    String templateName = jsonNode.get("templateName").asText();
    String engineType = jsonNode.get("engineType").asText();
    String operator = jsonNode.get("operator").asText();

    if (StringUtils.isBlank(templateUid)) {
      return Message.error("parameters:templateUid can not be empty(请求参数【templateUid】不能为空)");
    }
    if (StringUtils.isBlank(templateName)) {
      return Message.error("parameters:templateName can not be empty(请求参数【templateName】不能为空)");
    }
    if (StringUtils.isBlank(engineType)) {
      return Message.error("parameters:engineType can not be empty(请求参数【engineType】不能为空)");
    }
    if (StringUtils.isBlank(operator)) {
      return Message.error("parameters:operator can not be empty(请求参数【operator】不能为空)");
    }
    boolean isFullMode = true;
    try {
      isFullMode = jsonNode.get("isFullMode").asBoolean();
      logger.info("will update by param isFullMode:" + isFullMode);
    } catch (Exception e) {
      logger.info("will update by default isFullMode:" + isFullMode);
    }

    JsonNode itemParms = jsonNode.get("itemList");

    List<ConfigKeyLimitVo> confKeyList = new ArrayList<>();
    if (itemParms != null && !itemParms.isNull()) {
      try {
        confKeyList =
            JsonUtils.jackson()
                .readValue(itemParms.toString(), new TypeReference<List<ConfigKeyLimitVo>>() {});
      } catch (JsonProcessingException e) {
        return Message.error(
            "parameters:itemList parsing failed(请求参数【itemList】解析失败), error with:" + e.getMessage());
      }
    } else {
      return Message.error("parameters:itemList can not be empty(请求参数【itemList】不能为空)");
    }

    logger.info(
        "request parameters templateUid:{}, templateName:{}, engineType:{}, operator:{},isFullMode:{}, itemList:[{}]",
        templateUid,
        templateName,
        engineType,
        operator,
        itemParms.asText());

    templateConfigKeyService.updateKeyMapping(
        templateUid, templateName, engineType, operator, confKeyList, isFullMode);
    return Message.ok();
  }

  @ApiOperation(value = "queryKeyInfoList", notes = "query key info list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "templateUidList", dataType = "Array", value = "templateUidList"),
  })
  @RequestMapping(path = "/queryKeyInfoList", method = RequestMethod.POST)
  public Message queryKeyInfoList(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws ConfigurationException {
    String username = ModuleUserUtils.getOperationUser(req, "queryKeyInfoList");
    String token = ModuleUserUtils.getToken(req);
    // check special admin token
    if (StringUtils.isNotBlank(token)) {
      if (!Configuration.isAdminToken(token)) {
        logger.warn("Token:{} has no permission to queryKeyInfoList.", token);
        return Message.error("Token:" + token + " has no permission to queryKeyInfoList.");
      }
    } else if (!Configuration.isAdmin(username)) {
      logger.warn("User:{} has no permission to queryKeyInfoList.", username);
      return Message.error("User:" + username + " has no permission to queryKeyInfoList.");
    }

    JsonNode templateUidListParms = jsonNode.get("templateUidList");

    List<String> uuidList = new ArrayList<>();
    if (templateUidListParms != null && !templateUidListParms.isNull()) {
      try {
        uuidList =
            JsonUtils.jackson()
                .readValue(templateUidListParms.toString(), new TypeReference<List<String>>() {});
      } catch (JsonProcessingException e) {
        return Message.error(
            "parameters:templateUidList parsing failed(请求参数【templateUidList】解析失败), error with:"
                + e.getMessage());
      }
    } else {
      return Message.error(
          "parameters:templateUidList can not be empty(请求参数【templateUidList】不能为空)");
    }

    List<Object> result = templateConfigKeyService.queryKeyInfoList(uuidList);

    return Message.ok().data("list", result);
  }

  @ApiOperation(value = "apply", notes = "apply conf template rule", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "templateUid",
        dataType = "String",
        required = true,
        value = "templateUid"),
    @ApiImplicitParam(name = "application", dataType = "String", value = "application"),
    @ApiImplicitParam(name = "engineType", dataType = "String", value = "engineType"),
    @ApiImplicitParam(name = "engineVersion", dataType = "String", value = "engineVersion"),
    @ApiImplicitParam(name = "operator", dataType = "String", value = "operator"),
    @ApiImplicitParam(name = "userList", dataType = "Array", value = "userList"),
  })
  @RequestMapping(path = "/apply", method = RequestMethod.POST)
  public Message apply(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws ConfigurationException {
    String username = ModuleUserUtils.getOperationUser(req, "apply");
    String token = ModuleUserUtils.getToken(req);
    // check special admin token
    if (StringUtils.isNotBlank(token)) {
      if (!Configuration.isAdminToken(token)) {
        logger.warn("Token:{} has no permission to apply.", token);
        return Message.error("Token:" + token + " has no permission to apply.");
      }
    } else if (!Configuration.isAdmin(username)) {
      logger.warn("User:{} has no permission to apply.", username);
      return Message.error("User:" + username + " has no permission to apply.");
    }

    String templateUid = jsonNode.get("templateUid").asText();
    String application = jsonNode.get("application").asText();
    String engineType = jsonNode.get("engineType").asText();
    String engineVersion = jsonNode.get("engineVersion").asText();
    String operator = jsonNode.get("operator").asText();

    if (StringUtils.isBlank(templateUid)) {
      return Message.error("parameters:templateUid can not be empty(请求参数【templateUid】不能为空)");
    }
    if (StringUtils.isBlank(application)) {
      return Message.error("parameters:application can not be empty(请求参数【application】不能为空)");
    }
    if (StringUtils.isBlank(engineType)) {
      return Message.error("parameters:engineType can not be empty(请求参数【engineType】不能为空)");
    }
    if (StringUtils.isBlank(engineVersion)) {
      return Message.error("parameters:engineVersion can not be empty(请求参数【engineVersion】不能为空)");
    }
    if (StringUtils.isBlank(operator)) {
      return Message.error("parameters:operator can not be empty(请求参数【operator】不能为空)");
    }

    JsonNode userParms = jsonNode.get("userList");
    List<String> userList = new ArrayList<>();
    if (userParms != null && !userParms.isNull()) {
      try {
        userList =
            JsonUtils.jackson()
                .readValue(userParms.toString(), new TypeReference<List<String>>() {});
      } catch (JsonProcessingException e) {
        return Message.error(
            "parameters:userList parsing failed(请求参数【userList】解析失败), error with:" + e.getMessage());
      }
    } else {
      return Message.error("parameters:userList can not be empty(请求参数【userList】不能为空)");
    }

    logger.info(
        "request parameters templateUid:{}, application:{}, engineType:{}, engineVersion:{}, operator:{},userList:[{}]",
        templateUid,
        application,
        engineType,
        engineVersion,
        operator,
        String.join(",", userList));

    Map<String, Object> result =
        templateConfigKeyService.apply(
            templateUid, application, engineType, engineVersion, operator, userList);

    Message message = Message.ok();
    message.getData().putAll(result);
    return message;
  }
}
