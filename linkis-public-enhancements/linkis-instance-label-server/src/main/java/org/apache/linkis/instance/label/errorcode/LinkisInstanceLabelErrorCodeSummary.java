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

package org.apache.linkis.instance.label.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisInstanceLabelErrorCodeSummary implements LinkisErrorCode {
  INSERT_SERVICE_INSTANCE(14100, "Failed to insert service instance(插入服务实例失败)"),
  ONLY_ADMIN_CAN_VIEW(14100, "Only admin can view all instances(只有管理员才能查看所有实例)."),
  ONLY_ADMIN_CAN_MODIFY(14100, "Only admin can modify instance label(只有管理员才能修改标签)."),
  INCLUDE_REPEAT(14100, "Failed to update label, include repeat label(更新label失败，包含重复label)"),
  Express_All(14100, "");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LinkisInstanceLabelErrorCodeSummary(int errorCode, String errorDesc) {
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
