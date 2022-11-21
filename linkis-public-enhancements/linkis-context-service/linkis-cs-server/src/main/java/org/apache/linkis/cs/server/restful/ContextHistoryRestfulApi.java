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
import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.server.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "cs(contextservice) history operation")
@RestController
@RequestMapping(path = "/contextservice")
public class ContextHistoryRestfulApi implements CsRestfulParent {

  @Autowired private CsScheduler csScheduler;

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
    if (StringUtils.isEmpty(history.getSource())) {
      throw new CSErrorException(97000, "history source cannot be empty");
    }
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID, history);
    return generateResponse(answerJob, "");
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
    if (StringUtils.isEmpty(history.getSource())) {
      throw new CSErrorException(97000, "history source cannot be empty");
    }
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, history);
    return generateResponse(answerJob, "");
  }

  @ApiOperation(
      value = "getHistories",
      notes = "get content history list",
      response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextID", dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "getHistories", method = RequestMethod.POST)
  public Message getHistories(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID);
    Message message = generateResponse(answerJob, "contextHistory");
    return message;
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
    if (StringUtils.isEmpty(source)) {
      throw new CSErrorException(97000, "history source cannot be empty");
    }
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, source);
    Message message = generateResponse(answerJob, "contextHistory");
    return message;
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
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, keywords);
    Message message = generateResponse(answerJob, "contextHistory");
    return message;
  }

  @Override
  public ServiceType getServiceType() {
    return ServiceType.CONTEXT_HISTORY;
  }

  @Override
  public CsScheduler getScheduler() {
    return this.csScheduler;
  }
}
