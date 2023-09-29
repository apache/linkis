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

package org.apache.linkis.cli.application.interactor.job;

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.interactor.job.data.LinkisJobData;
import org.apache.linkis.cli.application.interactor.job.data.LinkisLogData;
import org.apache.linkis.cli.application.interactor.job.data.LinkisOnceJobData;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisOnceDesc;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.job.JobDescription;
import org.apache.linkis.cli.common.entity.operator.JobOperator;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.constants.CommonConstants;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.job.*;
import org.apache.linkis.cli.core.utils.CommonUtils;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.linkis.cli.core.utils.SchedulerUtils;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisOnceJob extends LinkisJob
    implements ManagableBackendJob,
        LogAccessibleJob,
        ResultAccessibleJob,
        AsyncBackendJob,
        TerminatableJob {

  private static final Logger logger = LoggerFactory.getLogger(LinkisOnceJob.class);

  private LinkisOnceDesc jobDesc;
  private LinkisOnceJobData data;
  private TerminateToken terminateToken = new TerminateToken();
  private Boolean isAsync = false;

  public void setAsync(Boolean async) {
    isAsync = async;
  }

  public Boolean isAsync() {
    return isAsync;
  }

  @Override
  public JobDescription getJobDesc() {
    return jobDesc;
  }

  public void setJobDesc(LinkisOnceDesc desc) {
    this.jobDesc = desc;
  }

  @Override
  public TerminateToken getTerminateToken() {
    return terminateToken;
  }

  @Override
  public LinkisJobData getJobData() {
    return data;
  }

  public void setJobData(LinkisOnceJobData data) {
    this.data = data;
  }

  @Override
  public JobOperator getJobOperator() {
    return null;
  }

  /** AsyncBackendJob */
  @Override
  public void submit() throws LinkisClientRuntimeException {
    StringBuilder infoBuilder = new StringBuilder();
    infoBuilder
        .append("connecting to linkis gateway:")
        .append(data.getOnceJobAdapter().getServerUrl());
    LogUtils.getInformationLogger().info(infoBuilder.toString());
    data.getOnceJobAdapter().submit();
    data.getOnceJobAdapter().updateStatus();
    infoBuilder.setLength(0);
    infoBuilder
        .append("JobId:")
        .append(data.getJobID())
        .append(System.lineSeparator())
        .append("ExecId:")
        .append(data.getExecID());
    LogUtils.getPlaintTextLogger().info(infoBuilder.toString());
    if (isAsync) {
      data.setSuccess(data.getJobStatus() != null && data.getJobStatus().isJobSubmitted());
    }
  }

  @Override
  public void updateJobStatus() throws LinkisClientRuntimeException {
    if (!data.getJobStatus().isJobFinishedState()) {
      data.getOnceJobAdapter().updateStatus();
      String log2 =
          "\n---------------------------------------------------\n"
              + "\ttask "
              + data.getJobID()
              + " status is "
              + data.getJobStatus()
              + ", progress : "
              + data.getJobProgress()
              + "\n---------------------------------------------------";
      logger.info(log2);
    }
  }

  @Override
  public void waitJobComplete() throws LinkisClientRuntimeException {
    data.getOnceJobAdapter().waitForComplete();
    updateJobStatus();
    data.setSuccess(data.getJobStatus() != null && data.getJobStatus().isJobSuccess());
    waitIncLogComplete(data);
  }

  /** TerminatableJob */
  @Override
  public void terminate() throws LinkisClientRuntimeException {
    terminateToken.setTerminate();
    doKill();
  }

  @Override
  public void startRetrieveResult() {
    // TODO:wait for OnceJob to support this feature
    data.sendResultFin();
  }

  @Override
  public void startRetrieveLog() {
    data.setIncLogMode(true);
    startRetrieveLogInternal(data);
  }

  public void startRetrieveLogInternal(LinkisOnceJobData jobData) {
    if (!(jobData instanceof LinkisLogData)) {
      throw new LinkisClientExecutionException(
          "EXE0034", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "JobData is not LinkisLogData");
    }
    if (jobData.getUser() == null || jobData.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    LinkisOnceJobData logData = jobData;
    if (logData.getJobStatus() != null) {
      try {
        Thread logConsumer = new Thread(() -> logData.notifyLogListener(), "Log-Consumer");
        Thread logRetriever = new Thread(() -> queryLogLoop(logData), "Log-Retriever");
        SchedulerUtils.getCachedThreadPoolExecutor().execute(logRetriever);
        SchedulerUtils.getCachedThreadPoolExecutor().execute(logConsumer);
      } catch (Exception e) {
        logger.warn("Failed to retrieve log", e);
      }
    }
  }

  private void queryJobLogOneIteration(LinkisOnceJobData data) throws LinkisClientRuntimeException {
    try {
      data.getOnceJobAdapter().queryJobLogOneIteration();
      //
      // data.updateByOperResult(getJobOperator().queryRunTimeLogFromLine(data.getUser(),
      // data.getJobID(), data.getExecID(), fromLine));
    } catch (Exception e) {
      // job is finished while we start query log(but request is not send).
      // then probably server cache is gone and we got a exception here.
      // however we cannot know if this happens based on the exception message
      logger.warn(
          "Caught exception when querying runtime-log. Probably server-side has close stream. Will try openLog api if Job is completed.",
          e);
      if (data.getJobStatus().isJobFinishedState()) {
        CommonUtils.doSleepQuietly(500l);
        //
        // data.updateByOperResult(getJobOperator().queryPersistedLogFromLine(data.getUser(),
        // data.getJobID(), data.getExecID(), fromLine));
      }
    }
  }

  public void queryLogLoop(LinkisOnceJobData data) {
    boolean hasNext = true;
    int retryCnt = 0;
    final int MAX_RETRY = 12; // continues fails for 90s, then exit thread
    try {
      while (hasNext) {
        try {
          queryJobLogOneIteration(data);
        } catch (Exception e) {
          logger.error("Cannot get inc-log:", e);
          // and yes sometimes server may not be able to prepare persisted-log
          retryCnt++;
          if (retryCnt >= MAX_RETRY) {
            logger.error(
                "Continuously failing to query inc-log for "
                    + MAX_RETRY * (MAX_RETRY + 2) * 500 / 1000
                    + "s. Will no longer try to query log",
                e);
            break;
          }
          Utils.doSleepQuietly(500l + 500l * retryCnt); // maybe server problem. sleep longer
          continue;
        }
        retryCnt = 0;
        if (data.isIncLogMode()) {
          hasNext =
              data.hasNextLogLine() == null
                  ? !data.getJobStatus().isJobFinishedState()
                  : data.hasNextLogLine();
        } else {
          hasNext = false;
        }
        if (hasNext) {
          String msg =
              MessageFormat.format(
                  "Job is still running, status={0}, progress={1}",
                  data.getJobStatus(), String.valueOf(data.getJobProgress() * 100) + "%");
          logger.info(msg);
        }
        Utils.doSleepQuietly(AppConstants.JOB_QUERY_SLEEP_MILLS);
      }
    } catch (Exception e) {
      logger.error("Something goes wrong. Job Log may be incomplete", e);
    } finally {
      data.sendLogFin();
    }
  }

  private void waitIncLogComplete(LinkisJobData data) {
    if (!(data instanceof LinkisOnceJobData)) {
      return;
    }
    int retry = 0;
    int MAX_RETRY = 300; // wait for 10 minutes after job finish
    while (retry++ < MAX_RETRY) {
      if (((LinkisOnceJobData) data).logFinReceived()) {
        return;
      }
      CommonUtils.doSleepQuietly(CommonConstants.JOB_QUERY_SLEEP_MILLS);
    }
    String msg =
        "Job is in Finished state(SUCCEED/FAILED/CANCELED) but client keep querying inclog for "
            + (MAX_RETRY * CommonConstants.JOB_QUERY_SLEEP_MILLS / 1000)
            + "seconds. Execution ends forcefully. Next will try handle execution result.";
    logger.warn(msg);
    LogUtils.getInformationLogger().warn(msg);
  }

  //    /**
  //     * LogAccessibleJob
  //     */
  //    @Override
  //    public void startRetrieveLog() {

  //    }

  /** ManagableBackendJob */
  @Override
  public void doManage() throws LinkisClientRuntimeException {}

  @Override
  public boolean isSuccess() {
    return data.isSuccess();
  }

  private void doKill() {
    String msg;
    if (data.getJobStatus().isJobCancelled()) {
      msg = "Kill job aborted: Job is failed or has already been canceled.";
      data.setSuccess(false);
      data.setMessage(msg);
    } else if (data.getJobStatus().isJobFinishedState()) {
      msg = "Kill job aborted: Job is already in finished-state(SUCCEED/FAILED).";
      data.setSuccess(false);
      data.setMessage(msg);
      //            throw new LinkisClientExecutionException(JobStatus.FAILED, "EXE0004",
      // ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    } else {
      data.getOnceJobAdapter().kill();
      updateJobStatus();
      data.setSuccess(true);
      data.setMessage("successfully killed job");
    }
  }
}
