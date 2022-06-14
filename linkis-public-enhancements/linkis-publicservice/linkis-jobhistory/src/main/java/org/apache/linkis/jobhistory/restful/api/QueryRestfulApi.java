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

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import io.swagger.annotations.*;
import io.swagger.models.Swagger;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.entity.job.QueryException;
import org.apache.linkis.jobhistory.cache.impl.DefaultQueryCacheManager;
import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.dao.JobDetailMapper;
import org.apache.linkis.jobhistory.entity.*;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
import org.apache.linkis.jobhistory.util.QueryUtils;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Api(tags = "管理台首页功能接口")
@RestController
@RequestMapping(path = "/jobhistory")
public class QueryRestfulApi {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired private JobHistoryQueryService jobHistoryQueryService;
    @Autowired private JobDetailMapper jobDetailMapper;

    @Autowired private DefaultQueryCacheManager queryCacheManager;

    @ApiOperation(value="管理员验证", notes="用来验证是否为管理员，如果是则返回true不是则false")
    /*@ApiOperationSupport(
            responses = @DynamicResponseParameters(properties = {
                    @DynamicParameter(value = "验证是否为管理员false不是true即为是管理员",name = "admin", required=true),
                    @DynamicParameter(value = "名称",name = "name"),
            })

    )*/
    @RequestMapping(path = "/governanceStationAdmin", method = RequestMethod.GET)
    public Message governanceStationAdmin(HttpServletRequest req) {

        String username = ModuleUserUtils.getOperationUser(req, "governanceStationAdmin");
        String[] split = JobhistoryConfiguration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
        boolean match = Arrays.stream(split).anyMatch(username::equalsIgnoreCase);
        return Message.ok().data("admin", match);
    }
    @ApiOperation(value="历史详细记录", notes="通过历史记录ID获取某条历史的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="id",value="历史记录Id",paramType="query",dataType="Long")

    })
    @RequestMapping(path = "/{id}/get", method = RequestMethod.GET)
    public Message getTaskByID(HttpServletRequest req, @PathVariable("id") Long jobId) {
        String username = SecurityFilter.getLoginUsername(req);
        if (QueryUtils.isJobHistoryAdmin(username)
                || !JobhistoryConfiguration.JOB_HISTORY_SAFE_TRIGGER()) {
            username = null;
        }
        JobHistory jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(jobId, username);
        //        List<SubJobDetail> subJobDetails =
        // TaskConversions.jobdetails2SubjobDetail(jobDetailMapper.selectJobDetailByJobHistoryId(jobId));
        try {
            if (null != jobHistory) {
                QueryUtils.exchangeExecutionCode(jobHistory);
            }
        } catch (Exception e) {
            log.error(
                    "Exchange executionCode for job with id : {} failed, {}",
                    jobHistory.getId(),
                    e);
        }
        QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
        // todo check
        if (taskVO == null) {
            return Message.error(
                    "The corresponding job was not found, or there may be no permission to view the job"
                            + "(没有找到对应的job，也可能是没有查看该job的权限)");
        }
        //        for (SubJobDetail subjob : subJobDetails) {
        //            if (!StringUtils.isEmpty(subjob.getResultLocation())) {
        //                taskVO.setResultLocation(subjob.getResultLocation());
        //                break;
        //            }
        //        }
        return Message.ok().data(TaskConstant.TASK, taskVO);
    }

    @ApiOperation(value="全局历史", notes="根据条件获取全局历史数据列表默认获取全部")
    @ApiImplicitParams({
            @ApiImplicitParam(name="taskID",value="ID",paramType="query",dataType="Long"),
            @ApiImplicitParam(name="tpageNow",value="页码",paramType="query",dataType="Integer"),
            @ApiImplicitParam(name="pageSize",value="页面数量",paramType="query",dataType="Integer"),
            @ApiImplicitParam(name="isAdminView",value="是否为管理员模式或者普通模式",paramType="query",dataType="Boolean"),
            @ApiImplicitParam(name="startDate",value="开始时间",paramType="query",dataType="Long"),
            @ApiImplicitParam(name="endDate",value="结束时间",paramType="query",dataType="Long"),
            @ApiImplicitParam(name="status",value="结束时间",paramType="query",dataType="String"),
            @ApiImplicitParam(name="executeApplicationName",value="操作人",paramType="query",dataType="String"),
            @ApiImplicitParam(name="creator",value="创建者",paramType="query",dataType="String"),
            @ApiImplicitParam(name="proxyUser",value="代理用户",paramType="query",dataType="String"),

    })
    /** Method list should not contain subjob, which may cause performance problems. */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public Message list(
            HttpServletRequest req,
            @RequestParam(value = "startDate", required = false) Long startDate,
            @RequestParam(value = "endDate", required = false) Long endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNow", required = false) Integer pageNow,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "taskID", required = false) Long taskID,
            @RequestParam(value = "executeApplicationName", required = false)
                    String executeApplicationName,
            @RequestParam(value = "creator", required = false) String creator,
            @RequestParam(value = "proxyUser", required = false) String proxyUser,
            @RequestParam(value = "isAdminView", required = false) Boolean isAdminView)
            throws IOException, QueryException {
        String username = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(status)) {
            status = null;
        }

        if (StringUtils.isEmpty(pageNow)) {
            pageNow = 1;
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = 20;
        }
        if (endDate == null) {
            endDate = System.currentTimeMillis();
        }
        if (startDate == null) {
            startDate = 0L;
        }
        if (StringUtils.isEmpty(creator)) {
            creator = null;
        } else {
            if (!QueryUtils.checkNameValid(creator)) {
                return Message.error("Invalid creator : " + creator);
            }
        }
        if (!StringUtils.isEmpty(executeApplicationName)) {
            if (!QueryUtils.checkNameValid(executeApplicationName)) {
                return Message.error("Invalid applicationName : " + executeApplicationName);
            }
        } else {
            executeApplicationName = null;
        }
        Date sDate = new Date(startDate);
        Date eDate = new Date(endDate);
        if (sDate.getTime() == eDate.getTime()) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(endDate);
            instance.add(Calendar.DAY_OF_MONTH, 1);
            eDate = new Date(instance.getTime().getTime()); // todo check
        }
        if (isAdminView == null) {
            isAdminView = false;
        }
        if (QueryUtils.isJobHistoryAdmin(username)) {
            if (isAdminView) {
                if (proxyUser != null) {
                    username = StringUtils.isEmpty(proxyUser) ? null : proxyUser;
                } else {
                    username = null;
                }
            }
        }
        List<JobHistory> queryTasks = null;
        PageHelper.startPage(pageNow, pageSize);
        try {
            queryTasks =
                    jobHistoryQueryService.search(
                            taskID,
                            username,
                            status,
                            creator,
                            sDate,
                            eDate,
                            executeApplicationName,
                            null);
        } finally {
            PageHelper.clearPage();
        }

        PageInfo<JobHistory> pageInfo = new PageInfo<>(queryTasks);
        List<JobHistory> list = pageInfo.getList();
        long total = pageInfo.getTotal();
        List<QueryTaskVO> vos = new ArrayList<>();
        for (JobHistory jobHistory : list) {
            QueryUtils.exchangeExecutionCode(jobHistory);
            //            List<JobDetail> jobDetails =
            // jobDetailMapper.selectJobDetailByJobHistoryId(jobHistory.getId());
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
        return Message.ok()
                .data(TaskConstant.TASKS, vos)
                .data(JobRequestConstants.TOTAL_PAGE(), total);
    }

    /** Method list should not contain subjob, which may cause performance problems. */
    @RequestMapping(path = "/listundone", method = RequestMethod.GET)
    public Message listundone(
            HttpServletRequest req,
            @RequestParam(value = "startDate", required = false) Long startDate,
            @RequestParam(value = "endDate", required = false) Long endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNow", required = false) Integer pageNow,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "startTaskID", required = false) Long taskID,
            @RequestParam(value = "engineType", required = false) String engineType,
            @RequestParam(value = "creator", required = false) String creator)
            throws IOException, QueryException {
        String username = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(status)) {
            status = "Running,Inited,Scheduled";
        }
        if (StringUtils.isEmpty(pageNow)) {
            pageNow = 1;
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = 20;
        }
        if (endDate == null) {
            endDate = System.currentTimeMillis();
        }
        if (startDate == null) {
            startDate = 0L;
        }
        if (StringUtils.isEmpty(creator)) {
            creator = null;
        }
        Date sDate = new Date(startDate);
        Date eDate = new Date(endDate);
        if (sDate.getTime() == eDate.getTime()) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(endDate);
            instance.add(Calendar.DAY_OF_MONTH, 1);
            eDate = new Date(instance.getTime().getTime()); // todo check
        }
        List<JobHistory> queryTasks = null;
        PageHelper.startPage(pageNow, pageSize);
        try {
            queryTasks =
                    jobHistoryQueryService.search(
                            taskID,
                            username,
                            status,
                            creator,
                            sDate,
                            eDate,
                            engineType,
                            queryCacheManager.getUndoneTaskMinId());
        } finally {
            PageHelper.clearPage();
        }

        PageInfo<JobHistory> pageInfo = new PageInfo<>(queryTasks);
        List<JobHistory> list = pageInfo.getList();
        long total = pageInfo.getTotal();
        List<QueryTaskVO> vos = new ArrayList<>();
        for (JobHistory jobHistory : list) {
            QueryUtils.exchangeExecutionCode(jobHistory);
            QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
            vos.add(taskVO);
        }
        return Message.ok()
                .data(TaskConstant.TASKS, vos)
                .data(JobRequestConstants.TOTAL_PAGE(), total);
    }
}
