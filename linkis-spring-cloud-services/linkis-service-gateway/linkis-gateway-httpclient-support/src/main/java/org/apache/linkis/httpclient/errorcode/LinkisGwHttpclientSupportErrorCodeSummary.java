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

package org.apache.linkis.httpclient.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisGwHttpclientSupportErrorCodeSummary implements LinkisErrorCode {
  AUTHTOKENVALUE_BE_EXISTS(
      10901,
      "The value of authTokenValue in ClientConfig must be exists, since no password is found to login(ClientConfig中authTokenValue的值必须存在，因为没有找到密码登录)."),
  TOKEN_AUTHENTICATION(
      10901,
      "cannot use token authentication, since no user is found to proxy(无法使用令牌 token 身份验证，因为找不到代理用户)"),
  CLIENTCONFIG_MUST(10901, "ClientConfig must specify the DWS version(ClientConfig必须指定DWS版本)");
  /** 错误码 */
  private final int errorCode;
  /** 错误描述 */
  private final String errorDesc;

  LinkisGwHttpclientSupportErrorCodeSummary(int errorCode, String errorDesc) {
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
