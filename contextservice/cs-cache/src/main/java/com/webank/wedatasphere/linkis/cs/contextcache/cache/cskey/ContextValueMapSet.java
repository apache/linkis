package com.webank.wedatasphere.linkis.cs.contextcache.cache.cskey;


import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;

import java.util.List;
import java.util.Map;

/**
 * @author peacewong
 * @date 2020/2/11 15:20
 */
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

    List<ContextKey> findByKeyPrefix(String preFix, ContextType csType);
}
