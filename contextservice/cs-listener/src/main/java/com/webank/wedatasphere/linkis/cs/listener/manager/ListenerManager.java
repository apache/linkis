package com.webank.wedatasphere.linkis.cs.listener.manager;

import com.webank.wedatasphere.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.DefaultContextIDCallbackEngine;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.DefaultContextKeyCallbackEngine;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/21
 */
public interface ListenerManager {
     public ContextAsyncListenerBus getContextAsyncListenerBus(); //单例
     public DefaultContextIDCallbackEngine getContextIDCallbackEngine(); //单例
     public DefaultContextKeyCallbackEngine getContextKeyCallbackEngine(); //单例
}
