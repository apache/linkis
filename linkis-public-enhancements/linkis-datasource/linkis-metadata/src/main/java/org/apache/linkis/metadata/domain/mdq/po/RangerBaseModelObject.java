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

package org.apache.linkis.metadata.domain.mdq.po;

public class RangerBaseModelObject implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String guid;
  private Boolean isEnabled;
  private String createdBy;
  private String updatedBy;
  private String createTime;
  private String updateTime;
  private Long version;

  public RangerBaseModelObject() {
    setIsEnabled(null);
  }

  public void updateFrom(RangerBaseModelObject other) {
    setIsEnabled(other.getIsEnabled());
  }

  /** @return the id */
  public Long getId() {
    return id;
  }
  /** @param id the id to set */
  public void setId(Long id) {
    this.id = id;
  }
  /** @return the guid */
  public String getGuid() {
    return guid;
  }
  /** @param guid the guid to set */
  public void setGuid(String guid) {
    this.guid = guid;
  }
  /** @return the isEnabled */
  public Boolean getIsEnabled() {
    return isEnabled;
  }
  /** @param isEnabled the isEnabled to set */
  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled == null ? Boolean.TRUE : isEnabled;
  }
  /** @return the createdBy */
  public String getCreatedBy() {
    return createdBy;
  }
  /** @param createdBy the createdBy to set */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }
  /** @return the updatedBy */
  public String getUpdatedBy() {
    return updatedBy;
  }
  /** @param updatedBy the updatedBy to set */
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }
  /** @return the createTime */
  public String getCreateTime() {
    return createTime;
  }
  /** @param createTime the createTime to set */
  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }
  /** @return the updateTime */
  public String getUpdateTime() {
    return updateTime;
  }
  /** @param updateTime the updateTime to set */
  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }
  /** @return the version */
  public Long getVersion() {
    return version;
  }
  /** @param version the version to set */
  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    toString(sb);

    return sb.toString();
  }

  public StringBuilder toString(StringBuilder sb) {
    sb.append("id={").append(id).append("} ");
    sb.append("guid={").append(guid).append("} ");
    sb.append("isEnabled={").append(isEnabled).append("} ");
    sb.append("createdBy={").append(createdBy).append("} ");
    sb.append("updatedBy={").append(updatedBy).append("} ");
    sb.append("createTime={").append(createTime).append("} ");
    sb.append("updateTime={").append(updateTime).append("} ");
    sb.append("version={").append(version).append("} ");

    return sb;
  }
}
