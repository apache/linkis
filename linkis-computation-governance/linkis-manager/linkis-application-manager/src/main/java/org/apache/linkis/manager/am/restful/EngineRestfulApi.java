/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.am.restful;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.exception.AMErrorCode;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.service.engine.EngineCreateService;
import org.apache.linkis.manager.am.service.engine.EngineInfoService;
import org.apache.linkis.manager.am.service.engine.EngineOperateService;
import org.apache.linkis.manager.am.service.engine.EngineStopService;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.am.vo.AMEngineNodeVo;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.protocol.engine.EngineCreateRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateResponse;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.UserModifiable;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "EC(engineconn) operation")
@RequestMapping(
        path = "/linkisManager",
        produces = {"application/json"})
@RestController
public class EngineRestfulApi {

    @Autowired private EngineInfoService engineInfoService;

    @Autowired private EngineCreateService engineCreateService;

    @Autowired private EngineOperateService engineOperateService;

    @Autowired private NodeLabelService nodeLabelService;

    @Autowired private EngineStopService engineStopService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LabelBuilderFactory stdLabelBuilderFactory =
            LabelBuilderFactoryContext.getLabelBuilderFactory();

    private static final Logger logger = LoggerFactory.getLogger(EngineRestfulApi.class);

    @ApiOperation(value = "createEngineConn", notes = "create an engineconn", response = Message.class)
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/createEngineConn", method = RequestMethod.POST)
    public Message createEngineConn(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws IOException, InterruptedException {
        String userName = ModuleUserUtils.getOperationUser(req, "createEngineConn");
        EngineCreateRequest engineCreateRequest =
                objectMapper.treeToValue(jsonNode, EngineCreateRequest.class);
        engineCreateRequest.setUser(userName);
        long timeout = engineCreateRequest.getTimeOut();
        if (timeout <= 0) {
            timeout = AMConfiguration.ENGINE_CONN_START_REST_MAX_WAIT_TIME().getValue().toLong();
            engineCreateRequest.setTimeOut(timeout);
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
        } catch (Exception e) {
            logger.error(String.format("User %s create engineConn failed.", userName), e);
            return Message.error(
                    String.format(
                            "Create engineConn failed, caused by %s.",
                            ExceptionUtils.getRootCauseMessage(e)));
        }
        logger.info(
                "Finished to create a engineConn for user {}. NodeInfo is {}.",
                userName,
                engineNode);
        // to transform to a map
        Map<String, Object> retEngineNode = new HashMap<>();
        retEngineNode.put("serviceInstance", engineNode.getServiceInstance());
        if (null == engineNode.getNodeStatus()) {
            engineNode.setNodeStatus(NodeStatus.Starting);
        }
        retEngineNode.put("nodeStatus", engineNode.getNodeStatus().toString());
        retEngineNode.put("ticketId", engineNode.getTicketId());
        retEngineNode.put("ecmServiceInstance", engineNode.getEMNode().getServiceInstance());
        return Message.ok("create engineConn succeed.").data("engine", retEngineNode);
    }

    @ApiOperation(value = "getEngineConn", notes = "get engineconn", response = Message.class)
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/getEngineConn", method = RequestMethod.POST)
    public Message getEngineConn(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws AMErrorException {
        String userName = ModuleUserUtils.getOperationUser(req, "getEngineConn");
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if (!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.error("You have no permission to access EngineConn " + serviceInstance);
        }
        return Message.ok().data("engine", engineNode);
    }

    @ApiOperation(value = "kill egineconn", notes = "kill engineconn", response = Message.class)
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/killEngineConn", method = RequestMethod.POST)
    public Message killEngineConn(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws Exception {

        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        String userName =
                ModuleUserUtils.getOperationUser(req, "killEngineConn：" + serviceInstance);
        logger.info("User {} try to kill engineConn {}.", userName, serviceInstance);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if (!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.error("You have no permission to kill EngineConn " + serviceInstance);
        }
        EngineStopRequest stopEngineRequest = new EngineStopRequest(serviceInstance, userName);
        Sender sender = Sender.getSender(Sender.getThisServiceInstance());
        engineStopService.stopEngine(stopEngineRequest, sender);
        logger.info("Finished to kill engineConn {}.", serviceInstance);
        return Message.ok("Kill engineConn succeed.");
    }

    @ApiOperation(value = "kill eginecon", notes = "kill one engineconn or more ", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "engineInstance", required = false, dataType = "String", value = "engine instance", example = "bdpujes110003:12295"),
        @ApiImplicitParam(name = "applicationName", required = false, dataType = "String", value = "application name",example = "linkis-cg-engineconn")
    })
    @ApiOperationSupport(ignoreParameters = {"param"})
    @RequestMapping(path = "/rm/enginekill", method = RequestMethod.POST)
    public Message killEngine(HttpServletRequest req, @RequestBody Map<String, String>[] param)
            throws Exception {
        String userName = ModuleUserUtils.getOperationUser(req, "enginekill");
        Sender sender = Sender.getSender(Sender.getThisServiceInstance());
        for (Map<String, String> engineParam : param) {
            String moduleName = engineParam.get("applicationName");
            String engineInstance = engineParam.get("engineInstance");
            EngineStopRequest stopEngineRequest =
                    new EngineStopRequest(
                            ServiceInstance.apply(moduleName, engineInstance), userName);
            engineStopService.stopEngine(stopEngineRequest, sender);
            logger.info("Finished to kill engines");
        }
        return Message.ok("Kill engineConn succeed.");
    }

    @ApiOperation(value = "listUserEngines", notes = "get user's engineconn list", response = Message.class)
    @RequestMapping(path = "/listUserEngines", method = RequestMethod.GET)
    public Message listUserEngines(HttpServletRequest req) {
        String userName = ModuleUserUtils.getOperationUser(req, "listUserEngines");
        List<EngineNode> engineNodes = engineInfoService.listUserEngines(userName);
        return Message.ok().data("engines", engineNodes);
    }

    @ApiOperation(value = "listEMEngines", notes = "get the list of engineconn under an ECM", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "em", required = false, dataType = "Map", value = "ecm object"),
        @ApiImplicitParam(name = "serviceInstance", required = false, dataType = "Map", value = "service instance"),
        @ApiImplicitParam(name = "applicationName", required = false, dataType = "String", value = "application name", example = "linkis-cg-engineconnmanager"),
        @ApiImplicitParam(name = "instance", required = false, dataType = "String", value = "instance"),
        @ApiImplicitParam(name = "emInstance", required = false, dataType = "String", value = "ecm instance", example = "bdpujes110003:9102"),
        @ApiImplicitParam(name = "engineType", required = false, dataType = "String", value = "engine type"),
        @ApiImplicitParam(name = "nodeStatus", required = false, dataType = "String", value = "node status"),
        @ApiImplicitParam(name = "owner", required = false, dataType = "String", value = "owner"),
    })
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/listEMEngines", method = RequestMethod.POST)
    public Message listEMEngines(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws IOException, AMErrorException {
        String username = ModuleUserUtils.getOperationUser(req, "listEMEngines");
        if (!isAdmin(username)) {
            throw new AMErrorException(
                    210003, "Only admin can search engine information(只有管理员才能查询所有引擎信息).");
        }
        AMEMNode amemNode = objectMapper.treeToValue(jsonNode.get("em"), AMEMNode.class);
        JsonNode emInstace = jsonNode.get("emInstance");
        JsonNode nodeStatus = jsonNode.get("nodeStatus");
        JsonNode engineType = jsonNode.get("engineType");
        JsonNode owner = jsonNode.get("owner");
        List<EngineNode> engineNodes = engineInfoService.listEMEngines(amemNode);
        ArrayList<AMEngineNodeVo> allengineNodes = AMUtils.copyToAMEngineNodeVo(engineNodes);
        ArrayList<AMEngineNodeVo> allEMVoFilter1 = allengineNodes;
        if (CollectionUtils.isNotEmpty(allEMVoFilter1) && emInstace != null) {
            allEMVoFilter1 =
                    (ArrayList<AMEngineNodeVo>)
                            allEMVoFilter1.stream()
                                    .filter(
                                            em ->
                                                    em.getInstance() != null
                                                            && em.getInstance()
                                                                    .contains(emInstace.asText()))
                                    .collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter2 = allEMVoFilter1;
        if (CollectionUtils.isNotEmpty(allEMVoFilter2)
                && nodeStatus != null
                && !StringUtils.isEmpty(nodeStatus.asText())) {
            allEMVoFilter2 =
                    (ArrayList<AMEngineNodeVo>)
                            allEMVoFilter2.stream()
                                    .filter(
                                            em ->
                                                    em.getNodeStatus() != null
                                                            && em.getNodeStatus()
                                                                    .equals(
                                                                            NodeStatus.valueOf(
                                                                                    nodeStatus
                                                                                            .asText())))
                                    .collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter3 = allEMVoFilter2;
        if (CollectionUtils.isNotEmpty(allEMVoFilter3)
                && owner != null
                && !StringUtils.isEmpty(owner.asText())) {
            allEMVoFilter3 =
                    (ArrayList<AMEngineNodeVo>)
                            allEMVoFilter3.stream()
                                    .filter(
                                            em ->
                                                    em.getOwner() != null
                                                            && em.getOwner()
                                                                    .equalsIgnoreCase(
                                                                            owner.asText()))
                                    .collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter4 = allEMVoFilter3;
        if (CollectionUtils.isNotEmpty(allEMVoFilter4)
                && engineType != null
                && !StringUtils.isEmpty(engineType.asText())) {
            allEMVoFilter4 =
                    (ArrayList<AMEngineNodeVo>)
                            allEMVoFilter4.stream()
                                    .filter(
                                            em ->
                                                    em.getEngineType() != null
                                                            && em.getEngineType()
                                                                    .equalsIgnoreCase(
                                                                            engineType.asText()))
                                    .collect(Collectors.toList());
        }
        return Message.ok().data("engines", allEMVoFilter4);
    }

    @ApiOperation(value = "modifyEngineInfo", notes = "modify engineconn info", response = Message.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "applicationName", required = false, dataType = "String", value = "application name", example = "linkis-cg-engineconn"),
        @ApiImplicitParam(name = "emStatus",required = false, dataType = "String",  value = "ecm status", example = "Starting,Unlock,Locked,Idle,Busy,Running,ShuttingDown,Failed,Success"),
        @ApiImplicitParam(name = "instance", dataType = "String", required = false, value = "instance", example = "bdpujes110003:12295"),
        @ApiImplicitParam(name = "labels", dataType = "List", required = false, value = "labels"),
        @ApiImplicitParam(name = "labelKey", dataType = "String", required = false, value = "label key", example = "engineInstance"),
        @ApiImplicitParam(name = "stringValue", dataType = "String", required = false, value = "stringValue", example = "linkis-cg-engineconn-bdpujes110003:12295")
    })
    @ApiOperationSupport(ignoreParameters = {"jsonNode"})
    @RequestMapping(path = "/modifyEngineInfo", method = RequestMethod.PUT)
    public Message modifyEngineInfo(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws AMErrorException, LabelErrorException {
        String username = ModuleUserUtils.getOperationUser(req, "modifyEngineInfo");
        if (!isAdmin(username)) {
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
                        210003,
                        "Failed to update label, include repeat label(更新label失败，包含重复label)");
            }
            nodeLabelService.updateLabelsToNode(serviceInstance, newLabelList);
            logger.info("success to update label of instance: " + serviceInstance.getInstance());
        }
        return Message.ok("success to update engine information(更新引擎信息成功)");
    }

    @ApiOperation(value = "listAllNodeHealthyStatus", notes = "get all node healthy staus list", response = Message.class)
    @RequestMapping(path = "/listAllNodeHealthyStatus", method = RequestMethod.GET)
    public Message listAllNodeHealthyStatus(
            HttpServletRequest req,
            @RequestParam(value = "onlyEditable", required = false) Boolean onlyEditable) {
        NodeStatus[] nodeStatus = NodeStatus.values();
        return Message.ok().data("nodeStatus", nodeStatus);
    }

    @ApiOperation(value = "executeEngineConnOperation", notes = "execute engine conn operation", response = Message.class)
    @RequestMapping(path = "/executeEngineConnOperation", method = RequestMethod.POST)
    public Message executeEngineConnOperation(
            HttpServletRequest req, @RequestBody JsonNode jsonNode) throws Exception {
        String userName = ModuleUserUtils.getOperationUser(req, "executeEngineConnOperation");
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        logger.info("User {} try to execute Engine Operation {}.", userName, serviceInstance);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if (!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.error(
                    "You have no permission to execute Engine Operation " + serviceInstance);
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

    private boolean isAdmin(String user) {
        return AMConfiguration.isAdmin(user);
    }

    static ServiceInstance getServiceInstance(JsonNode jsonNode) throws AMErrorException {
        String applicationName = jsonNode.get("applicationName").asText();
        String instance = jsonNode.get("instance").asText();
        if (StringUtils.isEmpty(applicationName)) {
            throw new AMErrorException(
                    AMErrorCode.QUERY_PARAM_NULL.getCode(),
                    "applicationName cannot be null(请求参数applicationName不能为空)");
        }
        if (StringUtils.isEmpty(instance)) {
            throw new AMErrorException(
                    AMErrorCode.QUERY_PARAM_NULL.getCode(),
                    "instance cannot be null(请求参数instance不能为空)");
        }
        return ServiceInstance.apply(applicationName, instance);
    }
}
