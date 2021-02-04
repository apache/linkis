/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.cs.contextcache.cache.cskey;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSWarnException;
import com.webank.wedatasphere.linkis.cs.contextcache.index.ContextInvertedIndexSet;
import com.webank.wedatasphere.linkis.cs.contextcache.parser.ContextKeyValueParser;

import java.util.List;
import java.util.Map;

/**
 * @author peacewong
 * @date 2020/2/12 10:24
 */
public interface ContextKeyValueContext {

    ContextID getContextID();

    void setContextID(ContextID contextID) throws CSWarnException;

    ContextInvertedIndexSet getContextInvertedIndexSet();

    ContextValueMapSet getContextValueMapSet();

    ContextKeyValueParser getContextKeyValueParser();

    ContextKeyValue put(ContextKeyValue contextKeyValue);

    ContextKeyValue  getContextKeyValue(ContextKey contextKey, ContextType contextType);

    List<ContextKeyValue>  getValues(String keyword, ContextType contextType);

    List<ContextKeyValue>  getValues(List<String> contextKeys, ContextType contextType);

    List<ContextKeyValue>  getAllValues(ContextType contextType);

    List<ContextKeyValue>  getAllLikes(String regex,ContextType contextType);

    List<ContextKeyValue> getAll();

    ContextKeyValue remove(ContextKey contextKey);

    Map<String, ContextKeyValue> removeAll(ContextType contextType);

    Boolean putAll(List<ContextKeyValue> contextKeyValueList);

    void removeByKeyPrefix(String preFix);

    void removeByKeyPrefix(String preFix, ContextType csType);
}
