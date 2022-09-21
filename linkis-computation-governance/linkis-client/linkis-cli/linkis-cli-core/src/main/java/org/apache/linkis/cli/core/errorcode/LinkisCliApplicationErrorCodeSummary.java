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

public enum LinkisCliApplicationErrorCodeSummary {
  LIENT_FACTORY(
      "BLD0007",
      "Cannot build UjesClientDriverContext: gatewayUrl is empty(无法构建 UjesClientDriverContext：gatewayUrl 为空)",
      "Cannot build UjesClientDriverContext: gatewayUrl is empty(无法构建 UjesClientDriverContext：gatewayUrl 为空)"),
  SPECIFY(
      "BLD0010",
      "Cannot specify admin-user as submit-user(无法将管理员用户指定为提交用户)",
      "Cannot specify admin-user as submit-user(无法将管理员用户指定为提交用户)"),
  SHOULD_BE_THE_SAME(
      "BLD0011",
      "Submit-User should be the same as Auth-Key under Static-Authentication-Strategy (Submit-User 应与 Static-Authentication-Strategy 下的 Auth-Key 相同)",
      "Submit-User should be the same as Auth-Key under Static-Authentication-Strategy (Submit-User 应与 Static-Authentication-Strategy 下的 Auth-Key 相同)"),
  SPECIFY_SCRIPT(
      "BLD0005",
      "User specified script file does not exist: (用户指定的脚本文件不存在)",
      "User specified script file does not exist: (用户指定的脚本文件不存在"),
  UNABLE_TO_READ(
      "BLD0006",
      "Cannot read user specified script file(无法读取用户指定的脚本文件)",
      "Cannot read user specified script file(无法读取用户指定的脚本文件)"),
  SHOULD_BE_INSTANCE(
      "EXE0003",
      "JobOperator of LinkisManageJob should be instance of LinkisJobOperator(LinkisManageJob 的 JobOperator 应该是 LinkisJobOperator 的实例)",
      "JobOperator of LinkisManageJob should be instance of LinkisJobOperator(LinkisManageJob 的 JobOperator 应该是 LinkisJobOperator 的实例)"),
  IS_NOT_INSTANCE_OF(
      "EXE0030",
      "JobSubType is not instance of JobManSubType(JobSubType 不是 JobManSubType 的实例)",
      "JobSubType is not instance of JobManSubType(JobSubType 不是 JobManSubType 的实例)"),
  NOT_SUPPORTED(
      "EXE0002",
      "JobSubType is not supported(不支持 JobSubType)",
      "JobSubType is not supported(不支持 JobSubType)"),
  USER_OR_JOBID_EXECID(
      "EXE0036",
      "user or jobID or execID is null(user 或 jobID 或 execID 为 null)",
      "user or jobID or execID is null(user 或 jobID 或 execID 为 null)"),
  JIBDATA_IS_NOT_RESU(
      "EXE0034",
      "JobData is not LinkisResultData(JobData 不是 LinkisResultData)",
      "JobData is not LinkisResultData(JobData 不是 LinkisResultData)"),
  JIBDATA_IS_NOT_LOG(
      "EXE0034",
      " JobData is not LinkisLogData(JobData 不是 LinkisLogData)",
      " JobData is not LinkisLogData(JobData 不是 LinkisLogData)"),
  JOBSTATUS_IS_NULL("EXE0038", "jobStatus is null(工作状态为空)", "jobStatus is null(工作状态为空)"),
  GOT_BLANK_RESULTLOCATION(
      "EXE0037",
      "Got blank ResultLocation from server. Job may not have result-set. Will not try to retrieve any Result(从服务器获得空白 ResultLocation。 作业可能没有结果集。 不会尝试检索任何结果)",
      "Got blank ResultLocation from server. Job may not have result-set. Will not try to retrieve any Result(从服务器获得空白 ResultLocation。 作业可能没有结果集。 不会尝试检索任何结果)"),
  GOT_NULL_OR_EMPTY(
      "EXE0039",
      "Got null or empty ResultSetPaths(得到 null 或空的 ResultSetPaths)",
      "Got null or empty ResultSetPaths(得到 null 或空的 ResultSetPaths)"),
  SOMETHING_FOES_WRONG(
      "EXE0040",
      "Something foes wrong. Got null as \\'hasNextPage\\'.(有什么不对劲的。 得到 null 作为 \\'hasNextPage\\'。)",
      "Something foes wrong. Got null as \\'hasNextPage\\'.(有什么不对劲的。 得到 null 作为 \\'hasNextPage\\'。)"),
  INSTANCE_IS_NULL(
      "EXE0040",
      "Instance of is null(Instance of is null)",
      "Instance of is null(Instance of is null)"),
  CONTINUPUSLY(
      "EXE0013",
      "Cannot get jobStatus from server continuously for N seconds. Client aborted! Error message'.(无法连续 N 秒从服务器获取 jobStatus。 客户端中止！ 错误信息)",
      "Cannot get jobStatus from server continuously for {0} seconds. Client aborted! Error message'.(无法连续 N 秒从服务器获取 jobStatus。 客户端中止！ 错误信息)"),
  LOGDATA_CLONEABLE(
      "EXE0035",
      "logData is not Cloneable(logData 不可克隆)",
      " logData is not Cloneable(logData 不可克隆)"),
  ABNORMAL_STATUS(
      "EXE0006", "Job is in abnormal status(作业处于异常状态)", " Job is in abnormal status(作业处于异常状态)"),
  PROPERLY_INITIATED(
      "EXE0041",
      "onceJob is not properly initiated(onceJob 未正确启动)",
      "onceJob is not properly initiated(onceJob 未正确启动)"),
  UJES_CLIENT_DRIVER(
      "EXE0011",
      "UjesClientDriver is null(UjesClientDriver 为空)",
      "UjesClientDriver is null(UjesClientDriver 为空)"),
  SUBMIT_JOB("EXE0011", "Failed to submit job(提交作业失败)", "Failed to submit job(提交作业失败)"),
  SUBMIT_JOB_REASON(
      "EXE0012",
      "Failed to submit job， Reason: ####(提交作业失败，原因：####)",
      "Failed to submit job， Reason: ####(提交作业失败，原因：####)"),
  GET_JOB_STATUS_TIME(
      "EXE0013",
      "Get job status failed. retry time (获取作业状态失败。 重试时间XXXX)",
      "Get job status failed. retry time (获取作业状态失败。 重试时间XXXX)"),
  GET_LOG_TIME_TASKID(
      "EXE0015",
      "Get log failed. retry time : {0}/{1}. taskID={2}. Reason: {3}  ( 获取日志失败。 重试时间XXXX,任务id=##,原因:##)",
      "Get log failed. retry time : {0}/{1}. taskID={2}. Reason: {3} ( 获取日志失败。 重试时间XXXX,任务id=##,原因:##)"),
  GET_LOG_TIME(
      "EXE0016",
      "Get log failed. Retry time ( 获取日志失败。 重试时间XXXX)",
      "Get log failed. Retry time( 获取日志失败。 重试时间XXXX)"),
  GET_LOG_TIME_EXHAUSTED(
      "EXE0016",
      "Get log failed. Retry exhausted. taskID=##, Reason: ## ( 获取日志失败。 重试用尽。 任务 ID=##，原因：##)",
      "Get log failed. Retry exhausted. taskID=##, Reason: ##( 获取日志失败。 重试用尽。 任务 ID=##，原因：##)"),
  GET_OPENLOG_LOG_TIME_EXHAUSTED(
      "EXE0017",
      "Get log from openLog failed. Retry exhausted. taskID=##(从 openLog 获取日志失败。 重试用尽。 任务ID=##)",
      "Get log from openLog failed. Retry exhausted. taskID=##( 从 openLog 获取日志失败。 重试用尽。 任务ID=##)"),
  GET_OPENLOG_LOG_TIME_REASON(
      "EXE0017",
      "Get log from openLog failed. Retry exhausted. taskID=##, Reason: ## (从 openLog 获取日志失败。 重试用尽。 任务 ID=##，原因：##)",
      "Get log from openLog failed. Retry exhausted. taskID=##, Reason: ## ( 从 openLog 获取日志失败。 重试用尽。 任务 ID=##，原因：##)"),
  GET_PROGRESS_FAILED_EXHAUSTED(
      "EXE0019",
      "Get progress failed. Retry exhausted. taskID=##(获取进度失败。 重试已用尽。 任务ID=##)",
      "Get progress failed. Retry exhausted. taskID=##( 获取进度失败。 重试已用尽。 任务ID=##)"),
  GET_PROGRESS_FAILED_REASON(
      "EXE0020",
      "Get progress failed. Retry exhausted. taskID=##, Reason: ## (获取进度失败。 重试已用尽。 任务 ID=##，原因：##)",
      "Get progress failed. Retry exhausted. taskID=##, Reason: ## ( 获取进度失败。 重试用尽。 任务 ID=##，原因：##)"),
  KILL_JOB_FAILED(
      "EXE0025",
      "Kill job failed. taskId=# Retry exhausted.(杀死作业失败。 taskId=# 重试已用完。)",
      "Kill job failed. taskId=# Retry exhausted.(杀死作业失败。 taskId=# 重试已用完。)"),
  KILL_JOB_FAILED_REASON(
      "EXE0025",
      "Kill job failed. Retry exhausted. taskId=#, Reason: # (杀死作业失败。 重试用尽。 taskId=#，原因：#)",
      "Kill job failed. Retry exhausted. taskId=#, Reason: # (杀死作业失败。 重试用尽。 taskId=#，原因：#)"),
  GET_RESULTSET_FAILED(
      "EXE0021",
      "Get ResultSet Failed: Cannot get a valid jobInfo(获取 ResultSet 失败：无法获取有效的 jobInfo)",
      "Get ResultSet Failed: Cannot get a valid jobInfo(获取 ResultSet 失败：无法获取有效的 jobInfo)"),
  GET_RESULTSET_NOT_SUCCEED(
      "EXE0021",
      "Get ResultSet Failed: job Status is not Succeed.(获取 ResultSet 失败：作业状态不成功。)",
      "Get ResultSet Failed: job Status is not Succeed.(获取 ResultSet 失败：作业状态不成功。)"),
  RESULTLOCATION_IS_BLANK(
      "EXE0021", "ResultLocation is blank.(结果位置为空白。)", "ResultLocation is blank.(结果位置为空白。)"),
  GET_RESULTSETARRAY_FAILED(
      "EXE0022",
      "Get resultSetArray failed. Retry exhausted. taskID=#(获取 resultSetArray 失败。 重试用尽。 任务ID=#)",
      "Get resultSetArray failed. Retry exhausted. taskID=#(获取 resultSetArray 失败。 重试用尽。 任务ID=#)"),
  GET_RESULTSETARRAY_EXHAUSTED(
      "EXE0023",
      "  Get resultSetArray failed. retry exhausted.  taskId=#, Reason: # (获取 resultSetArray 失败。 重试用尽。 taskId=#，原因：#)",
      "  Get resultSetArray failed. retry exhausted.  taskId=#, Reason: #  (获取 resultSetArray 失败。 重试用尽。 taskId=#，原因：#)"),
  GET_RESULTSET_EXHAUSTED_PATH(
      "EXE0024",
      "Get resultSet failed. Retry exhausted. path= #(获取结果集失败。 重试用尽。 路径=#)",
      "Get resultSet failed. Retry exhausted. path=# (获取结果集失败。 重试用尽。 路径=#)"),
  GET_RESULTSET_EXHAUSTED_REASON(
      "EXE0024",
      "Get resultSet failed. Retry exhausted. Path=#, Reason: #(获取结果集失败。 重试用尽。 路径=#，原因：#)",
      "Get resultSet failed. Retry exhausted. Path=#, Reason: #(获取结果集失败。 重试用尽。 路径=#，原因：#)"),
  CANNOT_INIT_UJE(
      "EXE0010",
      " Cannot init UJESClient(无法初始化 UJESClient)",
      " Cannot init UJESClient(无法初始化 UJESClient)"),
  CANNOT_INIT_DWS(
      "EXE0010",
      " Cannot init DWSClientConfig(无法初始化 DWSClientConfig)",
      " Cannot init DWSClientConfig(无法初始化 DWSClientConfig)"),
  COMMAND_TYPE_IS_NOT(
      "EXE0029",
      " Command Type is not supported(不支持命令类型)",
      " Command Type is not supported(不支持命令类型)"),
  MODEL_IS_NOT_INSTANCE_JOBINCLOG(
      "PST0001",
      " Input model for LinkisLogPresenter is not instance of LinkisJobIncLogModel(LinkisLogPresenter 的输入模型不是 LinkisJobIncLogModel 的实例)",
      "  Input model for LinkisLogPresenter is not instance of LinkisJobIncLogModel(LinkisLogPresenter 的输入模型不是 LinkisJobIncLogModel 的实例)"),
  MODEL_IS_NOT_INSTANCE_RESULTINFO(
      "PST0001",
      "  Input model for  LinkisResultInfoPresenter is not instance of LinkisResultInfoModel(LinkisResultInfoPresenter 的输入模型不是 LinkisResultInfoModel 的实例)",
      "   Input model for  LinkisResultInfoPresenter is not instance of LinkisResultInfoModel(LinkisResultInfoPresenter 的输入模型不是 LinkisResultInfoModel 的实例)"),
  RESULTPRESENTER_TRIGGERED(
      "EXE0029",
      " Job is not completed but triggered ResultPresenter(作业未完成但触发 ResultPresenter)",
      " Job is not completed but triggered ResultPresenter(作业未完成但触发 ResultPresenter)"),
  INSTANCE_RESULTIPRESEMTER(
      "PST0001",
      "  Input model for  LinkisResultPresenter is not instance of LinkisResultModel(LinkisResultPresenter 的输入模型不是 LinkisResultModel 的实例)",
      "   Input model for  LinkisResultPresenter is not instance of LinkisResultModel(LinkisResultPresenter 的输入模型不是 LinkisResultModel 的实例)"),
  PRESENTWAY_RESULTIPRESEMTER(
      "PST0002",
      "  Input PresentWay for  LinkisResultPresenter is not instance of PresentWayImpl(LinkisResultPresenter 的输入 PresentWay 不是 PresentWayImpl 的实例)",
      "   Input PresentWay for  LinkisResultPresenter is not instance of PresentWayImpl(LinkisResultPresenter 的输入 PresentWay 不是 PresentWayImpl 的实例)"),
  VISITED_IN_DESCENDING(
      "PST0002",
      " Linkis resultsets are visited in descending order or are not visited one-by-one(  Linkis 结果集按降序访问或不逐一访问)",
      " Linkis resultsets are visited in descending order or are not visited one-by-one(Linkis 结果集按降序访问或不逐一访问)"),
  ENV_IS_EMPTY(
      "PRP0007",
      "configuration root path specified by env variable:#  is empty. (env 变量指定的配置根路径：# 为空。)",
      "configuration root path specified by env variable:#  is empty.(env 变量指定的配置根路径：# 为空。)"),
  USER_CONFIGURATION(
      "PRP0007",
      "User cannot specify non-customizable configuration: #  (用户不能指定不可定制的配置：#  )",
      "User cannot specify non-customizable configuration: # (用户不能指定不可定制的配置：#  )"),
  CORRECT_TYPE(
      "TFM0001",
      "Input of UJESResultAdapter is not of correct type. Current type: # (UJESResultAdapter 的输入类型不正确。 当前类型： #)",
      "Input of UJESResultAdapter is not of correct type. Current type: # (UJESResultAdapter 的输入类型不正确。 当前类型：# )"),
  CONVERT_RESULTSETMETA(
      "TFM0005",
      "Failed to convert ResultSetMeta(ResultSetMeta 转换失败)",
      "Failed to convert ResultSetMeta (ResultSetMeta 转换失败)"),
  CONVERT_RESULTSE(
      "TFM0007", "Failed to convert ResultSet(转换结果集失败)", "Failed to convert ResultSet(转换结果集失败)"),
  INITJOBINFO_IS_NOT(
      "TFM0010",
      "Failed to init LinkisJobInfoModel: # is not instance of LinkisJobDataImpl(初始化 LinkisJobInfoModel 失败：# 不是 LinkisJobDataImpl 的实例)",
      "Failed to init LinkisJobInfoModel: # is not instance of LinkisJobDataImpl(初始化 LinkisJobInfoModel 失败：# 不是 LinkisJobDataImpl 的实例)"),
  INITJOBKILL_IS_NOT(
      "TFM0010",
      "Failed to init LinkisJobKillModel: # is not instance of LinkisJobDataImpl(初始化 LinkisJobKillModel 失败：# 不是 LinkisJobDataImpl 的实例)",
      "Failed to init LinkisJobKillModel: # is not instance of LinkisJobDataImpl(初始化 LinkisJobKillModel 失败：# 不是 LinkisJobDataImpl 的实例)"),
  INITLOG_IS_NOT(
      "TFM0010",
      "Failed to init LinkisLogModel: # is not instance of LinkisLogData(初始化 LinkisLogModel 失败：# 不是 LinkisLogData 的实例)",
      "Failed to init LinkisLogModel: # is not instance of LinkisLogData(初始化 LinkisLogModel 失败：# 不是 LinkisLogData 的实例)"),
  INITRESULTINFO_IS_NOT(
      "TFM0010",
      "Failed to init LinkisResultInfoModel: # is not instance of LinkisResultData(初始化 LinkisResultInfoModel 失败：# 不是 LinkisResultData 的实例)",
      "Failed to init LinkisResultInfoModel: # is not instance of LinkisResultData(初始化 LinkisResultInfoModel 失败：# 不是 LinkisResultData 的实例)"),
  INITRESULT_IS_NOT(
      "TFM0010",
      "Failed to init LinkisResultModel: # is not instance of LinkisResultData(初始化 LinkisResultModel 失败：# 不是 LinkisResultData 的实例)",
      "Failed to init LinkisResultModel: # is not instance of LinkisResultData(初始化 LinkisResultModel 失败：# 不是 LinkisResultData 的实例)"),
  INITJOBDATAIMPL_IS_NOT(
      "TFM0010",
      "Failed to init LinkisJobInfoModel: # is not instance of LinkisJobDataImpl(初始化 LinkisJobInfoModel 失败：# 不是 LinkisJobDataImpl 的实例)",
      "Failed to init LinkisJobInfoModel: # is not instance of LinkisJobDataImpl(初始化 LinkisJobInfoModel 失败：# 不是 LinkisJobDataImpl 的实例)"),
  CAN_ONLY_SPECIFY(
      "VLD0001",
      "Can only specify 1 of: ###(只能指定 1 个：###)",
      "Can only specify 1 of: ###(只能指定 1 个：###)"),
  STRING_ARRAY_RAW_VALUE(
      "VLD0001",
      "has raw-value but failed to convert it into String-array. Raw-value:###(具有原始值，但未能将其转换为字符串数组。 原始值：###)",
      "has raw-value but failed to convert it into String-array. Raw-value:###(具有原始值，但未能将其转换为字符串数组。 原始值：###)"),
  ILLEGAL_ARGUMENT(
      "CMD0011", "Illegal argument::###(非法论据：：###)", "Illegal argument::###(非法论据：：###)"),
  OPTION_ASSUME_SCRIPT(
      "VLD0001",
      "Argument:# is not a linkis-cli option. Assume it's script file, but no file named  is found (参数：# 不是 linkis-cli 选项。 假设它是脚本文件，但没有找到名为的文件)",
      "Argument:# is not a linkis-cli option. Assume it's script file, but no file named  is found (参数：# 不是 linkis-cli 选项。 假设它是脚本文件，但没有找到名为的文件)"),
  SPECIFY_AT_MOST(
      "VLD0001",
      "Can only specify at most one of linkis-cli option: ###(最多只能指定一个 linkis-cli 选项：)",
      "Can only specify at most one of linkis-cli option: ###(最多只能指定一个 linkis-cli 选项：)"),
  SPECIFY_AT_LEASTT(
      "VLD0001",
      "Need to specify at least one of linkis-cli option:# or  script-path and script-arguments (需要指定至少一个 linkis-cli 选项：# 或 script-path 和 script-arguments)",
      "Need to specify at least one of linkis-cli option:# or  script-path and script-arguments (需要指定至少一个 linkis-cli 选项：# 或 script-path 和 script-arguments)"),
  NOT_A_INSTANCE(
      "VLD0007",
      "Input of LinkisSubmitValidator is not instance of LinkisManageJob. Type: #(LinkisSubmitValidator 的输入不是 LinkisManageJob 的实例。 类型：#)",
      "Input of LinkisSubmitValidator is not instance of LinkisManageJob. Type:# (LinkisSubmitValidator 的输入不是 LinkisManageJob 的实例。 类型：#)"),
  VALIDATION_FAILED(
      "VLD0008",
      "LinkisJobMan validation failed. Reason: #(LinkisJobMan 验证失败。 原因：#)",
      "LinkisJobMan validation failed. Reason: # (LinkisJobMan 验证失败。 原因：#)"),
  NOT_A_INSTANCE_SUBMITJOB(
      "VLD0007",
      "Input of LinkisSubmitValidator is not instance of LinkisSubmitJob. Type: #(LinkisSubmitValidator 的输入不是 LinkisSubmitJob 的实例。 类型：#)",
      "Input of LinkisSubmitValidator is not instance of LinkisSubmitJob. Type:# (LinkisSubmitValidator 的输入不是 LinkisSubmitJob 的实例。 类型：#)"),
  NOT_A_INSTANCE_UJESCLIENT(
      "VLD0009",
      "Input of UJESContextValidator is not instance of UjesClientDriverContext. Type: #(UJESContextValidator 的输入不是 UjesClientDriverContext 的实例。 类型：#)",
      "Input of UJESContextValidator is not instance of UjesClientDriverContext. Type:# (UJESContextValidator 的输入不是 UjesClientDriverContext 的实例。 类型：#)"),
  VALIDATION_FAILED_JOB(
      "VLD0010",
      "LinkisJob validation failed. Reason: #(LinkisJob 验证失败。 原因：#)",
      "LinkisJob validation failed. Reason: # (LinkisJob 验证失败。 原因：#)");

  /** error code(错误码) */
  private String errorCode;
  /** wrong description(错误描述 ) */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因 ) */
  private String comment;

  LinkisCliApplicationErrorCodeSummary(String errorCode, String errorDesc, String comment) {
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
