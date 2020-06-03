package com.webank.wedatasphere.linkis.cs.listener.event;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;

/**
 * @author peacewong
 * @date 2020/2/20 15:03
 */
public interface ContextIDEvent extends Event {

    ContextID getContextID();
}
