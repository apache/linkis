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

/**
 * Created by shanhuang on 9/11/18.
 */
public class EmResourceMetaData {
    private Integer id;
    private String emApplicationName;
    private String emInstance;
    private String totalResource;
    private String protectedResource;
    private String resourcePolicy;
    private String usedResource;
    private String leftResource;
    private String lockedResource;
    private Long registerTime;

    public EmResourceMetaData(Integer id, String emApplicationName, String emInstance, String totalResource, String protectedResource, String resourcePolicy, String usedResource, String leftResource, String lockedResource, Long registerTime) {
        this.id = id;
        this.emApplicationName = emApplicationName;
        this.emInstance = emInstance;
        this.totalResource = totalResource;
        this.protectedResource = protectedResource;
        this.resourcePolicy = resourcePolicy;
        this.usedResource = usedResource;
        this.leftResource = leftResource;
        this.lockedResource = lockedResource;
        this.registerTime = registerTime;
    }

    public EmResourceMetaData(String emApplicationName, String emInstance, String totalResource, String protectedResource, String resourcePolicy, String usedResource, String leftResource, String lockedResource, Long registerTime) {
        this.emApplicationName = emApplicationName;
        this.emInstance = emInstance;
        this.totalResource = totalResource;
        this.protectedResource = protectedResource;
        this.resourcePolicy = resourcePolicy;
        this.usedResource = usedResource;
        this.leftResource = leftResource;
        this.lockedResource = lockedResource;
        this.registerTime = registerTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getTotalResource() {
        return totalResource;
    }

    public void setTotalResource(String totalResource) {
        this.totalResource = totalResource;
    }

    public String getProtectedResource() {
        return protectedResource;
    }

    public void setProtectedResource(String protectedResource) {
        this.protectedResource = protectedResource;
    }

    public String getResourcePolicy() {
        return resourcePolicy;
    }

    public void setResourcePolicy(String resourcePolicy) {
        this.resourcePolicy = resourcePolicy;
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

    public String getLockedResource() {
        return lockedResource;
    }

    public void setLockedResource(String lockedResource) {
        this.lockedResource = lockedResource;
    }

    public Long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Long registerTime) {
        this.registerTime = registerTime;
    }
}
