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

package org.apache.linkis.engineplugin.presto.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;

public enum PrestoErrorCodeSummary {
  /**
   * 10000-10999 linkis-frame 11000-12999 linkis-commons 13000-14999 linkis-spring-cloud-services
   * 15000-19999 linkis-public-enhancements 20000-24999 linkis-computation-governance 25000-25999
   * linkis-extensions 26000-29999 linkis-engineconn-plugins
   */
  PRESTO_STATE_INVALID(
      26001,
      "Presto status error,statement is not finished(Presto服务状态异常, 查询语句没有执行结束)",
      "The presto service is abnormal, resulting in the query statement not being completed(presto服务出现异常，导致查询语句未执行完成)",
      "prestoEngine"),
  PRESTO_CLIENT_ERROR(
      26002,
      "Presto client error(Presto客户端异常)",
      "Presto client is abnormal for some reason(presto客户端由于某些原因异常)",
      "prestoEngine");

  private int errorCode;

  private String errorDesc;

  private String comment;

  private String module;

  PrestoErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
    ErrorCodeUtils.validateErrorCode(errorCode, 26000, 29999);
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
