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

/** @TableName linkis_ps_dm_datasource_type_key */
@TableName(value = "linkis_ps_dm_datasource_type_key")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceTypeKeyEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** */
  private Integer dataSourceTypeId;

  /** */
  @TableField(value = "`key`")
  private String key;

  /** */
  @TableField(value = "`name`")
  private String name;

  /** */
  private String nameEn;

  /** */
  private String defaultValue;

  /** */
  private String valueType;

  /** */
  @TableField(value = "`scope`")
  private String scope;

  /** */
  @TableField(value = "`require`")
  private Integer require;

  /** */
  private String description;

  /** */
  private String descriptionEn;

  /** */
  private String valueRegex;

  /** */
  private Long refId;

  /** */
  private String refValue;

  /** */
  private String dataSource;

  /** */
  private Date updateTime;

  /** */
  private Date createTime;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;

  /** */
  public Integer getId() {
    return id;
  }

  /** */
  public void setId(Integer id) {
    this.id = id;
  }

  /** */
  public Integer getDataSourceTypeId() {
    return dataSourceTypeId;
  }

  /** */
  public void setDataSourceTypeId(Integer dataSourceTypeId) {
    this.dataSourceTypeId = dataSourceTypeId;
  }

  /** */
  public String getKey() {
    return key;
  }

  /** */
  public void setKey(String key) {
    this.key = key;
  }

  /** */
  public String getName() {
    return name;
  }

  /** */
  public void setName(String name) {
    this.name = name;
  }

  /** */
  public String getNameEn() {
    return nameEn;
  }

  /** */
  public void setNameEn(String nameEn) {
    this.nameEn = nameEn;
  }

  /** */
  public String getDefaultValue() {
    return defaultValue;
  }

  /** */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /** */
  public String getValueType() {
    return valueType;
  }

  /** */
  public void setValueType(String valueType) {
    this.valueType = valueType;
  }

  /** */
  public String getScope() {
    return scope;
  }

  /** */
  public void setScope(String scope) {
    this.scope = scope;
  }

  /** */
  public Integer getRequire() {
    return require;
  }

  /** */
  public void setRequire(Integer require) {
    this.require = require;
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
  public String getDescriptionEn() {
    return descriptionEn;
  }

  /** */
  public void setDescriptionEn(String descriptionEn) {
    this.descriptionEn = descriptionEn;
  }

  /** */
  public String getValueRegex() {
    return valueRegex;
  }

  /** */
  public void setValueRegex(String valueRegex) {
    this.valueRegex = valueRegex;
  }

  /** */
  public Long getRefId() {
    return refId;
  }

  /** */
  public void setRefId(Long refId) {
    this.refId = refId;
  }

  /** */
  public String getRefValue() {
    return refValue;
  }

  /** */
  public void setRefValue(String refValue) {
    this.refValue = refValue;
  }

  /** */
  public String getDataSource() {
    return dataSource;
  }

  /** */
  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  /** */
  public Date getUpdateTime() {
    return updateTime;
  }

  /** */
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  /** */
  public Date getCreateTime() {
    return createTime;
  }

  /** */
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
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
    DatasourceTypeKeyEntity other = (DatasourceTypeKeyEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getDataSourceTypeId() == null
            ? other.getDataSourceTypeId() == null
            : this.getDataSourceTypeId().equals(other.getDataSourceTypeId()))
        && (this.getKey() == null ? other.getKey() == null : this.getKey().equals(other.getKey()))
        && (this.getName() == null
            ? other.getName() == null
            : this.getName().equals(other.getName()))
        && (this.getNameEn() == null
            ? other.getNameEn() == null
            : this.getNameEn().equals(other.getNameEn()))
        && (this.getDefaultValue() == null
            ? other.getDefaultValue() == null
            : this.getDefaultValue().equals(other.getDefaultValue()))
        && (this.getValueType() == null
            ? other.getValueType() == null
            : this.getValueType().equals(other.getValueType()))
        && (this.getScope() == null
            ? other.getScope() == null
            : this.getScope().equals(other.getScope()))
        && (this.getRequire() == null
            ? other.getRequire() == null
            : this.getRequire().equals(other.getRequire()))
        && (this.getDescription() == null
            ? other.getDescription() == null
            : this.getDescription().equals(other.getDescription()))
        && (this.getDescriptionEn() == null
            ? other.getDescriptionEn() == null
            : this.getDescriptionEn().equals(other.getDescriptionEn()))
        && (this.getValueRegex() == null
            ? other.getValueRegex() == null
            : this.getValueRegex().equals(other.getValueRegex()))
        && (this.getRefId() == null
            ? other.getRefId() == null
            : this.getRefId().equals(other.getRefId()))
        && (this.getRefValue() == null
            ? other.getRefValue() == null
            : this.getRefValue().equals(other.getRefValue()))
        && (this.getDataSource() == null
            ? other.getDataSource() == null
            : this.getDataSource().equals(other.getDataSource()))
        && (this.getUpdateTime() == null
            ? other.getUpdateTime() == null
            : this.getUpdateTime().equals(other.getUpdateTime()))
        && (this.getCreateTime() == null
            ? other.getCreateTime() == null
            : this.getCreateTime().equals(other.getCreateTime()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result =
        prime * result + ((getDataSourceTypeId() == null) ? 0 : getDataSourceTypeId().hashCode());
    result = prime * result + ((getKey() == null) ? 0 : getKey().hashCode());
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    result = prime * result + ((getNameEn() == null) ? 0 : getNameEn().hashCode());
    result = prime * result + ((getDefaultValue() == null) ? 0 : getDefaultValue().hashCode());
    result = prime * result + ((getValueType() == null) ? 0 : getValueType().hashCode());
    result = prime * result + ((getScope() == null) ? 0 : getScope().hashCode());
    result = prime * result + ((getRequire() == null) ? 0 : getRequire().hashCode());
    result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
    result = prime * result + ((getDescriptionEn() == null) ? 0 : getDescriptionEn().hashCode());
    result = prime * result + ((getValueRegex() == null) ? 0 : getValueRegex().hashCode());
    result = prime * result + ((getRefId() == null) ? 0 : getRefId().hashCode());
    result = prime * result + ((getRefValue() == null) ? 0 : getRefValue().hashCode());
    result = prime * result + ((getDataSource() == null) ? 0 : getDataSource().hashCode());
    result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
    result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", dataSourceTypeId=").append(dataSourceTypeId);
    sb.append(", key=").append(key);
    sb.append(", name=").append(name);
    sb.append(", nameEn=").append(nameEn);
    sb.append(", defaultValue=").append(defaultValue);
    sb.append(", valueType=").append(valueType);
    sb.append(", scope=").append(scope);
    sb.append(", require=").append(require);
    sb.append(", description=").append(description);
    sb.append(", descriptionEn=").append(descriptionEn);
    sb.append(", valueRegex=").append(valueRegex);
    sb.append(", refId=").append(refId);
    sb.append(", refValue=").append(refValue);
    sb.append(", dataSource=").append(dataSource);
    sb.append(", updateTime=").append(updateTime);
    sb.append(", createTime=").append(createTime);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
