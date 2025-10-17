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

import java.sql.Timestamp;

/** PythonModuleInfo实体类，用于表示Python模块包信息。 这个类包含了模块的详细信息，如名称、描述、路径、引擎类型、加载状态、过期状态等。 */
public class PythonModuleInfoVO {
  // 自增id，用于唯一标识每一个模块
  private Long id;

  // Python模块名称
  private String name;

  // Python模块描述
  private String description;

  // HDFS路径，存储模块的物理位置
  private String path;

  // 引擎类型，例如：python, spark 或 all
  private String engineType;

  // 创建用户，记录创建模块的用户信息
  private String createUser;

  // 修改用户，记录最后修改模块的用户信息
  private String updateUser;

  // 是否加载，0-未加载，1-已加载
  private boolean isLoad;

  // 是否过期，0-未过期，1-已过期
  private Boolean isExpire;

  // 创建时间，记录模块创建的时间
  private Timestamp createTime;

  // 修改时间，记录模块最后修改的时间
  private Timestamp updateTime;
  private String pythonModule;

  // 默认构造函数
  public PythonModuleInfoVO() {}

  // 具有所有参数的构造函数
  public PythonModuleInfoVO(
      Long id,
      String name,
      String description,
      String path,
      String engineType,
      String createUser,
      String updateUser,
      boolean isLoad,
      Boolean isExpire,
      Timestamp createTime,
      Timestamp updateTime) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.path = path;
    this.engineType = engineType;
    this.createUser = createUser;
    this.updateUser = updateUser;
    this.isLoad = isLoad;
    this.isExpire = isExpire;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }

  // Getter和Setter方法
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

  public boolean isLoad() {
    return isLoad;
  }

  public void setLoad(boolean isLoad) {
    this.isLoad = isLoad;
  }

  public Boolean isExpire() {
    return isExpire;
  }

  public void setExpire(Boolean isExpire) {
    this.isExpire = isExpire;
  }

  public Timestamp getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Timestamp createTime) {
    this.createTime = createTime;
  }

  public Timestamp getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Timestamp updateTime) {
    this.updateTime = updateTime;
  }

  public String getPythonModule() {
    return pythonModule;
  }

  public void setPythonModule(String pythonModule) {
    this.pythonModule = pythonModule;
  }

  // 重写toString方法，用于调试和日志记录
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
        + ", pythonModule="
        + pythonModule
        + '}';
  }
}
