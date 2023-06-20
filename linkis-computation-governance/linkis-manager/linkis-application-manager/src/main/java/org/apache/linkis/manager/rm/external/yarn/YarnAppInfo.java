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

package org.apache.linkis.manager.rm.external.yarn;

import org.apache.linkis.manager.common.entity.resource.YarnResource;
import org.apache.linkis.manager.rm.external.domain.ExternalAppInfo;

public class YarnAppInfo implements ExternalAppInfo {
  private String id;
  private String user;
  private String status;
  private String applicationType;
  private YarnResource usedResource;

  public YarnAppInfo(
      String id, String user, String status, String applicationType, YarnResource usedResource) {
    this.id = id;
    this.user = user;
    this.status = status;
    this.applicationType = applicationType;
    this.usedResource = usedResource;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(String applicationType) {
    this.applicationType = applicationType;
  }

  public YarnResource getUsedResource() {
    return usedResource;
  }

  public void setUsedResource(YarnResource usedResource) {
    this.usedResource = usedResource;
  }
}
