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

package org.apache.linkis.bml.service;

import java.util.List;

public interface BmlProjectService {

  int createBmlProject(
      String projectName, String creator, List<String> editUsers, List<String> accessUsers);

  boolean checkEditPriv(String projectName, String username);

  boolean checkAccessPriv(String projectName, String username);

  void setProjectEditPriv(String projectName, List<String> editUsers);

  void addProjectEditPriv(String projectName, List<String> editUsers);

  void deleteProjectEditPriv(String projectName, List<String> editUsers);

  void setProjectAccessPriv(String projectName, List<String> accessUsers);

  void addProjectAccessPriv(String projectName, List<String> accessUsers);

  void deleteProjectAccessPriv(String projectName, List<String> accessUsers);

  String getProjectNameByResourceId(String resourceId);

  void addProjectResource(String resourceId, String projectName);

  void attach(String projectName, String resourceId);

  void updateProjectUsers(
      String username, String projectName, List<String> editUsers, List<String> accessUsers);
}
