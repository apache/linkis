/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.httpclient.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisHttpclientErrorCodeSummary implements LinkisErrorCode {
  CONNECT_TO_SERVERURL(
          10901,
          "connect to serverUrl:{0} failed! because gateway server is unhealthy(连接到 serverUrl {0} 失败，因为网关服务器请求失败)!"),
  REQUEST_FAILED_HTTP(
          10905,
          "URL request failed!(URL 请求失败)"),
  RETRY_EXCEPTION(
          10900,
          "retry exception(重试异常)"),
  MESSAGE_PARSE_EXCEPTION(
          10900,
          "Discovery is not enable(未启用发现)!"),

  METHOD_NOT_SUPPORT_EXCEPTION(
          10902,
          "Not supported client method(不支持客户端方法)!");


  /** error code(错误码) */
  private final int errorCode;
  /** wrong description(错误描述 )*/
  private final String errorDesc;


  LinkisHttpclientErrorCodeSummary(int errorCode, String errorDesc) {
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
