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

package org.apache.linkis.bml.client.errorcode;

public enum LinkisBmlClientErrorCodeSummary {
  MATERIAL_HOUSE(
      70025,
      "material house client request failed(物料库客户端请求失败)",
      "material house client request failed(物料库客户端请求失败)"),
  RETURNED_BY_THE(
      70021,
      "The result returned by the repository client POST request does not match(物料库客户端POST请求返回的result不匹配)",
      "The result returned by the repository client POST request does not match(物料库客户端POST请求返回的result不匹配)"),
  CATALOG_PASSED(
      70035,
      "The catalog that was passed into the store does not exist or is illegal(传入物料库的目录不存在或非法)",
      "The catalog that was passed into the store does not exist or is illegal(传入物料库的目录不存在或非法)"),
  BML_ERROR_EXCEP(70038, "", ""),
  RETURNED_CLIENT_MATCH(
      70078,
      "The result returned by the repository client GET request does not match(物料库客户端GET请求返回的result不匹配)",
      "The result returned by the repository client GET request does not match(物料库客户端GET请求返回的result不匹配)"),
  AN_ERROR_OCCURRED(
      70081,
      "An error occurred in the material client(物料库客户端出现错误)",
      "An error occurred in the material client(物料库客户端出现错误)"),
  SERVERUEL_CANNOT_BE_NULL(
      70081,
      "serverUrl cannot be null.(serverUrl 不能为空.)",
      "serverUrl cannot be null.(serverUrl 不能为空.)"),
  FAILED_COPY_INPUTSTREAM(
      70081,
      "failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)",
      "failed to copy inputStream and outputStream (inputStream和outputStream流copy失败)");

  /** (errorCode)错误码 */
  private int errorCode;
  /** (errorDesc)错误描述 */
  private String errorDesc;
  /** Possible reasons for the error(错误可能出现的原因) */
  private String comment;

  LinkisBmlClientErrorCodeSummary(int errorCode, String errorDesc, String comment) {
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
