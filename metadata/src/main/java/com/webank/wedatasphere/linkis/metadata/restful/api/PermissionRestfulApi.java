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

package com.webank.wedatasphere.linkis.metadata.restful.api;

import com.webank.wedatasphere.linkis.metadata.domain.View;
import com.webank.wedatasphere.linkis.metadata.restful.remote.PermissionRestfulRemote;
import com.webank.wedatasphere.linkis.metadata.service.PermissionService;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
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
/**
 * Created by shanhuang on 9/13/18.
 */
@Path("permission")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class PermissionRestfulApi implements PermissionRestfulRemote {

    private static final Logger logger = LoggerFactory.getLogger(PermissionRestfulApi.class);


    @Autowired
    PermissionService permissionService;


    @POST
    @Path("create")
    public Response createPermission(View view, @Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            permissionService.createPermission(view, userName);
        } catch (Exception e) {
            logger.error("create permission error ", e);
        }
        return Message.messageToResponse(Message.ok("").data("permissions", view.getPermissions()));
    }
}
