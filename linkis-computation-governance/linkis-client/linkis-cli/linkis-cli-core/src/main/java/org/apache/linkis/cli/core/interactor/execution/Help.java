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
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.command.CmdTemplateFactory;
import org.apache.linkis.cli.core.interactor.result.ExecutionResultImpl;
import org.apache.linkis.cli.core.interactor.result.ExecutionStatusEnum;
import org.apache.linkis.cli.core.present.HelpInfoPresenter;
import org.apache.linkis.cli.core.present.model.HelpInfoModel;

import java.util.Map;

public class Help implements Execution {
  @Override
  public ExecutionResult execute(Map<String, Job> jobs) {

    if (jobs.size() > 1) {
      throw new LinkisClientExecutionException(
          "EXE0001",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Multiple Jobs is not Supported by current execution");
    }

    HelpInfoModel model = new HelpInfoModel();
    Job job = jobs.values().toArray(new Job[jobs.size()])[0];

    model.buildModel(CmdTemplateFactory.getTemplateOri(job.getCmdType()));

    new HelpInfoPresenter().present(model, null);
    return new ExecutionResultImpl(null, ExecutionStatusEnum.SUCCEED, null);
  }

  @Override
  public boolean terminate(Map<String, Job> jobs) {
    return true;
  }
}
