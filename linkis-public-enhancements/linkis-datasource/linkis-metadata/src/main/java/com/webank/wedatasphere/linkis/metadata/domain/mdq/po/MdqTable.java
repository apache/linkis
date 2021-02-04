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
package com.webank.wedatasphere.linkis.metadata.domain.mdq.po;

import java.util.Date;


public class MdqTable {
    private Long id;
    private String database;
    private String name;
    private String alias;
    private String creator;
    private String comment;
    private Date createTime;
    private String productName;
    private String projectName;
    private String usage;
    private Integer lifecycle;
    private Integer useWay;
    private Boolean isImport;
    private Integer modelLevel;
    private Boolean isExternalUse;
    private Boolean isPartitionTable;
    private Boolean isAvailable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public Integer getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Integer lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Integer getUseWay() {
        return useWay;
    }

    public void setUseWay(Integer useWay) {
        this.useWay = useWay;
    }

    public Boolean getImport() {
        return isImport;
    }

    public void setImport(Boolean anImport) {
        isImport = anImport;
    }

    public Integer getModelLevel() {
        return modelLevel;
    }

    public void setModelLevel(Integer modelLevel) {
        this.modelLevel = modelLevel;
    }

    public Boolean getExternalUse() {
        return isExternalUse;
    }

    public void setExternalUse(Boolean externalUse) {
        isExternalUse = externalUse;
    }

    public Boolean getPartitionTable() {
        return isPartitionTable;
    }

    public void setPartitionTable(Boolean partitionTable) {
        isPartitionTable = partitionTable;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "MdqTable{" +
                "id=" + id +
                ", database='" + database + '\'' +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", creator='" + creator + '\'' +
                ", comment='" + comment + '\'' +
                ", createTime=" + createTime +
                ", productName='" + productName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", usage='" + usage + '\'' +
                ", lifecycle=" + lifecycle +
                ", useWay=" + useWay +
                ", isImport=" + isImport +
                ", modelLevel=" + modelLevel +
                ", isExternalUse=" + isExternalUse +
                ", isPartitionTable=" + isPartitionTable +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
