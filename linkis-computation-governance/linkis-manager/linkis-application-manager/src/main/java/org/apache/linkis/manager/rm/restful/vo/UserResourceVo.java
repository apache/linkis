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

package org.apache.linkis.manager.rm.restful.vo;

import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.rm.conf.ResourceStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class UserResourceVo implements Serializable {

  private Integer id;

  private String username;

  private String creator;

  private String engineTypeWithVersion;

  private ResourceType resourceType;

  private Map maxResource;

  private Map minResource;

  private Map usedResource;

  private Map lockedResource;

  private Map expectedResource;

  private Map leftResource;

  private Date createTime;

  private Date updateTime;

  private ResourceStatus loadResourceStatus;

  private ResourceStatus queueResourceStatus;

  public ResourceStatus getLoadResourceStatus() {
    return loadResourceStatus;
  }

  public void setLoadResourceStatus(ResourceStatus loadResourceStatus) {
    this.loadResourceStatus = loadResourceStatus;
  }

  public ResourceStatus getQueueResourceStatus() {
    return queueResourceStatus;
  }

  public void setQueueResourceStatus(ResourceStatus queueResourceStatus) {
    this.queueResourceStatus = queueResourceStatus;
  }

  public Integer getId() {
    return id;
  }

  public String getEngineTypeWithVersion() {
    return engineTypeWithVersion;
  }

  public void setEngineTypeWithVersion(String engineTypeWithVersion) {
    this.engineTypeWithVersion = engineTypeWithVersion;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
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

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }
}
