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

import org.apache.linkis.manager.common.entity.enumeration.NodeHealthy;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.label.entity.Label;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AMEngineNodeVo {

  private String emInstance;

  private NodeStatus nodeStatus;

  private String lock;

  private List<Label<?>> labels;

  private String applicationName;

  private String instance;

  private ResourceType resourceType;

  private Map maxResource;

  private Map minResource;

  private Map usedResource;

  private Map lockedResource;

  private Map expectedResource;

  private Map leftResource;

  private String owner;

  private Integer runningTasks;

  private Integer pendingTasks;

  private Integer succeedTasks;

  private Integer failedTasks;

  private Long maxMemory;

  private Long usedMemory;

  private Float systemCPUUsed;

  private Long systemLeftMemory;

  private NodeHealthy nodeHealthy;

  private String msg;

  private Date startTime;

  private String engineType;

  public String getEmInstance() {
    return emInstance;
  }

  public void setEmInstance(String emInstance) {
    this.emInstance = emInstance;
  }

  public NodeStatus getNodeStatus() {
    return nodeStatus;
  }

  public void setNodeStatus(NodeStatus nodeStatus) {
    this.nodeStatus = nodeStatus;
  }

  public String getLock() {
    return lock;
  }

  public void setLock(String lock) {
    this.lock = lock;
  }

  public List<Label<?>> getLabels() {
    return labels;
  }

  public void setLabels(List<Label<?>> labels) {
    this.labels = labels;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public ResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  public Map getMaxResource() {
    return maxResource;
  }

  public void setMaxResource(Map maxResource) {
    this.maxResource = maxResource;
  }

  public Map getMinResource() {
    return minResource;
  }

  public void setMinResource(Map minResource) {
    this.minResource = minResource;
  }

  public Map getUsedResource() {
    return usedResource;
  }

  public void setUsedResource(Map usedResource) {
    this.usedResource = usedResource;
  }

  public Map getLockedResource() {
    return lockedResource;
  }

  public void setLockedResource(Map lockedResource) {
    this.lockedResource = lockedResource;
  }

  public Map getExpectedResource() {
    return expectedResource;
  }

  public void setExpectedResource(Map expectedResource) {
    this.expectedResource = expectedResource;
  }

  public Map getLeftResource() {
    return leftResource;
  }

  public void setLeftResource(Map leftResource) {
    this.leftResource = leftResource;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Integer getRunningTasks() {
    return runningTasks;
  }

  public void setRunningTasks(Integer runningTasks) {
    this.runningTasks = runningTasks;
  }

  public Integer getPendingTasks() {
    return pendingTasks;
  }

  public void setPendingTasks(Integer pendingTasks) {
    this.pendingTasks = pendingTasks;
  }

  public Integer getSucceedTasks() {
    return succeedTasks;
  }

  public void setSucceedTasks(Integer succeedTasks) {
    this.succeedTasks = succeedTasks;
  }

  public Integer getFailedTasks() {
    return failedTasks;
  }

  public void setFailedTasks(Integer failedTasks) {
    this.failedTasks = failedTasks;
  }

  public Long getMaxMemory() {
    return maxMemory;
  }

  public void setMaxMemory(Long maxMemory) {
    this.maxMemory = maxMemory;
  }

  public Long getUsedMemory() {
    return usedMemory;
  }

  public void setUsedMemory(Long usedMemory) {
    this.usedMemory = usedMemory;
  }

  public Float getSystemCPUUsed() {
    return systemCPUUsed;
  }

  public void setSystemCPUUsed(Float systemCPUUsed) {
    this.systemCPUUsed = systemCPUUsed;
  }

  public Long getSystemLeftMemory() {
    return systemLeftMemory;
  }

  public void setSystemLeftMemory(Long systemLeftMemory) {
    this.systemLeftMemory = systemLeftMemory;
  }

  public NodeHealthy getNodeHealthy() {
    return nodeHealthy;
  }

  public void setNodeHealthy(NodeHealthy nodeHealthy) {
    this.nodeHealthy = nodeHealthy;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }
}
