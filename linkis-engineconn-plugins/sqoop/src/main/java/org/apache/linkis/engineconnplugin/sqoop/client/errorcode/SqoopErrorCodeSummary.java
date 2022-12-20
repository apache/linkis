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

package org.apache.linkis.engineconnplugin.sqoop.client.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum SqoopErrorCodeSummary implements LinkisErrorCode {
  NOT_SUPPORT_METHOD_ID(16023, ""),
  NOT_SUPPORT_METHOD(
      16023, "Not support method for requestExpectedResource(不支持 requestExpectedResource 的方法)."),
  EXEC_SQOOP_CODE_ERROR(16023, "Exec Sqoop Code Error(执行 Sqoop 代码错误)"),
  NEW_A_INSTANCE_OF(16023, "New a instance of:{0} failed!(新建:{0} 实例失败！)"),
  ERROR_IN_CLOSING_ID(16025, ""),
  UNABLE_TO_CLOSE(
      16025, "Unable to close the mapReduce job related to cluster(无法关闭与集群相关的 mapReduce 作业)"),
  ERROR_IN_CLOSING(16025, "Error in closing sqoop client(关闭 sqoop 客户端时出错)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  SqoopErrorCodeSummary(int errorCode, String errorDesc) {
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
