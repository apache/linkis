package com.webank.wedatasphere.linkis.cs.listener;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.listener.event.ContextKeyEvent;

/**
 * @author peacewong
 * @date 2020/2/15 11:24
 */
public interface CSKeyListener extends ContextAsyncEventListener {
    @Override
    void onEvent(Event event);
    void onCSKeyUpdate(ContextKeyEvent contextKeyEvent);
    void onCSKeyAccess(ContextKeyEvent contextKeyEvent);

}
