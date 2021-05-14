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

package com.webank.wedatasphere.linkis.cs.server;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextID;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKey;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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
import java.util.Map;


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
    @Path("createContext")
    public Response createContext(@Context HttpServletRequest request) throws Exception{

        //request.getAuthType()
//        if (null == jsonNode.get(ContextHTTPConstant.PROJECT_NAME_STR) || null == jsonNode.get(ContextHTTPConstant.FLOW_NAME_STR)){
//            LOGGER.error("projectName or flowName is null");
//            throw new ErrorException(80035, "projectName or flowName is null");
//        }
//        String projectName = jsonNode.get(ContextHTTPConstant.PROJECT_NAME_STR).getTextValue();
//        String flowName = jsonNode.get(ContextHTTPConstant.FLOW_NAME_STR).getTextValue();
        String projectName = "";
        String flowName = "";
        HttpAnswerJob answerJob = submitRestJob(request, ServiceMethod.GET, projectName, flowName);
        return generateResponse(answerJob, "");
    }


    @POST
    @Path("getContextValue")
    public Response getContextValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException {
        //ContextID contextID, ContextKey contextKey
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        ContextKey contextKey = new PersistenceContextKey();
        contextKey.setKey("flow1.node1");
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, contextKey);
        return generateResponse(answerJob,"");
    }


/*    {
        "type":"And",
            "left":{
        "type":"ContextType",
                "contextType":"DATA"
    },
        "right":{
        "type":"And",
                "left":{
            "type":"ContextScope",
                    "contextScope":"PRIVATE"
        },
        "right":{
            "type":"Regex",
                    "regex":"[abc]]"
        }
    }
    }*/
    @POST
    @Path("searchContextValue")
    public Response searchContextValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException {
        ContextID contextID = objectMapper.convertValue(jsonNode.get("contextID"), PersistenceContextID.class);
        Map<Object, Object> conditionMap = objectMapper.convertValue(jsonNode.get("condition"), new org.codehaus.jackson.type.TypeReference<Map<Object, Object>>() {
        });
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, conditionMap);
        return generateResponse(answerJob, "");
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
    public Response setValueByKey(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException {
        /*JSONSerializer jsonSerializer = new JSONSerializer();
        PersistenceContextID contextID = new PersistenceContextID();
        //// TODO: 2020/2/26 手动修改contextid
        contextID.setContextId("84716");
        ContextKey contextKey = new PersistenceContextKey();
        //必传
        contextKey.setContextScope(ContextScope.FRIENDLY);
        contextKey.setContextScope(ContextScope.PROTECTED);
        contextKey.setContextType(ContextType.METADATA);
        contextKey.setKey("flow1.node1");
        ContextValue contextValue = new PersistenceContextValue();
        CSTable csTable = new CSTable();
        csTable.setName("table1");
        csTable.setCreator("neiljianliu");
        String encode = jsonSerializer.encode(csTable);
        contextValue.setValue(encode);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SET, contextID, contextKey, contextValue);
        return generateResponse(answerJob,"");*/
        return null;
    }

    @POST
    @Path("setValue")
    public Response setValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException, CSErrorException {
        /*JSONSerializer jsonSerializer = new JSONSerializer();
        PersistenceContextID contextID = new PersistenceContextID();
        //// TODO: 2020/2/26 手动修改contextid
        contextID.setContextId("84716");
        ContextKey contextKey = new PersistenceContextKey();
        //必传
        contextKey.setContextScope(ContextScope.FRIENDLY);
        // TODO: 2020/2/26 type是必要参数
        contextKey.setContextType(ContextType.METADATA);
        contextKey.setContextScope(ContextScope.PROTECTED);
        contextKey.setKey("flow1.node2");
        ContextValue contextValue = new PersistenceContextValue();
        CSTable csTable = new CSTable();
        csTable.setName("tableupdate");
        csTable.setCreator("neiljianliuupdate");
        String encode = jsonSerializer.encode(csTable);
        contextValue.setValue(encode);
        ContextKeyValue contextKeyValue = new PersistenceContextKeyValue();
        contextKeyValue.setContextValue(contextValue);
        contextKeyValue.setContextKey(contextKey);
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SET, contextID, contextKeyValue);
        return generateResponse(answerJob,"");*/
        return null;
    }

    @POST
    @Path("resetValue")
    public Response resetValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException {
        ContextID contextID = null;
        ContextKey contextKey = null;
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.RESET, contextID, contextKey);
        return generateResponse(answerJob,"");
    }

    @POST
    @Path("removeValue")
    public Response removeValue(@Context HttpServletRequest req,JsonNode jsonNode) throws InterruptedException {
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        ContextKey contextKey = new PersistenceContextKey();
        contextKey.setKey("flow1.node1");
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, contextKey);
        return generateResponse(answerJob,"");
    }

    @POST
    @Path("removeAllValue")
    public Response removeAllValue(@Context HttpServletRequest req, JsonNode jsonNode) throws InterruptedException {
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVEALL, contextID);
        return generateResponse(answerJob,"");
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
