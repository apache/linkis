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

package org.apache.linkis.monitor.bml.cleaner.vo;

public class CleanResourceVo {
  private String resourceId;
  private int versionCount;
  private String maxVersion;

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public int getVersionCount() {
    return versionCount;
  }

  public void setVersionCount(int versionCount) {
    this.versionCount = versionCount;
  }

  public String getMaxVersion() {
    return maxVersion;
  }

  public void setMaxVersion(String maxVersion) {
    this.maxVersion = maxVersion;
  }
}
