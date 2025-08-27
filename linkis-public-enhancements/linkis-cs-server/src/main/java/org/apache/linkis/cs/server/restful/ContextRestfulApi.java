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

package org.apache.linkis.cs.server.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.protocol.ContextHTTPConstant;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.cs.exception.ContextSearchFailedException;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.service.ContextService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.cs.common.utils.CSCommonUtils.localDatetimeToDate;
import static org.apache.linkis.cs.errorcode.LinkisCsServerErrorCodeSummary.NO_PERMISSION;
import static org.apache.linkis.cs.errorcode.LinkisCsServerErrorCodeSummary.PARAMS_CANNOT_EMPTY;

@Api(tags = "cs(contextservice) operation")
@RestController
@RequestMapping(path = "/contextservice")
public class ContextRestfulApi implements CsRestfulParent {

  private static final Logger logger = LoggerFactory.getLogger(ContextRestfulApi.class);

  @Autowired private ContextService contextService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @ApiOperation(value = "getContextValue", notes = "get context value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKey", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "getContextValue", method = RequestMethod.POST)
  public Message getContextValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "getContextValue,contextID:" + contextID.getContextId());
    ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
    Object res = contextService.getContextValue(contextID, contextKey);
    return generateMessage(res, "contextValue");
  }

  @ApiOperation(
      value = "searchContextValue",
      notes = "search context value",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "condition", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "searchContextValue", method = RequestMethod.POST)
  public Message searchContextValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException, ContextSearchFailedException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(
        req, "searchContextValue,contextID:" + contextID.getContextId());
    JsonNode condition = jsonNode.get("condition");
    Map<Object, Object> conditionMap =
        objectMapper.convertValue(condition, new TypeReference<Map<Object, Object>>() {});
    Object res = contextService.searchContextValue(contextID, conditionMap);
    return generateMessage(res, "contextKeyValue");
  }

  @ApiOperation(value = "setValueByKey", notes = "set value by key", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKey", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "setValueByKey", method = RequestMethod.POST)
  public Message setValueByKey(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException, InterruptedException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "setValueByKey,contextID:" + contextID.getContextId());
    ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
    ContextValue contextValue = getContextValueFromJsonNode(jsonNode);
    contextService.setValueByKey(contextID, contextKey, contextValue);
    return generateMessage(null, "");
  }

  @ApiOperation(value = "setValue", notes = "set value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKeyValue", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "setValue", method = RequestMethod.POST)
  public Message setValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "setValue,contextID:" + contextID.getContextId());
    ContextKeyValue contextKeyValue = getContextKeyValueFromJsonNode(jsonNode);
    contextService.setValue(contextID, contextKeyValue);
    return generateMessage(null, "");
  }

  @ApiOperation(value = "resetValue", notes = "reset value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKey", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "resetValue", method = RequestMethod.POST)
  public Message resetValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "resetValue,contextID:" + contextID.getContextId());
    ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
    contextService.resetValue(contextID, contextKey);
    return generateMessage(null, "");
  }

  @ApiOperation(value = "removeValue", notes = "remove value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKey", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "removeValue", method = RequestMethod.POST)
  public Message removeValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "removeValue,contextID:" + contextID.getContextId());
    ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
    contextService.removeValue(contextID, contextKey);
    return generateMessage(null, "");
  }

  @ApiOperation(value = "removeAllValue", notes = "remove all value", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKey", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "removeAllValue", method = RequestMethod.POST)
  public Message removeAllValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "removeAllValue,contextID:" + contextID.getContextId());
    contextService.removeAllValue(contextID);
    return generateMessage(null, "");
  }

  @ApiOperation(
      value = "removeAllValueByKeyPrefixAndContextType",
      notes = "remove all value by key prefix and context type",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKeyType", required = true, dataType = "String"),
    @ApiImplicitParam(name = "keyPrefix", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "removeAllValueByKeyPrefixAndContextType", method = RequestMethod.POST)
  public Message removeAllValueByKeyPrefixAndContextType(
      HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(
        req, "removeAllValueByKeyPrefixAndContextType,contextID:" + contextID.getContextId());
    String contextType = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_TYPE_STR).textValue();
    String keyPrefix = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR).textValue();
    contextService.removeAllValueByKeyPrefixAndContextType(
        contextID, ContextType.valueOf(contextType), keyPrefix);
    return generateMessage(null, "");
  }

  @ApiOperation(
      value = "removeAllValueByKeyAndContextType",
      notes = "remove all value by key and context type",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextKeyType", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextKey", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "removeAllValueByKeyAndContextType", method = RequestMethod.POST)
  public Message removeValueByKeyAndContextType(
      HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    String contextType = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_TYPE_STR).textValue();
    String keyStr = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_STR).textValue();
    contextService.removeValueByKeyAndContextType(
        contextID, ContextType.valueOf(contextType), keyStr);
    return generateMessage(null, "");
  }

  @ApiOperation(
      value = "removeAllValueByKeyPrefix",
      notes = "remove all value by key prefix",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextID", dataType = "String"),
    @ApiImplicitParam(name = "keyPrefix", dataType = "String")
  })
  @RequestMapping(path = "removeAllValueByKeyPrefix", method = RequestMethod.POST)
  public Message removeAllValueByKeyPrefix(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(
        req, "removeAllValueByKeyPrefix,contextID:" + contextID.getContextId());
    String keyPrefix = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR).textValue();
    contextService.removeAllValueByKeyPrefix(contextID, keyPrefix);
    return generateMessage(null, "");
  }

  @ApiOperation(
      value = "clearAllContextByID",
      notes = "clear all context by id",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "idList", required = true, dataType = "String", value = "id list"),
  })
  @RequestMapping(path = "clearAllContextByID", method = RequestMethod.POST)
  public Message clearAllContextByID(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException {
    if (null == jsonNode
        || !jsonNode.has("idList")
        || !jsonNode.get("idList").isArray()
        || (jsonNode.get("idList").isArray() && jsonNode.get("idList").size() == 0)) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "idList"));
    }
    ModuleUserUtils.getOperationUser(req, "clearAllContextByID");
    ArrayNode idArray = (ArrayNode) jsonNode.get("idList");
    logger.info("clearAllContextByID idList size : {}", idArray.size());
    List<String> idList = new ArrayList<>(idArray.size());
    for (int i = 0; i < idArray.size(); i++) {
      idList.add(idArray.get(i).asText());
    }
    Message resp = generateMessage(contextService.clearAllContextByID(idList), "num");
    resp.setMethod("/api/contextservice/clearAllContextByID");
    return resp;
  }

  @ApiOperation(
      value = "clearAllContextByID(time)",
      notes = "clear all context by id(time)",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "createTimeStart", required = true, dataType = "String"),
    @ApiImplicitParam(name = "createTimeEnd", required = true, dataType = "String"),
    @ApiImplicitParam(name = "updateTimeStart", required = true, dataType = "String"),
    @ApiImplicitParam(name = "updateTimeEnd", required = true, dataType = "String"),
    @ApiImplicitParam(name = "accessTimeStart", required = true, dataType = "String"),
    @ApiImplicitParam(name = "accessTimeEnd", required = true, dataType = "String")
  })
  @RequestMapping(path = "clearAllContextByTime", method = RequestMethod.POST)
  public Message clearAllContextByID(
      HttpServletRequest req, @RequestBody Map<String, Object> bodyMap)
      throws InterruptedException, CSErrorException {
    String username = ModuleUserUtils.getOperationUser(req, "clearAllContextByTime");
    if (Configuration.isNotAdmin(username)) {
      throw new CSErrorException(NO_PERMISSION.getErrorCode(), NO_PERMISSION.getErrorDesc());
    }
    if (null == bodyMap || bodyMap.isEmpty()) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "idList"));
    }
    Date createTimeStart = null;
    Date createTimeEnd = null;
    Date updateTimeStart = null;
    Date updateTimeEnd = null;
    Date accessTimeStart = null;
    Date accessTimeEnd = null;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(CSCommonUtils.DEFAULT_TIME_FORMAT);
    if (bodyMap.containsKey("createTimeStart") && null != bodyMap.get("createTimeStart"))
      createTimeStart =
          localDatetimeToDate(LocalDateTime.parse((String) bodyMap.get("createTimeStart"), dtf));
    if (bodyMap.containsKey("createTimeEnd") && null != bodyMap.get("createTimeEnd"))
      createTimeEnd =
          localDatetimeToDate(LocalDateTime.parse((String) bodyMap.get("createTimeEnd"), dtf));
    if (bodyMap.containsKey("updateTimeStart") && null != bodyMap.get("updateTimeStart"))
      updateTimeStart =
          localDatetimeToDate(LocalDateTime.parse((String) bodyMap.get("updateTimeStart"), dtf));
    if (bodyMap.containsKey("updateTimeEnd") && null != bodyMap.get("updateTimeEnd"))
      updateTimeEnd =
          localDatetimeToDate(LocalDateTime.parse((String) bodyMap.get("updateTimeEnd"), dtf));
    if (bodyMap.containsKey("accessTimeStart") && null != bodyMap.get("accessTimeStart"))
      accessTimeStart =
          localDatetimeToDate(LocalDateTime.parse((String) bodyMap.get("accessTimeStart"), dtf));
    if (bodyMap.containsKey("accessTimeEnd") && null != bodyMap.get("accessTimeEnd"))
      accessTimeEnd =
          localDatetimeToDate(LocalDateTime.parse((String) bodyMap.get("accessTimeEnd"), dtf));
    if (null == createTimeStart
        && null == createTimeEnd
        && null == updateTimeStart
        && null == updateTimeEnd) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(
              PARAMS_CANNOT_EMPTY.getErrorDesc(),
              "createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd"));
    }
    logger.info(
        "clearAllContextByTime: user : {}, createTimeStart : {}, createTimeEnd : {}, updateTimeStart : {}, updateTimeEnd : {}, accessTimeStart : {}, accessTimeEnd : {}.",
        username,
        createTimeStart,
        createTimeEnd,
        updateTimeStart,
        updateTimeEnd,
        accessTimeStart,
        accessTimeEnd);
    Message resp =
        generateMessage(
            contextService.clearAllContextByTime(
                createTimeStart,
                createTimeEnd,
                updateTimeStart,
                updateTimeEnd,
                accessTimeStart,
                accessTimeEnd),
            "num");
    resp.setMethod("/api/contextservice/clearAllContextByTime");
    return resp;
  }

  @Override
  public ServiceType getServiceType() {
    return ServiceType.CONTEXT;
  }
}
