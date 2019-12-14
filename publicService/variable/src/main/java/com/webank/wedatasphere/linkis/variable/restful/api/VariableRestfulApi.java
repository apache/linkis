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

package com.webank.wedatasphere.linkis.variable.restful.api;

import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.variable.entity.VarKeyValueVO;
import com.webank.wedatasphere.linkis.variable.exception.VariableException;
import com.webank.wedatasphere.linkis.variable.service.VariableService;
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

/**
 * Created by cooperyang on 2018/10/17.
 */

@Component
@Path("/variable")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VariableRestfulApi {

    @Autowired
    private VariableService variableService;

    ObjectMapper mapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /*@POST
    @Path("addGlobalVariable")
    public Response addGlobalVariable(@Context HttpServletRequest req, JsonNode json) throws IOException {
        String userName = SecurityFilter.getLoginUsername(req);
        List globalVariables = mapper.readValue(json.get("globalVariables"), List.class);
        globalVariables.stream().forEach(f -> {
            String j = BDPJettyServerHelper.gson().toJson(f);
            variableService.addGlobalVariable(BDPJettyServerHelper.gson().fromJson(j, VarKeyValueVO.class), userName);
        });
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("removeGlobalVariable")
    public Response removeGlobalVariable(@Context HttpServletRequest req, JsonNode json) {
        String userName = SecurityFilter.getLoginUsername(req);
        Long keyID = json.get("keyID").getLongValue();
        variableService.removeGlobalVariable(keyID);
        return Message.messageToResponse(Message.ok());
    }*/

    @GET
    @Path("listGlobalVariable")
    public Response listGlobalVariable(@Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<VarKeyValueVO> kvs = variableService.listGlobalVariable(userName);
        return Message.messageToResponse(Message.ok().data("globalVariables", kvs));
    }

    @POST
    @Path("saveGlobalVariable")
    public Response saveGlobalVariable(@Context HttpServletRequest req, JsonNode json) throws IOException, VariableException {
        String userName = SecurityFilter.getLoginUsername(req);
        List<VarKeyValueVO> userVariables = variableService.listGlobalVariable(userName);
        List globalVariables = mapper.readValue(json.get("globalVariables"), List.class);
        variableService.saveGlobalVaraibles(globalVariables, userVariables, userName);
        return Message.messageToResponse(Message.ok());
    }
}
