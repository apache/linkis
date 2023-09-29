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

package org.apache.linkis.cli.core.interactor.result;

import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.present.Model;
import org.apache.linkis.cli.common.entity.present.Presenter;
import org.apache.linkis.cli.common.entity.result.ExecutionResult;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresentResultHandler implements ResultHandler {
  private static Logger logger = LoggerFactory.getLogger(PresentResultHandler.class);
  Presenter presenter;
  Model model;

  public void checkInit() {
    if (presenter == null || model == null) {
      throw new LinkisClientExecutionException(
          "EXE0031",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionResultErr,
          "Presenter or model is null");
    }
  }

  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  public void setModel(Model model) {
    this.model = model;
  }

  @Override
  public void process(ExecutionResult executionResult) {
    checkInit();
    Map<String, Job> jobs = executionResult.getJobs();
    // Probably need modification if we further want multiple-jobs support
    // but we probably don't want to support that
    if (jobs != null) {
      for (Job job : jobs.values()) {
        if (job != null) {
          model.buildModel(job.getJobData());
        }
        try {
          presenter.present(model, job.getPresentWay());
        } catch (Exception e) {
          logger.error("Execution failed because exception thrown when presenting data.", e);
          executionResult.setExecutionStatus(ExecutionStatusEnum.FAILED);
          executionResult.setException(e);
        }
      }
    }
  }
}
