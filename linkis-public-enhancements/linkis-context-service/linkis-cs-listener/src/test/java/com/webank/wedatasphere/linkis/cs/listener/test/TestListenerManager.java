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
package com.webank.wedatasphere.linkis.cs.listener.test;

import com.webank.wedatasphere.linkis.common.listener.Event;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.ListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.ContextKeyValueBean;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.DefaultContextIDCallbackEngine;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.DefaultContextKeyCallbackEngine;
import com.webank.wedatasphere.linkis.cs.listener.event.enumeration.OperateType;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import com.webank.wedatasphere.linkis.cs.listener.event.impl.DefaultContextKeyEvent;
import com.webank.wedatasphere.linkis.cs.listener.manager.ListenerManager;
import com.webank.wedatasphere.linkis.cs.listener.manager.imp.DefaultContextListenerManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/22
 */
public class TestListenerManager {
    @Test
    public void testGetContextAsyncListenerBus() {
        DefaultContextListenerManager defaultContextListenerManager = DefaultContextListenerManager.getInstance();

        ContextAsyncListenerBus contextAsyncListenerBus = defaultContextListenerManager.getContextAsyncListenerBus();

        DefaultContextIDCallbackEngine contextIDCallbackEngine = defaultContextListenerManager.getContextIDCallbackEngine();

        DefaultContextKeyCallbackEngine contextKeyCallbackEngine = defaultContextListenerManager.getContextKeyCallbackEngine();
        //client1的contextID
        TestContextID testContextID1 = new TestContextID();
        testContextID1.setContextId("18392881376");

        //client2的contextID
        TestContextID testContextID2 = new TestContextID();
        testContextID2.setContextId("13431335441");

        List<ContextKey> csKeys1 = new ArrayList<>();
        TestContextKey testContextKey1 = new TestContextKey();
        testContextKey1.setKey("key1");
        TestContextKey testContextKey2 = new TestContextKey();
        testContextKey2.setKey("key2");
        csKeys1.add(testContextKey1);
        csKeys1.add(testContextKey2);

        List<ContextKey> csKeys2 = new ArrayList<>();
        TestContextKey testContextKey3 = new TestContextKey();
        testContextKey3.setKey("key3");
        TestContextKey testContextKey4 = new TestContextKey();
        testContextKey4.setKey("key4");
        csKeys2.add(testContextKey3);
        csKeys2.add(testContextKey4);


        ListenerDomain ListenerDomain1;

        ListenerDomain ListenerDomain2;

        ListenerDomain ListenerDomain3;




        DefaultContextKeyEvent defaultContextKeyEvent = new DefaultContextKeyEvent();
        defaultContextKeyEvent.setContextID(testContextID1);
        defaultContextKeyEvent.setOperateType(OperateType.UPDATE);
        TestContextKeyValue testContextKeyValue = new TestContextKeyValue();
        testContextKeyValue.setContextKey(testContextKey1);
        TestContextValue testContextValue = new TestContextValue();
        testContextValue.setValue("chaogefeng");
        testContextKeyValue.setContextValue(testContextValue);
        defaultContextKeyEvent.setContextKeyValue(testContextKeyValue);
        contextAsyncListenerBus.doPostEvent(contextKeyCallbackEngine, defaultContextKeyEvent);
        ArrayList<ContextKeyValueBean> clientSource2ListenerCallback = contextKeyCallbackEngine.getListenerCallback("127.0.0.1:8888");
        System.out.println("----------------------------------------------------------------------");
        for (ContextKeyValueBean contextKeyValueBean : clientSource2ListenerCallback) {
            System.out.println("返回的bean里面对应的contexID: " + contextKeyValueBean.getCsID().getContextId());
            System.out.println("返回的bean里面对应的cskeys: " + contextKeyValueBean.getCsKey().getKey());
            if (contextKeyValueBean.getCsValue() != null) {
                System.out.println("返回的bean里面对应的value: " + contextKeyValueBean.getCsValue().getValue());
            }
        }
    }

}
