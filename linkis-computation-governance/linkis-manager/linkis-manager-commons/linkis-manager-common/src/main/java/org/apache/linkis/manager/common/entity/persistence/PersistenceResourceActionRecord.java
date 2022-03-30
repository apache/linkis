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

package org.apache.linkis.manager.common.entity.persistence;

import org.apache.linkis.manager.common.entity.resource.Resource;
import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.common.utils.ResourceUtils;

import java.util.Date;

public class PersistenceResourceActionRecord {

    private Integer id;

    private String labelValue;

    private String ticketId;

    private Integer requestTimes;

    private String requestResourceAll;

    private Integer usedTimes;

    private String usedResourceAll;

    private Integer releaseTimes;

    private String releaseResourceAll;

    private Date updateTime;

    private Date createTime;

    public PersistenceResourceActionRecord() {}

    public PersistenceResourceActionRecord(String labelValue, String ticketId, Resource resource) {
        this.labelValue = labelValue;
        this.ticketId = ticketId;
        this.requestTimes = 0;
        this.requestResourceAll =
                ResourceUtils.serializeResource(Resource.initResource(ResourceType.LoadInstance));
        this.usedTimes = 0;
        this.usedResourceAll =
                ResourceUtils.serializeResource(Resource.initResource(ResourceType.LoadInstance));
        this.releaseTimes = 0;
        this.releaseResourceAll =
                ResourceUtils.serializeResource(Resource.initResource(ResourceType.LoadInstance));
        this.updateTime = new Date(System.currentTimeMillis());
        this.createTime = new Date(System.currentTimeMillis());
    }

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

    public String getRequestResourceAll() {
        return requestResourceAll;
    }

    public void setRequestResourceAll(String requestResourceAll) {
        this.requestResourceAll = requestResourceAll;
    }

    public Integer getUsedTimes() {
        return usedTimes;
    }

    public void setUsedTimes(Integer usedTimes) {
        this.usedTimes = usedTimes;
    }

    public String getUsedResourceAll() {
        return usedResourceAll;
    }

    public void setUsedResourceAll(String usedResourceAll) {
        this.usedResourceAll = usedResourceAll;
    }

    public Integer getReleaseTimes() {
        return releaseTimes;
    }

    public void setReleaseTimes(Integer releaseTimes) {
        this.releaseTimes = releaseTimes;
    }

    public String getReleaseResourceAll() {
        return releaseResourceAll;
    }

    public void setReleaseResourceAll(String releaseResourceAll) {
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
