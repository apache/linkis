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
import org.apache.linkis.cli.application.interactor.job.data.LinkisResultData;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisJobManDesc;
import org.apache.linkis.cli.application.interactor.job.subtype.LinkisManSubType;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOperator;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.job.JobData;
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

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisManageJob extends LinkisJob
    implements ManagableBackendJob, TerminatableJob, LogAccessibleJob, ResultAccessibleJob {
  private static final Logger logger = LoggerFactory.getLogger(LinkisManageJob.class);

  private LinkisJobManDesc jobDesc;
  private LinkisJobData data;
  private TerminateToken terminateToken = new TerminateToken();

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
    super.setOperator(operator);
  }

  @Override
  public LinkisJobManDesc getJobDesc() {
    return jobDesc;
  }

  public void setJobDesc(LinkisJobManDesc jobDesc) {
    this.jobDesc = jobDesc;
  }

  @Override
  public LinkisJobData getJobData() {
    return data;
  }

  public void setJobData(LinkisJobData data) {
    this.data = data;
  }

  @Override
  public boolean isSuccess() {
    return data.isSuccess();
  }

  @Override
  public void doManage() throws LinkisClientRuntimeException {
    LinkisManSubType subType = (LinkisManSubType) getSubType();
    if (!(subType instanceof LinkisManSubType)) {
      throw new LinkisClientExecutionException(
          "EXE0030",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "JobSubType is not instance of JobManSubType");
    }
    switch (subType) {
      case STATUS:
        try {
          data.updateByOperResult(
              getJobOperator().queryJobInfo(jobDesc.getUser(), jobDesc.getJobID()));
          if (data.getJobStatus() != null) {
            data.setSuccess(true);
          }
        } catch (Exception e) {
          data.setSuccess(false);
          data.setException(e);
        }
        break;
        //            case JOB_DESC:
        //                result = jobManagableBackendExecutor.queryJobDesc(job);
        //                break;
      case LOG:
        try {
          ((LinkisLogData) data).setIncLogMode(false);
          startRetrieveLog();
          waitLogFin();
          data.setSuccess(true);
        } catch (Exception e) {
          data.setSuccess(false);
          data.setException(e);
        }
        break;
      case RESULT:
        try {
          startRetrieveResult();
          data.setSuccess(true);
        } catch (LinkisClientExecutionException e) {
          if (e.getCode().equals("EXE0037")) {
            ((LinkisResultData) data).sendResultFin(); // inform listener to stop
            data.setSuccess(true);
          } else {
            data.setSuccess(false);
            data.setException(e);
          }
          LogUtils.getInformationLogger().warn(e.getMessage());
        } catch (Exception e) {
          data.setSuccess(false);
          data.setException(e);
          LogUtils.getInformationLogger().warn(e.getMessage());
        }
        break;
        //            case LIST:
        //                resultData = jobManExecutor.queryJobList(job);
        //                break;
      case KILL:
        doKill();
        break;
      default:
        throw new LinkisClientExecutionException(
            "EXE0002",
            ErrorLevel.ERROR,
            CommonErrMsg.ExecutionErr,
            "JobSubType + \"" + subType + "\" is not supported");
    }
  }

  @Override
  public void startRetrieveLog() {
    if (jobDesc.getUser() == null || jobDesc.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    data.updateByOperResult(getJobOperator().queryJobInfo(jobDesc.getUser(), jobDesc.getJobID()));
    startRetrieveLogInternal(data);
  }

  public void waitLogFin() {
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
  }

  public void startRetrieveLogInternal(JobData jobData) {
    if (!(jobData instanceof LinkisLogData)) {
      throw new LinkisClientExecutionException(
          "EXE0034", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "JobData is not LinkisLogData");
    }
    if (jobData.getUser() == null || jobData.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    LinkisLogData logData = (LinkisLogData) jobData;
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

  public void queryLogLoop(LinkisLogData data) {
    int curLogIdx;
    int nextLogIdx;
    boolean hasNext = true;
    int retryCnt = 0;
    final int MAX_RETRY = 12; // continues fails for 90s, then exit thread
    try {
      while (hasNext) {
        curLogIdx = data.getNextLogLineIdx() == null ? 0 : data.getNextLogLineIdx();
        try {
          data.updateByOperResult(getJobOperator().queryJobInfo(data.getUser(), data.getJobID()));
          queryJobLogFromLine(data, curLogIdx);
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
        nextLogIdx = data.getNextLogLineIdx() == null ? curLogIdx : data.getNextLogLineIdx();
        if (data.isIncLogMode()) {
          hasNext = data.hasNextLogLine() == null ? curLogIdx < nextLogIdx : data.hasNextLogLine();
        } else {
          hasNext = curLogIdx < nextLogIdx;
        }
        if (curLogIdx >= nextLogIdx) {
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

  private void queryJobLogFromLine(LinkisLogData data, int fromLine)
      throws LinkisClientRuntimeException {
    if (!data.getJobStatus().isJobFinishedState()) {
      try {
        data.updateByOperResult(
            getJobOperator()
                .queryRunTimeLogFromLine(
                    data.getUser(), data.getJobID(), data.getExecID(), fromLine));
      } catch (Exception e) {
        // job is finished while we start query log(but request is not send).
        // then probably server cache is gone and we got a exception here.
        // however we cannot know if this happens based on the exception message
        logger.warn(
            "Caught exception when querying runtime-log. Probably server-side has close stream. Will try openLog api if Job is completed.",
            e);
        if (data.getJobStatus().isJobFinishedState()) {
          CommonUtils.doSleepQuietly(500l);
          data.updateByOperResult(
              getJobOperator()
                  .queryPersistedLogFromLine(
                      data.getUser(), data.getJobID(), data.getExecID(), fromLine));
        }
      }
    } else {
      try {
        data.updateByOperResult(
            getJobOperator()
                .queryPersistedLogFromLine(
                    data.getLogPath(), data.getUser(), data.getJobID(), fromLine));
      } catch (Exception e) {
        logger.error("Cannot get persisted-inc-log:", e);
        // and yes sometimes server may not be able to prepare persisted-log
        throw e;
      }
    }
  }

  @Override
  public void startRetrieveResult() {
    if (!(data instanceof LinkisResultData)) {
      throw new LinkisClientExecutionException(
          "EXE0034",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "JobData is not LinkisResultData");
    }
    if (jobDesc.getUser() == null || jobDesc.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    data.updateByOperResult(getJobOperator().queryJobInfo(jobDesc.getUser(), jobDesc.getJobID()));
    if (data.getJobStatus() == null) {
      throw new LinkisClientExecutionException(
          "EXE0038", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "jobStatus is null");
    }
    LinkisResultData resultData = (LinkisResultData) data;
    if (!resultData.getJobStatus().isJobSuccess()
        || StringUtils.isBlank(resultData.getResultLocation())) {
      resultData.updateByOperResult(
          getJobOperator().queryJobInfo(resultData.getUser(), resultData.getJobID()));
    }
    if (!resultData.getJobStatus().isJobSuccess()) {
      //            throw new LinkisClientExecutionException("EXE0035", ErrorLevel.ERROR,
      // CommonErrMsg.ExecutionErr, "Job status is not success but \'" +
      // resultData.getJobStatus() + "\'. Will not try to retrieve any Result");
      LogUtils.getInformationLogger()
          .info(
              "Job status is not success but \'"
                  + resultData.getJobStatus()
                  + "\'. Will not try to retrieve any Result");
      resultData.sendResultFin(); // inform listener to stop
      return;
    }
    if (StringUtils.isBlank(resultData.getResultLocation())) {
      throw new LinkisClientExecutionException(
          "EXE0037",
          ErrorLevel.WARN,
          CommonErrMsg.ExecutionErr,
          "Got blank ResultLocation from server. Job may not have result-set. Will not try to retrieve any Result");
    }

    resultData.updateByOperResult(
        getJobOperator()
            .queryResultSetPaths(
                resultData.getUser(), resultData.getJobID(), resultData.getResultLocation()));
    if (resultData.getResultSetPaths() == null || resultData.getResultSetPaths().length == 0) {
      String msg = "Your job got no result.";
      logger.warn(msg);
      resultData.sendResultFin(); // inform listener to stop
      resultData.setHasResult(false);
      return;
    }

    try {
      resultData.setHasResult(true);
      Thread resultRetriever = new Thread(() -> queryResultLoop(resultData), "Result-Retriever");
      SchedulerUtils.getCachedThreadPoolExecutor().execute(resultRetriever);
    } catch (Exception e) {
      logger.error("Failed to retrieve result", e);
      throw e;
    }
  }

  public void queryResultLoop(LinkisResultData data) {
    boolean hasNext = true;
    int retryCnt = 0;
    final int MAX_RETRY = 30; // continues fails for 250s, then exit
    int idx = 0;
    try {
      while (hasNext) {
        try {
          hasNext = queryOneResult(data, idx);
        } catch (LinkisClientRuntimeException e) {
          logger.error("Cannot get result:", e);
          retryCnt++;
          if (retryCnt >= MAX_RETRY) {
            logger.error(
                "Continuously failing to query result for "
                    + MAX_RETRY * (MAX_RETRY + 2) * 500 / 1000
                    + "s. Will no longer try to query result",
                e);
            return;
          } else {
            hasNext = true;
          }
          Utils.doSleepQuietly(500l + 500l * retryCnt); // maybe server problem. sleep longer
          continue;
        }
        idx++;
      }
    } catch (Exception e) {
      logger.error("Something goes wrong. Job Result may be incomplete", e);
      throw e;
    } finally {
      data.sendResultFin();
    }
  }

  private boolean queryOneResult(LinkisResultData data, int idxResultSet) {
    Integer curPage = 1;
    boolean hasNextResult = true;
    boolean hasNextPage = true;
    while (hasNextPage) {
      data.updateByOperResult(
          getJobOperator()
              .queryResultSetGivenResultSetPath(
                  data.getResultSetPaths(),
                  idxResultSet,
                  data.getUser(),
                  curPage,
                  AppConstants.RESULTSET_PAGE_SIZE));
      if (data.hasNextResultPage() == null) {
        throw new LinkisClientExecutionException(
            "EXE0040",
            ErrorLevel.ERROR,
            CommonErrMsg.ExecutionResultErr,
            "Something foes wrong. Got null as \'hasNextPage\'.");
      }
      hasNextPage = data.hasNextResultPage();

      curPage++;
      hasNextResult = idxResultSet + 1 < data.getResultSetPaths().length;
    }
    return hasNextResult;
  }

  public void doKill() {
    data.updateByOperResult(getJobOperator().queryJobInfo(jobDesc.getUser(), jobDesc.getJobID()));
    if (data.getUser() == null || data.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    if (data.getJobStatus() == null) {
      throw new LinkisClientExecutionException(
          "EXE0038", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "jobStatus is null");
    }
    String msg;
    if (data.getJobStatus().isJobCancelled()) {
      msg = "Kill job aborted: Job has already been canceled.";
      data.setSuccess(false);
      data.setMessage(msg);
    } else if (data.getJobStatus().isJobFinishedState()) {
      msg = "Kill job aborted: Job is already in finished-state(SUCCEED/FAILED).";
      data.setSuccess(false);
      data.setMessage(msg);
      //            throw new LinkisClientExecutionException(JobStatus.FAILED, "EXE0004",
      // ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    } else {
      try {
        data.updateByOperResult(
            getJobOperator().queryJobInfo(jobDesc.getUser(), jobDesc.getJobID()));
        data.updateByOperResult(
            getJobOperator().kill(data.getUser(), data.getJobID(), data.getExecID()));
      } catch (Exception e) {
        data.setSuccess(false);
        data.setMessage("Exception thrown when trying to send kill request");
        data.setException(e);
      }
      msg = "Kill request has been sent";
      LogUtils.getPlaintTextLogger().info(msg);
      int retryCnt = 0;
      final int MAX_RETRY = 5 * 6;
      while (!data.getJobStatus().isJobFinishedState() && !data.getJobStatus().isJobCancelled()) {
        CommonUtils.doSleepQuietly(CommonConstants.JOB_QUERY_SLEEP_MILLS);
        try {
          data.updateByOperResult(
              getJobOperator().queryJobInfo(jobDesc.getUser(), jobDesc.getJobID()));
          retryCnt = 0; // if exception then will not go here
        } catch (Exception e) {
          retryCnt++;
          CommonUtils.doSleepQuietly(5 * CommonConstants.JOB_QUERY_SLEEP_MILLS);
          if (retryCnt >= MAX_RETRY) {
            data.setSuccess(false);
            data.setMessage(
                MessageFormat.format(
                    "After send kill. Client cannot get jobStatus from server continuously for {0} seconds. Client aborted. Assume kill failed! Error message: \n",
                    MAX_RETRY * 5 * CommonConstants.JOB_QUERY_SLEEP_MILLS));
            data.setException(e);
            return;
          }
        }
      }
      if (data.getJobStatus().isJobFinishedState() && !data.getJobStatus().isJobCancelled()) {
        msg = "Kill Failed: Job Current status: " + data.getJobStatus();
        data.setSuccess(false);
        data.setMessage(msg);
        //                throw new LinkisClientExecutionException(JobStatus.FAILED,
        // "EXE0004", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
      } else if (data.getJobStatus().isJobCancelled()) {
        msg =
            MessageFormat.format(
                "Kill successful: jobId={0}, status={1}.", data.getJobID(), data.getJobStatus());
        data.setSuccess(true);
        data.setMessage(msg);
        //                LogUtils.getPlaintTextLogger().info(msg);
      }
    }
    return;
  }

  @Override
  public TerminateToken getTerminateToken() {
    return terminateToken;
  }

  public void setTerminateToken(TerminateToken terminateToken) {
    this.terminateToken = terminateToken;
  }

  @Override
  public void terminate() throws LinkisClientRuntimeException {
    return;
  }
}
