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
 
package org.apache.linkis.cli.core.presenter.display.factory;

import org.apache.linkis.cli.common.entity.job.OutputWay;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.display.DisplayDriver;
import org.apache.linkis.cli.core.presenter.display.PlainTextFileDriver;
import org.apache.linkis.cli.core.presenter.display.StdOutDriver;

public class DisplayDriverFactory {
    public static DisplayDriver getDisplayDriver(OutputWay outputWay) {
        if (outputWay == OutputWay.STANDARD) {
            return new StdOutDriver();
        } else if (outputWay == OutputWay.TEXT_FILE) {
            return new PlainTextFileDriver();
        } else {
            throw new PresenterException("PST0009", ErrorLevel.ERROR, CommonErrMsg.ParserInitErr, "Display driver: " + outputWay.name() + "is not supported");
        }
    }
}