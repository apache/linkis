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

package org.apache.linkis.manager.am.service.engine;

import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.protocol.engine.EngineConnReleaseRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineSuicideRequest;
import org.apache.linkis.rpc.Sender;

import java.util.Map;

public interface EngineStopService {

  public static void askEngineToSuicide(EngineSuicideRequest engineSuicideRequest) {
    if (engineSuicideRequest.getServiceInstance() == null) return;
    Sender.getSender(engineSuicideRequest.getServiceInstance()).send(engineSuicideRequest);
  }

  void stopEngine(EngineStopRequest engineStopRequest, Sender sender);

  /**
   * Asyn stop all unlock ec node of a specified ecm
   *
   * @param ecmInstance the specified ecm.
   * @param operatorName the username who request this operation
   * @return Map
   */
  Map<String, Object> stopUnlockEngineByECM(String ecmInstance, String operatorName);

  void asyncStopEngine(EngineStopRequest engineStopRequest);

  /**
   * Async stop a ec node with change node status
   *
   * @param engineStopRequest
   * @return
   */
  void asyncStopEngineWithUpdateMetrics(EngineStopRequest engineStopRequest);

  void engineSuicide(EngineSuicideRequest engineSuicideRequest, Sender sender);

  void dealEngineRelease(EngineConnReleaseRequest engineConnReleaseRequest, Sender sender);

  void engineConnInfoClear(EngineNode ecNode);
}
