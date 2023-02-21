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

package org.apache.linkis.manager.common.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum ManagerCommonErrorCodeSummary implements LinkisErrorCode {
  RESOURCE_LATER_CREATED(
      10022,
      "The tag resource was created later than the used resource was created(无需清理该标签的资源,该标签资源的创建时间晚于已用资源的创建时间)"),
  OPERATION_MULTIPLIED(11002, "Unsupported operation: multiplied(不支持的操作：multiplied)"),
  NOT_RESOURCE_POLICY(11003, "Not supported resource result policy (不支持该资源结果策略)"),
  NOT_RESOURCE_RESULT_TYPE(11003, "Not supported resource result type(不支持该资源结果类型)"),
  NOT_RESOURCE_TYPE(11003, "Not supported resource type:{0}(不支持的资源类型)"),
  NOT_RESOURCE_STRING(11003, "Not supported resource serializable string(不支持资源可序列化字符串) "),
  FAILED_REQUEST_RESOURCE(11006, "Failed to request external resource(请求外部资源失败)"),
  YARN_QUEUE_EXCEPTION(11006, "Get the Yarn queue information exception(获取Yarn队列信息异常)"),
  YARN_APPLICATION_EXCEPTION(
      11006, "Get the Yarn Application information exception.(获取Yarn Application信息异常)"),
  YARN_NOT_EXISTS_QUEUE(11006, "Queue:{0} does not exist in YARN(YARN 中不存在队列:{0})"),
  ONLY_SUPPORT_FAIRORCAPA(
      11006,
      "Only support fairScheduler or capacityScheduler, not support schedulerType:{0}(仅支持 fairScheduler 或 capacityScheduler)"),
  GET_YARN_EXCEPTION(
      11007,
      "Get active Yarn resourcemanager from:{0} exception.(从 {0} 获取主 Yarn resourcemanager 异常)"),
  NO_RESOURCE(
      11201,
      "No resource available found for label:{0}, please check the resource in the database.(资源标签没有资源,请检查数据库中的资源)"),
  NO_RESOURCE_AVAILABLE(11201, "No resource available found for em:{0}(没有为 em 找到可用的资源)"),
  NO_FOUND_RESOURCE_TYPE(
      110012,
      "No ExternalResourceRequester found for resource type:{0}(找不到资源类型的 ExternalResourceRequester)"),
  NO_SUITABLE_CLUSTER(
      110013,
      "No suitable ExternalResourceProvider found for cluster:{0}(没有为集群找到合适的 ExternalResourceProvider)"),
  REFUSE_REQUEST(
      110022,
      "Resource label:{0} has no usedResource, please check, refuse request usedResource(资源标签：{0} 没有usedResource，请检查，拒绝请求usedResource)"),

  ONLY_ADMIN_READ(120010, "Only admin can read all user's resource.(只有管理员可以读取所有用户的资源.)"),
  ONLY_ADMIN_RESET(120011, "Only admin can reset user's resource.(只有管理员可以重置用户的资源.)");
  /** (errorCode)错误码 */
  private final int errorCode;
  /** (errorDesc)错误描述 */
  private final String errorDesc;

  ManagerCommonErrorCodeSummary(int errorCode, String errorDesc) {
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
