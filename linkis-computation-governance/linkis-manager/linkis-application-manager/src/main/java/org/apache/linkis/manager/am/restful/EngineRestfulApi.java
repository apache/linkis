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

package org.apache.linkis.manager.am.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.constant.ec.ECConstants;
import org.apache.linkis.governance.common.utils.JobUtils;
import org.apache.linkis.governance.common.utils.LoggerUtils;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.manager.EngineNodeManager;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.service.engine.*;
import org.apache.linkis.manager.am.util.ECResourceInfoUtils;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.am.vo.AMEngineNodeVo;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.common.protocol.engine.*;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "EC(engineconn) operation")
@RequestMapping(
    path = "/linkisManager",
    produces = {"application/json"})
@RestController
public class EngineRestfulApi {

  @Autowired private EngineInfoService engineInfoService;

  @Autowired private EngineAskEngineService engineAskService;
  @Autowired private EngineCreateService engineCreateService;

  @Autowired private EngineNodeManager engineNodeManager;

  @Autowired private EngineOperateService engineOperateService;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private EngineStopService engineStopService;

  @Autowired private ECResourceInfoService ecResourceInfoService;

  @Autowired private EngineReuseService engineReuseService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private LabelBuilderFactory stdLabelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  private static final Logger logger = LoggerFactory.getLogger(EngineRestfulApi.class);

