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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** @TableName linkis_ps_dm_datasource_type */
@TableName(value = "linkis_ps_dm_datasource_type")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasourceTypeEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** */
  private String name;

  /** */
  private String description;

  @TableField(value = "`option`")
  private String option;

  /** */
  private String classifier;

  /** */
  private String icon;

  /** */
  private Integer layers;

  @TableField(exist = false)
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
  public String getName() {
    return name;
  }

  /** */
  public void setName(String name) {
    this.name = name;
  }

  /** */
  public String getDescription() {
    return description;
  }

  /** */
  public void setDescription(String description) {
    this.description = description;
  }

  /** */
  public String getOption() {
    return option;
  }

  /** */
  public void setOption(String option) {
    this.option = option;
  }

  /** */
  public String getClassifier() {
    return classifier;
  }

  /** */
  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  /** */
  public String getIcon() {
    return icon;
  }

  /** */
  public void setIcon(String icon) {
    this.icon = icon;
  }

  /** */
  public Integer getLayers() {
    return layers;
  }

  /** */
  public void setLayers(Integer layers) {
    this.layers = layers;
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
    DatasourceTypeEntity other = (DatasourceTypeEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getName() == null
            ? other.getName() == null
            : this.getName().equals(other.getName()))
        && (this.getDescription() == null
            ? other.getDescription() == null
            : this.getDescription().equals(other.getDescription()))
        && (this.getOption() == null
            ? other.getOption() == null
            : this.getOption().equals(other.getOption()))
        && (this.getClassifier() == null
            ? other.getClassifier() == null
            : this.getClassifier().equals(other.getClassifier()))
        && (this.getIcon() == null
            ? other.getIcon() == null
            : this.getIcon().equals(other.getIcon()))
        && (this.getLayers() == null
            ? other.getLayers() == null
            : this.getLayers().equals(other.getLayers()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
    result = prime * result + ((getOption() == null) ? 0 : getOption().hashCode());
    result = prime * result + ((getClassifier() == null) ? 0 : getClassifier().hashCode());
    result = prime * result + ((getIcon() == null) ? 0 : getIcon().hashCode());
    result = prime * result + ((getLayers() == null) ? 0 : getLayers().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", name=").append(name);
    sb.append(", description=").append(description);
    sb.append(", option=").append(option);
    sb.append(", classifier=").append(classifier);
    sb.append(", icon=").append(icon);
    sb.append(", layers=").append(layers);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
