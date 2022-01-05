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
 
package org.apache.linkis.cli.core.exception.handler;

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.exception.handler.ExceptionHandler;
import org.apache.linkis.cli.core.data.ClientContext;
import org.apache.linkis.cli.core.exception.CommandException;
import org.apache.linkis.cli.core.presenter.HelpInfoPresenter;
import org.apache.linkis.cli.core.presenter.model.HelpInfoModel;

import java.util.Map;

/**
 * @description: Display help-info if required
 */
public class CommandExceptionHandler implements ExceptionHandler {
    //TODO:move to application
    @Override
    public void handle(Exception e) {
        if (e instanceof CommandException) {
            if (((CommandException) e).requireHelp()) {

                Map<String, CmdTemplate> templateMap = ClientContext.getGeneratedTemplateMap();
                CmdTemplate template = templateMap.get(((CommandException) e).getCmdType().getName());

                if (template != null) {
                    HelpInfoModel model = new HelpInfoModel(template);

                    new HelpInfoPresenter().present(model);
                }
            }
        }
        new DefaultExceptionHandler().handle(e);
    }
}