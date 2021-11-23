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
 
package org.apache.linkis.cli.application.suite;

import org.apache.linkis.cli.application.constants.LinkisClientKeys;
import org.apache.linkis.cli.application.interactor.command.LinkisCmdType;
import org.apache.linkis.cli.application.interactor.command.template.UniversalCmdTemplate;
import org.apache.linkis.cli.application.interactor.execution.executor.LinkisJobManExecutorBuilder;
import org.apache.linkis.cli.application.interactor.execution.executor.LinkisSubmitExecutorBuilder;
import org.apache.linkis.cli.application.interactor.job.LinkisJobBuilder;
import org.apache.linkis.cli.application.interactor.job.LinkisJobManBuilder;
import org.apache.linkis.cli.application.interactor.result.PresentResultHandler;
import org.apache.linkis.cli.application.presenter.DefaultStdOutPresenter;
import org.apache.linkis.cli.application.presenter.LinkisJobResultPresenter;
import org.apache.linkis.cli.application.presenter.converter.LinkisJobInfoModelConverter;
import org.apache.linkis.cli.application.presenter.converter.LinkisJobKillModelConverter;
import org.apache.linkis.cli.application.presenter.converter.LinkisResultModelConverter;
import org.apache.linkis.cli.common.entity.command.CmdType;
import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.HelpExecution;
import org.apache.linkis.cli.core.interactor.execution.JobManagement;
import org.apache.linkis.cli.core.interactor.execution.SyncSubmission;
import org.apache.linkis.cli.core.interactor.execution.executor.ExecutorBuilder;
import org.apache.linkis.cli.core.interactor.job.JobBuilder;
import org.apache.linkis.cli.core.interactor.result.DefaultResultHandler;
import org.apache.linkis.cli.core.interactor.var.VarAccess;


public class SuiteFactoryImpl implements ExecutionSuiteFactory {
    @Override
    public ExecutionSuite getSuite(CmdType cmdType, VarAccess varAccess) {
        JobBuilder jobBuilder;
        ExecutorBuilder executorBuilder;
        Execution execution;
        ResultHandler defaultHandler = new DefaultResultHandler();

        if (cmdType == LinkisCmdType.UNIVERSAL) {
            PresentResultHandler presentHandler = new PresentResultHandler();
            if (varAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_KILL_OPT)) {
                execution = new JobManagement();
                executorBuilder = new LinkisJobManExecutorBuilder();
                jobBuilder = new LinkisJobManBuilder();
                presentHandler.setPresenter(new DefaultStdOutPresenter());
                presentHandler.setConverter(new LinkisJobKillModelConverter());
            } else if (varAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_STATUS_OPT)) {
                execution = new JobManagement();
                executorBuilder = new LinkisJobManExecutorBuilder();
                jobBuilder = new LinkisJobManBuilder();
                presentHandler.setPresenter(new DefaultStdOutPresenter());
                presentHandler.setConverter(new LinkisJobInfoModelConverter());
            } else if (varAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_HELP_OPT)) {
                execution = new HelpExecution();
                ((HelpExecution) execution).setTemplate(new UniversalCmdTemplate());
                return new ExecutionSuite(execution, null, null, defaultHandler);
            } else {
                //TODO:support async_exec
                execution = new SyncSubmission();
                executorBuilder = new LinkisSubmitExecutorBuilder();
                jobBuilder = new LinkisJobBuilder();
                presentHandler.setPresenter(new LinkisJobResultPresenter());
                presentHandler.setConverter(new LinkisResultModelConverter());

            }
            return new ExecutionSuite(execution, jobBuilder, executorBuilder, presentHandler, defaultHandler);
        } else {
            throw new ExecutorException("EXE0029", ErrorLevel.ERROR, CommonErrMsg.ExecutionInitErr, "Command Type is not supported");
        }

    }

}
