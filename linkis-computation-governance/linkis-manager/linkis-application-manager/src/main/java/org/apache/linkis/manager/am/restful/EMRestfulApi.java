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
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.converter.DefaultMetricsConverter;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.manager.EngineNodeManager;
import org.apache.linkis.manager.am.service.em.ECMOperateService;
import org.apache.linkis.manager.am.service.em.EMInfoService;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.am.vo.EMNodeVo;
import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.protocol.OperateRequest$;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest$;
import org.apache.linkis.manager.common.protocol.em.ECMOperateResponse;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "ECM(engineconnmanager) operation")
@RequestMapping(
    path = "/linkisManager",
    produces = {"application/json"})
@RestController
public class EMRestfulApi {

  @Autowired private EMInfoService emInfoService;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private DefaultMetricsConverter defaultMetricsConverter;

  @Autowired private EngineNodeManager engineNodeManager;

  @Autowired private ECMOperateService ecmOperateService;

  private LabelBuilderFactory stdLabelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  private Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);

  private String[] adminOperations = AMConfiguration.ECM_ADMIN_OPERATIONS().getValue().split(",");

  private void checkAdmin(String userName) throws AMErrorException {
    if (AMConfiguration.isNotAdmin(userName)) {
      throw new AMErrorException(210003, "Only admin can modify ECMs(只有管理员才能修改ECM).");
    }
  }

  @ApiOperation(value = "listAllEMs", notes = "get all ECM service list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "instance", dataType = "String", value = "Instance"),
    @ApiImplicitParam(
        name = "nodeHealthy",
        dataType = "String",
        value = "node  healthy status",
        example = "Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable"),
    @ApiImplicitParam(name = "owner", required = false, dataType = "String", value = "Owner")
  })
  // todo add healthInfo
  @RequestMapping(path = "/listAllEMs", method = RequestMethod.GET)
  public Message listAllEMs(
      HttpServletRequest req,
      @RequestParam(value = "instance", required = false) String instance,
      @RequestParam(value = "nodeHealthy", required = false) String nodeHealthy,
      @RequestParam(value = "owner", required = false) String owner)
      throws AMErrorException {
    checkAdmin(ModuleUserUtils.getOperationUser(req, "listAllEMs"));
    List<EMNodeVo> emNodeVos = AMUtils.copyToEMVo(emInfoService.getAllEM());
    if (CollectionUtils.isNotEmpty(emNodeVos)) {
      Stream<EMNodeVo> stream = emNodeVos.stream();
      if (StringUtils.isNotBlank(instance)) {
        stream = stream.filter(em -> em.getInstance().contains(instance));
      }
      if (StringUtils.isNotBlank(nodeHealthy)) {
        stream =
            stream.filter(
                em ->
                    em.getNodeHealthy() == null
                        || em.getNodeHealthy().equals(NodeHealthy.valueOf(nodeHealthy)));
      }
      if (StringUtils.isNotBlank(owner)) {
        stream = stream.filter(em -> em.getOwner().equalsIgnoreCase(owner));
      }
      emNodeVos = stream.collect(Collectors.toList());
    }
    return Message.ok().data("EMs", emNodeVos);
  }

  @ApiOperation(
      value = "listAllECMHealthyStatus",
      notes = "get all ECM healthy status",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "onlyEditable", dataType = "Boolean", value = "only editable")
  })
  @RequestMapping(path = "/listAllECMHealthyStatus", method = RequestMethod.GET)
  public Message listAllNodeHealthyStatus(
      HttpServletRequest req,
      @RequestParam(value = "onlyEditable", required = false) Boolean onlyEditable) {
    NodeHealthy[] nodeHealthy = NodeHealthy.values();
    if (onlyEditable) {
      nodeHealthy =
          new NodeHealthy[] {
            NodeHealthy.Healthy,
            NodeHealthy.UnHealthy,
            NodeHealthy.WARN,
            NodeHealthy.StockAvailable,
            NodeHealthy.StockUnavailable
          };
    }
    return Message.ok().data("nodeHealthy", nodeHealthy);
  }

  @ApiOperation(value = "modifyEMInfo", notes = "modify ECM info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconnmanager"),
    @ApiImplicitParam(
        name = "emStatus",
        dataType = "String",
        example = "Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable"),
    @ApiImplicitParam(name = "instance", dataType = "String", example = "bdpujes110:9102"),
    @ApiImplicitParam(name = "labels", dataType = "List", value = "Labels"),
    @ApiImplicitParam(name = "labelKey", dataType = "String", example = "emInstance"),
    @ApiImplicitParam(
        name = "stringValue",
        dataType = "String",
        example = "linkis-cg-engineconn-bdpujes110003:12295")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/modifyEMInfo", method = RequestMethod.PUT)
  @Transactional(rollbackFor = Exception.class)
  public Message modifyEMInfo(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException, LabelErrorException {
    String username = ModuleUserUtils.getOperationUser(req, "modifyEMInfo");
    checkAdmin(username);
    String applicationName = jsonNode.get("applicationName").asText();
    String instance = jsonNode.get("instance").asText();
    if (StringUtils.isEmpty(applicationName)) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getCode(),
          "applicationName cannot be null(请求参数applicationName不能为空)");
    }
    if (StringUtils.isEmpty(instance)) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getCode(), "instance cannot be null(请求参数instance不能为空)");
    }
    ServiceInstance serviceInstance = ServiceInstance.apply(applicationName, instance);
    if (serviceInstance == null) {
      throw new AMErrorException(
          AMErrorCode.QUERY_PARAM_NULL.getCode(),
          "serviceInstance:" + applicationName + " non-existent(" + applicationName + ")");
    }
    String healthyStatus = jsonNode.get("emStatus").asText();
    if (healthyStatus != null) {
      NodeHealthyInfo nodeHealthyInfo = new NodeHealthyInfo();
      nodeHealthyInfo.setNodeHealthy(NodeHealthy.valueOf(healthyStatus));
      emInfoService.updateEMInfo(serviceInstance, nodeHealthyInfo);
    }
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
            210003, "Failed to update label, include repeat labels(更新label失败，包含重复label)");
      }
      nodeLabelService.updateLabelsToNode(serviceInstance, newLabelList);
      logger.info("success to update label of instance: " + serviceInstance.getInstance());
    }
    return Message.ok("success");
  }

  @ApiOperation(
      value = "executeECMOperationByEC",
      notes = "EC execute ECM operation",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/executeECMOperationByEC", method = RequestMethod.POST)
  public Message executeECMOperationByEC(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {

    ServiceInstance serviceInstance = EngineRestfulApi.getServiceInstance(jsonNode);
    String userName = ModuleUserUtils.getOperationUser(req, "executeECMOperationByEC");
    logger.info(
        "User {} try to execute ECM Operation by EngineConn {}.", userName, serviceInstance);
    EngineNode engineNode = engineNodeManager.getEngineNode(serviceInstance);
    Map<String, Object> parameters;
    try {
      parameters =
          JsonUtils.jackson()
              .readValue(
                  jsonNode.get("parameters").toString(),
                  new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      logger.error(
          "Fail to process the operation parameters: [{}] in request",
          jsonNode.get("parameters").toString(),
          e);
      return Message.error(
          "Fail to process the operation parameters, cased by "
              + ExceptionUtils.getRootCauseMessage(e));
    }
    parameters.put(ECMOperateRequest.ENGINE_CONN_INSTANCE_KEY(), serviceInstance.getInstance());
    if (!userName.equals(engineNode.getOwner()) && AMConfiguration.isNotAdmin(userName)) {
      return Message.error(
          "You have no permission to execute ECM Operation by this EngineConn " + serviceInstance);
    }
    return executeECMOperation(engineNode.getEMNode(), new ECMOperateRequest(userName, parameters));
  }

  @ApiOperation(
      value = "executeECMOperation",
      notes = "execute ECM operation",
      response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/executeECMOperation", method = RequestMethod.POST)
  public Message executeECMOperation(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String userName = ModuleUserUtils.getOperationUser(req, "executeECMOperation");
    ServiceInstance serviceInstance = EngineRestfulApi.getServiceInstance(jsonNode);
    logger.info("User {} try to execute ECM Operation with {}.", userName, serviceInstance);
    EMNode ecmNode = this.emInfoService.getEM(serviceInstance);
    Map<String, Object> parameters;
    try {
      parameters =
          JsonUtils.jackson()
              .readValue(
                  jsonNode.get("parameters").toString(),
                  new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      logger.error(
          "Fail to process the operation parameters: [{}] in request",
          jsonNode.get("parameters").toString(),
          e);
      return Message.error(
          "Fail to process the operation parameters, cased by "
              + ExceptionUtils.getRootCauseMessage(e));
    }
    return executeECMOperation(ecmNode, new ECMOperateRequest(userName, parameters));
  }

  @ApiOperation(value = "openEngineLog", notes = "open Engine log", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "applicationName",
        dataType = "String",
        example = "linkis-cg-engineconn"),
    @ApiImplicitParam(name = "emInstance", dataType = "String", example = "bdpujes110003:910"),
    @ApiImplicitParam(name = "instance", dataType = "String", example = "bdpujes110003:21976"),
    @ApiImplicitParam(name = "parameters", dataType = "Map", value = "Parameters"),
    @ApiImplicitParam(name = "logType", dataType = "String", example = "stdout"),
    @ApiImplicitParam(name = "fromLine", dataType = "String", example = "0"),
    @ApiImplicitParam(name = "pageSize", dataType = "String", defaultValue = "1000"),
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @RequestMapping(path = "/openEngineLog", method = RequestMethod.POST)
  public Message openEngineLog(HttpServletRequest req, @RequestBody JsonNode jsonNode)
      throws AMErrorException {
    String userName = ModuleUserUtils.getOperationUser(req, "openEngineLog");
    EMNode ecmNode;
    Map<String, Object> parameters;
    try {
      String emInstance = jsonNode.get("emInstance").asText();
      String engineInstance = jsonNode.get("instance").asText();
      ServiceInstance serviceInstance = EngineRestfulApi.getServiceInstance(jsonNode);
      logger.info("User {} try to open engine: {} log.", userName, serviceInstance);
      ecmNode =
          this.emInfoService.getEM(
              ServiceInstance.apply("linkis-cg-engineconnmanager", emInstance));
      logger.info("ecm node info:{}", ecmNode);
      parameters =
          JsonUtils.jackson()
              .readValue(
                  jsonNode.get("parameters").toString(),
                  new TypeReference<Map<String, Object>>() {});
      String logType = (String) parameters.get("logType");
      if (!logType.equals("stdout") && !logType.equals("stderr")) {
        throw new AMErrorException(
            AMErrorCode.PARAM_ERROR.getCode(), AMErrorCode.PARAM_ERROR.getMessage());
      }
      parameters.put(OperateRequest$.MODULE$.OPERATOR_NAME_KEY(), "engineConnLog");
      parameters.put(ECMOperateRequest$.MODULE$.ENGINE_CONN_INSTANCE_KEY(), engineInstance);
      if (!parameters.containsKey("enableTail")) {
        parameters.put("enableTail", true);
      }
    } catch (JsonProcessingException e) {
      logger.error(
          "Fail to process the operation parameters: [{}] in request",
          jsonNode.get("parameters").toString(),
          e);
      return Message.error(
          "Fail to process the operation parameters, cased by "
              + ExceptionUtils.getRootCauseMessage(e));
    } catch (Exception e) {
      logger.error("Failed to open engine log, error:", e);
      return Message.error(e.getMessage());
    }
    return executeECMOperation(ecmNode, new ECMOperateRequest(userName, parameters));
  }

  private Message executeECMOperation(EMNode ecmNode, ECMOperateRequest ecmOperateRequest) {
    String operationName = OperateRequest$.MODULE$.getOperationName(ecmOperateRequest.parameters());
    if (ArrayUtils.contains(adminOperations, operationName)
        && AMConfiguration.isNotAdmin(ecmOperateRequest.user())) {
      logger.warn(
          "User {} has no permission to execute {} admin Operation in ECM {}.",
          ecmOperateRequest.user(),
          operationName,
          ecmNode.getServiceInstance());
      return Message.error(
          "You have no permission to execute "
              + operationName
              + " admin Operation in ECM "
              + ecmNode.getServiceInstance());
    }
    ECMOperateResponse engineOperateResponse =
        ecmOperateService.executeOperation(ecmNode, ecmOperateRequest);

    return Message.ok()
        .data("result", engineOperateResponse.getResult())
        .data("errorMsg", engineOperateResponse.errorMsg())
        .data("isError", engineOperateResponse.isError());
  }
}
