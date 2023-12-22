/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.doris.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum DorisErrorCodeSummary implements LinkisErrorCode {
  CHECK_DORIS_PARAMETER_FAILED(23001, "Failed to check the doris parameter(doris参数检查失败)"),
  DORIS_TEST_CONNECTION_FAILED(23002, "The doris test connection failed(doris测试连接失败)"),

  DORIS_STREAM_LOAD_FILE_PATH_NOT_BLANK(
      23003, "The doris stream load file path cannot be empty(doris stream load file path不能为空)"),
  DORIS_STREAM_LOAD_FILE_PATH_NOT_FILE(
      23004, "The doris stream load file path must be a file(doris stream load file path必须是一个文件)"),
  DORIS_STREAM_LOAD_FILE_PATH_NOT_SUPPORTED_TYPE_FILE(
      23005,
      "The doris stream load file path This file type is not currently supported(doris stream load file path目前不支持该文件类型)");

  private final int errorCode;

  private final String errorDesc;

  DorisErrorCodeSummary(int errorCode, String errorDesc) {
    ErrorCodeUtils.validateErrorCode(errorCode, 26000, 29999);
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
  }

  @Override
  public int getErrorCode() {
    return errorCode;
  }

  @Override
  public String getErrorDesc() {
    return errorDesc;
  }
}
