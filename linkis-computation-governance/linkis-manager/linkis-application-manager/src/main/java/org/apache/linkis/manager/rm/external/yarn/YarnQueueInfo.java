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

public class YarnQueueInfo {

  public YarnQueueInfo(
      YarnResource maxResource,
      YarnResource usedResource,
      int maxApps,
      int numPendingApps,
      int numActiveApps) {
    this.maxResource = maxResource;
    this.usedResource = usedResource;
    this.maxApps = maxApps;
    this.numPendingApps = numPendingApps;
    this.numActiveApps = numActiveApps;
  }

  private YarnResource maxResource;

  private YarnResource usedResource;

  private int maxApps;

  private int numPendingApps;

  private int numActiveApps;

  public YarnResource getMaxResource() {
    return maxResource;
  }

  public void setMaxResource(YarnResource maxResource) {
    this.maxResource = maxResource;
  }

  public YarnResource getUsedResource() {
    return usedResource;
  }

  public void setUsedResource(YarnResource usedResource) {
    this.usedResource = usedResource;
  }

  public int getMaxApps() {
    return maxApps;
  }

  public void setMaxApps(int maxApps) {
    this.maxApps = maxApps;
  }

  public int getNumPendingApps() {
    return numPendingApps;
  }

  public void setNumPendingApps(int numPendingApps) {
    this.numPendingApps = numPendingApps;
  }

  public int getNumActiveApps() {
    return numActiveApps;
  }

  public void setNumActiveApps(int numActiveApps) {
    this.numActiveApps = numActiveApps;
  }
}
