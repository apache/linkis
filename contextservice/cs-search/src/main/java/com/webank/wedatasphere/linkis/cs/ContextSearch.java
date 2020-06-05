package com.webank.wedatasphere.linkis.cs;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.contextcache.ContextCacheService;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.ContextCache;
import com.webank.wedatasphere.linkis.cs.exception.ContextSearchFailedException;

import java.util.List;
import java.util.Map;

public interface ContextSearch {

    List<ContextKeyValue> search(ContextCacheService contextCacheService, ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException;
    List<ContextKeyValue> search(ContextCacheService contextCacheService, ContextID contextID, Condition condition) throws ContextSearchFailedException;
}
