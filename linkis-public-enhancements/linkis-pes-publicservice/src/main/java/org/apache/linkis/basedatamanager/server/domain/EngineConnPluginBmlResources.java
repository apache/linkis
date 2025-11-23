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
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("linkis_cg_engine_conn_plugin_bml_resources")
public class EngineConnPluginBmlResources implements Serializable {
  private static final long serialVersionUID = -6976329190060659981L;

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  private String engineConnType;

  private String version;

  private String fileName;

  private Long fileSize;

  private Long lastModified;

  private String bmlResourceId;

  private String bmlResourceVersion;

  private Date createTime;

  private Date lastUpdateTime;

  /** Primary key */
  public Long getId() {
    return id;
  }

  /** Primary key */
  public void setId(Long id) {
    this.id = id;
  }

  /** Engine type */
  public String getEngineConnType() {
    return engineConnType;
  }

  /** Engine type */
  public void setEngineConnType(String engineConnType) {
    this.engineConnType = engineConnType;
  }

  /** version */
  public String getVersion() {
    return version;
  }

  /** version */
  public void setVersion(String version) {
    this.version = version;
  }

  /** file name */
  public String getFileName() {
    return fileName;
  }

  /** file name */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /** file size */
  public Long getFileSize() {
    return fileSize;
  }

  /** file size */
  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  /** File update time */
  public Long getLastModified() {
    return lastModified;
  }

  /** File update time */
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  /** Owning system */
  public String getBmlResourceId() {
    return bmlResourceId;
  }

  /** Owning system */
  public void setBmlResourceId(String bmlResourceId) {
    this.bmlResourceId = bmlResourceId;
  }

  /** Resource owner */
  public String getBmlResourceVersion() {
    return bmlResourceVersion;
  }

  /** Resource owner */
  public void setBmlResourceVersion(String bmlResourceVersion) {
    this.bmlResourceVersion = bmlResourceVersion;
  }

  /** created time */
  public Date getCreateTime() {
    return createTime;
  }

  /** created time */
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  /** updated time */
  public Date getLastUpdateTime() {
    return lastUpdateTime;
  }

  /** updated time */
  public void setLastUpdateTime(Date lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }
}
