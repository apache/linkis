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

package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.entity.Resource;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ResourceDao {

  List<Resource> getResources(Map paramMap);

  void deleteResource(@Param("resourceId") String resourceId);

  void batchDeleteResources(@Param("resourceIds") List<String> resourceIds);

  long uploadResource(Resource resource);

  int checkExists(@Param("resourceId") String resourceId);

  Resource getResource(@Param("resourceId") String resourceId);

  String getUserByResourceId(@Param("resourceId") String resourceId);

  void changeOwner(
      @Param("resourceId") String resourceId,
      @Param("oldOwner") String oldOwner,
      @Param("newOwner") String newOwner);
}
