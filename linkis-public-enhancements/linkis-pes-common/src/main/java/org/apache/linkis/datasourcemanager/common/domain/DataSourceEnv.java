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

package org.apache.linkis.datasourcemanager.common.domain;

import org.apache.linkis.datasourcemanager.common.exception.JsonErrorException;
import org.apache.linkis.datasourcemanager.common.util.json.Json;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/** Store the data source environment information */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonIgnoreProperties(
    value = {"hibernateLazyInitializer", "handler"},
    ignoreUnknown = true)
public class DataSourceEnv {

  private Long id;
  /** Environment name */
  @NotNull private String envName;

  /** Environment description */
  @Size(min = 0, max = 200)
  private String envDesc;

  /** ID of data source type */
  @NotNull private Long dataSourceTypeId;

  private DataSourceType dataSourceType;
  /** Connection parameters for environment */
  private Map<String, Object> connectParams = new HashMap<>();

  /** Parameter JSON string */
  @JsonIgnore private String parameter;

  /** Create time */
  private Date createTime;

  /** Creator */
  private String createUser;

  /** Modify time */
  private Date modifyTime;

  /** Modify user */
  private String modifyUser;

  @JsonIgnore private List<DataSourceParamKeyDefinition> keyDefinitions = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEnvName() {
    return envName;
  }

  public void setEnvName(String envName) {
    this.envName = envName;
  }

  public String getEnvDesc() {
    return envDesc;
  }

  public void setEnvDesc(String envDesc) {
    this.envDesc = envDesc;
  }

  public Map<String, Object> getConnectParams() {
    if (connectParams.isEmpty() && StringUtils.isNotBlank(parameter)) {
      try {
        connectParams.putAll(Objects.requireNonNull(Json.fromJson(parameter, Map.class)));
      } catch (JsonErrorException e) {
        // Ignore
      }
    }
    return connectParams;
  }

  public void setConnectParams(Map<String, Object> connectParams) {
    this.connectParams = connectParams;
  }

  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public Date getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(Date modifyTime) {
    this.modifyTime = modifyTime;
  }

  public String getModifyUser() {
    return modifyUser;
  }

  public void setModifyUser(String modifyUser) {
    this.modifyUser = modifyUser;
  }

  public Long getDataSourceTypeId() {
    return dataSourceTypeId;
  }

  public void setDataSourceTypeId(Long dataSourceTypeId) {
    this.dataSourceTypeId = dataSourceTypeId;
  }

  public List<DataSourceParamKeyDefinition> getKeyDefinitions() {
    return keyDefinitions;
  }

  public void setKeyDefinitions(List<DataSourceParamKeyDefinition> keyDefinitions) {
    this.keyDefinitions = keyDefinitions;
  }

  public DataSourceType getDataSourceType() {
    return dataSourceType;
  }

  public void setDataSourceType(DataSourceType dataSourceType) {
    this.dataSourceType = dataSourceType;
  }
}
