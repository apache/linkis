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

package org.apache.linkis.gateway.authentication.entity;

import java.util.Date;

public class TokenEntity {
  private String id;
  private String tokenName;
  private String tokenSign;
  private String legalUsersStr;
  private String legalHostsStr;
  private String businessOwner;
  private Date createTime;
  private Date updateTime;
  private Long elapseDay;
  private String updateBy;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTokenName() {
    return tokenName;
  }

  public void setTokenName(String tokenName) {
    this.tokenName = tokenName;
  }

  public String getLegalUsersStr() {
    return legalUsersStr;
  }

  public void setLegalUsersStr(String legalUsersStr) {
    this.legalUsersStr = legalUsersStr;
  }

  public String getLegalHostsStr() {
    return legalHostsStr;
  }

  public void setLegalHostsStr(String legalHostsStr) {
    this.legalHostsStr = legalHostsStr;
  }

  public String getBusinessOwner() {
    return businessOwner;
  }

  public void setBusinessOwner(String businessOwner) {
    this.businessOwner = businessOwner;
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

  public Long getElapseDay() {
    return elapseDay;
  }

  public void setElapseDay(Long elapseDay) {
    this.elapseDay = elapseDay;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public String getTokenSign() {
    return tokenSign;
  }

  public void setTokenSign(String tokenSign) {
    this.tokenSign = tokenSign;
  }
}
