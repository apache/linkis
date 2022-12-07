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

package org.apache.linkis.common.errorcode;

public enum LinkisFrameErrorCodeSummary {
  VALIDATE_ERROR_CODE_FAILED(
      10000,
      "Error code definition is incorrect(错误码定义有误)",
      "Error code definition exceeds the maximum value or is less than the minimum value(错误码定义超过最大值或者小于最小值)",
      "linkis-frame");

  private int errorCode;

  private String errorDesc;

  private String comment;

  private String module;

  LinkisFrameErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
    ErrorCodeUtils.validateErrorCode(errorCode, 10000, 10999);
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
    this.module = module;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorDesc() {
    return errorDesc;
  }

  public void setErrorDesc(String errorDesc) {
    this.errorDesc = errorDesc;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
