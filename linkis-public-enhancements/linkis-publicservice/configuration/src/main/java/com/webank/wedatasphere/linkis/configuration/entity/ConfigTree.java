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

package com.webank.wedatasphere.linkis.configuration.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allenlliu on 2018/10/17.
 */

public class ConfigTree {
    private Long id;
    private Long parentID;
    private String name;
    private String description;
    private Long applicationID;
    private List<ConfigTree> childrens = new ArrayList<>();
    private List<ConfigKeyValueVO> settings = new ArrayList();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentID() {
        return parentID;
    }

    public void setParentID(Long parentID) {
        this.parentID = parentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(Long applicationID) {
        this.applicationID = applicationID;
    }

    public List<ConfigTree> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<ConfigTree> childrens) {
        this.childrens = childrens;
    }

    public List<ConfigKeyValueVO> getSettings() {
        return settings;
    }

    public void setSettings(List<ConfigKeyValueVO> settings) {
        this.settings = settings;
    }
}
