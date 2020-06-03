package com.webank.wedatasphere.linkis.cs.client.listener;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.common.listener.EventListener;

/**
 * created by cooperyang on 2020/2/11
 * Description:
 */
public interface ContextClientListener extends EventListener {
    void onContextCreated(Event event);
    void onContextUpdated(Event event);
    void onEvent(Event event);
}
