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

package org.apache.linkis.manager.common.protocol.bml;

import java.io.Serializable;

public class BmlResource implements Serializable {
  private String fileName;
  private String resourceId;
  private String version;
  private BmlResourceVisibility visibility;
  private String visibleLabels;
  private String owner;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public BmlResourceVisibility getVisibility() {
    return visibility;
  }

  public void setVisibility(BmlResourceVisibility visibility) {
    this.visibility = visibility;
  }

  public String getVisibleLabels() {
    return visibleLabels;
  }

  public void setVisibleLabels(String visibleLabels) {
    this.visibleLabels = visibleLabels;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public static enum BmlResourceVisibility {
    Public,
    Private,
    Label
  }
}
