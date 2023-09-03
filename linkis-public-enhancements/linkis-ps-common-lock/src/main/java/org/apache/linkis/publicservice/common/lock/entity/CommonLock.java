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

package org.apache.linkis.publicservice.common.lock.entity;

import java.util.Date;

public class CommonLock {
  private int id;
  private String lockObject;
  private Long timeOut;

  private Date updateTime;
  private Date createTime;
  private String updator;
  private String creator;
  private String locker;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getLockObject() {
    return lockObject;
  }

  public void setLockObject(String lockObject) {
    this.lockObject = lockObject;
  }

  public Long getTimeOut() {
    return timeOut;
  }

  public void setTimeOut(Long timeOut) {
    this.timeOut = timeOut;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getUpdator() {
    return updator;
  }

  public void setUpdator(String updator) {
    this.updator = updator;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getLocker() {
    return locker;
  }

  public void setLocker(String locker) {
    this.locker = locker;
  }
}
