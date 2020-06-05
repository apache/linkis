package com.webank.wedatasphere.linkis.cs.client.listener;

import com.webank.wedatasphere.linkis.common.listener.Event;

/**
 * created by cooperyang on 2020/2/11
 * Description:
 * Manager的作用是为了方便用户将的
 */
public class ContextClientListenerManager {

    private static ContextClientListenerBus<ContextClientListener, Event> contextClientListenerBus;

    public static ContextClientListenerBus<ContextClientListener, Event> getContextClientListenerBus(){
        if (contextClientListenerBus == null){
            synchronized (ContextClientListenerManager.class){
                if (contextClientListenerBus == null){
                    contextClientListenerBus = new ContextClientListenerBus<ContextClientListener, Event>();
                }
            }
        }
        return contextClientListenerBus;
    }

}
