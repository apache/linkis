package com.webank.wedatasphere.linkis.cs.contextcache.cache.guava;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.webank.wedatasphere.linkis.cs.contextcache.cache.csid.ContextIDValue;
import com.webank.wedatasphere.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import com.webank.wedatasphere.linkis.cs.listener.manager.imp.DefaultContextListenerManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.webank.wedatasphere.linkis.cs.listener.event.enumeration.OperateType.ACCESS;

@Component
public class ContextIDRemoveListener implements RemovalListener<String, ContextIDValue> {

    private static final Logger logger = LoggerFactory.getLogger(ContextIDRemoveListener.class);


    ContextAsyncListenerBus listenerBus = DefaultContextListenerManager.getInstance().getContextAsyncListenerBus();


    @Override
    public void onRemoval(RemovalNotification<String, ContextIDValue> removalNotification) {
        ContextIDValue value = removalNotification.getValue();
        String contextIDStr = removalNotification.getKey();
        if (StringUtils.isBlank(contextIDStr) || null == value || null == value.getContextID() ){
            return;
        }
        logger.info("Start to remove ContextID({}) from cache", contextIDStr);
        DefaultContextIDEvent defaultContextIDEvent = new DefaultContextIDEvent();
        defaultContextIDEvent.setContextID(value.getContextKeyValueContext().getContextID());
        defaultContextIDEvent.setOperateType(ACCESS);
        listenerBus.post(defaultContextIDEvent);
        logger.info("Finished to remove ContextID({}) from cache", contextIDStr);
    }
}
