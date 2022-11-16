/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.jobhistory.restful.api;

import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.entity.job.QueryException;
import org.apache.linkis.jobhistory.cache.impl.DefaultQueryCacheManager;
import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.entity.*;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
import org.apache.linkis.jobhistory.util.QueryUtils;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.lang3.time.DateUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "query api")
@RestController
@RequestMapping(path = "/jobhistory")
public class QueryRestfulApi {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private JobHistoryQueryService jobHistoryQueryService;

  @Autowired private DefaultQueryCacheManager queryCacheManager;

  @ApiOperation(
      value = "governanceStationAdmin",
      notes = "get admin user name",
      response = Message.class)
  @RequestMapping(path = "/governanceStationAdmin", method = RequestMethod.GET)
  public Message governanceStationAdmin(HttpServletRequest req) {
    String username = ModuleUserUtils.getOperationUser(req, "governanceStationAdmin");
    String[] split = JobhistoryConfiguration.GOVERNANCE_STATION_ADMIN().getValue().split(",");
    boolean match = Arrays.stream(split).anyMatch(username::equalsIgnoreCase);
    return Message.ok().data("admin", match);
  }

  @ApiOperation(value = "getTaskByID", notes = "get task by id", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "jobId", required = true, dataType = "long", example = "12345")
  })
  @RequestMapping(path = "/{id}/get", method = RequestMethod.GET)
  public Message getTaskByID(HttpServletRequest req, @PathVariable("id") Long jobId) {
    String username = SecurityFilter.getLoginUsername(req);
    if (QueryUtils.isJobHistoryAdmin(username)
        || !JobhistoryConfiguration.JOB_HISTORY_SAFE_TRIGGER()) {
      username = null;
    }
    JobHistory jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(jobId, username);

    try {
      if (null != jobHistory) {
        QueryUtils.exchangeExecutionCode(jobHistory);
      }
    } catch (Exception e) {
      log.error("Exchange executionCode for job with id : {} failed, {}", jobHistory.getId(), e);
    }
    QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
    // todo check
    if (taskVO == null) {
      return Message.error(
          "The corresponding job was not found, or there may be no permission to view the job"
              + "(没有找到对应的job，也可能是没有查看该job的权限)");
    }

    return Message.ok().data(TaskConstant.TASK, taskVO);
  }

  /** Method list should not contain subjob, which may cause performance problems. */
  @ApiOperation(value = "list", notes = "list", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long", example = "1658937600001"),
    @ApiImplicitParam(name = "endDate", dataType = "long", example = "1658937600000"),
    @ApiImplicitParam(name = "status", dataType = "String", example = ""),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", value = "page now"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
    @ApiImplicitParam(name = "taskID", required = false, dataType = "long", value = "task id"),
    @ApiImplicitParam(name = "executeApplicationName", dataType = "String"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "jobId", required = false, dataType = "String", value = "job id"),
    @ApiImplicitParam(name = "isAdminView", dataType = "Boolean"),
  })
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
              taskID, username, status, creator, sDate, eDate, executeApplicationName, null);
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
    return Message.ok().data(TaskConstant.TASKS, vos).data(JobRequestConstants.TOTAL_PAGE(), total);
  }

  /** Method list should not contain subjob, which may cause performance problems. */
  @ApiOperation(value = "listundonetasks", notes = "list undone tasks", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "long", value = "end date"),
    @ApiImplicitParam(name = "status", required = false, dataType = "String", value = "status"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", value = "page now"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "startTaskID", dataType = "long"),
  })
  @RequestMapping(path = "/listundonetasks", method = RequestMethod.GET)
  public Message listundonetasks(
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
    } else {
      if (!QueryUtils.checkNameValid(creator)) {
        return Message.error("Invalid creator : " + creator);
      }
    }
    if (StringUtils.isEmpty(engineType)) {
      engineType = null;
    } else {
      if (!QueryUtils.checkNameValid(engineType)) {
        return Message.error("Invalid engienType: " + engineType);
      }
    }
    Date sDate = new Date(startDate);
    Date eDate = new Date(endDate);
    if (startDate == 0L) {
      sDate = DateUtils.addDays(eDate, -1);
    }
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
    return Message.ok().data(TaskConstant.TASKS, vos).data(JobRequestConstants.TOTAL_PAGE(), total);
  }

  @ApiOperation(value = "listundone", notes = "list undone", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "long", value = "end date"),
    @ApiImplicitParam(name = "status", required = false, dataType = "String", value = "status"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", value = "page now"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(name = "engineType", dataType = "String"),
    @ApiImplicitParam(name = "startTaskID", dataType = "long"),
  })
  /** Method list should not contain subjob, which may cause performance problems. */
  @RequestMapping(path = "/listundone", method = RequestMethod.GET)
  public Message listundone(
      HttpServletRequest req,
      @RequestParam(value = "startDate", required = false) Long startDate,
      @RequestParam(value = "endDate", required = false) Long endDate,
      @RequestParam(value = "pageNow", required = false) Integer pageNow,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "startTaskID", required = false) Long taskID,
      @RequestParam(value = "engineType", required = false) String engineType,
      @RequestParam(value = "creator", required = false) String creator)
      throws IOException, QueryException {
    String username = SecurityFilter.getLoginUsername(req);
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
    if (StringUtils.isEmpty(engineType)) {
      engineType = null;
    } else {
      if (!QueryUtils.checkNameValid(engineType)) {
        return Message.error("Invalid engienType: " + engineType);
      }
    }
    Date sDate = new Date(startDate);
    Date eDate = new Date(endDate);
    if (startDate == 0L) {
      sDate = DateUtils.addDays(eDate, -1);
    }
    if (sDate.getTime() == eDate.getTime()) {
      Calendar instance = Calendar.getInstance();
      instance.setTimeInMillis(endDate);
      instance.add(Calendar.DAY_OF_MONTH, 1);
      eDate = new Date(instance.getTime().getTime());
    }
    Integer total =
        jobHistoryQueryService.countUndoneTasks(
            username, creator, sDate, eDate, engineType, queryCacheManager.getUndoneTaskMinId());

    return Message.ok().data(JobRequestConstants.TOTAL_PAGE(), total);
  }
}
