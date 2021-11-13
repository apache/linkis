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
 
package org.apache.linkis.cli.application.presenter;

import org.apache.linkis.cli.application.driver.LinkisClientDriver;
import org.apache.linkis.cli.application.driver.transformer.DriverTransformer;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.Presenter;

/**
 * @description: QueryBasedPresenter makes further queries to linkis in order to display some results
 */
public abstract class QueryBasedPresenter implements Presenter {
    protected LinkisClientDriver clientDriver;
    protected DriverTransformer transformer;
    // TODO: use executor rather than driver in presenter

    public void setClientDriver(LinkisClientDriver clientDriver) {
        this.clientDriver = clientDriver;
    }

    public void setTransformer(DriverTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void checkInit() {
        if (clientDriver == null ||
                transformer == null) {
            throw new PresenterException("PST0003", ErrorLevel.ERROR, CommonErrMsg.PresenterInitErr,
                    "Cannot init QueryBasedPresenter: " +
                            " driver: " + clientDriver +
                            " transformer:" + transformer
            );

        }
        clientDriver.checkInit();
    }


}