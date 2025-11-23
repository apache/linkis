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

package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ResourceManagerMapper {

  void registerResource(PersistenceResource persistenceResource);

  void nodeResourceUpdate(
      @Param("ticketId") String ticketId,
      @Param("persistenceResource") PersistenceResource persistenceResource);

  void nodeResourceUpdateByResourceId(
      @Param("resourceId") int resourceId,
      @Param("persistenceResource") PersistenceResource persistenceResource);

  Integer getNodeResourceUpdateResourceId(@Param("instance") String instance);

  void deleteResourceAndLabelId(@Param("instance") String instance);

  void deleteResourceByInstance(@Param("instance") String instance);

  void deleteResourceByTicketId(@Param("ticketId") String ticketId);

  List<PersistenceResource> getResourceByInstanceAndResourceType(
      @Param("instance") String instance, @Param("resourceType") String resourceType);

  List<PersistenceResource> getResourceByServiceInstance(@Param("instance") String instance);

  PersistenceResource getNodeResourceByTicketId(@Param("ticketId") String ticketId);

  List<PersistenceResource> getResourceByUserName(@Param("userName") String userName);

  List<PersistenceLabel> getLabelsByTicketId(@Param("ticketId") String ticketId);

  void deleteResourceById(@Param("ids") List<Integer> ids);

  void deleteResourceRelByResourceId(@Param("ids") List<Integer> ids);

  PersistenceResource getResourceById(@Param("id") Integer id);
}
