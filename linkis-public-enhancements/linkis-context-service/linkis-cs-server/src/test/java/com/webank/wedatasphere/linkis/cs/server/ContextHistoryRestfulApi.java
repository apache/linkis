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

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.history.ContextHistory;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextHistory;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextID;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/contextservice")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContextHistoryRestfulApi implements CsRestfulParent {

    @Autowired
    private CsScheduler csScheduler;

    @POST
    @Path("createHistory")
    public Response createHistory(@Context HttpServletRequest req, JsonNode json) throws InterruptedException, CSErrorException {
        ContextHistory history = new PersistenceContextHistory();
        history.setSource("server1:prot1");
        history.setContextType(ContextType.METADATA);
        ContextID contextID = new PersistenceContextID();

        contextID.setContextId("84716");
        //source and contextid cannot be empty
        if (StringUtils.isEmpty(history.getSource())) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.CREATE, contextID, history);
        return generateResponse(answerJob, "");
    }

    @POST
    @Path("removeHistory")
    public Response removeHistory(@Context HttpServletRequest req, JsonNode json) throws InterruptedException, CSErrorException {
        ContextHistory history = new PersistenceContextHistory();
        history.setSource("server1:prot1");
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        //source and contextid cannot be empty
        if (StringUtils.isEmpty(history.getSource())) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.REMOVE, contextID, history);
        return generateResponse(answerJob, "");
    }


    @GET
    @Path("getHistories")
    public Response getHistories(@Context HttpServletRequest req, JsonNode json) throws InterruptedException, CSErrorException {
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID);
        return generateResponse(answerJob, "");
    }

    @GET
    @Path("getHistory")
    public Response getHistory(@Context HttpServletRequest req, JsonNode json) throws InterruptedException, CSErrorException {
        //ContextID contextID, String source
        String source = "server1:prot1";
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        //source and contextid cannot be empty
        if (StringUtils.isEmpty(source)) {
            throw new CSErrorException(97000, "history source cannot be empty");
        }
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.GET, contextID, source);
        return generateResponse(answerJob, "");
    }

    @GET
    @Path("searchHistory")
    public Response searchHistory(@Context HttpServletRequest req, JsonNode json) throws InterruptedException, CSErrorException {
        //ContextID contextID, String[] keywords
        ContextID contextID = new PersistenceContextID();
        contextID.setContextId("84716");
        String[] keywords = new String[]{"keyword1","keyword2"};
        if (StringUtils.isEmpty(contextID.getContextId())) {
            throw new CSErrorException(97000, "contxtId cannot be empty");
        }
        HttpAnswerJob answerJob = submitRestJob(req, ServiceMethod.SEARCH, contextID, keywords);
        return generateResponse(answerJob, "");
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
