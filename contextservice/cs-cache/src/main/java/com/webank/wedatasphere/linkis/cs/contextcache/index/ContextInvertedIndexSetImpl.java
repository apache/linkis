package com.webank.wedatasphere.linkis.cs.contextcache.index;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author peacewong
 * @date 2020/2/10 19:54
 */
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
