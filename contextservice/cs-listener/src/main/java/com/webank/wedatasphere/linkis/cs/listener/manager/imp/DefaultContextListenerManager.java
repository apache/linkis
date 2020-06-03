package com.webank.wedatasphere.linkis.cs.listener.manager.imp;

import com.webank.wedatasphere.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.DefaultContextIDCallbackEngine;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.DefaultContextKeyCallbackEngine;
import com.webank.wedatasphere.linkis.cs.listener.manager.ListenerManager;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/21
 */
public class DefaultContextListenerManager implements ListenerManager {
    @Override
    public ContextAsyncListenerBus getContextAsyncListenerBus() {
        ContextAsyncListenerBus contextAsyncListenerBus = ContextAsyncListenerBus.getInstance();
        return contextAsyncListenerBus;
    }

    @Override
    public DefaultContextIDCallbackEngine getContextIDCallbackEngine() {
        DefaultContextIDCallbackEngine instanceIdCallbackEngine = DefaultContextIDCallbackEngine.getInstance();
        return instanceIdCallbackEngine;
    }

    @Override
    public DefaultContextKeyCallbackEngine getContextKeyCallbackEngine() {
        DefaultContextKeyCallbackEngine instanceKeyCallbackEngine = DefaultContextKeyCallbackEngine.getInstance();
        return instanceKeyCallbackEngine;
    }

    private static DefaultContextListenerManager singleDefaultContextListenerManager = null;

    private DefaultContextListenerManager() {
    }

    public static DefaultContextListenerManager getInstance() {
        if (singleDefaultContextListenerManager == null) {
            synchronized (DefaultContextListenerManager.class) {
                if (singleDefaultContextListenerManager == null) {
                    singleDefaultContextListenerManager = new DefaultContextListenerManager();
                }
            }
        }
        return singleDefaultContextListenerManager;
    }
}
