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

package org.apache.linkis.engineplugin.hive.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum HiveErrorCodeSummary implements LinkisErrorCode {
  CREATE_HIVE_EXECUTOR_ERROR(26040, "failed to create hive executor(创建hive执行器失败)"),
  HIVE_EXEC_JAR_ERROR(
      26041, "cannot find hive-exec.jar, start session failed(找不到 hive-exec.jar，启动会话失败)"),
  GET_FIELD_SCHEMAS_ERROR(26042, "cannot get the field schemas(无法获取字段 schemas)"),
  INVALID_VALUE(26043, "invalid value(无效值)"),
  COMPILE_HIVE_QUERY_ERROR(26044, "failed to compile hive query(hive语句编译失败)"),
  ;

  private final int errorCode;

  private final String errorDesc;

  HiveErrorCodeSummary(int errorCode, String errorDesc) {
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
