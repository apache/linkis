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

package org.apache.linkis.manager.label.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LabelCommonErrorCodeSummary implements LinkisErrorCode {
  UPDATE_LABEL_FAILED(40001, "Update label realtion failed(更新标签属性失败)"),
  LABEL_ERROR_CODE(
      40001,
      "The value of the label is set incorrectly, only one value can be set, and the separator symbol '-' cannot be used(标签的值设置错误,只能设置一个值，不能使用分割符符号 '-') "),
  FAILED_BUILD_COMBINEDLABEL(40001, "Failed to build combinedLabel(构建组合标签失败)"),
  FAILED_READ_INPUT_STREAM(40001, "Fail to read value input stream(读取值输入流失败)"),
  FAILED_CONSTRUCT_INSTANCE(40001, "Fail to construct a label instance of:{0}(未能构建标签实例)"),
  NOT_SUPPORT_ENVTYPE(40001, "Not support envType:{0}(不支持 envType)"),
  CHECK_LABEL_REMOVE_REQUEST(
      130001,
      "ServiceInstance in request is null, please check label remove request(请求中的 ServiceInstance 为空，请检查标签删除请求)");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  LabelCommonErrorCodeSummary(int errorCode, String errorDesc) {
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
