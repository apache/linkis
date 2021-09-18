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

import com.webank.wedatasphere.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.CommonContextKeyListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.server.Message;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Message onBindIDListener(HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        String source = jsonNode.get("source").getTextValue();
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextIDListenerDomain listener = new CommonContextIDListenerDomain();
        listener.setSource(source);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.BIND, contextID, listener);
        return generateResponse(answerJob, "");
    }


    @RequestMapping(path = "onBindKeyListener",method = RequestMethod.POST)
    public Message onBindKeyListener(HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException, IOException, ClassNotFoundException {
        String source = jsonNode.get("source").getTextValue();
        ContextID contextID = getContextIDFromJsonNode(jsonNode);
        ContextKey contextKey = getContextKeyFromJsonNode(jsonNode);
        CommonContextKeyListenerDomain listener = new CommonContextKeyListenerDomain();
        listener.setSource(source);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.BIND, contextID, contextKey, listener);
        return generateResponse(answerJob, "");
    }


    @RequestMapping(path = "heartbeat",method = RequestMethod.POST)
    public Message heartbeat(HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, IOException, CSErrorException {
        String source = jsonNode.get("source").getTextValue();
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
