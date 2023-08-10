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

public class ConfigUserValue {

  private String key;

  private String name;
  //  linkis_ps_configuration_config_key  id
  private Integer configKeyId;

  private String description;

  private String defaultValue;

  private String engineType;
  //  linkis_ps_configuration_config_value id
  private Integer configValueId;

  private String configValue;
  //  linkis_cg_manager_label id
  private Integer configLabelId;

  private String labelValue;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Integer getConfigKeyId() {
    return configKeyId;
  }

  public void setConfigKeyId(Integer configKeyId) {
    this.configKeyId = configKeyId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public Integer getConfigValueId() {
    return configValueId;
  }

  public void setConfigValueId(Integer configValueId) {
    this.configValueId = configValueId;
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

  public String getLabelValue() {
    return labelValue;
  }

  public void setLabelValue(String labelValue) {
    this.labelValue = labelValue;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ConfigUserValue{"
        + "key='"
        + key
        + '\''
        + ", name='"
        + name
        + '\''
        + ", configKeyId="
        + configKeyId
        + ", description='"
        + description
        + '\''
        + ", defaultValue='"
        + defaultValue
        + '\''
        + ", engineType='"
        + engineType
        + '\''
        + ", configValueId="
        + configValueId
        + ", configValue='"
        + configValue
        + '\''
        + ", configLabelId="
        + configLabelId
        + ", labelValue='"
        + labelValue
        + '\''
        + '}';
  }
}
