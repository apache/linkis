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

package org.apache.linkis.manager.engineplugin.shell.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisCommonsErrorCodeSummary implements LinkisErrorCode {
  NO_ILLEGAL_USER_HOLDS(50036, "No illegal user holds this process(没有非法用户持有这个进程)"),
  SHELL_CODE_IS_WRONG(50037, "shell code is wrong(shell代码错误)");

  /** 错误码 */
  private final int errorCode;
  /** 错误描述 */
  private final String errorDesc;

  LinkisCommonsErrorCodeSummary(int errorCode, String errorDesc) {
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
