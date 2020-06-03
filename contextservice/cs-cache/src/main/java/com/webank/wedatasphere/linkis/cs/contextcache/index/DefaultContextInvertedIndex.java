package com.webank.wedatasphere.linkis.cs.contextcache.index;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author peacewong
 * @date 2020/2/10 9:45
 */
public class DefaultContextInvertedIndex implements ContextInvertedIndex {

    /**
     *  TODO Added ContextKey scoring feature
     */
    Multimap<String, String> indexMultimap = HashMultimap.create();

    @Override
    public List<String> getContextKeys(String keyword) {
        Collection<String> keywords = indexMultimap.get(keyword);
        return new ArrayList<String>(keywords);
    }

    @Override
    public boolean addValue(String keyword, String contextKey) {
       return indexMultimap.put(keyword, contextKey);
    }

    @Override
    public List<String> getContextKeys(List<String> keywords) {
        if(CollectionUtils.isEmpty(keywords)) {
            return null;
        }
        List<String> contextKeys = new ArrayList<>();
        for(String keyword:keywords){
            contextKeys.addAll(getContextKeys(keyword));
        }
        return contextKeys;
    }

    @Override
    public boolean remove(String keyword, String contextKey) {
        return indexMultimap.remove(keyword, contextKey);
    }
}
