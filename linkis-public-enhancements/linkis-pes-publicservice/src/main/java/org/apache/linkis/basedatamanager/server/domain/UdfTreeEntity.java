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

package org.apache.linkis.basedatamanager.server.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** @TableName linkis_ps_udf_tree */
@TableName(value = "linkis_ps_udf_tree")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UdfTreeEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** */
  private Long parent;

  /** Category name of the function. It would be displayed in the front-end */
  private String name;

  /** */
  private String userName;

  /** */
  private String description;

  /** */
  private Date createTime;

  /** */
  private Date updateTime;

  /** Used to distinguish between udf and function */
  private String category;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;

  /** */
  public Long getId() {
    return id;
  }

  /** */
  public void setId(Long id) {
    this.id = id;
  }

  /** */
  public Long getParent() {
    return parent;
  }

  /** */
  public void setParent(Long parent) {
    this.parent = parent;
  }

  /** Category name of the function. It would be displayed in the front-end */
  public String getName() {
    return name;
  }

  /** Category name of the function. It would be displayed in the front-end */
  public void setName(String name) {
    this.name = name;
  }

  /** */
  public String getUserName() {
    return userName;
  }

  /** */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /** */
  public String getDescription() {
    return description;
  }

  /** */
  public void setDescription(String description) {
    this.description = description;
  }

  /** */
  public Date getCreateTime() {
    return createTime;
  }

  /** */
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  /** */
  public Date getUpdateTime() {
    return updateTime;
  }

  /** */
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  /** Used to distinguish between udf and function */
  public String getCategory() {
    return category;
  }

  /** Used to distinguish between udf and function */
  public void setCategory(String category) {
    this.category = category;
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (that == null) {
      return false;
    }
    if (getClass() != that.getClass()) {
      return false;
    }
    UdfTreeEntity other = (UdfTreeEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getParent() == null
            ? other.getParent() == null
            : this.getParent().equals(other.getParent()))
        && (this.getName() == null
            ? other.getName() == null
            : this.getName().equals(other.getName()))
        && (this.getUserName() == null
            ? other.getUserName() == null
            : this.getUserName().equals(other.getUserName()))
        && (this.getDescription() == null
            ? other.getDescription() == null
            : this.getDescription().equals(other.getDescription()))
        && (this.getCreateTime() == null
            ? other.getCreateTime() == null
            : this.getCreateTime().equals(other.getCreateTime()))
        && (this.getUpdateTime() == null
            ? other.getUpdateTime() == null
            : this.getUpdateTime().equals(other.getUpdateTime()))
        && (this.getCategory() == null
            ? other.getCategory() == null
            : this.getCategory().equals(other.getCategory()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getParent() == null) ? 0 : getParent().hashCode());
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
    result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
    result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
    result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
    result = prime * result + ((getCategory() == null) ? 0 : getCategory().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", parent=").append(parent);
    sb.append(", name=").append(name);
    sb.append(", userName=").append(userName);
    sb.append(", description=").append(description);
    sb.append(", createTime=").append(createTime);
    sb.append(", updateTime=").append(updateTime);
    sb.append(", category=").append(category);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
