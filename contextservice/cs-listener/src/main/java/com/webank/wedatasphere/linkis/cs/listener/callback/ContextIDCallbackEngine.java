package com.webank.wedatasphere.linkis.cs.listener.callback;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ListenerDomain;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/20
 *
 */
public interface ContextIDCallbackEngine extends CallbackEngine {
    void registerClient(ListenerDomain listenerDomain);
}
