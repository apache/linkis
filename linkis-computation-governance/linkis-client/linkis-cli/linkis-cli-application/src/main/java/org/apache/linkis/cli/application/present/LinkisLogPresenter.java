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

import org.apache.linkis.cli.application.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.present.model.LinkisLogModel;
import org.apache.linkis.cli.common.entity.present.Model;
import org.apache.linkis.cli.common.entity.present.PresentWay;
import org.apache.linkis.cli.common.entity.present.Presenter;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.present.PresentModeImpl;
import org.apache.linkis.cli.core.present.display.DisplayOperFactory;
import org.apache.linkis.cli.core.present.display.DisplayOperator;
import org.apache.linkis.cli.core.present.display.data.StdoutDisplayData;
import org.apache.linkis.cli.core.utils.CommonUtils;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisLogPresenter implements Presenter, LinkisClientListener {
  private static Logger logger = LoggerFactory.getLogger(LinkisLogPresenter.class);

  @Override
  public void present(Model model, PresentWay presentWay) {
    if (!(model instanceof LinkisLogModel)) {
      throw new PresenterException(
          "PST0001",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Input model for \"LinkisLogPresenter\" is not instance of \"LinkisJobIncLogModel\"");
    }
    LinkisLogModel logModel = (LinkisLogModel) model;
    DisplayOperator displayOper = DisplayOperFactory.getDisplayOper(PresentModeImpl.STDOUT);
    while (!logModel.logFinReceived()) {
      String incLog = logModel.consumeLog();
      if (StringUtils.isNotEmpty(incLog)) {
        displayOper.doOutput(new StdoutDisplayData(incLog));
      }
      CommonUtils.doSleepQuietly(500l);
    }
    String incLog = logModel.consumeLog();
    if (StringUtils.isNotEmpty(incLog)) {
      displayOper.doOutput(new StdoutDisplayData(incLog));
    }
  }

  @Override
  public void update(LinkisClientEvent event, Object msg) {
    Model model = new LinkisLogModel();
    model.buildModel(msg);
    this.present(model, null);
  }
}
