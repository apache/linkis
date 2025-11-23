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

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.LinkisCommonErrorException;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.entity.job.QueryException;
import org.apache.linkis.jobhistory.cache.impl.DefaultQueryCacheManager;
import org.apache.linkis.jobhistory.conf.JobhistoryConfiguration;
import org.apache.linkis.jobhistory.conversions.TaskConversions;
import org.apache.linkis.jobhistory.entity.*;
import org.apache.linkis.jobhistory.service.JobHistoryDiagnosisService;
import org.apache.linkis.jobhistory.service.JobHistoryQueryService;
import org.apache.linkis.jobhistory.transitional.TaskStatus;
import org.apache.linkis.jobhistory.util.JobhistoryUtils;
import org.apache.linkis.jobhistory.util.QueryUtils;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.Consts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
  private Sender sender =
      Sender.getSender(
          Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue());;

  @Autowired private JobHistoryQueryService jobHistoryQueryService;

  @Autowired private DefaultQueryCacheManager queryCacheManager;
  @Autowired private JobHistoryDiagnosisService jobHistoryDiagnosisService;

  @ApiOperation(
      value = "governanceStationAdmin",
      notes = "get admin user name",
      response = Message.class)
  @RequestMapping(path = "/governanceStationAdmin", method = RequestMethod.GET)
  public Message governanceStationAdmin(HttpServletRequest req) {
    String username = ModuleUserUtils.getOperationUser(req, "governanceStationAdmin");
    String departmentId = JobhistoryUtils.getDepartmentByuser(username);
    return Message.ok()
        .data("admin", Configuration.isAdmin(username))
        .data("historyAdmin", Configuration.isJobHistoryAdmin(username))
        .data("deptAdmin", Configuration.isDepartmentAdmin(username))
        .data("canResultSet", Configuration.canResultSetByDepartment(departmentId))
        .data("errorMsgTip", Configuration.ERROR_MSG_TIP().getValue());
  }

  @ApiOperation(value = "getTaskByID", notes = "get task by id", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "jobId", required = true, dataType = "long", example = "12345"),
    @ApiImplicitParam(
        name = "brief",
        required = false,
        dataType = "boolean",
        value = "only return brief info if true")
  })
  @RequestMapping(path = "/{id}/get", method = RequestMethod.GET)
  public Message getTaskByID(
      HttpServletRequest req,
      @PathVariable("id") Long jobId,
      @RequestParam(value = "brief", required = false) String brief) {
    String username = SecurityFilter.getLoginUsername(req);
    if (Configuration.isJobHistoryAdmin(username)
        || !JobhistoryConfiguration.JOB_HISTORY_SAFE_TRIGGER()
        || Configuration.isDepartmentAdmin(username)) {
      username = null;
    }
    JobHistory jobHistory = null;
    if (Boolean.parseBoolean(brief)) {
      jobHistory = jobHistoryQueryService.getJobHistoryByIdAndNameBrief(jobId, username);
    } else if (JobhistoryConfiguration.JOB_HISTORY_QUERY_EXECUTION_CODE_SWITCH()) {
      // 简要模式或配置为不查询执行代码时，使用NoCode方法
      jobHistory = jobHistoryQueryService.getJobHistoryByIdAndNameNoCode(jobId, username);
    } else {
      jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(jobId, username);
      try {
        if (null != jobHistory) {
          QueryUtils.exchangeExecutionCode(jobHistory);
        }
      } catch (Exception e) {
        log.error("Exchange executionCode for job with id : {} failed, {}", jobHistory.getId(), e);
      }
    }

    QueryTaskVO taskVO;
    if (Boolean.parseBoolean(brief)) {
      taskVO = TaskConversions.jobHistory2BriefTaskVO(jobHistory);
    } else {
      taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
    }

    // todo check  20503 is retry error code
    if (taskVO == null) {
      return Message.error(
          "The corresponding job was not found, or there may be no permission to view the job"
              + "(没有找到对应的job，也可能是没有查看该job的权限)");
    } else if (taskVO.getStatus().equals(TaskStatus.Running.toString())
        && !Integer.valueOf(20503).equals(taskVO.getErrCode())) {
      //  任务运行时不显示异常信息(Do not display exception information during task runtime)
      taskVO.setErrCode(null);
      taskVO.setErrDesc(null);
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
    @ApiImplicitParam(name = "instance", required = false, dataType = "String", value = "instance"),
    @ApiImplicitParam(
        name = "engineInstance",
        required = false,
        dataType = "String",
        value = "engineInstance"),
    @ApiImplicitParam(name = "runType", required = false, dataType = "String", value = "runType")
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
      @RequestParam(value = "isAdminView", required = false) Boolean isAdminView,
      @RequestParam(value = "isDeptView", required = false) Boolean isDeptView,
      @RequestParam(value = "instance", required = false) String instance,
      @RequestParam(value = "engineInstance", required = false) String engineInstance,
      @RequestParam(value = "runType", required = false) String runType)
      throws IOException, QueryException {
    List<JobHistory> queryTasks = null;
    try {
      queryTasks =
          getJobhistoryList(
              req,
              startDate,
              endDate,
              status,
              pageNow,
              pageSize,
              taskID,
              executeApplicationName,
              creator,
              proxyUser,
              isAdminView,
              isDeptView,
              instance,
              engineInstance,
              runType);
    } catch (Exception e) {
      return Message.error(e.getMessage());
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

  /** Method list should not contain subjob, which may cause performance problems. */
  @ApiOperation(value = "listundonetasks", notes = "list undone tasks", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "startDate",
        required = false,
        dataType = "Long",
        value = "start date"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "Long", value = "end date"),
    @ApiImplicitParam(name = "status", required = false, dataType = "String", value = "status"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", value = "page now"),
    @ApiImplicitParam(
        name = "pageSize",
        required = false,
        dataType = "Integer",
        value = "page size"),
    @ApiImplicitParam(
        name = "startTaskID",
        required = false,
        dataType = "Long",
        value = "start task id"),
    @ApiImplicitParam(
        name = "engineType",
        required = false,
        dataType = "String",
        value = "engine type"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator")
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
    if (StringUtils.isBlank(status)) {
      status = "Running,Inited,Scheduled";
    }
    if (null == pageNow) {
      pageNow = 1;
    }
    if (null == pageSize) {
      pageSize = 20;
    }
    if (endDate == null) {
      endDate = System.currentTimeMillis();
    }
    if (startDate == null) {
      startDate = 0L;
    }
    if (StringUtils.isBlank(creator)) {
      creator = null;
    } else {
      if (!QueryUtils.checkNameValid(creator)) {
        return Message.error("Invalid creator : " + creator);
      }
    }
    if (StringUtils.isBlank(engineType)) {
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
              queryCacheManager.getUndoneTaskMinId(),
              null,
              null,
              null,
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
      QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
      vos.add(taskVO);
    }
    return Message.ok().data(TaskConstant.TASKS, vos).data(JobRequestConstants.TOTAL_PAGE(), total);
  }

  @ApiOperation(value = "listundone", notes = "list undone", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = "startDate",
        required = false,
        dataType = "Long",
        value = "start date"),
    @ApiImplicitParam(name = "endDate", required = false, dataType = "Long", value = "end date"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", value = "page now"),
    @ApiImplicitParam(
        name = "pageSize",
        required = false,
        dataType = "Integer",
        value = "page size"),
    @ApiImplicitParam(
        name = "startTaskID",
        required = false,
        dataType = "Long",
        value = "startTaskID"),
    @ApiImplicitParam(
        name = "engineType",
        required = false,
        dataType = "String",
        value = "engineType"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator")
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
    if (StringUtils.isBlank(creator)) {
      creator = null;
    } else if (!QueryUtils.checkNameValid(creator)) {
      return Message.error("Invalid creator : " + creator);
    }
    if (StringUtils.isBlank(engineType)) {
      engineType = null;
    } else if (!QueryUtils.checkNameValid(engineType)) {
      return Message.error("Invalid engienType: " + engineType);
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

  @ApiOperation(value = "list-taskids", notes = "list by task id list", response = Message.class)
  @RequestMapping(path = "/list-taskids", method = RequestMethod.GET)
  public Message listTaskIds(
      HttpServletRequest req, @RequestParam(value = "taskID", required = false) String taskids) {
    String username = SecurityFilter.getLoginUsername(req);
    if (Configuration.isAdmin(username) || Configuration.isJobHistoryAdmin(username)) {
      username = null;
    }
    if (StringUtils.isBlank(taskids)) {
      return Message.error("Invalid taskID cannot be empty");
    }
    Matcher matcher = Pattern.compile("^[0-9,]*$").matcher(taskids);
    if (!matcher.matches()) {
      return Message.error("TaskID contains illegal characters");
    }
    List<String> taskidList =
        Arrays.stream(taskids.split(",")).distinct().collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(taskidList)
        && taskidList.size() > JobhistoryConfiguration.JOB_HISTORY_QUERY_LIMIT().getValue()) {
      return Message.error("TaskID size cannot exceed 30");
    }
    List<JobHistory> jobHistories = jobHistoryQueryService.searchByTasks(taskidList, username);
    List<QueryTaskVO> vos = new ArrayList<>();
    for (JobHistory jobHistory : jobHistories) {
      QueryUtils.exchangeExecutionCode(jobHistory);
      QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
      vos.add(taskVO);
    }
    return Message.ok().data(JobRequestConstants.JOB_HISTORY_LIST(), vos);
  }

  @ApiOperation(
      value = "job-extra-info",
      notes = "job extra info:metrix info ,job runtime",
      response = Message.class)
  @RequestMapping(path = "/job-extra-info", method = RequestMethod.GET)
  public Message jobeExtraInfo(
      HttpServletRequest req, @RequestParam(value = "jobId", required = false) Long jobId) {
    String username = SecurityFilter.getLoginUsername(req);
    if (null == jobId) {
      return Message.error("Invalid jobId cannot be empty");
    }
    JobHistory jobHistory = null;
    if (Configuration.isJobHistoryAdmin(username) || Configuration.isAdmin(username)) {
      username = null;
      jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(jobId, username);
    } else {
      if (Configuration.isDepartmentAdmin(username)) {
        String departmentId = JobhistoryUtils.getDepartmentByuser(username);
        if (StringUtils.isNotBlank(departmentId)) {
          List<JobHistory> list =
              jobHistoryQueryService.search(
                  jobId, null, null, null, null, null, null, null, null, departmentId, null, null);
          if (!CollectionUtils.isEmpty(list)) {
            jobHistory = list.get(0);
          }
        }
      } else {
        jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(jobId, username);
      }
    }

    if (jobHistory == null) {
      return Message.error(
          "The corresponding job was not found, or there may be no permission to view the job"
              + "(没有找到对应的job，也可能是没有查看该job的权限)");
    } else {
      try {
        QueryUtils.exchangeExecutionCode(jobHistory);
      } catch (Exception e) {
        log.error("Exchange executionCode for job with id : {} failed, {}", jobHistory.getId(), e);
      }
    }
    Map<String, String> metricsMap =
        BDPJettyServerHelper.gson().fromJson(jobHistory.getMetrics(), Map.class);
    metricsMap.put("executionCode", jobHistory.getExecutionCode());
    metricsMap.put("runtime", TaskConversions.getJobRuntime(metricsMap));
    return Message.ok().data("metricsMap", metricsMap);
  }

  @ApiOperation(
      value = "download job list",
      notes = "download job history list",
      response = Message.class)
  @RequestMapping(path = "/download-job-list", method = RequestMethod.GET)
  public void downloadJobList(
      HttpServletRequest req,
      HttpServletResponse response,
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
      @RequestParam(value = "isAdminView", required = false) Boolean isAdminView,
      @RequestParam(value = "isDeptView", required = false) Boolean isDeptView,
      @RequestParam(value = "instance", required = false) String instance,
      @RequestParam(value = "engineInstance", required = false) String engineInstance)
      throws IOException, QueryException {
    String userName = ModuleUserUtils.getOperationUser(req, "downloadEngineLog");
    String language = req.getHeader("Content-Language");
    ServletOutputStream outputStream = null;
    try {
      List<JobHistory> queryTasks =
          getJobhistoryList(
              req,
              startDate,
              endDate,
              status,
              pageNow,
              5000,
              taskID,
              executeApplicationName,
              creator,
              proxyUser,
              isAdminView,
              isDeptView,
              instance,
              engineInstance,
              null);
      PageInfo<JobHistory> pageInfo = new PageInfo<>(queryTasks);
      if (pageInfo.getTotal() > 5000) {
        queryTasks.addAll(
            getJobhistoryList(
                req,
                startDate,
                endDate,
                status,
                2,
                5000,
                taskID,
                executeApplicationName,
                creator,
                proxyUser,
                isAdminView,
                isDeptView,
                instance,
                engineInstance,
                null));
      }
      List<QueryTaskVO> vos =
          pageInfo.getList().stream()
              .peek(jobHistory -> QueryUtils.exchangeExecutionCode(jobHistory))
              .map(jobHistory -> TaskConversions.jobHistory2TaskVO(jobHistory, null))
              .collect(Collectors.toList());
      byte[] bytes =
          JobhistoryUtils.downLoadJobToExcel(
              vos, language, isAdminView, isDeptView, pageInfo.getTotal());
      response.setCharacterEncoding(Consts.UTF_8.toString());
      response.addHeader("Content-Type", "application/json;charset=UTF-8");
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.addHeader(
          "Content-Disposition",
          "attachment;filename=" + userName + "_" + startDate + "_history.xlsx");
      response.addHeader("Content-Length", bytes.length + "");
      outputStream = response.getOutputStream();
      outputStream.write(bytes);
    } catch (Exception e) {
      response.reset();
      log.warn("Download Job History Failed Msg :", e);
    } finally {
      if (outputStream != null) {
        outputStream.flush();
      }
      IOUtils.closeQuietly(outputStream);
    }
  }

  /** Method list should not contain subjob, which may cause performance problems. */
  @ApiOperation(value = "listDurationTop", notes = "listDurationTop", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startDate", dataType = "long", example = "1658937600001"),
    @ApiImplicitParam(name = "endDate", dataType = "long", example = "1658937600000"),
    @ApiImplicitParam(name = "executeApplicationName", dataType = "String"),
    @ApiImplicitParam(name = "creator", required = false, dataType = "String", value = "creator"),
    @ApiImplicitParam(
        name = "proxyUser",
        required = false,
        dataType = "String",
        value = "proxyUser"),
    @ApiImplicitParam(name = "pageNow", required = false, dataType = "Integer", value = "page now"),
    @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
  })
  @RequestMapping(path = "/listDurationTop", method = RequestMethod.GET)
  public Message listDurationTop(
      HttpServletRequest req,
      @RequestParam(value = "startDate", required = false) Long startDate,
      @RequestParam(value = "endDate", required = false) Long endDate,
      @RequestParam(value = "executeApplicationName", required = false)
          String executeApplicationName,
      @RequestParam(value = "creator", required = false) String creator,
      @RequestParam(value = "proxyUser", required = false) String proxyUser,
      @RequestParam(value = "pageNow", required = false) Integer pageNow,
      @RequestParam(value = "pageSize", required = false) Integer pageSize)
      throws QueryException {
    if (org.springframework.util.StringUtils.isEmpty(pageNow)) {
      pageNow = 1;
    }
    if (org.springframework.util.StringUtils.isEmpty(pageSize)) {
      pageSize = 20;
    }
    if (org.springframework.util.StringUtils.isEmpty(proxyUser)) {
      proxyUser = null;
    } else {
      if (!QueryUtils.checkNameValid(proxyUser)) {
        return Message.error("Invalid proxyUser : " + proxyUser);
      }
    }
    if (org.springframework.util.StringUtils.isEmpty(creator)) {
      creator = null;
    } else {
      if (!QueryUtils.checkNameValid(creator)) {
        return Message.error("Invalid creator : " + creator);
      }
    }
    if (!org.springframework.util.StringUtils.isEmpty(executeApplicationName)) {
      if (!QueryUtils.checkNameValid(executeApplicationName)) {
        return Message.error("Invalid applicationName : " + executeApplicationName);
      }
    } else {
      executeApplicationName = null;
    }

    if (endDate == null) {
      endDate = System.currentTimeMillis();
    }
    if (startDate == null) {
      startDate = 0L;
    }

    Date sDate = new Date(startDate);
    Date eDate = new Date(endDate);
    if (sDate.getTime() == eDate.getTime()) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(endDate);
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      eDate = new Date(calendar.getTime().getTime()); // todo check
    }
    List<JobHistory> queryTasks = null;
    PageHelper.startPage(pageNow, pageSize);
    try {
      queryTasks =
          jobHistoryQueryService.taskDurationTopN(
              sDate, eDate, proxyUser, creator, executeApplicationName);
    } finally {
      PageHelper.clearPage();
    }

    List<QueryTaskVO> vos = new ArrayList<>();
    for (JobHistory jobHistory : queryTasks) {
      QueryUtils.exchangeExecutionCode(jobHistory);
      QueryTaskVO taskVO = TaskConversions.jobHistory2TaskVO(jobHistory, null);
      vos.add(taskVO);
    }
    return Message.ok().data(TaskConstant.TASKS, vos);
  }

  private List<JobHistory> getJobhistoryList(
      HttpServletRequest req,
      Long startDate,
      Long endDate,
      String status,
      Integer pageNow,
      Integer pageSize,
      Long taskID,
      String executeApplicationName,
      String creator,
      String proxyUser,
      Boolean isAdminView,
      Boolean isDeptView,
      String instance,
      String engineInstance,
      String runType)
      throws LinkisCommonErrorException {
    String username = SecurityFilter.getLoginUsername(req);
    if (StringUtils.isBlank(status)) {
      status = null;
    }
    if (null == pageNow) {
      pageNow = 1;
    }
    if (null == pageSize) {
      pageSize = 20;
    }
    if (null == endDate) {
      endDate = System.currentTimeMillis();
    }
    if (null == startDate) {
      startDate = 0L;
    }
    if (StringUtils.isBlank(creator)) {
      creator = null;
    } else if (!QueryUtils.checkNameValid(creator)) {
      throw new LinkisCommonErrorException(21304, "Invalid creator : " + creator);
    }
    if (StringUtils.isNotBlank(executeApplicationName)) {
      if (!QueryUtils.checkNameValid(executeApplicationName)) {
        throw new LinkisCommonErrorException(
            21304, "Invalid applicationName : " + executeApplicationName);
      }
    } else {
      executeApplicationName = null;
    }
    Date sDate = new Date(startDate);
    Date eDate = new Date(endDate);
    if (sDate.getTime() == eDate.getTime()) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(endDate);
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      eDate = new Date(calendar.getTime().getTime()); // todo check
    }
    if (isAdminView == null) {
      isAdminView = false;
    }
    String departmentId = null;
    if (Configuration.isJobHistoryAdmin(username) & isAdminView) {
      if (StringUtils.isNotBlank(proxyUser)) {
        username = proxyUser;
      } else {
        username = null;
      }
    } else if (null != isDeptView && isDeptView) {
      departmentId = JobhistoryUtils.getDepartmentByuser(username);
      if (StringUtils.isNotBlank(departmentId) && StringUtils.isNotBlank(proxyUser)) {
        username = proxyUser;
      } else {
        username = null;
      }
    }
    if (StringUtils.isBlank(instance)) {
      instance = null;
    } else if (!QueryUtils.checkInstanceNameValid(instance)) {
      throw new LinkisCommonErrorException(21304, "Invalid instances : " + instance);
    }
    if (StringUtils.isBlank(engineInstance)) {
      engineInstance = null;
    } else if (!QueryUtils.checkInstanceNameValid(engineInstance)) {
      throw new LinkisCommonErrorException(21304, "Invalid instances : " + engineInstance);
    }

    List<JobHistory> queryTasks = new ArrayList<>();
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
              null,
              instance,
              departmentId,
              engineInstance,
              runType);
    } finally {
      PageHelper.clearPage();
    }
    return queryTasks;
  }

  @ApiOperation(
      value = "diagnosis-query",
      notes = "query failed task diagnosis msg",
      response = Message.class)
  @RequestMapping(path = "/diagnosis-query", method = RequestMethod.GET)
  public Message queryFailedTaskDiagnosis(
      HttpServletRequest req, @RequestParam(value = "taskID", required = false) String taskID) {
    String username = ModuleUserUtils.getOperationUser(req, "diagnosis-query");
    if (StringUtils.isBlank(taskID)) {
      return Message.error("Invalid jobId cannot be empty");
    }
    if (!QueryUtils.checkNumberValid(taskID)) {
      throw new LinkisCommonErrorException(21304, "Invalid taskID : " + taskID);
    }
    JobHistory jobHistory = null;
    boolean isAdmin = Configuration.isJobHistoryAdmin(username) || Configuration.isAdmin(username);
    boolean isDepartmentAdmin = Configuration.isDepartmentAdmin(username);
    if (isAdmin) {
      jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(Long.valueOf(taskID), null);
    } else if (isDepartmentAdmin) {
      String departmentId = JobhistoryUtils.getDepartmentByuser(username);
      if (StringUtils.isNotBlank(departmentId)) {
        List<JobHistory> list =
            jobHistoryQueryService.search(
                Long.valueOf(taskID),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                departmentId,
                null,
                null);
        if (!CollectionUtils.isEmpty(list)) {
          jobHistory = list.get(0);
        }
      }
    } else {
      jobHistory = jobHistoryQueryService.getJobHistoryByIdAndName(Long.valueOf(taskID), username);
    }
    String diagnosisMsg = "";
    if (jobHistory != null) {
      String jobStatus = jobHistory.getStatus();
      JobDiagnosis jobDiagnosis = jobHistoryDiagnosisService.selectByJobId(Long.valueOf(taskID));
      if (null == jobDiagnosis) {
        diagnosisMsg = JobhistoryUtils.getDiagnosisMsg(taskID);
        jobDiagnosis = new JobDiagnosis();
        jobDiagnosis.setJobHistoryId(Long.valueOf(taskID));
        jobDiagnosis.setDiagnosisContent(diagnosisMsg);
        jobDiagnosis.setCreatedTime(new Date());
        jobDiagnosis.setUpdatedDate(new Date());
        if (TaskStatus.isComplete(TaskStatus.valueOf(jobStatus))) {
          jobDiagnosis.setOnlyRead("1");
        }
        jobHistoryDiagnosisService.insert(jobDiagnosis);
      } else {
        if (StringUtils.isNotBlank(jobDiagnosis.getOnlyRead())
            && "1".equals(jobDiagnosis.getOnlyRead())) {
          diagnosisMsg = jobDiagnosis.getDiagnosisContent();
        } else {
          diagnosisMsg = JobhistoryUtils.getDiagnosisMsg(taskID);
          jobDiagnosis.setDiagnosisContent(diagnosisMsg);
          jobDiagnosis.setUpdatedDate(new Date());
          jobDiagnosis.setDiagnosisContent(diagnosisMsg);
          if (TaskStatus.isComplete(TaskStatus.valueOf(jobStatus))) {
            jobDiagnosis.setOnlyRead("1");
          }
          jobHistoryDiagnosisService.update(jobDiagnosis);
        }
      }
    }
    return Message.ok().data("diagnosisMsg", diagnosisMsg);
  }
}
