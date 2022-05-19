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

package org.apache.linkis.cs.server.restful;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.protocol.ContextHTTPConstant;
import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.server.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/contextservice")
public class ContextRestfulApi implements CsRestfulParent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextRestfulApi.class);

    @Autowired private CsScheduler csScheduler;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(path = "getContextValue", method = RequestMethod.POST)
    public Message getContextValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, contextKey);
        Message message = generateResponse(answerJob, "contextValue");
        return message;
    }

    @RequestMapping(path = "searchContextValue", method = RequestMethod.POST)
    public Message searchContextValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        JsonNode condition = jsonNode.get("condition");
        Map<Object, Object> conditionMap =
                objectMapper.convertValue(condition, new TypeReference<Map<Object, Object>>() {});
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, conditionMap);
        Message message = generateResponse(answerJob, "contextKeyValue");
        return message;
    }

    /*
    @RequestMapping(path = "searchContextValueByCondition",method = RequestMethod.GET)
    public Message searchContextValueByCondition(HttpServletRequest req, JsonNode jsonNode) throws InterruptedException {
        Condition condition = null;
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, condition);
        return generateResponse(answerJob,"");
    }*/

    @RequestMapping(path = "setValueByKey", method = RequestMethod.POST)
    public Message setValueByKey(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws CSErrorException, IOException, ClassNotFoundException, InterruptedException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        ContextValue contextValue = getContextValueFromJsonNode(jsonNode);
        HttpAnswerJob answerJob =
                submitRestJob(req, ServiceMethod.SET, contextID, contextKey, contextValue);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "setValue", method = RequestMethod.POST)
    public Message setValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKeyValue contextKeyValue = getContextKeyValueFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SET, contextID, contextKeyValue);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "resetValue", method = RequestMethod.POST)
    public Message resetValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.RESET, contextID, contextKey);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "removeValue", method = RequestMethod.POST)
    public Message removeValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, contextKey);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "removeAllValue", method = RequestMethod.POST)
    public Message removeAllValue(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVEALL, contextID);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "removeAllValueByKeyPrefixAndContextType", method = RequestMethod.POST)
    public Message removeAllValueByKeyPrefixAndContextType(
            HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        String contextType = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_TYPE_STR).textValue();
        String keyPrefix = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR).textValue();
        HttpAnswerJob answerJob =
                submitRestJob(
                        req,
                        ServiceMethod.REMOVEALL,
                        contextID,
                        ContextType.valueOf(contextType),
                        keyPrefix);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "removeAllValueByKeyPrefix", method = RequestMethod.POST)
    public Message removeAllValueByKeyPrefix(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        String keyPrefix = jsonNode.get(ContextHTTPConstant.CONTEXT_KEY_PREFIX_STR).textValue();
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVEALL, contextID, keyPrefix);
        return generateResponse(answerJob, "");
    }

    @RequestMapping(path = "clearAllContextByID", method = RequestMethod.POST)
    public Message clearAllContextByID(HttpServletRequest req, @RequestBody JsonNode jsonNode)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        if (null == jsonNode || jsonNode.has("idList") ||
                !jsonNode.get("idList").isArray() ||
                (jsonNode.get("idList").isArray() && ((ArrayNode) jsonNode.get("idList")).size() == 0)) {
            throw new CSErrorException(97000, "idList cannot be empty.");
        }
        ArrayNode idArray = (ArrayNode) jsonNode.get("idList");
        List<String> idList = new ArrayList<>(idArray.size());
        for (int i = 0; i < idArray.size(); i++) {
            idList.add(idArray.get(i).asText());
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CLEAR, idList);
        return generateResponse(answerJob, "num");
    }

    @RequestMapping(path = "clearAllContextByTime", method = RequestMethod.POST)
    public Message clearAllContextByID(HttpServletRequest req, @RequestBody Map<String, Object> bodyMap)
            throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        if (null == bodyMap || bodyMap.isEmpty()) {
            throw new CSErrorException(97000, "idList cannot be empty.");
        }
        Date createTimeStart = null;
        Date createTimeEnd = null;
        Date updateTimeStart = null;
        Date updateTimeEnd = null;
        if (bodyMap.containsKey("createTimeStart")) createTimeStart = (Date) bodyMap.get("createTimeStart");
        if (bodyMap.containsKey("createTimeEnd")) createTimeEnd = (Date) bodyMap.get("createTimeEnd");
        if (bodyMap.containsKey("updateTimeStart")) updateTimeStart = (Date) bodyMap.get("updateTimeStart");
        if (bodyMap.containsKey("updateTimeEnd")) updateTimeEnd = (Date) bodyMap.get("updateTimeEnd");
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CLEAR, createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd);
        return generateResponse(answerJob, "num");
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
