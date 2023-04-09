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

package org.apache.linkis.manager.am.service.impl;

import org.apache.linkis.manager.am.event.message.EngineConnPidCallbackEvent;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNodeEntity;
import org.apache.linkis.manager.persistence.impl.DefaultNodeManagerPersistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineConnManageService {
  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnManageService.class);

  @Autowired private DefaultNodeManagerPersistence defaultNodeManagerPersistence;

  @EventListener
  public void onUpdatePid(EngineConnPidCallbackEvent event) {
    PersistenceNodeEntity node =
        (PersistenceNodeEntity)
            defaultNodeManagerPersistence.getNode(event.getProtocol().serviceInstance());
    if (Objects.nonNull(node)) {
      // update procees Id of service instance
      node.setProcessId(event.getProtocol().pid());
      defaultNodeManagerPersistence.updateNodeInstance(node);
    }
  }
}
