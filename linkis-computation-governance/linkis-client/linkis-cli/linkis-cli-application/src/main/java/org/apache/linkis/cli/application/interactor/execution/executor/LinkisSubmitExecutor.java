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

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.driver.LinkisClientDriver;
import org.apache.linkis.cli.application.driver.transformer.DriverTransformer;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobKill;
import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobSubmitExec;
import org.apache.linkis.cli.application.interactor.job.LinkisJob;
import org.apache.linkis.cli.application.interactor.job.LinkisJobMan;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobExec;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.executor.AsyncBackendExecutor;
import org.apache.linkis.cli.core.interactor.execution.executor.LogRetrievable;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobSubmitExec;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @description: Executor that encapsulates methods needed to submit task Linkis
 */
public class LinkisSubmitExecutor implements AsyncBackendExecutor, LogRetrievable {
    private final static Logger logger = LoggerFactory.getLogger(LinkisSubmitExecutor.class);

    LinkisClientDriver driver;
    DriverTransformer driverTransformer; // currently just use this. consider modify to interface later

    private String taskIdCache;

    public void setDriver(LinkisClientDriver driver) {
        this.driver = driver;
    }

    public void setDriverTransformer(DriverTransformer driverTransformer) {
        this.driverTransformer = driverTransformer;
    }

