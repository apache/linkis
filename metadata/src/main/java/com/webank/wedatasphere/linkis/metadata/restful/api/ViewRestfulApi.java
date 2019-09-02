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
import com.webank.wedatasphere.linkis.metadata.restful.remote.ViewRestfulRemote;
import com.webank.wedatasphere.linkis.metadata.service.ViewService;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
/**
 * Created by shanhuang on 9/13/18.
 */
@Path("view")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class ViewRestfulApi implements ViewRestfulRemote {

    @Autowired
    ViewService viewService;

    @GET
    @Path("find")
    public Response find(@QueryParam("name") String name, @Context HttpServletRequest req) {
        String userName = SecurityFilter.getLoginUsername(req);
        List<View> views = viewService.find(name, userName);
        return Message.messageToResponse(Message.ok("search successful(查询成功)！").data("views", views));
    }

    @POST
    @Path("create")
    public Response create(View view, @Context HttpServletRequest req){
        String userName = SecurityFilter.getLoginUsername(req);
        try {
            viewService.create(view, userName);
        } catch (Exception e) {
            return Message.messageToResponse(Message.error("Creation failed(创建失败)", e));
        }
        return Message.messageToResponse(Message.ok("Created successfully(创建成功)").data("view", view));
    }

}
