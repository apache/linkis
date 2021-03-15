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

import com.webank.wedatasphere.linkis.manager.am.service.engine.EngineInfoService;
import com.webank.wedatasphere.linkis.manager.common.entity.node.AMEMNode;
import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component
@Path("linkisManager")
public class EngineRestfulApi {

    @Autowired
    private EngineInfoService engineInfoService;

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

}
