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

package org.apache.linkis.entrance.job;

import org.apache.linkis.common.log.LogUtils;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.entrance.exception.EntranceErrorException;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.log.LogHandler;
import org.apache.linkis.entrance.log.LogReader;
import org.apache.linkis.entrance.log.LogWriter;
import org.apache.linkis.entrance.log.WebSocketCacheLogReader;
import org.apache.linkis.entrance.log.WebSocketLogWriter;
import org.apache.linkis.entrance.persistence.PersistenceManager;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.constant.job.JobRequestConstants;
import org.apache.linkis.governance.common.protocol.task.RequestTask$;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.orchestrator.plans.ast.QueryParams$;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.protocol.utils.TaskUtils;
import org.apache.linkis.scheduler.executer.ExecuteRequest;
import org.apache.linkis.scheduler.queue.JobInfo;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntranceExecutionJob extends EntranceJob implements LogHandler {

  private static final Logger logger = LoggerFactory.getLogger(EntranceExecutionJob.class);

  private static final ThreadLocal<SimpleDateFormat> dateFormatLocal =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  private LogReader logReader;
  private LogWriter logWriter;
  private Object logWriterLocker = new Object();
  private WebSocketCacheLogReader webSocketCacheLogReader;
  private WebSocketLogWriter webSocketLogWriter;

  private PersistenceManager persistenceManager;

  public EntranceExecutionJob(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  public Object getLogWriterLocker() {
    return logWriterLocker;
  }

  @Override
  public void setLogReader(LogReader logReader) {
    this.logReader = logReader;
  }

  @Override
  public Option<LogReader> getLogReader() {
    return Option.apply(logReader);
  }

  @Override
  public void setLogWriter(LogWriter logWriter) {
    this.logWriter = logWriter;
  }

  @Override
  public Option<LogWriter> getLogWriter() {
    return Option.apply(logWriter);
  }

  @Override
  public Option<WebSocketCacheLogReader> getWebSocketLogReader() {
    return Option.apply(webSocketCacheLogReader);
  }

  @Override
  public void setWebSocketLogReader(WebSocketCacheLogReader webSocketCacheLogReader) {
    this.webSocketCacheLogReader = webSocketCacheLogReader;
  }

  @Override
  public void setWebSocketLogWriter(WebSocketLogWriter webSocketLogWriter) {
    this.webSocketLogWriter = webSocketLogWriter;
  }

  @Override
  public Option<WebSocketLogWriter> getWebSocketLogWriter() {
    return Option.apply(this.webSocketLogWriter);
  }

  @Override
  public void init() throws EntranceErrorException {
    updateNewestAccessByClientTimestamp();
  }

  @Override
  public ExecuteRequest jobToExecuteRequest() throws EntranceErrorException {
    // add resultSet path root
    Map<String, String> starupMapTmp = new HashMap<>();
    Map<String, Object> starupMapOri = TaskUtils.getStartupMap(getParams());
    if (starupMapOri.isEmpty()) {
      TaskUtils.addStartupMap(getParams(), starupMapOri);
    }
    if (!starupMapOri.containsKey(JobRequestConstants.JOB_REQUEST_LIST())) {
      starupMapOri.put(JobRequestConstants.JOB_ID(), String.valueOf(getJobRequest().getId()));
    }
    for (Map.Entry<String, Object> entry : starupMapOri.entrySet()) {
      if (null != entry.getKey() && null != entry.getValue()) {
        starupMapTmp.put(entry.getKey(), entry.getValue().toString());
      }
    }
    Map<String, Object> runtimeMapOri = TaskUtils.getRuntimeMap(getParams());
    if (null == runtimeMapOri || runtimeMapOri.isEmpty()) {
      TaskUtils.addRuntimeMap(getParams(), new HashMap<>());
      runtimeMapOri = TaskUtils.getRuntimeMap(getParams());
    }
    if (!runtimeMapOri.containsKey(JobRequestConstants.JOB_ID())) {
      runtimeMapOri.put(JobRequestConstants.JOB_ID(), String.valueOf(getJobRequest().getId()));
    }
    Map<String, String> runtimeMapTmp = new HashMap<>();
    for (Map.Entry<String, Object> entry : runtimeMapOri.entrySet()) {
      if (null != entry.getKey() && null != entry.getValue()) {
        runtimeMapTmp.put(entry.getKey(), entry.getValue().toString());
      }
    }
    String resultSetPathRoot = GovernanceCommonConf.RESULT_SET_STORE_PATH().getValue(runtimeMapTmp);
    Map<String, Object> jobMap = new HashMap<String, Object>();
    jobMap.put(RequestTask$.MODULE$.RESULT_SET_STORE_PATH(), resultSetPathRoot);
    runtimeMapOri.put(QueryParams$.MODULE$.JOB_KEY(), jobMap);

    EntranceExecuteRequest executeRequest = new EntranceExecuteRequest(this);
    List<Label<?>> labels = new ArrayList<Label<?>>(getJobRequest().getLabels());
    executeRequest.setLabels(labels);
    return executeRequest;
  }

  @Override
  public String getName() {
    return "jobGroupId:" + String.valueOf(getJobRequest().getId()) + " execID:" + getId();
  }

  @Override
  public String getId() {
    return super.getId();
  }

  @Override
  public JobInfo getJobInfo() {
    String execID = getId();
    String state = this.getState().toString();
    float progress = this.getProgress();
    SimpleDateFormat simpleDateFormat = dateFormatLocal.get();

    if (getJobRequest().getMetrics() == null) {
      getJobRequest().setMetrics(new HashMap<>());
    }

    Map<String, Object> metricsMap = getJobRequest().getMetrics();
    String createTime =
        metricsMap.containsKey(TaskConstant.JOB_SUBMIT_TIME)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_SUBMIT_TIME))
            : "not created";
    String scheduleTime =
        metricsMap.containsKey(TaskConstant.JOB_SCHEDULE_TIME)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_SCHEDULE_TIME))
            : "not scheduled";
    String startTime =
        metricsMap.containsKey(TaskConstant.JOB_RUNNING_TIME)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_RUNNING_TIME))
            : "not submitted to orchestrator";
    String endTime =
        metricsMap.containsKey(TaskConstant.JOB_COMPLETE_TIME)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_COMPLETE_TIME))
            : "on running or not started";
    String runTime;
    if (metricsMap.containsKey(TaskConstant.JOB_COMPLETE_TIME)) {
      runTime =
          ByteTimeUtils.msDurationToString(
              (((Date) metricsMap.get(TaskConstant.JOB_COMPLETE_TIME))).getTime()
                  - (((Date) metricsMap.get(TaskConstant.JOB_SUBMIT_TIME))).getTime());
    } else {
      runTime =
          "The task did not end normally and the usage time could not be counted.(任务并未正常结束，无法统计使用时间)";
    }

    String jobToOrchestrator =
        metricsMap.containsKey(TaskConstant.JOB_TO_ORCHESTRATOR)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_TO_ORCHESTRATOR))
            : "not to orchestrator";
    String jobRequestEC =
        metricsMap.containsKey(TaskConstant.JOB_REQUEST_EC_TIME)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_REQUEST_EC_TIME))
            : "not request ec";
    String jobSubmitToEC =
        metricsMap.containsKey(TaskConstant.JOB_SUBMIT_TO_EC_TIME)
            ? simpleDateFormat.format(metricsMap.get(TaskConstant.JOB_SUBMIT_TO_EC_TIME))
            : "not submit to  ec";

    StringBuffer sb = new StringBuffer();
    sb.append("Task creation time(任务创建时间): ")
        .append(createTime)
        .append(", Task scheduling time(任务调度时间): ")
        .append(scheduleTime)
        .append(", Task start time(任务开始时间): ")
        .append(startTime)
        .append(", Mission end time(任务结束时间): ")
        .append(endTime)
        .append("\n")
        .append(LogUtils.generateInfo(""))
        .append("Task submit to Orchestrator time:")
        .append(jobToOrchestrator)
        .append(", Task request EngineConn time:")
        .append(jobRequestEC)
        .append(", Task submit to EngineConn time:")
        .append(jobSubmitToEC)
        .append("\n")
        .append(
            LogUtils.generateInfo(
                "Your mission(您的任务) "
                    + this.getJobRequest().getId()
                    + " The total time spent is(总耗时时间为): "
                    + runTime));

    String metric = sb.toString();

    return new JobInfo(execID, null, state, progress, metric);
  }

  @Override
  public void close() throws IOException {
    logger.info("job:" + jobRequest().getId() + " is closing");
    try {
      if (this.getLogWriter().isDefined()) {
        IOUtils.closeQuietly(this.getLogWriter().get());
      } else {
        logger.info("job:" + jobRequest().getId() + "LogWriter is null");
      }
      if (this.getLogReader().isDefined()) {
        IOUtils.closeQuietly(getLogReader().get());
      }
    } catch (Exception e) {
      logger.warn("Close logWriter and logReader failed. {}", e.getMessage(), e);
    }
  }
}
