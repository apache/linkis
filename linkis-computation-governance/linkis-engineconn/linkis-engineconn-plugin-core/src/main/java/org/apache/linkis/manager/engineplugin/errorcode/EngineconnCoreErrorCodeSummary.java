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

package org.apache.linkis.manager.engineplugin.errorcode;

public enum EngineconnCoreErrorCodeSummary {
  FAILED_CREATE_ELR(10001, "Failed to createEngineConnLaunchRequest(创建 EngineConnLaunchRequest失败)"),
  ETL_REQUESTED(10001, "EngineTypeLabel are requested(需要参数 EngineTypeLabel)"),
  CANNOT_INSTANCE_ECE(20000, "Cannot instance EngineConnExecution(无法实例化 EngineConnExecution)"),

  CANNOT_DEFAULT_EF(20000, "Cannot find default ExecutorFactory(找不到默认的 ExecutorFactory)"),
  ETL_NOT_EXISTS(20000, "EngineTypeLabel is not exists(EngineTypeLabel 不存在)"),
  UCL_NOT_EXISTS(20000, "UserCreatorLabel is not exists(UserCreatorLabel 不存在)"),
  CANNOT_HOME_PATH_EC(20001, "Cannot find the home path of engineConn(找不到 engineConn 的 home 路径)"),
  CANNOT_HOME_PATH_DIST(
      20001, "Cannot find the home path of engineconn dist(找不到 engineconn dist 的 home 路径)"),
  DIST_IS_EMPTY(
      20001,
      "The dist of EngineConn is empty,engineConnType is:{0}(EngineConn 的 dist 为空,engineConnType为：{})"),
  ENGIN_VERSION_NOT_FOUND(
      20001,
      "Cannot find the path of engineConn with specified version: {0} and engineConnType: {1}(找不到版本为：{0} engineConnType 为:{1}的engineConn路径"),
  DIST_IRREGULAR_EXIST(
      20001,
      "The dist of engineConnType:{0} is irregular, both the version dir and non-version dir are exist,(engineConnType:{0} 的 dist 目录不符合规范，版本目录和非版本目录都存在)"),
  NO_PERMISSION_FILE(
      20001,
      "System have no permission to delete old engineConn file:{0}(系统无权删除旧的engineConn文件:{0})"),
  LIB_CONF_DIR_NECESSARY(
      20001,
      "The `lib` and `conf` dir is necessary in engineConnType:{0} dist(`lib` 和 `conf` 目录在 engineConnType:{0} dist目录中必需存在)"),

  NOT_SUPPORTED_EF(20011, "Not supported ExecutorFactory(不支持 ExecutorFactory)"),
  DERTL_CANNOT_NULL(
      70101, "DefaultEngineRunTypeLabel cannot be null(DefaultEngineRunTypeLabel 不能为空)"),
  CANNOT_GET_LABEL_KEY(70102, "Cannot get key of label:{0}(无法获取标签:{0}的 key)"),
  MINRESOURCE_MAXRESOURCE_NO_SAME(
      70103,
      "The minResource:{0} is not the same with the maxResource:{1}(minResource:{0} 与 maxResource:{1} 不同)"),
  FAILED_ENGINE_INSTANCE(70062, "Failed to init engine conn plugin instance(无法初始化引擎连接插件实例)"),
  NO_PUBLIC_CONSTRUCTOR(70062, "No public constructor in pluginClass(pluginClass 中没有公共构造函数)"),

  ILLEGAL_ARGUMENTS(
      70062, "Illegal arguments in constructor of pluginClass(pluginClass 的构造函数中非法参数)"),

  UNABLE_PLUGINCLASS(70062, "Unable to construct pluginClass(无法构造pluginClass)"),
  UNABLE_CLASS(70062, "Unable to load class(无法加载类)"),
  PLUGIN_FAIL_TO_LOAD(70062, ""),
  NO_PLUGIN_FOUND(70063, "No plugin found , please check your configuration(未找到插件，请检查您的配置)"),
  PLUGIN_NOT_FOUND(70063, ""),
  NO_WRITE_PERMISSION(70064, "Have no write permission to directory(对目录没有写权限)"),
  PLUGIN_FAIL_TO_LOAD_RES(70064, "");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;

  EngineconnCoreErrorCodeSummary(int errorCode, String errorDesc) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
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

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