    @Override
    public JobSubmitExec submit(Job job) throws LinkisClientRuntimeException {
        if (!(job instanceof LinkisJob)) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0025", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Input job is not instance of LinkisJob!");
        }
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("connecting to linkis gateway:").append(driver.getContext().getGatewayUrl());
        LogUtils.getInformationLogger().info(infoBuilder.toString());
        JobSubmitExec execData = new LinkisJobSubmitExec();
        execData.setOutputWay(job.getOutputWay());
        execData.setOutputPath(job.getOutputPath());
        DWSResult jobSubmitResult = driver.submit((LinkisJob) job);
        execData = updateExecDataByDwsResult(execData, jobSubmitResult);
        infoBuilder.setLength(0);
        infoBuilder.append("JobId:").append(execData.getJobID());
        taskIdCache = execData.getJobID();
        LogUtils.getPlaintTextLogger().info(infoBuilder.toString());
        return execData;
    }

    /**
     * update job status and progress
     */
    @Override
    public JobSubmitExec updateJobStatus(JobSubmitExec execData) throws LinkisClientRuntimeException {

        DWSResult jobInfoResult = driver.queryJobInfo(
                ((LinkisJobSubmitExec) execData).getUser(),
                ((LinkisJobSubmitExec) execData).getTaskID()
        );
        execData = updateExecDataByDwsResult(execData, jobInfoResult);
        if (StringUtils.isBlank(taskIdCache)) {
            taskIdCache = execData.getJobID();
        }

        String log2 = "\n---------------------------------------------------\n" +
                "\ttask " + ((LinkisJobSubmitExec) execData).getTaskID() +
                " status is " + execData.getJobStatus() +
                ", progress : " + ((LinkisJobSubmitExec) execData).getJobProgress() +
                "\n---------------------------------------------------";
        logger.info(log2);
        return execData;
    }

    @Override
    public JobSubmitExec checkSubmit(JobSubmitExec execData) throws LinkisClientRuntimeException {

        LinkisJobSubmitExec linkisData = (LinkisJobSubmitExec) execData;

        if (StringUtils.isBlank(linkisData.getTaskID())) {
            logger.error("Job Failed: empty taskID");
            execData.setJobStatus(JobStatus.UNKNOWN);
            throw new ExecutorException(execData.getJobStatus(), "EXE0026", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Job Failed: empty taskID");
        }

        if (!linkisData.isJobSubmitted()) {
            try {
                DWSResult jobInfoResult = driver.queryJobInfo(linkisData.getUser(), linkisData.getTaskID());
                logger.info("Jobinfo from Linkis: \n{}", Utils.GSON.toJson(jobInfoResult));
                linkisData = updateExecDataByDwsResult(linkisData, jobInfoResult);
            } catch (Exception e) {
                logger.warn("", e);
            }

            if (!Utils.isValidExecId(linkisData.getExecID())) {
                /**
                 * this should not happen
                 */
                int retry = 0;
                final int MAX_RETRY = 3;
                while (retry++ < MAX_RETRY) {
                    Utils.doSleepQuietly(AppConstants.JOB_QUERY_SLEEP_MILLS);
                    String msg = "Linkis ExecID is empty or not valid, which should not happen. Now try get it.";
                    logger.warn(msg);

                    try {
                        DWSResult jobInfoResult = driver.queryJobInfo(linkisData.getUser(), linkisData.getTaskID());
                        linkisData = updateExecDataByDwsResult(linkisData, jobInfoResult);
                    } catch (Exception e) {
                        logger.warn("", e);
                    }

                    if (Utils.isValidExecId(linkisData.getExecID())) {
                        msg = "Updated execID=" + linkisData.getExecID();
                        logger.info(msg);
                        break;
                    }
                }
            }
            if (!Utils.isValidExecId(linkisData.getExecID())) {
                linkisData.setJobStatus(JobStatus.UNKNOWN);
            }

            logger.info("Job submission status: \n\tFinal status={}\n\tUpdated taskID={} \n\tExecID={}",
                    linkisData.getJobStatus(),
                    linkisData.getTaskID(),
                    linkisData.getExecID()
            );
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("TaskId:").append(linkisData.getTaskID()).append(System.lineSeparator())
                    .append("ExecId:").append(linkisData.getExecID());
            LogUtils.getPlaintTextLogger().info(infoBuilder.toString());
        }
        return linkisData;
    }

    @Override
    public JobSubmitExec doGetFinalResult(JobSubmitExec execData) throws LinkisClientRuntimeException {
        if (!(execData instanceof LinkisJobSubmitExec)) {
            execData.setJobStatus(JobStatus.UNKNOWN);
            throw new ExecutorException(execData.getJobStatus(), "EXE0027", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Input execData is not instance of LinkisJobSubmitExec!");
        }
        String[] resultSetPaths = driver.queryResultSetPaths(
                ((LinkisJobSubmitExec) execData).getUser(),
                ((LinkisJobSubmitExec) execData).getTaskID(),
                ((LinkisJobSubmitExec) execData).getResultLocation()
        );

        ((LinkisJobSubmitExec) execData).setResultSetPaths(resultSetPaths);
        return execData;
    }


    public void checkInit() {
        if (driver == null ||
                driverTransformer == null) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0028", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "LinkisSubmitExecutor is not inited." + Utils.GSON.toJson(this));
        }
        driver.checkInit();
    }

    private LinkisJobSubmitExec updateExecDataByDwsResult(JobExec execData, DWSResult result) {
        JobExec data = driverTransformer.convertAndUpdateExecData(execData, result);
        if (!(data instanceof LinkisJobSubmitExec)) {
            throw new ExecutorException(JobStatus.UNKNOWN, "EXE0027", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Input execData is not instance of LinkisJobSubmitExec!");
        }
        return (LinkisJobSubmitExec) data;
    }


    @Override
    public boolean terminate(Job job) {
        //kill job if job is submitted
        if (StringUtils.isNotBlank(taskIdCache)) {
            System.out.println("\nKilling job: " + taskIdCache);
            LinkisJobManageExecutor jobManageExecutor = new LinkisJobManageExecutor();
            jobManageExecutor.setDriver(this.driver);
            jobManageExecutor.setDriverTransformer(this.driverTransformer);
            LinkisJobMan linkisJobMan = new LinkisJobMan();
            linkisJobMan.setJobId(taskIdCache);
            linkisJobMan.setSubmitUser(job.getSubmitUser());
            linkisJobMan.setProxyUser(job.getProxyUser());
            try {
                LinkisJobKill result = (LinkisJobKill) jobManageExecutor.killJob(linkisJobMan);
                if (result.isSuccess()) {
                    System.out.println("Successfully killed job: " + taskIdCache + " on exit");
                } else {
                    System.out.println("Failed to kill job: " + taskIdCache + " on exit. Current job status: " + result.getJobStatus().name());
                }
            } catch (Exception e) {
                System.out.println("Failed to kill job: " + taskIdCache + " on exit");
                System.out.println(ExceptionUtils.getStackTrace(e));
                return false;
            }
        }
        return true;
    }

}