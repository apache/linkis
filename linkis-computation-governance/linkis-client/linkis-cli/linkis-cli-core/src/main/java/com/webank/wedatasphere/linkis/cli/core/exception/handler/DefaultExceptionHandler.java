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

package com.webank.wedatasphere.linkis.cli.core.exception.handler;

import com.webank.wedatasphere.linkis.cli.common.exception.LinkisClientRuntimeException;
import com.webank.wedatasphere.linkis.cli.common.exception.handler.ExceptionHandler;
import com.webank.wedatasphere.linkis.cli.core.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: write log to stdout, stderr and log file
 */
public class DefaultExceptionHandler implements ExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public void handle(Exception exception) {
        if (exception instanceof LinkisClientRuntimeException) {
            LinkisClientRuntimeException e = (LinkisClientRuntimeException) exception;
            switch (e.getLevel()) {
                case INFO:
                    logger.info(e.getMessage(), e);
                    LogUtils.getInformationLogger().info(e.getMessage());
                    break;
                case WARN:
                    logger.warn(e.getMessage(), e);
                    LogUtils.getInformationLogger().warn(e.getMessage());
                    break;
                case ERROR:
                    logger.error(e.getMessage(), e);
                    LogUtils.getInformationLogger().error(e.getMessage(), e);
                    break;
                case FATAL:
                    String msg = StringUtils.substringAfter(e.getMessage(), "[ERROR]");
                    logger.error(msg, e);
                    LogUtils.getInformationLogger().error("[FATAL]" + msg, e);
                    System.exit(-1);
                    break;
            }

        } else {
            logger.error(exception.getMessage(), exception);
            LogUtils.getInformationLogger().error(exception.getMessage(), exception);
        }
    }
}