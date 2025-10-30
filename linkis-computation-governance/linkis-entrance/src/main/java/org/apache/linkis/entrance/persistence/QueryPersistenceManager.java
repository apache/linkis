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

package org.apache.linkis.entrance.persistence;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.utils.LinkisUtils;
import org.apache.linkis.entrance.EntranceContext;
import org.apache.linkis.entrance.cli.heartbeat.CliHeartbeatMonitor;
import org.apache.linkis.entrance.conf.EntranceConfiguration;
import org.apache.linkis.entrance.cs.CSEntranceHelper;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.log.FlexibleErrorCodeManager;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.protocol.utils.TaskUtils;
import org.apache.linkis.scheduler.executer.OutputExecuteResponse;
import org.apache.linkis.scheduler.queue.Job;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scala.Option;
import scala.Tuple2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary.EXEC_FAILED_TO_RETRY;

public class QueryPersistenceManager extends PersistenceManager {
  private static final Logger logger = LoggerFactory.getLogger(QueryPersistenceManager.class);

  private EntranceContext entranceContext;
  private PersistenceEngine persistenceEngine;
  private ResultSetEngine resultSetEngine;

  private CliHeartbeatMonitor cliHeartbeatMonitor;

  public CliHeartbeatMonitor getCliHeartbeatMonitor() {
    return cliHeartbeatMonitor;
  }

  public void setCliHeartbeatMonitor(CliHeartbeatMonitor cliHeartbeatMonitor) {
    this.cliHeartbeatMonitor = cliHeartbeatMonitor;
  }

  public void setPersistenceEngine(PersistenceEngine persistenceEngine) {
    this.persistenceEngine = persistenceEngine;
  }

  public void setResultSetEngine(ResultSetEngine resultSetEngine) {
    this.resultSetEngine = resultSetEngine;
  }

  @Override
  public EntranceContext getEntranceContext() {
    return entranceContext;
  }

  @Override
  public void setEntranceContext(EntranceContext entranceContext) {
    this.entranceContext = entranceContext;
  }

  @Override
  public PersistenceEngine createPersistenceEngine() {
    return this.persistenceEngine;
  }

  @Override
  public ResultSetEngine createResultSetEngine() {
    return resultSetEngine;
  }

  @Override
  public void onResultSetCreated(Job job, OutputExecuteResponse response) {}

  @Override
  public void onProgressUpdate(Job job, float progress, JobProgressInfo[] progressInfo) {
    float updatedProgress = progress;
    if (updatedProgress < 0) {
      logger.error(
          "Got negitive progress : "
              + progress
              + ", job : "
              + ((EntranceJob) job).getJobRequest().getId());
      updatedProgress = -1 * progress;
    }
    if (Double.isNaN(updatedProgress)) {
      return;
    }
    EntranceJob entranceJob = (EntranceJob) job;
    float persistedProgress = 0.0f;
    try {
      persistedProgress = Float.parseFloat(entranceJob.getJobRequest().getProgress());
    } catch (Exception e) {
      logger.warn("Invalid progress : " + entranceJob.getJobRequest().getProgress(), e);
    }
    boolean notUpdate = false;
    if (job.getProgress() >= 0
        && persistedProgress >= updatedProgress
        && entranceJob.getUpdateMetrisFlag()) {
      notUpdate = true;
      if (EntranceConfiguration.TASK_RETRY_ENABLED() && updatedProgress == 0) { // NOSONAR
        notUpdate = false;
      }
    }
    if (notUpdate) {
      return;
    }
    if (updatedProgress > 1) {
      logger.debug("Progress {} beyond 1 will not be updated", updatedProgress);
      return;
    }
    job.setProgress(updatedProgress);
    entranceJob.setUpdateMetrisFlag(true);
    entranceJob.getJobRequest().setProgress(String.valueOf(updatedProgress));
    updateJobStatus(job);
  }

  @Override
  public boolean onJobFailed(
      Job job, String code, Map<String, Object> props, int errorCode, String errorDesc) {
    if (!EntranceConfiguration.TASK_RETRY_ENABLED()) {
      return false;
    }

    if (!(job instanceof EntranceJob)) {
      return false;
    }

    boolean containsAny = false;
    String errorDescArray = EntranceConfiguration.SUPPORTED_RETRY_ERROR_DESC();
    String errorCodeArray = EntranceConfiguration.SUPPORTED_RETRY_ERROR_CODES();
    for (String keyword : errorDescArray.split(",")) {
      if (errorDesc.contains(keyword.trim()) || errorCodeArray.contains(errorCode + "")) {
        containsAny = true;
        break;
      }
    }

    if (!containsAny) {
      return false;
    }

    AtomicBoolean canRetry = new AtomicBoolean(false);
    String aiSqlKey = EntranceConfiguration.AI_SQL_KEY().key();
    String retryNumKey = EntranceConfiguration.RETRY_NUM_KEY().key();

    final EntranceJob entranceJob = (EntranceJob) job;

    // 处理广播表
    String dataFrameKey = EntranceConfiguration.SUPPORT_ADD_RETRY_CODE_KEYS();
    if (containsAny(errorDesc, dataFrameKey)) {
      entranceJob
          .getJobRequest()
          .setExecutionCode("set spark.sql.autoBroadcastJoinThreshold=-1; " + code);
    }

    Map<String, Object> startupMap = TaskUtils.getStartupMap(props);
    // 只对 aiSql 做重试
    if ("true".equals(startupMap.get(aiSqlKey))) {
      LinkisUtils.tryAndWarn(
          () -> {
            int retryNum = (int) startupMap.getOrDefault(retryNumKey, 1);
            boolean canRetryCode = canRetryCode(code);
            if (retryNum > 0 && canRetryCode) {
              logger.info(
                  "mark task: {} status to WaitForRetry, current retryNum: {}, for errorCode: {}, errorDesc: {}",
                  entranceJob.getJobInfo().getId(),
                  retryNum,
                  errorCode,
                  errorDesc);
              // 重试
              job.transitionWaitForRetry();

              // 修改错误码和错误描述
              entranceJob.getJobRequest().setErrorCode(EXEC_FAILED_TO_RETRY.getErrorCode());
              entranceJob.getJobRequest().setErrorDesc(EXEC_FAILED_TO_RETRY.getErrorDesc());
              canRetry.set(true);
              startupMap.put(retryNumKey, retryNum - 1);
              // once 引擎
              if ((boolean) EntranceConfiguration.AI_SQL_RETRY_ONCE().getValue()) {
                // once 引擎
                ExecuteOnceLabel onceLabel =
                    LabelBuilderFactoryContext.getLabelBuilderFactory()
                        .createLabel(ExecuteOnceLabel.class);
                List<Label<?>> labels = entranceJob.getJobRequest().getLabels();
                labels.add(onceLabel);
                logger.info("aisql retry add once label for task id:{}", job.getJobInfo().getId());
                startupMap.put("executeOnce", true);
              }
              TaskUtils.addStartupMap(props, startupMap);
              logger.info("task {} set retry status success.", entranceJob.getJobInfo().getId());
            } else {
              logger.info("task {} not support retry.", entranceJob.getJobInfo().getId());
            }
          },
          logger);
    }
    return canRetry.get();
  }

