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

package org.apache.linkis.manager.common.entity.persistence;

import java.util.Date;

public class PersistenceResource {
  private int id;
  private String maxResource;
  private String minResource;
  private String usedResource;
  private String leftResource;
  private String expectedResource;
  private String lockedResource;

  private String resourceType;

  private String labelKey;

  private String labelValue;

  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  private String ticketId;

  private Date updateTime;
  private Date createTime;
  private String updator;
  private String creator;

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getMaxResource() {
    return maxResource;
  }

  public void setMaxResource(String maxResource) {
    this.maxResource = maxResource;
  }

  public String getMinResource() {
    return minResource;
  }

  public void setMinResource(String minResource) {
    this.minResource = minResource;
  }

  public String getUsedResource() {
    return usedResource;
  }

  public void setUsedResource(String usedResource) {
    this.usedResource = usedResource;
  }

  public String getLeftResource() {
    return leftResource;
  }

  public void setLeftResource(String leftResource) {
    this.leftResource = leftResource;
  }

  public String getExpectedResource() {
    return expectedResource;
  }

  public void setExpectedResource(String expectedResource) {
    this.expectedResource = expectedResource;
  }

  public String getLockedResource() {
    return lockedResource;
  }

  public void setLockedResource(String lockedResource) {
    this.lockedResource = lockedResource;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getUpdator() {
    return updator;
  }

  public void setUpdator(String updator) {
    this.updator = updator;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getLabelKey() {
    return labelKey;
  }

  public void setLabelKey(String labelKey) {
    this.labelKey = labelKey;
  }

  public String getLabelValue() {
    return labelValue;
  }

  public void setLabelValue(String labelValue) {
    this.labelValue = labelValue;
  }
}
