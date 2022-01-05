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
 
package org.apache.linkis.cli.application.interactor.result;

import org.apache.linkis.cli.common.entity.execution.ExecutionResult;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ExecutorException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.Presenter;
import org.apache.linkis.cli.core.presenter.model.ModelConverter;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;


public class PresentResultHandler implements ResultHandler {
    Presenter presenter;
    ModelConverter converter;

    public Presenter getPresenter() {
        return presenter;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public ModelConverter getConverter() {
        return converter;
    }

    public void setConverter(ModelConverter converter) {
        this.converter = converter;
    }

    public void checkInit() {
        if (presenter == null || converter == null) {
            throw new ExecutorException("EXE0031", ErrorLevel.ERROR, CommonErrMsg.ExecutionResultErr, "Presenter or model-converter is null");
        }
    }

    @Override
    public void process(ExecutionResult executionResult) {
        checkInit();
//        if (executionResult.getExecutionStatus() == ExecutionStatus.SUCCEED) {
        PresenterModel model = converter.convertToModel(executionResult.getData());
        presenter.present(model);
//        }
    }
}
