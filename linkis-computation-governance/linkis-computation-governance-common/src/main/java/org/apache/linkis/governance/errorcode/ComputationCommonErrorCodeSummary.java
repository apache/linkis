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

package org.apache.linkis.governance.errorcode;

public enum ComputationCommonErrorCodeSummary {
  CANNOT_FIND_OPERATOR_NAMED(
      20030,
      "Cannot find operator named {0}.(找不到名为 {0} 的运算符.)",
      "Cannot find operator named {0}.(找不到名为 {0} 的运算符.)"),
  IS_NOT_EXISTS(20031, "{0} is not exists.({0} 不存在.)", "{0} is not exists.({0} 不存在.)"),
  UNKNOWN_FOR_KEY(20305, "Unknown {0} for key {1}.(未知密钥)", "Unknown {0} for key {1}.(未知密钥)"),
  UNKNOWN_CLASS_TYPE_CANNOT_CAST(
      20305,
      "Unknown class type, cannot cast {0}(未知的类类型，无法强制转换 {0})",
      "Unknown class type, cannot cast {0}(未知的类类型，无法强制转换 {0})"),
  PARAMETER_OF_IS_NOT_EXISTS(
      20305,
      "The parameter of {0} is not exists.({0} 的参数不存在.)",
      "The parameter of {0} is not exists.({0} 的参数不存在.)"),
  JOB_CODE(25000, "", ""),
  SPARK_HAS_STOPPED_RESTART(
      25000,
      "Spark application sc has already stopped, please restart it.(Spark 应用程序 sc 已经停止，请重新启动它.)",
      "Spark application sc has already stopped, please restart it.(Spark 应用程序 sc 已经停止，请重新启动它.)"),
  INVALID_EXECUTOR_OR_NOT_INSTANCE(
      40100,
      "Invalid executor or not instance of SensibleEngine.(无效的执行程序或不是 SensibleEngine 的实例.)",
      "Invalid executor or not instance of SensibleEngine.(无效的执行程序或不是 SensibleEngine 的实例.)"),
  INVALID_COMPUTATION_EXECUTOR(
      40100, "Invalid computationExecutor(无效的计算执行程序)", "Invalid computationExecutor(无效的计算执行程序)"),
  INVALID_LOCK_OR_CODE(
      40102, "Invalid lock or code(请获取到锁后再提交任务.)", "Invalid lock or code(请获取到锁后再提交任务.)"),
  INVALID_LOCK(40103, "Lock :{0} not exist(锁：{0} 不存在)", "Lock :{0} not exist(锁：{0} 不存在)"),
  SENDTOENTRANCE_ERROR(
      40105,
      "SendToEntrance error.(SendToEntrance 错误.)",
      "SendToEntrance error.(SendToEntrance 错误.)"),
  INIT_EXECUTORS_FAILED(
      40106, "Init executors failed. (初始化执行器失败.)", "Init executors failed. (初始化执行器失败.)"),
  INVALID_RESOURCEID(
      40108,
      "Invalid resourceId {0}, it is too length.(无效的resourceId {0})",
      "Invalid resourceId {0}, it is too length.(无效的resourceId {0})"),
  INVALID_RESOURCEID_CONTAIN(
      40108,
      "Invalid resource {0}, it doesn't contain {1}.(资源 {0} 无效，它不包含 {1}.)",
      "Invalid resource {0}, it doesn't contain {1}.(资源 {0} 无效，它不包含 {1}.)"),
  UNKNOWN_RESULTSET(50050, "unknown resultSet(未知结果集):{0}", "unknown resultSet(未知结果集):{0}");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  ComputationCommonErrorCodeSummary(int errorCode, String errorDesc, String comment) {
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
