/*
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
 */

package com.webank.wedatasphere.linkis.manager.am.restful;

import com.webank.wedatasphere.linkis.common.exception.DWCRetryException;
import com.webank.wedatasphere.linkis.manager.am.service.engine.*;
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMEMNode;
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode;
import com.webank.wedatasphere.linkis.manager.common.protocol.engine.*;
import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.context.MessageSchedulerContext;
import com.webank.wedatasphere.linkis.message.publisher.MessagePublisher;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component
@Path("linkisManager")
public class EngineRestfulApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineRestfulApi.class);
    @Autowired
    private EngineAskEngineService engineAskEngineService;
    @Autowired
    private EngineInfoService engineInfoService;
    @Autowired
    private EngineCreateService engineCreateService;
    @Autowired
    private EngineReuseService engineReuseService;
    @Autowired
    private EngineStopService engineStopService;
    @Autowired
    private MessagePublisher messagePublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/listUserEngines")
    public Response listUserEngines(@Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<EngineNode> engineNodes = engineInfoService.listUserEngines(userName);
        return Message.messageToResponse(Message.ok().data("engines", engineNodes));
    }

    @POST
    @Path("/listEMEngines")
    public Response listEMEngines(@Context HttpServletRequest req, JsonNode jsonNode) throws IOException {
        AMEMNode amemNode = objectMapper.readValue(jsonNode.get("em"), AMEMNode.class);
        List<EngineNode> engineNodes = engineInfoService.listEMEngines(amemNode);
        return Message.messageToResponse(Message.ok().data("engines", engineNodes));
    }

    @POST
    @Path("/askEngine")
    public Response askEngine(@Context HttpServletRequest req, Map<String,Object> json) throws IOException {
        EngineAskRequest engineAskRequest = objectMapper.convertValue(json,EngineAskRequest.class);
        MessageJob job = messagePublisher.publish(engineAskRequest);
        Object engins = engineAskEngineService.askEngine(engineAskRequest, job.getMethodContext());
        if(engins instanceof EngineNode){
            return Message.messageToResponse(Message.ok().data("engine", engins));
        }else if(engins instanceof EngineAskAsyncResponse){
            return Message.messageToResponse(Message.ok().data("engineResponse", engins));
        }
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/createEngine")
    public Response createEngine(@Context HttpServletRequest req, Map<String,Object> json) throws IOException {
        EngineCreateRequest engineCreateRequest = objectMapper.convertValue(json,EngineCreateRequest.class);
        MessageJob job = messagePublisher.publish(engineCreateRequest);
        try {
            EngineNode engine = engineCreateService.createEngine(engineCreateRequest, job.getMethodContext());
            return Message.messageToResponse(Message.ok().data("engine", engine));
        } catch (DWCRetryException e) {
            Message message = Message.error(e.getMessage());
            message.setMethod("/api/linkisManager/createEngines");
            return  Message.messageToResponse(message);
        }
    }

    @POST
    @Path("/stopEngine")
    public Response stopEngine(@Context HttpServletRequest req, Map<String,Object> json) throws IOException {
        EngineStopRequest engineStopRequest = objectMapper.convertValue(json,EngineStopRequest.class);
        MessageJob job = messagePublisher.publish(engineStopRequest);
        engineStopService.stopEngine(engineStopRequest,job.getMethodContext());
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/reuseEngine")
    public Response reuseEngine(@Context HttpServletRequest req, Map<String,Object> json) throws IOException {
        EngineReuseRequest engineReuseRequest = objectMapper.convertValue(json,EngineReuseRequest.class);
        try {
            EngineNode engine = engineReuseService.reuseEngine(engineReuseRequest);
            return Message.messageToResponse(Message.ok().data("engine", engine));
        } catch (DWCRetryException e) {
            Message message = Message.error(e.getMessage());
            message.setMethod("/api/linkisManager/reuseEngine");
            return  Message.messageToResponse(message);
        }
    }




}
