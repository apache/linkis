package com.webank.wedatasphere.linkis.cs.contextcache.parser;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;

import java.util.Set;

/**
 * @author peacewong
 * @date 2020/2/9 16:15
 */
public interface ContextKeyValueParser {

    Set<String> parse(ContextKeyValue contextKeyValue);
}
