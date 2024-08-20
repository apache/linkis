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

package org.apache.linkis.cs.contextcache.cache;

import org.apache.linkis.common.listener.Event;
import org.apache.linkis.cs.listener.CSIDListener;
import org.apache.linkis.cs.listener.ListenerBus.ContextAsyncListenerBus;
import org.apache.linkis.cs.listener.event.ContextIDEvent;
import org.apache.linkis.cs.listener.manager.imp.DefaultContextListenerManager;
import org.apache.linkis.cs.persistence.entity.PersistenceContextID;
import org.apache.linkis.cs.persistence.persistence.ContextIDPersistence;
import org.apache.linkis.cs.persistence.persistence.ContextMapPersistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultContextAddListener implements CSIDListener {

  private static final Logger logger = LoggerFactory.getLogger(DefaultContextAddListener.class);

  private ContextAsyncListenerBus listenerBus =
      DefaultContextListenerManager.getInstance().getContextAsyncListenerBus();

  @Autowired private ContextIDPersistence contextIDPersistence;

  @Autowired private ContextMapPersistence contextMapPersistence;

  @PostConstruct
  private void init() {
    listenerBus.addListener(this);
  }

  @Override
  public void onEventError(Event event, Throwable t) {}

  @Override
  public void onCSIDAccess(ContextIDEvent contextIDEvent) {}

  @Override
  public void onCSIDADD(ContextIDEvent contextIDEvent) {
    try {
      PersistenceContextID pContextID = new PersistenceContextID();
      pContextID.setContextId(contextIDEvent.getContextID().getContextId());
      pContextID.setAccessTime(new Date());
      contextIDPersistence.updateContextID(pContextID);
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Updated accesstime for contextID : {}", contextIDEvent.getContextID().getContextId());
      }
    } catch (Exception e) {
      logger.error("Update context accessTime failed, {}", e.getMessage(), e);
    }
  }

  @Override
  public void onCSIDRemoved(ContextIDEvent contextIDEvent) {}

  @Override
  public void onEvent(Event event) {}
}
