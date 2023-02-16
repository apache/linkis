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

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum SparkErrorCodeSummary implements LinkisErrorCode {
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
      "Invalid EngineConn engine session obj, failed to create sparkPython executor(EngineConn 引擎会话 obj 无效，无法创建 sparkPython 执行程序)"),

  DATA_CALC_CONFIG_VALID_FAILED(43001, "Config data validate failed"),
  DATA_CALC_CONFIG_TYPE_NOT_VALID(43002, "[{0}] is not a valid type"),

  DATA_CALC_DATASOURCE_NOT_CONFIG(43011, "DataSource {0} is not configured!"),

  DATA_CALC_DATABASE_NOT_SUPPORT(43012, "DataSource [{0}] type [{1}] is not supported"),

  DATA_CALC_COLUMN_NOT_MATCH(
      43021,
      "{0}st column ({1}[{2}]) name or data type does not match target table column ({3}[{4}])"),
  DATA_CALC_COLUMN_NUM_NOT_MATCH(
      43022,
      "{0} requires that the data to be inserted have the same number of columns as the target table: target table has {1} column(s) but the inserted data has {2} column(s)"),
  DATA_CALC_FIELD_NOT_EXIST(43023, "{0} columns({1}) are not exist in source columns"),
  DATA_CALC_VARIABLE_NOT_EXIST(43024, "Please set [{0}] in variables"),

  NOT_SUPPORT_ADAPTER(43031, "Not support Adapter for spark application."),

  YARN_APPLICATION_START_FAILED(
      43032, "The application start failed, since yarn applicationId is null."),

  NOT_SUPPORT_METHOD(43040, "Not support method for requestExpectedResource."),
  ;

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  SparkErrorCodeSummary(int errorCode, String errorDesc) {
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