  @ApiOperation(value = "askEngineConn", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/askEngineConn", method = RequestMethod.POST)
  public Message askEngineConn(
      HttpServletRequest req, @RequestBody EngineAskRequest engineAskRequest)
      throws IOException, InterruptedException {
    String userName = ModuleUserUtils.getOperationUser(req, "askEngineConn");
    engineAskRequest.setUser(userName);
    long timeout = engineAskRequest.getTimeOut();
    if (timeout <= 0) {
      timeout = AMConfiguration.ENGINE_CONN_START_REST_MAX_WAIT_TIME.getValue().toLong();
      engineAskRequest.setTimeOut(timeout);
    }
    Map<String, Object> retEngineNode = new HashMap<>();
    logger.info(
        "User {} try to ask an engineConn with maxStartTime {}. EngineAskRequest is {}.",
        userName,
        ByteTimeUtils.msDurationToString(timeout),
        engineAskRequest);
    Sender sender = Sender.getSender(Sender.getThisServiceInstance());
    EngineNode engineNode = null;

    // try to reuse ec first
    String taskId = JobUtils.getJobIdFromStringMap(engineAskRequest.getProperties());
    LoggerUtils.setJobIdMDC(taskId);
    logger.info("received task : {}, engineAskRequest : {}", taskId, engineAskRequest);
    if (!engineAskRequest.getLabels().containsKey(LabelKeyConstant.EXECUTE_ONCE_KEY)) {
      EngineReuseRequest engineReuseRequest = new EngineReuseRequest();
      engineReuseRequest.setLabels(engineAskRequest.getLabels());
      engineReuseRequest.setTimeOut(engineAskRequest.getTimeOut());
      engineReuseRequest.setUser(engineAskRequest.getUser());
      engineReuseRequest.setProperties(engineAskRequest.getProperties());
      boolean end = false;
      EngineNode reuseNode = null;
      int count = 0;
      int MAX_RETRY = 2;
      while (!end) {
        try {
          reuseNode = engineReuseService.reuseEngine(engineReuseRequest, sender);
          end = true;
        } catch (LinkisRetryException e) {
          logger.error(
              "task: {}, user: {} reuse engine failed", taskId, engineReuseRequest.getUser(), e);
          Thread.sleep(1000);
          end = false;
          count += 1;
          if (count > MAX_RETRY) {
            end = true;
          }
        } catch (Exception e1) {
          logger.info(
              "task: {} user: {} reuse engine failed", taskId, engineReuseRequest.getUser(), e1);
          end = true;
        }
      }
      if (null != reuseNode) {
        logger.info(
            "Finished to ask engine for task: {}, user: {} by reuse node {}",
            taskId,
            engineReuseRequest.getUser(),
            reuseNode);
        LoggerUtils.removeJobIdMDC();
        engineNode = reuseNode;
      }
    }

    if (null != engineNode) {
      fillResultEngineNode(retEngineNode, engineNode);
      return Message.ok("reuse engineConn ended.").data("engine", retEngineNode);
    }

    String engineAskAsyncId = EngineAskEngineService$.MODULE$.getAsyncId();
    Callable<Object> createECTask =
        new Callable() {
          @Override
          public Object call() {
            LoggerUtils.setJobIdMDC(taskId);
            logger.info(
                "Task: {}, start to async({}) createEngine: {}",
                taskId,
                engineAskAsyncId,
                engineAskRequest.getCreateService());
            // remove engineInstance label if exists
            engineAskRequest.getLabels().remove("engineInstance");
            EngineCreateRequest engineCreateRequest = new EngineCreateRequest();
            engineCreateRequest.setLabels(engineAskRequest.getLabels());
            engineCreateRequest.setTimeout(engineAskRequest.getTimeOut());
            engineCreateRequest.setUser(engineAskRequest.getUser());
            engineCreateRequest.setProperties(engineAskRequest.getProperties());
            engineCreateRequest.setCreateService(engineAskRequest.getCreateService());
            try {
              EngineNode createNode = engineCreateService.createEngine(engineCreateRequest, sender);
              long timeout = 0L;
              if (engineCreateRequest.getTimeout() <= 0) {
                timeout = AMConfiguration.ENGINE_START_MAX_TIME.getValue().toLong();
              } else {
                timeout = engineCreateRequest.getTimeout();
              }
              // useEngine need to add timeout
              EngineNode createEngineNode = engineNodeManager.useEngine(createNode, timeout);
              if (null == createEngineNode) {
                throw new LinkisRetryException(
                    AMConstant.EM_ERROR_CODE,
                    "create engine${createNode.getServiceInstance} success, but to use engine failed");
              }
              logger.info(
                  "Task: $taskId finished to ask engine for user ${engineAskRequest.getUser} by create node $createEngineNode");
              return createEngineNode;
            } catch (Exception e) {
              logger.error(
                  "Task: {} failed to ask engine for user {} by create node", taskId, userName, e);
              return new LinkisRetryException(AMConstant.EM_ERROR_CODE, e.getMessage());
            } finally {
              LoggerUtils.removeJobIdMDC();
            }
          }
        };

    try {
      Object rs = createECTask.call();
      if (rs instanceof LinkisRetryException) {
        throw (LinkisRetryException) rs;
      } else {
        engineNode = (EngineNode) rs;
      }
    } catch (LinkisRetryException retryException) {
      logger.error(
          "User {} create engineConn failed get retry  exception. can be Retry",
          userName,
          retryException);
      return Message.error(
              String.format(
                  "Create engineConn failed, caused by %s.",
                  ExceptionUtils.getRootCauseMessage(retryException)))
          .data("canRetry", true);
    } catch (Exception e) {
      LoggerUtils.removeJobIdMDC();
      logger.error("User {} create engineConn failed get retry  exception", userName, e);
      return Message.error(
          String.format(
              "Create engineConn failed, caused by %s.", ExceptionUtils.getRootCauseMessage(e)));
    }

    LoggerUtils.removeJobIdMDC();
    fillResultEngineNode(retEngineNode, engineNode);
    logger.info(
        "Finished to create a engineConn for user {}. NodeInfo is {}.", userName, engineNode);
    // to transform to a map
    return Message.ok("create engineConn ended.").data("engine", retEngineNode);
  }

  private void fillNullNode(
      Map<String, Object> retEngineNode, EngineAskAsyncResponse askAsyncResponse) {
    retEngineNode.put(AMConstant.EC_ASYNC_START_RESULT_KEY, AMConstant.EC_ASYNC_START_RESULT_FAIL);
    retEngineNode.put(
        AMConstant.EC_ASYNC_START_FAIL_MSG_KEY,
        "Got null response for asyId : " + askAsyncResponse.id());
    retEngineNode.put(ECConstants.MANAGER_SERVICE_INSTANCE_KEY(), Sender.getThisServiceInstance());
  }

  private void fillResultEngineNode(Map<String, Object> retEngineNode, EngineNode engineNode) {
    retEngineNode.put(
        AMConstant.EC_ASYNC_START_RESULT_KEY, AMConstant.EC_ASYNC_START_RESULT_SUCCESS);
    retEngineNode.put("serviceInstance", engineNode.getServiceInstance());
    if (null == engineNode.getNodeStatus()) {
      engineNode.setNodeStatus(NodeStatus.Starting);
    }
    retEngineNode.put(ECConstants.NODE_STATUS_KEY(), engineNode.getNodeStatus().toString());
    retEngineNode.put(ECConstants.EC_TICKET_ID_KEY(), engineNode.getTicketId());
    EMNode emNode = engineNode.getEMNode();
    if (null != emNode) {
      retEngineNode.put(
          ECConstants.ECM_SERVICE_INSTANCE_KEY(), engineNode.getEMNode().getServiceInstance());
    }
    retEngineNode.put(ECConstants.MANAGER_SERVICE_INSTANCE_KEY(), Sender.getThisServiceInstance());
  }

  @ApiOperation(value = "createEngineConn", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/createEngineConn", method = RequestMethod.POST)
  public Message createEngineConn(
      HttpServletRequest req, @RequestBody EngineCreateRequest engineCreateRequest)
      throws IOException, InterruptedException {
    String userName = ModuleUserUtils.getOperationUser(req, "createEngineConn");
    engineCreateRequest.setUser(userName);
    long timeout = engineCreateRequest.getTimeout();
    if (timeout <= 0) {
      timeout = AMConfiguration.ENGINE_CONN_START_REST_MAX_WAIT_TIME.getValue().toLong();
      engineCreateRequest.setTimeout(timeout);
    }
    logger.info(
        "User {} try to create a engineConn with maxStartTime {}. EngineCreateRequest is {}.",
        userName,
        ByteTimeUtils.msDurationToString(timeout),
        engineCreateRequest);
    Sender sender = Sender.getSender(Sender.getThisServiceInstance());
    EngineNode engineNode;
    try {
      engineNode = engineCreateService.createEngine(engineCreateRequest, sender);
    } catch (LinkisRetryException e) {
      logger.error(
          "User {} create engineConn failed get retry  exception. can be Retry", userName, e);
      return Message.error(
              String.format(
                  "Create engineConn failed, caused by %s.", ExceptionUtils.getRootCauseMessage(e)))
          .data("canRetry", true);
    } catch (Exception e) {
      logger.error(String.format("User %s create engineConn failed.", userName), e);
      return Message.error(
          String.format(
              "Create engineConn failed, caused by %s.", ExceptionUtils.getRootCauseMessage(e)));
    }
    logger.info(
        "Finished to create a engineConn for user {}. NodeInfo is {}.", userName, engineNode);
    // to transform to a map
    Map<String, Object> retEngineNode = new HashMap<>();
    fillResultEngineNode(retEngineNode, engineNode);
    return Message.ok("create engineConn succeed.").data("engine", retEngineNode);
  }

  @ApiOperation(value = "getEngineConn", notes = "get engineconn", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/getEngineConn", method = RequestMethod.POST)
  public Message getEngineConn(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String userName = ModuleUserUtils.getOperationUser(req, "getEngineConn");
    ServiceInstance serviceInstance = getServiceInstance(jsonNode);
    JsonNode ticketIdNode = jsonNode.get("ticketId");
    EngineNode engineNode = null;
    try {
      engineNode = engineNodeManager.getEngineNodeInfo(serviceInstance);
    } catch (Exception e) {
      logger.info("Instances {} does not exist", serviceInstance.getInstance());
    }
    String ecMetrics = null;
    if (null == engineNode) {
      ECResourceInfoRecord ecInfo = null;
      if (null != ticketIdNode) {
        try {
          ecInfo = ecResourceInfoService.getECResourceInfoRecord(ticketIdNode.asText());
        } catch (Exception e) {
          logger.info("TicketId  {} does not exist", ticketIdNode.asText());
        }
      }
      if (null == ecInfo) {
        ecInfo =
            ecResourceInfoService.getECResourceInfoRecordByInstance(serviceInstance.getInstance());
      }
      if (null == ecInfo) {
        return Message.error("Instance does not exist " + serviceInstance);
      }
      if (null == ecMetrics) {
        ecMetrics = ecInfo.getMetrics();
      }
      engineNode = ECResourceInfoUtils.convertECInfoTOECNode(ecInfo);
    } else {
      ecMetrics = engineNode.getEcMetrics();
    }
    if (!userName.equals(engineNode.getOwner()) && Configuration.isNotAdmin(userName)) {
      return Message.error("You have no permission to access EngineConn " + serviceInstance);
    }
    Message result = Message.ok().data("engine", engineNode);
    result.data(AMConstant.EC_METRICS_KEY, ecMetrics);
    return result;
  }

  @ApiOperation(value = "kill egineconn", notes = "kill engineconn", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/killEngineConn", method = RequestMethod.POST)
  public Message killEngineConn(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws Exception {

    ServiceInstance serviceInstance = getServiceInstance(jsonNode);
    String userName = ModuleUserUtils.getOperationUser(req, "killEngineConn：" + serviceInstance);
    logger.info("User {} try to kill engineConn {}.", userName, serviceInstance);
    EngineNode engineNode = engineNodeManager.getEngineNode(serviceInstance);
    if (!userName.equals(engineNode.getOwner()) && Configuration.isNotAdmin(userName)) {
      return Message.error("You have no permission to kill EngineConn " + serviceInstance);
    }
    EngineStopRequest stopEngineRequest = new EngineStopRequest(serviceInstance, userName);
    Sender sender = Sender.getSender(Sender.getThisServiceInstance());
    engineStopService.stopEngine(stopEngineRequest, sender);
    logger.info("Finished to kill engineConn {}.", serviceInstance);
    return Message.ok("Kill engineConn succeed.");
  }

  @ApiOperation(value = "kill egineconns of a ecm", notes = "", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "instance",
        dataType = "String",
        required = true,
        example = "bdp110:9210")
  })
  @ApiOperationSupport(ignoreParameters = {"param"})
  @RequestMapping(path = "/rm/killUnlockEngineByEM", method = RequestMethod.POST)
  public Message killUnlockEngine(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {

    JsonNode ecmInstance = jsonNode.get("instance");
    if (null == ecmInstance || StringUtils.isBlank(ecmInstance.textValue())) {
      throw new AMErrorException(
          210003, "instance is null in the parameters of the request(请求参数中【instance】为空)");
    }

    String operatMsg =
        MessageFormat.format("kill the unlock engines of ECM:{0}", ecmInstance.textValue());

    String userName = ModuleUserUtils.getOperationUser(req, operatMsg);
    if (Configuration.isNotAdmin(userName)) {
      throw new AMErrorException(
          210003,
          "Only admin can kill unlock engine of the specified ecm(只有管理员才能 kill 指定 ecm 下的所有空闲引擎).");
    }

    Map result = engineStopService.stopUnlockEngineByECM(ecmInstance.textValue(), userName);

    return Message.ok("Kill engineConn succeed.").data("result", result);
  }

  @ApiOperation(
      value = "kill eginecon",
      notes = "kill one engineconn or more ",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "engineInstance", dataType = "String", example = "bdp110:12295"),
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconn")
  })
  @ApiOperationSupport(ignoreParameters = {"param"})
  @RequestMapping(path = "/rm/enginekill", method = RequestMethod.POST)
  public Message killEngine(HttpServletRequest req, @RequestBody Map<String, String>[] param) {
    String userName = ModuleUserUtils.getOperationUser(req, "enginekill");

    Sender sender = Sender.getSender(Sender.getThisServiceInstance());
    for (Map<String, String> engineParam : param) {
      String moduleName = engineParam.get("applicationName");
      String engineInstance = engineParam.get("engineInstance");
      logger.info("try to kill engine with engineInstance:{}", engineInstance);
      EngineStopRequest stopEngineRequest =
          new EngineStopRequest(ServiceInstance.apply(moduleName, engineInstance), userName);
      engineStopService.stopEngine(stopEngineRequest, sender);
    }
    logger.info("Finished to kill engines");
    return Message.ok("Kill engineConn succeed.");
  }

  @ApiOperationSupport(ignoreParameters = {"param"})
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "instances",
        dataType = "Array",
        example = "[\"bdp110:12295\",\"bdp110:12296\"]")
  })
  @RequestMapping(path = "/rm/enginekillAsyn", method = RequestMethod.POST)
  public Message killEngineAsyn(HttpServletRequest req, @RequestBody JsonNode jsonNode) {
    String username = ModuleUserUtils.getOperationUser(req, "enginekill");
    String token = ModuleUserUtils.getToken(req);

    // check special token
    if (StringUtils.isNotBlank(token)) {
      if (!Configuration.isAdminToken(token)) {
        logger.warn("Token {} has no permission to asyn kill engines.", token);
        return Message.error("Token:" + token + " has no permission to asyn kill engines.");
      }
    } else if (!Configuration.isAdmin(username)) {
      logger.warn("User {} has no permission to asyn kill engines.", username);
      return Message.error("User:" + username + " has no permission to asyn kill engines.");
    }

    JsonNode instancesParam = jsonNode.get("instances");

    if (instancesParam == null || instancesParam.size() == 0) {
      return Message.error(
          "instances is null in the parameters of the request(请求参数中【instances】为空)");
    }

    List<String> instancesList = null;
    try {
      instancesList =
          JsonUtils.jackson()
              .readValue(instancesParam.toString(), new TypeReference<List<String>>() {});
    } catch (JsonProcessingException e) {
      return Message.error("instances parameters parsing failed(请求参数【instances】解析失败)");
    }

    for (String engineInstance : instancesList) {
      String moduleName = GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue();
      EngineStopRequest stopEngineRequest =
          new EngineStopRequest(ServiceInstance.apply(moduleName, engineInstance), username);
      engineStopService.asyncStopEngineWithUpdateMetrics(stopEngineRequest);
    }
    logger.info("Finished to kill engines");
    return Message.ok("Kill engineConn succeed.");
  }

  @ApiOperation(
      value = "listUserEngines",
      notes = "get user's engineconn list",
      response = Message.class)
  @RequestMapping(path = "/listUserEngines", method = RequestMethod.GET)
  public Message listUserEngines(HttpServletRequest req) {
    String userName = ModuleUserUtils.getOperationUser(req, "listUserEngines");
    List<EngineNode> engineNodes = engineInfoService.listUserEngines(userName);
    return Message.ok().data("engines", engineNodes);
  }

  @ApiOperation(
      value = "listEMEngines",
      notes = "get the list of engineconn under an ECM",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "em", required = false, dataType = "Map", value = "ecm object"),
    @ApiImplicitParam(name = "serviceInstance", dataType = "Map"),
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconnmanager"),
    @ApiImplicitParam(name = "instance", required = false, dataType = "String", value = "instance"),
    @ApiImplicitParam(name = "emInstance", dataType = "String", example = "bdp110:9102"),
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "nodeStatus", dataType = "String"),
    @ApiImplicitParam(name = "owner", dataType = "String", value = "owner")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/listEMEngines", method = RequestMethod.POST)
  public Message listEMEngines(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws IOException, AMErrorException {
    String username = ModuleUserUtils.getOperationUser(req, "listEMEngines");
    if (Configuration.isNotAdmin(username)) {
      throw new AMErrorException(
          210003, "Only admin can search engine information(只有管理员才能查询所有引擎信息).");
    }
    AMEMNode amemNode = objectMapper.treeToValue(jsonNode.get("em"), AMEMNode.class);
    JsonNode emInstace = jsonNode.get("emInstance");
    JsonNode nodeStatus = jsonNode.get("nodeStatus");
    JsonNode engineType = jsonNode.get("engineType");
    JsonNode owner = jsonNode.get("owner");
    List<EngineNode> engineNodes = engineInfoService.listEMEngines(amemNode);
    List<AMEngineNodeVo> allengineNodes = AMUtils.copyToAMEngineNodeVo(engineNodes);
    if (CollectionUtils.isNotEmpty(allengineNodes)) {
      Stream<AMEngineNodeVo> stream = allengineNodes.stream();
      if (null != emInstace) {
        stream =
            stream.filter(
                em ->
                    StringUtils.isNotBlank(em.getInstance())
                        && em.getInstance().contains(emInstace.asText()));
      }
      if (null != nodeStatus && StringUtils.isNotBlank(nodeStatus.asText())) {
        stream =
            stream.filter(
                em ->
                    null != em.getNodeStatus()
                        && em.getNodeStatus().equals(NodeStatus.valueOf(nodeStatus.asText())));
      }
      if (null != owner && StringUtils.isNotBlank(owner.asText())) {
        stream =
            stream.filter(
                em ->
                    StringUtils.isNotBlank(em.getOwner())
                        && em.getOwner().equalsIgnoreCase(owner.asText()));
      }
      if (null != engineType && StringUtils.isNotBlank(engineType.asText())) {
        stream =
            stream.filter(
                em ->
                    StringUtils.isNotBlank(em.getEngineType())
                        && em.getEngineType().equalsIgnoreCase(engineType.asText()));
      }
      allengineNodes = stream.collect(Collectors.toList());
    }
    return Message.ok().data("engines", allengineNodes);
  }

  @ApiOperation(
      value = "modifyEngineInfo",
      notes = "modify engineconn info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconn"),
    @ApiImplicitParam(
        name = "emStatus",
        dataType = "String",
        example = "Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success"),
    @ApiImplicitParam(name = "instance", dataType = "String", example = "bdp110:12295"),
    @ApiImplicitParam(name = "labels", dataType = "List", required = false, value = "labels"),
    @ApiImplicitParam(name = "labelKey", dataType = "String", example = "engineInstance"),
    @ApiImplicitParam(name = "stringValue", dataType = "String", example = "linkis-cg:12295"),
    @ApiImplicitParam(name = "nodeHealthy", dataType = "String", example = "UnHealthy")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/modifyEngineInfo", method = RequestMethod.PUT)
  public Message modifyEngineInfo(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException, LabelErrorException {
    String username = ModuleUserUtils.getOperationUser(req, "modifyEngineInfo");
    if (Configuration.isNotAdmin(username)) {
      throw new AMErrorException(
          210003, "Only admin can modify engineConn information(只有管理员才能修改引擎信息).");
    }
    ServiceInstance serviceInstance = getServiceInstance(jsonNode);

    JsonNode labels = jsonNode.get("labels");
    Set<String> labelKeySet = new HashSet<>();
    if (labels != null) {
      ArrayList<Label<?>> newLabelList = new ArrayList<>();
      Iterator<JsonNode> iterator = labels.iterator();
      while (iterator.hasNext()) {
        JsonNode label = iterator.next();
        String labelKey = label.get("labelKey").asText();
        String stringValue = label.get("stringValue").asText();
        Label newLabel = stdLabelBuilderFactory.createLabel(labelKey, stringValue);
        if (newLabel instanceof UserModifiable) {
          ((UserModifiable) newLabel).valueCheck(stringValue);
        }
        labelKeySet.add(labelKey);
        newLabelList.add(newLabel);
      }
      if (labelKeySet.size() != newLabelList.size()) {
        throw new AMErrorException(
            210003, "Failed to update label, include repeat label(更新label失败，包含重复label)");
      }
      nodeLabelService.updateLabelsToNode(serviceInstance, newLabelList);
      logger.info("success to update label of instance: " + serviceInstance.getInstance());
    }

    // 修改引擎健康状态，只支持 Healthy和 UnHealthy
    String healthyKey = "Healthy";
    String unHealthyKey = "UnHealthy";
    JsonNode nodeHealthy = jsonNode.get("nodeHealthy");
    if (nodeHealthy != null && healthyKey.equals(nodeHealthy.asText())) {
      engineInfoService.updateEngineHealthyStatus(serviceInstance, NodeHealthy.Healthy);
    } else if (nodeHealthy != null && unHealthyKey.equals(nodeHealthy.asText())) {
      engineInfoService.updateEngineHealthyStatus(serviceInstance, NodeHealthy.UnHealthy);
    }
    return Message.ok("success to update engine information(更新引擎信息成功)");
  }

  @ApiOperation(
      value = "batchSetEngineToUnHealthy",
      notes = "batch set engine to unHealthy",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "instances",
        dataType = "String",
        example =
            "[{\"instance\":\"bdplinkis1001:38701\",\"engineType\":\"spark\",\"applicationName\":\"linkis-cg-engineconn\"}]")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/batchSetEngineToUnHealthy", method = RequestMethod.POST)
  public Message batchSetEngineToUnHealthy(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String username = ModuleUserUtils.getOperationUser(req, "batchSetEngineToUnHealthy");
    if (Configuration.isNotAdmin(username)) {
      throw new AMErrorException(
          210003, "Only admin can modify engineConn healthy info(只有管理员才能修改引擎健康信息).");
    }

    JsonNode instances = jsonNode.get("instances");
    if (instances != null) {
      Iterator<JsonNode> iterator = instances.iterator();
      while (iterator.hasNext()) {
        JsonNode instanceNode = iterator.next();
        ServiceInstance serviceInstance = getServiceInstance(instanceNode);
        engineInfoService.updateEngineHealthyStatus(serviceInstance, NodeHealthy.UnHealthy);
      }
    }
    logger.info("success to batch update engine status to UnHealthy.");
    return Message.ok("success to update engine information(批量更新引擎健康信息成功)");
  }

  @ApiOperation(
      value = "listAllNodeHealthyStatus",
      notes = "get all node healthy staus list",
      response = Message.class)
  @RequestMapping(path = "/listAllNodeHealthyStatus", method = RequestMethod.GET)
  public Message listAllNodeHealthyStatus(
      HttpServletRequest req,
      @RequestParam(value = "onlyEditable", required = false) Boolean onlyEditable) {
    NodeStatus[] nodeStatus = NodeStatus.values();
    return Message.ok().data("nodeStatus", nodeStatus);
  }

  @ApiOperation(
      value = "executeEngineConnOperation",
      notes = "execute engine conn operation",
      response = Message.class)
  @RequestMapping(path = "/executeEngineConnOperation", method = RequestMethod.POST)
  public Message executeEngineConnOperation(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws Exception {
    String userName = ModuleUserUtils.getOperationUser(req, "executeEngineConnOperation");
    ServiceInstance serviceInstance = getServiceInstance(jsonNode);
    logger.info("User {} try to execute Engine Operation {}.", userName, serviceInstance);
    EngineNode engineNode = engineNodeManager.getEngineNode(serviceInstance);
    if (null == engineNode) {
      return Message.ok()
          .data("isError", true)
          .data("errorMsg", "Ec : " + serviceInstance.toString() + " not found.");
    }
    if (!userName.equals(engineNode.getOwner()) && Configuration.isNotAdmin(userName)) {
      return Message.error("You have no permission to execute Engine Operation " + serviceInstance);
    }
    Map<String, Object> parameters =
        objectMapper.convertValue(
            jsonNode.get("parameters"), new TypeReference<Map<String, Object>>() {});

    EngineOperateRequest engineOperateRequest = new EngineOperateRequest(userName, parameters);
    EngineOperateResponse engineOperateResponse =
        engineOperateService.executeOperation(engineNode, engineOperateRequest);
    return Message.ok()
        .data("result", engineOperateResponse.getResult())
        .data("errorMsg", engineOperateResponse.errorMsg())
        .data("isError", engineOperateResponse.isError());
  }

  @ApiOperation(
      value = "kill egineconns of a ecm",
      notes = "Kill engine by cteator or engineType",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "creator", dataType = "String", required = true, example = "IDE"),
    @ApiImplicitParam(
        name = "engineType",
        dataType = "String",
        required = true,
        example = "hive-2.3.3"),
  })
  @ApiOperationSupport(ignoreParameters = {"param"})
  @RequestMapping(path = "/rm/killEngineByCreatorEngineType", method = RequestMethod.POST)
  public Message killEngineByUpdateConfig(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String userName = ModuleUserUtils.getOperationUser(req);
    String jvmUser = StorageUtils.getJvmUser();
    if (jvmUser.equals(userName)) {
      return Message.error(
          jvmUser + " users do not support this feature (" + jvmUser + " 用户不支持此功能)");
    }
    JsonNode creator = jsonNode.get("creator");
    if (null == creator || StringUtils.isBlank(creator.textValue())) {
      return Message.error("instance is null in the parameters of the request(请求参数中【creator】为空)");
    }
    String creatorStr = Configuration.getGlobalCreator(creator.textValue());
    String engineType = "";
    if (null != jsonNode.get("engineType")) {
      engineType = jsonNode.get("engineType").textValue();
    }
    if (StringUtils.isNotBlank(engineType)
        && AMConfiguration.isUnAllowKilledEngineType(engineType)) {
      return Message.error("multi user engine does not support this feature(多用户引擎不支持此功能)");
    }
    if (Configuration.GLOBAL_CONF_SYMBOL().equals(engineType)) {
      Arrays.stream(AMConfiguration.UDF_KILL_ENGINE_TYPE.split(","))
          .forEach(
              engine ->
                  engineStopService.stopUnlockECByUserCreatorAndECType(
                      userName, creatorStr, engine));
    } else {
      engineStopService.stopUnlockECByUserCreatorAndECType(userName, creatorStr, engineType);
    }
    return Message.ok("Kill engineConn succeed");
  }

  static ServiceInstance getServiceInstance(JsonNode jsonNode) throws AMErrorException {
    String applicationName = jsonNode.get("applicationName").asText();
    String instance = jsonNode.get("instance").asText();
    if (StringUtils.isEmpty(applicationName)) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getErrorCode(),
          "applicationName cannot be null(请求参数applicationName不能为空)");
    }
    if (StringUtils.isEmpty(instance)) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getErrorCode(), "instance cannot be null(请求参数instance不能为空)");
    }
    return ServiceInstance.apply(applicationName, instance);
  }
}
