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

package org.apache.linkis.engineconn.core.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisEngineconnCoreErrorCodeSummary implements LinkisErrorCode {
  NEED_ENGINE_BEFORE_CALL(
      12100,
      "You need to wait for engine conn to be initialized before starting to call(在开始调用之前，您需要等待 engine conn 初始化)"),
  CANNOT_PARSE_FOR_NODE(12101, "Cannot parse cs table for node：{0}(无法解析节点：{0} 的 cs 表)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisEngineconnCoreErrorCodeSummary(int errorCode, String errorDesc) {
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
