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

package org.apache.linkis.configuration.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;
import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisConfigurationErrorCodeSummary implements LinkisErrorCode {
  BUILD_LABEL_ID(14100, ""),
  CONFIGURATION_NOT_TYPE(14100, "Configuration does not support engine type:{0}(配置暂不支持{0}引擎类型)"),
  CORRESPONDING_ENGINE_TYPE(
      14100, "The corresponding engine type is not matched:{0}(没有匹配到对应的引擎类型:{0})"),
  FAILED_TO_BUILD_LABEL(14100, "Failed to build label(建立标签失败)"),
  BUILD_LABEL_IS_NULL(14100, "Failed to build label ,label is null(建立标签失败，标签为空)"),
  CONFIGKEY_CANNOT_BE_NULL(14100, "ConfigKey cannot be null(configKey 不能为空)"),
  CONFIG_KEY_NOT_EXISTS(14100, "Config key not exists:{0}(配置键不存在：{0})"),
  LABEL_NOT_EXISTS(14100, "Label not exists:{0}(标签不存在{0})"),
  KEY_CANNOT_EMPTY(14100, "Key cannot be null(Key 不能为空)"),
  PARAMS_CANNOT_BE_EMPTY(14100, "Params cannot be empty!(参数不能为空！)"),
  TOKEN_IS_ERROR(14100, "Token is error(令牌是错误的)"),
  IS_NULL_CANNOT_BE_ADDED(14100, "CategoryName is null, cannot be added(categoryName 为空，无法添加)"),
  CANNOT_BE_INCLUDED(14100, "CategoryName cannot be included '-'(类别名称不能包含 '-')"),
  CREATOR_IS_NULL_CANNOT_BE_ADDED(14100, "Creator is null, cannot be added(创建者为空，无法添加)"),
  ENGINE_TYPE_IS_NULL(14100, "Engine type is null, cannot be added(引擎类型为空，无法添加)"),
  INCORRECT_FIXED_SUCH(
      14100,
      "The saved engine type parameter is incorrect, please send it in a fixed format, such as spark-2.4.3(保存的引擎类型参数有误，请按照固定格式传送，例如spark-2.4.3)"),
  INCOMPLETE_RECONFIRM(14100, "Incomplete request parameters, please reconfirm(请求参数不完整，请重新确认)"),
  ONLY_ADMIN_PERFORM(14100, "Only admin have permission to perform this operation(限管理员执行此操作)"),
  THE_LABEL_PARAMETER_IS_EMPTY(14100, " The label parameter is empty(标签参数为空)"),
  ERROR_VALIDATOR_RANGE(14100, "Error validator range！(错误验证器范围！)"),
  TYPE_OF_LABEL_NOT_SUPPORTED(14100, "This type of label is not supported:{0}(不支持这种类型的标签：{0})");

  /** 错误码 */
  private final int errorCode;
  /** 错误描述 */
  private final String errorDesc;
  /** 错误可能出现的原因 */
  LinkisConfigurationErrorCodeSummary(int errorCode, String errorDesc) {
    ErrorCodeUtils.validateErrorCode(errorCode, 10000, 24999);
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
