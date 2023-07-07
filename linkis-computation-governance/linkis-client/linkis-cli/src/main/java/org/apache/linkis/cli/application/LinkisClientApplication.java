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

package org.apache.linkis.cli.application;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.job.Job;
import org.apache.linkis.cli.application.entity.job.JobResult;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.interactor.command.CmdTemplateFactory;
import org.apache.linkis.cli.application.interactor.command.template.UniversalCmdTemplate;
import org.apache.linkis.cli.application.interactor.job.interactive.InteractiveJob;
import org.apache.linkis.cli.application.interactor.job.jobcmd.JobCmdJob;
import org.apache.linkis.cli.application.interactor.job.once.LinkisOnceJob;
import org.apache.linkis.cli.application.operator.OperManager;
import org.apache.linkis.cli.application.operator.once.OnceOperBuilder;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperBuilder;
import org.apache.linkis.cli.application.present.HelpPresenter;
import org.apache.linkis.cli.application.present.model.HelpInfoModel;
import org.apache.linkis.cli.application.utils.LoggerManager;
import org.apache.linkis.cli.application.utils.SchedulerManager;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisClientApplication {
  private static Logger logger = LoggerFactory.getLogger(LinkisClientApplication.class);

  public static void main(String[] args) {
    /*
     generate template
    */
    CmdTemplateFactory.register(new UniversalCmdTemplate());

    /*
    build ctx
     */
    CliCtx ctx = null;
    try {
      ctx = CtxBuilder.buildCtx(args);
    } catch (CommandException e) {
      CmdTemplate template = CmdTemplateFactory.getTemplateOri(e.getCmdType());
      if (template != null) {
        printHelp(template);
      }
      LoggerManager.getInformationLogger().error("Failed to build CliCtx", e);
      System.exit(-1);
    }

    /*
    prepare oper
     */
    OperManager.register(CliKeys.Linkis_OPER, new LinkisOperBuilder());
    OperManager.register(CliKeys.LINKIS_ONCE, new OnceOperBuilder());

    /*
    run job
     */
    Job job;
    if (isJobCmd(ctx)) {
      job = new JobCmdJob();
    } else if (isOnceCmd(ctx)) {
      job = new LinkisOnceJob();
    } else {
      job = new InteractiveJob();
    }
    job.build(ctx);
    JobResult result;
    try {
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    if (job != null) {
                      job.onDestroy();
                    }
                  }));
      result = job.run();
    } catch (Exception e) {
      logger.error("Failed to execute job", e);
      result =
          new JobResult() {
            @Override
            public Boolean isSuccess() {
              return false;
            }

            @Override
            public String getMessage() {
              return "Failed to execute job" + ExceptionUtils.getStackTrace(e);
            }

            @Override
            public Map<String, String> getExtraMessage() {
              return new HashMap<>();
            }
          };
    }

    /*
    process result
     */
    printIndicator(result);

    SchedulerManager.shutDown();

    if (result.isSuccess()) {
      System.exit(0);
    } else {
      System.exit(-1);
    }
  }

  private static void printHelp(CmdTemplate template) {
    HelpInfoModel model = new HelpInfoModel();
    model.buildModel(template);
    new HelpPresenter().present(model);
  }

  private static void printIndicator(JobResult jobResult) {
    if (jobResult.isSuccess()) {
      LoggerManager.getPlaintTextLogger().info(CliConstants.SUCCESS_INDICATOR);
    } else {
      LoggerManager.getPlaintTextLogger().info(CliConstants.FAILURE_INDICATOR);
    }
  }

  private static Boolean isJobCmd(CliCtx ctx) {
    if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_KILL_OPT)
        || ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_STATUS_OPT)
        || ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_DESC_OPT)
        || ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_LOG_OPT)
        || ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_RESULT_OPT)
        || ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_LIST_OPT)) {
      return true;
    }
    return false;
  }

  private static Boolean isOnceCmd(CliCtx ctx) {
    return false;
  }
}
