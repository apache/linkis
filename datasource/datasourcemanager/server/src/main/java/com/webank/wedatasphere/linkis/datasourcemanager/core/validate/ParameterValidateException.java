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

package com.webank.wedatasphere.linkis.datasourcemanager.core.validate;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;

import static com.webank.wedatasphere.linkis.datasourcemanager.common.ServiceErrorCode.PARAM_VALIDATE_ERROR;


/**
 * @author georgeqiao
 * 2020/02/11
 */


public class ParameterValidateException extends ErrorException {
    public ParameterValidateException(String desc) {
        super(PARAM_VALIDATE_ERROR.getValue(), desc);
    }

    public ParameterValidateException(String desc, String ip, int port, String serviceKind) {
        super(PARAM_VALIDATE_ERROR.getValue(), desc, ip, port, serviceKind);
    }
}
