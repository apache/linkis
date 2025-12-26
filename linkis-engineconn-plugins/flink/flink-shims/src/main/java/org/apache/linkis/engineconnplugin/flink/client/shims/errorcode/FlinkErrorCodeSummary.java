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

package org.apache.linkis.engineconnplugin.flink.client.shims.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum FlinkErrorCodeSummary implements LinkisErrorCode {
  CANNOT_MODULE_ALREADY(
      16020,
      "Cannot register module:{0}, because a module with this name is already registered(无法注册模块：{0},因为已注册具有此名称的模块)."),
  CANNOT_CATALOG_ALREADY(
      16020,
      "Cannot create catalog:{0}, because a catalog with this name is already registered(无法创建目录：{0},因为同名的目录已注册)."),
  CANNOT_TABLE_ALREADY(
      16020,
      "Cannot create table:{0}, because a table with this name is already registered(无法创建表：{0}，因为同名的表已注册)."),
  CANNOT_FUNCTION_ALREADY(
      16020,
      "Cannot create function:{}, because a function with this name is already registered(无法创建函数：{0}，因为同名函数已注册)."),
  SQL_CODE_EMPTY(16020, "The sql code is empty(sql 代码为空)"),
  NOT_SUPPORT_RUNTYPE(16020, "Not support the runType:{}(不支持的runType：{})."),
  PLANNER_MUST_THESE(16020, "Planner must be one of:{}(Planner 必须是以下之一)."),
  EXECUTION_MUST_THESE(16020, "Execution type must be one of:{}(Execution 类型必须是以下之一)."),
  NOT_SUPPORTED_YARNTARGET(16020, "Not supported YarnDeploymentTarget(不支持 YarnDeploymentTarget)"),

  KUBERNETES_CONFIG_FILE_EMPTY(
      16020, "The kubernetes config file is empty:{}(kubernetes config file为空)."),
  UNKNOWN_CHECKPOINT_MODE(16020, "Unknown checkpoint mode:{0}(未知的 checkpoint 模式)."),
  HUDIJARS_NOT_EXISTS(16020, "hudi jars does not exist(hudi jars 不存在)."),
  PATH_NOT_EXIST(16020, "The path:{0} is not exist or is not a directory(路径：{0}不存在或不是目录)"),
  BOT_PARSE_ENVIRONMENT(16020, "Could not parse environment file，because:{0}(无法解析 environment 文件)"),
  CONFIGURATION_ENTRY_INVALID(16020, "Invalid configuration entry(无效的配置项)"),

  FLINK_INIT_EXCEPTION_ID(16020, ""),

  ONLY_RESET_ALL(16021, "Only RESET ALL is supported now(当前只支持 RESET ALL)"),
  SUPPORTED_COMMAND_CALL(16021, "Unsupported command call:{0}(不支持该命令调用)"),
  ONLY_SINGLE_STATEMENT(16021, "Only single statement is supported now(当前只支持单个语句)"),
  UNKNOWN_STATEMENT(16021, "Unknown statement:{0}"),
  FAILED_PARSE_STATEMENT(16021, "Failed to parse statement(解析语句失败)."),
  FAILED_DROP_STATEMENT(16021, "Failed to parse drop view statement(无法解析 drop view 语句)."),
  SQL_PARSE_ID(16021, ""),
  SUPPORTED_SOURCES(16022, "Unsupported execution type for sources(不支持的源执行类型)."),
  SUPPORTED_SINKS(16022, "Unsupported execution type for sinks(接收器不支持的执行类型)."),
  SUPPORTED_FUNCTION_TYPE(16022, "Unsupported function type:{0}(不支持的函数类型)."),
  NOT_SUPPORT_TRANSFORM(
      16022, "Not support to transform this resultSet to JobId(不支持将此 resultSet 转换为 JobId)."),
  ALREADY_CURRENT_SESSION(16022, "has already been defined in the current session."),
  NOT_EXIST_SESSION(16022, "does not exist in the current session."),
  QUERY_CANCELED(16022, "The job for this query has been canceled(此查询的作业已取消)."),
  NOT_JOB_ASD_ADMIN(16022, "No job is generated, please ask admin for help(未生成作业，请向管理员寻求帮助)!"),
  NOT_SUPPORT_GRAMMAR(16022, "Not support this grammar{0}(不支持该语法：{0})"),
  ERROR_SUBMITTING_JOB(16022, "Error while submitting job(提交作业时出错)."),
  NOT_SOCKET_RETRIEVAL(
      16022, "Result The retrieved gateway address is invalid, the address is(结果 检索到的网关地址无效，地址为):"),
  NOT_DETERMINE_ADDRESS_JOB(
      16022,
      "Could not determine address of the gateway for result retrieval  by connecting to the job manager. Please specify the gateway address manually.(无法通过连接到作业管理器来确定用于检索结果的网关地址,请手动指定网关地址.)"),
  NOT_DETERMINE_ADDRESS(
      16022,
      "Could not determine address of the gateway for result retrieval,Please specify the gateway address manually.(无法确定检索结果的网关地址，请手动指定网关地址.)"),
  INVALID_SQL_STATEMENT(16022, "Invalid SQL statement.(无效的 SQL 语句.)"),
  NO_TABLE_FOUND(16022, "No table with this name could be found.(找不到具有此名称的表.)"),
  INVALID_SQL_QUERY(16022, "Invalid SQL query.(无效的 SQL 查询.)"),
  FAILED_SWITCH_DATABASE(16022, "Failed to switch to database (无法切换到数据库):{0}"),
  FAILED_SWITCH_CATALOG(16022, "Failed to switch to catalog (无法切换到目录):{0}"),
  SQL_EXECUTION_ID(16022, ""),
  NO_JOB_SUBMITTED(16023, "No job has been submitted. This is a bug.(未提交任何作业,这是一个错误.)"),
  NOT_SAVEPOINT_MODE(16023, "not supported savepoint operator mode(不支持保存点操作员模式)"),
  CLUSTER_NOT_EXIST(16023, "Cluster information don't exist.(集群信息不存在.)"),
  APPLICATIONID_NOT_EXIST(16023, "No applicationId is exists!(不存在 applicationId!)"),
  NOT_RETRIEVE_RESULT(16023, "The accumulator could not retrieve the result.(累加器无法检索结果.)"),
  NOT_SUPPORT_METHOD(
      16023, "Not support method for requestExpectedResource.(不支持 requestExpectedResource 的方法.)"),
  NOT_SUPPORT_SAVEPOTION(16023, "Not support to do savepoint for  (不支持为保存点):{0}"),
  CREATE_INSTANCE_FAILURE(
      16023, "Create a new instance of failure, the instance is(新建失败实例,实例为):{0}"),
  NOT_CREATE_CLUSTER(16023, "Job:could not retrieve or create a cluster.(作业：无法检索或创建集群.)"),
  OPERATION_FAILED(16023, "Job: operation failed(作业：操作失败)"),
  NOT_FLINK_VERSION(
      16023,
      "Not support flink version, StreamExecutionEnvironment.class does not exist getConfiguration method!(不支持flink版本，StreamExecutionEnvironment.class不存在getConfiguration方法!)"),
  EXECUTE_FAILED(
      16023,
      "StreamExecutionEnvironment.getConfiguration() execute failed!(StreamExecutionEnvironment.getConfiguration() 执行失败！)"),
  JOB_EXECUTION_ID(16023, ""),
  NOT_SUPPORT_FLINK(
      20001,
      "Not support ClusterDescriptorAdapter for flink application.(不支持 flink 应用的 ClusterDescriptorAdapter.)"),
  KUBERNETES_IS_NULL(
      20001,
      "The application start failed, since kubernetes kubernetesClusterID is null.(应用程序启动失败，因为 kubernetes kubernetesClusterID  为 null.)"),
  YARN_IS_NULL(
      20001,
      "The application start failed, since yarn applicationId is null.(应用程序启动失败，因为 yarn applicationId 为 null.)"),
  NOT_SUPPORT_SIMPLENAME(
      20001,
      "Not support {0} for FlinkSQLComputationExecutor.(不支持 FlinkSQLComputationExecutor 的 {0}.)"),
  ADAPTER_IS_NULL(
      20001,
      "Fatal error, ClusterDescriptorAdapter is null, please ask admin for help.(致命错误，ClusterDescriptorAdapter 为空，请向管理员寻求帮助.)"),
  EXECUTORINIT_ID(20001, "");

  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  FlinkErrorCodeSummary(int errorCode, String errorDesc) {
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
