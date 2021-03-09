/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.cs.server.restful;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.protocol.ContextHTTPConstant;
import com.webank.wedatasphere.linkis.cs.common.utils.CSCommonUtils;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.server.Message;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by patinousward on 2020/2/18.
 */
@Component
@Path("/contextservice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContextIDRestfulApi implements CsRestfulParent {

    @Autowired
    private CsScheduler csScheduler;

    @POST
    @Path("createContextID")
    public Response createContextID(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, ClassNotFoundException, IOException, CSErrorException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID);
        return Message.messageToResponse(generateResponse(answerJob, "contextId"));
    }

    @GET
    @Path("getContextID")
    public Response getContextID(@Context HttpServletRequest req, @QueryParam("contextId") String id) throws InterruptedException, CSErrorException {
        if (StringUtils.isEmpty(id)) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, id);
        Message message = generateResponse(answerJob, "contextID");
        return Message.messageToResponse(message);
    }

    @POST
    @Path("updateContextID")
    public Response updateContextID(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.UPDATE, contextID);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("resetContextID")
    public Response resetContextID(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException {
        if (!jsonNode.has(ContextHTTPConstant.CONTEXT_ID_STR)) {
            throw new CSErrorException(97000, ContextHTTPConstant.CONTEXT_ID_STR + " cannot be empty");
        }
        String id = jsonNode.get(ContextHTTPConstant.CONTEXT_ID_STR).getTextValue();
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.RESET, id);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }


    @POST
    @Path("removeContextID")
    public Response removeContextID(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException {
        String id = jsonNode.get("contextId").getTextValue();
        if (StringUtils.isEmpty(id)) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, id);
        return Message.messageToResponse(generateResponse(answerJob, ""));
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
