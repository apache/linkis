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

package org.apache.linkis.engineplugin.repl.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum ReplErrorCodeSummary implements LinkisErrorCode {
  NOT_SUPPORT_REPL_TYPE(29001, "Repl engineplugin does not support this type(Repl引擎不支持这个类型)"),
  REPL_CODE_IS_NOT_BLANK(29002, "Repl engine code cannot be empty(Repl引擎代码不能为空)"),

  UNABLE_RESOLVE_JAVA_METHOD_NAME(
      29003, "Repl engine unable to resolve java method name(Repl引擎无法解析java方法名称)"),

  REPL_SCALA_TASK_EXECUTOR_FAILED(
      29004, "Repl engine scala task executor failed(Repl引擎scala任务执行失败)");

  private final int errorCode;

  private final String errorDesc;

  ReplErrorCodeSummary(int errorCode, String errorDesc) {
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
