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

import org.apache.linkis.bml.entity.BmlProject;

import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface BmlProjectDao {

  void createNewProject(@Param("bmlProject") BmlProject bmlProject);

  BmlProject getBmlProject(@Param("projectName") String projectName);

  void setProjectPriv(
      @Param("projectId") Integer projectId,
      @Param("usernames") List<String> usernames,
      @Param("priv") int priv,
      @Param("creator") String creator,
      @Param("createTime") Date createTime);

  Integer getPrivInProject(
      @Param("projectName") String projectName, @Param("username") String username);

  void addProjectResource(@Param("projectId") Integer id, @Param("resourceId") String resourceId);

  String getProjectNameByResourceId(@Param("resourceId") String resourceId);

  Integer getProjectIdByName(@Param("projectName") String projectName);

  void attachResourceAndProject(
      @Param("projectId") Integer projectId, @Param("resourceId") String resourceId);

  int checkIfExists(@Param("projectId") Integer projectId, @Param("resourceId") String resourceId);

  void deleteAllPriv(@Param("projectId") Integer projectId);
}
