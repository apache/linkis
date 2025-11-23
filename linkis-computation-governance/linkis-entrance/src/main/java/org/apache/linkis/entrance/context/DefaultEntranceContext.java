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

package org.apache.linkis.entrance.context;

import org.apache.linkis.entrance.EntranceContext;
import org.apache.linkis.entrance.EntranceParser;
import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.apache.linkis.entrance.event.EntranceEvent;
import org.apache.linkis.entrance.event.EntranceEventListener;
import org.apache.linkis.entrance.event.EntranceEventListenerBus;
import org.apache.linkis.entrance.interceptor.EntranceInterceptor;
import org.apache.linkis.entrance.log.LogManager;
import org.apache.linkis.entrance.persistence.PersistenceManager;
import org.apache.linkis.scheduler.Scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(ServiceNameConsts.ENTRANCE_CONTEXT)
public class DefaultEntranceContext extends EntranceContext {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEntranceContext.class);

  @Autowired private EntranceParser entranceParser;

  @Autowired private PersistenceManager persistenceManager;

  @Autowired private LogManager logManager;

  @Autowired private Scheduler scheduler;

  @Autowired
  @Qualifier(ServiceNameConsts.ENTRANCE_INTERCEPTOR)
  private EntranceInterceptor[] entranceInterceptors;

  @Autowired private EntranceEventListenerBus<EntranceEventListener, EntranceEvent> listenerBus;

  public DefaultEntranceContext(
      EntranceParser entranceParser,
      PersistenceManager persistenceManager,
      LogManager logManager,
      Scheduler scheduler,
      EntranceInterceptor[] entranceInterceptors,
      EntranceEventListenerBus<EntranceEventListener, EntranceEvent> listenerBus) {
    this.entranceParser = entranceParser;
    this.persistenceManager = persistenceManager;
    this.logManager = logManager;
    this.scheduler = scheduler;
    this.entranceInterceptors = entranceInterceptors;
    this.listenerBus = listenerBus;
  }

  public DefaultEntranceContext() {}

  @PostConstruct
  public void init() {
    entranceParser.setEntranceContext(this);
    logger.info("Finished init entranceParser from postConstruct end!");
    persistenceManager.setEntranceContext(this);
    logManager.setEntranceContext(this);
  }

  @Override
  public Scheduler getOrCreateScheduler() {
    return scheduler;
  }

  @Override
  public EntranceParser getOrCreateEntranceParser() {
    return this.entranceParser;
  }

  @Override
  public EntranceInterceptor[] getOrCreateEntranceInterceptors() {
    return entranceInterceptors;
  }

  @Override
  public LogManager getOrCreateLogManager() {
    return logManager;
  }

  @Override
  public PersistenceManager getOrCreatePersistenceManager() {
    return persistenceManager;
  }

  @Override
  public EntranceEventListenerBus<EntranceEventListener, EntranceEvent>
      getOrCreateEventListenerBus() {
    return this.listenerBus;
  }
}
