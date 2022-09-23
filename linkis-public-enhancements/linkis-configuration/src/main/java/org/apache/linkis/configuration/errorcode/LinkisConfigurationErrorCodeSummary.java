/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.configuration.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;

public enum LinkisConfigurationErrorCodeSummary {
  BUILD_LABEL_ID(14100, "", ""),
  FAILED_TO_BUILD_LABEL(14100, "Failed to build label(建立标签失败)", "Failed to build label(建立标签失败)"),
  BUILD_LABEL_IS_NULL(
      14100,
      "Failed to build label ,label is null(建立标签失败，标签为空)",
      "Failed to build label ,label is null(建立标签失败，标签为空)"),
  CONFIGKEY_CANNOT_BE_NULL(
      14100,
      "configKey cannot be null(configKey 不能为空)",
      "configKey cannot be null(configKey 不能为空)"),
  CONFIG_KEY_NOT_EXISTS(
      14100, "config key not exists:(配置键不存在：)", "config key not exists:(配置键不存在：)"),
  LABEL_NOT_EXISTS(14100, "label not exists:(标签不存在：)", "label not exists:(标签不存在：)"),
  KEY_OR_VALUE_CANNOT(
      14100, "key or value cannot be null(键或值不能为空)", " key or value cannot be null(键或值不能为空)"),
  PARAMS_CANNOT_BE_EMPTY(
      14100, "params cannot be empty!(参数不能为空！)", "params cannot be empty!(参数不能为空！)"),
  TOKEN_IS_ERROR(14100, "token is error(令牌是错误的)", "token is error(令牌是错误的)"),
  IS_NULL_CANNOT_BE_ADDED(
      14100,
      "categoryName is null, cannot be added(categoryName 为空，无法添加)",
      "categoryName is null, cannot be added(categoryName 为空，无法添加)"),
  CANNOT_BE_INCLUDED(
      14100,
      "categoryName cannot be included '-'(类别名称不能包含 '-')",
      "categoryName cannot be included '-'(类别名称不能包含 '-')"),
  CREATOR_IS_NULL_CANNOT_BE_ADDED(
      14100,
      "creator is null, cannot be added(创建者为空，无法添加)",
      "creator is null, cannot be added(创建者为空，无法添加)"),
  ENGINE_TYPE_IS_NULL(
      14100,
      "engine type is null, cannot be added(引擎类型为空，无法添加)",
      "engine type is null, cannot be added(引擎类型为空，无法添加)"),
  INCORRECT_FIXED_SUCH(
      14100,
      "The saved engine type parameter is incorrect, please send it in a fixed format, such as spark-2.4.3(保存的引擎类型参数有误，请按照固定格式传送，例如spark-2.4.3)",
      "The saved engine type parameter is incorrect, please send it in a fixed format, such as spark-2.4.3(保存的引擎类型参数有误，请按照固定格式传送，例如spark-2.4.3)"),
  INCOMPLETE_RECONFIRM(
      14100,
      "Incomplete request parameters, please reconfirm(请求参数不完整，请重新确认)",
      "Incomplete request parameters, please reconfirm(请求参数不完整，请重新确认)"),
  ONLY_ADMIN_CAN_MODIFY(
      14100,
      "only admin can modify category(只有管理员才能修改目录)",
      "only admin can modify category(只有管理员才能修改目录)"),
  THE_LABEL_PARAMETER_IS_EMPTY(
      14100, " The label parameter is empty(标签参数为空)", " The label parameter is empty(标签参数为空)"),
  ERROR_VALIDATOR_RANGE(
      14100, "error validator range！(错误验证器范围！)", "error validator range！(错误验证器范围！)"),
  TYPE_OF_LABEL_NOT_SUPPORTED(
      14100,
      "this type of label is not supported:{}(不支持这种类型的标签：{})",
      "this type of label is not supported:{}(不支持这种类型的标签：{})");

  /** 错误码 */
  private int errorCode;
  /** 错误描述 */
  private String errorDesc;
  /** 错误可能出现的原因 */
  private String comment;

  LinkisConfigurationErrorCodeSummary(int errorCode, String errorDesc, String comment) {
    ErrorCodeUtils.validateErrorCode(errorCode, 20000, 24999);
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
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

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
