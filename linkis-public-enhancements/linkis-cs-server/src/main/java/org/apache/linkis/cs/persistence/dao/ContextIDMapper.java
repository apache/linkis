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

package org.apache.linkis.cs.persistence.dao;

import org.apache.linkis.cs.persistence.entity.PersistenceContextID;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ContextIDMapper {
  void createContextID(PersistenceContextID persistenceContextID);

  void deleteContextID(String contextId);

  PersistenceContextID getContextID(String contextId);

  void updateContextID(PersistenceContextID persistenceContextID);

  List<PersistenceContextID> searchContextID(PersistenceContextID persistenceContextID);

  List<PersistenceContextID> getAllContextIDByTime(
      @Param("createTimeStart") Date createTimeStart,
      @Param("createTimeEnd") Date createTimeEnd,
      @Param("updateTimeStart") Date updateTimeStart,
      @Param("updateTimeEnd") Date updateTimeEnd,
      @Param("accessTimeStart") Date accessTimeStart,
      @Param("accessTimeEnd") Date accessTimeEnd);
}
