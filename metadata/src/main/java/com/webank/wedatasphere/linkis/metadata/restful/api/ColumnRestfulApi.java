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

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.metadata.domain.Column;
import com.webank.wedatasphere.linkis.metadata.domain.View;
import com.webank.wedatasphere.linkis.metadata.restful.remote.ColumnRestfulRemote;
import com.webank.wedatasphere.linkis.metadata.service.ColumnService;
import com.webank.wedatasphere.linkis.server.Message;
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
import java.util.List;
/**
 * Created by shanhuang on 9/13/18.
 */
@Path("column")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Component
public class ColumnRestfulApi implements ColumnRestfulRemote {

    @Autowired
    ColumnService columnService;

    @POST
    @Path("lookup")
    public Response lookup(View view, @Context HttpServletRequest req){
        List<Column> columns = Lists.newArrayList();
        try {
            columns = columnService.lookup(view);
        } catch (Exception e) {
            return Message.messageToResponse(Message.error("Query failed(查询失败)", e));
        }
        return Message.messageToResponse(Message.ok("Query successful(查询成功)").data("columns", columns));
    }


}
