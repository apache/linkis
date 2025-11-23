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

package org.apache.linkis.entrance.vo;

import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus;

import java.io.Serializable;

public class YarnResourceWithStatusVo implements Serializable {

  public Long queueMemory;

  public Integer queueCores;

  public Integer queueInstances;

  public String jobStatus;

  public String applicationId;

  public String queue;

  public YarnResourceWithStatusVo(String applicationId, ResourceWithStatus resourceWithStatus) {
    this.queueMemory = resourceWithStatus.queueMemory();
    this.queueCores = resourceWithStatus.queueCores();
    this.queueInstances = resourceWithStatus.queueInstances();
    this.jobStatus = resourceWithStatus.jobStatus();
    this.applicationId = applicationId;
    this.queue = resourceWithStatus.queue();
  }

  public YarnResourceWithStatusVo(
      String applicationId,
      Long queueMemory,
      Integer queueCores,
      Integer queueInstances,
      String jobStatus,
      String queue) {
    this.applicationId = applicationId;
    this.queueMemory = queueMemory;
    this.queueCores = queueCores;
    this.queueInstances = queueInstances;
    this.jobStatus = jobStatus;
    this.queue = queue;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public Long getQueueMemory() {
    return queueMemory;
  }

  public void setQueueMemory(Long queueMemory) {
    this.queueMemory = queueMemory;
  }

  public Integer getQueueCores() {
    return queueCores;
  }

  public void setQueueCores(Integer queueCores) {
    this.queueCores = queueCores;
  }

  public Integer getQueueInstances() {
    return queueInstances;
  }

  public void setQueueInstances(Integer queueInstances) {
    this.queueInstances = queueInstances;
  }

  public String getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }
}
