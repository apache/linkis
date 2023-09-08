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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Resource spec. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource {
  /** Amount of CPU allocated to the pod. */
  private Double cpu;

  /** Amount of memory allocated to the pod. Example: 1024m, 1g */
  private String memory;

  /** Amount of ephemeral storage allocated to the pod. Example: 1024m, 2G */
  private String ephemeralStorage;

  public Resource(Double cpu, String memory, String ephemeralStorage) {
    this.cpu = cpu;
    this.memory = memory;
    this.ephemeralStorage = ephemeralStorage;
  }

  public Double getCpu() {
    return cpu;
  }

  public void setCpu(Double cpu) {
    this.cpu = cpu;
  }

  public String getMemory() {
    return memory;
  }

  public void setMemory(String memory) {
    this.memory = memory;
  }

  public String getEphemeralStorage() {
    return ephemeralStorage;
  }

  public void setEphemeralStorage(String ephemeralStorage) {
    this.ephemeralStorage = ephemeralStorage;
  }

  public Resource() {}
}
