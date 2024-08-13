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

package org.apache.linkis.cs.persistence;

import org.apache.linkis.cs.common.entity.enumeration.ExpireType;
import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.entity.source.UserContextID;

import java.util.Date;

public class AContextID implements UserContextID, HAContextID {

  private String contextId;

  private String user = "hadoop";

  private String instance = "instance";

  private String backupInstance = "backup";

  private String application = "spark";

  private ExpireType expireType = ExpireType.NEVER;

  private Date expireTime = new Date();

  private String project = "project1";

  private String flow = "flow1";

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getFlow() {
    return flow;
  }

  public void setFlow(String flow) {
    this.flow = flow;
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

  @Override
  public int getContextIDType() {
    return 0;
  }

  @Override
  public void setContextIDType(int contextIDType) {}

  @Override
  public HAContextID copy() {
    return null;
  }
}
