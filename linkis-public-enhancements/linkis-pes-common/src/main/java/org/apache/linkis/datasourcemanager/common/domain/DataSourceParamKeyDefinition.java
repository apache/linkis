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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/** Parameter key definition for data source type */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@JsonIgnoreProperties(
    value = {"hibernateLazyInitializer", "handler"},
    ignoreUnknown = true)
public class DataSourceParamKeyDefinition {
  /** Key-value type */
  public enum ValueType {
    /** Email format */
    EMAIL(String.class),
    /** String */
    TEXT(String.class),
    /** String */
    TEXTAREA(String.class),
    /** Long */
    NUMBER(Long.class),
    /** SELECT */
    SELECT(String.class),
    /** List */
    LIST(List.class),
    /** Map */
    MAP(Map.class),
    /** RADIO */
    RADIO(String.class),
    /** Password */
    PASSWORD(String.class),
    /** DateTime */
    DATE(Date.class),
    /** File */
    FILE(InputStream.class);
    Class<?> type;

    ValueType(Class<?> type) {
      this.type = type;
    }

    public Class<?> getJavaType() {
      return this.type;
    }
  }

  public enum Scope {
    /** Env SCOPE */
    ENV,
  }
  /** Definition id */
  private Long id;

  /** Key name */
  private String key;

  /** Definition description */
  private String description;

  /** Option name */
  private String name;

  /** Default value */
  private String defaultValue;

  /** Value type */
  private ValueType valueType;
  /** Scope */
  private Scope scope;
  /** If the definition is required */
  private boolean require;
  /** Value regex */
  private String valueRegex;

  /** Reference id */
  private Long refId;

  /** Reference value */
  private String refValue;

  /** Form fill content */
  private String dataSource;

  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public ValueType getValueType() {
    return valueType;
  }

  public void setValueType(ValueType valueType) {
    this.valueType = valueType;
  }

  public String getValueRegex() {
    return valueRegex;
  }

  public void setValueRegex(String valueRegex) {
    this.valueRegex = valueRegex;
  }

  public boolean isRequire() {
    return require;
  }

  public void setRequire(boolean require) {
    this.require = require;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public Long getRefId() {
    return refId;
  }

  public void setRefId(Long refId) {
    this.refId = refId;
  }

  public String getRefValue() {
    return refValue;
  }

  public void setRefValue(String refValue) {
    this.refValue = refValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataSourceParamKeyDefinition that = (DataSourceParamKeyDefinition) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
