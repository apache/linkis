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

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.persistence.entity.PersistenceContextKeyValue;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ContextMapMapper {
  void createMap(PersistenceContextKeyValue pKV);

  void updateMap(PersistenceContextKeyValue pKV);

  PersistenceContextKeyValue getContextMap(
      @Param("contextID") ContextID contextID, @Param("contextKey") ContextKey contextKey);

  List<PersistenceContextKeyValue> getAllContextMapByKey(
      @Param("contextID") ContextID contextID, @Param("key") String key);

  List<PersistenceContextKeyValue> getAllContextMapByContextID(
      @Param("contextID") ContextID contextID);

  List<PersistenceContextKeyValue> getAllContextMapByScope(
      @Param("contextID") ContextID contextID, @Param("contextScope") ContextScope contextScope);

  List<PersistenceContextKeyValue> getAllContextMapByType(
      @Param("contextID") ContextID contextID, @Param("contextType") ContextType contextType);

  List<PersistenceContextKeyValue> getAllContextMap(PersistenceContextKeyValue pKV);

  List<PersistenceContextKeyValue> getAllContextMapByTime(
      @Param("createTimeStart") Date createTimeStart,
      @Param("createTimeEnd") Date createTimeEnd,
      @Param("updateTimeStart") Date updateTimeStart,
      @Param("updateTimeEnd") Date updateTimeEnd,
      @Param("accessTimeStart") Date accessTimeStart,
      @Param("accessTimeEnd") Date accessTimeEnd);

  void removeContextMap(
      @Param("contextID") ContextID contextID, @Param("contextKey") ContextKey contextKey);

  void removeAllContextMapByContextID(@Param("contextID") ContextID contextID);

  void removeAllContextMapByType(
      @Param("contextID") ContextID contextID, @Param("contextType") ContextType contextType);

  void removeAllContextMapByScope(
      @Param("contextID") ContextID contextID, @Param("contextScope") ContextScope contextScope);

  void removeByKeyPrefixAndContextType(
      @Param("contextID") ContextID contextID,
      @Param("contextType") ContextType contextType,
      @Param("keyPrefix") String keyPrefix);

  void removeByKeyAndContextType(
      @Param("contextID") ContextID contextID,
      @Param("contextType") ContextType contextType,
      @Param("keyStr") String keyStr);

  void removeByKeyPrefix(
      @Param("contextID") ContextID contextID, @Param("keyPrefix") String keyPrefix);
}
