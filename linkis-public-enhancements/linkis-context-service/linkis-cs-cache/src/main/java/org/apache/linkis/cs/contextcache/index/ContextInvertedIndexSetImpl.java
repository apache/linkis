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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ContextInvertedIndexSetImpl  implements ContextInvertedIndexSet{

    private static final Logger logger = LoggerFactory.getLogger(ContextInvertedIndexSetImpl.class);

    private Map<String, ContextInvertedIndex> invertedIndexMap = new HashMap<>();

    @Override
    public ContextInvertedIndex getContextInvertedIndex(ContextType contextType) {
        String csType = contextType.name();
        if (! invertedIndexMap.containsKey(csType)){
            synchronized (csType.intern()){
                if (! invertedIndexMap.containsKey(csType)){
                    logger.info("For ContextType({}) init invertedIndex", csType);
                    invertedIndexMap.put(csType, new DefaultContextInvertedIndex());
                }
            }
        }
       return invertedIndexMap.get(csType);
    }

    @Override
    public boolean addValue(String keyword, ContextKey contextKey) {
        return addValue(keyword, contextKey.getKey(), contextKey.getContextType());
    }

    @Override
    public boolean addValue(String keyword, String contextKey, ContextType contextType) {
        return  getContextInvertedIndex(contextType).addValue(keyword, contextKey);
    }

    @Override
    public boolean addKeywords(Set<String> keywords, String contextKey, ContextType contextType) {
        Iterator<String> iterator = keywords.iterator();
        while (iterator.hasNext()){
            addValue(iterator.next(), contextKey, contextType);
        }
        return true;
    }

    @Override
    public List<String> getContextKeys(String keyword, ContextType contextType) {
        return getContextInvertedIndex(contextType).getContextKeys(keyword);
    }

    @Override
    public boolean remove(String keyword, String contextKey, ContextType contextType) {
        return getContextInvertedIndex(contextType).remove(keyword, contextKey);
    }

    @Override
    public ContextInvertedIndex removeAll(ContextType contextType) {
        return invertedIndexMap.remove(contextType.name());
    }
}
