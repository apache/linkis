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

package org.apache.linkis.cs.persistence.entity;

import org.apache.linkis.cs.common.entity.listener.ListenerDomain;

import java.util.Date;

public class PersistenceContextKeyListener implements ListenerDomain {

  private Integer id;

  private String source;

  private Integer keyId;

  private Date createTime;

  private Date updateTime;

  private Date accessTime;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getKeyId() {
    return keyId;
  }

  public void setKeyId(Integer keyId) {
    this.keyId = keyId;
  }

  @Override
  public String getSource() {
    return this.source;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }
}
