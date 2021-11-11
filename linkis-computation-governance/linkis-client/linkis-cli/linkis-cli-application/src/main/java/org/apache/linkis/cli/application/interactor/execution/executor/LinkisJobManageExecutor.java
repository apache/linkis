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
 
package org.apache.linkis.cli.application.interactor.execution.executor;

import org.apache.linkis.cli.application.driver.LinkisClientDriver;
import org.apache.linkis.cli.application.driver.transformer.DriverTransformer;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobInfo;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobKill;
import org.apache.linkis.cli.application.interactor.job.LinkisJobMan;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobExec;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.constants.Constants;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.executor.JobManagableBackendExecutor;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobManExec;
import org.apache.linkis.cli.core.utils.CommonUtils;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/*
 * TODO: combine this with LinkisSubmitExecutor into LinkisExecutor
 */
public class LinkisJobManageExecutor implements JobManagableBackendExecutor {
    private final static Logger logger = LoggerFactory.getLogger(LinkisJobManageExecutor.class);

    LinkisClientDriver driver;
    DriverTransformer driverTransformer; // currently just use this. consider modify to interface later

    public void setDriver(LinkisClientDriver driver) {
        this.driver = driver;
    }

    public void setDriverTransformer(DriverTransformer driverTransformer) {
        this.driverTransformer = driverTransformer;
    }

    @Override
    public JobManExec queryJobInfo(Job job) throws LinkisClientRuntimeException {
        LinkisJobInfo linkisJobInfo = new LinkisJobInfo();
        linkisJobInfo.setJobID(((LinkisJobMan) job).getJobId());
        try {
            DWSResult jobInfoResult = driver.queryJobInfo(
                    job.getProxyUser(),
                    ((LinkisJobMan) job).getJobId()
            );
            linkisJobInfo = (LinkisJobInfo) updateExecDataByDwsResult(linkisJobInfo, jobInfoResult);
        } catch (Exception e) {
            linkisJobInfo.setSuccess(false);
            linkisJobInfo.setMessage("Exception thrown when trying to query job info");
            linkisJobInfo.setException(e);
            return linkisJobInfo;
        }
        linkisJobInfo.setSuccess(true);
        return linkisJobInfo;
    }

    @Override
    public JobManExec queryJobDesc(Job job) throws LinkisClientRuntimeException {
        //TODO
        return null;
    }

    @Override
    public JobManExec queryJobLog(Job job) throws LinkisClientRuntimeException {
        //TODO
        return null;
    }

    @Override
    public JobManExec queryJobResult(Job job) throws LinkisClientRuntimeException {
        //TODO
        JobManExec execData = this.queryJobInfo(job);
        if (!(execData instanceof LinkisJobInfo)) {
            execData.setSuccess(false);
            throw new ExecutorException("EXE0027", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Input execData is not instance of LinkisJobInfo!");
        }
//        String[] resultSetPaths = driver.queryResultSetPaths(
//                ((LinkisJobSubmitExec) execData).getUser(),
//                ((LinkisJobSubmitExec) execData).getTaskID(),
//                ((LinkisJobSubmitExec) execData).getResultLocation()
//        );
//
//        ((LinkisJobSubmitExec) execData).setResultSetPaths(resultSetPaths);
        return null;
    }

    @Override
    public JobManExec queryJobList(Job job) throws LinkisClientRuntimeException {
        //TODO
        return null;
    }

    @Override
    public JobManExec killJob(Job job) throws LinkisClientRuntimeException {
        LinkisJobKill killData = new LinkisJobKill();
        killData.setJobID(((LinkisJobMan) job).getJobId());
        DWSResult jobInfoResult;
        try {
            jobInfoResult = driver.queryJobInfo(
                    job.getProxyUser(),
                    ((LinkisJobMan) job).getJobId()
            );
            killData = (LinkisJobKill) updateExecDataByDwsResult(killData, jobInfoResult);
        } catch (Exception e) {
            killData.setSuccess(false);
            killData.setMessage("Cannot find job status from Linkis.");
            killData.setException(e);
            return killData;
        }

        String msg;
        if (killData.isJobCancelled()) {
            msg = "Kill job aborted: Job has already been canceled.";
            killData.setSuccess(false);
            killData.setMessage(msg);
        } else if (killData.isJobCompleted()) {
            msg = "Kill job aborted: Job has already completed.";
            killData.setSuccess(false);
            killData.setMessage(msg);
//            throw new ExecutorException(JobStatus.FAILED, "EXE0004", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
        } else {
            try {
                driver.kill(killData.getUser(), killData.getTaskID(), killData.getExecID());
            } catch (Exception e) {
                killData.setSuccess(false);
                killData.setMessage("Exception thrown when trying to send kill request");
                killData.setException(e);
                return killData;
            }
            msg = "Kill request has been sent";
            LogUtils.getPlaintTextLogger().info(msg);
            int retryCnt = 0;
            final int MAX_RETRY = 10;
            while (!killData.isJobCompleted() && !killData.isJobCancelled()) {
                CommonUtils.doSleepQuietly(Constants.JOB_QUERY_SLEEP_MILLS);
                try {
                    jobInfoResult = driver.queryJobInfo(
                            job.getProxyUser(),
                            ((LinkisJobMan) job).getJobId()
                    );
                    retryCnt = 0; //if exception then will not go here
                } catch (Exception e) {
                    retryCnt++;
                    CommonUtils.doSleepQuietly(5 * Constants.JOB_QUERY_SLEEP_MILLS);
                    if (retryCnt >= MAX_RETRY) {
                        killData.setSuccess(false);
                        killData.setMessage(MessageFormat.format("After send kill. Client cannot get jobStatus from server continuously for {0} seconds. Client aborted. Assume kill failed! Error message: \n", MAX_RETRY * 5 * Constants.JOB_QUERY_SLEEP_MILLS));
                        killData.setException(e);
                        return killData;
                    }
                }

                killData = (LinkisJobKill) updateExecDataByDwsResult(killData, jobInfoResult);
            }
            if (killData.isJobCompleted() && !killData.isJobCancelled()) {
                msg = "Kill job aborted: Job has already completed.";
                killData.setSuccess(false);
                killData.setMessage(msg);
//                throw new ExecutorException(JobStatus.FAILED, "EXE0004", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
            } else if (killData.isJobCancelled()) {
                msg = MessageFormat.format(
                        "Kill successful: jobId={0}, status={1}.",
                        killData.getJobID(),
                        killData.getJobStatus());
                killData.setSuccess(true);
                killData.setMessage(msg);
//                LogUtils.getPlaintTextLogger().info(msg);
            }

        }

        return killData;
    }

    private JobManExec updateExecDataByDwsResult(JobManExec execData, DWSResult result) {
        JobExec data = driverTransformer.convertAndUpdateExecData(execData, result);
        if (!(data instanceof JobManExec)) {
            throw new ExecutorException("EXE0032", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Failed to convert DWSResult into JobManExec");
        }
        return (JobManExec) data;
    }

    @Override
    public boolean terminate(Job job) {
        return true;
    }
}
