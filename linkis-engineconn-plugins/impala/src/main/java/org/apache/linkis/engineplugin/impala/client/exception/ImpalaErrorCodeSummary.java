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

package org.apache.linkis.engineplugin.impala.client.exception;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum ImpalaErrorCodeSummary implements LinkisErrorCode {
  ClosedError(29040, "session is closed(当前会话已断开)"),
  ExecutionError(29041, "server report an error(执行失败)"),
  RequestError(29042, "failed to send request to server(请求提交失败)"),
  StillRunningError(29043, "target is still running(任务正在运行中)"),
  InvalidHandleError(29044, "current handle is invalid(当前会话不存在)"),
  ParallelLimitError(29045, "reach the parallel limit(当前已达到最大并行度配置)"),
  LoginError(29046, "failed to login to target server(服务器认证失败)");

  private final int errorCode;

  private final String errorDesc;

  ImpalaErrorCodeSummary(int errorCode, String errorDesc) {
    ErrorCodeUtils.validateErrorCode(errorCode, 29000, 29999);
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
