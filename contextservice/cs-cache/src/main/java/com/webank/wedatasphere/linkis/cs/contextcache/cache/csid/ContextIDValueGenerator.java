package com.webank.wedatasphere.linkis.cs.contextcache.cache.csid;


import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

/**
 * @author peacewong
 * @date 2020/2/13 14:47
 */
public interface ContextIDValueGenerator {

    ContextIDValue createContextIDValue(ContextID contextID) throws CSErrorException;

}
