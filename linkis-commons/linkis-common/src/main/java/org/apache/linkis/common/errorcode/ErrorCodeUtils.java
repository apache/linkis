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
 
package org.apache.linkis.common.errorcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.common.errorcode.LinkisFrameErrorCodeSummary.VALIDATE_ERROR_CODE_FAILED;


public class ErrorCodeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorCodeUtils.class);

    public static void validateErrorCode(int errCode, int startCode, int endCode) {
        if (errCode < startCode || errCode > endCode) {
            LOGGER.error("You error code validate failed, please fix it and reboot");
            System.exit(VALIDATE_ERROR_CODE_FAILED.getErrorCode());
        }
    }
}
