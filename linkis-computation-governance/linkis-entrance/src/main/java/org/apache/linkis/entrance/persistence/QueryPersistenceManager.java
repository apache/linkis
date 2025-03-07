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
import org.apache.linkis.entrance.cs.CSEntranceHelper;
import org.apache.linkis.entrance.execute.EntranceJob;
import org.apache.linkis.entrance.log.FlexibleErrorCodeManager;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.entity.job.JobRequest;
import org.apache.linkis.protocol.engine.JobProgressInfo;
import org.apache.linkis.scheduler.executer.OutputExecuteResponse;
import org.apache.linkis.scheduler.queue.Job;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scala.Option;
import scala.Tuple2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    if (job.getProgress() >= 0
        && persistedProgress >= updatedProgress
        && entranceJob.getUpdateMetrisFlag()) {
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
    AtomicBoolean canRetry = new AtomicBoolean(false);
    // TODO 改成配置
    String aiSqlKey = "linkis.ai.sql";
    String retryNumKey = "linkis.ai.retry.num";
    String errorCodeArray = "01002,01003,13005,13006,13012";

    // 只对 aiSql 做重试
    if (props != null && "true".equals(props.get(aiSqlKey))) {
      LinkisUtils.tryAndWarn(
          () -> {
            int retryNum = (int) props.getOrDefault(retryNumKey, 0);
            boolean canRetryCode = canRetryCode(code);
            if (retryNum > 0
                && errorCodeArray.contains(String.valueOf(errorCode))
                && canRetryCode) {
              logger.info(
                  "mark task: {} status to WaitForRetry, current retryNum: {}, for errorCode: {}, errorDesc: {}",
                  job.getId(),
                  retryNum,
                  errorCode,
                  errorDesc);
              job.transitionWaitForRetry();
              canRetry.set(true);
              props.put(retryNumKey, retryNum - 1);
            }
          },
          logger);
    }
    return canRetry.get();
  }

  private boolean canRetryCode(String code) {
    String exceptCode =
        "INSERT INTO, INSERT OVERWRITE TABLE, CREATE TABLE, ALTER TABLE, CREATE TEMPORARY";
    String[] keywords = exceptCode.split(",");
    for (String keyword : keywords) {
      // 使用空格分割关键字，并移除空字符串
      String[] parts = keyword.trim().split("\\s+");
      StringBuilder regexBuilder = new StringBuilder("\\s*");
      for (String part : parts) {
        regexBuilder.append(part);
        regexBuilder.append("\\s*");
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
}
