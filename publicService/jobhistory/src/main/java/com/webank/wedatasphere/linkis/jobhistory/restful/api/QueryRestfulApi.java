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

package com.webank.wedatasphere.linkis.jobhistory.restful.api;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.webank.wedatasphere.linkis.jobhistory.entity.QueryTask;
import com.webank.wedatasphere.linkis.jobhistory.entity.QueryTaskVO;
import com.webank.wedatasphere.linkis.jobhistory.exception.QueryException;
import com.webank.wedatasphere.linkis.jobhistory.service.QueryService;
import com.webank.wedatasphere.linkis.jobhistory.util.QueryUtil;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * johnnwang
 * 018/10/19
 */
@Component
@Path("/jobhistory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QueryRestfulApi{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private QueryService queryService;

    @GET
    @Path("/{id}/get")
    public Response getTaskByID(@Context HttpServletRequest req, @PathParam("id") Long taskID) {
        String username = SecurityFilter.getLoginUsername(req);
        QueryTaskVO vo = queryService.getTaskByID(taskID,username);
        return Message.messageToResponse(Message.ok().data("task",vo));
    }

    @GET
    @Path("/list")
    public Response list(@Context HttpServletRequest req,@QueryParam("startDate") Long startDate,
                         @QueryParam("endDate") Long endDate,@QueryParam("status") String status,
                         @QueryParam("pageNow") Integer pageNow,@QueryParam("pageSize") Integer pageSize,
                         @QueryParam("taskID") Long taskID,@QueryParam("executeApplicationName")String executeApplicationName) throws IOException, QueryException {
        String username = SecurityFilter.getLoginUsername(req);
        if(StringUtils.isEmpty(pageNow)){
            pageNow = 1;
        }
        if(StringUtils.isEmpty(pageSize)){
            pageSize = 20;
        }
        if (startDate != null && endDate == null){
            endDate = System.currentTimeMillis();
        }
        PageHelper.startPage(pageNow,pageSize);
        Date sDate = null;
        Date eDate = null;
        if (startDate != null){
            sDate= new Date(startDate);
        }
        if (endDate != null){
            //eDate= new Date(endDate);
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(endDate);
            instance.add(Calendar.DAY_OF_MONTH,1);
            eDate  = instance.getTime();
        }
        List<QueryTask> queryTasks = queryService.search(taskID,username,status, sDate,eDate,executeApplicationName);
        PageInfo<QueryTask> pageInfo = new PageInfo<>(queryTasks);
        List<QueryTask> list = pageInfo.getList();
        long total = pageInfo.getTotal();
        List<QueryTaskVO> vos = QueryUtil.getQueryVOList(list);
        return Message.messageToResponse(Message.ok().data("tasks", vos).data("totalPage",total));
    }
}
