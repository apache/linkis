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

package org.apache.linkis.entrance.exception;

public enum EntranceErrorCode {
  /** */
  CACHE_NOT_READY(200, "shared cache not ready "),
  ENTRANCE_CAST_FAIL(20002, "class cast failed "),
  PARAM_CANNOT_EMPTY(20008, "params cannot be empty "),
  LABEL_PARAMS_INVALID(20009, "Label params invalid "),
  EXECUTE_REQUEST_INVALID(20010, "EntranceExecuteRequest invalid "),
  SUBMIT_JOB_ERROR(20011, "Submit job error "),
  INIT_JOB_ERROR(20012, "Init job error "),
  RESULT_NOT_PERSISTED_ERROR(20013, "Result not persisted error "),
  GROUP_NOT_FOUND(20014, "group not found"),
  EXECUTION_CODE_ISNULL(20015, "execute code is null, nothing will be execute!(执行代码为空，没有任何代码会被执行)"),
  JOB_UPDATE_FAILED(20016, "job update failed"),
  VARIABLE_NULL_EXCEPTION(20017, "variable is null"),
  USER_NULL_EXCEPTION(20018, "User information not obtained"),
  USER_IP_EXCEPTION(20019, "User IP address is not configured");

  private int errCode;
  private String desc;

  EntranceErrorCode(int errCode, String desc) {
    this.errCode = errCode;
    this.desc = desc;
  }

  public int getErrCode() {
    return errCode;
  }

  public String getDesc() {
    return desc;
  }
}
