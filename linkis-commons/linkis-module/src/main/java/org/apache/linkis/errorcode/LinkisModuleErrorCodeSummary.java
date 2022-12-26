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

package org.apache.linkis.errorcode;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum LinkisModuleErrorCodeSummary implements LinkisErrorCode {
  DATAWORKCLOUD_MUST_VERSION(
      10010,
      "DataWorkCloud service must set the version, please add property [wds.linkis.server.version] to properties file(DataWorkCloud 服务必须设置版本，请将属性 [wds.linkis.server.version] 添加到属性文件)."),
  FETCH_MAPCACHE_ERROR(
      10021,
      "Failed to get user parameters,because RPC request{0} Service failed(获取用户参数失败！因为 RPC 请求{0}服务失败)!"),
  NOT_EXISTS_APPLICATION(10050, "Application {0} does not exist any instances(应用程序 {0} 不存在任何实例)"),
  HAVE_NOT_SET(
      11000,
      "The wds.linkis.server.home或BDP_SERVER_HOME haven't set(wds.linkis.server.home 或 BDP_SERVER_HOME 没有设置)!"),
  VERIFICATION_CANNOT_EMPTY(11001, "Verification failed,{0} cannot be empty!(验证失败,{0} 不能为空！)"),
  LOGGED_ID(11002, ""),
  NOT_LOGGED(11002, "Login has expired, please log in again!(登录已过期，请重新登录！)"),
  ILLEGAL_ID(11003, ""),
  ILLEGAL_USER_TOKEN(11003, "Illegal user token information(非法的用户token信息)."),
  SERVERSOCKET_NOT_EXIST(11004, "ServerSocket:{0} does not exist!(ServerSocket:{0}不存在！)"),
  WEBSOCKET_IS_FULL(
      11005,
      "The receive queue for WebSocket is full, please try again later(WebSocket的接收队列已满，请稍后重试)!"),
  WEBSOCKET_STOPPED(
      11035,
      "WebSocket consumer has stopped, please contact the administrator to handle(WebSocket的消费器已停止，请联系管理员处理)!");

  /** error code(错误码) */
  private final int errorCode;
  /** wrong description(错误描述 ) */
  private final String errorDesc;

  LinkisModuleErrorCodeSummary(int errorCode, String errorDesc) {
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
