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

package org.apache.linkis.gateway.authentication.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisGwAuthenticationErrorCodeSummary implements LinkisErrorCode {
  TOKEN_IS_NULL(15205, "token is null(token 令牌为空)!"),
  FAILED_TO_LOAD_TOKEN(15200, "Failed to load token from DB into cache(无法将 token 令牌从数据库加载到缓存中)!"),
  TOKEN_VALID_OR_STALE(15201, "Token is not valid or stale(token 令牌无效或已过期)!"),
  ILLEGAL_TOKENUSER(15202, "Illegal TokenUser for Token(Token非法用户)!"),
  ILLEGAL_HOST(15203, "Illegal Host for Token(Token非法主机)!"),
  INVALID_TOKEN(15204, "Invalid Token(令牌无效)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisGwAuthenticationErrorCodeSummary(int errorCode, String errorDesc) {
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
