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

package org.apache.linkis.manager.am.manager;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest;
import org.apache.linkis.manager.common.protocol.em.ECMOperateResponse;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest;

import java.util.List;

public interface EMNodeManager {

  void emRegister(EMNode emNode);

  List listEngines(EMNode emNode);

  List listUserEngines(EMNode emNode, String user);

  List listUserNodes(String user);

  /**
   * Get detailed em information from the persistence
   *
   * @param scoreServiceInstances
   * @return
   */
  EMNode[] getEMNodes(ScoreServiceInstance[] scoreServiceInstances);

  EMNode getEM(ServiceInstance serviceInstance);

  void stopEM(EMNode emNode);

  void deleteEM(EMNode emNode);

  void pauseEM(ServiceInstance serviceInstance);

  /**
   * 1. request engineManager to launch engine 2. persist engine info
   *
   * @param engineConnLaunchRequest engine launch request
   * @param emNode ecm node
   * @return engine node
   */
  EngineNode createEngine(EngineConnLaunchRequest engineConnLaunchRequest, EMNode emNode);

  void stopEngine(EngineStopRequest engineStopRequest, EMNode emNode);

  void addEMNodeInstance(EMNode emNode);

  void initEMNodeMetrics(EMNode emNode);

  ECMOperateResponse executeOperation(EMNode ecmNode, ECMOperateRequest request);
}
