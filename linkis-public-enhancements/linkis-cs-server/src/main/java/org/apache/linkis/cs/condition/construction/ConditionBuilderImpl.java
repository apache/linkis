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

package org.apache.linkis.cs.condition.construction;

import org.apache.linkis.cs.common.entity.enumeration.ContextScope;
import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.impl.ContainsCondition;
import org.apache.linkis.cs.condition.impl.ContextScopeCondition;
import org.apache.linkis.cs.condition.impl.ContextTypeCondition;
import org.apache.linkis.cs.condition.impl.RegexCondition;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class ConditionBuilderImpl implements ConditionBuilder {

  Collection<ContextType> contextTypes;
  Collection<ContextScope> contextScopes;
  String regex;
  String containsValue;

  @Override
  public ConditionBuilder contextTypes(Collection<ContextType> contextTypes) {
    this.contextTypes = contextTypes;
    return this;
  }

  @Override
  public ConditionBuilder contextScopes(Collection<ContextScope> contextScopes) {
    this.contextScopes = contextScopes;
    return this;
  }

  @Override
  public ConditionBuilder regex(String regex) {
    this.regex = regex;
    return this;
  }

  @Override
  public ConditionBuilder contains(String value) {
    this.containsValue = value;
    return this;
  }

  @Override
  public Condition build() {
    Condition condition = null;
    if (CollectionUtils.isNotEmpty(contextTypes)) {
      for (ContextType contextType : contextTypes) {
        Condition subCondition = new ContextTypeCondition(contextType);
        condition = combineCondition(condition, subCondition, true);
      }
    }
    Condition conditionContextScope = null;
    if (CollectionUtils.isNotEmpty(contextScopes)) {
      for (ContextScope contextScope : contextScopes) {
        Condition subCondition = new ContextScopeCondition(contextScope);
        conditionContextScope = combineCondition(conditionContextScope, subCondition, true);
      }
      condition = combineCondition(condition, conditionContextScope, false);
    }

    if (StringUtils.isNotBlank(regex)) {
      Condition subCondition = new RegexCondition(regex);
      condition = combineCondition(condition, subCondition, false);
    }
    if (StringUtils.isNotBlank(containsValue)) {
      Condition subCondition = new ContainsCondition(containsValue);
      condition = combineCondition(condition, subCondition, false);
    }
    return condition;
  }

  private Condition combineCondition(Condition condition, Condition subCondition, boolean or) {
    if (condition == null) {
      condition = subCondition;
    } else if (or) {
      condition = condition.or(subCondition);
    } else {
      condition = condition.and(subCondition);
    }
    return condition;
  }
}
