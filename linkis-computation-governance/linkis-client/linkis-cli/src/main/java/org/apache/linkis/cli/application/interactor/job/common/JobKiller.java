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

package org.apache.linkis.cli.application.interactor.job.common;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOper;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.MessageFormat;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobKiller {
  private static final Logger logger = LoggerFactory.getLogger(JobKiller.class);

  private LinkisJobOper oper;

  public JobKiller(LinkisJobOper linkisJobOperator) {
    this.oper = linkisJobOperator;
  }

  public KillResult doKill(String username, String jobId) {

    LinkisOperResultAdapter jobInfoResult;
    try {
      jobInfoResult = oper.queryJobInfo(username, jobId);
    } catch (Exception e) {
      return new KillResult(
          true, "Failed to query jobInfo" + ExceptionUtils.getStackTrace(e), new HashMap<>());
    }
    if (jobInfoResult.getUser() == null || jobInfoResult.getJobID() == null) {
      return new KillResult(false, "user or jobID is null", new HashMap<>());
    }
    if (jobInfoResult.getJobStatus() == null) {
      return new KillResult(false, "jobStatus is null", new HashMap<>());
    }
    if (jobInfoResult.getJobStatus().isJobCancelled()) {
      String msg = "Kill job aborted: Job has already been canceled.";
      return new KillResult(false, msg, new HashMap<>());
    } else if (jobInfoResult.getJobStatus().isJobFinishedState()) {
      String msg = "Kill job aborted: Job is already in finished-state(SUCCEED/FAILED).";
      return new KillResult(false, msg, new HashMap<>());
      //            throw new LinkisClientExecutionException(JobStatus.FAILED, "EXE0004",
      // ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
    } else {
      try {
        LinkisOperResultAdapter jobKillResult =
            oper.kill(
                jobInfoResult.getUser(),
                jobInfoResult.getJobID(),
                jobInfoResult.getStrongerExecId());
      } catch (Exception e) {
        return new KillResult(
            false,
            "Exception thrown when trying to send kill request. Messgae: "
                + ExceptionUtils.getStackTrace(e),
            new HashMap<>());
      }
      String msg = "Kill request has been sent";
      LoggerManager.getPlaintTextLogger().info(msg);
      int retryCnt = 0;
      final int MAX_RETRY = 5 * 6;
      while (!jobInfoResult.getJobStatus().isJobFinishedState()
          && !jobInfoResult.getJobStatus().isJobCancelled()) {
        CliUtils.doSleepQuietly(CliConstants.JOB_QUERY_SLEEP_MILLS);
        try {
          jobInfoResult = oper.queryJobInfo(jobInfoResult.getUser(), jobInfoResult.getJobID());
          retryCnt = 0; // if exception then will not go here
        } catch (Exception e) {
          retryCnt++;
          CliUtils.doSleepQuietly(5 * CliConstants.JOB_QUERY_SLEEP_MILLS);
          if (retryCnt >= MAX_RETRY) {
            return new KillResult(
                false,
                MessageFormat.format(
                    "After send kill. Client cannot get jobStatus from server continuously for {0} seconds. Client aborted. Assume kill failed! Error message: \n",
                    MAX_RETRY * 5 * CliConstants.JOB_QUERY_SLEEP_MILLS),
                new HashMap<>());
          }
        }
      }
      if (jobInfoResult.getJobStatus().isJobFinishedState()
          && !jobInfoResult.getJobStatus().isJobCancelled()) {
        msg = "Kill Failed: Job Current status: " + jobInfoResult.getJobStatus();
        return new KillResult(false, msg, new HashMap<>());
        //                throw new LinkisClientExecutionException(JobStatus.FAILED,
        // "EXE0004", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, msg);
      } else if (jobInfoResult.getJobStatus().isJobCancelled()) {
        msg =
            MessageFormat.format(
                "Kill successful: jobId={0}, status={1}.",
                jobInfoResult.getJobID(), jobInfoResult.getJobStatus());
        return new KillResult(true, msg, new HashMap<>());
        //                LogUtils.getPlaintTextLogger().info(msg);
      } else {
        return new KillResult(false, "Unknown Error!!", new HashMap<>());
      }
    }
  }
}
