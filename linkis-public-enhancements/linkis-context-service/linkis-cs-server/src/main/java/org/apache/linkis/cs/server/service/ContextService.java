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

package org.apache.linkis.cs.server.service;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.common.entity.source.ContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.exception.ContextSearchFailedException;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class ContextService extends AbstractService {

  public abstract ContextValue getContextValue(ContextID contextID, ContextKey contextKey);

  public abstract List<ContextKeyValue> searchContextValue(
      ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException;

  // public abstract List<ContextKeyValue> searchContextValueByCondition(Condition condition)
  // throws ContextSearchFailedException;

  public abstract void setValueByKey(
      ContextID contextID, ContextKey contextKey, ContextValue contextValue)
      throws CSErrorException, ClassNotFoundException, JsonProcessingException;

  public abstract void setValue(ContextID contextID, ContextKeyValue contextKeyValuee)
      throws CSErrorException, ClassNotFoundException, JsonProcessingException;

  public abstract void resetValue(ContextID contextID, ContextKey contextKey)
      throws CSErrorException;

  public abstract void removeValue(ContextID contextID, ContextKey contextKey)
      throws CSErrorException;

  public abstract void removeAllValue(ContextID contextID) throws CSErrorException;

  public abstract void removeAllValueByKeyPrefixAndContextType(
      ContextID contextID, ContextType contextType, String keyPrefix) throws CSErrorException;

  public abstract void removeValueByKeyAndContextType(
      ContextID contextID, ContextType contextType, String keyPrefix) throws CSErrorException;

  public abstract void removeAllValueByKeyPrefix(ContextID contextID, String keyPrefix)
      throws CSErrorException;

  public abstract int clearAllContextByID(List<String> idList) throws CSErrorException;

  public abstract int clearAllContextByTime(
      Date createTimeStart,
      Date createTimeEnd,
      Date updateTimeStart,
      Date updateTimeEnd,
      Date accessTimeStart,
      Date accessTimeEnd)
      throws CSErrorException;
}
