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

import java.util.Map;

public class ConfigKeyValue {

  private Long id;

  private String key;

  private String description;

  private String name;

  private String defaultValue;

  private String validateType;

  private String validateRange;

  private Boolean isAdvanced;

  private Boolean isHidden;

  private Integer level;

  private String engineType;

  private String treeName;

  private Long valueId;

  private String configValue;

  private Integer configLabelId;

  private String unit;

  private Boolean isUserDefined;

  private Map specialLimit;

  public Map getSpecialLimit() {
    return specialLimit;
  }

  public void setSpecialLimit(Map specialLimit) {
    this.specialLimit = specialLimit;
  }

  public Boolean getIsUserDefined() {
    return isUserDefined;
  }

  public void setIsUserDefined(Boolean isUserDefined) {
    this.isUserDefined = isUserDefined;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String configValue) {
    this.configValue = configValue;
  }

  public Integer getConfigLabelId() {
    return configLabelId;
  }

  public void setConfigLabelId(Integer configLabelId) {
    this.configLabelId = configLabelId;
  }

  public String getTreeName() {
    return treeName;
  }

  public void setTreeName(String treeName) {
    this.treeName = treeName;
  }

  public Long getValueId() {
    return valueId;
  }

  public void setValueId(Long valueId) {
    this.valueId = valueId;
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

  public String getValidateType() {
    return validateType;
  }

  public void setValidateType(String validateType) {
    this.validateType = validateType;
  }

  public String getValidateRange() {
    return validateRange;
  }

  public void setValidateRange(String validateRange) {
    this.validateRange = validateRange;
  }

  public Boolean getAdvanced() {
    return isAdvanced;
  }

  public void setAdvanced(Boolean advanced) {
    isAdvanced = advanced;
  }

  public Boolean getHidden() {
    return isHidden;
  }

  public void setHidden(Boolean hidden) {
    isHidden = hidden;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }
}
