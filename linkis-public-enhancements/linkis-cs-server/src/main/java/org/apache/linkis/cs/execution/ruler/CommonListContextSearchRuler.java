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

package org.apache.linkis.cs.execution.ruler;

import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.execution.matcher.ContextSearchMatcher;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonListContextSearchRuler extends AbstractContextSearchRuler {

  private static Logger logger = LoggerFactory.getLogger(CommonListContextSearchRuler.class);

  public CommonListContextSearchRuler(ContextSearchMatcher matcher) {
    super(matcher);
  }

  @Override
  public List<ContextKeyValue> rule(List<ContextKeyValue> contextKeyValues) {
    List<ContextKeyValue> filtered = Lists.newArrayList();
    if (CollectionUtils.isEmpty(contextKeyValues)) {
      logger.warn("Empty result fetched from cache.");
      return filtered;
    }
    int size = contextKeyValues.size();
    for (int i = 0; i < size; i++) {
      ContextKeyValue contextKeyValue = contextKeyValues.get(i);
      if (matcher.match(contextKeyValue)) {
        filtered.add(contextKeyValue);
      }
    }
    return filtered;
  }
}
