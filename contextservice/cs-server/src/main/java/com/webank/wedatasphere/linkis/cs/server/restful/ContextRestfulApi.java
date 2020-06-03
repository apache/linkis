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

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.protocol.ContextHTTPConstant;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.server.Message;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;


/**
 * Created by patinousward on 2020/2/18.
 */
@Component
@Path("contextservice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContextRestfulApi implements CsRestfulParent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextRestfulApi.class);

    @Autowired
    private CsScheduler csScheduler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @POST
    @Path("getContextValue")
    public Response getContextValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, contextKey);
        Message message = generateResponse(answerJob, "contextValue");
        return Message.messageToResponse(message);
    }


    @POST
    @Path("searchContextValue")
    public Response searchContextValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        JsonNode condition = jsonNode.get("condition");
        Map<Object, Object> conditionMap = objectMapper.convertValue(condition, new TypeReference<Map<Object, Object>>() {
        });
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, conditionMap);
        Message message = generateResponse(answerJob, "contextKeyValue");
        return Message.messageToResponse(message);
    }

/*    @GET
    @Path("searchContextValueByCondition")
    public Response searchContextValueByCondition(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException {
        Condition condition = null;
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, condition);
        return generateResponse(answerJob,"");
    }*/


    @POST
    @Path("setValueByKey")
    public Response setValueByKey(@Context HttpServletRequest req, JsonNode jsonNode) throws CSErrorException, IOException, ClassNotFoundException, InterruptedException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        ContextValue contextValue = getContextValueFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SET, contextID, contextKey, contextValue);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("setValue")
    public Response setValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKeyValue contextKeyValue = getContextKeyValueFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SET, contextID, contextKeyValue);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("resetValue")
    public Response resetValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.RESET, contextID, contextKey);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("removeValue")
    public Response removeValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, contextKey);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("removeAllValue")
    public Response removeAllValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVEALL, contextID);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("removeAllValueByKeyPrefixAndContextType")
    public Response removeAllValueByKeyPrefixAndContextType(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        String  contextType = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_TYPE_STR).getTextValue();
        String keyPrefix = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR).getTextValue();
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVEALL, contextID, ContextType.valueOf(contextType),keyPrefix);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @POST
    @Path("removeAllValueByKeyPrefix")
    public Response removeAllValueByKeyPrefix(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        String keyPrefix = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR).getTextValue();
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVEALL, contextID,keyPrefix);
        return Message.messageToResponse(generateResponse(answerJob, ""));
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CONTEXT;
    }

    @Override
    public CsScheduler getScheduler() {
        return this.csScheduler;
    }
}
