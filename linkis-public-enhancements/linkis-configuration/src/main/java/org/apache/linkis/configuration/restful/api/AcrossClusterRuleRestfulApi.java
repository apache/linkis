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
import org.apache.linkis.configuration.entity.AcrossClusterRule;
import org.apache.linkis.configuration.service.AcrossClusterRuleService;
import org.apache.linkis.configuration.util.CommonUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "across cluster rule api")
@RestController
@RequestMapping(path = "/configuration/acrossClusterRule")
public class AcrossClusterRuleRestfulApi {

  @Autowired private AcrossClusterRuleService acrossClusterRuleService;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @ApiOperation(
      value = "valid acrossClusterRule",
      notes = "valid acrossClusterRule",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "Integer", value = "id"),
    @ApiImplicitParam(name = "isValid", dataType = "String", value = "isValid"),
  })
  @RequestMapping(path = "/isValid", method = RequestMethod.PUT)
  public Message isValidRule(HttpServletRequest req, @RequestBody Map<String, Object> json) {
    String operationUser = ModuleUserUtils.getOperationUser(req, "execute valid acrossClusterRule");
    String username = null;
    if (!Configuration.isAdmin(operationUser)) {
      username = operationUser;
    }

    Integer idInt = (Integer) json.get("id");
    Long id = idInt.longValue();
    String isValid = (String) json.get("isValid");

    if (StringUtils.isBlank(isValid)) {
      return Message.error("Failed to valid acrossClusterRule, Illegal Input Param: isValid");
    }

    try {
      acrossClusterRuleService.validAcrossClusterRule(id, isValid, username);
    } catch (Exception e) {
      log.info("valid acrossClusterRule failed: ", e);
      return Message.error("valid acrossClusterRule failed");
    }

    return Message.ok();
  }

  @ApiOperation(
      value = "valid acrossClusterRules by batch",
      notes = "valid acrossClusterRules by batch",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "ids", dataType = "List<Integer>", value = "ids"),
    @ApiImplicitParam(name = "isValid", dataType = "String", value = "isValid"),
  })
  @RequestMapping(path = "/isValidByBatch", method = RequestMethod.PUT)
  public Message isValidRulesByBatch(
      HttpServletRequest req, @RequestBody Map<String, Object> json) {
    String operationUser = ModuleUserUtils.getOperationUser(req, "execute valid acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to valid acrossClusterRule batch, msg: only administrators can configure");
    }

    @SuppressWarnings("unchecked")
    ArrayList<Integer> ids = (ArrayList<Integer>) json.get("ids");
    List<Long> ruleIds = new ArrayList<>();
    for (Integer id : ids) {
      ruleIds.add(id.longValue());
    }

    String isValid = (String) json.get("isValid");

    if (StringUtils.isBlank(isValid)) {
      return Message.error("Failed to valid acrossClusterRule, Illegal Input Param: isValid");
    }

    try {
      acrossClusterRuleService.validAcrossClusterRuleByBatch(ruleIds, isValid);
    } catch (Exception e) {
      log.info("valid acrossClusterRule failed: ", e);
      return Message.error("valid acrossClusterRule failed");
    }

    return Message.ok();
  }

  @ApiOperation(
      value = "query acrossClusterRule list",
      notes = "query acrossClusterRule list",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "username", dataType = "String", value = "username"),
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "clusterName"),
    @ApiImplicitParam(name = "pageNow", dataType = "Integer", value = "pageNow"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer", value = "pageSize"),
  })
  @RequestMapping(path = "/list", method = RequestMethod.GET)
  public Message queryAcrossClusterRuleList(
      HttpServletRequest req,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "clusterName", required = false) String clusterName,
      @RequestParam(value = "pageNow", required = false) Integer pageNow,
      @RequestParam(value = "pageSize", required = false) Integer pageSize) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute query acrossClusterRule List");

    if (!Configuration.isAdmin(operationUser)) {
      if (StringUtils.isNotBlank(username) && !operationUser.equals(username)) {
        username = "noexist";
      } else {
        username = operationUser;
      }
    }

    if (StringUtils.isBlank(username)) username = null;
    if (StringUtils.isBlank(creator)) creator = null;
    if (StringUtils.isBlank(clusterName)) clusterName = null;
    if (null == pageNow) pageNow = 1;
    if (null == pageSize) pageSize = 20;
    Map<String, Object> resultMap = null;
    try {
      resultMap =
          acrossClusterRuleService.queryAcrossClusterRuleList(
              creator, username, clusterName, pageNow, pageSize);
    } catch (Exception e) {
      log.info("query acrossClusterRule List failed: ", e);
      return Message.error("query acrossClusterRule List failed");
    }

    Message msg = Message.ok();
    msg.getData().putAll(resultMap);
    return msg;
  }

  @ApiOperation(
      value = "delete acrossClusterRule",
      notes = "delete acrossClusterRule",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "Integer", value = "id"),
  })
  @RequestMapping(path = "/delete", method = RequestMethod.DELETE)
  public Message deleteAcrossClusterRule(
      HttpServletRequest req, @RequestParam(value = "id", required = false) Integer id) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute delete acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to delete acrossClusterRule,msg: only administrators can configure");
    }

    try {
      acrossClusterRuleService.deleteAcrossClusterRule(id.longValue());
    } catch (Exception e) {
      log.info("delete acrossClusterRule failed: ", e);
      return Message.error("delete acrossClusterRule failed");
    }

    return Message.ok();
  }

  @ApiOperation(
      value = "delete acrossClusterRule by batch",
      notes = "delete acrossClusterRule by batch",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "ids", dataType = "List<Integer>", value = "ids"),
  })
  @RequestMapping(path = "/deleteByBatch", method = RequestMethod.PUT)
  public Message deleteAcrossClusterRuleByBatch(
      HttpServletRequest req, @RequestBody Map<String, Object> json) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute delete acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to delete acrossClusterRule,msg: only administrators can configure");
    }

    @SuppressWarnings("unchecked")
    ArrayList<Integer> ids = (ArrayList<Integer>) json.get("ids");
    List<Long> ruleIds = new ArrayList<>();
    for (Integer id : ids) {
      ruleIds.add(id.longValue());
    }

    try {
      acrossClusterRuleService.deleteAcrossClusterRuleByBatch(ruleIds);
    } catch (Exception e) {
      log.info("delete acrossClusterRule failed: ", e);
      return Message.error("delete acrossClusterRule failed");
    }

    return Message.ok();
  }

  @ApiOperation(
      value = "delete acrossClusterRule",
      notes = "delete acrossClusterRule",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "username", dataType = "String", value = "username"),
  })
  @RequestMapping(path = "/deleteByUsername", method = RequestMethod.DELETE)
  public Message deleteAcrossClusterRuleByUsername(
      HttpServletRequest req, @RequestParam(value = "username", required = false) String username) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute delete acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to delete acrossClusterRule,msg: only administrators can configure");
    }

    if (StringUtils.isBlank(username)) {
      return Message.error("Failed to delete acrossClusterRule, Illegal Input Param: username");
    }

    try {
      acrossClusterRuleService.deleteAcrossClusterRuleByUsername(username);
    } catch (Exception e) {
      log.info("delete acrossClusterRule failed：", e);
      return Message.error("delete acrossClusterRule failed, username is: " + username);
    }

    return Message.ok();
  }

  @ApiOperation(
      value = "delete acrossClusterRule",
      notes = "delete acrossClusterRule",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "crossQueue", dataType = "String", value = "crossQueue"),
  })
  @RequestMapping(path = "/deleteByCrossQueue", method = RequestMethod.DELETE)
  public Message deleteAcrossClusterRuleByCrossQueue(
      HttpServletRequest req,
      @RequestParam(value = "crossQueue", required = false) String crossQueue) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute delete acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to delete acrossClusterRule,msg: only administrators can configure");
    }

    if (StringUtils.isBlank(crossQueue)) {
      return Message.error(
          "Failed to delete acrossClusterRule, Illegal Input Param: " + crossQueue);
    }

    try {
      acrossClusterRuleService.deleteAcrossClusterRuleByCrossQueue(
          CommonUtils.concatQueue(crossQueue));
    } catch (Exception e) {
      log.info("delete acrossClusterRule failed：", e);
      return Message.error("delete acrossClusterRule failed, crossQueue is: " + crossQueue);
    }

    return Message.ok();
  }

  @ApiOperation(
      value = "update acrossClusterRule",
      notes = "update acrossClusterRule ",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "Integer", value = "id"),
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "clusterName"),
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "username", dataType = "String", value = "username"),
    @ApiImplicitParam(name = "isValid", dataType = "String", value = "isValid"),
    @ApiImplicitParam(name = "startTime", dataType = "String", value = "startTime"),
    @ApiImplicitParam(name = "endTime", dataType = "String", value = "endTime"),
    @ApiImplicitParam(name = "crossQueue", dataType = "String", value = "crossQueue"),
    @ApiImplicitParam(name = "priorityCluster", dataType = "String", value = "priorityCluster"),
    @ApiImplicitParam(
        name = "targetCPUThreshold",
        dataType = "String",
        value = "targetCPUThreshold"),
    @ApiImplicitParam(
        name = "targetMemoryThreshold",
        dataType = "String",
        value = "targetMemoryThreshold"),
    @ApiImplicitParam(
        name = "originCPUPercentageThreshold",
        dataType = "String",
        value = "originCPUPercentageThreshold"),
    @ApiImplicitParam(
        name = "originMemoryPercentageThreshold",
        dataType = "String",
        value = "originMemoryPercentageThreshold"),
    @ApiImplicitParam(
        name = "targetCPUPercentageThreshold",
        dataType = "String",
        value = "targetCPUPercentageThreshold"),
    @ApiImplicitParam(
        name = "targetMemoryPercentageThreshold",
        dataType = "String",
        value = "targetMemoryPercentageThreshold"),
  })
  @RequestMapping(path = "/update", method = RequestMethod.PUT)
  public Message updateAcrossClusterRule(
      HttpServletRequest req, @RequestBody Map<String, Object> json) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute update acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to update acrossClusterRule,msg: only administrators can configure");
    }

    Integer idInt = (Integer) json.get("id");
    Long id = idInt.longValue();
    String clusterName = (String) json.get("clusterName");
    String creator = (String) json.get("creator");
    String username = (String) json.get("username");
    String isValid = (String) json.get("isValid");
    String startTime = (String) json.get("startTime");
    String endTime = (String) json.get("endTime");
    String crossQueue = (String) json.get("crossQueue");
    String priorityCluster = (String) json.get("priorityCluster");
    String targetCPUThreshold = (String) json.get("targetCPUThreshold");
    String targetMemoryThreshold = (String) json.get("targetMemoryThreshold");
    String targetCPUPercentageThreshold = (String) json.get("targetCPUPercentageThreshold");
    String targetMemoryPercentageThreshold = (String) json.get("targetMemoryPercentageThreshold");
    String originCPUPercentageThreshold = (String) json.get("originCPUPercentageThreshold");
    String originMemoryPercentageThreshold = (String) json.get("originMemoryPercentageThreshold");
    if (StringUtils.isBlank(clusterName)
        || StringUtils.isBlank(creator)
        || StringUtils.isBlank(username)
        || StringUtils.isBlank(isValid)
        || StringUtils.isBlank(startTime)
        || StringUtils.isBlank(endTime)
        || StringUtils.isBlank(crossQueue)
        || StringUtils.isBlank(priorityCluster)
        || StringUtils.isBlank(targetCPUThreshold)
        || StringUtils.isBlank(targetMemoryThreshold)
        || StringUtils.isBlank(targetCPUPercentageThreshold)
        || StringUtils.isBlank(targetMemoryPercentageThreshold)
        || StringUtils.isBlank(originCPUPercentageThreshold)
        || StringUtils.isBlank(originMemoryPercentageThreshold)) {
      return Message.error("Failed to add acrossClusterRule, Illegal Input Param");
    }

    try {
      String rules =
          CommonUtils.ruleMap2String(
              startTime,
              endTime,
              crossQueue,
              priorityCluster,
              targetCPUThreshold,
              targetMemoryThreshold,
              targetCPUPercentageThreshold,
              targetMemoryPercentageThreshold,
              originCPUPercentageThreshold,
              originMemoryPercentageThreshold);
      AcrossClusterRule acrossClusterRule = new AcrossClusterRule();
      acrossClusterRule.setId(id);
      acrossClusterRule.setClusterName(clusterName.toLowerCase());
      acrossClusterRule.setCreator(creator);
      acrossClusterRule.setUsername(username);
      acrossClusterRule.setUpdateBy(operationUser);
      acrossClusterRule.setRules(rules);
      acrossClusterRule.setIsValid(isValid);
      acrossClusterRuleService.updateAcrossClusterRule(acrossClusterRule);
    } catch (Exception e) {
      log.info("update acrossClusterRule failed：", e);
      return Message.error("update acrossClusterRule failed, rule already exits");
    }
    return Message.ok();
  }

  @ApiOperation(
      value = "update acrossClusterRule by batch",
      notes = "update acrossClusterRule by batch",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "ids", dataType = "List<Integer>", value = "ids"),
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "clusterName"),
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "isValid", dataType = "String", value = "isValid"),
    @ApiImplicitParam(name = "startTime", dataType = "String", value = "startTime"),
    @ApiImplicitParam(name = "endTime", dataType = "String", value = "endTime"),
    @ApiImplicitParam(name = "crossQueue", dataType = "String", value = "crossQueue"),
    @ApiImplicitParam(name = "priorityCluster", dataType = "String", value = "priorityCluster"),
    @ApiImplicitParam(
        name = "targetCPUThreshold",
        dataType = "String",
        value = "targetCPUThreshold"),
    @ApiImplicitParam(
        name = "targetMemoryThreshold",
        dataType = "String",
        value = "targetMemoryThreshold"),
    @ApiImplicitParam(
        name = "originCPUPercentageThreshold",
        dataType = "String",
        value = "originCPUPercentageThreshold"),
    @ApiImplicitParam(
        name = "originMemoryPercentageThreshold",
        dataType = "String",
        value = "originMemoryPercentageThreshold"),
    @ApiImplicitParam(
        name = "targetCPUPercentageThreshold",
        dataType = "String",
        value = "targetCPUPercentageThreshold"),
    @ApiImplicitParam(
        name = "targetMemoryPercentageThreshold",
        dataType = "String",
        value = "targetMemoryPercentageThreshold"),
  })
  @RequestMapping(path = "/updateByBatch", method = RequestMethod.PUT)
  public Message updateAcrossClusterRuleByBatch(
      HttpServletRequest req, @RequestBody Map<String, Object> json) {
    String operationUser =
        ModuleUserUtils.getOperationUser(req, "execute update acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to update acrossClusterRule,msg: only administrators can configure");
    }

    @SuppressWarnings("unchecked")
    ArrayList<Integer> ids = (ArrayList<Integer>) json.get("ids");
    List<Long> ruleIds = new ArrayList<>();
    for (Integer id : ids) {
      ruleIds.add(id.longValue());
    }

    String clusterName = (String) json.get("clusterName");
    String creator = (String) json.get("creator");
    String isValid = (String) json.get("isValid");
    String startTime = (String) json.get("startTime");
    String endTime = (String) json.get("endTime");
    String crossQueue = (String) json.get("crossQueue");
    String priorityCluster = (String) json.get("priorityCluster");
    String targetCPUThreshold = (String) json.get("targetCPUThreshold");
    String targetMemoryThreshold = (String) json.get("targetMemoryThreshold");
    String targetCPUPercentageThreshold = (String) json.get("targetCPUPercentageThreshold");
    String targetMemoryPercentageThreshold = (String) json.get("targetMemoryPercentageThreshold");
    String originCPUPercentageThreshold = (String) json.get("originCPUPercentageThreshold");
    String originMemoryPercentageThreshold = (String) json.get("originMemoryPercentageThreshold");
    if (StringUtils.isBlank(clusterName)
        || StringUtils.isBlank(creator)
        || StringUtils.isBlank(isValid)
        || StringUtils.isBlank(startTime)
        || StringUtils.isBlank(endTime)
        || StringUtils.isBlank(crossQueue)
        || StringUtils.isBlank(priorityCluster)
        || StringUtils.isBlank(targetCPUThreshold)
        || StringUtils.isBlank(targetMemoryThreshold)
        || StringUtils.isBlank(targetCPUPercentageThreshold)
        || StringUtils.isBlank(targetMemoryPercentageThreshold)
        || StringUtils.isBlank(originCPUPercentageThreshold)
        || StringUtils.isBlank(originMemoryPercentageThreshold)) {
      return Message.error("Failed to add acrossClusterRule, Illegal Input Param");
    }

    try {
      String rules =
          CommonUtils.ruleMap2String(
              startTime,
              endTime,
              crossQueue,
              priorityCluster,
              targetCPUThreshold,
              targetMemoryThreshold,
              targetCPUPercentageThreshold,
              targetMemoryPercentageThreshold,
              originCPUPercentageThreshold,
              originMemoryPercentageThreshold);
      AcrossClusterRule acrossClusterRule = new AcrossClusterRule();
      acrossClusterRule.setClusterName(clusterName.toLowerCase());
      acrossClusterRule.setCreator(creator);
      acrossClusterRule.setUpdateBy(operationUser);
      acrossClusterRule.setRules(rules);
      acrossClusterRule.setIsValid(isValid);
      acrossClusterRuleService.updateAcrossClusterRuleByBatch(ruleIds, acrossClusterRule);
    } catch (Exception e) {
      log.info("update acrossClusterRule failed：", e);
      return Message.error("update acrossClusterRule failed, rule already exits");
    }
    return Message.ok();
  }

  @ApiOperation(
      value = "add acrossClusterRule",
      notes = "add acrossClusterRule ",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "clusterName", dataType = "String", value = "clusterName"),
    @ApiImplicitParam(name = "creator", dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "username", dataType = "String", value = "username"),
    @ApiImplicitParam(name = "isValid", dataType = "String", value = "isValid"),
    @ApiImplicitParam(name = "startTime", dataType = "String", value = "startTime"),
    @ApiImplicitParam(name = "endTime", dataType = "String", value = "endTime"),
    @ApiImplicitParam(name = "crossQueue", dataType = "String", value = "crossQueue"),
    @ApiImplicitParam(name = "priorityCluster", dataType = "String", value = "priorityCluster"),
    @ApiImplicitParam(
        name = "targetCPUThreshold",
        dataType = "String",
        value = "targetCPUThreshold"),
    @ApiImplicitParam(
        name = "targetMemoryThreshold",
        dataType = "String",
        value = "targetMemoryThreshold"),
    @ApiImplicitParam(
        name = "originCPUPercentageThreshold",
        dataType = "String",
        value = "originCPUPercentageThreshold"),
    @ApiImplicitParam(
        name = "originMemoryPercentageThreshold",
        dataType = "String",
        value = "originMemoryPercentageThreshold"),
    @ApiImplicitParam(
        name = "targetCPUPercentageThreshold",
        dataType = "String",
        value = "targetCPUPercentageThreshold"),
    @ApiImplicitParam(
        name = "targetMemoryPercentageThreshold",
        dataType = "String",
        value = "targetMemoryPercentageThreshold"),
  })
  @RequestMapping(path = "/add", method = RequestMethod.POST)
  public Message insertAcrossClusterRule(
      HttpServletRequest req, @RequestBody Map<String, Object> json) {
    String operationUser = ModuleUserUtils.getOperationUser(req, "execute add acrossClusterRule");
    if (!Configuration.isAdmin(operationUser)) {
      return Message.error(
          "Failed to add acrossClusterRule,msg: only administrators can configure");
    }

    String clusterName = (String) json.get("clusterName");
    String creator = (String) json.get("creator");
    String username = (String) json.get("username");
    String isValid = (String) json.get("isValid");
    String startTime = (String) json.get("startTime");
    String endTime = (String) json.get("endTime");
    String crossQueue = (String) json.get("crossQueue");
    String priorityCluster = (String) json.get("priorityCluster");
    String targetCPUThreshold = (String) json.get("targetCPUThreshold");
    String targetMemoryThreshold = (String) json.get("targetMemoryThreshold");
    String targetCPUPercentageThreshold = (String) json.get("targetCPUPercentageThreshold");
    String targetMemoryPercentageThreshold = (String) json.get("targetMemoryPercentageThreshold");
    String originCPUPercentageThreshold = (String) json.get("originCPUPercentageThreshold");
    String originMemoryPercentageThreshold = (String) json.get("originMemoryPercentageThreshold");
    if (StringUtils.isBlank(clusterName)
        || StringUtils.isBlank(creator)
        || StringUtils.isBlank(username)
        || StringUtils.isBlank(isValid)
        || StringUtils.isBlank(startTime)
        || StringUtils.isBlank(endTime)
        || StringUtils.isBlank(crossQueue)
        || StringUtils.isBlank(priorityCluster)
        || StringUtils.isBlank(targetCPUThreshold)
        || StringUtils.isBlank(targetMemoryThreshold)
        || StringUtils.isBlank(targetCPUPercentageThreshold)
        || StringUtils.isBlank(targetMemoryPercentageThreshold)
        || StringUtils.isBlank(originCPUPercentageThreshold)
        || StringUtils.isBlank(originMemoryPercentageThreshold)) {
      return Message.error("Failed to add acrossClusterRule, Illegal Input Param");
    }

    try {
      String rules =
          CommonUtils.ruleMap2String(
              startTime,
              endTime,
              crossQueue,
              priorityCluster,
              targetCPUThreshold,
              targetMemoryThreshold,
              targetCPUPercentageThreshold,
              targetMemoryPercentageThreshold,
              originCPUPercentageThreshold,
              originMemoryPercentageThreshold);
      AcrossClusterRule acrossClusterRule = new AcrossClusterRule();
      acrossClusterRule.setClusterName(clusterName.toLowerCase());
      acrossClusterRule.setCreator(creator);
      acrossClusterRule.setUsername(username);
      acrossClusterRule.setCreateBy(operationUser);
      acrossClusterRule.setUpdateBy(operationUser);
      acrossClusterRule.setRules(rules);
      acrossClusterRule.setIsValid(isValid);
      acrossClusterRuleService.insertAcrossClusterRule(acrossClusterRule);
    } catch (Exception e) {
      log.info("add acrossClusterRule failed：", e);
      return Message.error("add acrossClusterRule failed, rule already exits");
    }

    return Message.ok();
  }
}
