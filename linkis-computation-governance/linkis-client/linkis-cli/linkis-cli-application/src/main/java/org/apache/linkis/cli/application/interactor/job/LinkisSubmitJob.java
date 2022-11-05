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

import org.apache.linkis.cli.application.interactor.job.data.LinkisJobData;
import org.apache.linkis.cli.application.interactor.job.data.LinkisLogData;
import org.apache.linkis.cli.application.interactor.job.data.LinkisResultData;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisJobManDesc;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisSubmitDesc;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOperator;
import org.apache.linkis.cli.common.entity.operator.JobOperator;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.constants.CommonConstants;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.job.*;
import org.apache.linkis.cli.core.utils.CommonUtils;
import org.apache.linkis.cli.core.utils.LogUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisSubmitJob extends LinkisJob
    implements AsyncBackendJob, LogAccessibleJob, ResultAccessibleJob, TerminatableJob {
  private static final Logger logger = LoggerFactory.getLogger(LinkisSubmitJob.class);

  private LinkisSubmitDesc jobDesc;
  private LinkisJobData data;
  private TerminateToken terminateToken = new TerminateToken();
  private LinkisManageJob manageJob = new LinkisManageJob();
  private Boolean isAsync = false;

  public void setAsync(Boolean async) {
    isAsync = async;
  }

  public Boolean isAsync() {
    return isAsync;
  }

  @Override
  public LinkisJobOperator getJobOperator() {
    if (!(super.getJobOperator() instanceof LinkisJobOperator)) {
      throw new LinkisClientExecutionException(
          "EXE0003",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "JobOperator of LinkisManageJob should be instance of LinkisJobOperator");
    }
    return (LinkisJobOperator) super.getJobOperator();
  }

  @Override
  public void setOperator(JobOperator operator) {
    if (!(operator instanceof LinkisJobOperator)) {
      throw new LinkisClientExecutionException(
          "EXE0003",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "JobOperator of LinkisManageJob should be instance of LinkisJobOperator");
    }
    manageJob.setOperator(operator);
    super.setOperator(operator);
  }

  @Override
  public LinkisSubmitDesc getJobDesc() {
    return jobDesc;
  }

  public void setJobDesc(LinkisSubmitDesc jobDesc) {
    this.jobDesc = jobDesc;
  }

  @Override
  public LinkisJobData getJobData() {
    return data;
  }

  public void setJobData(LinkisJobData data) {
    manageJob.setJobData(data);
    this.data = data;
  }

  @Override
  public TerminateToken getTerminateToken() {
    return terminateToken;
  }

  public void setTerminateToken(TerminateToken terminateToken) {
    this.terminateToken = terminateToken;
  }

  @Override
  public void submit() throws LinkisClientRuntimeException {
    StringBuilder infoBuilder = new StringBuilder();
    infoBuilder.append("connecting to linkis gateway:").append(getJobOperator().getServerUrl());
    LogUtils.getInformationLogger().info(infoBuilder.toString());
    data.updateByOperResult(getJobOperator().submit(jobDesc));
    CommonUtils.doSleepQuietly(2000l);
    LinkisJobManDesc jobManDesc = new LinkisJobManDesc();
    jobManDesc.setJobId(data.getJobID());
    jobManDesc.setUser(data.getUser());
    manageJob.setJobDesc(jobManDesc);
    data.updateByOperResult(getJobOperator().queryJobInfo(data.getUser(), data.getJobID()));
    infoBuilder.setLength(0);
    infoBuilder
        .append("JobId:")
        .append(data.getJobID())
        .append(System.lineSeparator())
        .append("TaskId:")
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
    data.updateByOperResult(getJobOperator().queryJobInfo(data.getUser(), data.getJobID()));
    getJobOperator().queryJobStatus(data.getUser(), data.getJobID(), data.getExecID());
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

  @Override
  public void waitJobComplete() throws LinkisClientRuntimeException {
    int retryCnt = 0;
    final int MAX_RETRY = 30;
    while (!data.getJobStatus().isJobFinishedState()) {
      // query progress
      try {
        data.updateByOperResult(getJobOperator().queryJobInfo(data.getUser(), data.getJobID()));
        getJobOperator().queryJobStatus(data.getUser(), data.getJobID(), data.getExecID());
      } catch (Exception e) {
        logger.warn("", e);
        retryCnt++;
        if (retryCnt >= MAX_RETRY) {
          throw new LinkisClientExecutionException(
              "EXE0013",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Cannot get jobStatus from server continuously for {0} seconds. Client aborted! Error message: \n",
              MAX_RETRY * 5 * CommonConstants.JOB_QUERY_SLEEP_MILLS,
              e);
        }
        CommonUtils.doSleepQuietly(
            5 * CommonConstants.JOB_QUERY_SLEEP_MILLS); // maybe server problem. sleep
        // longer
        continue;
      }
      retryCnt = 0; // reset counter
      checkJobAvailability(data);
      CommonUtils.doSleepQuietly(CommonConstants.JOB_QUERY_SLEEP_MILLS);
    }
    data.setSuccess(data.getJobStatus() != null && data.getJobStatus().isJobSuccess());
    waitIncLogComplete(data);
  }

  private void waitIncLogComplete(LinkisJobData data) {
    if (!(data instanceof LinkisLogData)) {
      return;
    }
    int retry = 0;
    int MAX_RETRY = 300; // wait for 10 minutes after job finish
    while (retry++ < MAX_RETRY) {
      if (((LinkisLogData) data).logFinReceived()) {
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

  @Override
  public void startRetrieveResult() {
    try {
      manageJob.startRetrieveResult();
      data.setSuccess(true);
    } catch (LinkisClientExecutionException e) {
      if (e.getCode().equals("EXE0037")) {
        data.setSuccess(true);
        LogUtils.getInformationLogger().warn(e.getMessage());
      } else {
        data.setSuccess(false);
        data.setException(e);
      }
      ((LinkisResultData) data).sendResultFin(); // inform listener to stop
    } catch (Exception e) {
      data.setSuccess(false);
      data.setException(e);
      ((LinkisResultData) data).sendResultFin(); // inform listener to stop
    }
  }

  @Override
  public void startRetrieveLog() {
    if (!(data instanceof LinkisLogData)) {
      throw new LinkisClientExecutionException(
          "EXE0034", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "JobData is not LinkisLogData");
    }
    LinkisLogData dataCopy;
    try {
      dataCopy = ((LinkisLogData) data).clone(); // make a copy to avoid race condition
    } catch (CloneNotSupportedException e) {
      throw new LinkisClientExecutionException(
          "EXE0035", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "logData is not Cloneable", e);
    }
    dataCopy.setIncLogMode(true);
    manageJob.startRetrieveLogInternal(dataCopy);
  }

  @Override
  public void terminate() throws LinkisClientRuntimeException {
    terminateToken.setTerminate();
    // kill job if job is submitted
    if (StringUtils.isNotBlank(data.getJobID())) {
      System.out.println("\nKilling job: " + data.getJobID());
      try {
        manageJob.doKill();
        if (data.getJobStatus().isJobCancelled()) {
          System.out.println("Successfully killed job: " + data.getJobID() + " on exit");
        } else {
          System.out.println(
              "Failed to kill job: "
                  + data.getJobID()
                  + " on exit. Current job status: "
                  + data.getJobStatus());
        }
      } catch (Exception e) {
        System.out.println("Failed to kill job: " + data.getJobID() + " on exit");
        System.out.println(ExceptionUtils.getStackTrace(e));
      }
    }
  }

  private void checkJobAvailability(LinkisJobData data) throws LinkisClientRuntimeException {
    if (data.getJobStatus().isJobAbnormalStatus()) {
      throw new LinkisClientExecutionException(
          "EXE0006",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "Job is in abnormal status: " + CommonUtils.GSON.toJson(data));
    }
  }
}