  private boolean canRetryCode(String code) {
    String exceptCode = EntranceConfiguration.UNSUPPORTED_RETRY_CODES();
    String[] keywords = exceptCode.split(",");
    for (String keyword : keywords) {
      // 使用空格分割关键字，并移除空字符串
      String[] parts = keyword.trim().split("\\s+");
      StringBuilder regexBuilder = new StringBuilder("\\s*");
      for (String part : parts) {
        regexBuilder.append(part);
        regexBuilder.append("\\s*");
      }
      if (keyword.startsWith("CREATE")) {
        regexBuilder.delete(regexBuilder.length() - 3, regexBuilder.length());
        regexBuilder.append("\\b(?!\\s+IF\\s+NOT\\s+EXISTS)");
      }
      if (keyword.startsWith("DROP")) {
        regexBuilder.delete(regexBuilder.length() - 3, regexBuilder.length());
        regexBuilder.append("\\b(?!\\s+IF\\s+EXISTS)");
      }

      String regex = regexBuilder.toString();
      Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(code);
      if (matcher.find()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onJobScheduled(Job job) {
    updateJobStatus(job);
  }

  @Override
  public void onJobInited(Job job) {
    cliHeartbeatMonitor.registerIfCliJob(job);
  }

  @Override
  public void onJobRunning(Job job) {
    updateJobStatus(job);
  }

  @Override
  public void onJobWaitForRetry(Job job) {
    updateJobStatus(job);
  }

  @Override
  public void onJobCompleted(Job job) {
    // to set jobID to CS
    try {
      if (job.isSucceed()) {
        CSEntranceHelper.registerCSRSData(job);
      } else {
        JobRequest jobRequest =
            this.entranceContext.getOrCreateEntranceParser().parseToJobRequest(job);
        if (null == jobRequest.getErrorCode() || jobRequest.getErrorCode() == 0) {
          Option<Tuple2<String, String>> tuple2Option =
              FlexibleErrorCodeManager.errorMatch(jobRequest.getErrorDesc());
          if (tuple2Option.isDefined()) {
            logger.info(jobRequest.getId() + " to reset errorCode by errorMsg");
            Tuple2<String, String> errorCodeContent = tuple2Option.get();
            jobRequest.setErrorCode(Integer.parseInt(errorCodeContent._1));
            jobRequest.setErrorDesc(errorCodeContent._2);
          }
        }
      }
    } catch (Throwable e) {
      logger.error("Failed to register cs rs data ", e);
    }
    cliHeartbeatMonitor.unRegisterIfCliJob(job);
    updateJobStatus(job);
    job.clear();
  }

  private void updateJobStatus(Job job) {
    JobRequest jobRequest = null;
    if (job.isCompleted()) {
      job.setProgress(1);
    } else if (job.getProgress() >= 1 && job instanceof EntranceJob) {
      job.setProgress(GovernanceCommonConf.FAKE_PROGRESS());
      ((EntranceJob) job)
          .getJobRequest()
          .setProgress(String.valueOf(GovernanceCommonConf.FAKE_PROGRESS()));
    }
    try {
      jobRequest = this.entranceContext.getOrCreateEntranceParser().parseToJobRequest(job);
      if (job.isSucceed()) {
        jobRequest.setErrorCode(0);
        jobRequest.setErrorDesc("");
      }
    } catch (Exception e) {
      entranceContext.getOrCreateLogManager().onLogUpdate(job, e.getMessage());
      logger.error("update job status failed, reason:", e);
    }
    try {
      createPersistenceEngine().updateIfNeeded(jobRequest);
    } catch (ErrorException e) {
      entranceContext.getOrCreateLogManager().onLogUpdate(job, e.getMessage());
      throw e;
    }
  }

  @Override
  public void onResultSizeCreated(Job job, int resultSize) {}

  private static boolean containsAny(String src, String target) {
    if (StringUtils.isBlank(target)) {
      return false;
    }
    return containsAny(src, target.split(","));
  }

  private static boolean containsAny(String src, String[] target) {
    if (target == null || StringUtils.isBlank(src)) {
      return false;
    }
    for (String item : target) {
      if (src.toLowerCase().contains(item.toLowerCase())) {
        return true;
      }
    }
    return false;
  }
}
