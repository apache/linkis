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
package com.webank.wedatasphere.linkis.engine.impala.client.util;


import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.ExceptionCode;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException;
import org.apache.hive.service.cli.thrift.TStatus;

/**
 *
 * Created by dingqihuang on Sep 20, 2019
 *
 */
public class ImpalaThriftUtil {
    private static final char[] hexCode = "0123456789abcdef".toCharArray();

    /**
     * 检查返回状态
     *
     * @param status
     * @param resultListener 用于接收信息
     * @throws SubmitException
     */
    public static void checkStatus(TStatus status, ResultListener resultListener) throws SubmitException {
        switch (status.getStatusCode()) {

            case STILL_EXECUTING_STATUS:
                throw SubmitException.of(ExceptionCode.StillRunningError);
            case ERROR_STATUS:
                throw SubmitException.of(ExceptionCode.ExecutionError, status.getErrorMessage());
            case INVALID_HANDLE_STATUS:
                throw SubmitException.of(ExceptionCode.InvalidHandleError);
            case SUCCESS_WITH_INFO_STATUS:
                if (resultListener != null) {
                    resultListener.message(status.getInfoMessages());
                }
                break;
            case SUCCESS_STATUS:
        }
    }

    /**
     * 检查返回状态
     *
     * @param status
     * @throws SubmitException
     */
    public static void checkStatus(TStatus status) throws SubmitException {
        checkStatus(status,null);
    }

    /*
     * impala 输出id的格式
     */
    public static String printUniqueId(byte[] b) {
        StringBuilder sb = new StringBuilder(":");
        for (int i = 0; i < 8; ++i) {
            sb.append(hexCode[(b[15 - i] >> 4) & 0xF]);
            sb.append(hexCode[(b[15 - i] & 0xF)]);
            sb.insert(0, hexCode[(b[i] & 0xF)]);
            sb.insert(0, hexCode[(b[i] >> 4) & 0xF]);
        }
        return sb.toString();
    }
}
