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

/** @TableName linkis_ps_dm_datasource_env */
@TableName(value = "linkis_ps_dm_datasource_env")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceEnvEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** */
  private String envName;

  /** */
  private String envDesc;

  /** */
  private Integer datasourceTypeId;

  /** */
  private String parameter;

  /** */
  private Date createTime;

  /** */
  private String createUser;

  /** */
  private Date modifyTime;

  /** */
  private String modifyUser;

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
  public String getEnvName() {
    return envName;
  }

  /** */
  public void setEnvName(String envName) {
    this.envName = envName;
  }

  /** */
  public String getEnvDesc() {
    return envDesc;
  }

  /** */
  public void setEnvDesc(String envDesc) {
    this.envDesc = envDesc;
  }

  /** */
  public Integer getDatasourceTypeId() {
    return datasourceTypeId;
  }

  /** */
  public void setDatasourceTypeId(Integer datasourceTypeId) {
    this.datasourceTypeId = datasourceTypeId;
  }

  /** */
  public String getParameter() {
    return parameter;
  }

  /** */
  public void setParameter(String parameter) {
    this.parameter = parameter;
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
  public String getCreateUser() {
    return createUser;
  }

  /** */
  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  /** */
  public Date getModifyTime() {
    return modifyTime;
  }

  /** */
  public void setModifyTime(Date modifyTime) {
    this.modifyTime = modifyTime;
  }

  /** */
  public String getModifyUser() {
    return modifyUser;
  }

  /** */
  public void setModifyUser(String modifyUser) {
    this.modifyUser = modifyUser;
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
    DatasourceEnvEntity other = (DatasourceEnvEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getEnvName() == null
            ? other.getEnvName() == null
            : this.getEnvName().equals(other.getEnvName()))
        && (this.getEnvDesc() == null
            ? other.getEnvDesc() == null
            : this.getEnvDesc().equals(other.getEnvDesc()))
        && (this.getDatasourceTypeId() == null
            ? other.getDatasourceTypeId() == null
            : this.getDatasourceTypeId().equals(other.getDatasourceTypeId()))
        && (this.getParameter() == null
            ? other.getParameter() == null
            : this.getParameter().equals(other.getParameter()))
        && (this.getCreateTime() == null
            ? other.getCreateTime() == null
            : this.getCreateTime().equals(other.getCreateTime()))
        && (this.getCreateUser() == null
            ? other.getCreateUser() == null
            : this.getCreateUser().equals(other.getCreateUser()))
        && (this.getModifyTime() == null
            ? other.getModifyTime() == null
            : this.getModifyTime().equals(other.getModifyTime()))
        && (this.getModifyUser() == null
            ? other.getModifyUser() == null
            : this.getModifyUser().equals(other.getModifyUser()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getEnvName() == null) ? 0 : getEnvName().hashCode());
    result = prime * result + ((getEnvDesc() == null) ? 0 : getEnvDesc().hashCode());
    result =
        prime * result + ((getDatasourceTypeId() == null) ? 0 : getDatasourceTypeId().hashCode());
    result = prime * result + ((getParameter() == null) ? 0 : getParameter().hashCode());
    result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
    result = prime * result + ((getCreateUser() == null) ? 0 : getCreateUser().hashCode());
    result = prime * result + ((getModifyTime() == null) ? 0 : getModifyTime().hashCode());
    result = prime * result + ((getModifyUser() == null) ? 0 : getModifyUser().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", envName=").append(envName);
    sb.append(", envDesc=").append(envDesc);
    sb.append(", datasourceTypeId=").append(datasourceTypeId);
    sb.append(", parameter=").append(parameter);
    sb.append(", createTime=").append(createTime);
    sb.append(", createUser=").append(createUser);
    sb.append(", modifyTime=").append(modifyTime);
    sb.append(", modifyUser=").append(modifyUser);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
