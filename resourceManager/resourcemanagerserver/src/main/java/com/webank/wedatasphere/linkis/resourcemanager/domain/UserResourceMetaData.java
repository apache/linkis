/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.resourcemanager.domain;

import java.util.Date;

/**
 * Created by shanhuang on 9/11/18.
 */
public class UserResourceMetaData {
    private Integer id;
    private String user;
    private String ticketId;
    private String creator;
    private String emApplicationName;
    private String emInstance;
    private String engineApplicationName;
    private String engineInstance;
    private String userLockedResource;
    private String userUsedResource;
    private String resourceType;
    private Long lockedTime;
    private Long usedTime;

    public UserResourceMetaData(Integer id, String user, String ticketId, String creator, String emApplicationName, String emInstance, String engineApplicationName, String engineInstance, String userLockedResource, String userUsedResource, String resourceType, Long lockedTime, Long usedTime) {
        this.id = id;
        this.user = user;
        this.ticketId = ticketId;
        this.creator = creator;
        this.emApplicationName = emApplicationName;
        this.emInstance = emInstance;
        this.engineApplicationName = engineApplicationName;
        this.engineInstance = engineInstance;
        this.userLockedResource = userLockedResource;
        this.userUsedResource = userUsedResource;
        this.resourceType = resourceType;
        this.lockedTime = lockedTime;
        this.usedTime = usedTime;
    }

    public UserResourceMetaData(String user, String ticketId, String creator, String emApplicationName, String emInstance, String engineApplicationName, String engineInstance, String userLockedResource, String userUsedResource, String resourceType, Long lockedTime, Long usedTime) {
        this.user = user;
        this.ticketId = ticketId;
        this.creator = creator;
        this.emApplicationName = emApplicationName;
        this.emInstance = emInstance;
        this.engineApplicationName = engineApplicationName;
        this.engineInstance = engineInstance;
        this.userLockedResource = userLockedResource;
        this.userUsedResource = userUsedResource;
        this.resourceType = resourceType;
        this.lockedTime = lockedTime;
        this.usedTime = usedTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getEmApplicationName() {
        return emApplicationName;
    }

    public void setEmApplicationName(String emApplicationName) {
        this.emApplicationName = emApplicationName;
    }

    public String getEmInstance() {
        return emInstance;
    }

    public void setEmInstance(String emInstance) {
        this.emInstance = emInstance;
    }

    public String getEngineApplicationName() {
        return engineApplicationName;
    }

    public void setEngineApplicationName(String engineApplicationName) {
        this.engineApplicationName = engineApplicationName;
    }

    public String getEngineInstance() {
        return engineInstance;
    }

    public void setEngineInstance(String engineInstance) {
        this.engineInstance = engineInstance;
    }

    public String getUserLockedResource() {
        return userLockedResource;
    }

    public void setUserLockedResource(String userLockedResource) {
        this.userLockedResource = userLockedResource;
    }

    public String getUserUsedResource() {
        return userUsedResource;
    }

    public void setUserUsedResource(String userUsedResource) {
        this.userUsedResource = userUsedResource;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(Long lockedTime) {
        this.lockedTime = lockedTime;
    }

    public Long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(Long usedTime) {
        this.usedTime = usedTime;
    }
}
