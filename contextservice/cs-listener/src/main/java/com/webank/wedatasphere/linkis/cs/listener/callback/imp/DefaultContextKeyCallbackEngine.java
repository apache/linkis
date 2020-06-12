/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.cs.listener.callback.imp;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multiset;
import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.CommonContextKeyListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.ListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.listener.CSKeyListener;
import com.webank.wedatasphere.linkis.cs.listener.callback.ContextKeyCallbackEngine;
import com.webank.wedatasphere.linkis.cs.listener.event.ContextKeyEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextKeyEvent;
import com.webank.wedatasphere.linkis.cs.listener.manager.imp.DefaultContextListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/20
 */
public class DefaultContextKeyCallbackEngine implements CSKeyListener, ContextKeyCallbackEngine {
    private static final Logger logger = LoggerFactory.getLogger(DefaultContextKeyCallbackEngine.class);

    private HashMultimap<String, ContextID> registerCSIDcsClients = HashMultimap.create();//key为clientSource的instance值

    private HashMultimap<String, ContextKeyValueBean> registerCSIDcsKeyValues = HashMultimap.create();//key为 contextId 的ID值

    //注册csClient及其监听的csKeys
    @Override
    public void registerClient(ListenerDomain listenerDomain) {
        if (listenerDomain != null && listenerDomain instanceof CommonContextKeyListenerDomain) {
            CommonContextKeyListenerDomain commonContextKeyListenerDomain = (CommonContextKeyListenerDomain) listenerDomain;
            String source = commonContextKeyListenerDomain.getSource();
            ContextID contextID = commonContextKeyListenerDomain.getContextID();
            ContextKey contextKey = commonContextKeyListenerDomain.getContextKey();
            if (source != null && contextID != null) {
                synchronized (registerCSIDcsClients) {
                    logger.info("要注册的csClient和contextId: " + source + ":" + contextID);
                    registerCSIDcsClients.put(source, contextID);
                }
            }
            //针对cskey生成一个bean，cskey对应的value值目前为空
            if (contextKey != null) {
                ContextKeyValueBean contextKeyValueBean = new ContextKeyValueBean();
                contextKeyValueBean.setCsKey(contextKey);
                contextKeyValueBean.setCsID(contextID);
                synchronized (registerCSIDcsKeyValues) {
                    logger.info("要注册的contextId: " + contextID.getContextId());
                    registerCSIDcsKeyValues.put(contextID.getContextId(), contextKeyValueBean);
                }
            }
        }
    }

    //通过 source 拿到 ContextID，遍历 ContextID，返回监听的 beans
    @Override
    public ArrayList<ContextKeyValueBean> getListenerCallback(String source) {
        ArrayList<ContextKeyValueBean> arrayContextKeyValueBeans = new ArrayList<>();
        Set<ContextID> contextIDS = registerCSIDcsClients.get(source);
        //返回所有的 ContextKeyValueBean
        if (contextIDS.size() > 0) {
            for (ContextID csId : contextIDS) {
                arrayContextKeyValueBeans.addAll(registerCSIDcsKeyValues.get(csId.getContextId()));
            }
        }
        return arrayContextKeyValueBeans;
    }

    @Override
    public void onEvent(Event event) {
        DefaultContextKeyEvent defaultContextKeyEvent = null;
        if (event != null && event instanceof DefaultContextKeyEvent) {
            defaultContextKeyEvent = (DefaultContextKeyEvent) event;
        }
        if (null == defaultContextKeyEvent) {
            logger.info("defaultContextKeyEvent event 为空");
            return;
        }
        logger.info("defaultContextKeyEvent 要更新事件的ID: " + defaultContextKeyEvent.getContextID().getContextId());
        logger.info("defaultContextKeyEvent 要更新事件的key: " + defaultContextKeyEvent.getContextKeyValue().getContextKey().getKey());
        logger.info("defaultContextKeyEvent 要更新的value" + defaultContextKeyEvent.getContextKeyValue().getContextValue().getValue());
        switch (defaultContextKeyEvent.getOperateType()) {
            case UPDATE:
                onCSKeyUpdate(defaultContextKeyEvent);
                break;
            case ACCESS:
                onCSKeyAccess(defaultContextKeyEvent);
                break;
            default:
                logger.info("检查defaultContextKeyEvent event操作类型");
        }
    }

    //更新 cskey 对应的 value 值
    @Override
    public void onCSKeyUpdate(ContextKeyEvent cskeyEvent) {
        DefaultContextKeyEvent defaultContextKeyEvent = null;
        if (cskeyEvent != null && cskeyEvent instanceof DefaultContextKeyEvent) {
            defaultContextKeyEvent = (DefaultContextKeyEvent) cskeyEvent;
        }
        if (null == defaultContextKeyEvent) {
            return;
        }

        synchronized (registerCSIDcsKeyValues) {
            //遍历所有csid,如果csid跟事件中的相同，则取出该csid所有的bean,更新所有bean中的csvalue.
            Set<ContextKeyValueBean> contextKeyValueBeans = registerCSIDcsKeyValues.get(defaultContextKeyEvent.getContextID().getContextId());
            for (ContextKeyValueBean contextKeyValueBean : contextKeyValueBeans) {
                if (contextKeyValueBean.getCsKey().getKey().equals(defaultContextKeyEvent.getContextKeyValue().getContextKey().getKey())) {
                    contextKeyValueBean.setCsValue(defaultContextKeyEvent.getContextKeyValue().getContextValue());
                }
            }
        }
    }


    //todo
    @Override
    public void onCSKeyAccess(ContextKeyEvent cskeyEvent) {

    }

    //todo
    @Override
    public void onEventError(Event event, Throwable t) {

    }

    private static DefaultContextKeyCallbackEngine singleDefaultContextKeyCallbackEngine = null;

    private DefaultContextKeyCallbackEngine() {
    }

    public static DefaultContextKeyCallbackEngine getInstance() {
        if (singleDefaultContextKeyCallbackEngine == null) {
            synchronized (DefaultContextKeyCallbackEngine.class) {
                if (singleDefaultContextKeyCallbackEngine == null) {
                    singleDefaultContextKeyCallbackEngine = new DefaultContextKeyCallbackEngine();
                    DefaultContextListenerManager instanceContextListenerManager = DefaultContextListenerManager.getInstance();
                    instanceContextListenerManager.getContextAsyncListenerBus().addListener(singleDefaultContextKeyCallbackEngine);
                    logger.info("add listerner singleDefaultContextKeyCallbackEngine success");
                }
            }
        }
        return singleDefaultContextKeyCallbackEngine;
    }

}
