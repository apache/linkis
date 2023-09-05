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

import java.util.Date;

public class AcrossClusterRule {

  private Long id;
  private String clusterName;
  private String creator;
  private String username;
  private Date createTime;
  private String createBy;
  private Date updateTime;
  private String updateBy;
  private String rules;
  private String isValid;

  public AcrossClusterRule() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getUpdateBy() {
    return updateBy;
  }

  public void setUpdateBy(String updateBy) {
    this.updateBy = updateBy;
  }

  public String getRules() {
    return rules;
  }

  public void setRules(String rules) {
    this.rules = rules;
  }

  public String getIsValid() {
    return isValid;
  }

  public void setIsValid(String isValid) {
    this.isValid = isValid;
  }

  @Override
  public String toString() {
    return "AcrossClusterRule{"
        + "id="
        + id
        + ", clusterName='"
        + clusterName
        + '\''
        + ", creator='"
        + creator
        + '\''
        + ", username='"
        + username
        + '\''
        + ", createTime="
        + createTime
        + ", createBy='"
        + createBy
        + '\''
        + ", updateTime="
        + updateTime
        + ", updateBy='"
        + updateBy
        + '\''
        + ", rules='"
        + rules
        + '\''
        + ", isValid='"
        + isValid
        + '\''
        + '}';
  }
}
