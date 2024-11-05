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

package org.apache.linkis.manager.common.entity.resource;

import java.util.Date;

public class CommonNodeResource implements NodeResource {

  private Integer id;

  private ResourceType resourceType;

  private Resource maxResource;

  private Resource minResource;

  private Resource usedResource;

  private Resource lockedResource;

  private Resource expectedResource;

  private Resource leftResource;

  private Date createTime;

  private Date updateTime;

  private Integer maxApps;

  private Integer numPendingApps;

  private Integer numActiveApps;

  public static NodeResource initNodeResource(ResourceType resourceType) {
    CommonNodeResource commonNodeResource = new CommonNodeResource();
    commonNodeResource.setResourceType(resourceType);
    Resource zeroResource = Resource.initResource(resourceType);
    commonNodeResource.setMaxResource(zeroResource);
    commonNodeResource.setMinResource(zeroResource);
    commonNodeResource.setUsedResource(zeroResource);
    commonNodeResource.setLockedResource(zeroResource);
    commonNodeResource.setExpectedResource(zeroResource);
    commonNodeResource.setLeftResource(zeroResource);
    return commonNodeResource;
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public ResourceType getResourceType() {
    return resourceType;
  }

  @Override
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public Date getCreateTime() {
    return createTime;
  }

  @Override
  public Date getUpdateTime() {
    return updateTime;
  }

  @Override
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  @Override
  public Resource getMaxResource() {
    return maxResource;
  }

  @Override
  public void setMaxResource(Resource maxResource) {
    this.maxResource = maxResource;
  }

  @Override
  public Resource getMinResource() {
    return minResource;
  }

  @Override
  public void setMinResource(Resource minResource) {
    this.minResource = minResource;
  }

  @Override
  public Resource getUsedResource() {
    return usedResource;
  }

  @Override
  public void setUsedResource(Resource usedResource) {
    this.usedResource = usedResource;
  }

  @Override
  public Resource getLockedResource() {
    return lockedResource;
  }

  @Override
  public void setLockedResource(Resource lockedResource) {
    this.lockedResource = lockedResource;
  }

  @Override
  public Resource getExpectedResource() {
    return expectedResource;
  }

  @Override
  public void setExpectedResource(Resource expectedResource) {
    this.expectedResource = expectedResource;
  }

  @Override
  public Resource getLeftResource() {
    if (this.leftResource == null && getMaxResource() != null && getUsedResource() != null) {
      return getMaxResource().minus(getUsedResource());
    } else {
      return this.leftResource;
    }
  }

  @Override
  public void setLeftResource(Resource leftResource) {
    this.leftResource = leftResource;
  }

  public Integer getMaxApps() {
    return maxApps;
  }

  public void setMaxApps(Integer maxApps) {
    this.maxApps = maxApps;
  }

  public Integer getNumPendingApps() {
    return numPendingApps;
  }

  public void setNumPendingApps(Integer numPendingApps) {
    this.numPendingApps = numPendingApps;
  }

  public Integer getNumActiveApps() {
    return numActiveApps;
  }

  public void setNumActiveApps(Integer numActiveApps) {
    this.numActiveApps = numActiveApps;
  }

  @Override
  public String toString() {
    return "CommonNodeResource{"
        + "resourceType="
        + resourceType
        + ", maxResource="
        + maxResource
        + ", minResource="
        + minResource
        + ", usedResource="
        + usedResource
        + ", lockedResource="
        + lockedResource
        + ", expectedResource="
        + expectedResource
        + ", leftResource="
        + leftResource
        + '}';
  }
}
