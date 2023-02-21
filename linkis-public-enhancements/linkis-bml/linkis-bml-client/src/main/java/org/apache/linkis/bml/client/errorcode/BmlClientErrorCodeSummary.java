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

package org.apache.linkis.bml.client.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum BmlClientErrorCodeSummary implements LinkisErrorCode {
  POST_REQUEST_RESULT_NOT_MATCH(
      20060,
      "the result returned by the repository client POST request does not match(物料库客户端POST请求返回的result不匹配)"),

  BML_CLIENT_FAILED(
      20061, "failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)"),
  SERVER_URL_NOT_NULL(20062, "serverUrl cannot be null(serverUrl 不能为空)");

  private final int errorCode;

  private final String errorDesc;

  BmlClientErrorCodeSummary(int errorCode, String errorDesc) {
    ErrorCodeUtils.validateErrorCode(errorCode, 20000, 24999);
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
