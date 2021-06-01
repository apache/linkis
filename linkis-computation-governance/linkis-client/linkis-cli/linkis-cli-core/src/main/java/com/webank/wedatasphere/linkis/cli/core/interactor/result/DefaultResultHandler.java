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

package com.webank.wedatasphere.linkis.cli.core.interactor.result;

import com.webank.wedatasphere.linkis.cli.common.entity.execution.ExecutionResult;
import com.webank.wedatasphere.linkis.cli.common.entity.execution.jobexec.ExecutionStatus;
import com.webank.wedatasphere.linkis.cli.common.entity.result.ResultHandler;
import com.webank.wedatasphere.linkis.cli.core.constants.Constants;
import com.webank.wedatasphere.linkis.cli.core.utils.LogUtils;

import static java.lang.System.exit;

/**
 * exit -1 when failure and exit 0 when success
 */
public class DefaultResultHandler implements ResultHandler {
    @Override
    public void process(ExecutionResult executionResult) {
        if (executionResult.getExecutionStatus() == ExecutionStatus.SUCCEED) {
            LogUtils.getPlaintTextLogger().info(Constants.SUCCESS_INDICATOR);
            exit(0);
        } else {
            LogUtils.getPlaintTextLogger().info(Constants.FAILURE_INDICATOR);
            exit(-1);
        }
    }
}
