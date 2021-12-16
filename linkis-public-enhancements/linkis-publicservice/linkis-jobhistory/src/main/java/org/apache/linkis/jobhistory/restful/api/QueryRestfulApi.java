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
 
package org.apache.linkis.jobhistory.restful.api;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.entity.job.SubJobDetail;
import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.dao.JobDetailMapper;
import org.apache.linkis.jobhistory.entity.*;
import org.apache.linkis.governance.common.entity.job.QueryException;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
import org.apache.linkis.jobhistory.util.QueryUtils;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.Date;

@RestController
@RequestMapping(path = "/jobhistory")
public class QueryRestfulApi {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobHistoryQueryService jobHistoryQueryService;
    @Autowired
    private JobDetailMapper jobDetailMapper;

    @RequestMapping(path = "/governanceStationAdmin",method = RequestMethod.GET)
    public Message governanceStationAdmin(HttpServletRequest req) {
        String username = SecurityFilter.getLoginUsername(req);
        String[] split = JobhistoryConfiguration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        boolean match = Arrays.stream(split).anyMatch(username::equalsIgnoreCase);
        return Message.ok().data("admin", match);
    }

    @RequestMapping(path = "/{id}/get",method = RequestMethod.GET)
    public Message getTaskByID(HttpServletRequest req, @PathVariable("id") Long jobId) {
        String username = SecurityFilter.getLoginUsername(req);
        if (QueryUtils.isJobHistoryAdmin(username) || !JobhistoryConfiguration.JOB_HISTORY_SAFE_TRIGGER()) {
            username = null;
        }
        JobHistory jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(jobId, username);
        List<SubJobDetail> subJobDetails = TaskConversions.jobdetails2SubjobDetail(jobDetailMapper.selectJobDetailByJobHistoryId(jobId));
        QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, subJobDetails);
        // todo check
        if(taskVO == null){
            return Message.error("The corresponding job was not found, or there may be no permission to view the job" +
                    "(没有找到对应的job，也可能是没有查看该job的权限)");
        }
        for (SubJobDetail subjob : subJobDetails) {
            if (!StringUtils.isEmpty(subjob.getResultLocation())) {
                taskVO.setResultLocation(subjob.getResultLocation());
                break;
            }
        }
        return Message.ok().data(TaskConstant.TASK, taskVO);
    }

    /**
     * Method list should not contain subjob, which may cause performance problems.
     */
    @RequestMapping(path = "/list",method = RequestMethod.GET)
    public Message list(HttpServletRequest req, @RequestParam(value="startDate",required=false) Long startDate,
                         @RequestParam(value="endDate",required=false) Long endDate, @RequestParam(value="status",required=false) String status,
                         @RequestParam(value="pageNow",required=false) Integer pageNow, @RequestParam(value="pageSize",required=false) Integer pageSize,
                         @RequestParam(value="taskID",required=false) Long taskID, @RequestParam(value="executeApplicationName",required=false) String executeApplicationName,
                         @RequestParam(value="proxyUser",required=false) String proxyUser) throws IOException, QueryException {
        String username = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(pageNow)) {
            pageNow = 1;
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = 20;
        }
        if (startDate != null && endDate == null) {
            endDate = System.currentTimeMillis();
        }
        Date sDate = null;
        Date eDate = null;
        if (startDate != null) {
            sDate = new Date(startDate);
        }
        if (endDate != null) {
            eDate= new Date(endDate);
//            Calendar instance = Calendar.getInstance();
//            instance.setTimeInMillis(endDate);
//            instance.add(Calendar.DAY_OF_MONTH, 1);
//            eDate = new Date(instance.getTime().getTime()); // todo check
        }
        if (proxyUser != null && QueryUtils.isJobHistoryAdmin(username)) {
            if (!StringUtils.isEmpty(proxyUser)) {
                username = proxyUser;
            } else {
                username = null;
            }
        }
        List<JobHistory> queryTasks = null;
        PageHelper.startPage(pageNow, pageSize);
        try {
            queryTasks = jobHistoryQueryService.search(taskID, username, status, sDate, eDate, executeApplicationName);
        } finally {
            PageHelper.clearPage();
        }

        PageInfo<JobHistory> pageInfo = new PageInfo<>(queryTasks);
        List<JobHistory> list = pageInfo.getList();
        long total = pageInfo.getTotal();
        List<QueryTaskVO> vos = new ArrayList<>();
        for (JobHistory jobHistory : list) {
            QueryUtils.exchangeExecutionCode(jobHistory);
//            List<JobDetail> jobDetails = jobDetailMapper.selectJobDetailByJobHistoryId(jobHistory.getId());
            QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
            vos.add(taskVO);
            /*// todo add first resultLocation to taskVO
            for (JobDetail subjob : jobDetails) {
                if (!StringUtils.isEmpty(subjob.getResult_location())) {
                    taskVO.setResultLocation(subjob.getResult_location());
                }
                break;
            }*/
        }
        return Message.ok().data(TaskConstant.TASKS, vos)
                .data(JobRequestConstants.TOTAL_PAGE(), total);
    }
}
