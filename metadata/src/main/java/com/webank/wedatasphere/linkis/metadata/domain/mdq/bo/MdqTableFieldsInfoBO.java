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
package com.webank.wedatasphere.linkis.metadata.domain.mdq.bo;


public class MdqTableFieldsInfoBO {
    private String name;
    private String type;
    private Integer length;
    private String alias;
    private String express;
    private Boolean isPrimary;
    private Boolean isPartitionField;
    private String rule;
    private String comment;

    private String partitionsValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public Boolean getPartitionField() {
        return isPartitionField;
    }

    public void setPartitionField(Boolean partitionField) {
        isPartitionField = partitionField;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPartitionsValue() {
        return partitionsValue;
    }

    public void setPartitionsValue(String partitionsValue) {
        this.partitionsValue = partitionsValue;
    }
}
