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

import org.apache.linkis.cli.application.interactor.execution.jobexec.LinkisJobInfo;
import org.apache.linkis.cli.application.presenter.model.LinkisJobInfoModel;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.TransformerException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.model.ModelConverter;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;
import org.apache.commons.lang.exception.ExceptionUtils;


public class LinkisJobInfoModelConverter implements ModelConverter {
    @Override
    public PresenterModel convertToModel(Object data) {
        if (!(data instanceof LinkisJobInfo)) {
            throw new TransformerException("TFM0010", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert data into LinkisJobInfo: " + data.getClass().getCanonicalName() + "is not instance of \"LinkisJobInfo\"");
        }
        LinkisJobInfo infoData = (LinkisJobInfo) data;
        LinkisJobInfoModel model = new LinkisJobInfoModel(
                infoData.getCid(),
                infoData.getJobID(),
                infoData.isSuccess(),
                infoData.getMessage(),
                infoData.getTaskID(),
                infoData.getInstance(),
                infoData.getExecId(),
                infoData.getStrongerExecId(),
                infoData.getUmUser(),
                infoData.getExecutionCode(),
                infoData.getLogPath(),
                infoData.getJobStatus(),
                infoData.getEngineType(),
                infoData.getRunType(),
                infoData.getCostTime(),
                infoData.getCreatedTime(),
                infoData.getUpdatedTime(),
                infoData.getEngineStartTime(),
                infoData.getErrCode(),
                infoData.getErrMsg(),
                infoData.getExecuteApplicationName(),
                infoData.getRequestApplicationName(),
                infoData.getProgress()
        );
        if (infoData.getException() != null) {
            model.setException(infoData.getException().getMessage());
            model.setCause(ExceptionUtils.getRootCauseMessage(infoData.getException()));
        }
        return model;
    }
}
