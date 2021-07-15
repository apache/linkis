/*
 *
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.linkis.manager.am.restful;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.common.utils.ByteTimeUtils;
import com.webank.wedatasphere.linkis.manager.am.conf.AMConfiguration;
import com.webank.wedatasphere.linkis.manager.am.exception.AMErrorCode;
import com.webank.wedatasphere.linkis.manager.am.exception.AMErrorException;
import com.webank.wedatasphere.linkis.manager.am.service.engine.EngineCreateService;
import com.webank.wedatasphere.linkis.manager.am.service.engine.EngineInfoService;
import com.webank.wedatasphere.linkis.manager.am.utils.AMUtils;
import com.webank.wedatasphere.linkis.manager.am.vo.AMEngineNodeVo;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMEMNode;
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode;
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineCreateRequest;
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.EngineStopRequest;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.entity.UserModifiable;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelErrorException;
import com.webank.wedatasphere.linkis.manager.label.service.NodeLabelService;
import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.publisher.MessagePublisher;
import com.webank.wedatasphere.linkis.resourcemanager.utils.RMUtils;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component
@Path("/linkisManager")
public class EngineRestfulApi {

    @Autowired
    private EngineInfoService engineInfoService;

    @Autowired
    private EngineCreateService engineCreateService;

    @Autowired
    private NodeLabelService nodeLabelService;

    @Autowired
    private MessagePublisher messagePublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LabelBuilderFactory stdLabelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    private Logger logger = LoggerFactory.getLogger(EMRestfulApi.class);

    @POST
    @Path("createEngineConn")
    public Response createEngineConn(@Context HttpServletRequest req, JsonNode jsonNode)
            throws IOException, InterruptedException {
        String userName = SecurityFilter.getLoginUsername(req);
        EngineCreateRequest engineCreateRequest = objectMapper.readValue(jsonNode, EngineCreateRequest.class);
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
            return Message.messageToResponse(Message
                    .error("Create engineConn timeout, usually caused by the too long initialization of EngineConn(创建引擎超时，通常都是因为初始化引擎时间太长导致)."));
        } catch (ExecutionException e) {
            logger.error(String.format("User %s create engineConn failed.", userName), e);
            return Message.messageToResponse(Message
                    .error(String.format("Create engineConn failed, caused by %s.", ExceptionUtils.getRootCauseMessage(e))));
        }
        logger.info("Finished to create a engineConn for user {}. NodeInfo is {}.", userName, engineNode);
        //to transform to a map
        Map<String, Object> retEngineNode = new HashMap<>();
        retEngineNode.put("serviceInstance", engineNode.getServiceInstance());
        retEngineNode.put("nodeStatus", engineNode.getNodeStatus().toString());
        return Message.messageToResponse(Message.ok("create engineConn succeed.").data("engine", retEngineNode));
    }


    @POST
    @Path("/getEngineConn")
    public Response getEngineConn(@Context HttpServletRequest req, JsonNode jsonNode) throws AMErrorException {
        String userName = SecurityFilter.getLoginUsername(req);
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if(!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.messageToResponse(Message.error("You have no permission to access EngineConn " + serviceInstance));
        }
        return Message.messageToResponse(Message.ok().data("engine", engineNode));
    }

    @POST
    @Path("killEngineConn")
    public Response killEngineConn(@Context HttpServletRequest req, JsonNode jsonNode) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        ServiceInstance serviceInstance = getServiceInstance(jsonNode);
        logger.info("User {} try to kill engineConn {}.", userName, serviceInstance);
        EngineNode engineNode = engineCreateService.getEngineNode(serviceInstance);
        if(!userName.equals(engineNode.getOwner()) && !isAdmin(userName)) {
            return Message.messageToResponse(Message.error("You have no permission to kill EngineConn " + serviceInstance));
        }
        EngineStopRequest stopEngineRequest = new EngineStopRequest(serviceInstance, userName);
        MessageJob job = messagePublisher.publish(stopEngineRequest);
        job.get(RMUtils.MANAGER_KILL_ENGINE_EAIT().getValue().toLong(), TimeUnit.MILLISECONDS);
        logger.info("Finished to kill engineConn {}.", serviceInstance);
        return Message.messageToResponse(Message.ok("Kill engineConn succeed."));
    }

    @GET
    @Path("/listUserEngines")
    public Response listUserEngines(@Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<EngineNode> engineNodes = engineInfoService.listUserEngines(userName);
        return Message.messageToResponse(Message.ok().data("engines", engineNodes));
    }

    @POST
    @Path("/listEMEngines")
    public Response listEMEngines(@Context HttpServletRequest req, JsonNode jsonNode) throws IOException, AMErrorException {
        String username = SecurityFilter.getLoginUsername(req);
        if(!isAdmin(username)){
            throw new AMErrorException(210003,"Only admin can search engine information(只有管理员才能查询所有引擎信息).");
        }
        AMEMNode amemNode = objectMapper.readValue(jsonNode.get("em"), AMEMNode.class);
        JsonNode emInstace = jsonNode.get("emInstance");
        JsonNode nodeStatus = jsonNode.get("nodeStatus");
        JsonNode engineType = jsonNode.get("engineType");
        JsonNode owner = jsonNode.get("owner");
        List<EngineNode> engineNodes = engineInfoService.listEMEngines(amemNode);
        ArrayList<AMEngineNodeVo> allengineNodes = AMUtils.copyToAMEngineNodeVo(engineNodes);
        ArrayList<AMEngineNodeVo> allEMVoFilter1 = allengineNodes;
        if(CollectionUtils.isNotEmpty(allEMVoFilter1) && emInstace != null){
            allEMVoFilter1 = (ArrayList<AMEngineNodeVo>) allEMVoFilter1.stream().filter(em -> {return em.getInstance().contains(emInstace.asText());}).collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter2 = allEMVoFilter1;
        if(CollectionUtils.isNotEmpty(allEMVoFilter2) && nodeStatus != null && !StringUtils.isEmpty(nodeStatus.asText())){
            allEMVoFilter2 = (ArrayList<AMEngineNodeVo>) allEMVoFilter2.stream().filter(em -> {return em.getNodeStatus() != null ? em.getNodeStatus().equals(NodeStatus.valueOf(nodeStatus.asText())) : false;}).collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter3 = allEMVoFilter2;
        if(CollectionUtils.isNotEmpty(allEMVoFilter3) && owner != null && !StringUtils.isEmpty(owner.asText())){
            allEMVoFilter3 = (ArrayList<AMEngineNodeVo>) allEMVoFilter3.stream().filter(em ->{return em.getOwner().equalsIgnoreCase(owner.asText());}).collect(Collectors.toList());
        }
        ArrayList<AMEngineNodeVo> allEMVoFilter4 = allEMVoFilter3;
        if(CollectionUtils.isNotEmpty(allEMVoFilter4) && engineType != null && !StringUtils.isEmpty(engineType.asText())){
            allEMVoFilter4 = (ArrayList<AMEngineNodeVo>) allEMVoFilter4.stream().filter(em ->{return em.getEngineType().equalsIgnoreCase(engineType.asText());}).collect(Collectors.toList());
        }
        return Message.messageToResponse(Message.ok().data("engines", allEMVoFilter4));
    }

    @PUT
    @Path("/modifyEngineInfo")
    public Response modifyEngineInfo(@Context HttpServletRequest req, JsonNode jsonNode) throws AMErrorException, LabelErrorException {
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
        return Message.messageToResponse(Message.ok("success to update engine information(更新引擎信息成功)"));
    }

    @GET
    @Path("/listAllNodeHealthyStatus")
    public Response listAllNodeHealthyStatus(@Context HttpServletRequest req, @QueryParam("onlyEditable") Boolean onlyEditable){
        NodeStatus[] nodeStatus = NodeStatus.values();
        return Message.messageToResponse(Message.ok().data("nodeStatus", nodeStatus));
    }

    private boolean isAdmin(String user) {
        String[] adminArray = AMConfiguration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        return ArrayUtils.contains(adminArray, user);
    }

    private ServiceInstance getServiceInstance(JsonNode jsonNode) throws AMErrorException {
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
