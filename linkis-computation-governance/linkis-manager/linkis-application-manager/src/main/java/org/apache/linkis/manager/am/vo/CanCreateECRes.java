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

package org.apache.linkis.manager.am.vo;

import java.util.Map;

public class CanCreateECRes {

  private boolean canCreateEC = true;

  /** Need to give the reason when canCreateEc set false */
  private String reason;

  private String labelResource;

  private String ecmResource;

  private String requestResource;

  private String yarnResource;

  private Map<String, Object> labels;

  public boolean isCanCreateEC() {
    return canCreateEC;
  }

  public void setCanCreateEC(boolean canCreateEC) {
    this.canCreateEC = canCreateEC;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getLabelResource() {
    return labelResource;
  }

  public void setLabelResource(String labelResource) {
    this.labelResource = labelResource;
  }

  public String getEcmResource() {
    return ecmResource;
  }

  public void setEcmResource(String ecmResource) {
    this.ecmResource = ecmResource;
  }

  public String getRequestResource() {
    return requestResource;
  }

  public void setRequestResource(String requestResource) {
    this.requestResource = requestResource;
  }

  public Map<String, Object> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, Object> labels) {
    this.labels = labels;
  }

  public String getYarnResource() {
    return yarnResource;
  }

  public void setYarnResource(String yarnResource) {
    this.yarnResource = yarnResource;
  }

  @Override
  public String toString() {
    return "CanCreateECRes{" + "canCreateEC=" + canCreateEC + ", reason='" + reason + '\'' + '}';
  }
}
