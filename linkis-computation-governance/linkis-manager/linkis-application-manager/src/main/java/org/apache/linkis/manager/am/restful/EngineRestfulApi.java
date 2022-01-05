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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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
import org.apache.linkis.message.builder.MessageJob;
import org.apache.linkis.message.publisher.MessagePublisher;
import org.apache.linkis.resourcemanager.utils.RMUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RequestMapping(path = "/linkisManager", produces = {"application/json"})
@RestController
public class EngineRestfulApi {

    @Autowired
    private EngineInfoService engineInfoService;

    @Autowired
    private EngineCreateService engineCreateService;

    @Autowired
    private EngineOperateService engineOperateService;

    @Autowired
    private NodeLabelService nodeLabelService;

    @Autowired
    private EngineStopService engineStopService;

    @Autowired
    private MessagePublisher messagePublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LabelBuilderFactory stdLabelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    private Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);

    @RequestMapping(path = "/createEngineConn", method = RequestMethod.POST)
    public Message createEngineConn( HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws IOException, InterruptedException {
        String userName = SecurityFilter.getLoginUsername(req);
        EngineCreateRequest engineCreateRequest = objectMapper.treeToValue(jsonNode, EngineCreateRequest.class);
        engineCreateRequest.setUser(userName);
        long timeout = engineCreateRequest.getTimeOut();
        if(timeout <= 0) {
            timeout = AMConfiguration.ENGINE_CONN_START_REST_MAX_WAIT_TIME().getValue().toLong();
            engineCreateRequest.setTimeOut(timeout);
        }
        logger.info("User {} try to create a engineConn with maxStartTime {}. EngineCreateRequest is {}.", userName,
                ByteTimeUtils.msDurationToString(timeout), engineCreateRequest);
        MessageJob job = messagePublisher.publish(engineCreateRequest);
        EngineNode engineNode;
        try {
            engineNode = (EngineNode) job.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.error(String.format("User %s create engineConn timeout.", userName), e);
            job.cancel(true);
            return Message
                    .error("Create engineConn timeout, usually caused by the too long initialization of EngineConn(创建引擎超时，通常都是因为初始化引擎时间太长导致).");
        } catch (ExecutionException e) {
            logger.error(String.format("User %s create engineConn failed.", userName), e);
            return Message
                    .error(String.format("Create engineConn failed, caused by %s.", ExceptionUtils.getRootCauseMessage(e)));
        }
        logger.info("Finished to create a engineConn for user {}. NodeInfo is {}.", userName, engineNode);
        //to transform to a map
        Map<String, Object> retEngineNode = new HashMap<>();
        retEngineNode.put("serviceInstance", engineNode.getServiceInstance());
        if (null == engineNode.getNodeStatus()) {
            engineNode.setNodeStatus(NodeStatus.Starting);
        } else {
            retEngineNode.put("nodeStatus", engineNode.getNodeStatus().toString());
        }
        retEngineNode.put("ticketId", engineNode.getTicketId());
        retEngineNode.put("ecmServiceInstance", engineNode.getEMNode().getServiceInstance());
        return Message.ok("create engineConn succeed.").data("engine", retEngineNode);
    }


    @RequestMapping(path = "/getEngineConn", method = RequestMethod.POST)
    public Message getEngineConn( HttpServletRequest req, @RequestBody JsonNode jsonNode) throws AMErrorException {
        String userName = SecurityFilter.getLoginUsername(req);
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if(!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.error("You have no permission to access EngineConn " + serviceInstance);
        }
        return Message.ok().data("engine", engineNode);
    }

    @RequestMapping(path = "/killEngineConn", method = RequestMethod.POST)
    public Message killEngineConn( HttpServletRequest req, @RequestBody JsonNode jsonNode) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        logger.info("User {} try to kill engineConn {}.", userName, serviceInstance);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if(!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.error("You have no permission to kill EngineConn " + serviceInstance);
        }
        EngineStopRequest stopEngineRequest = new EngineStopRequest(serviceInstance, userName);
        engineStopService.stopEngine(stopEngineRequest);
        logger.info("Finished to kill engineConn {}.", serviceInstance);
        return Message.ok("Kill engineConn succeed.");
    }

    @RequestMapping(path = "/rm/enginekill", method = RequestMethod.POST)
    public Message killEngine( HttpServletRequest req, @RequestBody Map<String, String>[] param) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        for (Map<String, String>engineParam : param) {
            String moduleName = engineParam.get("applicationName");
            String engineInstance = engineParam.get("engineInstance");
            EngineStopRequest stopEngineRequest = new EngineStopRequest(ServiceInstance.apply(moduleName, engineInstance), userName);
            engineStopService.stopEngine(stopEngineRequest);
            logger.info("Finished to kill engines");
        }
        return Message.ok("Kill engineConn succeed.");
    }

    @RequestMapping(path = "/listUserEngines", method = RequestMethod.GET)
    public Message listUserEngines( HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<EngineNode> engineNodes = engineInfoService.listUserEngines(userName);
        return Message.ok().data("engines", engineNodes);
    }

    @RequestMapping(path = "/listEMEngines", method = RequestMethod.POST)
    public Message listEMEngines( HttpServletRequest req, @RequestBody JsonNode jsonNode) throws IOException, AMErrorException {
        String username = SecurityFilter.getLoginUsername(req);
        if(!isAdmin(username)){
            throw new AMErrorException(210003,"Only admin can search engine information(只有管理员才能查询所有引擎信息).");
        }
        AMEMNode amemNode = objectMapper.treeToValue(jsonNode.get("em"), AMEMNode.class);
        JsonNode emInstace = jsonNode.get("emInstance");
        JsonNode nodeStatus = jsonNode.get("nodeStatus");
        JsonNode engineType = jsonNode.get("engineType");
        JsonNode owner = jsonNode.get("owner");
        List<EngineNode> engineNodes = engineInfoService.listEMEngines(amemNode);
        ArrayList<AMEngineNodeVo> allengineNodes = AMUtils.copyToAMEngineNodeVo(engineNodes);
        ArrayList<AMEngineNodeVo> allEMVoFilter1 = allengineNodes;
        if(CollectionUtils.isNotEmpty(allEMVoFilter1) && emInstace != null){
            allEMVoFilter1 = (ArrayList<AMEngineNodeVo>) allEMVoFilter1.stream().filter(em -> em.getInstance() != null && em.getInstance().contains(emInstace.asText())).collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter2 = allEMVoFilter1;
        if(CollectionUtils.isNotEmpty(allEMVoFilter2) && nodeStatus != null && !StringUtils.isEmpty(nodeStatus.asText())){
            allEMVoFilter2 = (ArrayList<AMEngineNodeVo>) allEMVoFilter2.stream().filter(em -> em.getNodeStatus() != null && em.getNodeStatus().equals(NodeStatus.valueOf(nodeStatus.asText()))).collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter3 = allEMVoFilter2;
        if(CollectionUtils.isNotEmpty(allEMVoFilter3) && owner != null && !StringUtils.isEmpty(owner.asText())){
            allEMVoFilter3 = (ArrayList<AMEngineNodeVo>) allEMVoFilter3.stream().filter(em -> em.getOwner() != null && em.getOwner().equalsIgnoreCase(owner.asText())).collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter4 = allEMVoFilter3;
        if(CollectionUtils.isNotEmpty(allEMVoFilter4) && engineType != null && !StringUtils.isEmpty(engineType.asText())){
            allEMVoFilter4 = (ArrayList<AMEngineNodeVo>) allEMVoFilter4.stream().filter(em -> em.getEngineType() != null && em.getEngineType().equalsIgnoreCase(engineType.asText())).collect(Collectors.toList());
        }
        return Message.ok().data("engines", allEMVoFilter4);
    }

    @RequestMapping(path = "/modifyEngineInfo", method = RequestMethod.PUT)
    public Message modifyEngineInfo( HttpServletRequest req, @RequestBody JsonNode jsonNode) throws AMErrorException, LabelErrorException {
        String username = SecurityFilter.getLoginUsername(req);
        if(!isAdmin(username)){
            throw new AMErrorException(210003,"Only admin can modify engineConn information(只有管理员才能修改引擎信息).");
        }
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        JsonNode labels = jsonNode.get("labels");
        Set<String> labelKeySet = new HashSet<>();
        if(labels != null){
            ArrayList<Label<?>> newLabelList = new ArrayList<>();
            Iterator<JsonNode> iterator = labels.iterator();
            while(iterator.hasNext()){
                JsonNode label = iterator.next();
                String labelKey = label.get("labelKey").asText();
                String stringValue = label.get("stringValue").asText();
                Label newLabel = stdLabelBuilderFactory.createLabel(labelKey, stringValue);
                if(newLabel instanceof UserModifiable) {
                    ((UserModifiable) newLabel).valueCheck(stringValue);
                }
                labelKeySet.add(labelKey);
                newLabelList.add(newLabel);
            }
            if(labelKeySet.size() != newLabelList.size()){
                throw new AMErrorException(210003, "Failed to update label, include repeat label(更新label失败，包含重复label)");
            }
            nodeLabelService.updateLabelsToNode(serviceInstance, newLabelList);
            logger.info("success to update label of instance: " + serviceInstance.getInstance());
        }
        return Message.ok("success to update engine information(更新引擎信息成功)");
    }

    @RequestMapping(path = "/listAllNodeHealthyStatus", method = RequestMethod.GET)
    public Message listAllNodeHealthyStatus( HttpServletRequest req,
                                             @RequestParam(value = "onlyEditable",required = false) Boolean onlyEditable){
        NodeStatus[] nodeStatus = NodeStatus.values();
        return Message.ok().data("nodeStatus", nodeStatus);
    }

    @RequestMapping(path = "/executeEngineConnOperation", method = RequestMethod.POST)
    public Message executeEngineConnOperation(HttpServletRequest req, @RequestBody JsonNode jsonNode) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        logger.info("User {} try to execute Engine Operation {}.", userName, serviceInstance);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if(!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.error("You have no permission to execute Engine Operation " + serviceInstance);
        }
        Map<String, Object> parameters = objectMapper.convertValue(jsonNode.get("parameters")
                , new TypeReference<Map<String, Object>>(){});

        EngineOperateRequest engineOperateRequest = new EngineOperateRequest(userName, parameters);
        EngineOperateResponse engineOperateResponse = engineOperateService.executeOperation(engineNode, engineOperateRequest);
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
        if(StringUtils.isEmpty(applicationName)){
            throw new AMErrorException(AMErrorCode.QUERY_PARAM_NULL.getCode(), "applicationName cannot be null(请求参数applicationName不能为空)");
        }
        if(StringUtils.isEmpty(instance)){
            throw new AMErrorException(AMErrorCode.QUERY_PARAM_NULL.getCode(), "instance cannot be null(请求参数instance不能为空)");
        }
        return ServiceInstance.apply(applicationName,instance);
    }
}
