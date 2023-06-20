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

import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest;
import org.apache.linkis.manager.common.protocol.em.ECMOperateResponse;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopResponse;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest;
import org.apache.linkis.manager.service.common.pointer.EMNodPointer;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEMNodPointer extends AbstractNodePointer implements EMNodPointer {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEMNodPointer.class);

  private Node node;

  public DefaultEMNodPointer(Node node) {
    this.node = node;
  }

  /**
   * 与该远程指针关联的node信息
   *
   * @return
   */
  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public EngineNode createEngine(EngineConnLaunchRequest engineConnLaunchRequest) {
    logger.info("Start to createEngine ask em " + getNode().getServiceInstance());
    Object result = getSender().ask(engineConnLaunchRequest);
    if (result instanceof EngineNode) {
      EngineNode engineNode = (EngineNode) result;
      logger.info(
          "Succeed to createEngine ask em "
              + getNode().getServiceInstance()
              + ", engineNode "
              + engineNode);
      return engineNode;
    } else {
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          "Failed to createEngine ask em " + getNode().getServiceInstance() + "result: " + result);
    }
  }

  @Override
  public void stopEngine(EngineStopRequest engineStopRequest) {
    Object result = getSender().ask(engineStopRequest);
    if (result instanceof EngineStopResponse) {
      EngineStopResponse engineStopResponse = (EngineStopResponse) result;
      if (!engineStopResponse.getStopStatus()) {
        logger.info(
            "Kill engine : "
                + engineStopRequest.getServiceInstance().toString()
                + " failed, because "
                + engineStopResponse.getMsg()
                + " . Will ask engine to suicide.");
      } else {
        logger.info(
            "Succeed to kill engine " + engineStopRequest.getServiceInstance().toString() + ".");
      }
    } else {
      logger.warn(
          "Ask em : "
              + getNode().getServiceInstance().toString()
              + " to kill engine : "
              + engineStopRequest.getServiceInstance().toString()
              + " failed, response is : "
              + BDPJettyServerHelper.gson().toJson(result)
              + ".");
    }
  }

  @Override
  public ECMOperateResponse executeOperation(ECMOperateRequest ecmOperateRequest) {
    Object result = getSender().ask(ecmOperateRequest);
    if (result instanceof ECMOperateResponse) {
      return (ECMOperateResponse) result;
    } else {
      throw new AMErrorException(AMConstant.ENGINE_ERROR_CODE, "Failed to execute ECM operation.");
    }
  }
}
