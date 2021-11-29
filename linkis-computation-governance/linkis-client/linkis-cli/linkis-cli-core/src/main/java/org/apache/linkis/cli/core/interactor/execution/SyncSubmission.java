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
 
package org.apache.linkis.cli.core.interactor.execution;

import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.execution.ExecutionResult;
import org.apache.linkis.cli.common.entity.execution.executor.Executor;
import org.apache.linkis.cli.common.entity.execution.jobexec.ExecutionStatus;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.constants.Constants;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.executor.AsyncBackendExecutor;
import org.apache.linkis.cli.core.interactor.execution.executor.LogRetrievable;
import org.apache.linkis.cli.core.interactor.execution.executor.SyncBackendExecutor;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobSubmitExec;
import org.apache.linkis.cli.core.interactor.execution.observer.event.IncLogEvent;
import org.apache.linkis.cli.core.interactor.execution.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.core.interactor.execution.observer.listener.LinkisClientObserver;
import org.apache.linkis.cli.core.interactor.execution.observer.listener.TriggerObserver;
import org.apache.linkis.cli.core.utils.CommonUtils;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute job synchronously
 * TODO: put exception during execution in ExecutionResult and do not interrupt execution
 */
public class SyncSubmission implements Execution {
    private final static Logger logger = LoggerFactory.getLogger(SyncSubmission.class);
    ExecutionStatus executionStatus = ExecutionStatus.UNDEFINED;
    private LinkisClientEvent incLogEvent = new IncLogEvent();
    private TriggerObserver incLogFinObserver = new TriggerObserver();

    public void checkInit() {
        if (incLogEvent == null ||
                incLogFinObserver == null) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0001", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "Executor is not properly inited:");
        }
    }

    public SyncSubmission registerIncLogEventListener(LinkisClientObserver listener) {
        incLogEvent.register(listener);
        return this;
    }

    public SyncSubmission getIncLogFinObserverRegistered(LinkisClientEvent event) {
        event.register(incLogFinObserver);
        return this;
    }

    @Override
    public ExecutionResult execute(Executor executor, Job job) {
        JobSubmitExec resultData;
        ExecutionStatus executionStatus;
        Exception exception = null; //TODO

        if (executor instanceof SyncBackendExecutor) {
            resultData = ((SyncBackendExecutor) executor).submitAndGetResult(job);
        } else if (executor instanceof AsyncBackendExecutor) {
            resultData = ExecWithAsyncBackend((AsyncBackendExecutor) executor, job);
        } else {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0004", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Executor Type: \"" + this.getClass().getCanonicalName() + "\" is not Supported");
        }

        if (resultData.isJobSuccess()) {
            executionStatus = ExecutionStatus.SUCCEED;
        } else if (!resultData.isJobCompleted()) {
            executionStatus = ExecutionStatus.UNDEFINED;
        } else {
            executionStatus = ExecutionStatus.FAILED;
        }
        return new ExecutionResultImpl(resultData, executionStatus, exception);
    }

    private JobSubmitExec ExecWithAsyncBackend(AsyncBackendExecutor executor, Job job) {
        JobSubmitExec result = null;

        JobSubmitExec execData = executor.submit(job);
        CommonUtils.doSleepQuietly(Constants.JOB_QUERY_SLEEP_MILLS);
        execData = executor.checkSubmit(execData);
        checkJobAvailability(execData);

        if (!execData.isJobSubmitted()) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0005", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Retry exhausted checking job submission. Job is probably not submitted");
        } else {
            //Output that job is submitted
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("Job is successfully submitted!").append(System.lineSeparator());
            LogUtils.getInformationLogger().info(infoBuilder.toString());
        }

    /*
      Inform observer to start presenting inclog asynchronously
      will automatically clone data to avoid race condition
     */

        if (executor instanceof LogRetrievable) {
            this.incLogEvent.notifyObserver(this.incLogEvent, execData);
        }

        int retryCnt = 0;
        final int MAX_RETRY = 30;
        while (!execData.isJobCompleted()) {
            //query progress
            try {
                execData = executor.updateJobStatus(execData);
            } catch (Exception e) {
                logger.warn("", e);
                retryCnt++;
                if (retryCnt >= MAX_RETRY) {
                    throw new ExecutorException("EXE0013", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Cannot get jobStatus from server continuously for {0} seconds. Client aborted! Error message: \n", MAX_RETRY * 5 * Constants.JOB_QUERY_SLEEP_MILLS, e);
                }
                CommonUtils.doSleepQuietly(5 * Constants.JOB_QUERY_SLEEP_MILLS); //maybe server problem. sleep longer
                continue;
            }
            retryCnt++;
            checkJobAvailability(execData);
//      execData = asyncBackendExecutorExecutor.doIncLog(execData);
            CommonUtils.doSleepQuietly(Constants.JOB_QUERY_SLEEP_MILLS);
        }
//    while (!execData.isLogFinish()) {
//      //remained logs
//      execData = asyncBackendExecutorExecutor.doIncLog(execData);
//    }
        if (execData.isJobSuccess()) {
            executionStatus = ExecutionStatus.SUCCEED;
        } else {
            executionStatus = ExecutionStatus.FAILED;
        }


    /*
      Output message: success/fail
     */
        try {
            result = executor.doGetFinalResult(execData);
        } catch (Exception e) {
            logger.warn("Exception thrown when trying to query final result. Status will change to FAILED", e);
            // job state may change after submission
            try {
                result = executor.updateJobStatus(execData);
            } catch (Exception e2) {
                logger.warn("", e);
                // shouldn't go here
                result.setJobStatus(JobStatus.UNKNOWN);
            }
        }
    /*
      Inform observer to start handling result
      will automatically clone data to avoid potential race condition
    */
        waitIncLogComplete();

        return result;
    }

    private void checkJobAvailability(JobSubmitExec execData) throws LinkisClientRuntimeException {
        if (execData.isJobAbnormalStatus()) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0006", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Job is in abnormal status: " + CommonUtils.GSON.toJson(execData));
        }
    }

    private void waitIncLogComplete() {
        int retry = 0;
        int MAX_RETRY = 300; //wait for 10 minutes after job finish
        while (retry++ < MAX_RETRY) {
            if (incLogFinObserver.isTriggered()) {
                return;
            }
            CommonUtils.doSleepQuietly(Constants.JOB_QUERY_SLEEP_MILLS);
        }
        String msg = "Job is completed but client keep querying inclog for " + (MAX_RETRY * Constants.JOB_QUERY_SLEEP_MILLS / 1000) + "seconds. Execution ends forcefully. Next will try handle execution result.";
        logger.warn(msg);
        LogUtils.getInformationLogger().warn(msg);
    }

    @Override
    public boolean terminate(Executor executor, Job job) {
        return executor.terminate(job);
    }

}
