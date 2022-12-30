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
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.protocol.ContextHTTPConstant;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.cs.common.utils.CSCommonUtils.localDatetimeToDate;

@Api(tags = "cs(contextservice) recording operation")
@RestController
@RequestMapping(path = "/contextservice")
public class ContextIDRestfulApi implements CsRestfulParent {

  private static final Logger logger = LoggerFactory.getLogger(ContextIDRestfulApi.class);

  @Autowired private CsScheduler csScheduler;

  @ApiOperation(value = "createContextID", notes = "create context Id", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextID", dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "createContextID", method = RequestMethod.POST)
  public Message createContextID(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, ClassNotFoundException, IOException, CSErrorException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID);
    return generateResponse(answerJob, "contextId");
  }

  @ApiOperation(value = "GetContextID", notes = "Get_Context_Id", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextId", required = false, dataType = "String")})
  @RequestMapping(path = "getContextID", method = RequestMethod.GET)
  public Message getContextID(
      HttpServletRequest req, @RequestParam(value = "contextId", required = false) String id)
      throws InterruptedException, CSErrorException {
    if (StringUtils.isEmpty(id)) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, id);
    Message message = generateResponse(answerJob, "contextID");
    return message;
  }

  @ApiOperation(value = "updateContextID", notes = "update content id", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextId", dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "updateContextID", method = RequestMethod.POST)
  public Message updateContextID(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    ContextID contextID = getContextIDFromJsonNode(jsonNode);
    if (StringUtils.isEmpty(contextID.getContextId())) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.UPDATE, contextID);
    return generateResponse(answerJob, "");
  }

