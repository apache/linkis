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
 
package org.apache.linkis.cli.core.presenter;

import org.apache.linkis.cli.common.entity.command.CmdOption;
import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.command.template.option.MapOption;
import org.apache.linkis.cli.core.interactor.command.template.option.Parameter;
import org.apache.linkis.cli.core.interactor.command.template.option.StdOption;
import org.apache.linkis.cli.core.presenter.display.DisplayDriver;
import org.apache.linkis.cli.core.presenter.display.StdOutDriver;
import org.apache.linkis.cli.core.presenter.model.HelpInfoModel;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: Display help-info
 */
public class HelpInfoPresenter implements Presenter {
    private static Logger logger = LoggerFactory.getLogger(HelpInfoPresenter.class);
    DisplayDriver driver = new StdOutDriver();

    @Override
    public void checkInit() {
        if (driver == null) {
            throw new PresenterException("PST0007", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Driver is null");
        }
    }

    @Override
    public void present(PresenterModel model) {
        if (!(model instanceof HelpInfoModel)) {
            throw new PresenterException("PST0010", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Input for HelpInfoPresenter is not instance of model");
        }
        HelpInfoModel helpInfoModel = (HelpInfoModel) model;


        String helpInfo = getUsage(helpInfoModel.getTemplate());


        driver.doOutput(helpInfo);
    }

    /**
     * Help info for sub-command
     */

    private String getUsage(CmdTemplate template) {
        StringBuilder sb = new StringBuilder();
        List<CmdOption<?>> options = template.getOptions();
        List<CmdOption<?>> stdOptions = new ArrayList<>();
        List<CmdOption<?>> parameters = new ArrayList<>();
        List<CmdOption<?>> mapOptions = new ArrayList<>();
        for (CmdOption<?> o : options) {
            if (o instanceof StdOption<?>) {
                stdOptions.add(o);
            } else if (o instanceof Parameter<?>) {
                parameters.add(o);
            } else if (o instanceof MapOption) {
                mapOptions.add(o);
            }
        }

        sb.append("Usage: ").append(template.getCmdType().getName()).append(
                options.size() > 0 ? " [OPTIONS] " : " ");
        for (CmdOption<?> p : parameters) {
            if (p instanceof Parameter<?>) {
                sb.append(((Parameter<?>) p).repr()).append(" ");
            }

        }
        if (!"".equals(template.getCmdType().getDesc())) {
            sb.append("\n\t").append(template.getCmdType().getDesc());
        }

        sb.append(options.size() > 0 ? "\nOptions:\n" : "\n");
        for (CmdOption<?> o : stdOptions) {
            sb.append(o.toString()).append("\n");
        }

        sb.append(options.size() > 0 ? "\nMapOptions:\n" : "\n");
        for (CmdOption<?> o : mapOptions) {
            sb.append(o.toString()).append("\n");
        }

        sb.append(parameters.size() > 0 ? "Parameters:\n" : "\n");
        for (CmdOption<?> p : parameters) {
            sb.append(p.toString()).append("\n");
        }

        return sb.toString();
    }
}