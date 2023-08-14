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

package org.apache.linkis.manager.am.service;

import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.common.protocol.em.GetEMEnginesRequest;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest;
import org.apache.linkis.manager.label.entity.Label;

import java.util.List;

public interface EMEngineService {

  List<EngineNode> listEngines(GetEMEnginesRequest getEMEnginesRequest);

  EngineNode createEngine(EngineConnBuildRequest engineBuildRequest, EMNode emNode);

  void stopEngine(EngineNode engineNode, EMNode emNode);

  EMNode[] getEMNodes(ScoreServiceInstance[] scoreServiceInstances);

  EMNode[] getEMNodes(List<Label<?>> labels);
}
