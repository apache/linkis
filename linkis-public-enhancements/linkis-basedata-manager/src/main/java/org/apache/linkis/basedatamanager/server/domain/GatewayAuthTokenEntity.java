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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@TableName("linkis_mg_gateway_auth_token")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayAuthTokenEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** */
  private String tokenName;

  /** */
  private String legalUsers;

  /** */
  private String legalHosts;

  /** */
  private String businessOwner;

  /** */
  private Date createTime;

  /** */
  private Date updateTime;

  /** */
  private Long elapseDay;

  /** */
  private String updateBy;

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
  public String getTokenName() {
    return tokenName;
  }

  /** */
  public void setTokenName(String tokenName) {
    this.tokenName = tokenName;
  }

  /** */
  public String getLegalUsers() {
    return legalUsers;
  }

  /** */
  public void setLegalUsers(String legalUsers) {
    this.legalUsers = legalUsers;
  }

  /** */
  public String getLegalHosts() {
    return legalHosts;
  }

  /** */
  public void setLegalHosts(String legalHosts) {
    this.legalHosts = legalHosts;
  }

  /** */
  public String getBusinessOwner() {
    return businessOwner;
  }

  /** */
  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
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

  /** */
  public Long getElapseDay() {
    return elapseDay;
  }

  /** */
  public void setElapseDay(Long elapseDay) {
    this.elapseDay = elapseDay;
  }

  /** */
  public String getUpdateBy() {
    return updateBy;
  }

  /** */
  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
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
    GatewayAuthTokenEntity other = (GatewayAuthTokenEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getTokenName() == null
            ? other.getTokenName() == null
            : this.getTokenName().equals(other.getTokenName()))
        && (this.getLegalUsers() == null
            ? other.getLegalUsers() == null
            : this.getLegalUsers().equals(other.getLegalUsers()))
        && (this.getLegalHosts() == null
            ? other.getLegalHosts() == null
            : this.getLegalHosts().equals(other.getLegalHosts()))
        && (this.getBusinessOwner() == null
            ? other.getBusinessOwner() == null
            : this.getBusinessOwner().equals(other.getBusinessOwner()))
        && (this.getCreateTime() == null
            ? other.getCreateTime() == null
            : this.getCreateTime().equals(other.getCreateTime()))
        && (this.getUpdateTime() == null
            ? other.getUpdateTime() == null
            : this.getUpdateTime().equals(other.getUpdateTime()))
        && (this.getElapseDay() == null
            ? other.getElapseDay() == null
            : this.getElapseDay().equals(other.getElapseDay()))
        && (this.getUpdateBy() == null
            ? other.getUpdateBy() == null
            : this.getUpdateBy().equals(other.getUpdateBy()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getTokenName() == null) ? 0 : getTokenName().hashCode());
    result = prime * result + ((getLegalUsers() == null) ? 0 : getLegalUsers().hashCode());
    result = prime * result + ((getLegalHosts() == null) ? 0 : getLegalHosts().hashCode());
    result = prime * result + ((getBusinessOwner() == null) ? 0 : getBusinessOwner().hashCode());
    result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
    result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
    result = prime * result + ((getElapseDay() == null) ? 0 : getElapseDay().hashCode());
    result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", tokenName=").append(tokenName);
    sb.append(", legalUsers=").append(legalUsers);
    sb.append(", legalHosts=").append(legalHosts);
    sb.append(", businessOwner=").append(businessOwner);
    sb.append(", createTime=").append(createTime);
    sb.append(", updateTime=").append(updateTime);
    sb.append(", elapseDay=").append(elapseDay);
    sb.append(", updateBy=").append(updateBy);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
