package com.webank.wedatasphere.linkis.cs.listener.event.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.listener.event.ContextIDEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.enumeration.OperateType;

/**
 * @author peacewong
 * @date 2020/2/20 15:03
 */
public class DefaultContextIDEvent implements ContextIDEvent {

    private ContextID contextID;

    public OperateType getOperateType() {
        return operateType;
    }

    public void setOperateType(OperateType operateType) {
        this.operateType = operateType;
    }

    //TODO
    private OperateType operateType;

    @Override
    public ContextID getContextID() {
        return contextID;
    }

    public void setContextID(ContextID contextID) {
        this.contextID = contextID;
    }
}
