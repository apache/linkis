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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.entity.NodeExistStatus;
import org.apache.linkis.governance.common.protocol.engineconn.RequestEngineStatusBatch;
import org.apache.linkis.governance.common.protocol.engineconn.ResponseEngineStatusBatch;
import org.apache.linkis.governance.common.utils.GovernanceConstant;
import org.apache.linkis.manager.am.manager.EMNodeManager;
import org.apache.linkis.manager.am.manager.EngineNodeManager;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.RMNode;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.rm.ResourceInfo;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineInfoService extends AbstractEngineService implements EngineInfoService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineInfoService.class);

  @Autowired private EngineNodeManager engineNodeManager;

  @Autowired private EMNodeManager emNodeManager;

  @Autowired private ResourceManager resourceManager;

  @Autowired private NodeLabelService labelService;

  private final LabelBuilderFactory labelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Override
  public List<EngineNode> listUserEngines(String user) {
    List<EngineNode> nodes = engineNodeManager.listEngines(user);
    return getEngineNodes(nodes);
  }

  private List<EngineNode> getEngineNodes(List<EngineNode> nodes) {
    List<ServiceInstance> serviceInstances =
        nodes.stream().map(EngineNode::getServiceInstance).collect(Collectors.toList());

    ResourceInfo resourceInfo1 =
        resourceManager.getResourceInfo(serviceInstances.toArray(new ServiceInstance[0]));
    List<RMNode> rmNodes = resourceInfo1.getResourceInfo();

    Map<String, RMNode> resourceInfoMap =
        rmNodes.stream()
            .collect(
                Collectors.toMap(
                    entry -> entry.getServiceInstance().toString(),
                    entry -> entry,
                    (existingValue, newValue) -> newValue));
    nodes.forEach(
        node -> {
          RMNode rmNode = resourceInfoMap.get(node.getServiceInstance().toString());
          if (Objects.nonNull(rmNode)) {
            node.setNodeResource(rmNode.getNodeResource());
          }
          node.setLabels(labelService.getNodeLabels(node.getServiceInstance()));
        });
    return nodes;
  }

  @Override
  public List<EngineNode> listEMEngines(EMNode em) {
    List<EngineNode> nodes = emNodeManager.listEngines(em);
    return getEngineNodes(nodes);
  }

  @Receiver
  @Override
  public ResponseEngineStatusBatch dealBatchGetEngineStatus(RequestEngineStatusBatch request) {
    Map<ServiceInstance, NodeExistStatus> map = new HashMap<>();
    if (request.engineList().size() > GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT()) {
      return new ResponseEngineStatusBatch(
          map,
          String.format(
              "Engines size %d in request cannot exceed the batch limit of %d",
              request.engineList().size(), GovernanceConstant.REQUEST_ENGINE_STATUS_BATCH_LIMIT()));
    }
    request
        .engineList()
        .forEach(
            e -> {
              EngineNode engineNode = null;
              try {
                engineNode = engineNodeManager.getEngineNode(e);
                if (engineNode == null) {
                  map.put(e, NodeExistStatus.UnExist);
                } else {
                  map.put(e, NodeExistStatus.Exist);
                }
              } catch (Throwable t) {
                logger.info(String.format("Get engineNode of %s error. ", e), t);
                map.put(e, NodeExistStatus.Unknown);
              }
            });
    return new ResponseEngineStatusBatch(map, null);
  }

  @Override
  public void modifyEngineLabel(ServiceInstance instance, Map<String, String> labelKeyValue) {
    labelKeyValue.forEach(
        (key, value) -> {
          labelService.updateLabelToNode(instance, labelBuilderFactory.createLabel(key, value));
          logger.info(
              String.format(
                  "instance:%s success to update label, labelKey:%s labelValue:%s",
                  instance, key, value));
        });
  }
}
