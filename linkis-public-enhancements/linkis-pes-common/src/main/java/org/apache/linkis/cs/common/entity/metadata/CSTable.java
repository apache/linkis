/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.common.entity.metadata;

import org.apache.linkis.cs.common.annotation.KeywordMethod;

import java.util.Date;
import java.util.List;

public class CSTable implements Table {

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
  private Boolean isView;
  private String location;
  private CSColumn[] columns;
  private List<CSPartition> partitions;
  private CSDB db;

  @Override
  @KeywordMethod
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getAlias() {
    return alias;
  }

  @KeywordMethod
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public void setAlias(String alias) {
    this.alias = alias;
  }

  @Override
  public String getCreator() {
    return creator;
  }

  @Override
  public void setCreator(String creator) {
    this.creator = creator;
  }

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public Date getCreateTime() {
    return createTime;
  }

  @Override
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  @Override
  public String getProductName() {
    return productName;
  }

  @Override
  public void setProductName(String productName) {
    this.productName = productName;
  }

  @Override
  public String getProjectName() {
    return projectName;
  }

  @Override
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  @Override
  public String getUsage() {
    return usage;
  }

  @Override
  public void setUsage(String usage) {
    this.usage = usage;
  }

  @Override
  public Integer getLifecycle() {
    return lifecycle;
  }

  @Override
  public void setLifecycle(Integer lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override
  public Integer getUseWay() {
    return useWay;
  }

  @Override
  public void setUseWay(Integer useWay) {
    this.useWay = useWay;
  }

  @Override
  public Boolean getImport() {
    return isImport;
  }

  @Override
  public void setImport(Boolean anImport) {
    isImport = anImport;
  }

  @Override
  public Integer getModelLevel() {
    return modelLevel;
  }

  @Override
  public void setModelLevel(Integer modelLevel) {
    this.modelLevel = modelLevel;
  }

  @Override
  public Boolean getExternalUse() {
    return isExternalUse;
  }

  @Override
  public void setExternalUse(Boolean externalUse) {
    isExternalUse = externalUse;
  }

  @Override
  public Boolean getPartitionTable() {
    return isPartitionTable;
  }

  @Override
  public void setPartitionTable(Boolean partitionTable) {
    isPartitionTable = partitionTable;
  }

  @Override
  public Boolean getAvailable() {
    return isAvailable;
  }

  @Override
  public void setAvailable(Boolean available) {
    isAvailable = available;
  }

  public Boolean isView() {
    return this.isView;
  }

  @Override
  public Boolean getView() {
    return isView;
  }

  @Override
  public void setView(Boolean view) {
    isView = view;
  }

  @Override
  public CSColumn[] getColumns() {
    return this.columns;
  }

  @Override
  public void setColumns(CSColumn[] columns) {
    this.columns = columns;
  }

  @Override
  public List<CSPartition> getPartitions() {
    return partitions;
  }

  @Override
  public void setPartitions(List<CSPartition> partitions) {
    this.partitions = partitions;
  }

  @Override
  public CSDB getDb() {
    return db;
  }

  @Override
  public void setDb(CSDB db) {
    this.db = db;
  }
}
