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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** @TableName linkis_mg_gateway_auth_token */
@TableName("linkis_cg_rm_external_resource_provider")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RmExternalResourceProviderEntity implements Serializable {
  /** */
  @TableId(type = IdType.AUTO)
  private Integer id;

  /** */
  private String resourceType;

  /** */
  private String name;

  /** */
  private String labels;

  /** */
  private String config;

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
  public String getResourceType() {
    return resourceType;
  }

  /** */
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
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
  public String getLabels() {
    return labels;
  }

  /** */
  public void setLabels(String labels) {
    this.labels = labels;
  }

  /** */
  public String getConfig() {
    return config;
  }

  /** */
  public void setConfig(String config) {
    this.config = config;
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
    RmExternalResourceProviderEntity other = (RmExternalResourceProviderEntity) that;
    return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
        && (this.getResourceType() == null
            ? other.getResourceType() == null
            : this.getResourceType().equals(other.getResourceType()))
        && (this.getName() == null
            ? other.getName() == null
            : this.getName().equals(other.getName()))
        && (this.getLabels() == null
            ? other.getLabels() == null
            : this.getLabels().equals(other.getLabels()))
        && (this.getConfig() == null
            ? other.getConfig() == null
            : this.getConfig().equals(other.getConfig()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    result = prime * result + ((getResourceType() == null) ? 0 : getResourceType().hashCode());
    result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
    result = prime * result + ((getLabels() == null) ? 0 : getLabels().hashCode());
    result = prime * result + ((getConfig() == null) ? 0 : getConfig().hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(" [");
    sb.append("Hash = ").append(hashCode());
    sb.append(", id=").append(id);
    sb.append(", resourceType=").append(resourceType);
    sb.append(", name=").append(name);
    sb.append(", labels=").append(labels);
    sb.append(", config=").append(config);
    sb.append(", serialVersionUID=").append(serialVersionUID);
    sb.append("]");
    return sb.toString();
  }
}
