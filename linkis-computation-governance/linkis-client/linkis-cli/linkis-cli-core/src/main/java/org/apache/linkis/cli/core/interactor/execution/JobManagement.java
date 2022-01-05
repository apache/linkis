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
import org.apache.linkis.cli.common.entity.execution.SubExecutionType;
import org.apache.linkis.cli.common.entity.execution.executor.Executor;
import org.apache.linkis.cli.common.entity.execution.jobexec.ExecutionStatus;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobStatus;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.executor.JobManagableBackendExecutor;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobManExec;

/**
 * TODO: put exception during execution in ExecutionResult and do not interrupt execution
 */
public class JobManagement implements Execution {

    private ExecutionStatus executionStatus = ExecutionStatus.UNDEFINED;

    @Override
    public ExecutionResult execute(Executor executor, Job job) {
        if (!(executor instanceof JobManagableBackendExecutor)) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0004", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "Executor \"" + this.getClass().getCanonicalName() + "\" is not JobManagableBackendExecutor");
        }

        JobManExec resultData = null;
        ExecutionStatus executionStatus;
        Exception exception = null; //TODO

        JobManagableBackendExecutor jobManExecutor = (JobManagableBackendExecutor) executor;
        SubExecutionType subExecutionType = job.getSubExecutionType();
        if (!(subExecutionType instanceof JobManSubType)) {
            throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0030", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "SubExecutionType is not instance of JobManSubType");
        }
        switch ((JobManSubType) subExecutionType) {
            case STATUS:
                try {
                    resultData = jobManExecutor.queryJobInfo(job);
                    if (resultData == null || !resultData.isSuccess()) {
                        executionStatus = ExecutionStatus.FAILED;
                    } else {
                        executionStatus = ExecutionStatus.SUCCEED;
                    }
                } catch (Exception e) {
                    executionStatus = ExecutionStatus.FAILED;
                }
                break;
//            case JOB_DESC:
//                result = jobManagableBackendExecutor.queryJobDesc(job);
//                break;
//            case LOG:
//                result = jobManagableBackendExecutor.queryJobLog(job);
//                break;
//            case LIST:
//                result = jobManagableBackendExecutor.queryJobList(job);
//                break;
            case KILL:
                try {
                    resultData = jobManExecutor.killJob(job);
                    if (resultData == null || !resultData.isSuccess()) {
                        executionStatus = ExecutionStatus.FAILED;
                    } else {
                        executionStatus = ExecutionStatus.SUCCEED;
                    }
                } catch (Exception e) {
                    executionStatus = ExecutionStatus.FAILED;
                }
                break;
            default:
                throw new ExecutorException(JobStatus.UNSUBMITTED, "EXE0002", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "SubExecutionType + \"" + job.getSubExecutionType() + "\" is not supported");
        }
        return new ExecutionResultImpl(resultData, executionStatus, exception);
    }

    @Override
    public boolean terminate(Executor executor, Job job) {
        return true;
    }


}
