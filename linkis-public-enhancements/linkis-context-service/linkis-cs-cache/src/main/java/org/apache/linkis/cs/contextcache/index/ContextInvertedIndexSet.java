/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.contextcache.index;


import org.apache.linkis.cs.common.entity.enumeration.ContextType;
import org.apache.linkis.cs.common.entity.source.ContextKey;

import java.util.List;
import java.util.Set;

public interface ContextInvertedIndexSet {

    ContextInvertedIndex getContextInvertedIndex(ContextType contextType);

    boolean addValue(String keyword, ContextKey contextKey);

    boolean addValue(String keyword, String contextKey, ContextType contextType);

    boolean addKeywords(Set<String> keywords, String contextKey, ContextType contextType);

    List<String> getContextKeys(String keyword, ContextType contextType);

    boolean remove(String keyword, String contextKey, ContextType contextType);

    ContextInvertedIndex removeAll(ContextType contextType);
}
