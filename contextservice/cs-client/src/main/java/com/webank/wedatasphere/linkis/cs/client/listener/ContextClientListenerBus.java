package com.webank.wedatasphere.linkis.cs.client.listener;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.common.listener.ListenerBus;
import com.webank.wedatasphere.linkis.common.listener.ListenerEventBus;

/**
 * created by cooperyang on 2020/2/11
 * Description:
 */
public class ContextClientListenerBus<L extends ContextClientListener, E extends Event> extends ListenerEventBus<L, E> {


    private static final String NAME = "ContextClientListenerBus";

    private static final int CAPACITY = 10;

    private static final int THREAD_SIZE = 20;

    private static final int MAX_FREE_TIME = 5000;

    public ContextClientListenerBus(){
        super(CAPACITY, NAME,THREAD_SIZE,MAX_FREE_TIME);
    }



    @Override
    public void doPostEvent(L listener, E event) {
        listener.onEvent(event);
    }
}
