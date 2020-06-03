package com.webank.wedatasphere.linkis.cs.contextcache.index;


import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;

import java.util.List;
import java.util.Set;

/**
 * @author peacewong
 * @date 2020/2/10 17:27
 */
public interface ContextInvertedIndexSet {

    ContextInvertedIndex getContextInvertedIndex(ContextType contextType);

    boolean addValue(String keyword, ContextKey contextKey);

    boolean addValue(String keyword, String contextKey, ContextType contextType);

    boolean addKeywords(Set<String> keywords, String contextKey, ContextType contextType);

    List<String> getContextKeys(String keyword, ContextType contextType);

    boolean remove(String keyword, String contextKey, ContextType contextType);

    ContextInvertedIndex removeAll(ContextType contextType);
}
