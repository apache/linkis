package com.webank.wedatasphere.linkis.cs.contextcache.index;

import java.util.List;

/**
 * @author peacewong
 * @date 2020/2/10 9:42
 */
public interface ContextInvertedIndex {

    List<String> getContextKeys(String keyword);

    boolean addValue(String keyword, String contextKey);

    List<String> getContextKeys(List<String> keywords);

    boolean remove(String keyword, String contextKey);
}
