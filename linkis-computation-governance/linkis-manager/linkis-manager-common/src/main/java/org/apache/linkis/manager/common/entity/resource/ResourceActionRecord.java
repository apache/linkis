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

public class ResourceActionRecord {

  private Integer id;

  private String labelValue;

  private String ticketId;

  private Integer requestTimes;

  private Resource requestResourceAll;

  private Integer usedTimes;

  private Resource usedResourceAll;

  private Integer releaseTimes;

  private Resource releaseResourceAll;

  private Date updateTime;

  private Date createTime;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getLabelValue() {
    return labelValue;
  }

  public void setLabelValue(String labelValue) {
    this.labelValue = labelValue;
  }

  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  public Integer getRequestTimes() {
    return requestTimes;
  }

  public void setRequestTimes(Integer requestTimes) {
    this.requestTimes = requestTimes;
  }

  public Resource getRequestResourceAll() {
    return requestResourceAll;
  }

  public void setRequestResourceAll(Resource requestResourceAll) {
    this.requestResourceAll = requestResourceAll;
  }

  public Integer getUsedTimes() {
    return usedTimes;
  }

  public void setUsedTimes(Integer usedTimes) {
    this.usedTimes = usedTimes;
  }

  public Resource getUsedResourceAll() {
    return usedResourceAll;
  }

  public void setUsedResourceAll(Resource usedResourceAll) {
    this.usedResourceAll = usedResourceAll;
  }

  public Integer getReleaseTimes() {
    return releaseTimes;
  }

  public void setReleaseTimes(Integer releaseTimes) {
    this.releaseTimes = releaseTimes;
  }

  public Resource getReleaseResourceAll() {
    return releaseResourceAll;
  }

  public void setReleaseResourceAll(Resource releaseResourceAll) {
    this.releaseResourceAll = releaseResourceAll;
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
}
