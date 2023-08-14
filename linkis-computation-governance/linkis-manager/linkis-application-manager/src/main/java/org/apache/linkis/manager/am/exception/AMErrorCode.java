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

package org.apache.linkis.manager.am.exception;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum AMErrorCode implements LinkisErrorCode {
  QUERY_PARAM_NULL(21001, "query param cannot be null(请求参数不能为空)"),

  UNSUPPORT_VALUE(21002, "unsupport value(不支持的值类型)"),

  PARAM_ERROR(210003, "param error(参数错误)"),

  NOT_EXISTS_ENGINE_CONN(210003, "Not exists EngineConn(不存在的引擎)"),

  AM_CONF_ERROR(210004, "AM configuration error(AM配置错误)"),

  ASK_ENGINE_ERROR_RETRY(210005, "Ask engine error, retry(请求引擎失败，重试)"),

  EC_OPERATE_ERROR(210006, "Failed to execute operation(引擎操作失败)");

  private final int errorCode;

  private final String errorDesc;

  AMErrorCode(int errorCode, String errorDesc) {
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
