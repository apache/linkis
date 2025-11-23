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

/** @TableName linkis_ps_datasource_access */
@TableName(value = "linkis_ps_datasource_access")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceAccessEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** */
  private Long tableId;

  /** */
  private String visitor;

  /** */
  private String fields;

  /** */
  private Integer applicationId;

  /** */
  private Date accessTime;

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
  public Long getTableId() {
    return tableId;
  }

  /** */
  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  /** */
  public String getVisitor() {
    return visitor;
  }

  /** */
  public void setVisitor(String visitor) {
    this.visitor = visitor;
  }

  /** */
  public String getFields() {
    return fields;
  }

  /** */
  public void setFields(String fields) {
    this.fields = fields;
  }

  /** */
  public Integer getApplicationId() {
    return applicationId;
  }

  /** */
  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
  }

  /** */
  public Date getAccessTime() {
    return accessTime;
  }

  /** */
  public void setAccessTime(Date accessTime) {
    this.accessTime = accessTime;
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
    DatasourceAccessEntity other = (DatasourceAccessEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getTableId() == null
            ? other.getTableId() == null
            : this.getTableId().equals(other.getTableId()))
        && (this.getVisitor() == null
            ? other.getVisitor() == null
            : this.getVisitor().equals(other.getVisitor()))
        && (this.getFields() == null
            ? other.getFields() == null
            : this.getFields().equals(other.getFields()))
        && (this.getApplicationId() == null
            ? other.getApplicationId() == null
            : this.getApplicationId().equals(other.getApplicationId()))
        && (this.getAccessTime() == null
            ? other.getAccessTime() == null
            : this.getAccessTime().equals(other.getAccessTime()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getTableId() == null) ? 0 : getTableId().hashCode());
    result = prime * result + ((getVisitor() == null) ? 0 : getVisitor().hashCode());
    result = prime * result + ((getFields() == null) ? 0 : getFields().hashCode());
    result = prime * result + ((getApplicationId() == null) ? 0 : getApplicationId().hashCode());
    result = prime * result + ((getAccessTime() == null) ? 0 : getAccessTime().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", tableId=").append(tableId);
    sb.append(", visitor=").append(visitor);
    sb.append(", fields=").append(fields);
    sb.append(", applicationId=").append(applicationId);
    sb.append(", accessTime=").append(accessTime);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
