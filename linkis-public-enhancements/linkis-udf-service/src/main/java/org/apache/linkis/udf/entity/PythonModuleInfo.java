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

import java.util.Date;

public class PythonModuleInfo {
  private Long id;
  private String name;
  private String description;
  private String path;
  private String engineType;
  private String createUser;
  private String updateUser;
  private Integer isLoad;
  private Integer isExpire;
  private Date createTime;
  private Date updateTime;
  private String pythonModule;

  public PythonModuleInfo() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public String getUpdateUser() {
    return updateUser;
  }

  public void setUpdateUser(String updateUser) {
    this.updateUser = updateUser;
  }

  public Integer getIsLoad() {
    return isLoad;
  }

  public void setIsLoad(Integer isLoad) {
    this.isLoad = isLoad;
  }

  public Integer getIsExpire() {
    return isExpire;
  }

  public void setIsExpire(Integer isExpire) {
    this.isExpire = isExpire;
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

  public String getPythonModule() {
    return pythonModule;
  }

  public void setPythonModule(String pythonModule) {
    this.pythonModule = pythonModule;
  }

  @Override
  public String toString() {
    return "PythonModuleInfo{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", path='"
        + path
        + '\''
        + ", engineType='"
        + engineType
        + '\''
        + ", createUser='"
        + createUser
        + '\''
        + ", updateUser='"
        + updateUser
        + '\''
        + ", isLoad="
        + isLoad
        + ", isExpire="
        + isExpire
        + ", createTime="
        + createTime
        + ", updateTime="
        + updateTime
        + ",  pythonModule="
        + pythonModule
        + '}';
  }
}
