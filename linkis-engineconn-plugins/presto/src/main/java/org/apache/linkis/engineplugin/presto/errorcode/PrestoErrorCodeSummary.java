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

package org.apache.linkis.engineplugin.presto.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum PrestoErrorCodeSummary implements LinkisErrorCode {
  PRESTO_STATE_INVALID(
      26001, "Presto status error,statement is not finished(Presto服务状态异常, 查询语句没有执行结束)"),
  PRESTO_CLIENT_ERROR(26002, "Presto client error(Presto客户端异常)");

  private final int errorCode;

  private final String errorDesc;

  PrestoErrorCodeSummary(int errorCode, String errorDesc) {
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
