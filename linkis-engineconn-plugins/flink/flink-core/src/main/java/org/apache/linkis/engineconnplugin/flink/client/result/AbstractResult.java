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

package org.apache.linkis.engineconnplugin.flink.client.result;

import org.apache.linkis.engineconnplugin.flink.listener.FlinkListenerGroupImpl;

public abstract class AbstractResult<C, R> extends FlinkListenerGroupImpl implements Result<C, R> {

  protected C clusterId;
  protected String webInterfaceUrl;

  @Override
  public void setClusterInformation(C clusterId, String webInterfaceUrl) {
    if (this.clusterId != null || this.webInterfaceUrl != null) {
      throw new IllegalStateException("Cluster information is already present.");
    }
    this.clusterId = clusterId;
    this.webInterfaceUrl = webInterfaceUrl;
  }

  public C getClusterId() {
    if (this.clusterId == null) {
      throw new IllegalStateException("Cluster ID has not been set.");
    }
    return clusterId;
  }

  public String getWebInterfaceUrl() {
    if (this.webInterfaceUrl == null) {
      throw new IllegalStateException("Cluster web interface URL has not been set.");
    }
    return webInterfaceUrl;
  }
}
