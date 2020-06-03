package com.webank.wedatasphere.linkis.cs.contextcache.cache;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValue;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.ContextCacheMetric;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author peacewong
 * @date 2020/2/12 16:45
 */
public interface ContextCache {

    ContextIDValue getContextIDValue(ContextID contextID)  throws CSErrorException;

    void remove(ContextID contextID);

    void put(ContextIDValue contextIDValue) throws CSErrorException;

    Map<String, ContextIDValue> getAllPresent(List<ContextID> contextIDList);

    void refreshAll() throws CSErrorException;

    void putAll(List<ContextIDValue> contextIDValueList) throws CSErrorException;

    ContextIDValue loadContextIDValue(ContextID contextID);

    ContextCacheMetric getContextCacheMetric();
}
