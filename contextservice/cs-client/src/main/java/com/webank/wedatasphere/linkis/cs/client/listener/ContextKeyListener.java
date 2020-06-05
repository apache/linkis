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
package com.webank.wedatasphere.linkis.cs.client.listener;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.client.Context;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * created by cooperyang on 2020/2/18
 * Description:一个微服务对contextKey的监听器
 */
public abstract class ContextKeyListener implements ContextClientListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextKeyListener.class);

    private ContextKey contextKey;

    private Context context;

    public ContextKeyListener(){

    }

    public ContextKeyListener(ContextKey contextKey){
        this.contextKey =  contextKey;
    }

    public ContextKey getContextKey() {
        return contextKey;
    }

    public void setContextKey(ContextKey contextKey) {
        this.contextKey = contextKey;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onContextUpdated(Event event) {
        if (event instanceof DefaultContextKeyEvent){
            context.setLocal(((DefaultContextKeyEvent) event).getContextKeyValue());
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof DefaultContextKeyEvent){
            DefaultContextKeyEvent defaultContextKeyEvent = (DefaultContextKeyEvent)event;
            if (defaultContextKeyEvent.getContextKeyValue().getContextKey().equals(contextKey)){
                switch(defaultContextKeyEvent.getOperateType()){
                    case UPDATE:onContextUpdated(defaultContextKeyEvent);break;
                    case CREATE:onContextCreated(defaultContextKeyEvent);break;
                    default:break;
                }
            }
        }
    }
}
