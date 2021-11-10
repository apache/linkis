/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cli.application.driver;

import org.apache.linkis.cli.application.constants.LinkisConstants;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.constants.UjesClientDriverConstants;
import org.apache.linkis.cli.application.driver.context.LinkisClientDriverContext;
import org.apache.linkis.cli.application.driver.context.UjesClientDriverContext;
import org.apache.linkis.cli.application.interactor.job.LinkisJob;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.common.exception.LinkisException;
import org.apache.linkis.httpclient.authentication.AuthenticationStrategy;
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.ujes.client.UJESClient;
import org.apache.linkis.ujes.client.UJESClientImpl;
import org.apache.linkis.ujes.client.request.JobSubmitAction;
import org.apache.linkis.ujes.client.request.OpenLogAction;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * @description: UjesClientDriver encapsulates UjesClient directly, calling apis to submit job/ query info etc, and translating UjesClient's exception.
 */
public class UjesClientDriver implements LinkisClientDriver {
    private Logger logger = LoggerFactory.getLogger(UjesClientDriver.class);

    private UJESClient client;
    private UjesClientDriverContext context;

    @Override
    public void initDriver(LinkisClientDriverContext context) throws LinkisClientRuntimeException {
        if (null == context) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0008", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "Cannot init UjesClientDriver: driver context is null");
        }
        if (!(context instanceof UjesClientDriverContext)) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0009", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "Cannot init UjesClientDriver: driver context is not instance of UjesClientDriverContext");
        }

        UjesClientDriverContext ujesContext = (UjesClientDriverContext) context;
        this.context = ujesContext;

        if (null == client) {
            try {

                AuthenticationStrategy authenticationStrategy;
                if (StringUtils.isBlank(ujesContext.getAuthenticationStrategyStr()) ||
                        !LinkisConstants.AUTH_STRATEGY_TOKEN.equalsIgnoreCase(ujesContext.getAuthenticationStrategyStr())) {
                    authenticationStrategy = new StaticAuthenticationStrategy(); //this has to be newed here otherwise log-in fails for static
                } else {
                    authenticationStrategy = new TokenAuthenticationStrategy();
                }

                DWSClientConfigBuilder builder = DWSClientConfigBuilder.newBuilder();
                DWSClientConfig config = ((DWSClientConfigBuilder) (
                        builder.addServerUrl(ujesContext.getGatewayUrl())
                                .connectionTimeout(30000)
                                .discoveryEnabled(false).discoveryFrequency(1, TimeUnit.MINUTES)
                                .loadbalancerEnabled(true)
                                .maxConnectionSize(5)
                                .retryEnabled(false).readTimeout(ujesContext.getReadTimeoutMills())
                                .setAuthenticationStrategy(authenticationStrategy)  //token/static
                                .setAuthTokenKey(ujesContext.getTokenKey())         //static authentication -> submitUser must be the same as tokenKey
                                .setAuthTokenValue(ujesContext.getTokenValue())))
                        .setDWSVersion(ujesContext.getDwsVersion()).build();

                client = new UJESClientImpl(config);
            } catch (Exception e) {
                throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0010", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "Cannot init UjesClientDriver", e);

            }

            logger.info("Linkis ujes client inited.");
        } else {
            logger.info("No need to init linkis ujes client.");
        }
    }

    @Override
    public void close() {
        if (null != client) {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Close error. " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void checkInit() throws LinkisClientRuntimeException {
        if (client == null) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0011", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "UjesClientDriver is null");
        }
    }

    /**
     * submit Job but does not query for progress
     *
     * @param
     * @return
     */
    @Override
    public JobSubmitResult submit(LinkisJob linkisJob) throws LinkisClientRuntimeException {
        checkInit();
        JobSubmitResult jobSubmitResult;
        try {
            JobSubmitAction jobSubmitAction = JobSubmitAction.builder()
                    .setUser(linkisJob.getSubmitUser())
                    .addExecuteUser(linkisJob.getProxyUser())
                    .setExecutionContent(linkisJob.getExecutionMap())
                    .addExecuteCode((String) linkisJob.getExecutionMap().get(LinkisKeys.KEY_CODE))
                    .setStartupParams(linkisJob.getParamConfMap())
                    .setVariableMap(linkisJob.getParamVarsMap())
                    .setLabels(linkisJob.getLabelMap())
                    .setSource(linkisJob.getSourceMap())
                    .build();
            logger.info("Request info to Linkis: \n{}", Utils.GSON.toJson(jobSubmitAction));

            /* Old API */
//      JobExecuteAction jobExecuteAction = JobExecuteAction.builder()
//          .setCreator((String) linkisJob.getLabelMap().get(LinkisKeys.KEY_USER_CREATOR))
//          .setUser(linkisJob.getSubmitUser())
//          .addExecuteCode((String) linkisJob.getExecutionMap().get(LinkisKeys.KEY_CODE))
//          .setEngineTypeStr((String) linkisJob.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE))
//          .setRunTypeStr((String) linkisJob.getExecutionMap().get(LinkisKeys.KEY_CODETYPE))
//          .setStartupParams(linkisJob.getParamConfMap())
//          .setVariableMap(linkisJob.getParamVarsMap())
//          .setScriptPath((String) linkisJob.getSourceMap().get(LinkisKeys.KEY_SCRIPT_PATH))
//          .build();
//      logger.debug("Request info to Linkis Old: \n{}", Utils.GSON.toJson(jobExecuteAction));
//      jobExecuteResult = client.execute(jobExecuteAction);


            jobSubmitResult = client.submit(jobSubmitAction);
            logger.info("Response info from Linkis: \n{}", Utils.GSON.toJson(jobSubmitAction));

        } catch (Exception e) {
            //must throw if exception
            throw new ExecutorException(JobStatus.UNKNOWN, "EXE0011", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Failed to submit job", e);
        }

        if (StringUtils.isBlank(jobSubmitResult.getTaskID())) {
            throw new ExecutorException(JobStatus.UNKNOWN, "EXE0012", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Fail to get TaskId from Linkis after job submission");
        }

        return jobSubmitResult;
    }


    /**
     * loop and get job info until we success and get a valid execID
     *
     * @param user
     * @param taskID
     * @return
     */
    @Override
    public JobInfoResult queryJobInfo(String user, String taskID) throws LinkisClientRuntimeException {
        checkInit();
        JobExecuteResult executeResult = new JobExecuteResult();
        executeResult.setTaskID(taskID);
        executeResult.setUser(user);
        JobInfoResult jobInfoResult = null;
        int retryTime = 0;
        final int MAX_RETRY_TIME = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

        while (retryTime++ < MAX_RETRY_TIME) {
            try {
                jobInfoResult = client.getJobInfo(executeResult);
                logger.debug("job-info: " + Utils.GSON.toJson(jobInfoResult));
                if (jobInfoResult == null || 0 != jobInfoResult.getStatus()) {
                    String reason;
                    if (jobInfoResult == null) {
                        reason = "JobInfoResult is null";
                    } else {
                        reason = "server returns non-zero status-code";
                    }
                    String msg = MessageFormat.format("Get job info failed. retry time : {0}/{1}. taskID={0}, Reason: {1}", retryTime, MAX_RETRY_TIME, taskID, reason);

                    logger.warn("", new ExecutorException("EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Get job info failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
                if (e instanceof LinkisException) {
                    msg += " " + e.toString();
                }
                logger.warn(msg, e);
                if (retryTime >= MAX_RETRY_TIME) {
                    throw new ExecutorException("EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg, e);
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }
        if (jobInfoResult == null || 0 != jobInfoResult.getStatus()) {
            String reason;
            if (jobInfoResult == null) {
                reason = "JobInfoResult is null";
            } else {
                reason = "server returns non-zero status-code";
            }
            String msg = MessageFormat.format("Get info failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
            throw new ExecutorException("EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        }

        return jobInfoResult;
    }


    @Override
    public JobLogResult queryRunTimeLogFromLine(String user, String taskID, String execID, int fromLine) throws LinkisClientRuntimeException {
        checkInit();
        JobExecuteResult jobExecuteResult = new JobExecuteResult();
        jobExecuteResult.setUser(user);
        jobExecuteResult.setTaskID(taskID);
        jobExecuteResult.setExecID(execID);

        JobLogResult logResult = null;
        int retryTime = 0;
        final int MAX_RETRY_TIME = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

        while (retryTime++ < MAX_RETRY_TIME) {
            try {
                logResult = client.log(jobExecuteResult, fromLine, UjesClientDriverConstants.MAX_LOG_SIZE);
                logger.debug("runtime-log-result:" + Utils.GSON.toJson(logResult));
                if (logResult == null || 0 != logResult.getStatus()) {
                    String reason;
                    if (logResult == null) {
                        reason = "JobLogResult is null";
                    } else {
                        reason = "server returns non-zero status-code";
                    }
                    String msg = MessageFormat.format("Get log failed. retry time : {0}/{1}. taskID={2}. Reason: {3}", retryTime, MAX_RETRY_TIME, taskID, reason);
                    logger.warn("", new ExecutorException("EXE0015", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Get log failed. Retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
//                logger.warn("", new ExecutorException("EXE0016", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
                if (retryTime >= MAX_RETRY_TIME) {
                    throw new ExecutorException("EXE0016", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg, e);
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }
        if (logResult == null || 0 != logResult.getStatus()) {
            String reason;
            if (logResult == null) {
                reason = "JobLogResult is null";
            } else {
                reason = "server returns non-zero status-code";
            }
            String msg = MessageFormat.format("Get log failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
//            logger.warn("", new ExecutorException("EXE0016", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg));
            throw new ExecutorException("EXE0016", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        }
        return logResult;
    }

    @Override
    public OpenLogResult queryPersistedLogAll(String logPath, String user, String taskID) throws LinkisClientRuntimeException {
        checkInit();
        int retryCnt = 0;
        final int MAX_RETRY_TIMES = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;
        OpenLogResult openLogResult = null;

        int backCnt = 0;
        final int MAX_BACK_TIMES = 3;

        while (retryCnt++ < MAX_RETRY_TIMES) {
            try {
                openLogResult = client.openLog(OpenLogAction.newBuilder().setLogPath(logPath).setProxyUser(user).build());
                logger.debug("persisted-log-result:" + Utils.GSON.toJson(openLogResult));
                if (openLogResult == null ||
                        0 != openLogResult.getStatus() ||
                        StringUtils.isBlank(openLogResult.getLog()[UjesClientDriverConstants.IDX_FOR_LOG_TYPE_ALL])) {
                    String reason;
                    if (openLogResult == null) {
                        reason = "OpenLogResult is null";
                    } else if (0 != openLogResult.getStatus()) {
                        reason = "server returns non-zero status-code";
                    } else {
                        reason = "server returns empty log";
                    }
                    String msg = MessageFormat.format("Get log from openLog failed. retry time : {0}/{1}. taskID={2}. Reason: {3}", retryCnt, MAX_RETRY_TIMES, taskID, reason);
                    logger.warn(msg);
                    if (retryCnt >= MAX_RETRY_TIMES) {
                        if (backCnt >= MAX_BACK_TIMES) {
                            msg = MessageFormat.format("Get log from openLog failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
                            throw new ExecutorException("EXE0017", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
                        } else {
                            backCnt++;
                            retryCnt = 0;
                            Utils.doSleepQuietly(10000l);//wait 10s and try again
                        }
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Get log from openLog failed. retry time : {0}/{1}", retryCnt, MAX_RETRY_TIMES);
                if (e instanceof LinkisException) {
                    msg += " " + e.toString();
                }
                logger.warn(msg, e);
                if (retryCnt >= MAX_RETRY_TIMES) {
                    if (backCnt >= MAX_BACK_TIMES) {
                        throw new ExecutorException("EXE0017", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Get log from openLog failed. Retry exhausted. taskID=" + taskID, e);
                    } else {
                        backCnt++;
                        retryCnt = 0;
                        Utils.doSleepQuietly(10000l);//wait 10s and try again
                    }
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }
        return openLogResult;
    }


    @Override
    public JobProgressResult queryProgress(String user, String taskID, String execId) throws LinkisClientRuntimeException {
        checkInit();
        JobExecuteResult executeResult = new JobExecuteResult();
        executeResult.setTaskID(taskID);
        executeResult.setUser(user);
        executeResult.setExecID(execId);

        JobProgressResult jobProgressResult = null;
        int retryTime = 0;
        final int MAX_RETRY_TIME = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

        while (retryTime++ < MAX_RETRY_TIME) {
            try {
                jobProgressResult = client.progress(executeResult);
                if (jobProgressResult == null || 0 != jobProgressResult.getStatus()) {
                    String reason;
                    if (jobProgressResult == null) {
                        reason = "JobProgressResult is null";
                    } else {
                        reason = "server returns non-zero status-code";
                    }
                    String msg = MessageFormat.format("Get progress failed. retry time : {0}/{1}. taskID={2}. Reason: {3}", retryTime, MAX_RETRY_TIME, taskID, reason);
                    logger.warn(msg);
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Get progress failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
                if (e instanceof LinkisException) {
                    msg += " " + e.toString();
                }
                logger.warn(msg, e);
                if (retryTime >= MAX_RETRY_TIME) {
                    throw new ExecutorException("EXE0019", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Get progress failed. Retry exhausted. taskID=" + taskID, e);
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }

        if (jobProgressResult == null || 0 != jobProgressResult.getStatus()) {
            String reason;
            if (jobProgressResult == null) {
                reason = "JobProgressResult is null";
            } else {
                reason = "server returns non-zero status-code";
            }
            String msg = MessageFormat.format("Get progress failed. Retry exhausted. taskID={0}, Reason: {1}", taskID, reason);
            throw new ExecutorException("EXE0020", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        }

        return jobProgressResult;
    }

    @Override
    public ResultSetResult[] queryAllResults(String user, String taskID, String resultSetLocation) throws LinkisClientRuntimeException {
        String[] resultSetArray = queryResultSetPaths(user, taskID, resultSetLocation);
        ResultSetResult[] results = new ResultSetResult[resultSetArray.length];
        for (int i = 0; i < resultSetArray.length; i++) {
            results[i] = queryResultSetGivenResultSetPath(resultSetArray[i], user, null, null);
        }
        return results;
    }

    @Override
    public String[] queryResultSetPaths(String user, String taskID, String resultLocation) {
        checkInit();

        JobInfoResult jobInfoResult = queryJobInfo(user, taskID);
        if (null == jobInfoResult) {
            String msg = "Get ResultSet Failed: Cannot get a valid jobInfo";
            logger.error(msg);
            throw new ExecutorException(JobStatus.UNKNOWN, "EXE0021", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);

        }
        if (!jobInfoResult.isSucceed()) {
            String msg = "Get ResultSet Failed: job Status is not \"Succeed\", .";
            throw new ExecutorException(JobStatus.UNKNOWN, "EXE0021", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        }

        if (StringUtils.isBlank(jobInfoResult.getRequestPersistTask().getResultLocation())) {
            //sometimes server-side does not return this
            jobInfoResult.getRequestPersistTask().setResultLocation(resultLocation);
        }

        if (StringUtils.isBlank(jobInfoResult.getRequestPersistTask().getResultLocation())) {
            logger.warn("Cannot get resultLocation from linkis server. May not be able to display resultSet");
        }

        String[] resultSetArray = null;

        int retryTime = 0;
        final int MAX_RETRY_TIME = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

        while (retryTime++ < MAX_RETRY_TIME) {
            try {
                resultSetArray = jobInfoResult.getResultSetList(client); //this makes call to server
                if (resultSetArray == null || 0 == resultSetArray.length) {
                    String reason;
                    if (resultSetArray == null) {
                        reason = "array is null";
                    } else {
                        reason = "array length is zero";
                    }
                    String msg = MessageFormat.format("Get resultSetArray failed. retry time : {0}/{1}. taskID={2} Reason: {3}", retryTime, MAX_RETRY_TIME, taskID, reason);
                    logger.warn(msg);
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Get resultSetArray failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
                if (e instanceof LinkisException) {
                    msg += " " + e.toString();
                }
                logger.warn(msg, e);
                if (retryTime >= MAX_RETRY_TIME) {
                    throw new ExecutorException(null, "EXE0022", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Get resultSetArray failed. Retry exhausted. taskID=" + taskID, e);
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }
        if (resultSetArray == null || 0 == resultSetArray.length) {
            String reason;
            if (resultSetArray == null) {
                reason = "array is null";
            } else {
                reason = "array length is zero";
            }
            String msg = MessageFormat.format("Get resultSetArray failed. retry exhausted. taskID={0}. Reason: {1}", taskID, reason);
            logger.warn(msg);
            throw new ExecutorException("EXE0023", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg + ". taskID=" + taskID);
        }
        return resultSetArray;
    }

    @Override
    public ResultSetResult queryResultSetGivenResultSetPath(String resultSetPath, String user, Integer page, Integer pageSize) {
        checkInit();
        int retryTime = 0;
        final int MAX_RETRY_TIME = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;
        ResultSetResult result = null;

        while (retryTime++ < MAX_RETRY_TIME) {
            try {
                ResultSetAction action = ResultSetAction.builder()
                        .setPath(resultSetPath).setUser(user)
                        .setPage(page).setPageSize(pageSize)
                        .build();
                result = client.resultSet(action);
                logger.debug("resultset-result:" + Utils.GSON.toJson(result));
                if (result == null || 0 != result.getStatus()) {
                    String reason;
                    if (result == null) {
                        reason = "array is null";
                    } else {
                        reason = "server returns non-zero status-code";
                    }
                    String msg = MessageFormat.format("Get resultSet failed. retry time : {0}/{1}. path={2}, Reason: {3}", retryTime, MAX_RETRY_TIME, resultSetPath, reason);
                    logger.warn(msg);
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Get resultSet failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
                if (e instanceof LinkisException) {
                    msg += " " + e.toString();
                }
                logger.warn(msg, e);
                if (retryTime >= MAX_RETRY_TIME) {
                    throw new ExecutorException("EXE0024", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Get resultSet failed. Retry exhausted. path=" + resultSetPath, e);
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }
        if (result == null || 0 != result.getStatus()) {
            String reason;
            if (result == null) {
                reason = "ResultSetResult is null";
            } else {
                reason = "server returns non-zero status-code";
            }
            String msg = MessageFormat.format("Get resultSet failed. Retry exhausted. Path={0}, Reason: {1}", resultSetPath, reason);
            throw new ExecutorException("EXE0024", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        }
        return result;
    }

    @Override
    public JobKillResult kill(String user, String taskId, String execId) throws LinkisClientRuntimeException {
        checkInit();

        int retryTime = 0;
        final int MAX_RETRY_TIME = UjesClientDriverConstants.DRIVER_REQUEST_MAX_RETRY_TIME;

        JobKillResult result = null;

        while (retryTime++ < MAX_RETRY_TIME) {
            try {
                JobExecuteResult killRequest = new JobExecuteResult();
                killRequest.setUser(user);
                killRequest.setTaskID(taskId);
                killRequest.setExecID(execId);
                result = client.kill(killRequest);
                logger.debug("job-kill-result:" + Utils.GSON.toJson(result));
                if (result == null || 0 != result.getStatus()) {
                    String reason;
                    if (result == null) {
                        reason = "result is null";
                    } else {
                        reason = "server returns non-zero status-code";
                    }
                    String msg = MessageFormat.format("Kill job failed. retry time : {0}/{1}. taskId={2}, Reason: {3}", retryTime, MAX_RETRY_TIME, taskId, reason);
                    logger.warn(msg);
                } else {
                    break;
                }
            } catch (Exception e) {
                String msg = MessageFormat.format("Kill job failed. retry time : {0}/{1}", retryTime, MAX_RETRY_TIME);
                if (e instanceof LinkisException) {
                    msg += " " + e.toString();
                }
                logger.warn(msg, e);
                if (retryTime >= MAX_RETRY_TIME) {
                    throw new ExecutorException("EXE0025", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Kill job failed. taskId={0} Retry exhausted.", taskId, e);
                }
            }
            Utils.doSleepQuietly(UjesClientDriverConstants.DRIVER_QUERY_SLEEP_MILLS);
        }
        if (result == null || 0 != result.getStatus()) {
            String reason;
            if (result == null) {
                reason = "result is null";
            } else {
                reason = "server returns non-zero status-code";
            }
            String msg = MessageFormat.format("Kill job failed. Retry exhausted. taskId={0}, Reason: {1}", taskId, reason);
            throw new ExecutorException("EXE0025", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        }
        return result;
    }

    @Override
    public UjesClientDriverContext getContext() {
        return context;
    }
}