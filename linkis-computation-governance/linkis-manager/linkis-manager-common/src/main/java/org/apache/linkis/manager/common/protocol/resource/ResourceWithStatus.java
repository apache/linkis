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

package org.apache.linkis.manager.common.protocol.resource;

/**
 * @param queueMemory memory unit is bytes
 * @param queueCores
 * @param queueInstances
 * @param jobStatus
 */
public class ResourceWithStatus {
  private long queueMemory;
  private int queueCores;
  private int queueInstances;
  private String jobStatus;
  private String queue;

  public ResourceWithStatus() {}

  public ResourceWithStatus(
      long queueMemory, int queueCores, int queueInstances, String jobStatus, String queue) {
    this.queueMemory = queueMemory;
    this.queueCores = queueCores;
    this.queueInstances = queueInstances;
    this.jobStatus = jobStatus;
    this.queue = queue;
  }

  public long getQueueMemory() {
    return queueMemory;
  }

  public int getQueueCores() {
    return queueCores;
  }

  public int getQueueInstances() {
    return queueInstances;
  }

  public String getJobStatus() {
    return jobStatus;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueueMemory(long queueMemory) {
    this.queueMemory = queueMemory;
  }

  public void setQueueCores(int queueCores) {
    this.queueCores = queueCores;
  }

  public void setQueueInstances(int queueInstances) {
    this.queueInstances = queueInstances;
  }

  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  public long queueMemory() {
    return queueMemory;
  }

  public int queueCores() {
    return queueCores;
  }

  public int queueInstances() {
    return queueInstances;
  }

  public String jobStatus() {
    return jobStatus;
  }

  public String queue() {
    return queue;
  }
}
