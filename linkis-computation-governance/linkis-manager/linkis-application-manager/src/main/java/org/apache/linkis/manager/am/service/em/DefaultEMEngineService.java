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

import org.apache.linkis.engineplugin.server.service.EngineConnLaunchService;
import org.apache.linkis.governance.common.utils.ECPathUtils;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.manager.EMNodeManager;
import org.apache.linkis.manager.am.service.ECResourceInfoService;
import org.apache.linkis.manager.am.service.EMEngineService;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.node.*;
import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.protocol.em.*;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.utils.ManagerUtils;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest;
import org.apache.linkis.manager.label.entity.EngineNodeLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.service.LabelResourceService;
import org.apache.linkis.manager.service.common.label.LabelFilter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEMEngineService implements EMEngineService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEMEngineService.class);

  @Autowired private EMNodeManager emNodeManager;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private EngineConnLaunchService engineConnLaunchService;

  @Autowired private LabelFilter labelFilter;

  @Autowired private ECResourceInfoService ecResourceInfoService;

  @Autowired private LabelResourceService labelResourceService;

  @Override
  public List listEngines(GetEMEnginesRequest getEMEnginesRequest) {
    AMEMNode emNode = new AMEMNode();
    emNode.setServiceInstance(getEMEnginesRequest.getEm());
    return emNodeManager.listEngines(emNode);
  }

  @Override
  public EngineNode createEngine(EngineConnBuildRequest engineBuildRequest, EMNode emNode) {
    logger.info(
        "EM " + emNode.getServiceInstance() + " start to create Engine " + engineBuildRequest);
    EngineConnLaunchRequest engineConnLaunchRequest =
        engineConnLaunchService.createEngineConnLaunchRequest(engineBuildRequest);

    EngineNode engineNode = emNodeManager.createEngine(engineConnLaunchRequest, emNode);
    logger.info(
        "EM "
            + emNode.getServiceInstance()
            + " Finished to create Engine "
            + engineBuildRequest.ticketId());
    engineNode.setLabels(
        emNode.getLabels().stream()
            .filter(label -> label instanceof EngineNodeLabel)
            .collect(Collectors.toList()));
    engineNode.setEMNode(emNode);
    return engineNode;
  }

  @Override
  public void stopEngine(EngineNode engineNode, EMNode emNode) {
    if (emNode == null) {
      logger.error("The emNode of " + engineNode.getServiceInstance() + " is null");
      return;
    }
    logger.debug(
        "EM "
            + emNode.getServiceInstance()
            + " start to stop Engine "
            + engineNode.getServiceInstance());
    EngineStopRequest engineStopRequest = new EngineStopRequest();
    engineStopRequest.setServiceInstance(engineNode.getServiceInstance());

    engineStopRequest.setIdentifierType(engineNode.getMark());
    engineStopRequest.setIdentifier(engineNode.getIdentifier());

    ECResourceInfoRecord ecResourceInfo = null;
    if (StringUtils.isNotBlank(engineNode.getTicketId())) {
      ecResourceInfo = ecResourceInfoService.getECResourceInfoRecord(engineNode.getTicketId());
    } else {
      ecResourceInfo =
          ecResourceInfoService.getECResourceInfoRecordByInstance(
              engineNode.getServiceInstance().getInstance());
    }
    if (ecResourceInfo != null) {
      engineStopRequest.setEngineType(ecResourceInfo.getEngineType());
      engineStopRequest.setLogDirSuffix(ecResourceInfo.getLogDirSuffix());
    } else {
      if (CollectionUtils.isEmpty(engineNode.getLabels())) {
        // node labels is empty, engine already been stopped
        logger.info(
            "DefaultEMEngineService stopEngine node labels is empty, engine: {} have already been stopped.",
            engineStopRequest.getServiceInstance());
        return;
      }

      RMLabelContainer rMLabelContainer = labelResourceService.enrichLabels(engineNode.getLabels());

      PersistenceResource persistenceResource =
          labelResourceService.getPersistenceResource(rMLabelContainer.getEngineInstanceLabel());
      if (persistenceResource == null) {
        // persistenceResource is null, engine already been stopped
        logger.info(
            "DefaultEMEngineService stopEngine persistenceResource is null, engine: {} have already been stopped.",
            engineStopRequest.getServiceInstance());
        return;
      }

      engineStopRequest.setEngineType(rMLabelContainer.getEngineTypeLabel().getEngineType());
      engineStopRequest.setLogDirSuffix(
          ECPathUtils.getECLogDirSuffix(
              rMLabelContainer.getEngineTypeLabel(),
              rMLabelContainer.getUserCreatorLabel(),
              persistenceResource.getTicketId()));
    }

    emNodeManager.stopEngine(engineStopRequest, emNode);
    // engineNodeManager.deleteEngineNode(engineNode)
    logger.info(
        "EM "
            + emNode.getServiceInstance()
            + " finished to stop Engine "
            + engineNode.getServiceInstance());
  }

  @Override
  public EMNode[] getEMNodes(ScoreServiceInstance[] scoreServiceInstances) {
    return emNodeManager.getEMNodes(scoreServiceInstances);
  }

  @Override
  public EMNode[] getEMNodes(List<Label<?>> labels) {
    Map<ScoreServiceInstance, List<Label<?>>> instanceAndLabels =
        nodeLabelService.getScoredNodeMapsByLabels(labelFilter.choseEMLabel(labels));
    if (MapUtils.isEmpty(instanceAndLabels)) {
      throw new AMErrorException(AMConstant.EM_ERROR_CODE, "No corresponding EM");
    }
    Optional<Label<?>> emInstanceLabelOption =
        labels.stream().filter(label -> label instanceof EMInstanceLabel).findFirst();

    Map<ScoreServiceInstance, List<Label<?>>> filterInstanceAndLabel;
    if (emInstanceLabelOption.isPresent()) {
      EMInstanceLabel emInstanceLabel = (EMInstanceLabel) emInstanceLabelOption.get();
      logger.info(
          "use emInstanceLabel , will be route to {}", emInstanceLabel.getServiceInstance());
      if (!instanceAndLabels.containsKey(emInstanceLabel.getServiceInstance())) {
        throw new AMErrorException(
            AMConstant.EM_ERROR_CODE,
            String.format(
                "You specified em %s, but the corresponding EM does not exist in the Manager",
                emInstanceLabel.getServiceInstance()));
      }
      filterInstanceAndLabel =
          instanceAndLabels.entrySet().stream()
              .filter(
                  entry ->
                      entry
                          .getKey()
                          .getServiceInstance()
                          .equals(emInstanceLabel.getServiceInstance()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } else {
      filterInstanceAndLabel = instanceAndLabels;
    }

    EMNode[] nodes =
        getEMNodes(filterInstanceAndLabel.keySet().toArray(new ScoreServiceInstance[0]));
    if (nodes == null) {
      return null;
    }

    Arrays.stream(nodes)
        .forEach(
            node -> {
              Optional<List<Label<?>>> persistenceLabel =
                  filterInstanceAndLabel.entrySet().stream()
                      .filter(
                          entry ->
                              entry.getKey().getServiceInstance().equals(node.getServiceInstance()))
                      .map(Map.Entry::getValue)
                      .findFirst();
              persistenceLabel.ifPresent(
                  labelList ->
                      node.setLabels(
                          labelList.stream()
                              .map(ManagerUtils::persistenceLabelToRealLabel)
                              .collect(Collectors.toList())));
            });
    return nodes;
  }
}
