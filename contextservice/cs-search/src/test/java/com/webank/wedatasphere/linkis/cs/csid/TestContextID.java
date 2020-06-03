package com.webank.wedatasphere.linkis.cs.csid;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;

/**
 * @author peacewong
 * @date 2020/2/13 20:41
 */
public class TestContextID implements ContextID {

    String contextID;

    @Override
    public String getContextId() {
        return contextID;
    }

    @Override
    public void setContextId(String contextId) {
        this.contextID = contextId;
    }
}
