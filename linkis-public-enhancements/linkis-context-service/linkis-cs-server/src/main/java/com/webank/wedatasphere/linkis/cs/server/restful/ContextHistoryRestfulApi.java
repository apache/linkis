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

package com.webank.wedatasphere.linkis.cs.server.restful;

import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.server.Message;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;


@Component
@Path("/contextservice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContextHistoryRestfulApi implements CsRestfulParent {

    @Autowired
    private CsScheduler csScheduler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Path("createHistory")
    public Response createHistory(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextHistory history = getContextHistoryFromJsonNode(jsonNode);
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        //source and contextid cannot be empty
        if (StringUtils.isEmpty(history.getSource())) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID, history);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("removeHistory")
    public Response removeHistory(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextHistory history = getContextHistoryFromJsonNode(jsonNode);
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        //source and contextid cannot be empty
        if (StringUtils.isEmpty(history.getSource())) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, history);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }


    @POST
    @Path("getHistories")
    public Response getHistories(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID);
        Message message = generateResponse(answerJob, "contextHistory");
        return Message.messageToResponse(message);
    }

    @POST
    @Path("getHistory")
    public Response getHistory(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        //ContextID contextID, String source
        String source = jsonNode.get("source").getTextValue();
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        //source and contextid cannot be empty
        if (StringUtils.isEmpty(source)) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, source);
        Message message = generateResponse(answerJob, "contextHistory");
        return Message.messageToResponse(message);
    }

    @POST
    @Path("searchHistory")
    public Response searchHistory(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        //ContextID contextID, String[] keywords
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        String[] keywords = objectMapper.readValue(jsonNode.get("keywords"), String[].class);
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, keywords);
        Message message = generateResponse(answerJob, "contextHistory");
        return Message.messageToResponse(message);
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
