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

package org.apache.linkis.errorcode.client;

import org.apache.linkis.common.conf.CommonVars;

public class ClientConfiguration {

  public static final CommonVars<String> LINKIS_GATEWAY_URL =
      CommonVars.apply("wds.linkis.gateway.url", "http://127.0.0.1:9001");

  public static final CommonVars<String> ERRORCODE_URL_PREFIX =
      CommonVars.apply("wds.linkis.errorcode.url.prefix", "/api/rest_j/v1/");

  public static final CommonVars<String> ERRORCODE_GET_URL =
      CommonVars.apply("wds.linkis.errorcode.get.url", "getErrorCodes");

  public static final CommonVars<Long> DEFAULT_CONNECT_TIME_OUT =
      CommonVars.apply("wds.linkis.errorcode.timeout", 10 * 60 * 1000L);

  public static final CommonVars<Long> DEFAULT_READ_TIME_OUT =
      CommonVars.apply("wds.linkis.errorcode.read.timeout", 10 * 60 * 1000L);

  public static final CommonVars<String> AUTH_TOKEN_VALUE =
      CommonVars.apply("wds.linkis.errorcode.auth.token", "BML-AUTH");

  public static final CommonVars<Long> FUTURE_TIME_OUT =
      CommonVars.apply("wds.linkis.errorcode.future.timeout", 2000L);

  public static String getGatewayUrl() {
    return LINKIS_GATEWAY_URL.getValue();
  }

  public static long getConnectTimeOut() {
    return DEFAULT_CONNECT_TIME_OUT.getValue();
  }

  public static long getReadTimeOut() {
    return DEFAULT_READ_TIME_OUT.getValue();
  }

  public static String getErrorCodeUrl() {
    return LINKIS_GATEWAY_URL.getValue()
        + ERRORCODE_URL_PREFIX.getValue()
        + ERRORCODE_GET_URL.getValue();
  }

  public static String getAuthKey() {
    return "Validation-Code";
  }

  public static String getAuthValue() {
    return AUTH_TOKEN_VALUE.getValue();
  }

  public static String getVersion() {
    return "v1";
  }
}
