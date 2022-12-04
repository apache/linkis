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

package org.apache.linkis.basedatamanager.server.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/** A query request for the save a configuration template */
@ApiModel(
    value = "configuration template save request",
    description = "configuration template save request")
public class ConfigurationTemplateSaveRequest implements Serializable {
  private static final long serialVersionUID = -5910138059732157333L;

  @ApiModelProperty(value = "key id.")
  private Long id;

  @ApiModelProperty(value = "engineLabelId.")
  private String engineLabelId;

  @ApiModelProperty(value = "key. eg: linkis.openlookeng.url")
  private String key;

  @ApiModelProperty(value = "description. eg: 例如:http://127.0.0.1:8080")
  private String description;

  @ApiModelProperty(value = "name. eg: 连接地址")
  private String name;

  @ApiModelProperty(value = "default value. eg: http://127.0.0.1:8080")
  private String defaultValue;

  @ApiModelProperty(value = "validate type. eg: Regex")
  private String validateType;

  @ApiModelProperty(
      value = "validate range. eg: ^\\\\s*http://([^:]+)(:\\\\d+)(/[^\\\\?]+)?(\\\\?\\\\S*)?$")
  private String validateRange;

  @ApiModelProperty(value = "engine conn type. eg: openlookeng")
  private String engineConnType;

  @ApiModelProperty(value = "is hidden. eg: 0-否，1-是")
  private Integer hidden;

  @ApiModelProperty(value = "is advanced. eg: 0-否，1-是")
  private Integer advanced;

  @ApiModelProperty(value = "level. eg: 1-一级，2-二级")
  private Integer level;

  @ApiModelProperty(value = "tree name. eg: 数据源配置")
  private String treeName;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEngineLabelId() {
    return engineLabelId;
  }

  public void setEngineLabelId(String engineLabelId) {
    this.engineLabelId = engineLabelId;
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

  public String getEngineConnType() {
    return engineConnType;
  }

  public void setEngineConnType(String engineConnType) {
    this.engineConnType = engineConnType;
  }

  public Integer getHidden() {
    return hidden;
  }

  public void setHidden(Integer hidden) {
    this.hidden = hidden;
  }

  public Integer getAdvanced() {
    return advanced;
  }

  public void setAdvanced(Integer advanced) {
    this.advanced = advanced;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public String getTreeName() {
    return treeName;
  }

  public void setTreeName(String treeName) {
    this.treeName = treeName;
  }
}
