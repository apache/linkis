package com.webank.wedatasphere.linkis.cs.execution.ruler;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.execution.matcher.ContextSearchMatcher;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommonListContextSearchRuler extends AbstractContextSearchRuler{

    private static Logger logger = LoggerFactory.getLogger(CommonListContextSearchRuler.class);

    public CommonListContextSearchRuler(ContextSearchMatcher matcher) {
        super(matcher);
    }

    @Override
    public List<ContextKeyValue> rule(List<ContextKeyValue> contextKeyValues) {
        List<ContextKeyValue> filtered = Lists.newArrayList();
        if(CollectionUtils.isEmpty(contextKeyValues)){
            logger.warn("Empty result fetched from cache.");
            return filtered;
        }
        int size = contextKeyValues.size();
        for(int i=0;i<size;i++){
            ContextKeyValue contextKeyValue = contextKeyValues.get(i);
            if(matcher.match(contextKeyValue)){
                filtered.add(contextKeyValue);
            }
        }
        return filtered;
    }
}
