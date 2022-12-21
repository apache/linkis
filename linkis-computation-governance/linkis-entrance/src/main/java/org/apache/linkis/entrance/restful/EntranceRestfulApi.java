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
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.protocol.utils.ZuulEntranceUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.listener.LogListener;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.scheduler.queue.SchedulerEventState;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.server.utils.ModuleUserUtils;

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
    logger.info("Begin to get an execID");
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
    pushLog(
        LogUtils.generateInfo(
            "You have submitted a new job, script code (after variable substitution) is"),
        job);
    pushLog(
        "************************************SCRIPT CODE************************************", job);
    pushLog(jobReq.getExecutionCode(), job);
    pushLog(
        "************************************SCRIPT CODE************************************", job);
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
                + ". Please wait it to be scheduled"),
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
    logger.info("Begin to get an execID");
    json.put(TaskConstant.EXECUTE_USER, ModuleUserUtils.getOperationUser(req));
    json.put(TaskConstant.SUBMIT_USER, SecurityFilter.getLoginUsername(req));
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
            "You have submitted a new job, script code (after variable substitution) is"),
        job);
    pushLog(
        "************************************SCRIPT CODE************************************", job);
    pushLog(jobRequest.getExecutionCode(), job);
    pushLog(
        "************************************SCRIPT CODE************************************", job);
    pushLog(
        LogUtils.generateInfo(
            "Your job is accepted,  jobID is "
                + job.getId()
                + " and jobReqId is "
                + jobReqId
                + " in "
                + Sender.getThisServiceInstance().toString()
                + ". Please wait it to be scheduled"),
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
    Message message = null;
    String realId = ZuulEntranceUtils.parseExecID(id)[3];
    ModuleUserUtils.getOperationUser(req, "status realId: " + realId);
    Option<Job> job = Option.apply(null);
    try {
      job = entranceServer.getJob(realId);
    } catch (Exception e) {
      logger.warn("获取任务 {} 状态时出现错误", realId, e.getMessage());
      long realTaskID = Long.parseLong(taskID);
      String status = JobHistoryHelper.getStatusByTaskID(realTaskID);
      message = Message.ok();
      message.setMethod("/api/entrance/" + id + "/status");
      message.data("status", status).data("execID", id);
      return message;
    }
    if (job.isDefined()) {
      if (job.get() instanceof EntranceJob) {
        ((EntranceJob) job.get()).updateNewestAccessByClientTimestamp();
      }
      message = Message.ok();
      message.setMethod("/api/entrance/" + id + "/status");
      message.data("status", job.get().getState().toString()).data("execID", id);
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
    Message message = null;
    String realId = ZuulEntranceUtils.parseExecID(id)[3];
    ModuleUserUtils.getOperationUser(req, "progress realId: " + realId);
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
            .data("execID", id)
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
    Message message = null;
    String realId = ZuulEntranceUtils.parseExecID(id)[3];
    ModuleUserUtils.getOperationUser(req, "progressWithResource realId: " + realId);
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
        Map<String, Object> metrics = jobRequest.getMetrics();
        Map<String, Object> metricsVo = new HashMap<>();
        if (metrics.containsKey(TaskConstant.ENTRANCEJOB_YARNRESOURCE)) {
          HashMap<String, ResourceWithStatus> resourceMap =
              (HashMap<String, ResourceWithStatus>)
                  metrics.get(TaskConstant.ENTRANCEJOB_YARNRESOURCE);
          ArrayList<YarnResourceWithStatusVo> resoureList = new ArrayList<>(12);
          if (null != resourceMap && !resourceMap.isEmpty()) {
            resourceMap.forEach(
                (applicationId, resource) -> {
                  resoureList.add(new YarnResourceWithStatusVo(applicationId, resource));
                });
            metricsVo.put(TaskConstant.ENTRANCEJOB_YARNRESOURCE, resoureList);
            Optional<Integer> cores =
                resourceMap.values().stream()
                    .map(resource -> resource.queueCores())
                    .reduce((x, y) -> x + y);
            Optional<Long> memory =
                resourceMap.values().stream()
                    .map(resource -> resource.queueMemory())
                    .reduce((x, y) -> x + y);
            float corePercent = 0.0f;
            float memoryPercent = 0.0f;
            if (cores.isPresent() && memory.isPresent()) {
              corePercent =
                  cores.get().floatValue()
                      / EntranceConfiguration.YARN_QUEUE_CORES_MAX().getHotValue();
              memoryPercent =
                  memory.get().floatValue()
                      / (EntranceConfiguration.YARN_QUEUE_MEMORY_MAX().getHotValue().longValue()
                          * 1024
                          * 1024
                          * 1024);
            }
            String coreRGB = RGBUtils.getRGB(corePercent);
            String memoryRGB = RGBUtils.getRGB(memoryPercent);
            metricsVo.put(TaskConstant.ENTRANCEJOB_CORE_PERCENT, corePercent);
            metricsVo.put(TaskConstant.ENTRANCEJOB_MEMORY_PERCENT, memoryPercent);
            metricsVo.put(TaskConstant.ENTRANCEJOB_CORE_RGB, coreRGB);
            metricsVo.put(TaskConstant.ENTRANCEJOB_MEMORY_RGB, memoryRGB);

            message.data(TaskConstant.ENTRANCEJOB_YARN_METRICS, metricsVo);
          } else {
            message.data(TaskConstant.ENTRANCEJOB_YARNRESOURCE, null);
          }
        } else {
          message.data(TaskConstant.ENTRANCEJOB_YARNRESOURCE, null);
        }

        message
            .data("progress", Math.abs(job.get().getProgress()))
            .data("execID", id)
            .data("progressInfo", list);
      }
    } else {
      message =
          Message.error(
              "The job corresponding to the ID is empty, and the corresponding task progress cannot be obtained.(ID 对应的job为空，不能获取相应的任务进度)");
    }
    return message;
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
    String realId = ZuulEntranceUtils.parseExecID(id)[3];
    ModuleUserUtils.getOperationUser(req, "log realId: " + realId);
    Option<Job> job = Option.apply(null);
    Message message = null;
    try {
      job = entranceServer.getJob(realId);
    } catch (final Throwable t) {
      message =
          Message.error(
              "The job you just executed has ended. This interface no longer provides a query. It is recommended that you download the log file for viewing.(您刚刚执行的job已经结束，本接口不再提供查询，建议您下载日志文件进行查看)");
      message.setMethod("/api/entrance/" + id + "/log");
      return message;
    }
    if (job.isDefined()) {
      logger.debug("begin to get log for {}(开始获取 {} 的日志)", job.get().getId(), job.get().getId());
      LogReader logReader =
          entranceServer.getEntranceContext().getOrCreateLogManager().getLogReader(realId);
      int fromLine = 0;
      int size = 100;
      boolean distinctLevel = true;
      if (req != null) {
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
      }

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
        message.data("log", "").data("execID", id).data("fromLine", retFromLine + fromLine);
      } catch (final IllegalArgumentException e) {
        logger.debug(
            "Failed to get log information for :{}(为 {} 获取日志失败)",
            job.get().getId(),
            job.get().getId(),
            e);
        message = Message.ok();
        message.setMethod("/api/entrance/" + id + "/log");
        message.data("log", "").data("execID", id).data("fromLine", retFromLine + fromLine);
        return message;
      } catch (final Exception e1) {
        logger.debug(
            "Failed to get log information for :{}(为 {} 获取日志失败)",
            job.get().getId(),
            job.get().getId(),
            e1);
        message = Message.error("Failed to get log information(获取日志信息失败)");
        message.setMethod("/api/entrance/" + id + "/log");
        message.data("log", "").data("execID", id).data("fromLine", retFromLine + fromLine);
        return message;
      } finally {
        if (null != logReader && job.get().isCompleted()) {
          IOUtils.closeQuietly(logReader);
        }
      }
      message = Message.ok();
      message.setMethod("/api/entrance/" + id + "/log");
      message.data("log", retLog).data("execID", id).data("fromLine", retFromLine + fromLine);
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
    ModuleUserUtils.getOperationUser(req, "killJobs");
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
      Option<Job> job = Option.apply(null);
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
      if (job.isEmpty()) {
        logger.warn("can not find a job in entranceServer, will force to kill it");
        waitToForceKill.add(taskID);
        message = Message.ok("Forced Kill task (强制杀死任务)");
        message.setMethod("/api/entrance/" + id + "/killJobs");
        message.setStatus(0);
        messages.add(message);
      } else {
        try {
          logger.info("begin to kill job {} ", job.get().getId());
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
                  "Job "
                      + jobReq.getId()
                      + " was kill by user successfully(任务"
                      + jobReq.getId()
                      + "已成功取消)");
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
                  "An exception occurred while killing the job, kill failed(kill job的时候出现了异常，kill失败)");
          message.setMethod("/api/entrance/" + id + "/kill");
          message.setStatus(1);
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
    @ApiImplicitParam(name = "id", required = true, dataType = "String", value = "excute id"),
    @ApiImplicitParam(name = "taskID", required = false, dataType = "String", value = "task id")
  })
  @Override
  @RequestMapping(path = "/{id}/kill", method = RequestMethod.GET)
  public Message kill(
      HttpServletRequest req,
      @PathVariable("id") String id,
      @RequestParam(value = "taskID", required = false) Long taskID) {
    String realId = ZuulEntranceUtils.parseExecID(id)[3];
    ModuleUserUtils.getOperationUser(req, "kill realId:" + realId);
    Option<Job> job = Option.apply(null);
    try {
      job = entranceServer.getJob(realId);
    } catch (Exception e) {
      logger.warn("can not find a job in entranceServer, will force to kill it", e);
      // 如果在内存中找不到该任务，那么该任务可能已经完成了，或者就是重启导致的
      JobHistoryHelper.forceKill(taskID);
      Message message = Message.ok("Forced Kill task (强制杀死任务)");
      message.setMethod("/api/entrance/" + id + "/kill");
      message.setStatus(0);
      return message;
    }
    Message message = null;
    if (job.isEmpty()) {
      logger.warn("can not find a job in entranceServer, will force to kill it");
      // 如果在内存中找不到该任务，那么该任务可能已经完成了，或者就是重启导致的
      JobHistoryHelper.forceKill(taskID);
      message = Message.ok("Forced Kill task (强制杀死任务)");
      message.setMethod("/api/entrance/" + id + "/kill");
      message.setStatus(0);
      return message;
    } else {
      try {
        logger.info("begin to kill job {} ", job.get().getId());
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
                "An exception occurred while killing the job, kill failed(kill job的时候出现了异常，kill失败)");
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
