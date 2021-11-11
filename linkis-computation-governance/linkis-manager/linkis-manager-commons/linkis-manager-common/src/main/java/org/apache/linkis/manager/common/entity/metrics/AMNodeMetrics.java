/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.manager.common.entity.metrics;

import org.apache.linkis.common.ServiceInstance;

import java.util.Date;


public class AMNodeMetrics implements NodeMetrics {

    private Integer status;

    private String overLoad;

    private String heartBeatMsg;

    private String healthy;

    private ServiceInstance serviceInstance;

    private Date updateTime;


    @Override
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setHealthy(String healthy) {
        this.healthy = healthy;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
