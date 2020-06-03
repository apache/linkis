package com.webank.wedatasphere.linkis.cs.listener.ListenerBus;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.common.listener.ListenerEventBus;
import com.webank.wedatasphere.linkis.cs.listener.ContextAsyncEventListener;
import com.webank.wedatasphere.linkis.cs.listener.conf.ContextListenerConf;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/21
 */
public class ContextAsyncListenerBus<L extends ContextAsyncEventListener, E extends Event> extends ListenerEventBus<L, E> {


    private static final String NAME = "ContextAsyncListenerBus";

    public ContextAsyncListenerBus() {
        super(ContextListenerConf.WDS_CS_LISTENER_ASYN_QUEUE_CAPACITY, NAME, ContextListenerConf.WDS_CS_LISTENER_ASYN_CONSUMER_THREAD_MAX, ContextListenerConf.WDS_CS_LISTENER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX);
    }

    @Override
    public void doPostEvent(L listener, E event) {
        listener.onEvent(event);
    }


    private static ContextAsyncListenerBus contextAsyncListenerBus = null;

    public static ContextAsyncListenerBus getInstance() {
        if (contextAsyncListenerBus == null) {
            synchronized (ContextAsyncListenerBus.class) {
                if (contextAsyncListenerBus == null) {
                    contextAsyncListenerBus = new ContextAsyncListenerBus();
                    contextAsyncListenerBus.start();
                }
            }
        }
        return contextAsyncListenerBus;
    }
}
