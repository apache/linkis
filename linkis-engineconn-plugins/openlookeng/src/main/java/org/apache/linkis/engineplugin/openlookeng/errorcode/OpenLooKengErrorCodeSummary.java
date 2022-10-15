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

package org.apache.linkis.engineplugin.openlookeng.errorcode;

import org.apache.linkis.common.errorcode.ErrorCodeUtils;

public enum OpenLooKengErrorCodeSummary {
  /**
   * 10000-10999 linkis-frame 11000-12999 linkis-commons 13000-14999 linkis-spring-cloud-services
   * 15000-19999 linkis-public-enhancements 20000-24999 linkis-computation-governance 25000-25999
   * linkis-extensions 26000-29999 linkis-engineconn-plugins
   */
  OPENLOOKENG_CLIENT_ERROR(
      26030,
      "openlookeng client error(openlookeng客户端异常)",
      "openlookeng client is abnormal due to some circumstances(openlookeng client由于某些情况异常)",
      "jdbcEngineConnExecutor"),

  OPENLOOKENG_STATUS_ERROR(
      26031,
      "openlookeng status error,Statement is not finished(openlookeng状态异常, 查询语句未完成)",
      "The status of openlookeng is abnormal, and the query statement cannot be executed and ended(openlookeng状态出现异常，查询语句无法执行结束)",
      "jdbcEngineConnExecutor");

  private int errorCode;

  private String errorDesc;

  private String comment;

  private String module;

  OpenLooKengErrorCodeSummary(int errorCode, String errorDesc, String comment, String module) {
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
