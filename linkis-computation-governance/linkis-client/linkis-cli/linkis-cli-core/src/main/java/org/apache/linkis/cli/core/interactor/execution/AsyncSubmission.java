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
import org.apache.linkis.cli.core.interactor.job.AsyncBackendJob;
import org.apache.linkis.cli.core.interactor.result.ExecutionResultImpl;
import org.apache.linkis.cli.core.interactor.result.ExecutionStatusEnum;
import org.apache.linkis.cli.core.utils.CommonUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute job asynchronously TODO: put exception during execution in ExecutionResult and do not
 * interrupt execution
 */
public class AsyncSubmission implements Execution {
  private static final Logger logger = LoggerFactory.getLogger(AsyncSubmission.class);

  @Override
  public ExecutionResult execute(Map<String, Job> jobs) {
    ExecutionStatus executionStatus;
    Exception exception = null; // TODO

    if (jobs == null || jobs.size() == 0) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Null or empty Jobs is submitted to current execution");
    }

    if (jobs.size() > 1) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Multiple Jobs is not Supported by current execution");
    }

    Job job = jobs.get(jobs.keySet().toArray(new String[jobs.size()])[0]);

    if (!(job instanceof AsyncBackendJob)) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Backend for \"" + job.getClass().getCanonicalName() + "\" does not support async");
    }

    if (job.getSubType() == null) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "SubExecType should not be null");
    }

    try {
      ((AsyncBackendJob) job).submit();
      CommonUtils.doSleepQuietly(CommonConstants.JOB_QUERY_SLEEP_MILLS);
      ((AsyncBackendJob) job).updateJobStatus();
      if (job.getJobData().getJobStatus().isJobSubmitted()) {
        executionStatus = ExecutionStatusEnum.SUCCEED;
      } else {
        executionStatus = ExecutionStatusEnum.FAILED;
        if (job.getJobData().getException() != null) {
          exception = job.getJobData().getException();
        }
      }
    } catch (Exception e) {
      exception = e;
      executionStatus = ExecutionStatusEnum.FAILED;
      logger.warn("Failed to submit job.", e);
    }

    return new ExecutionResultImpl(jobs, executionStatus, exception);
  }

  @Override
  public boolean terminate(Map<String, Job> jobs) {
    return true;
  }
}
