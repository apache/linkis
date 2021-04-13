/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.flink.exception;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.exception.WarnException;


public class IllegalArgumentException extends ErrorException {

    public IllegalArgumentException(int errCode, String desc) {
        super(errCode, desc);
    }

    public IllegalArgumentException(String desc) {
        super(20000, desc);
    }

    public IllegalArgumentException(Exception e) {
        super(20000, e.getMessage());
    }

    public IllegalArgumentException() {
        super(20000, "argument illegal");
    }

    public IllegalArgumentException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }

}
