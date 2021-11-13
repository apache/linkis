/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cs.contextcache.cache.guava;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.linkis.cs.contextcache.cache.csid.ContextIDValue;
import org.apache.linkis.cs.contextcache.cache.csid.impl.ContextIDValueImpl;
import org.apache.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import org.apache.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import org.apache.linkis.cs.listener.manager.imp.DefaultContextListenerManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.apache.linkis.cs.listener.event.enumeration.OperateType.DELETE;

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
        listenerBus.removeListener((ContextIDValueImpl)value);
        logger.info("Start to remove ContextID({}) from cache", contextIDStr);
        DefaultContextIDEvent defaultContextIDEvent = new DefaultContextIDEvent();
        defaultContextIDEvent.setContextID(value.getContextKeyValueContext().getContextID());
        defaultContextIDEvent.setOperateType(DELETE);
        listenerBus.post(defaultContextIDEvent);
        logger.info("Finished to remove ContextID({}) from cache", contextIDStr);
    }
}
