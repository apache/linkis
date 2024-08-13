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

package org.apache.linkis.cs.contextcache.cache.cskey;

import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextKey;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;

import java.util.List;
import java.util.Map;

public interface ContextValueMapSet {

  Map<String, ContextKeyValue> getContextValueMap(ContextType contextType);

  ContextKeyValue put(ContextKeyValue contextKeyValue);

  ContextKeyValue getByContextKey(ContextKey contextKey, ContextType contextType);

  ContextKeyValue getByContextKey(String contextKey, ContextType contextType);

  List<ContextKeyValue> getByContextKeys(List<String> contextKeys, ContextType contextType);

  List<ContextKeyValue> getAllValuesByType(ContextType contextType);

  List<ContextKeyValue> getAllLikes(String regex, ContextType contextType);

  List<ContextKeyValue> getAll();

  ContextKeyValue remove(String contextKey, ContextType contextType);

  Map<String, ContextKeyValue> removeAll(ContextType contextType);

  List<ContextKey> findByKeyPrefix(String preFix);

  List<ContextKey> findByKey(String keyStr);

  List<ContextKey> findByKeyPrefix(String preFix, ContextType csType);

  List<ContextKey> findByKey(String keyStr, ContextType csType);
}
