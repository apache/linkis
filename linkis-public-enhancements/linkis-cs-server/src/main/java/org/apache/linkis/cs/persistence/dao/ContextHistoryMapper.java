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

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.persistence.entity.PersistenceContextHistory;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ContextHistoryMapper {

  void createHistory(PersistenceContextHistory pHistory);

  PersistenceContextHistory getHistory(
      @Param("contextID") ContextID contextID, @Param("id") Long id);

  PersistenceContextHistory getHistoryBySource(
      @Param("contextID") ContextID contextID, @Param("source") String source);

  List<PersistenceContextHistory> getHistoriesByContextID(@Param("contextID") ContextID contextID);

  void removeHistory(@Param("contextID") ContextID contextID, @Param("source") String source);

  void updateHistory(
      @Param("contextID") ContextID contextID,
      @Param("pHistory") PersistenceContextHistory pHistory);

  List<PersistenceContextHistory> searchByKeywords(
      @Param("contextID") ContextID contextID, @Param("keywords") String[] keywords);

  List<PersistenceContextHistory> searchByKeywordsAndType(
      @Param("contextType") ContextType contextType, @Param("keywords") String[] keywords);
}
