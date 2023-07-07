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

package org.apache.linkis.cli.application.operator.ujes;

import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.entity.operator.JobOper;
import org.apache.linkis.cli.application.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.interactive.InteractiveJobDesc;
import org.apache.linkis.cli.application.operator.ujes.result.OpenLogResult2;
import org.apache.linkis.cli.application.operator.ujes.result.ResultSetResult2;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.common.exception.LinkisException;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.request.JobSubmitAction;
import org.apache.linkis.ujes.client.request.OpenLogAction;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.response.*;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Based on UjesClient */
public class LinkisJobOper implements JobOper {
  protected UJESClient client;
  private Logger logger = LoggerFactory.getLogger(LinkisJobOper.class);
  private String serverUrl;

  public UJESClient getUJESClient() {
    return client;
  }

  public void setUJESClient(UJESClient client) {
    this.client = client;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public void close() {
    if (null != client) {
      try {
        client.close();
      } catch (IOException e) {
        logger.error("Close error. " + e.getMessage(), e);
      }
    }
  }

  public void checkInit() throws LinkisClientRuntimeException {
    if (client == null) {
      throw new LinkisClientExecutionException(
          "EXE0011", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "UjesClientDriver is null");
    }
  }

  /**
   * submit Job but does not query for progress
   *
   * @param
   * @return
   */
  public LinkisOperResultAdapter submit(InteractiveJobDesc jobDesc)
      throws LinkisClientRuntimeException {
    checkInit();
    JobSubmitResult jobSubmitResult;
    try {
      JobSubmitAction jobSubmitAction =
          JobSubmitAction.builder()
              .setUser(jobDesc.getSubmitUser())
              .addExecuteUser(jobDesc.getProxyUser())
              .setExecutionContent(jobDesc.getExecutionMap())
              .addExecuteCode((String) jobDesc.getExecutionMap().get(LinkisKeys.KEY_CODE))
              .setStartupParams(jobDesc.getParamConfMap())
              .setRuntimeParams(jobDesc.getParamRunTimeMap())
              .setVariableMap(jobDesc.getParamVarsMap())
              .setLabels(jobDesc.getLabelMap())
              .setSource(jobDesc.getSourceMap())
              .build();
      logger.info("Request info to Linkis: \n{}", CliUtils.GSON.toJson(jobSubmitAction));

      /* Old API */
      //      JobExecuteAction jobExecuteAction = JobExecuteAction.builder()
      //          .setCreator((String)
      // linkisJob.getLabelMap().get(LinkisKeys.KEY_USER_CREATOR))
      //          .setUser(linkisJob.getSubmitUser())
      //          .addExecuteCode((String)
      // linkisJob.getExecutionMap().get(LinkisKeys.KEY_CODE))
      //          .setEngineTypeStr((String)
      // linkisJob.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE))
      //          .setRunTypeStr((String)
      // linkisJob.getExecutionMap().get(LinkisKeys.KEY_CODETYPE))
      //          .setStartupParams(linkisJob.getParamConfMap())
      //          .setVariableMap(linkisJob.getParamVarsMap())
      //          .setScriptPath((String)
      // linkisJob.getSourceMap().get(LinkisKeys.KEY_SCRIPT_PATH))
      //          .build();
      //      logger.debug("Request info to Linkis Old: \n{}",
      // Utils.GSON.toJson(jobExecuteAction));
      //      jobExecuteResult = client.execute(jobExecuteAction);

      jobSubmitResult = client.submit(jobSubmitAction);
      logger.info("Response info from Linkis: \n{}", CliUtils.GSON.toJson(jobSubmitAction));

    } catch (Exception e) {
      // must throw if exception
      throw new LinkisClientExecutionException(
          "EXE0011", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Failed to submit job", e);
    }

    if (jobSubmitResult == null
        || 0 != jobSubmitResult.getStatus()
        || StringUtils.isBlank(jobSubmitResult.getTaskID())) {
      String reason;
      if (jobSubmitResult == null) {
        reason = "JobSubmitResult is null";
      } else if (0 != jobSubmitResult.getStatus()) {
        reason = "server returns non-zero status-code. ";
        reason += jobSubmitResult.getMessage();
      } else {
        reason = "server returns blank TaskId";
      }
      String msg = MessageFormat.format("Failed to submit job， Reason: {0}", reason);
      throw new LinkisClientExecutionException(
          "EXE0012", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }

    return new UJESResultAdapter(jobSubmitResult);
  }

  /**
   * loop and get job info until we success and get a valid execID
   *
   * @param user
   * @param taskID
   * @return
   */
  public LinkisOperResultAdapter queryJobInfo(String user, String taskID)
      throws LinkisClientRuntimeException {
    if (user == null || taskID == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    return new UJESResultAdapter(queryJobInfoInternal(user, taskID));
  }

  public LinkisOperResultAdapter queryJobStatus(String user, String taskID, String execID)
      throws LinkisClientRuntimeException {
    if (user == null || taskID == null || execID == null) {
      throw new LinkisClientExecutionException(
          "EXE0036",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "user or jobID or execID is null");
    }
    checkInit();
    JobExecuteResult executeResult = new JobExecuteResult();
    executeResult.setTaskID(taskID);
    executeResult.setUser(user);
    executeResult.setExecID(execID);
    JobStatusResult jobStatusResult = null;
    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        jobStatusResult = client.status(executeResult);
        logger.debug("job-status: " + CliUtils.GSON.toJson(jobStatusResult));
        if (jobStatusResult == null || 0 != jobStatusResult.getStatus()) {
          String reason;
          if (jobStatusResult == null) {
            reason = "jobStatusResult is null";
          } else {
            reason = "server returns non-zero status-code. ";
            reason += jobStatusResult.getMessage();
          }
          String msg =
              MessageFormat.format(
                  "Get job status failed. retry time : {0}/{1}. taskID={0}, Reason: {1}",
                  retryTime, MAX_RETRY_TIME, taskID, reason);

          logger.debug(
              "",
              new LinkisClientExecutionException(
                  "EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Get job status failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.warn(msg, e);
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg, e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (jobStatusResult == null || 0 != jobStatusResult.getStatus()) {
      String reason;
      if (jobStatusResult == null) {
        reason = "jobStatusResult is null";
      } else {
        reason = "server returns non-zero status-code. ";
        reason += jobStatusResult.getMessage();
      }
      String msg =
          MessageFormat.format(
              "Get status failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
      throw new LinkisClientExecutionException(
          "EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }
    return new UJESResultAdapter(jobStatusResult);
  }

  private JobInfoResult queryJobInfoInternal(String user, String taskID)
      throws LinkisClientRuntimeException {
    checkInit();
    JobExecuteResult executeResult = new JobExecuteResult();
    executeResult.setTaskID(taskID);
    executeResult.setUser(user);
    JobInfoResult jobInfoResult = null;
    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        jobInfoResult = client.getJobInfo(executeResult);
        logger.debug("job-info: " + CliUtils.GSON.toJson(jobInfoResult));
        if (jobInfoResult == null || 0 != jobInfoResult.getStatus()) {
          String reason;
          if (jobInfoResult == null) {
            reason = "JobInfoResult is null";
          } else {
            reason = "server returns non-zero status-code. ";
            reason += jobInfoResult.getMessage();
          }
          String msg =
              MessageFormat.format(
                  "Get job info failed. retry time : {0}/{1}. taskID={0}, Reason: {1}",
                  retryTime, MAX_RETRY_TIME, taskID, reason);

          logger.debug(
              "",
              new LinkisClientExecutionException(
                  "EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Get job info failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.warn(msg, e);
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg, e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (jobInfoResult == null || 0 != jobInfoResult.getStatus()) {
      String reason;
      if (jobInfoResult == null) {
        reason = "JobInfoResult is null";
      } else {
        reason = "server returns non-zero status-code";
        reason += jobInfoResult.getMessage();
      }
      String msg =
          MessageFormat.format(
              "Get info failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
      throw new LinkisClientExecutionException(
          "EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }
    return jobInfoResult;
  }

  public LinkisOperResultAdapter queryRunTimeLogFromLine(
      String user, String taskID, String execID, int fromLine) throws LinkisClientRuntimeException {
    checkInit();
    JobExecuteResult jobExecuteResult = new JobExecuteResult();
    jobExecuteResult.setUser(user);
    jobExecuteResult.setTaskID(taskID);
    jobExecuteResult.setExecID(execID);

    JobLogResult logResult = null;
    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        logResult = client.log(jobExecuteResult, fromLine, UJESConstants.MAX_LOG_SIZE);
        logger.debug("runtime-log-result:" + CliUtils.GSON.toJson(logResult));
        if (logResult == null || 0 != logResult.getStatus()) {
          String reason;
          if (logResult == null) {
            reason = "JobLogResult is null";
          } else {
            reason = "server returns non-zero status-code";
            reason += logResult.getMessage();
          }
          String msg =
              MessageFormat.format(
                  "Get log failed. retry time : {0}/{1}. taskID={2}. Reason: {3}",
                  retryTime, MAX_RETRY_TIME, taskID, reason);
          logger.debug(
              "",
              new LinkisClientExecutionException(
                  "EXE0015", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format("Get log failed. Retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        //                logger.warn("", new LinkisClientExecutionException("EXE0016",
        // ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0016", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg, e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (logResult == null || 0 != logResult.getStatus()) {
      String reason;
      if (logResult == null) {
        reason = "JobLogResult is null";
      } else {
        reason = "server returns non-zero status-code. ";
        reason += logResult.getMessage();
      }
      String msg =
          MessageFormat.format(
              "Get log failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
      //            logger.warn("", new LinkisClientExecutionException("EXE0016",
      // ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
      throw new LinkisClientExecutionException(
          "EXE0016", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }
    return new UJESResultAdapter(logResult);
  }

  public LinkisOperResultAdapter queryPersistedLogFromLine(
      String logPath, String user, String taskID, int fromLine) {
    return new UJESResultAdapter(
        new OpenLogResult2(queryPersistedLogInternal(logPath, user, taskID), fromLine));
  }

  private OpenLogResult queryPersistedLogInternal(String logPath, String user, String taskID)
      throws LinkisClientRuntimeException {
    checkInit();
    int retryCnt = 0;
    final int MAX_RETRY_TIMES = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;
    OpenLogResult openLogResult = null;

    while (retryCnt++ < MAX_RETRY_TIMES) {
      try {
        openLogResult =
            client.openLog(
                OpenLogAction.newBuilder().setLogPath(logPath).setProxyUser(user).build());
        logger.debug("persisted-log-result:" + CliUtils.GSON.toJson(openLogResult));
        if (openLogResult == null
            || 0 != openLogResult.getStatus()
            || StringUtils.isBlank(openLogResult.getLog()[UJESConstants.IDX_FOR_LOG_TYPE_ALL])) {
          String reason;
          if (openLogResult == null) {
            reason = "OpenLogResult is null";
          } else if (0 != openLogResult.getStatus()) {
            reason = "server returns non-zero status-code. ";
            reason += openLogResult.getMessage();
          } else {
            reason = "server returns empty log";
          }
          String msg =
              MessageFormat.format(
                  "Get log from openLog failed. retry time : {0}/{1}. taskID={2}. Reason: {3}",
                  retryCnt, MAX_RETRY_TIMES, taskID, reason);
          logger.debug(msg);
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Get log from openLog failed. retry time : {0}/{1}", retryCnt, MAX_RETRY_TIMES);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.debug(msg, e);
        if (retryCnt >= MAX_RETRY_TIMES) {
          throw new LinkisClientExecutionException(
              "EXE0017",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Get log from openLog failed. Retry exhausted. taskID=" + taskID,
              e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (openLogResult == null
        || 0 != openLogResult.getStatus()
        || StringUtils.isBlank(openLogResult.getLog()[UJESConstants.IDX_FOR_LOG_TYPE_ALL])) {
      String reason;
      if (openLogResult == null) {
        reason = "OpenLogResult is null";
      } else if (0 != openLogResult.getStatus()) {
        reason = "server returns non-zero status-code";
      } else {
        reason = "server returns empty log";
      }
      String msg =
          MessageFormat.format(
              "Get log from openLog failed. retry time : {0}/{1}. taskID={2}. Reason: {3}",
              retryCnt, MAX_RETRY_TIMES, taskID, reason);
      logger.debug(msg);
      if (retryCnt >= MAX_RETRY_TIMES) {
        msg =
            MessageFormat.format(
                "Get log from openLog failed. Retry exhausted. taskID={0}, Reason: {1}",
                taskID, reason);
        throw new LinkisClientExecutionException(
            "EXE0017", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
      }
    }
    return openLogResult;
  }

  public UJESResultAdapter queryProgress(String user, String taskID, String execId)
      throws LinkisClientRuntimeException {
    checkInit();
    JobExecuteResult executeResult = new JobExecuteResult();
    executeResult.setTaskID(taskID);
    executeResult.setUser(user);
    executeResult.setExecID(execId);

    JobProgressResult jobProgressResult = null;
    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        jobProgressResult = client.progress(executeResult);
        if (jobProgressResult == null || 0 != jobProgressResult.getStatus()) {
          String reason;
          if (jobProgressResult == null) {
            reason = "JobProgressResult is null";
          } else {
            reason = "server returns non-zero status-code. ";
            reason += jobProgressResult.getMessage();
          }
          String msg =
              MessageFormat.format(
                  "Get progress failed. retry time : {0}/{1}. taskID={2}. Reason: {3}",
                  retryTime, MAX_RETRY_TIME, taskID, reason);
          logger.debug(msg);
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Get progress failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.warn(msg, e);
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0019",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Get progress failed. Retry exhausted. taskID=" + taskID,
              e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }

    if (jobProgressResult == null || 0 != jobProgressResult.getStatus()) {
      String reason;
      if (jobProgressResult == null) {
        reason = "JobProgressResult is null";
      } else {
        reason = "server returns non-zero status-code. ";
        reason += jobProgressResult.getMessage();
      }
      String msg =
          MessageFormat.format(
              "Get progress failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
      throw new LinkisClientExecutionException(
          "EXE0020", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }

    return new UJESResultAdapter(jobProgressResult);
  }

  public LinkisOperResultAdapter queryResultSetPaths(
      String user, String taskID, String resultLocation) {
    checkInit();

    JobInfoResult jobInfoResult = queryJobInfoInternal(user, taskID);
    if (null == jobInfoResult) {
      String msg = "Get ResultSet Failed: Cannot get a valid jobInfo";
      logger.error(msg);
      throw new LinkisClientExecutionException(
          "EXE0021", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }
    if (!jobInfoResult.isSucceed()) {
      String msg = "Get ResultSet Failed: job Status is not \"Succeed\", .";
      throw new LinkisClientExecutionException(
          "EXE0021", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }

    if (StringUtils.isBlank(jobInfoResult.getRequestPersistTask().getResultLocation())) {
      // sometimes server-side does not return this
      jobInfoResult.getRequestPersistTask().setResultLocation(resultLocation);
    }

    if (StringUtils.isBlank(jobInfoResult.getRequestPersistTask().getResultLocation())) {
      throw new LinkisClientExecutionException(
          "EXE0021", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "ResultLocation is blank.");
    }

    String[] resultSetArray = null;

    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        resultSetArray = jobInfoResult.getResultSetList(client); // this makes call to server
        if (resultSetArray == null || 0 == resultSetArray.length) {
          String reason;
          if (resultSetArray == null) {
            reason = "array is null";
          } else {
            reason = "array length is zero";
          }
          String msg =
              MessageFormat.format(
                  "Get resultSetArray failed. retry time : {0}/{1}. taskID={2} Reason: {3}",
                  retryTime, MAX_RETRY_TIME, taskID, reason);
          logger.debug(msg);
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Get resultSetArray failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.warn(msg, e);
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0022",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Get resultSetArray failed. Retry exhausted. taskID=" + taskID,
              e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (resultSetArray == null || 0 == resultSetArray.length) {
      String reason;
      if (resultSetArray == null) {
        reason = "array is null";
      } else {
        reason = "array length is zero";
      }
      String msg =
          MessageFormat.format(
              "Get resultSetArray failed. retry exhausted. taskID={0}. Reason: {1}",
              taskID, reason);
      logger.warn(msg);
    }
    return new UJESResultAdapter(resultSetArray);
  }

  public LinkisOperResultAdapter queryResultSetGivenResultSetPath(
      String[] resultSetPaths, int idxResultSet, String user, Integer page, Integer pageSize) {
    checkInit();
    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;
    ResultSetResult result = null;
    String resultSetPath = resultSetPaths[idxResultSet];
    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        ResultSetAction action =
            ResultSetAction.builder()
                .setPath(resultSetPath)
                .setUser(user)
                .setPage(page)
                .setPageSize(pageSize)
                .build();
        result = client.resultSet(action);
        logger.debug("resultset-result:" + CliUtils.GSON.toJson(result));
        if (result == null || 0 != result.getStatus()) {
          String reason;
          if (result == null) {
            reason = "array is null";
          } else {
            reason = "server returns non-zero status-code. ";
            reason += result.getMessage();
          }
          String msg =
              MessageFormat.format(
                  "Get resultSet failed. retry time : {0}/{1}. path={2}, Reason: {3}",
                  retryTime, MAX_RETRY_TIME, resultSetPath, reason);
          logger.debug(msg);
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Get resultSet failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.warn(msg, e);
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0024",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Get resultSet failed. Retry exhausted. path=" + resultSetPath,
              e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (result == null || 0 != result.getStatus()) {
      String reason;
      if (result == null) {
        reason = "ResultSetResult is null";
      } else {
        reason = "server returns non-zero status-code. ";
        reason += result.getMessage();
      }
      String msg =
          MessageFormat.format(
              "Get resultSet failed. Retry exhausted. Path={0}, Reason: {1}",
              resultSetPath, reason);
      throw new LinkisClientExecutionException(
          "EXE0024", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }
    return new UJESResultAdapter(new ResultSetResult2(idxResultSet, result));
  }

  public LinkisOperResultAdapter kill(String user, String taskId, String execId)
      throws LinkisClientRuntimeException {
    checkInit();

    int retryTime = 0;
    final int MAX_RETRY_TIME = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

    JobKillResult result = null;

    while (retryTime++ < MAX_RETRY_TIME) {
      try {
        JobExecuteResult killRequest = new JobExecuteResult();
        killRequest.setUser(user);
        killRequest.setTaskID(taskId);
        killRequest.setExecID(execId);
        result = client.kill(killRequest);
        logger.debug("job-kill-result:" + CliUtils.GSON.toJson(result));
        if (result == null || 0 != result.getStatus()) {
          String reason;
          if (result == null) {
            reason = "result is null";
          } else {
            reason = "server returns non-zero status-code. ";
            reason += result.getMessage();
          }
          String msg =
              MessageFormat.format(
                  "Kill job failed. retry time : {0}/{1}. taskId={2}, Reason: {3}",
                  retryTime, MAX_RETRY_TIME, taskId, reason);
          logger.debug(msg);
        } else {
          break;
        }
      } catch (Exception e) {
        String msg =
            MessageFormat.format(
                "Kill job failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
        if (e instanceof LinkisException) {
          msg += " " + e.toString();
        }
        logger.warn(msg, e);
        if (retryTime >= MAX_RETRY_TIME) {
          throw new LinkisClientExecutionException(
              "EXE0025",
              ErrorLevel.ERROR,
              CommonErrMsg.ExecutionErr,
              "Kill job failed. taskId={0} Retry exhausted.",
              taskId,
              e);
        }
      }
      CliUtils.doSleepQuietly(UJESConstants.DRIVER_QUERY_SLEEP_MILLS);
    }
    if (result == null || 0 != result.getStatus()) {
      String reason;
      if (result == null) {
        reason = "result is null";
      } else {
        reason = "server returns non-zero status-code. ";
        reason += result.getMessage();
      }
      String msg =
          MessageFormat.format(
              "Kill job failed. Retry exhausted. taskId={0}, Reason: {1}", taskId, reason);
      throw new LinkisClientExecutionException(
          "EXE0025", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    }
    return new UJESResultAdapter(result);
  }
}
