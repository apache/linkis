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

package org.apache.linkis.instance.label.entity;

import org.apache.linkis.common.ServiceInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Extends ServiceInstance, store the relationship of label */
public class InstanceInfo extends ServiceInstance {
  /** Automatic increment */
  private Integer id;

  private Date updateTime;

  private Date createTime;
  /** Labels related */
  private List<InsPersistenceLabel> labels = new ArrayList<>();

  public InstanceInfo() {}

  public InstanceInfo(ServiceInstance serviceInstance) {
    super.setInstance(serviceInstance.getInstance());
    super.setApplicationName(serviceInstance.getApplicationName());
  }

  public List<InsPersistenceLabel> getLabels() {
    return labels;
  }

  public void setLabels(List<InsPersistenceLabel> labels) {
    this.labels = labels;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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
