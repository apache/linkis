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

package org.apache.linkis.gateway.springcloud.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;

public enum GatewayErrorCodeSummary {
  /**
   * 10000-10999 linkis-frame 11000-12999 linkis-commons 13000-14999 linkis-spring-cloud-services
   * 15000-19999 linkis-public-enhancements 20000-24999 linkis-computation-governance 25000-25999
   * linkis-extensions 26000-29999 linkis-engineconn-plugins
   */
  WEBSOCKET_CONNECT_ERROR(
      13001,
      "Repeatedly creating a WebSocket connection(重复创建WebSocket连接)",
      "The service instance has created a WebSocket connection before, and cannot be created repeatedly(服务实例之前已经创建过WebSocket连接, 不能重复创建)",
      "linkis-spring-cloud-gateway");

  private int errorCode;

  private String errorDesc;

  private String comment;

  private String module;

  GatewayErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
    ErrorCodeUtils.validateErrorCode(errorCode, 13000, 14999); // 13000-14999
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
    this.comment = comment;
    this.module = module;
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

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  @Override
  public String toString() {
    return "errorCode: " + this.errorCode + ", errorDesc:" + this.errorDesc;
  }
}
