/*
package com.webank.wedatasphere.linkis.cs.contextcache.cache.guava;

import com.google.common.cache.CacheLoader;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValue;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValueGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContextIDCacheLoader extends CacheLoader<String, ContextIDValue> {

    private static final Logger logger = LoggerFactory.getLogger(ContextIDCacheLoader.class);

    @Autowired
    private ContextIDValueGenerator contextIDValueGenerator;
    
    @Override
    public ContextIDValue load(String contextID) throws Exception {
        logger.info("Start to load contextID:{}", contextID);
        ContextIDValue contextIDValue = contextIDValueGenerator.createContextIDValue(contextID);

        logger.info("Finished to load contextID:{}", contextID);
        return contextIDValue;
    }
}
*/
