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

package org.apache.linkis.engineconnplugin.seatunnel.client.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum SeatunnelErrorCodeSummary implements LinkisErrorCode {
  NOT_SUPPORT_METHOD_ID(17023, ""),
  ERROR_IN_CLOSING_ID(17025, ""),
  NOT_SUPPORT_METHOD(
      17023, "Not support method for requestExpectedResource.(不支持 requestExpectedResource 的方法)"),
  EXEC_SPARK_CODE_ERROR(17023, "Exec Seatunnel-Spark Code Error(执行 Seatunnel-Spark 代码错误)"),
  EXEC_FLINK_CODE_ERROR(17023, "Exec Seatunnel-Flink Code Error(执行 Seatunnel-Flink 代码错误)"),
  EXEC_FLINKSQL_CODE_ERROR(17023, "Exec Seatunnel-FlinkSQL Code Error(执行 Seatunnel-FlinkSQL 代码错误)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  SeatunnelErrorCodeSummary(int errorCode, String errorDesc) {
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
