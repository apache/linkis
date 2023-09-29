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

package org.apache.linkis.cli.core.interactor.execution;

import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.result.ExecutionResult;
import org.apache.linkis.cli.common.entity.result.ExecutionStatus;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.constants.CommonConstants;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.job.*;
import org.apache.linkis.cli.core.interactor.result.ExecutionResultImpl;
import org.apache.linkis.cli.core.interactor.result.ExecutionStatusEnum;
import org.apache.linkis.cli.core.utils.CommonUtils;
import org.apache.linkis.cli.core.utils.LogUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute job synchronously. i.e. Client submit job, and wait til job finish, and get result, no
 * matter what server behaves.
 */
public class SyncSubmission implements Execution {
  private static final Logger logger = LoggerFactory.getLogger(SyncSubmission.class);

  @Override
  public ExecutionResult execute(Map<String, Job> jobs) {

    ExecutionStatus executionStatus;
    Exception exception = null; // TODO

    if (jobs == null || jobs.size() == 0) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "Null or empty Jobs is submitted to current execution");
    }

    if (jobs.size() > 1) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "Multiple Jobs is not Supported by current execution");
    }

    Job job = jobs.get(jobs.keySet().toArray(new String[jobs.size()])[0]);

    if (job instanceof SyncBackendJob) {
      try {
        ((SyncBackendJob) job).submitAndGetResult();
      } catch (Exception e) {
        exception = e;
      }
    } else if (job instanceof AsyncBackendJob) {
      try {
        ExecWithAsyncBackend(job);
      } catch (Exception e) {
        exception = e;
        // TODO: throw or fail
      }
    } else {
      throw new LinkisClientExecutionException(
          "EXE0002",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "Executor Type: \"" + job.getClass().getCanonicalName() + "\" is not Supported");
    }

    if (job.getJobData() != null
        && job.getJobData().getJobStatus() != null
        && job.getJobData().getJobStatus().isJobSuccess()) {
      executionStatus = ExecutionStatusEnum.SUCCEED;
    } else if (job.getJobData().getJobStatus() == null
        || !job.getJobData().getJobStatus().isJobFinishedState()) {
      executionStatus = ExecutionStatusEnum.UNDEFINED;
      if (job.getJobData().getException() != null) {
        exception = job.getJobData().getException();
      }
    } else {
      executionStatus = ExecutionStatusEnum.FAILED;
      if (job.getJobData().getException() != null) {
        exception = job.getJobData().getException();
      }
    }

    return new ExecutionResultImpl(jobs, executionStatus, exception);
  }

  @Override
  public boolean terminate(Map<String, Job> jobs) {
    boolean ok = true;
    for (Job job : jobs.values()) {
      if (job.getJobData() == null || job.getJobData().getJobStatus() == null) {
        continue;
      }
      String jobId = job.getJobData().getJobID() == null ? "NULL" : job.getJobData().getJobID();
      if (job instanceof TerminatableJob) {
        try {
          ((TerminatableJob) job).terminate();
        } catch (Exception e) {
          System.out.println(
              "Failed to kill job: jobId=" + jobId + ". " + ExceptionUtils.getStackTrace(e));
        }
        if (!job.getJobData().getJobStatus().isJobCancelled()
            || !job.getJobData().getJobStatus().isJobFailure()) {
          ok = false;
          System.out.println(
              "Failed to kill job: jobId="
                  + jobId
                  + ", current status: "
                  + job.getJobData().getJobStatus().toString());
        } else {
          System.out.println(
              "Successfully killed job: jobId="
                  + jobId
                  + ", current status: "
                  + job.getJobData().getJobStatus().toString());
        }
      } else {
        System.out.println("Job \"" + jobId + "\"" + "is not terminatable");
      }
    }
    return ok;
  }

  private void ExecWithAsyncBackend(Job job) {

    if (!(job instanceof AsyncBackendJob)) {
      throw new LinkisClientExecutionException(
          "EXE0002",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "job is not instance of AsyncBackendJob");
    }
    AsyncBackendJob submitJob = (AsyncBackendJob) job;

    submitJob.submit();
    CommonUtils.doSleepQuietly(CommonConstants.JOB_QUERY_SLEEP_MILLS);

    if (!submitJob.getJobData().getJobStatus().isJobSubmitted()) {
      throw new LinkisClientExecutionException(
          "EXE0005",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "Retry exhausted checking job submission. Job is probably not submitted");
    } else {
      // Output that job is submitted
      StringBuilder infoBuilder = new StringBuilder();
      infoBuilder.append("Job is successfully submitted!").append(System.lineSeparator());
      LogUtils.getInformationLogger().info(infoBuilder.toString());
    }

    if (job instanceof LogAccessibleJob) {
      /*
         Non-blocking, call if back-end supports it
      */
      ((LogAccessibleJob) job).startRetrieveLog();
    }

    submitJob.waitJobComplete();

    if (submitJob.getJobData().getJobStatus().isJobFinishedState()) {
      if (job instanceof ResultAccessibleJob) {
        /*
           Non-blocking, call if back-end supports it
        */
        ((ResultAccessibleJob) job).startRetrieveResult();
      }
    }
  }
}
