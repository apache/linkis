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

package org.apache.linkis.cs.client.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum CsClientErrorCodeSummary implements LinkisErrorCode {
  CREATE_CONTEXT_FAILED(80015, "create context failed(创建上下文失败)"),
  GET_CONTEXT_VALUE_FAILED(80015, "get context value failed(获取上下文值失败)"),
  UPDATE_CONTEXT_FAILED(80015, "update context failed(更新上下文失败)"),
  RESET_CONTEXT_FAILED(80015, "reset context failed(重置上下文失败)"),
  REMOVE_CONTEXT_FAILED(80015, "remove context failed(刪除上下文失败)"),
  BIND_CONTEXTID_FAILED(80015, "bind context id failed(绑定上下文 ID 失败)"),
  SEARCH_CONDITION_FAILED(80015, "search condition failed(搜索失败)"),
  EXECUTE_FALIED(80015, "execute failed(执行失败)"),
  HAIDBYTIME_FAILED(80017, "searchHAIDByTime failed(搜索HAIDByTime失败)"),
  CLEAR_CONTEXT_HAID_FAILED(80017, "batch Clear Context By HAID failed(通过 HAID 批量清除上下文失败)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  CsClientErrorCodeSummary(int errorCode, String errorDesc) {
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
