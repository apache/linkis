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

package org.apache.linkis.cs.execution.impl;

import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.condition.BinaryLogicCondition;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.impl.ContextTypeCondition;
import org.apache.linkis.cs.contextcache.ContextCacheService;
import org.apache.linkis.cs.execution.AbstractConditionExecution;
import org.apache.linkis.cs.execution.fetcher.ContextCacheFetcher;
import org.apache.linkis.cs.execution.fetcher.ContextTypeContextSearchFetcher;

public abstract class BinaryLogicConditionExecution extends AbstractConditionExecution {

  ContextCacheFetcher fastFetcher;

  public BinaryLogicConditionExecution(
      BinaryLogicCondition condition,
      ContextCacheService contextCacheService,
      ContextID contextID) {
    super(condition, contextCacheService, contextID);
    ContextTypeCondition contextTypeCondition = findFastCondition(condition.getLeft(), condition);
    if (contextTypeCondition != null) {
      fastFetcher =
          new ContextTypeContextSearchFetcher(
              contextCacheService, contextTypeCondition.getContextType());
    }
  }

  protected ContextTypeCondition findFastCondition(
      Condition condition, BinaryLogicCondition parent) {
    if (condition instanceof BinaryLogicCondition) {
      BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
      return findFastCondition(binaryLogicCondition.getLeft(), binaryLogicCondition);
    } else if (condition instanceof ContextTypeCondition) {
      parent.setLeft(null);
      return (ContextTypeCondition) condition;
    } else {
      return null;
    }
  }

  @Override
  protected ContextCacheFetcher getFastFetcher() {
    return fastFetcher;
  }

  @Override
  protected boolean needOptimization() {
    return true;
  }
}
