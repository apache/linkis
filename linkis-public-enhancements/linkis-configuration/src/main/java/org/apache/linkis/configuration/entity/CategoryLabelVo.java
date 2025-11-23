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

package org.apache.linkis.configuration.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryLabelVo {

  private Integer categoryId;

  private Integer labelId;

  private String categoryName;

  private List<CategoryLabelVo> childCategory = new ArrayList<>();

  private String description;

  private String tag;

  private Date createTime;

  private Date updateTime;

  private Integer level;

  private String fatherCategoryName;

  public Integer getLabelId() {
    return labelId;
  }

  public void setLabelId(Integer labelId) {
    this.labelId = labelId;
  }

  public String getFatherCategoryName() {
    return fatherCategoryName;
  }

  public void setFatherCategoryName(String fatherCategoryName) {
    this.fatherCategoryName = fatherCategoryName;
  }

  public List<CategoryLabelVo> getChildCategory() {
    return childCategory;
  }

  public void setChildCategory(List<CategoryLabelVo> childCategory) {
    this.childCategory = childCategory;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
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

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public Integer getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Integer categoryId) {
    this.categoryId = categoryId;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }
}