  @ApiOperation(value = "resetContextID", notes = "reset context Id", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextId", dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "resetContextID", method = RequestMethod.POST)
  public Message resetContextID(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException {
    if (!jsonNode.has(ContextHTTPConstant.CONTEXT_ID_STR)) {
      throw new CSErrorException(97000, ContextHTTPConstant.CONTEXT_ID_STR + " cannot be empty");
    }
    String id = jsonNode.get(ContextHTTPConstant.CONTEXT_ID_STR).textValue();
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.RESET, id);
    return generateResponse(answerJob, "");
  }

  @ApiOperation(value = "removeContextID", notes = "remove context ID", response = Message.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "contextId", dataType = "String")})
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "removeContextID", method = RequestMethod.POST)
  public Message removeContextID(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws InterruptedException, CSErrorException {
    String id = jsonNode.get("contextId").textValue();
    if (StringUtils.isEmpty(id)) {
      throw new CSErrorException(97000, "contxtId cannot be empty");
    }
    HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, id);
    return generateResponse(answerJob, "");
  }

  @ApiOperation(
      value = "searchContextIDByTime",
      notes = "search contextId by time",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "createTimeStart", dataType = "String"),
    @ApiImplicitParam(name = "createTimeEnd", dataType = "String"),
    @ApiImplicitParam(name = "updateTimeStart", dataType = "String"),
    @ApiImplicitParam(name = "updateTimeEnd", dataType = "String"),
    @ApiImplicitParam(name = "accessTimeStart", dataType = "String"),
    @ApiImplicitParam(name = "accessTimeEnd", dataType = "String"),
    @ApiImplicitParam(name = "pageNow", dataType = "String", value = "page now"),
    @ApiImplicitParam(name = "pageSize", dataType = "String", value = "page size")
  })
  @RequestMapping(path = "searchContextIDByTime", method = RequestMethod.GET)
  public Message searchContextIDByTime(
      HttpServletRequest req,
      @RequestParam(value = "createTimeStart", required = false) String createTimeStart,
      @RequestParam(value = "createTimeEnd", required = false) String createTimeEnd,
      @RequestParam(value = "updateTimeStart", required = false) String updateTimeStart,
      @RequestParam(value = "updateTimeEnd", required = false) String updateTimeEnd,
      @RequestParam(value = "accessTimeStart", required = false) String accessTimeStart,
      @RequestParam(value = "accessTimeEnd", required = false) String accessTimeEnd,
      @RequestParam(value = "pageNow", required = false) Integer paramPageNow,
      @RequestParam(value = "pageSize", required = false) Integer paramPageSize)
      throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
    String username = ModuleUserUtils.getOperationUser(req);
    if (Configuration.isNotAdmin(username)) {
      throw new CSErrorException(97018, "Only station admins are allowed.");
    }
    logger.info(
        "user: {}, searchContextIDByTime : createTimeStart : {}, createTimeEnd : {}, updateTimeStart : {}, updateTimeEnd : {}, accessTimeStart : {}, accessTimeEnd : {}, pageNow : {}, pageSize : {}.",
        username,
        createTimeStart,
        createTimeEnd,
        updateTimeStart,
        updateTimeEnd,
        accessTimeStart,
        accessTimeEnd,
        paramPageNow,
        paramPageSize);

    if (null == createTimeStart
        && null == createTimeEnd
        && null == updateTimeStart
        && null == createTimeEnd
        && null == accessTimeStart
        && null == accessTimeEnd) {
      throw new CSErrorException(
          97000,
          "createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, accessTimeStart, accessTimeEnd cannot be all null.");
    }
    int pageStart = 0;
    if (null == paramPageNow || paramPageNow <= 0) {
      pageStart = 1;
    } else {
      pageStart = paramPageNow;
    }
    int pageSize = 0;
    if (null == paramPageSize
        || paramPageSize <= 0
        || paramPageSize > CSCommonUtils.CONTEXT_MAX_PAGE_SIZE) {
      pageSize = CSCommonUtils.CONTEXT_MAX_PAGE_SIZE;
    } else {
      pageSize = paramPageSize;
    }
    Date createTimeStartDate = null;
    Date createTimeEndDate = null;
    Date updateTimeStartDate = null;
    Date updateTimeEndDate = null;
    Date accessTimeStartDate = null;
    Date accessTimeEndDate = null;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(CSCommonUtils.DEFAULT_TIME_FORMAT);
    if (StringUtils.isNotBlank(createTimeStart))
      createTimeStartDate = localDatetimeToDate(LocalDateTime.parse(createTimeStart, dtf));
    if (StringUtils.isNotBlank(createTimeEnd))
      createTimeEndDate = localDatetimeToDate(LocalDateTime.parse(createTimeEnd, dtf));
    if (StringUtils.isNotBlank(updateTimeStart))
      updateTimeStartDate = localDatetimeToDate(LocalDateTime.parse(updateTimeStart, dtf));
    if (StringUtils.isNotBlank(updateTimeEnd))
      updateTimeEndDate = localDatetimeToDate(LocalDateTime.parse(updateTimeEnd, dtf));
    if (StringUtils.isNotBlank(accessTimeStart))
      accessTimeStartDate = localDatetimeToDate(LocalDateTime.parse(accessTimeStart, dtf));
    if (StringUtils.isNotBlank(accessTimeEnd))
      accessTimeEndDate = localDatetimeToDate(LocalDateTime.parse(accessTimeEnd, dtf));
    HttpAnswerJob answerJob =
        submitRestJob(
            req,
            ServiceMethod.SEARCH,
            createTimeStartDate,
            createTimeEndDate,
            updateTimeStartDate,
            updateTimeEndDate,
            accessTimeStartDate,
            accessTimeEndDate,
            pageStart,
            pageSize);
    Message resp = generateResponse(answerJob, "contextIDs");
    resp.setMethod("/api/contextservice/searchContextIDByTime");
    return resp;
  }

  @Override
  public ServiceType getServiceType() {
    return ServiceType.CONTEXT_ID;
  }

  @Override
  public CsScheduler getScheduler() {
    return this.csScheduler;
  }
}
