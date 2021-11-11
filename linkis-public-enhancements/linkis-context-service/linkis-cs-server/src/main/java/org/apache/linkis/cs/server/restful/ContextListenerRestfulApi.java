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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.listener.CommonContextKeyListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.server.Message;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping(path = "/contextservice")
public class ContextListenerRestfulApi implements CsRestfulParent {

    @Autowired
    private CsScheduler csScheduler;

    private ObjectMapper objectMapper = new ObjectMapper();


    @RequestMapping(path = "onBindIDListener",method = RequestMethod.POST)
    public Message onBindIDListener(HttpServletRequest req,@RequestBody JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        String source = jsonNode.get("source").textValue();
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextIDListenerDomain listener = new CommonContextIDListenerDomain();
        listener.setSource(source);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.BIND, contextID, listener);
        return generateResponse(answerJob, "");
    }


    @RequestMapping(path = "onBindKeyListener",method = RequestMethod.POST)
    public Message onBindKeyListener(HttpServletRequest req,@RequestBody JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        String source = jsonNode.get("source").textValue();
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        CommonContextKeyListenerDomain listener = new CommonContextKeyListenerDomain();
        listener.setSource(source);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.BIND, contextID, contextKey, listener);
        return generateResponse(answerJob, "");
    }


    @RequestMapping(path = "heartbeat",method = RequestMethod.POST)
    public Message heartbeat(HttpServletRequest req, @RequestBody JsonNode jsonNode) throws InterruptedException, IOException, CSErrorException {
        String source = jsonNode.get("source").textValue();
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.HEARTBEAT, source);
        return generateResponse(answerJob, "ContextKeyValueBean");
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CONTEXT_LISTENER;
    }

    @Override
    public CsScheduler getScheduler() {
        return this.csScheduler;
    }
}
