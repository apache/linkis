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

package org.apache.linkis.cli.core.errorcode;

public enum LinkisCliCoreErrorCodeSummary {
  REASON_IS_EMPTY(
      "BLD0003",
      "Cause: stdVarAccess or sysVarAccess is null(原因：stdVarAccess 或 sysVarAccess 为空)",
      "Cause: stdVarAccess or sysVarAccess is null(原因：stdVarAccess 或 sysVarAccess 为空)"),
  TEMPLATE_EXISTS(
      "CMD0022", "template: ## already exists(模板：###已存在)", "template: ## already exists(模板：###已存在"),
  SET_OPTION_VALUE(
      "CMD0010",
      "Failed to set option value: optionMap contains objects that is not Option(设置选项值失败：optionMap 包含不是选项的对象)",
      "Failed to set option value: optionMap contains objects that is not Option(设置选项值失败：optionMap 包含不是选项的对象)"),
  SET_PARAMETRT_VALUE(
      "CMD001",
      "Failed to set param value: parameters contains objects that is not Parameter(设置参数值失败：参数包含不是参数的对象)",
      "Failed to set param value: parameters contains objects that is not Parameter(设置参数值失败：参数包含不是参数的对象)"),
  IMPUT_OR_TEMPLATE(
      "CMD0009", "input or template is null(输入或模板为空)", "input or template is null(输入或模板为空)"),
  DAILED_TO_INIT_PARSER(
      "CMD0013",
      "failed to init parser: fitter is null(无法初始化解析器：fitter 为空)",
      "failed to init parser: fitter is null(无法初始化解析器：fitter 为空)"),
  PARAMMAPPER_DIFFERENT_KEY(
      "CMD0020",
      "ParamMapper should not map different keys into same key.(ParamMapper 不应将不同的键映射到同一个键。)",
      "ParamMapper should not map different keys into same key.(ParamMapper 不应将不同的键映射到同一个键。)"),
  MAKE_DEEP_COPY(
      "CMD0018",
      "failed to make deep copy of template(未能制作模板的深拷贝)",
      "failed to make deep copy of template(未能制作模板的深拷贝)"),
  DESCRIBED_BY(
      "CMD0021",
      "Input should be a Map-entry described by kv-pairs. e.g. key1=value1(输入应该是由 kv-pairs 描述的 Map-entry。 例如 键1=值1)",
      "Input should be a Map-entry described by kv-pairs. e.g. key1=value1(输入应该是由 kv-pairs 描述的 Map-entry。 例如 键1=值1)"),
  GENERATE_TEMPLATE(
      "CODE-001", "Failed to generate template.(无法生成模板。)", "Failed to generate template.(无法生成模板。)"),
  EMPTY_JOBS_IS_SUBMITTED(
      "EXE0001",
      "Null or empty Jobs is submitted to current execution.(将 Null 或空作业提交到当前执行。)",
      "Null or empty Jobs is submitted to current execution.(将 Null 或空作业提交到当前执行。)"),
  BACKEND_NOT_ASYNC(
      "EXE0001",
      "Backend for # does not support async(# 的后端不支持异步。)",
      "Backend for # does not support async(# 的后端不支持异步。)"),
  SUBEXECTYPE_SHOULD_NOT(
      "EXE0001",
      "SubExecType should not be null(SubExecType 不应为空。)",
      "SubExecType should not be null(SubExecType 不应为空。)"),
  MULTIPLE_JOBS_IS_NOT(
      "EXE0001",
      "Multiple Jobs is not Supported by current execution(当前执行不支持多个作业。)",
      "Multiple Jobs is not Supported by current execution(当前执行不支持多个作业。)"),
  BACKEND_NOT_MANAGEABLE(
      "EXE0001",
      "Backend for # is not manageable(# 的后端不可管理。)",
      "Backend for # is not manageable(# 的后端不可管理。)"),
  EXECUTOR_TYPE_IS_NOT(
      "EXE0002",
      "Executor Type: # is not Supported(执行器类型：# 不支持。)",
      "Executor Type: # is not Supported(执行器类型：# 不支持。)"),
  INSTANCE_ASYNCBACKENDJOB(
      "EXE0002",
      "job is not instance of AsyncBackendJob(作业不是 AsyncBackendJob 的实例。)",
      "job is not instance of AsyncBackendJob(作业不是 AsyncBackendJob 的实例。)"),
  EXHAUSTED_CHECKING_NOT_SUBMITTED(
      "EXE0005",
      "Retry exhausted checking job submission. Job is probably not submitted(重试用尽检查作业提交。 作业可能未提交。)",
      "Retry exhausted checking job submission. Job is probably not submitted(重试用尽检查作业提交。 作业可能未提交。)"),
  PRESENTER_OR_MODEL_IS_NOLL(
      "EXE0031", "Presenter or model is null(演示者或模型为空。)", "Presenter or model is null(演示者或模型为空。)"),
  DUPLICATE_JOBOPERATOR(
      "EXE0027",
      "Attempting to register a duplicate jobOperator, name:# (正在尝试注册一个重复的 jobOperator, name:#)",
      "Attempting to register a duplicate jobOperator, name:# (正在尝试注册一个重复的 jobOperator, name:#)"),
  REUSABLE_JOBOPERATOR(
      "EXE0028",
      "Failed to get a reusable joboperator, name:# (获取可重用作业操作符失败，名称:#)",
      "Failed to get a reusable joboperator, name:# (获取可重用作业操作符失败，名称：#)"),
  IS_NOT_MODEL(
      "PST0010",
      "Input for HelpInfoPresenter is not instance of model (HelpInfoPresenter 的输入不是模型的实例)",
      "Input for HelpInfoPresenter is not instance of model (HelpInfoPresenter 的输入不是模型的实例)"),
  DRIVER_IS_NULL("PST0007", "Driver is null (驱动程序为空)", "Driver is null(驱动程序为空)"),
  DUPLICATE_DISPLAYOPERATOR(
      "PST0012",
      "Attempting to register a duplicate DisplayOperator, name: #(尝试注册重复的 DisplayOperator，名称：#)",
      "Attempting to register a duplicate DisplayOperator, name: #(尝试注册重复的 DisplayOperator，名称：#)"),
  INSTANCE_FILEDISPLAYDATA(
      "PST0004",
      "input data is not instance of FileDisplayData(输入数据不是 FileDisplayData 的实例)",
      "input data is not instance of FileDisplayData(输入数据不是 FileDisplayData 的实例)"),
  CANNOT_MKDIR(
      "PST0005",
      "Cannot mkdir for path: #(无法为路径 mkdir：#)",
      "Cannot mkdir for path: #(无法为路径 mkdir：#)"),
  CANNOT_CREATE(
      "PST0006",
      "Cannot create file for path: #(无法为路径创建文件：#)",
      "Cannot create file for path: #(无法为路径创建文件：#)"),
  CANNOT_WRITE("PST0007", "Cannot write: #(不能写：#)", "Cannot write: #(不能写：#)"),
  DATA_IS_NOT_INSTANCE(
      "PST0008",
      "input data is not instance of StdoutDisplayData(输入数据不是 StdoutDisplayData 的实例)",
      "input data is not instance of StdoutDisplayData(输入数据不是 StdoutDisplayData 的实例)"),
  PROPERTIES_IS_EMPTY(
      "PRP0004",
      "Failed to  properties files because rootPath is empty(属性文件失败，因为 rootPath 为空)",
      "Failed to  properties files because rootPath is empty(属性文件失败，因为 rootPath 为空)"),
  PROPERTIES_FILES(
      "PRP0005",
      "Failed to list properties files(无法列出属性文件)",
      "Failed to list properties files(无法列出属性文件)"),
  PROPSFILESSCANNER_GIVEN(
      "PRP0006",
      "PropsFilesScanner has scanned 0 files given root:#(PropsFilesScanner 扫描了 0 个给定 root 的文件:#)",
      "PropsFilesScanner has scanned 0 files given root:#(PropsFilesScanner 扫描了 0 个给定 root 的文件:#)"),
  PROPERTIES_LOADER(
      "PRP0003",
      "properties loader is not inited because it contains no reader(属性加载器未启动，因为它不包含读取器)",
      "properties loader is not inited because it contains no reader(属性加载器未启动，因为它不包含读取器)"),
  SOURCE_PROPSPATH(
      "PRP0002", "Source:#propsPath(来源：#propsPath)", "Source:#propsPath(来源：#propsPath)"),
  IS_NOT_INITED(
      "PRP0001",
      "properties reader for source:#propsPath  is not inited. because of blank propsId or propsPath(source:#propsPath 的属性读取器未启动。 因为空白的 propsId 或 propsPath)",
      "properties reader for source:#propsPath  is not inited. because of blank propsId or propsPath(source:#propsPath 的属性读取器未启动。 因为空白的 propsId 或 propsPath)"),
  INTO_PARAMITEM(
      "TFM0012",
      "Failed to convert option into ParamItem: params contains duplicated identifier: #(无法将选项转换为 ParamItem：参数包含重复的标识符：)",
      "Failed to convert option into ParamItem: params contains duplicated identifier: #(无法将选项转换为 ParamItem：参数包含重复的标识符：)"),
  INIT_HELPINFOMODEL(
      "TFM0010",
      "Failed to init HelpInfoModel: # is not instance of  CmdTemplate (初始化 HelpInfoModel 失败：# 不是 CmdTemplate 的实例)",
      "Failed to init HelpInfoModel: # is not instance of  CmdTemplate (初始化 HelpInfoModel 失败：# 不是 CmdTemplate 的实例)"),
  NOT_A_INSTANCE_CMDTEMPLATE(
      "VLD0006",
      "Input of ParsedTplValidator is not instance of CmdTemplate. (LinkisSubmitValidator 的输入不是 CmdTemplate 的实例。)",
      "Input of ParsedTplValidator is not instance of CmdTemplate. (LinkisSubmitValidator 的输入不是 CmdTemplate 的实例。 )"),
  VALUE_CANNOT_BE_EMPTY(
      "VLD0003",
      "CmdOption value cannot be empty: paramName:#paramName  CmdType: #CmdType(CmdOption 值不能为空：paramName:#paramName CmdType:#CmdType)",
      "CmdOption value cannot be empty: paramName:#paramName  CmdType: #CmdType(CmdOption 值不能为空：paramName:#paramName CmdType:#CmdType )"),
  STDVARACCESS_IS_NOT_INITED(
      "VA0003",
      "stdVarAccess is not inited. cmdParams:#cmdParams defaultConf:#defaultConf subMapCache:subMapCache (未启动 stdVarAccess。 cmdParams:#cmdParams defaultConf:#defaultConf subMapCache:subMapCache)",
      "stdVarAccess is not inited. cmdParams:#cmdParams defaultConf:#defaultConf subMapCache:subMapCache (未启动 stdVarAccess。 cmdParams:#cmdParams defaultConf:#defaultConf subMapCache:subMapCache)"),
  VALUE_IS_NOT_STRING(
      "VA0002",
      "Cannot getVar #KEY  from config. Cause: value is not String (无法从配置中获取 Var #KEY。 原因：值不是字符串)",
      "Cannot getVar #KEY  from config. Cause: value is not String (无法从配置中获取 Var #KEY。 原因：值不是字符串)"),
  IN_NOT_SUPPORTED(
      "VA0004",
      "Cannot convertStringVal   to  : designated type is not supported(无法将StringVal 转换为：不支持指定类型)",
      "Cannot convertStringVal   to  : designated type is not supported(无法将StringVal 转换为：不支持指定类型)"),
  BOTH_NULL(
      "VA0001",
      "sys_prop and sys_env are both null(sys_prop 和 sys_env 都为空)",
      "sys_prop and sys_env are both null(sys_prop 和 sys_env 都为空)"),
  SAME_KEY_OCCURRED(
      "VA0002",
      "same key occurred in sys_prop and sys_env. will use sys_prop(sys_prop 和 sys_env 中出现了相同的键。 将使用 sys_prop)",
      "same key occurred in sys_prop and sys_env. will use sys_prop(sys_prop 和 sys_env 中出现了相同的键。 将使用 sys_prop)");

  /** error code(错误码) */
  private String errorCode;
  /** wrong description(错误描述 ) */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因 ) */
  private String comment;

  LinkisCliCoreErrorCodeSummary(String errorCode, String errorDesc, String comment) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
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
