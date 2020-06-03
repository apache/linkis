package com.webank.wedatasphere.linkis.cs.contextcache.cache.csid;

import com.webank.wedatasphere.linkis.cs.contextcache.cache.cskey.ContextKeyValueContext;
import com.webank.wedatasphere.linkis.cs.contextcache.metric.ContextIDMetric;

/**
 * @author peacewong
 * @date 2020/2/12 16:59
 */
public interface ContextIDValue {

    String getContextID();

    ContextKeyValueContext getContextKeyValueContext();

    void  refresh();

    ContextIDMetric getContextIDMetric();
}
