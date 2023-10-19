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

package org.apache.linkis.cli.application.interactor.job.interactive;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.job.Job;
import org.apache.linkis.cli.application.entity.job.JobResult;
import org.apache.linkis.cli.application.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.common.JobKiller;
import org.apache.linkis.cli.application.interactor.job.common.LogRetriever;
import org.apache.linkis.cli.application.interactor.job.common.ResultRetriever;
import org.apache.linkis.cli.application.operator.OperManager;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOper;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.application.present.LogPresenter;
import org.apache.linkis.cli.application.present.ResultPresenter;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveJob implements Job {

  private static final Logger logger = LoggerFactory.getLogger(InteractiveJob.class);

  private CliCtx ctx;

  private Boolean isAsync = false;

  private LinkisJobOper oper;

  private InteractiveJobDesc desc;

  private String username;

  private String jobId;

  @Override
  public void build(CliCtx ctx) {
    this.ctx = ctx;
    this.isAsync =
        ctx.getVarAccess().getVarOrDefault(Boolean.class, CliKeys.LINKIS_CLIENT_ASYNC_OPT, false);
    this.desc = InteractiveJobDescBuilder.build(ctx);
    this.oper = (LinkisJobOper) OperManager.getNew(CliKeys.Linkis_OPER, ctx);
  }

  @Override
  public JobResult run() {

    // Indicator
    StringBuilder infoBuilder = new StringBuilder();
    infoBuilder.append("connecting to linkis gateway:").append(oper.getServerUrl());
    LoggerManager.getInformationLogger().info(infoBuilder.toString());
    infoBuilder.setLength(0);

    // Submit
    LinkisOperResultAdapter submitResult = oper.submit(desc);
    CliUtils.doSleepQuietly(CliConstants.JOB_QUERY_SLEEP_MILLS);

    // JobInfo
    LinkisOperResultAdapter jobInfoResult =
        oper.queryJobInfo(submitResult.getUser(), submitResult.getJobID());
    oper.queryJobStatus(
        submitResult.getUser(), submitResult.getJobID(), submitResult.getStrongerExecId());
    infoBuilder.setLength(0);
    infoBuilder
        .append("JobId:")
        .append(submitResult.getJobID())
        .append(System.lineSeparator())
        .append("TaskId:")
        .append(submitResult.getJobID())
        .append(System.lineSeparator())
        .append("ExecId:")
        .append(submitResult.getStrongerExecId());
    LoggerManager.getPlaintTextLogger().info(infoBuilder.toString());
    infoBuilder.setLength(0);

    // Submit success or not
    if (!jobInfoResult.getJobStatus().isJobSubmitted()) {
      return new InteractiveJobResult(false, "Failed to submit job", new HashMap<>());
    } else {
      // Output that job is submitted
      infoBuilder.append("Job is successfully submitted!").append(System.lineSeparator());
      LoggerManager.getInformationLogger().info(infoBuilder.toString());
      infoBuilder.setLength(0);
      username = submitResult.getUser();
      jobId = submitResult.getJobID();
    }

    // async job, return
    if (isAsync) {
      return new InteractiveJobResult(
          jobInfoResult.getJobStatus().isJobSubmitted(),
          "Async Submission Success",
          new HashMap<>());
    }

    CliUtils.doSleepQuietly(2000l);

    // get log while running
    LogRetriever logRetriever =
        new LogRetriever(
            jobInfoResult.getUser(),
            jobInfoResult.getJobID(),
            jobInfoResult.getStrongerExecId(),
            true,
            oper,
            new LogPresenter());
    // async because we need to query job status
    logRetriever.retrieveLogAsync();

    // wait complete
    jobInfoResult = waitJobComplete(submitResult.getUser(), submitResult.getJobID(), submitResult.getStrongerExecId());
    logRetriever.waitIncLogComplete();

    // get result-set
    String outputPath =
        ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_COMMON_OUTPUT_PATH);
    ResultPresenter presenter;
    if (StringUtils.isBlank(outputPath)) {
      presenter = new ResultPresenter();
    } else {
      presenter = new ResultPresenter(true, outputPath);
    }

    ResultRetriever resultRetriever =
        new ResultRetriever(
            jobInfoResult.getUser(),
            jobInfoResult.getJobID(),
            jobInfoResult.getStrongerExecId(),
            oper,
            presenter);

    JobResult result = getResult(jobInfoResult, resultRetriever);

    return result;
  }

  private JobResult getResult(
      LinkisOperResultAdapter jobInfoResult, ResultRetriever resultRetriever)
      throws LinkisClientRuntimeException {
    if (!jobInfoResult.getJobStatus().isJobSuccess()) {
      LoggerManager.getInformationLogger()
          .info(
              "Job status is not success but \'"
                  + jobInfoResult.getJobStatus()
                  + "\'. Will not try to retrieve any Result");
      Map<String, String> extraMap = new HashMap<>();
      if (jobInfoResult.getErrCode() != null) {
        extraMap.put("errorCode", String.valueOf(jobInfoResult.getErrCode()));
      }
      if (StringUtils.isNotBlank(jobInfoResult.getErrDesc())) {
        extraMap.put("errorDesc", jobInfoResult.getErrDesc());
      }
      return new InteractiveJobResult(false, "Execute Error!!!", extraMap);
    }
    InteractiveJobResult result =
        new InteractiveJobResult(true, "Execute Success!!!", new HashMap<>());
    try {
      resultRetriever.retrieveResultSync();
      result.setSuccess(true);
      result.setMessage("execute success!!!");
    } catch (LinkisClientExecutionException e) {
      if (e.getCode().equals("EXE0037")) {
        result.setSuccess(true);
        result.setMessage("execute success!!!");
        LoggerManager.getInformationLogger().warn(e.getMessage());
      } else {
        result.setSuccess(false);
        result.setMessage("execute failed!!!\n" + ExceptionUtils.getStackTrace(e));
      }
      resultRetriever.setResultFin(); // inform listener to stop
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("execute failed!!!\n" + ExceptionUtils.getStackTrace(e));
      resultRetriever.setResultFin(); // inform listener to stop
    }
    return result;
  }

  private LinkisOperResultAdapter waitJobComplete(String user, String jobId, String execId)
      throws LinkisClientRuntimeException {
    int retryCnt = 0;
    final int MAX_RETRY = 30;

    LinkisOperResultAdapter jobInfoResult = oper.queryJobInfo(user, jobId);
    oper.queryJobStatus(user, jobId, execId);

    while (!jobInfoResult.getJobStatus().isJobFinishedState()) {
      // query progress
      try {
        jobInfoResult = oper.queryJobInfo(user, jobId);
        oper.queryJobStatus(
            jobInfoResult.getUser(), jobInfoResult.getJobID(), execId);
      } catch (Exception e) {
        logger.warn("", e);
        retryCnt++;
        if (retryCnt >= MAX_RETRY) {
          throw new LinkisClientExecutionException(
              "EXE0013",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Cannot get jobStatus from server continuously for {0} seconds. Client aborted! Error message: \n",
              MAX_RETRY * 5 * CliConstants.JOB_QUERY_SLEEP_MILLS,
              e);
        }
        CliUtils.doSleepQuietly(
            5 * CliConstants.JOB_QUERY_SLEEP_MILLS); // maybe server problem. sleep
        // longer
        continue;
      }
      retryCnt = 0; // reset counter
      if (jobInfoResult.getJobStatus().isJobAbnormalStatus()) {
        throw new LinkisClientExecutionException(
            "EXE0006",
            ErrorLevel.ERROR,
            CommonErrMsg.ExecutionErr,
            "Job is in abnormal status: " + CliUtils.GSON.toJson(jobInfoResult));
      }
      CliUtils.doSleepQuietly(CliConstants.JOB_QUERY_SLEEP_MILLS);
    }
    return jobInfoResult;
  }

  @Override
  public void onDestroy() {
    if (StringUtils.isBlank(username) || StringUtils.isBlank(jobId)) {
      logger.warn("Failed to kill job username or jobId is blank");
      return;
    }
    if (isAsync) {
      return;
    }
    try {
      new JobKiller(oper).doKill(username, jobId);
    } catch (Exception e) {
      logger.error("Failed to kill job", e);
    }
  }
}
