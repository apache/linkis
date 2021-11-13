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

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.execution.ExecutionResult;
import org.apache.linkis.cli.common.entity.execution.executor.Executor;
import org.apache.linkis.cli.common.entity.execution.jobexec.ExecutionStatus;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.core.presenter.HelpInfoPresenter;
import org.apache.linkis.cli.core.presenter.model.HelpInfoModel;

public class HelpExecution implements Execution {
    CmdTemplate template;

    public void setTemplate(CmdTemplate template) {
        this.template = template;
    }

    @Override
    public ExecutionResult execute(Executor executor, Job job) {
        new HelpInfoPresenter().present(new HelpInfoModel(template));
        return new ExecutionResultImpl(null, ExecutionStatus.SUCCEED, null);
    }

    @Override
    public boolean terminate(Executor executor, Job job) {
        return true;
    }
}
