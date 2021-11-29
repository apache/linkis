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
 
package org.apache.linkis.cli.core.interactor.execution.observer.listener;

import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.jobexec.JobSubmitExec;
import org.apache.linkis.cli.core.interactor.execution.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.core.presenter.Presenter;
import org.apache.linkis.cli.core.presenter.model.ModelConverter;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;


public class IncLogEventListener implements LinkisClientObserver {
    private Presenter presenter;
    private ModelConverter converter;

    public IncLogEventListener() {
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setConverter(ModelConverter converter) {
        this.converter = converter;
    }

    private void checkInit() {
        if (presenter == null || converter == null) {
            throw new LinkisClientRuntimeException("EVT0002", ErrorLevel.ERROR, CommonErrMsg.EventErr, "IncLogEventListener is not inited: listener or trnasformer is null");
        }
        presenter.checkInit();
    }

    @Override
    public void update(LinkisClientEvent event, Object msg) {
        checkInit();
        if (!(msg instanceof JobSubmitExec)) {
            throw new LinkisClientRuntimeException("EVT0003", ErrorLevel.ERROR, CommonErrMsg.EventErr,
                    "IncLogEventListener failed to call Presenter because msg is not instance of JobSubmitExec");
        }
        PresenterModel model = converter.convertToModel(msg);
        presenter.present(model);
    }

}