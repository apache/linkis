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

package org.apache.linkis.datasourcemanager.common.protocol;

import org.apache.linkis.protocol.message.RequestProtocol;

import java.util.HashMap;
import java.util.Map;

public class DsInfoResponse implements RequestProtocol {
  private Boolean status;
  private String dsType;

  private Map<String, Object> params;
  private String creator;
  private String errorMsg;

  public DsInfoResponse() {
    this(false);
  }

  public DsInfoResponse(Boolean status) {
    this(status, "");
  }

  public DsInfoResponse(Boolean status, String errorMsg) {
    this(status, "", new HashMap(), "", errorMsg);
  }

  public DsInfoResponse(
      Boolean status, String dsType, Map<String, Object> params, String creator, String errorMsg) {
    this.status = status;
    this.dsType = dsType;
    this.params = params;
    this.creator = creator;
    this.errorMsg = errorMsg;
  }

  public Boolean getStatus() {
    return status;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return status
   */
  @Deprecated
  public Boolean status() {
    return status;
  }

  public void setStatus(Boolean status) {
    this.status = status;
  }

  public String getDsType() {
    return dsType;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return dsType
   */
  @Deprecated
  public String dsType() {
    return dsType;
  }

  public void setDsType(String dsType) {
    this.dsType = dsType;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return params
   */
  @Deprecated
  public Map<String, Object> params() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public String getCreator() {
    return creator;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return creator
   */
  @Deprecated
  public String creator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  /**
   * compatible with scala case class
   *
   * @deprecated not recommend
   * @return errorMsg
   */
  @Deprecated
  public String errorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(String errorMsg) {
    this.errorMsg = errorMsg;
  }
}
