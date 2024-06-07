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

package org.apache.linkis.entrance.restful;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.entrance.EntranceServer;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.log.LogReader;
import org.apache.linkis.entrance.utils.JobHistoryHelper;
import org.apache.linkis.entrance.utils.RGBUtils;
import org.apache.linkis.entrance.vo.YarnResourceWithStatusVo;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.protocol.engine.JobInstance;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.protocol.utils.ZuulEntranceUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.listener.LogListener;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.scheduler.queue.SchedulerEventState;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.conf.ServerConfiguration;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.apache.linkis.utils.LinkisSpringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import scala.Option;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Description: an implementation class of EntranceRestfulRemote */
@Api(tags = "task operation")
@RestController
@RequestMapping(path = "/entrance")
public class EntranceRestfulApi implements EntranceRestfulRemote {

  private EntranceServer entranceServer;

  private static final Logger logger = LoggerFactory.getLogger(EntranceRestfulApi.class);

  @Autowired
  public void setEntranceServer(EntranceServer entranceServer) {
    this.entranceServer = entranceServer;
  }

  /**
   * The execute function handles the request submitted by the user to execute the task, and the
   * execution ID is returned to the user. execute函数处理的是用户提交执行任务的请求，返回给用户的是执行ID json Incoming
   * key-value pair(传入的键值对) Repsonse
   */
  @ApiOperation(value = "execute", notes = "execute the submitted task", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"json"})
  @Override
  @RequestMapping(path = "/execute", method = RequestMethod.POST)
  public Message execute(HttpServletRequest req, @RequestBody Map<String, Object> json) {
    Message message = null;
    String operationUser = ModuleUserUtils.getOperationUser(req);
    logger.info(
        "Begin to get execute task for user {}, Client IP {}",
        operationUser,
        LinkisSpringUtils.getClientIP(req));
    json.put(TaskConstant.EXECUTE_USER, ModuleUserUtils.getOperationUser(req));
    json.put(TaskConstant.SUBMIT_USER, SecurityFilter.getLoginUsername(req));
    HashMap<String, String> map = (HashMap<String, String>) json.get(TaskConstant.SOURCE);
    if (map == null) {
      map = new HashMap<>();
      json.put(TaskConstant.SOURCE, map);
    }
    String ip = JobHistoryHelper.getRequestIpAddr(req);
    map.put(TaskConstant.REQUEST_IP, ip);
    Job job = entranceServer.execute(json);
    JobRequest jobReq = ((EntranceJob) job).getJobRequest();
    Long jobReqId = jobReq.getId();
    ModuleUserUtils.getOperationUser(req, "execute task,id: " + jobReqId);
    String execID =
        ZuulEntranceUtils.generateExecID(
            job.getId(),
            Sender.getThisServiceInstance().getApplicationName(),
            new String[] {Sender.getThisInstance()});
    pushLog(
        LogUtils.generateInfo(
            "Your job is accepted,  jobID is "
                + execID
                + " and taskID is "
                + jobReqId
                + " in "
                + Sender.getThisServiceInstance().toString()
                + ". \n Please wait it to be scheduled(您的任务已经提交，进入排队中，如果一直没有更新日志，是任务并发达到了限制，可以在ITSM提Linkis参数修改单)"),
        job);
    message = Message.ok();
    message.setMethod("/api/entrance/execute");
    message.data("execID", execID);
    message.data("taskID", jobReqId);
    logger.info("End to get an an execID: {}, taskID: {}", execID, jobReqId);
    return message;
  }

  @ApiOperation(value = "submit", notes = "submit execute job", response = Message.class)
  @ApiOperationSupport(ignoreParameters = {"json"})
  @Override
  @RequestMapping(path = "/submit", method = RequestMethod.POST)
  public Message submit(HttpServletRequest req, @RequestBody Map<String, Object> json) {
    Message message = null;
    String executeUser = ModuleUserUtils.getOperationUser(req);
    logger.info(
        "Begin to get execute task for user {}, Client IP {}",
        executeUser,
        LinkisSpringUtils.getClientIP(req));
    json.put(TaskConstant.SUBMIT_USER, SecurityFilter.getLoginUsername(req));
    String token = ModuleUserUtils.getToken(req);
    Object tempExecuteUser = json.get(TaskConstant.EXECUTE_USER);
    // check special admin token
    if (StringUtils.isNotBlank(token) && tempExecuteUser != null) {
      if (Configuration.isAdminToken(token)) {
        logger.warn(
            "ExecuteUser variable will be replaced by system value: {} -> {}",
            tempExecuteUser,
            executeUser);
        executeUser = String.valueOf(tempExecuteUser);
      }
    }
    json.put(TaskConstant.EXECUTE_USER, executeUser);
    HashMap<String, String> map = (HashMap) json.get(TaskConstant.SOURCE);
    if (map == null) {
      map = new HashMap<>();
      json.put(TaskConstant.SOURCE, map);
    }
    String ip = JobHistoryHelper.getRequestIpAddr(req);
    map.put(TaskConstant.REQUEST_IP, ip);
    Job job = entranceServer.execute(json);
    JobRequest jobRequest = ((EntranceJob) job).getJobRequest();
    Long jobReqId = jobRequest.getId();
    ModuleUserUtils.getOperationUser(req, "submit jobReqId: " + jobReqId);
    pushLog(
        LogUtils.generateInfo(
            "Your job is accepted,  jobID is "
                + job.getId()
                + " and jobReqId is "
                + jobReqId
                + " in "
                + Sender.getThisServiceInstance().toString()
                + ". \n Please wait it to be scheduled(您的任务已经提交，进入排队中，如果一直没有更新日志，是任务并发达到了限制，可以在ITSM提Linkis参数修改单)"),
        job);
    String execID =
        ZuulEntranceUtils.generateExecID(
            job.getId(),
            Sender.getThisServiceInstance().getApplicationName(),
            new String[] {Sender.getThisInstance()});
    message = Message.ok();
    message.setMethod("/api/entrance/submit");
    message.data("execID", execID);
    message.data("taskID", jobReqId);
    logger.info("End to get an an execID: {}, taskID: {}", execID, jobReqId);
    return message;
  }

  private void pushLog(String log, Job job) {
    entranceServer.getEntranceContext().getOrCreateLogManager().onLogUpdate(job, log);
  }

  private JobInstance parseHeaderToJobInstance(HttpServletRequest req)
      throws JsonProcessingException {
    String jobStr =
        req.getHeader(ServerConfiguration.LINKIS_SERVER_ENTRANCE_HEADER_KEY().getValue());
    return BDPJettyServerHelper.gson().fromJson(jobStr, JobInstance.class);
  }

  @ApiOperation(value = "status", notes = "get task stats", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "taskID", required = false, dataType = "String", value = " task id"),
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "execute id ")
  })
  @Override
  @RequestMapping(path = "/{id}/status", method = RequestMethod.GET)
  public Message status(
      HttpServletRequest req,
      @PathVariable("id") String id,
      @RequestParam(value = "taskID", required = false) String taskID) {
    ModuleUserUtils.getOperationUser(req, "job status");
    Message message = null;
    String realId;
    String execID;
    if (id.startsWith(ZuulEntranceUtils.EXEC_ID())) {
      // execID
      realId = ZuulEntranceUtils.parseExecID(id)[3];
      execID = id;
    } else {
      // taskID
      JobInstance jobInstance;
      try {
        jobInstance = parseHeaderToJobInstance(req);
      } catch (JsonProcessingException e) {
        logger.error("parse JobInstance json error, id: {}", id);
        message = Message.error("parse JobInstance json error");
        message.setMethod("/api/entrance/" + id + "/status");
        return message;
      }

      // return ok when job complete
      if (SchedulerEventState.isCompletedByStr(jobInstance.status())) {
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/status");
        message.data("status", jobInstance.status()).data("execID", "").data("taskID", id);
        return message;
      } else if (jobInstance.instanceRegistryTimestamp() > jobInstance.createTimestamp()) {
        logger.warn("The job {} wait failover, return status is Inited", id);
        String status = SchedulerEventState.Inited().toString();
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/status");
        message.data("status", status).data("execID", "").data("taskID", id);
        return message;
      } else {
        realId = jobInstance.jobReqId();
        execID =
            ZuulEntranceUtils.generateExecID(
                realId,
                Sender.getThisServiceInstance().getApplicationName(),
                new String[] {Sender.getThisInstance()});
      }
    }

    Option<Job> job = null;
    try {
      job = entranceServer.getJob(realId);
    } catch (Exception e) {
      logger.warn("get {} status error", realId, e);
      if (StringUtils.isEmpty(taskID)) {
        message =
            Message.error(
                "Get job by ID error and cannot obtain the corresponding task status.(获取job时发生异常，不能获取相应的任务状态)");
        return message;
      }
      long realTaskID = Long.parseLong(taskID);
      String status = JobHistoryHelper.getStatusByTaskID(realTaskID);
      message = Message.ok();
      message.setMethod("/api/entrance/" + id + "/status");
      message.data("status", status).data("execID", execID);
      return message;
    }
    if (job != null && job.isDefined()) {
      if (job.get() instanceof EntranceJob) {
        ((EntranceJob) job.get()).updateNewestAccessByClientTimestamp();
      }
      message = Message.ok();
      message.setMethod("/api/entrance/" + id + "/status");
      message.data("status", job.get().getState().toString()).data("execID", execID);
    } else {
      message =
          Message.error(
              "ID The corresponding job is empty and cannot obtain the corresponding task status.(ID 对应的job为空，不能获取相应的任务状态)");
    }
    return message;
  }

  @ApiOperation(value = "progress", notes = "get task progress info", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "exectue id")
  })
  @Override
  @RequestMapping(path = "/{id}/progress", method = RequestMethod.GET)
  public Message progress(HttpServletRequest req, @PathVariable("id") String id) {
    ModuleUserUtils.getOperationUser(req, "job progress");
    Message message = null;
    String realId;
    String execID;
    if (id.startsWith(ZuulEntranceUtils.EXEC_ID())) {
      // execID
      realId = ZuulEntranceUtils.parseExecID(id)[3];
      execID = id;
    } else {
      // taskID
      JobInstance jobInstance;
      try {
        jobInstance = parseHeaderToJobInstance(req);
      } catch (JsonProcessingException e) {
        logger.error("parse JobInstance json error, id: {}", id);
        message = Message.error("parse JobInstance json error");
        message.setMethod("/api/entrance/" + id + "/progress");
        return message;
      }

      // return ok when job complete
      if (SchedulerEventState.isCompletedByStr(jobInstance.status())) {
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/progress");
        message
            .data("progress", "1.0")
            .data("execID", "")
            .data("taskID", id)
            .data("progressInfo", new ArrayList<>());
        return message;
      } else if (jobInstance.instanceRegistryTimestamp() > jobInstance.createTimestamp()) {
        logger.warn("The job {} wait failover, return progress is 0", id);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/progress");
        message
            .data("progress", 0)
            .data("execID", "")
            .data("taskID", id)
            .data("progressInfo", new ArrayList<>());
        return message;
      } else {
        realId = jobInstance.jobReqId();
        execID =
            ZuulEntranceUtils.generateExecID(
                realId,
                Sender.getThisServiceInstance().getApplicationName(),
                new String[] {Sender.getThisInstance()});
      }
    }

    Option<Job> job = null;
    try {
      job = entranceServer.getJob(realId);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    if (job != null && job.isDefined()) {
      JobProgressInfo[] jobProgressInfos = ((EntranceJob) job.get()).getProgressInfo();
      if (jobProgressInfos == null) {
        message =
            Message.error(
                "Can not get the corresponding progress information, it may be that the corresponding progress information has not been generated(不能获取相应的进度信息,可能是相应的进度信息还未生成)");
        message.setMethod("/api/entrance/" + id + "/progress");
      } else {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobProgressInfo jobProgressInfo : jobProgressInfos) {
          if ("true".equals(EntranceConfiguration.PROGRESS_PUSH().getValue())
              || jobProgressInfo.totalTasks() > 0) {
            setJobProgressInfos(list, jobProgressInfo);
          }
        }
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/progress");

        message
            .data("progress", Math.abs(job.get().getProgress()))
            .data("execID", execID)
            .data("progressInfo", list);
      }
    } else {
      message =
          Message.error(
              "The job corresponding to the ID is empty, and the corresponding task progress cannot be obtained.(ID 对应的job为空，不能获取相应的任务进度)");
    }
    return message;
  }

  @ApiOperation(
      value = "progressWithResource",
      notes = "get progress  and resource info",
      response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "execute id")
  })
  @Override
  @RequestMapping(path = "/{id}/progressWithResource", method = RequestMethod.GET)
  public Message progressWithResource(HttpServletRequest req, @PathVariable("id") String id) {
    ModuleUserUtils.getOperationUser(req, "job progressWithResource");
    Message message = null;
    String realId;
    String execID;
    if (id.startsWith(ZuulEntranceUtils.EXEC_ID())) {
      // execID
      realId = ZuulEntranceUtils.parseExecID(id)[3];
      execID = id;
    } else {
      // taskID
      JobInstance jobInstance;
      try {
        jobInstance = parseHeaderToJobInstance(req);
      } catch (JsonProcessingException e) {
        logger.error("parse JobInstance json error, id: {}", id);
        message = Message.error("parse JobInstance json error");
        message.setMethod("/api/entrance/" + id + "/progressWithResource");
        return message;
      }

      // return ok when job complete
      if (SchedulerEventState.isCompletedByStr(jobInstance.status())) {
        long realTaskID = Long.parseLong(id);
        JobRequest jobRequest = JobHistoryHelper.getTaskByTaskID(realTaskID);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/progressWithResource");
        Map<String, Object> metricsVo = new HashMap<>();
        buildYarnResource(jobRequest, metricsVo, message);
        message
            .data("progress", "1.0")
            .data("execID", "")
            .data("taskID", id)
            .data("progressInfo", new ArrayList<>());
        return message;
      } else if (jobInstance.instanceRegistryTimestamp() > jobInstance.createTimestamp()) {
        logger.warn("The job {} wait failover, return progress is 0 and resource is null", id);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/progressWithResource");
        message
            .data(TaskConstant.JOB_YARNRESOURCE, null)
            .data("progress", 0)
            .data("execID", "")
            .data("taskID", id)
            .data("progressInfo", new ArrayList<>());
        return message;
      } else {
        realId = jobInstance.jobReqId();
        execID =
            ZuulEntranceUtils.generateExecID(
                realId,
                Sender.getThisServiceInstance().getApplicationName(),
                new String[] {Sender.getThisInstance()});
      }
    }
    Option<Job> job = null;
    try {
      job = entranceServer.getJob(realId);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    if (job != null && job.isDefined()) {
      JobProgressInfo[] jobProgressInfos = ((EntranceJob) job.get()).getProgressInfo();
      if (jobProgressInfos == null) {
        message =
            Message.error(
                "Can not get the corresponding progress information, it may be that the corresponding progress information has not been generated(不能获取相应的进度信息,可能是相应的进度信息还未生成)");
        message.setMethod("/api/entrance/" + id + "/progressWithResource");
      } else {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobProgressInfo jobProgressInfo : jobProgressInfos) {
          if ("true".equals(EntranceConfiguration.PROGRESS_PUSH().getValue())
              || jobProgressInfo.totalTasks() > 0) {
            setJobProgressInfos(list, jobProgressInfo);
          }
        }
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/progressWithResource");

        JobRequest jobRequest = ((EntranceJob) job.get()).getJobRequest();
        Map<String, Object> metricsVo = new HashMap<>();
        buildYarnResource(jobRequest, metricsVo, message);

        message
            .data("progress", Math.abs(job.get().getProgress()))
            .data("execID", execID)
            .data("progressInfo", list);
      }
    } else {
      message =
          Message.error(
              "The job corresponding to the ID is empty, and the corresponding task progress cannot be obtained.(ID 对应的job为空，不能获取相应的任务进度)");
    }
    return message;
  }

  private void buildYarnResource(
      JobRequest jobRequest, Map<String, Object> metricsVo, Message message) {
    try {
      Map<String, Object> metrics = jobRequest.getMetrics();
      if (metrics.containsKey(TaskConstant.JOB_YARNRESOURCE)) {

        HashMap<String, ResourceWithStatus> resourceMap =
            (HashMap<String, ResourceWithStatus>) metrics.get(TaskConstant.JOB_YARNRESOURCE);
        ArrayList<YarnResourceWithStatusVo> resoureList = new ArrayList<>(12);
        if (null != resourceMap && !resourceMap.isEmpty()) {
          resourceMap.forEach(
              (applicationId, resource) -> {
                resoureList.add(new YarnResourceWithStatusVo(applicationId, resource));
              });
          metricsVo.put(TaskConstant.JOB_YARNRESOURCE, resoureList);
          Optional<Integer> cores =
              resourceMap.values().stream()
                  .map(resource -> resource.getQueueCores())
                  .reduce((x, y) -> x + y);
          Optional<Long> memory =
              resourceMap.values().stream()
                  .map(resource -> resource.queueMemory())
                  .reduce((x, y) -> x + y);
          float corePercent = 0.0f;
          float memoryPercent = 0.0f;
          if (cores.isPresent() && memory.isPresent()) {
            corePercent =
                cores.get().floatValue() / EntranceConfiguration.YARN_QUEUE_CORES_MAX().getValue();
            memoryPercent =
                memory.get().floatValue()
                    / (EntranceConfiguration.YARN_QUEUE_MEMORY_MAX().getValue().longValue()
                        * 1024
                        * 1024
                        * 1024);
          }
          String coreRGB = RGBUtils.getRGB(corePercent);
          String memoryRGB = RGBUtils.getRGB(memoryPercent);
          metricsVo.put(TaskConstant.JOB_CORE_PERCENT, corePercent);
          metricsVo.put(TaskConstant.JOB_MEMORY_PERCENT, memoryPercent);
          metricsVo.put(TaskConstant.JOB_CORE_RGB, coreRGB);
          metricsVo.put(TaskConstant.JOB_MEMORY_RGB, memoryRGB);

          message.data(TaskConstant.JOB_YARN_METRICS, metricsVo);
        } else {
          message.data(TaskConstant.JOB_YARNRESOURCE, null);
        }
      } else {
        message.data(TaskConstant.JOB_YARNRESOURCE, null);
      }
    } catch (Exception e) {
      logger.error("build yarnResource error", e);
    }
  }

  private void setJobProgressInfos(
      List<Map<String, Object>> list, JobProgressInfo jobProgressInfo) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", jobProgressInfo.id());
    map.put("succeedTasks", jobProgressInfo.succeedTasks());
    map.put("failedTasks", jobProgressInfo.failedTasks());
    map.put("runningTasks", jobProgressInfo.runningTasks());
    map.put("totalTasks", jobProgressInfo.totalTasks());
    list.add(map);
  }

  @ApiOperation(value = "log", notes = "get task log", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "execute id")
  })
  @Override
  @RequestMapping(path = "/{id}/log", method = RequestMethod.GET)
  public Message log(HttpServletRequest req, @PathVariable("id") String id) {
    ModuleUserUtils.getOperationUser(req, "get job log");
    Message message = null;
    int fromLine = 0;
    int size = 100;
    boolean distinctLevel = true;
    String fromLineStr = req.getParameter("fromLine");
    String sizeStr = req.getParameter("size");
    if (StringUtils.isNotBlank(fromLineStr)) {
      fromLine = Math.max(Integer.parseInt(fromLineStr), 0);
    }
    if (StringUtils.isNotBlank(sizeStr)) {
      size = Integer.parseInt(sizeStr) >= 0 ? Integer.parseInt(sizeStr) : 10000;
    }
    String distinctLevelStr = req.getParameter("distinctLevel");
    if ("false".equals(distinctLevelStr)) {
      distinctLevel = false;
    }

    String realId;
    String execID;
    if (id.startsWith(ZuulEntranceUtils.EXEC_ID())) {
      // execID
      realId = ZuulEntranceUtils.parseExecID(id)[3];
      execID = id;
    } else {
      // taskID
      JobInstance jobInstance;
      try {
        jobInstance = parseHeaderToJobInstance(req);
      } catch (JsonProcessingException e) {
        logger.error("parse JobInstance json error, id: {}", id);
        message = Message.error("parse JobInstance json error");
        message.setMethod("/api/entrance/" + id + "/log");
        return message;
      }

      // return ok when job complete
      if (SchedulerEventState.isCompletedByStr(jobInstance.status())) {
        message =
            Message.error(
                "The job you just executed has ended. This interface no longer provides a query. It is recommended that you download the log file for viewing.(您刚刚执行的job已经结束，本接口不再提供查询，建议您下载日志文件进行查看)");
        message.setMethod("/api/entrance/" + id + "/log");
        return message;
      } else if (jobInstance.instanceRegistryTimestamp() > jobInstance.createTimestamp()) {
        logger.warn("The job {} wait failover, return customer log", id);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/log");
        String log =
            LogUtils.generateInfo(
                "The job will failover soon, please try again later.(job很快就会failover，请稍后再试)");
        Object retLog;
        if (distinctLevel) {
          String[] array = new String[4];
          array[2] = log;
          array[3] = log;
          retLog = new ArrayList<String>(Arrays.asList(array));
        } else {
          retLog = log;
        }
        message.data("log", retLog).data("execID", "").data("taskID", id).data("fromLine", 0);
        return message;
      } else {
        realId = jobInstance.jobReqId();
        execID =
            ZuulEntranceUtils.generateExecID(
                realId,
                Sender.getThisServiceInstance().getApplicationName(),
                new String[] {Sender.getThisInstance()});
      }
    }

    Option<Job> job = null;
    try {
      job = entranceServer.getJob(realId);
    } catch (final Throwable t) {
      message =
          Message.error(
              "The job you just executed has ended. This interface no longer provides a query. It is recommended that you download the log file for viewing.(您刚刚执行的job已经结束，本接口不再提供查询，建议您下载日志文件进行查看)");
      message.setMethod("/api/entrance/" + id + "/log");
      return message;
    }
    if (job != null && job.isDefined()) {
      logger.debug("begin to get log for {}(开始获取 {} 的日志)", job.get().getId(), job.get().getId());
      LogReader logReader =
          entranceServer.getEntranceContext().getOrCreateLogManager().getLogReader(realId);

      Object retLog = null;
      int retFromLine = 0;
      try {
        if (distinctLevel) {
          String[] logs = new String[4];
          retFromLine = logReader.readArray(logs, fromLine, size);
          retLog = new ArrayList<String>(Arrays.asList(logs));
        } else {
          StringBuilder sb = new StringBuilder();
          retFromLine = logReader.read(sb, fromLine, size);
          retLog = sb.toString();
        }
      } catch (IllegalStateException e) {
        logger.debug(
            "Failed to get log information for :{}(为 {} 获取日志失败)",
            job.get().getId(),
            job.get().getId(),
            e);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/log");
        message.data("log", "").data("execID", execID).data("fromLine", retFromLine + fromLine);
      } catch (final IllegalArgumentException e) {
        logger.debug(
            "Failed to get log information for :{}(为 {} 获取日志失败)",
            job.get().getId(),
            job.get().getId(),
            e);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/log");
        message.data("log", "").data("execID", execID).data("fromLine", retFromLine + fromLine);
        return message;
      } catch (final Exception e1) {
        logger.debug(
            "Failed to get log information for :{}(为 {} 获取日志失败)",
            job.get().getId(),
            job.get().getId(),
            e1);
        message = Message.error("Failed to get log information(获取日志信息失败)");
        message.setMethod("/api/entrance/" + id + "/log");
        message.data("log", "").data("execID", execID).data("fromLine", retFromLine + fromLine);
        return message;
      } finally {
        if (null != logReader && job.get().isCompleted()) {
          IOUtils.closeQuietly(logReader);
        }
      }
      message = Message.ok();
      message.setMethod("/api/entrance/" + id + "/log");
      message.data("log", retLog).data("execID", execID).data("fromLine", retFromLine + fromLine);
      logger.debug("success to get log for {} (获取 {} 日志成功)", job.get().getId(), job.get().getId());
    } else {
      message =
          Message.error(
              "Can't find execID(不能找到execID): "
                  + id
                  + "Corresponding job, can not get the corresponding log(对应的job，不能获得对应的日志)");
      message.setMethod("/api/entrance/" + id + "/log");
    }
    return message;
  }

  @ApiOperation(value = "killJobs", notes = "kill jobs", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "strongExecId", required = true, dataType = "String")
  })
  @ApiOperationSupport(ignoreParameters = {"jsonNode"})
  @Override
  @RequestMapping(path = "/{id}/killJobs", method = RequestMethod.POST)
  public Message killJobs(
      HttpServletRequest req,
      @RequestBody JsonNode jsonNode,
      @PathVariable("id") String strongExecId) {
    JsonNode idNode = jsonNode.get("idList");
    JsonNode taskIDNode = jsonNode.get("taskIDList");
    ArrayList<Long> waitToForceKill = new ArrayList<>();
    String userName = ModuleUserUtils.getOperationUser(req, "killJobs");
    if (idNode.size() != taskIDNode.size()) {
      return Message.error(
          "The length of the ID list does not match the length of the TASKID list(id列表的长度与taskId列表的长度不一致)");
    }
    if (!idNode.isArray() || !taskIDNode.isArray()) {
      return Message.error("Request parameter error, please use array(请求参数错误，请使用数组)");
    }
    ArrayList<Message> messages = new ArrayList<>();
    for (int i = 0; i < idNode.size(); i++) {
      String id = idNode.get(i).asText();
      Long taskID = taskIDNode.get(i).asLong();
      String realId = ZuulEntranceUtils.parseExecID(id)[3];
      Option<Job> job = null;
      try {
        job = entranceServer.getJob(realId);
      } catch (Exception e) {
        logger.warn("can not find a job in entranceServer, will force to kill it", e.getMessage());
        // 如果在内存中找不到该任务，那么该任务可能已经完成了，或者就是重启导致的
        waitToForceKill.add(taskID);
        Message message = Message.ok("Forced Kill task (强制杀死任务)");
        message.setMethod("/api/entrance/" + id + "/kill");
        message.setStatus(0);
        messages.add(message);
        continue;
      }
      Message message = null;
      if (job == null || job.isEmpty()) {
        logger.warn("can not find a job in entranceServer, will force to kill it");
        waitToForceKill.add(taskID);
        message = Message.ok("Forced Kill task (强制杀死任务)");
        message.setMethod("/api/entrance/" + id + "/killJobs");
        message.setStatus(0);
        messages.add(message);
      } else {
        try {
          logger.info("begin to kill job {} ", job.get().getId());

          if (job.get() instanceof EntranceJob) {
            EntranceJob entranceJob = (EntranceJob) job.get();
            JobRequest jobReq = entranceJob.getJobRequest();
            if (!userName.equals(jobReq.getExecuteUser())
                && Configuration.isNotJobHistoryAdmin(userName)) {
              return Message.error(
                  "You have no permission to kill this job, excecute by user:"
                      + jobReq.getExecuteUser());
            }
          }
          job.get().kill();
          message = Message.ok("Successfully killed the job(成功kill了job)");
          message.setMethod("/api/entrance/" + id + "/kill");
          message.setStatus(0);
          message.data("execID", id);
          // ensure the job's state is cancelled in database
          if (job.get() instanceof EntranceJob) {
            EntranceJob entranceJob = (EntranceJob) job.get();
            JobRequest jobReq = entranceJob.getJobRequest();
            entranceJob.updateJobRequestStatus(SchedulerEventState.Cancelled().toString());
            jobReq.setProgress("1.0f");
            LogListener logListener = entranceJob.getLogListener().getOrElse(null);
            if (null != logListener) {
              logListener.onLogUpdate(
                  entranceJob,
                  LogUtils.generateInfo(
                      "Job "
                          + jobReq.getId()
                          + " was kill by user successfully(任务"
                          + jobReq.getId()
                          + "已成功取消)"));
            }
            this.entranceServer
                .getEntranceContext()
                .getOrCreatePersistenceManager()
                .createPersistenceEngine()
                .updateIfNeeded(jobReq);
          }
          logger.info("end to kill job {} ", job.get().getId());
        } catch (Throwable t) {
          logger.error("kill job {} failed ", job.get().getId(), t);
          message =
              Message.error(
                  "An exception occurred while killing the job, kill failed(kill job的时候出现了异常，kill失败)",
                  t);
          message.setMethod("/api/entrance/" + id + "/kill");
        }
      }
      messages.add(message);
    }
    if (!waitToForceKill.isEmpty()) {
      JobHistoryHelper.forceBatchKill(waitToForceKill);
    }
    return Message.ok("success").data("messages", messages);
  }

  @ApiOperation(value = "kill", notes = "kill", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "exec id"),
    @ApiImplicitParam(name = "taskID", required = false, dataType = "String", value = "task id")
  })
  @Override
  @RequestMapping(path = "/{id}/kill", method = RequestMethod.GET)
  public Message kill(
      HttpServletRequest req,
      @PathVariable("id") String id,
      @RequestParam(value = "taskID", required = false) Long taskID) {
    String userName = ModuleUserUtils.getOperationUser(req, "kill job");
    Message message = null;
    String realId;
    String execID;
    if (id.startsWith(ZuulEntranceUtils.EXEC_ID())) {
      // execID
      realId = ZuulEntranceUtils.parseExecID(id)[3];
      execID = id;
    } else {
      // taskID
      JobInstance jobInstance;
      try {
        jobInstance = parseHeaderToJobInstance(req);
      } catch (JsonProcessingException e) {
        logger.error("parse JobInstance json error, id: {}", id);
        message = Message.error("parse JobInstance json error");
        message.setMethod("/api/entrance/" + id + "/kill");
        return message;
      }

      // return ok when job complete
      if (SchedulerEventState.isCompletedByStr(jobInstance.status())) {
        message = Message.error("The job already completed. Do not support kill.(任务已经结束，不支持kill)");
        message.setMethod("/api/entrance/" + id + "/kill");
        return message;
      } else if (jobInstance.instanceRegistryTimestamp() > jobInstance.createTimestamp()) {
        logger.warn("The job {} wait failover, but now force kill", id);
        // TODO If failover during force kill, the job status may change from Cancelled to Running
        long taskId = Long.parseLong(id);
        JobHistoryHelper.forceKill(taskId);
        message = Message.ok("Forced Kill task (强制杀死任务)");
        message.setMethod("/api/entrance/" + id + "/kill");
        message.data("execID", "").data("taskID", id);
        return message;
      } else {
        realId = jobInstance.jobReqId();
        execID =
            ZuulEntranceUtils.generateExecID(
                realId,
                Sender.getThisServiceInstance().getApplicationName(),
                new String[] {Sender.getThisInstance()});
      }
    }

    Option<Job> job = null;
    try {
      job = entranceServer.getJob(realId);
    } catch (Exception e) {
      logger.warn("can not find a job in entranceServer, will force to kill it", e);
      // 如果在内存中找不到该任务，那么该任务可能已经完成了，或者就是重启导致的
      if (taskID == null || taskID <= 0) {
        message = Message.error("Get job by ID error, kill failed.(获取job时发生异常，kill失败)");
        return message;
      }
      JobHistoryHelper.forceKill(taskID);
      message = Message.ok("Forced Kill task (强制杀死任务)");
      message.setMethod("/api/entrance/" + id + "/kill");
      message.setStatus(0);
      return message;
    }

    if (job == null || job.isEmpty()) {
      logger.warn("can not find a job in entranceServer, will force to kill it");
      // 如果在内存中找不到该任务，那么该任务可能已经完成了，或者就是重启导致的
      JobHistoryHelper.forceKill(taskID);
      message = Message.ok("Forced Kill task (强制杀死任务)");
      message.setMethod("/api/entrance/" + id + "/kill");
      message.setStatus(0);
      return message;
    } else {
      try {

        if (job.get() instanceof EntranceJob) {
          EntranceJob entranceJob = (EntranceJob) job.get();
          JobRequest jobReq = entranceJob.getJobRequest();
          if (!userName.equals(jobReq.getExecuteUser())
              && Configuration.isNotJobHistoryAdmin(userName)) {
            return Message.error(
                "You have no permission to kill this job, excecute by user:"
                    + jobReq.getExecuteUser());
          }
        }

        logger.info("begin to kill job {} ", job.get().getId());
        job.get().kill();
        message = Message.ok("Successfully killed the job(成功kill了job)");
        message.setMethod("/api/entrance/" + id + "/kill");
        message.data("execID", execID);
        // ensure the job's state is cancelled in database
        if (job.get() instanceof EntranceJob) {
          EntranceJob entranceJob = (EntranceJob) job.get();
          JobRequest jobReq = entranceJob.getJobRequest();
          entranceJob.updateJobRequestStatus(SchedulerEventState.Cancelled().toString());
          this.entranceServer
              .getEntranceContext()
              .getOrCreatePersistenceManager()
              .createPersistenceEngine()
              .updateIfNeeded(jobReq);
        }
        logger.info("end to kill job {} ", job.get().getId());
      } catch (Throwable t) {
        logger.error("kill job {} failed ", job.get().getId(), t);
        message =
            Message.error(
                "An exception occurred while killing the job, kill failed(kill job的时候出现了异常，kill失败) with error:"
                    + t.getMessage(),
                t);
        message.setMethod("/api/entrance/" + id + "/kill");
        message.setStatus(1);
      }
    }
    return message;
  }

  @ApiOperation(value = "pause ", notes = "puase a task job", response = Message.class)
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "excete id")
  })
  @Override
  @RequestMapping(path = "/{id}/pause", method = RequestMethod.GET)
  public Message pause(HttpServletRequest req, @PathVariable("id") String id) {
    String realId = ZuulEntranceUtils.parseExecID(id)[3];
    ModuleUserUtils.getOperationUser(req, "pause realId:" + realId);
    Option<Job> job = entranceServer.getJob(realId);
    Message message = null;
    if (job.isEmpty()) {
      message =
          Message.error(
              "can not find the job of exexID :"
                  + id
                  + " can not pause (不能找到execID: "
                  + id
                  + "对应的job，不能进行pause)");
      message.setMethod("/api/entrance/" + id + "/pause");
      message.setStatus(1);
    } else {
      try {
        // todo job pause 接口还未实现和给出
        // job.pause();
        logger.info("begin to pause job {} ", job.get().getId());
        message = Message.ok("success to pause job (成功pause了job)");
        message.setStatus(0);
        message.data("execID", id);
        message.setMethod("/api/entrance/" + id + "/pause");
        logger.info("end to pause job {} ", job.get().getId());
      } catch (Throwable t) {
        logger.info("pause job {} failed ", job.get().getId());
        message =
            Message.error("Abnormal when pausing job, pause failed(pause job的时候出现了异常，pause失败)");
        message.setMethod("/api/entrance/" + id + "/pause");
        message.setStatus(1);
      }
    }
    return message;
  }
}
