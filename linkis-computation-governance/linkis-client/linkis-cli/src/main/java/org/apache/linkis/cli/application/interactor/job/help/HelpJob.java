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

package org.apache.linkis.cli.application.interactor.job.help;

import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.job.Job;
import org.apache.linkis.cli.application.entity.job.JobResult;
import org.apache.linkis.cli.application.present.HelpPresenter;
import org.apache.linkis.cli.application.present.model.HelpInfoModel;

import java.util.HashMap;
import java.util.Map;

public class HelpJob implements Job {
  private CliCtx ctx;

  @Override
  public void build(CliCtx ctx) {
    this.ctx = ctx;
  }

  @Override
  public JobResult run() {
    HelpInfoModel model = new HelpInfoModel();
    model.buildModel(ctx.getTemplate());
    new HelpPresenter().present(model);
    return new JobResult() {
      @Override
      public Boolean isSuccess() {
        return true;
      }

      @Override
      public String getMessage() {
        return "";
      }

      @Override
      public Map<String, String> getExtraMessage() {
        return new HashMap<>();
      }
    };
  }

  @Override
  public void onDestroy() {}
}
