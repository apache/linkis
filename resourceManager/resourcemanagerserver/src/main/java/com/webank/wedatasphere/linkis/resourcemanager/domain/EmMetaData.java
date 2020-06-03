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
public class EmMetaData {
    private Integer id;
    private String emName;
    private String resourceRequestPolicy;

    public EmMetaData(Integer id, String emName, String resourceRequestPolicy) {
        this.id = id;
        this.emName = emName;
        this.resourceRequestPolicy = resourceRequestPolicy;
    }

    public EmMetaData(String emName, String resourceRequestPolicy) {
        this.emName = emName;
        this.resourceRequestPolicy = resourceRequestPolicy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmName() {
        return emName;
    }

    public void setEmName(String emName) {
        this.emName = emName;
    }

    public String getResourceRequestPolicy() {
        return resourceRequestPolicy;
    }

    public void setResourceRequestPolicy(String resourceRequestPolicy) {
        this.resourceRequestPolicy = resourceRequestPolicy;
    }
}
