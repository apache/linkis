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

package org.apache.linkis.cli.application.interactor.job.jobcmd;

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.job.Job;
import org.apache.linkis.cli.application.entity.job.JobResult;
import org.apache.linkis.cli.application.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.common.JobKiller;
import org.apache.linkis.cli.application.interactor.job.common.KillResult;
import org.apache.linkis.cli.application.interactor.job.common.LogRetriever;
import org.apache.linkis.cli.application.interactor.job.common.ResultRetriever;
import org.apache.linkis.cli.application.operator.OperManager;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOper;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.application.present.JobInfoPresenter;
import org.apache.linkis.cli.application.present.LogPresenter;
import org.apache.linkis.cli.application.present.ResultPresenter;
import org.apache.linkis.cli.application.present.model.LinkisJobInfoModel;
import org.apache.linkis.cli.application.utils.LoggerManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobCmdJob implements Job {
  private static final Logger logger = LoggerFactory.getLogger(JobCmdJob.class);

  protected CliCtx ctx;

  protected LinkisJobOper oper;

  protected JobCmdDesc desc;

  @Override
  public void build(CliCtx ctx) {
    this.ctx = ctx;
    this.desc = JobCmdDescBuilder.build(ctx);
    this.oper = (LinkisJobOper) OperManager.getNew(CliKeys.Linkis_OPER, ctx);
  }

  @Override
  public JobResult run() {
    JobCmdSubType subType = desc.getSubType();
    if (!(subType instanceof JobCmdSubType)) {
      throw new LinkisClientExecutionException(
          "EXE0030",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "JobSubType is not instance of JobManSubType");
    }
    JobCmdJobResult result = new JobCmdJobResult(true, "Execute Success!!!", new HashMap<>());

    switch (subType) {
      case STATUS:
        try {
          LinkisOperResultAdapter jobInfoResult =
              oper.queryJobInfo(desc.getUser(), desc.getJobID());
          LinkisJobInfoModel model = new LinkisJobInfoModel();
          model.buildModel(jobInfoResult);
          new JobInfoPresenter().present(model);
        } catch (Exception e) {
          result.setSuccess(false);
          result.setMessage(ExceptionUtils.getStackTrace(e));
        }
        if (!result.isSuccess()) {
          LoggerManager.getPlaintTextLogger()
              .error("Failed to get job-info. Message: " + result.getMessage());
        }
        return result;
      case LOG:
        try {
          // get log while running
          LinkisOperResultAdapter jobInfoResult =
              oper.queryJobInfo(desc.getUser(), desc.getJobID());
          LogRetriever logRetriever =
              new LogRetriever(
                  jobInfoResult.getUser(),
                  jobInfoResult.getJobID(),
                  jobInfoResult.getStrongerExecId(),
                  false,
                  oper,
                  new LogPresenter());
          // async because we need to query job status
          logRetriever.retrieveLogAsync();
          logRetriever.waitIncLogComplete();
        } catch (Exception e) {
          result.setSuccess(false);
          result.setMessage(ExceptionUtils.getStackTrace(e));
        }
        if (!result.isSuccess()) {
          LoggerManager.getInformationLogger()
              .error("Failed to get log. Message: " + result.getMessage());
        }
        return result;
      case RESULT:
        // get log while running
        LinkisOperResultAdapter jobInfoResult = oper.queryJobInfo(desc.getUser(), desc.getJobID());
        // get result-set
        String outputPath =
            ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_COMMON_OUTPUT_PATH);
        ResultPresenter presenter;
        if (StringUtils.isBlank(outputPath)) {
          presenter = new ResultPresenter();
        } else {
          presenter = new ResultPresenter(true, outputPath);
        }

        ResultRetriever resultRetriever =
            new ResultRetriever(
                jobInfoResult.getUser(),
                jobInfoResult.getJobID(),
                jobInfoResult.getStrongerExecId(),
                oper,
                presenter);

        result = getResult(resultRetriever);
        if (!result.isSuccess()) {
          LoggerManager.getInformationLogger()
              .error("Failed to get result. Message: " + result.getMessage());
        }
        return result;
      case KILL:
        JobKiller jobKiller = new JobKiller(oper);
        KillResult killResult;
        try {
          killResult = jobKiller.doKill(desc.getUser(), desc.getJobID());
        } catch (Exception e) {
          killResult =
              new KillResult(
                  false,
                  "Failed to kill job. Messgae: " + ExceptionUtils.getStackTrace(e),
                  new HashMap<>());
        }
        if (killResult.isSuccess()) {
          LoggerManager.getPlaintTextLogger().info("Kill Success. Current job-info:");
        } else {
          LoggerManager.getPlaintTextLogger()
              .error("Kill Failed. Messgae: " + killResult.getMessage() + "\n Current job-info:");
        }
        try {
          LinkisOperResultAdapter jobInfoResult2 =
              oper.queryJobInfo(desc.getUser(), desc.getJobID());
          LinkisJobInfoModel model = new LinkisJobInfoModel();
          model.buildModel(jobInfoResult2);
          new JobInfoPresenter().present(model);
        } catch (Exception e) {
          LoggerManager.getInformationLogger().error("Failed to get jobInfo", e);
        }
        return new JobCmdJobResult(
            killResult.isSuccess(), killResult.getMessage(), killResult.getExtraMessage());
        //            case LIST:
        //                break;
        //            case JOB_DESC:
        //                break;
      default:
        return new JobCmdJobResult(
            false, "JobSubType + \"" + subType + "\" is not supported", new HashMap<>());
    }
  }

  private JobCmdJobResult getResult(ResultRetriever resultRetriever)
      throws LinkisClientRuntimeException {
    JobCmdJobResult result = new JobCmdJobResult(true, "Execute Success!!!", new HashMap<>());
    try {
      resultRetriever.retrieveResultSync();
      result.setSuccess(true);
      result.setMessage("execute success!!!");
    } catch (LinkisClientExecutionException e) {
      if (e.getCode().equals("EXE0037")) {
        result.setSuccess(true);
        result.setMessage("execute success!!!");
        LoggerManager.getInformationLogger().warn(e.getMessage());
      } else {
        result.setSuccess(false);
        result.setMessage("execute failed!!!\n" + ExceptionUtils.getStackTrace(e));
      }
      resultRetriever.setResultFin(); // inform listener to stop
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("execute failed!!!\n" + ExceptionUtils.getStackTrace(e));
      resultRetriever.setResultFin(); // inform listener to stop
    }
    return result;
  }

  @Override
  public void onDestroy() {}
}
