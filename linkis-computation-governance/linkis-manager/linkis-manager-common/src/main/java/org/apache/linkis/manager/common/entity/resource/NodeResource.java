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

package org.apache.linkis.manager.common.entity.resource;

import org.apache.linkis.protocol.message.RequestProtocol;

import java.io.Serializable;
import java.util.Date;

public interface NodeResource extends Serializable, RequestProtocol {

  Integer getId();

  void setId(Integer id);

  ResourceType getResourceType();

  void setCreateTime(Date createTime);

  Date getCreateTime();

  Date getUpdateTime();

  void setUpdateTime(Date updateTime);

  void setResourceType(ResourceType resourceType);

  void setMinResource(Resource minResource);

  Resource getMinResource();

  void setMaxResource(Resource maxResource);

  Resource getMaxResource();

  void setUsedResource(Resource usedResource);

  Resource getUsedResource();

  void setLockedResource(Resource lockedResource);

  Resource getLockedResource();

  void setExpectedResource(Resource expectedResource);

  Resource getExpectedResource();

  void setLeftResource(Resource leftResource);

  Resource getLeftResource();
}
