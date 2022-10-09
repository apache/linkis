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

package org.apache.linkis.manager.engineplugin.errorcode;

public enum EngineconnCoreErrorCodeSummary {
  FAILED_CREATE_ELR(
      10001,
      "Failed to createEngineConnLaunchRequest(未能创建EngineConnLaunchRequest)",
      "Failed to createEngineConnLaunchRequest(未能创建EngineConnLaunchRequest)"),
  ETL_REQUESTED(
      10001,
      "EngineTypeLabel are requested(请求 EngineTypeLabel)",
      "EngineTypeLabel are requested(请求 EngineTypeLabel)"),
  CANNOT_INSTANCE_ECE(
      20000,
      "Cannot instance EngineConnExecution(无法实例 EngineConnExecution)",
      "Cannot instance EngineConnExecution(无法实例 EngineConnExecution)"),

  CANNOT_DEFAULT_EF(
      20000,
      "Cannot find default ExecutorFactory(找不到默认的 ExecutorFactory)",
      "Cannot find default ExecutorFactory(找不到默认的 ExecutorFactory)"),
  ETL_NOT_EXISTS(
      20000,
      "EngineTypeLabel is not exists(EngineTypeLabel 不存在)",
      "EngineTypeLabel is not exists(EngineTypeLabel 不存在)"),
  UCL_NOT_EXISTS(
      20000,
      "UserCreatorLabel is not exists(UserCreatorLabel 不存在)",
      "UserCreatorLabel is not exists(UserCreatorLabel 不存在)"),
  CANNOT_HOME_PATH_EC(
      20001,
      "Cannot find the home path of engineConn(找不到engineConn的home路径)",
      "Cannot find the home path of engineConn(找不到engineConn的home路径)"),
  CANNOT_HOME_PATH_DIST(
      20001,
      "Cannot find the home path of engineconn dist(找不到 engineconn dist 的home路径)",
      "Cannot find the home path of engineconn dist(找不到 engineconn dist 的home路径)"),
  DIST_IS_EMPTY(
      20001,
      "The dist of EngineConn is empty,engineConnType(EngineConn 的 dist 为空,engineConnType):",
      "The dist of EngineConn is empty,engineConnType(EngineConn 的 dist 为空,engineConnType):"),
  DIST_IRREGULAR_EXIST(
      20001,
      "The dist of EngineConn is irregular, both the version dir and non-version dir are exist,engineConnType(EngineConn的dist是不规则的，版本目录和非版本目录都存在,engineConnType):",
      "The dist of EngineConn is irregular, both the version dir and non-version dir are exist,engineConnType(EngineConn的dist是不规则的，版本目录和非版本目录都存在,engineConnType):"),
  NO_PERMISSION_FILE(
      20001,
      "System have no permission to delete old engineConn file,File(系统无权删除旧的engineConn文件,File):",
      "System have no permission to delete old engineConn file,File(系统无权删除旧的engineConn文件,File):"),
  LIB_CONF_DIR_NECESSARY(
      20001,
      "The `lib` and `conf` dir is necessary in EngineConn dist,engineConnType(`lib` 和 `conf` 目录在 EngineConn dist 中是必需的,engineConnType):",
      "The `lib` and `conf` dir is necessary in EngineConn dist,engineConnType(`lib` 和 `conf` 目录在 EngineConn dist 中是必需的,engineConnType):"),

  NOT_SUPPORTED_EF(
      20011,
      "Not supported ExecutorFactory(不支持 ExecutorFactory)",
      "Not supported ExecutorFactory(不支持 ExecutorFactory)"),
  DERTL_CANNOT_NULL(
      70101,
      "DefaultEngineRunTypeLabel cannot be null(DefaultEngineRunTypeLabel 不能为空)",
      "DefaultEngineRunTypeLabel cannot be null(DefaultEngineRunTypeLabel 不能为空)"),
  CANNOT_GET_LABEL_KEY(
      70102,
      "Cannot get label key. labels (无法获取标签密钥,标签):",
      "Cannot get label key. labels (无法获取标签密钥,标签):"),
  MINRESOURCE_MAXRESOURCE_NO_SAME(
      70103,
      "The minResource {} is not the same with the maxResource(minResource {} 与 maxResource 不同)",
      "The minResource {} is not the same with the maxResource(minResource {} 与 maxResource 不同)"),
  FAILED_ENGINE_INSTANCE(
      70062,
      "Failed to init engine conn plugin instance(无法初始化引擎连接插件实例)",
      "Failed to init engine conn plugin instance(无法初始化引擎连接插件实例)"),
  NO_PUBLIC_CONSTRUCTOR(
      70062,
      "No public constructor in pluginClass(pluginClass 中没有公共构造函数)",
      "No public constructor in pluginClass(pluginClass 中没有公共构造函数)"),

  ILLEGAL_ARGUMENTS(
      70062,
      "Illegal arguments in constructor of pluginClass(pluginClass 的构造函数中的非法参数)",
      "Illegal arguments in constructor of pluginClass(pluginClass 的构造函数中的非法参数)"),

  UNABLE_PLUGINCLASS(
      70062,
      "Unable to construct pluginClass(无法构造pluginClass)",
      "Unable to construct pluginClass(无法构造pluginClass)"),
  UNABLE_CLASS(70062, "Unable to load class(无法加载类)", "Unable to load class(无法加载类)"),
  PLUGIN_FAIL_TO_LOAD(70062, "", ""),
  NO_PLUGIN_FOUND(
      70063,
      "No plugin found , please check your configuration(未找到插件，请检查您的配置)",
      "No plugin found , please check your configuration(未找到插件，请检查您的配置)"),
  PLUGIN_NOT_FOUND(70063, "", ""),
  NO_WRITE_PERMISSION(
      70064,
      "Have no write permission to directory(对目录没有写权限)",
      "Have no write permission to directory(对目录没有写权限)"),
  PLUGIN_FAIL_TO_LOAD_RES(70064, "", "");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  EngineconnCoreErrorCodeSummary(int errorCode, String errorDesc, String comment) {
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
