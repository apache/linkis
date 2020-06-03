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
