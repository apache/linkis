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

package org.apache.linkis.manager.am.pointer;

import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.protocol.RequestEngineLock;
import org.apache.linkis.manager.common.protocol.RequestEngineUnlock;
import org.apache.linkis.manager.common.protocol.ResponseEngineLock;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineOperateResponse;
import org.apache.linkis.manager.service.common.pointer.EngineNodePointer;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEngineNodPointer extends AbstractNodePointer implements EngineNodePointer {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineNodPointer.class);

  private Node node;

  public DefaultEngineNodPointer(Node node) {
    this.node = node;
  }

  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public Optional<String> lockEngine(RequestEngineLock requestEngineLock) {
    Object result = getSender().ask(requestEngineLock);
    if (result instanceof ResponseEngineLock) {
      ResponseEngineLock responseEngineLock = (ResponseEngineLock) result;
      if (responseEngineLock.getLockStatus()) {
        return Optional.of(responseEngineLock.getLock());
      } else {
        logger.info(
            "Failed to get locker,"
                + node.getServiceInstance()
                + ": "
                + responseEngineLock.getMsg());
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void releaseLock(RequestEngineUnlock requestEngineUnlock) {
    getSender().send(requestEngineUnlock);
  }

  @Override
  public EngineOperateResponse executeOperation(EngineOperateRequest engineOperateRequest) {
    Object result = getSender().ask(engineOperateRequest);
    if (result instanceof EngineOperateResponse) {
      return (EngineOperateResponse) result;
    } else {
      throw new AMErrorException(-1, "Illegal response of operation.");
    }
  }
}
