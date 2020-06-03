package com.webank.wedatasphere.linkis.cs.listener.event.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.listener.event.ContextKeyEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.enumeration.OperateType;

/**
 * @author peacewong
 * @date 2020/2/15 21:30
 */
public class DefaultContextKeyEvent implements ContextKeyEvent {

    private ContextID contextID;

    private ContextKeyValue contextKeyValue;

    private ContextKeyValue oldValue;


    private OperateType operateType;

    public ContextID getContextID() {
        return contextID;
    }

    public void setContextID(ContextID contextID) {
        this.contextID = contextID;
    }

    public ContextKeyValue getContextKeyValue() {
        return contextKeyValue;
    }

    public void setContextKeyValue(ContextKeyValue contextKeyValue) {
        this.contextKeyValue = contextKeyValue;
    }

    public OperateType getOperateType() {
        return operateType;
    }

    public void setOperateType(OperateType operateType) {
        this.operateType = operateType;
    }

    public ContextKeyValue getOldValue() {
        return oldValue;
    }

    public void setOldValue(ContextKeyValue oldValue) {
        this.oldValue = oldValue;
    }

}
