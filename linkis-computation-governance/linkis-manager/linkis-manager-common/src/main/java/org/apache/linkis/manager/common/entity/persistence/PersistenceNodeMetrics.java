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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;

import java.util.Date;
import java.util.Objects;

public class PersistenceNodeMetrics implements NodeMetrics {

  private String instance;

  private int status;
  private String overLoad;
  private String heartBeatMsg;
  private String healthy;

  private Date updateTime;
  private Date createTime;

  private ServiceInstance serviceInstance;

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public void setServiceInstance(ServiceInstance serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  @Override
  public ServiceInstance getServiceInstance() {
    return this.serviceInstance;
  }

  @Override
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    if (Objects.nonNull(status)) {
      this.status = status;
    }
  }

  @Override
  public String getOverLoad() {
    return overLoad;
  }

  public void setOverLoad(String overLoad) {
    this.overLoad = overLoad;
  }

  @Override
  public String getHeartBeatMsg() {
    return heartBeatMsg;
  }

  public void setHeartBeatMsg(String heartBeatMsg) {
    this.heartBeatMsg = heartBeatMsg;
  }

  @Override
  public String getHealthy() {
    return healthy;
  }

  @Override
  public void setHealthy(String healthy) {
    this.healthy = healthy;
  }

  @Override
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
