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

package org.apache.linkis.gateway.errorcode;

public enum LinkisGatewayCoreErrorCodeSummary {
  CANNOT_SERVICEID(
      11010,
      "Cannot find a correct serviceId for parsedServiceId {}, service list is:{}(无法为 parsedServiceId {} 找到正确的 serviceId，服务列表为：{})",
      "Cannot find a correct serviceId for parsedServiceId {}, service list is:{}(无法为 parsedServiceId {} 找到正确的 serviceId，服务列表为：{})"),
  CANNOT_ROETE_SERVICE(
      11011,
      "Cannot route to the corresponding service, URL: {} RouteLabel: {}(无法路由到相应的服务，URL：{} RouteLabel: {})",
      "Cannot route to the corresponding service, URL: {} RouteLabel: {}(无法路由到相应的服务，URL：{} RouteLabel: {})"),
  NO_SERVICES_REGISTRY(
      11011,
      "There are no services available in the registry URL (注册表 URL 中没有可用的服务):",
      "There are no services available in the registry URL (注册表 URL 中没有可用的服务):"),
  NO_ROUTE_SERVICE(
      11011,
      "There is no route label service with the corresponding app name (没有对应app名称的路由标签服务)",
      "There is no route label service with the corresponding app name (没有对应app名称的路由标签服务)"),
  CANNOT_INSTANCE(
      11012,
      "Cannot find an instance in the routing chain of serviceId {} , please retry (在 serviceId {} 的路由链中找不到实例，请重试)",
      "Cannot find an instance in the routing chain of serviceId {} , please retry(在 serviceId {} 的路由链中找不到实例，请重试)"),
  GET_REQUESTBODY_FAILED(
      18000,
      "get requestBody failed!(获取 requestBody 失败！)",
      "get requestBody failed!(获取 requestBody 失败！)");
  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  LinkisGatewayCoreErrorCodeSummary(int errorCode, String errorDesc, String comment) {
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
