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

package org.apache.linkis.udf.entity;

import org.apache.linkis.udf.vo.UDFInfoVo;

import java.util.Date;
import java.util.List;

public class UDFTree {

  private Long id;
  private Long parent;
  private String name;
  private String userName;
  private String description;

  private Date createTime;
  private Date updateTime;

  private String category;

  private List<UDFInfoVo> udfInfos;
  private List<UDFTree> childrens;

  public UDFTree() {};

  public UDFTree(
      Long id,
      Long parent,
      String name,
      String userName,
      String description,
      Date createTime,
      Date updateTime,
      String category) {
    this.id = id;
    this.parent = parent;
    this.name = name;
    this.userName = userName;
    this.description = description;
    this.createTime = createTime;
    this.updateTime = updateTime;
    this.category = category;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getParent() {
    return parent;
  }

  public void setParent(Long parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public List<UDFInfoVo> getUdfInfos() {
    return udfInfos;
  }

  public void setUdfInfos(List<UDFInfoVo> udfInfos) {
    this.udfInfos = udfInfos;
  }

  public List<UDFTree> getChildrens() {
    return childrens;
  }

  public void setChildrens(List<UDFTree> childrens) {
    this.childrens = childrens;
  }
}
