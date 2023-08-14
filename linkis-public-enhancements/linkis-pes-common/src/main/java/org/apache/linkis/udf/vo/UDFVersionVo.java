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

package org.apache.linkis.udf.vo;

import java.util.Date;

public class UDFVersionVo {
  private Long id;
  private Long udfId;
  private String path; // 仅存储用户上一次上传的路径 作提示用
  private String bmlResourceId;
  private String bmlResourceVersion;
  private Boolean isPublished; // 共享udf被使用的是已发布的最新版本
  private String registerFormat;
  private String useFormat;
  private String description;
  private Date createTime;

  private String md5;

  private Boolean isExpire;
  private String createUser;

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUdfId() {
    return udfId;
  }

  public void setUdfId(Long udfId) {
    this.udfId = udfId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getBmlResourceId() {
    return bmlResourceId;
  }

  public void setBmlResourceId(String bmlResourceId) {
    this.bmlResourceId = bmlResourceId;
  }

  public String getBmlResourceVersion() {
    return bmlResourceVersion;
  }

  public void setBmlResourceVersion(String bmlResourceVersion) {
    this.bmlResourceVersion = bmlResourceVersion;
  }

  public Boolean getPublished() {
    return isPublished;
  }

  public void setPublished(Boolean published) {
    isPublished = published;
  }

  public String getRegisterFormat() {
    return registerFormat;
  }

  public void setRegisterFormat(String registerFormat) {
    this.registerFormat = registerFormat;
  }

  public String getUseFormat() {
    return useFormat;
  }

  public void setUseFormat(String useFormat) {
    this.useFormat = useFormat;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Boolean getExpire() {
    return isExpire;
  }

  public void setExpire(Boolean expire) {
    isExpire = expire;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }
}
