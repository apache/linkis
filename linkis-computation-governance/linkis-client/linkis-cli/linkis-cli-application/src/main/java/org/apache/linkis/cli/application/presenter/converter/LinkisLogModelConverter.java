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
 
package org.apache.linkis.cli.application.presenter.converter;

import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobSubmitExec;
import org.apache.linkis.cli.application.presenter.model.LinkisJobIncLogModel;
import org.apache.linkis.cli.common.entity.job.OutputWay;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.TransformerException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.model.ModelConverter;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;


public class LinkisLogModelConverter implements ModelConverter {
    @Override
    public PresenterModel convertToModel(Object data) {
        if (!(data instanceof LinkisJobSubmitExec)) {
            throw new TransformerException("TFM0010", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert data into LinkisJobIncLogModel: " + data.getClass().getCanonicalName() + "is not instance of \"LinkisJobSubmitExec\"");
        }
        LinkisJobSubmitExec execData = (LinkisJobSubmitExec) data;
        LinkisJobIncLogModel model = new LinkisJobIncLogModel();
        model.setCid(execData.getCid());
        model.setJobID(execData.getJobID());
        model.setUser(execData.getUser());
        model.setExecID(execData.getExecID());
        model.setTaskID(execData.getTaskID());
        model.setFromLine(0);
        model.setLogPath(execData.getLogPath());
        model.setJobStatus(execData.getJobStatus());
        model.setOutputWay(OutputWay.STANDARD); //stdout
        model.setOutputPath(null); // currently wo don't allow printing log to file here

        return model;
    }
}