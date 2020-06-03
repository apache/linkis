package com.webank.wedatasphere.linkis.cs.contextcache.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peacewong
 * @date 2020/2/16 15:33
 */
public class SizeEstimator {

    private static final Logger logger = LoggerFactory.getLogger(SizeEstimator.class);


    private static ClassIntrospector classIntrospector = new ClassIntrospector();

    public static Long estimate(Object obj) {
        try {
            if (obj == null){
                return  0L;
            }
            ObjectInfo info = classIntrospector.introspect(obj);
            return info.getDeepSize();
        } catch (Throwable e) {
            logger.info("estimate size failed", e);
        }
        return 0L;
    }

}

