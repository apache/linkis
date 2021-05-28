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
package com.webank.wedatasphere.linkis.errorcode.client.handler;

import com.webank.wedatasphere.linkis.errorcode.common.ErrorCode;

import java.util.List;


public interface LogFileErrorCodeHandler extends ErrorCodeHandler{
    /**
     * 通过传入一个日志的路径，然后根据日志的路径去读取文件同时匹配错误码，然后进行匹配错误码
     * @param logFilePath 日志文件地址
     * @param type 辅助参数
     * @return
     */
    void handle(String logFilePath, int type);

    void handle(List<String> logFilePaths);


    /**
     *
     * @param logFilePath 文件地址
     * @param line 行数，如果不传 默认1000
     * @return 错误码信息
     */
    List<ErrorCode> handleFileLines(String logFilePath, int line);

}
