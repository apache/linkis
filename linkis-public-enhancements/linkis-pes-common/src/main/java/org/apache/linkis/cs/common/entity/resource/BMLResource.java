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

package org.apache.linkis.cs.common.entity.resource;

import org.apache.linkis.cs.common.annotation.KeywordMethod;

import java.util.Date;

public interface BMLResource extends Resource {

  @KeywordMethod
  String getResourceId();

  String getVersion();

  String getUser();

  void setUser(String user);

  String getSystem();

  void setSystem(String system);

  Boolean getEnableFlag();

  void setEnableFlag(Boolean enableFlag);

  Boolean getPrivate();

  void setPrivate(Boolean aPrivate);

  String getResourceHeader();

  void setResourceHeader(String resourceHeader);

  @KeywordMethod
  String getDownloadedFileName();

  void setDownloadedFileName(String downloadedFileName);

  String getSys();

  void setSys(String sys);

  Date getCreateTime();

  void setCreateTime(Date createTime);

  Boolean getExpire();

  void setExpire(Boolean expire);

  String getExpireType();

  void setExpireType(String expireType);

  String getExpireTime();

  void setExpireTime(String expireTime);

  Date getUpdateTime();

  void setUpdateTime(Date updateTime);

  String getUpdator();

  void setUpdator(String updator);

  Integer getMaxVersion();

  void setMaxVersion(Integer maxVersion);
}
