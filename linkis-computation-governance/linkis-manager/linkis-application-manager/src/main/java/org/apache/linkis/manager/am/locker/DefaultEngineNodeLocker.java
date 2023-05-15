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

package org.apache.linkis.manager.am.locker;

import org.apache.linkis.manager.common.entity.node.AMEngineNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.protocol.RequestEngineLock;
import org.apache.linkis.manager.common.protocol.RequestEngineUnlock;
import org.apache.linkis.manager.common.protocol.RequestManagerUnlock;
import org.apache.linkis.manager.common.protocol.engine.EngineLockType;
import org.apache.linkis.manager.service.common.pointer.NodePointerBuilder;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultEngineNodeLocker implements EngineNodeLocker {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineNodeLocker.class);

  @Autowired private NodePointerBuilder nodeBuilder;

  @Override
  public Optional<String> lockEngine(EngineNode engineNode, long timeout) {
    return nodeBuilder
        .buildEngineNodePointer(engineNode)
        .lockEngine(new RequestEngineLock(timeout, EngineLockType.Timed));
  }

  @Override
  public void releaseLock(EngineNode engineNode, String lock) {
    nodeBuilder.buildEngineNodePointer(engineNode).releaseLock(new RequestEngineUnlock(lock));
  }

  @Receiver
  public void releaseLock(RequestManagerUnlock requestManagerUnlock) {
    try {
      logger.info(
          String.format(
              "client%s Start to unlock engine %s",
              requestManagerUnlock.getClientInstance(), requestManagerUnlock.getEngineInstance()));
      AMEngineNode engineNode = new AMEngineNode();
      engineNode.setServiceInstance(requestManagerUnlock.getEngineInstance());
      releaseLock(engineNode, requestManagerUnlock.getLock());
      logger.info(
          String.format(
              "client%s Finished to unlock engine %s",
              requestManagerUnlock.getClientInstance(), requestManagerUnlock.getEngineInstance()));
    } catch (Exception e) {
      logger.error("release lock failed", e);
    }
  }
}
