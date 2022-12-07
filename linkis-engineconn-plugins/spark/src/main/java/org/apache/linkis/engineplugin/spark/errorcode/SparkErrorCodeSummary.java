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

package org.apache.linkis.engineplugin.spark.errorcode;

public enum SparkErrorCodeSummary {
  READ_RECORD_EXCEPTION(40001, "read record  exception(读取记录异常)"),
  DATAFRAME_EXCEPTION(40002, "dataFrame to local exception(dataFrame 到本地异常)"),
  OUT_ID(40003, ""),

  SPARK_STOPPED(
      40004, "Spark application has already stopped, please restart it(Spark 应用程序已停止，请重新启动)"),
  EXECUTE_SPARKSCALA_FAILED(40005, "execute sparkScala failed!(执行 sparkScala 失败！)"),
  SPARK_IS_NULL(40006, "sparkILoop is null(sparkILoop 为空)"),

  CSTABLE_NOT_FOUND(
      40007, "The csTable that name is：{0} not found in cs(在 cs 中找不到名称为:{0} 的 csTable)"),
  PYSPARK_STOPPED(40007, "Pyspark process has stopped, query failed!(Pyspark 进程已停止，查询失败！)"),
  CAN_NOT_NULL(40009, "sparkSession can not be null(sparkSession 不能为空)"),
  REQUEST_MDQ_FAILED(
      40010,
      "The request to the MDQ service to parse into executable SQL failed(向MDQ服务请求解析为可以执行的sql时失败)"),
  SPARK_CREATE_EXCEPTION(80002, "spark repl classdir create exception(spark repl classdir 创建异常)"),
  INVALID_CREATE_SPARKSQL(
      420001,
      "Invalid EngineConn engine session obj, failed to create sparkSql executor(EngineConn 引擎会话 obj 无效，无法创建 sparkSql 执行程序)"),
  INVALID_CREATE_SPARKPYTHON(
      420002,
      "Invalid EngineConn engine session obj, failed to create sparkPython executor(EngineConn 引擎会话 obj 无效，无法创建 sparkPython 执行程序)");
  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;

  SparkErrorCodeSummary(int errorCode, String errorDesc) {
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
