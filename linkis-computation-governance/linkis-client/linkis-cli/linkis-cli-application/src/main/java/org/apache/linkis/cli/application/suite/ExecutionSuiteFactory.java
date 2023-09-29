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

package org.apache.linkis.cli.application.suite;

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.application.interactor.command.LinkisCmdType;
import org.apache.linkis.cli.application.interactor.job.builder.LinkisManageJobBuilder;
import org.apache.linkis.cli.application.interactor.job.builder.LinkisOnceJobBuilder;
import org.apache.linkis.cli.application.interactor.job.builder.LinkisSubmitJobBuilder;
import org.apache.linkis.cli.application.interactor.job.subtype.LinkisManSubType;
import org.apache.linkis.cli.application.interactor.job.subtype.LinkisSubmitSubType;
import org.apache.linkis.cli.application.interactor.validate.LinkisManageValidator;
import org.apache.linkis.cli.application.interactor.validate.LinkisOnceSubmitValidator;
import org.apache.linkis.cli.application.interactor.validate.LinkisSubmitValidator;
import org.apache.linkis.cli.application.present.LinkisLogPresenter;
import org.apache.linkis.cli.application.present.LinkisResultInfoPresenter;
import org.apache.linkis.cli.application.present.LinkisResultPresenter;
import org.apache.linkis.cli.application.present.model.LinkisJobInfoModel;
import org.apache.linkis.cli.application.present.model.LinkisJobKillModel;
import org.apache.linkis.cli.application.present.model.LinkisResultInfoModel;
import org.apache.linkis.cli.application.present.model.LinkisResultModel;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.command.CmdType;
import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.job.JobData;
import org.apache.linkis.cli.common.entity.job.JobDescription;
import org.apache.linkis.cli.common.entity.job.JobSubType;
import org.apache.linkis.cli.common.entity.operator.JobOperator;
import org.apache.linkis.cli.common.entity.present.PresentWay;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.common.entity.validate.Validator;
import org.apache.linkis.cli.common.entity.var.VarAccess;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.AsyncSubmission;
import org.apache.linkis.cli.core.interactor.execution.Help;
import org.apache.linkis.cli.core.interactor.execution.JobManagement;
import org.apache.linkis.cli.core.interactor.execution.SyncSubmission;
import org.apache.linkis.cli.core.interactor.job.JobBuilder;
import org.apache.linkis.cli.core.interactor.result.DefaultResultHandler;
import org.apache.linkis.cli.core.interactor.result.PresentResultHandler;
import org.apache.linkis.cli.core.present.DefaultStdOutPresenter;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionSuiteFactory {
  private static Logger logger = LoggerFactory.getLogger(ExecutionSuiteFactory.class);

  public static ExecutionSuite getSuite(
      CmdType cmdType, VarAccess stdVarAccess, VarAccess sysVarAccess) {

    ExecutionSuite suite;
    Execution execution;
    Map<String, Job> jobs = new HashMap<>();
    JobSubType subType;
    JobBuilder jobBuilder;
    Validator validator;
    ResultHandler defaultHandler = new DefaultResultHandler();

    /*
    Prepare Builders and command-specific components
     */
    if (cmdType == LinkisCmdType.UNIVERSAL) {
      if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_KILL_OPT)) {
        execution = new JobManagement();
        jobBuilder = new LinkisManageJobBuilder();
        PresentResultHandler handler = new PresentResultHandler();
        handler.setPresenter(new DefaultStdOutPresenter());
        handler.setModel(new LinkisJobKillModel());
        validator = new LinkisManageValidator();
        subType = LinkisManSubType.KILL;
        suite = new ExecutionSuite(execution, jobs, handler, defaultHandler);
      } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_STATUS_OPT)) {
        execution = new JobManagement();
        jobBuilder = new LinkisManageJobBuilder();
        PresentResultHandler handler = new PresentResultHandler();
        handler.setPresenter(new DefaultStdOutPresenter());
        handler.setModel(new LinkisJobInfoModel());
        validator = new LinkisManageValidator();
        subType = LinkisManSubType.STATUS;
        suite = new ExecutionSuite(execution, jobs, handler, defaultHandler);
      } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_LOG_OPT)) {
        execution = new JobManagement();
        jobBuilder = new LinkisManageJobBuilder().setLogListener(new LinkisLogPresenter());
        validator = new LinkisManageValidator();
        subType = LinkisManSubType.LOG;
        suite = new ExecutionSuite(execution, jobs, null, defaultHandler);
      } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_RESULT_OPT)) {
        execution = new JobManagement();
        jobBuilder = new LinkisManageJobBuilder();
        PresentResultHandler handler = new PresentResultHandler();
        handler.setPresenter(new LinkisResultPresenter());
        handler.setModel(new LinkisResultModel());
        validator = new LinkisManageValidator();
        subType = LinkisManSubType.RESULT;
        suite = new ExecutionSuite(execution, jobs, handler, defaultHandler);
      } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_HELP_OPT)) {
        execution = new Help();
        jobs.put(
            "help",
            new Job() {
              @Override
              public String getCid() {
                return null;
              }

              @Override
              public CmdType getCmdType() {
                return cmdType;
              }

              @Override
              public JobSubType getSubType() {
                return null;
              }

              @Override
              public JobDescription getJobDesc() {
                return null;
              }

              @Override
              public JobData getJobData() {
                return null;
              }

              @Override
              public JobOperator getJobOperator() {
                return null;
              }

              @Override
              public PresentWay getPresentWay() {
                return null;
              }
            });
        return new ExecutionSuite(execution, jobs, null, defaultHandler);
      } else {
        Boolean asyncSubmission =
            stdVarAccess.getVarOrDefault(Boolean.class, AppKeys.LINKIS_CLIENT_ASYNC_OPT, false);
        if (asyncSubmission) {
          execution = new AsyncSubmission();
          PresentResultHandler handler = new PresentResultHandler();
          handler.setPresenter(new DefaultStdOutPresenter());
          handler.setModel(new LinkisJobInfoModel());
          jobBuilder = new LinkisSubmitJobBuilder().setAsync(true);
          subType = LinkisSubmitSubType.SUBMIT;
          suite = new ExecutionSuite(execution, jobs, handler, defaultHandler);
          validator = new LinkisSubmitValidator();
        } else {
          execution = new SyncSubmission();
          subType = LinkisSubmitSubType.SUBMIT;
          PresentResultHandler handler1 = new PresentResultHandler();
          handler1.setPresenter(new LinkisResultInfoPresenter());
          handler1.setModel(new LinkisResultInfoModel());
          PresentResultHandler handler2 = new PresentResultHandler();
          handler2.setPresenter(new LinkisResultPresenter());
          handler2.setModel(new LinkisResultModel());

          String mode =
              stdVarAccess.getVarOrDefault(
                  String.class, AppKeys.LINKIS_CLIENT_MODE_OPT, AppConstants.UJES_MODE);
          if (StringUtils.equalsIgnoreCase(mode, AppConstants.ONCE_MODE)) {
            jobBuilder = new LinkisOnceJobBuilder().setLogListener(new LinkisLogPresenter());
            ;
            validator = new LinkisOnceSubmitValidator();
          } else {
            jobBuilder = new LinkisSubmitJobBuilder().setLogListener(new LinkisLogPresenter());
            validator = new LinkisSubmitValidator();
          }
          suite = new ExecutionSuite(execution, jobs, handler1, handler2, defaultHandler);
        }
      }
      /*
      build job
       */
      Job job =
          jobBuilder
              .setCid(AppConstants.DUMMY_CID) // currently we don't need this
              .setCmdType(cmdType)
              .setJobSubType(subType)
              .setStdVarAccess(stdVarAccess)
              .setSysVarAccess(sysVarAccess)
              .build();
      logger.info("==========JOB============\n" + Utils.GSON.toJson(job.getJobDesc()));
      if (validator != null) {
        validator.doValidation(job);
      }

      jobs.put(job.getCid(), job);

      return suite;
    } else {
      throw new LinkisClientExecutionException(
          "EXE0029",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Command Type is not supported");
    }
  }
}
