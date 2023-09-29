/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.listener.callback.imp;

import org.apache.linkis.common.listener.Event;
import org.apache.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import org.apache.linkis.cs.common.entity.listener.ListenerDomain;
import org.apache.linkis.cs.common.entity.source.ContextID;
import org.apache.linkis.cs.listener.CSIDListener;
import org.apache.linkis.cs.listener.callback.ContextIDCallbackEngine;
import org.apache.linkis.cs.listener.event.ContextIDEvent;
import org.apache.linkis.cs.listener.event.impl.DefaultContextIDEvent;
import org.apache.linkis.cs.listener.manager.imp.DefaultContextListenerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultContextIDCallbackEngine implements CSIDListener, ContextIDCallbackEngine {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultContextIDCallbackEngine.class);
  private HashMultimap<String, ContextID> registerCSIDcsClients =
      HashMultimap.create(); // key为clientSource的instance值

  private List<ContextID> removedContextIDS = new ArrayList<>();

  // 不关心返回ContextKeyValueBean里面的cskeys和 value
  @Override
  public ArrayList<ContextKeyValueBean> getListenerCallback(String source) {
    Set<ContextID> ContextIDSets = registerCSIDcsClients.get(source);
    ArrayList<ContextKeyValueBean> contextKeyValueBeans = new ArrayList<>();
    for (ContextID contextID : removedContextIDS) {
      if (ContextIDSets.contains(contextID)) {
        ContextKeyValueBean contextKeyValueBean = new ContextKeyValueBean();
        contextKeyValueBean.setCsID(contextID);
        contextKeyValueBeans.add(contextKeyValueBean);
      }
    }
    return contextKeyValueBeans;
  }

  // ContextIDCallbackEngine 只监听ContextID ，对csKeys无感，可为空
  //    @Override
  //    public void registerClient(ContextID csId, List<ContextKey> csKeys, ClientSource csClient)
  // {
  //
  //        String instance = csClient.getInstance();
  //        if (instance != null && csId != null) {
  //            synchronized (registerCSIDcsClients) {
  //                registerCSIDcsClients.put(instance, csId);
  //            }
  //        }
  //    }

  @Override
  public void registerClient(ListenerDomain listenerDomain) {
    if (listenerDomain != null && listenerDomain instanceof CommonContextIDListenerDomain) {
      CommonContextIDListenerDomain commonContextIDListenerDomain =
          (CommonContextIDListenerDomain) listenerDomain;
      String source = commonContextIDListenerDomain.getSource();
      ContextID contextID = commonContextIDListenerDomain.getContextID();
      if (source != null && contextID != null) {
        synchronized (registerCSIDcsClients) {
          registerCSIDcsClients.put(source, contextID);
        }
      }
    }
  }

  @Override
  public void onEvent(Event event) {
    DefaultContextIDEvent defaultContextIDEvent = null;
    if (event != null && event instanceof DefaultContextIDEvent) {
      defaultContextIDEvent = (DefaultContextIDEvent) event;
    }
    if (null == defaultContextIDEvent) {
      logger.warn("defaultContextIDEvent event 为空");
      return;
    }
    switch (defaultContextIDEvent.getOperateType()) {
        // ADD, UPDATE, DELETE, REMOVEALL, ACCESS
      case REMOVEALL:
        onCSIDRemoved(defaultContextIDEvent);
        break;
      case ADD:
        onCSIDADD(defaultContextIDEvent);
        break;
      case ACCESS:
        onCSIDAccess(defaultContextIDEvent);
        break;
      case UPDATE:
        break;
      case DELETE:
        break;
      default:
        logger.info(
            "check defaultContextIDEvent event operate type(检查defaultContextIDEvent event操作类型)");
    }
  }

  @Override
  public void onCSIDAccess(ContextIDEvent contextIDEvent) {}

  @Override
  public void onCSIDADD(ContextIDEvent contextIDEvent) {}

  @Override
  public void onCSIDRemoved(ContextIDEvent contextIDEvent) {

    DefaultContextIDEvent defaultContextIDEvent = null;
    if (contextIDEvent != null && contextIDEvent instanceof DefaultContextIDEvent) {
      defaultContextIDEvent = (DefaultContextIDEvent) contextIDEvent;
    }
    if (null == defaultContextIDEvent) {
      return;
    }
    synchronized (removedContextIDS) {
      removedContextIDS.add(defaultContextIDEvent.getContextID());
    }
  }

  @Override
  public void onEventError(Event event, Throwable t) {}

  private static DefaultContextIDCallbackEngine singleDefaultContextIDCallbackEngine = null;

  private DefaultContextIDCallbackEngine() {}

  public static DefaultContextIDCallbackEngine getInstance() {
    if (singleDefaultContextIDCallbackEngine == null) {
      synchronized (DefaultContextIDCallbackEngine.class) {
        if (singleDefaultContextIDCallbackEngine == null) {
          singleDefaultContextIDCallbackEngine = new DefaultContextIDCallbackEngine();
          DefaultContextListenerManager instanceContextListenerManager =
              DefaultContextListenerManager.getInstance();
          instanceContextListenerManager
              .getContextAsyncListenerBus()
              .addListener(singleDefaultContextIDCallbackEngine);
          logger.info("add listerner singleDefaultContextIDCallbackEngine success");
        }
      }
    }
    return singleDefaultContextIDCallbackEngine;
  }
}
