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

package org.apache.linkis.cs.persistence.persistence;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;

import java.util.Date;
import java.util.List;

public interface ContextIDPersistence {

  ContextID createContextID(ContextID contextID) throws CSErrorException;

  void deleteContextID(String contextId) throws CSErrorException;

  void updateContextID(ContextID contextID) throws CSErrorException;

  ContextID getContextID(String contextId) throws CSErrorException;

  List<PersistenceContextID> searchContextID(PersistenceContextID contextID)
      throws CSErrorException;

  List<PersistenceContextID> searchCSIDByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd)
      throws CSErrorException;
}
