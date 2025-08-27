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

import org.apache.linkis.cs.common.entity.enumeration.ExpireType;
import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.entity.source.UserContextID;
import org.apache.linkis.cs.persistence.annotation.Ignore;

import java.util.Date;

@Ignore
public class PersistenceContextID implements UserContextID, HAContextID {

  private String contextId;

  private String user;

  private String instance;

  private String backupInstance;

  private String application;

  private ExpireType expireType;

  private Date expireTime;

  private String source;

  private Date createTime;

  private Date updateTime;

  private Date accessTime;

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public String getContextId() {
    return this.contextId;
  }

  @Override
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  @Override
  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public String getUser() {
    return this.user;
  }

  @Override
  public String getInstance() {
    return this.instance;
  }

  @Override
  public void setInstance(String instance) {
    this.instance = instance;
  }

  @Override
  public String getBackupInstance() {
    return this.backupInstance;
  }

  @Override
  public void setBackupInstance(String backupInstance) {
    this.backupInstance = backupInstance;
  }

  @Override
  public HAContextID copy() {
    PersistenceContextID persistenceContextID = new PersistenceContextID();
    persistenceContextID.setContextId(contextId);
    persistenceContextID.setAccessTime(accessTime);
    persistenceContextID.setApplication(application);
    persistenceContextID.setCreateTime(createTime);
    persistenceContextID.setUpdateTime(updateTime);
    persistenceContextID.setBackupInstance(backupInstance);
    persistenceContextID.setExpireTime(expireTime);
    persistenceContextID.setExpireType(expireType);
    persistenceContextID.setInstance(instance);
    persistenceContextID.setSource(source);
    persistenceContextID.setUser(user);
    persistenceContextID.setContextIDType(getContextIDType());
    return persistenceContextID;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public ExpireType getExpireType() {
    return expireType;
  }

  public void setExpireType(ExpireType expireType) {
    this.expireType = expireType;
  }

  public Date getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(Date expireTime) {
    this.expireTime = expireTime;
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

  public Date getAccessTime() {
    return accessTime;
  }

  public void setAccessTime(Date accessTime) {
    this.accessTime = accessTime;
  }
}
