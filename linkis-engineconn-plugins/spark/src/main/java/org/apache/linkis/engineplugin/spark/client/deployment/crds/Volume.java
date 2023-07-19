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

package org.apache.linkis.engineplugin.spark.client.deployment.crds;

public class Volume {

  private String name;

  private HostPath hostPath;

  private ConfigMap configMap;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public HostPath getHostPath() {
    return hostPath;
  }

  public void setHostPath(HostPath hostPath) {
    this.hostPath = hostPath;
  }

  public ConfigMap getConfigMap() {
    return configMap;
  }

  public void setConfigMap(ConfigMap configMap) {
    this.configMap = configMap;
  }
}
