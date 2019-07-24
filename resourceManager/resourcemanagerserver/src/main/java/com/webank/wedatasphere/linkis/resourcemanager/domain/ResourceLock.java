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
public class ResourceLock {

    private Integer id;
    private String user;
    private String emApplicationName;
    private String emInstance;

    public ResourceLock(Integer id, String user, String emApplicationName, String emInstance) {
        this.id = id;
        this.user = user;
        this.emApplicationName = emApplicationName;
        this.emInstance = emInstance;
    }

    public ResourceLock(String user, String emApplicationName, String emInstance) {
        this.user = user;
        this.emApplicationName = emApplicationName;
        this.emInstance = emInstance;
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
}
