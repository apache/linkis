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

package com.webank.wedatasphere.linkis.cli.application.interactor.validate;

import com.webank.wedatasphere.linkis.cli.application.constants.LinkisConstants;
import com.webank.wedatasphere.linkis.cli.application.driver.context.UjesClientDriverContext;
import com.webank.wedatasphere.linkis.cli.common.exception.LinkisClientRuntimeException;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.ValidateException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.validate.Validator;
import org.apache.commons.lang3.StringUtils;


public class UjesContextValidator implements Validator {
    @Override
    public void doValidation(Object input) throws LinkisClientRuntimeException {
        if (!(input instanceof UjesClientDriverContext)) {
            throw new ValidateException("VLD0009", ErrorLevel.ERROR, CommonErrMsg.ValidationErr, "Input of UjesContextValidator is not instance of UjesClientDriverContext");
        }
        boolean ok = true;
        StringBuilder reasonSb = new StringBuilder();
        UjesClientDriverContext context = (UjesClientDriverContext) input;
        if (StringUtils.isBlank(context.getGatewayUrl())) {
            reasonSb.append("gatewayUrl cannot be empty or blank").append(System.lineSeparator());
            ok = false;
        }
        if (StringUtils.isBlank(context.getAuthenticationStrategyStr())) {
            reasonSb.append("Authentication Strategy cannot be empty or blank").append(System.lineSeparator());
            ok = false;
        } else if (!LinkisConstants.AUTH_STRATEGY_STATIC.equalsIgnoreCase(context.getAuthenticationStrategyStr()) &&
                !LinkisConstants.AUTH_STRATEGY_TOKEN.equalsIgnoreCase(context.getAuthenticationStrategyStr())) {
            reasonSb.append("Authentication Strategy ").append(context.getAuthenticationStrategyStr()).append(" is not valid");
            ok = false;
        }
        if (StringUtils.isBlank(context.getTokenKey())) {
            reasonSb.append("tokenKey cannot be empty or blank").append(System.lineSeparator());
            ok = false;
        }
        if (StringUtils.isBlank(context.getTokenKey())) {
            reasonSb.append("tokenValue cannot be empty or blank").append(System.lineSeparator());
            ok = false;
        }
        if (!ok) {
            throw new ValidateException("VLD0010", ErrorLevel.ERROR, CommonErrMsg.ValidationErr, "LinkisJob validation failed. Reason: " + reasonSb.toString());
        }
    }
}