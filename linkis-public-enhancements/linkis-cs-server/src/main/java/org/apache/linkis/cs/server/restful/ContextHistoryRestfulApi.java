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

import org.apache.linkis.cs.common.entity.history.ContextHistory;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.service.ContextHistoryService;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.cs.errorcode.LinkisCsServerErrorCodeSummary.PARAMS_CANNOT_EMPTY;

@Api(tags = "cs(contextservice) history operation")
@RestController
@RequestMapping(path = "/contextservice")
public class ContextHistoryRestfulApi implements CsRestfulParent {

  private static final Logger logger = LoggerFactory.getLogger(ContextIDRestfulApi.class);

  @Autowired private ContextHistoryService contextHistoryService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @ApiOperation(value = "createHistory", notes = "create context history", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextHistory", required = true, dataType = "String"),
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "createHistory", method = RequestMethod.POST)
  public Message createHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {

    ContextHistory history = getContextHistoryFromJsonNode(jsonNode);

    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    // source and contextid cannot be empty
    if (StringUtils.isBlank(history.getSource())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "history source"));
    }
    if (StringUtils.isBlank(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "createHistory,contextID:" + contextID.getContextId());
    contextHistoryService.createHistroy(contextID, history);
    return generateMessage(null, "");
  }

  @ApiOperation(value = "removeHistory", notes = "remove context history", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextHistory", dataType = "String"),
    @ApiImplicitParam(name = "contextID", dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "removeHistory", method = RequestMethod.POST)
  public Message removeHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextHistory history = getContextHistoryFromJsonNode(jsonNode);
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    // source and contextid cannot be empty
    if (StringUtils.isBlank(history.getSource())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "history source"));
    }
    if (StringUtils.isBlank(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "removeHistory,contextID:" + contextID.getContextId());
    contextHistoryService.removeHistory(contextID, history);
    return generateMessage(null, "");
  }

  @ApiOperation(
      value = "getHistories",
      notes = "get content history list",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextID", dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "getHistories", method = RequestMethod.POST)
  public Message getHistories(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isBlank(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "getHistory,contextID:" + contextID.getContextId());
    Object history = contextHistoryService.getHistories(contextID);
    return generateMessage(history, "contextHistory");
  }

  @ApiOperation(value = "GetHistory", notes = "get context history", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String"),
    @ApiImplicitParam(name = "source", required = false, dataType = "String", value = "source")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "getHistory", method = RequestMethod.POST)
  public Message getHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    // ContextID contextID, String source
    String source = jsonNode.get("source").textValue();
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    // source and contextid cannot be empty
    if (StringUtils.isBlank(source)) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "history source"));
    }
    if (StringUtils.isBlank(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "getHistory,contextID:" + contextID.getContextId());
    Object history = contextHistoryService.getHistory(contextID, source);
    return generateMessage(history, "contextHistory");
  }

  @ApiOperation(value = "searchHistory", notes = "search history", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "contextID", required = true, dataType = "String"),
    @ApiImplicitParam(name = "keywords", dataType = "String", value = "key words")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "searchHistory", method = RequestMethod.POST)
  public Message searchHistory(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    // ContextID contextID, String[] keywords
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    String[] keywords = objectMapper.treeToValue(jsonNode.get("keywords"), String[].class);
    if (StringUtils.isBlank(contextID.getContextId())) {
      throw new CSErrorException(
          PARAMS_CANNOT_EMPTY.getErrorCode(),
          MessageFormat.format(PARAMS_CANNOT_EMPTY.getErrorDesc(), "ContextID"));
    }
    ModuleUserUtils.getOperationUser(req, "searchHistory,contextID:" + contextID.getContextId());
    Object history = contextHistoryService.searchHistory(contextID, keywords);
    return generateMessage(history, "contextHistory");
  }

  @Override
  public ServiceType getServiceType() {
    return ServiceType.CONTEXT_HISTORY;
  }
}
