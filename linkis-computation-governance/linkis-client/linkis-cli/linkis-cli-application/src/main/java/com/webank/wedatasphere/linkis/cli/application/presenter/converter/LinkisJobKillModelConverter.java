/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.application.presenter.converter;

import com.webank.wedatasphere.linkis.cli.application.interactor.execution.jobexec.LinkisJobKill;
import com.webank.wedatasphere.linkis.cli.application.presenter.model.LinkisJobKillResultModel;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.TransformerException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.presenter.model.ModelConverter;
import com.webank.wedatasphere.linkis.cli.core.presenter.model.PresenterModel;
import org.apache.commons.lang.exception.ExceptionUtils;


public class LinkisJobKillModelConverter implements ModelConverter {
    @Override
    public PresenterModel convertToModel(Object data) {
        if (!(data instanceof LinkisJobKill)) {
            throw new TransformerException("TFM0010", ErrorLevel.ERROR, CommonErrMsg.TransformerException,
                    "Failed to convert data into LinkisJobIncLogModel: " + data.getClass().getCanonicalName() + "is not instance of \"LinkisJobSubmitExec\"");
        }
        LinkisJobKill execData = (LinkisJobKill) data;
        LinkisJobKillResultModel model = new LinkisJobKillResultModel(
                execData.getCid(),
                execData.getJobID(),
                execData.isSuccess(),
                execData.getMessage(),
                execData.getTaskID(),
                execData.getExecID(),
                execData.getUser(),
                execData.getJobStatus()
        );
        if (execData.getException() != null) {
            model.setException(execData.getException().getMessage());
            model.setCause(ExceptionUtils.getRootCauseMessage(execData.getException()));
        }

        return model;
    }
}
