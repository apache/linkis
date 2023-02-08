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

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.exception.CSErrorException;

import java.util.Date;
import java.util.List;

public interface ContextMapPersistence {

  void create(ContextID contextID, ContextKeyValue contextKeyValue) throws CSErrorException;

  void update(ContextID contextID, ContextKeyValue contextKeyValue) throws CSErrorException;

  ContextKeyValue get(ContextID contextID, ContextKey contextKey) throws CSErrorException;

  List<ContextKeyValue> getAll(ContextID contextID, String key) throws CSErrorException;

  List<ContextKeyValue> getAll(ContextID contextID) throws CSErrorException;

  List<ContextKeyValue> getAll(ContextID contextID, ContextScope contextScope)
      throws CSErrorException;

  List<ContextKeyValue> getAll(ContextID contextID, ContextType contextType)
      throws CSErrorException;

  void reset(ContextID contextID, ContextKey contextKey) throws CSErrorException;

  void remove(ContextID contextID, ContextKey contextKey) throws CSErrorException;

  void removeAll(ContextID contextID) throws CSErrorException;

  void removeAll(ContextID contextID, ContextType contextType) throws CSErrorException;

  void removeAll(ContextID contextID, ContextScope contextScope) throws CSErrorException;

  void removeByKeyPrefix(ContextID contextID, String keyPrefix);

  void removeByKeyPrefix(ContextID contextID, ContextType contextType, String keyPrefix);

  void removeByKey(ContextID contextID, ContextType contextType, String keyPrefix);

  List<ContextKeyValue> searchContextIDByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd);
}
