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
package com.webank.wedatasphere.linkis.cs.persistence.dao;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by patinousward on 2020/2/13.
 */
public interface ContextMapMapper {
    void createMap(PersistenceContextKeyValue pKV);

    void updateMap(PersistenceContextKeyValue pKV);

    PersistenceContextKeyValue getContextMap(@Param("contextID") ContextID contextID, @Param("contextKey") ContextKey contextKey);

    List<PersistenceContextKeyValue> getAllContextMapByKey(@Param("contextID") ContextID contextID, @Param("key") String key);

    List<PersistenceContextKeyValue> getAllContextMapByContextID(@Param("contextID") ContextID contextID);

    List<PersistenceContextKeyValue> getAllContextMapByScope(@Param("contextID") ContextID contextID, @Param("contextScope") ContextScope contextScope);

    List<PersistenceContextKeyValue> getAllContextMapByType(@Param("contextID") ContextID contextID, @Param("contextType") ContextType contextType);

    void removeContextMap(@Param("contextID") ContextID contextID, @Param("contextKey") ContextKey contextKey);

    void removeAllContextMapByContextID(@Param("contextID") ContextID contextID);

    void removeAllContextMapByType(@Param("contextID") ContextID contextID, @Param("contextType") ContextType contextType);

    void removeAllContextMapByScope(@Param("contextID") ContextID contextID, @Param("contextScope") ContextScope contextScope);

    void removeByKeyPrefixAndContextType(@Param("contextID") ContextID contextID, @Param("contextType") ContextType contextType, @Param("keyPrefix") String keyPrefix);

    void removeByKeyPrefix(@Param("contextID") ContextID contextID, @Param("keyPrefix") String keyPrefix);
}
