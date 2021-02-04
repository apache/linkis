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
package com.webank.wedatasphere.linkis.bml.common;

/**
 * @author cooperyang
 * @date 2019-9-17
 */
public enum OperationEnum {
    /**
     * 任务操作类型
     */
    UPLOAD("upload", 0),
    UPDATE("update", 1),
    DOWNLOAD("download", 2),
    DELETE_VERSION("deleteVersion", 3),
    DELETE_RESOURCE("deleteResource", 4),
    DELETE_RESOURCES("deleteResources", 5);
    private String value;
    private int id;
    private OperationEnum(String value, int id){
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
