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

package org.apache.linkis.manager.am.service.em;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.manager.am.manager.EMNodeManager;
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.RMNode;
import org.apache.linkis.manager.common.protocol.em.GetEMInfoRequest;
import org.apache.linkis.manager.common.protocol.node.NodeHealthyRequest;
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.rm.ResourceInfo;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.manager.service.common.metrics.MetricsConverter;
import org.apache.linkis.manager.service.common.pointer.NodePointerBuilder;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEMInfoService implements EMInfoService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEMInfoService.class);

  @Autowired private EMNodeManager emNodeManager;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private ResourceManager resourceManager;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private NodePointerBuilder nodePointerBuilder;

  @Autowired private MetricsConverter defaultMetricsConverter;

  @Receiver
  @Override
  public EMNode getEM(GetEMInfoRequest getEMInfoRequest) {
    EMNode node = emNodeManager.getEM(getEMInfoRequest.getEm());
    if (node == null) {
      return new AMEMNode();
    } else {
      return node;
    }
  }

  @Override
  public EMNode[] getAllEM() {
    AliasServiceInstanceLabel label = new AliasServiceInstanceLabel();
    label.setAlias(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue());
    List<ServiceInstance> instances = nodeLabelService.getNodesByLabel(label);

    ResourceInfo resourceInfo =
        resourceManager.getResourceInfo(
            new ArrayList<>(nodeLabelService.getNodesByLabel(label))
                .toArray(new ServiceInstance[0]));

    Map<ServiceInstance, RMNode> resourceInfoMap =
        resourceInfo.getResourceInfo().stream()
            .collect(
                Collectors.toMap(
                    r -> r.getServiceInstance(), r -> r, (existingValue, newValue) -> newValue));

    return instances.stream()
        .map(emNodeManager::getEM)
        .filter(Objects::nonNull)
        .map(
            node -> {
              node.setLabels(nodeLabelService.getNodeLabels(node.getServiceInstance()));
              if (Objects.nonNull(resourceInfoMap.get(node.getServiceInstance()))) {
                RMNode rmNode = resourceInfoMap.get(node.getServiceInstance());
                node.setNodeResource(rmNode.getNodeResource());
              }
              return node;
            })
        .toArray(EMNode[]::new);
  }

  @Override
  public EMNode getEM(ServiceInstance serviceInstance) {
    if (serviceInstance == null) {
      return null;
    } else {
      return emNodeManager.getEM(serviceInstance);
    }
  }

  @Override
  public void updateEMInfo(ServiceInstance serviceInstance, NodeHealthyInfo nodeHealthyInfo) {
    EMNode node = emNodeManager.getEM(serviceInstance);
    if (node != null) {
      NodeMetrics metrics = nodeMetricManagerPersistence.getNodeMetrics(node);

      if (null != metrics && null != nodeHealthyInfo) {
        NodeHealthyInfo oldHealthyInfo = defaultMetricsConverter.parseHealthyInfo(metrics);
        if (!nodeHealthyInfo.getNodeHealthy().equals(oldHealthyInfo.getNodeHealthy())) {
          metrics.setHealthy(defaultMetricsConverter.convertHealthyInfo(nodeHealthyInfo));
          nodeMetricManagerPersistence.addOrupdateNodeMetrics(metrics);

          NodeHealthyRequest nodeHealthyRequest = new NodeHealthyRequest();
          nodeHealthyRequest.setNodeHealthy(nodeHealthyInfo.getNodeHealthy());
          nodePointerBuilder.buildEMNodePointer(node).updateNodeHealthyRequest(nodeHealthyRequest);
          logger.info(
              "success to update healthy metric of instance: {}, {}",
              serviceInstance.getInstance(),
              nodeHealthyInfo.getNodeHealthy());
        }
      }
    }
  }
}
