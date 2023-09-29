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

package org.apache.linkis.cli.application.present;

import org.apache.linkis.cli.application.present.model.LinkisResultInfoModel;
import org.apache.linkis.cli.common.entity.present.Model;
import org.apache.linkis.cli.common.entity.present.PresentWay;
import org.apache.linkis.cli.common.entity.present.Presenter;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.present.PresentModeImpl;
import org.apache.linkis.cli.core.present.display.DisplayOperFactory;
import org.apache.linkis.cli.core.present.display.data.StdoutDisplayData;
import org.apache.linkis.cli.core.utils.LogUtils;

import org.apache.commons.lang3.StringUtils;

public class LinkisResultInfoPresenter implements Presenter {
  @Override
  public void present(Model model, PresentWay presentWay) {
    if (!(model instanceof LinkisResultInfoModel)) {
      throw new PresenterException(
          "PST0001",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Input model for \"LinkisResultInfoPresenter\" is not instance of \"LinkisResultInfoModel\"");
    }
    DisplayOperFactory.getDisplayOper(PresentModeImpl.STDOUT)
        .doOutput(new StdoutDisplayData(formatResultIndicator((LinkisResultInfoModel) model)));
  }

  protected String formatResultIndicator(LinkisResultInfoModel model) {
    StringBuilder infoBuilder = new StringBuilder();
    String extraMsgStr = "";

    if (model.getMessage() != null) {
      extraMsgStr = model.getMessage().toString();
    }
    if (model.getJobStatus().isJobSuccess()) {

      LogUtils.getInformationLogger().info("Job execute successfully! Will try get execute result");
      infoBuilder
          .append("============Result:================")
          .append(System.lineSeparator())
          .append("TaskId:")
          .append(model.getJobID())
          .append(System.lineSeparator())
          .append("ExecId: ")
          .append(model.getExecID())
          .append(System.lineSeparator())
          .append("User:")
          .append(model.getUser())
          .append(System.lineSeparator())
          .append("Current job status:")
          .append(model.getJobStatus())
          .append(System.lineSeparator())
          .append("extraMsg: ")
          .append(extraMsgStr)
          .append(System.lineSeparator())
          .append("result: ")
          .append(extraMsgStr)
          .append(System.lineSeparator());
    } else if (model.getJobStatus().isJobFinishedState()) {
      LogUtils.getInformationLogger().info("Job failed! Will not try get execute result.");
      infoBuilder
          .append("============Result:================")
          .append(System.lineSeparator())
          .append("TaskId:")
          .append(model.getJobID())
          .append(System.lineSeparator())
          .append("ExecId: ")
          .append(model.getExecID())
          .append(System.lineSeparator())
          .append("User:")
          .append(model.getUser())
          .append(System.lineSeparator())
          .append("Current job status:")
          .append(model.getJobStatus())
          .append(System.lineSeparator())
          .append("extraMsg: ")
          .append(extraMsgStr)
          .append(System.lineSeparator());
      if (model.getErrCode() != null) {
        infoBuilder.append("errCode: ").append(model.getErrCode()).append(System.lineSeparator());
      }
      if (StringUtils.isNotBlank(model.getErrDesc())) {
        infoBuilder.append("errDesc: ").append(model.getErrDesc()).append(System.lineSeparator());
      }
    } else {
      throw new PresenterException(
          "PST0011",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Job is not completed but triggered ResultPresenter");
    }
    return infoBuilder.toString();
  }
}
