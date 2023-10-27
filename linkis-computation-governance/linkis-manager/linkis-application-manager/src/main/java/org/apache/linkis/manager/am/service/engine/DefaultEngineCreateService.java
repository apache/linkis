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
import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.engineplugin.server.service.EngineConnResourceFactoryService;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.utils.JobUtils;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.conf.EngineConnConfigurationService;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.label.EngineReuseLabelChooser;
import org.apache.linkis.manager.am.manager.EngineNodeManager;
import org.apache.linkis.manager.am.selector.ECAvailableRule;
import org.apache.linkis.manager.am.selector.NodeSelector;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.common.protocol.engine.EngineCreateRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.utils.ManagerUtils;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequestImpl;
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnCreationDescImpl;
import org.apache.linkis.manager.engineplugin.common.resource.TimeoutEngineResourceRequest;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.EngineNodeLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.label.service.UserLabelService;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.rm.AvailableResource;
import org.apache.linkis.manager.rm.NotEnoughResource;
import org.apache.linkis.manager.rm.ResultResource;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.manager.service.common.label.LabelChecker;
import org.apache.linkis.manager.service.common.label.LabelFilter;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineCreateService extends AbstractEngineService
    implements EngineCreateService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineCreateService.class);

  @Autowired private NodeSelector nodeSelector;
  @Autowired private NodeLabelService nodeLabelService;
  @Autowired private ResourceManager resourceManager;
  @Autowired private List<LabelChecker> labelCheckerList;
  @Autowired private LabelFilter labelFilter;
  @Autowired private UserLabelService userLabelService;
  @Autowired private EngineConnConfigurationService engineConnConfigurationService;
  @Autowired private EngineConnResourceFactoryService engineConnResourceFactoryService;
  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;
  @Autowired private List<EngineReuseLabelChooser> engineReuseLabelChoosers;
  @Autowired private EngineStopService engineStopService;

  @Receiver
  public EngineNode createEngine(EngineCreateRequest engineCreateRequest, Sender sender)
      throws LinkisRetryException {
    long startTime = System.currentTimeMillis();
    String taskId = JobUtils.getJobIdFromStringMap(engineCreateRequest.getProperties());
    logger.info("Task: {} start to create Engine for request: {}.", taskId, engineCreateRequest);
    LabelBuilderFactory labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();
    long timeout =
        engineCreateRequest.getTimeout() <= 0
            ? AMConfiguration.ENGINE_START_MAX_TIME.getValue().toLong()
            : engineCreateRequest.getTimeout();

    // 1. Check if Label is valid
    List<Label<?>> labelList =
        LabelUtils.distinctLabel(
            labelBuilderFactory.getLabels(engineCreateRequest.getLabels()),
            userLabelService.getUserLabels(engineCreateRequest.getUser()));

    // label chooser
    if (engineReuseLabelChoosers != null) {
      for (EngineReuseLabelChooser chooser : engineReuseLabelChoosers) {
        labelList = chooser.chooseLabels(labelList);
      }
    }

    // 2. Check if Node is available
    for (LabelChecker labelChecker : labelCheckerList) {
      if (!labelChecker.checkEngineLabel(labelList)) {
        throw new AMErrorException(
            AMConstant.EM_ERROR_CODE, "Need to specify engineType and userCreator label");
      }
    }

    List<Label<?>> emLabelList = new ArrayList<>(labelList);
    AliasServiceInstanceLabel emInstanceLabel =
        labelBuilderFactory.createLabel(AliasServiceInstanceLabel.class);
    emInstanceLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue());
    emLabelList.add(emInstanceLabel);

    // 2. Get all available ECMs by labels
    EMNode[] emScoreNodeList =
        getEMService()
            .getEMNodes(
                emLabelList.stream()
                    .filter(label -> !(label instanceof EngineTypeLabel))
                    .collect(Collectors.toList()));

    // 3. Get the ECM with the lowest load by selection algorithm
    Optional<Node> choseNode =
        emScoreNodeList == null || emScoreNodeList.length == 0
            ? Optional.empty()
            : nodeSelector.choseNode(emScoreNodeList);

    if (!choseNode.isPresent()) {
      throw new LinkisRetryException(
          AMConstant.EM_ERROR_CODE,
          "The em of labels " + engineCreateRequest.getLabels() + " not found");
    }

    EMNode emNode = (EMNode) choseNode.get();

    // 4. request resource
    Pair<String, NodeResource> resourcePair =
        requestResource(
            engineCreateRequest, labelFilter.choseEngineLabel(labelList), emNode, timeout);
    String resourceTicketId = resourcePair.getKey();
    NodeResource resource = resourcePair.getValue();

    // 5. build engineConn request
    EngineConnBuildRequest engineBuildRequest =
        new EngineConnBuildRequestImpl(
            resourceTicketId,
            labelFilter.choseEngineLabel(labelList),
            resource,
            new EngineConnCreationDescImpl(
                engineCreateRequest.getCreateService(),
                engineCreateRequest.getDescription(),
                engineCreateRequest.getProperties()));

    // 6. Call ECM to send engine start request
    // AM will update the serviceInstance table
    // It is necessary to replace the ticketID and update the Label of EngineConn
    // It is necessary to modify the id in EngineInstanceLabel to Instance information
    ServiceInstance oldServiceInstance = new ServiceInstance();
    oldServiceInstance.setApplicationName(
        GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue());
    oldServiceInstance.setInstance(resourceTicketId);

    final EngineNode engineNode;
    try {
      engineNode = getEMService().createEngine(engineBuildRequest, emNode);
    } catch (Throwable t) {
      logger.info(
          "Failed to create ec(" + resourceTicketId + ") ask ecm " + emNode.getServiceInstance());
      final EngineNode failedEcNode = getEngineNodeManager().getEngineNode(oldServiceInstance);
      if (null == failedEcNode) {
        logger.info(" engineConn does not exist in db: " + oldServiceInstance);
      } else {
        failedEcNode.setLabels(nodeLabelService.getNodeLabels(oldServiceInstance));
        failedEcNode
            .getLabels()
            .addAll(
                LabelUtils.distinctLabel(
                    labelFilter.choseEngineLabel(labelList), emNode.getLabels()));
        failedEcNode.setNodeStatus(NodeStatus.Failed);
        engineStopService.engineConnInfoClear(failedEcNode);
      }
      throw t;
    }

    logger.info(
        "Task: {} finished to create engineConn {}. ticketId is {} ",
        taskId,
        engineNode,
        resourceTicketId);
    engineNode.setTicketId(resourceTicketId);

    // 7.Update persistent information: including inserting engine/metrics
    final EngineNodeManager engineNodeManager = getEngineNodeManager();
    try {
      engineNodeManager.updateEngineNode(oldServiceInstance, engineNode);
    } catch (Throwable t) {
      logger.warn("Failed to update engineNode " + engineNode, t);
      final EngineStopRequest stopEngineRequest =
          new EngineStopRequest(engineNode.getServiceInstance(), ManagerUtils.getAdminUser());
      engineStopService.asyncStopEngine(stopEngineRequest);
      final EngineNode failedEcNode = engineNodeManager.getEngineNode(oldServiceInstance);
      if (null == failedEcNode) {
        logger.info(" engineConn does not exist in db: " + oldServiceInstance);
      } else {
        failedEcNode.setLabels(nodeLabelService.getNodeLabels(oldServiceInstance));
        failedEcNode
            .getLabels()
            .addAll(
                LabelUtils.distinctLabel(
                    labelFilter.choseEngineLabel(labelList), emNode.getLabels()));
        failedEcNode.setNodeStatus(NodeStatus.Failed);
        engineStopService.engineConnInfoClear(failedEcNode);
      }
      throw new LinkisRetryException(
          AMConstant.EM_ERROR_CODE, "Failed to update engineNode: " + t.getMessage());
    }

    // 8. Add the Label of EngineConn, and add the Alias of engineConn
    final AliasServiceInstanceLabel engineConnAliasLabel =
        labelBuilderFactory.createLabel(AliasServiceInstanceLabel.class);
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue());
    labelList.add(engineConnAliasLabel);
    nodeLabelService.addLabelsToNode(
        engineNode.getServiceInstance(),
        labelFilter.choseEngineLabel(
            LabelUtils.distinctLabel(labelList, fromEMGetEngineLabels(emNode.getLabels()))));

    if (System.currentTimeMillis() - startTime >= timeout
        && engineCreateRequest.isIgnoreTimeout()) {
      logger.info(
          "Return a EngineConn {} for request: {} since the creator set ignoreTimeout=true and maxStartTime is reached.",
          engineNode,
          engineCreateRequest);
      return engineNode;
    }

    final long leftWaitTime = timeout - (System.currentTimeMillis() - startTime);
    if (ECAvailableRule.getInstance().isNeedAvailable(labelList)) {
      ensureECAvailable(engineNode, resourceTicketId, leftWaitTime);
      logger.info(
          "Task: {} finished to create Engine for request: {} and get engineNode {}. time taken {} ms",
          taskId,
          engineCreateRequest,
          engineNode,
          (System.currentTimeMillis() - startTime));
    } else {
      logger.info(
          "Task: {} finished to create Engine for request: {} and get engineNode {}. And did not judge the availability, time taken "
              + "{} ms",
          taskId,
          engineCreateRequest,
          engineNode,
          (System.currentTimeMillis() - startTime));
    }

    return engineNode;
  }

  /**
   * Read the management console configuration and the parameters passed in by the user to combine
   * request resources
   *
   * @param engineCreateRequest
   * @param labelList
   * @param emNode
   * @param timeout
   * @return
   */
  private Pair<String, NodeResource> requestResource(
      EngineCreateRequest engineCreateRequest,
      List<Label<?>> labelList,
      EMNode emNode,
      long timeout) {
    if (engineCreateRequest.getProperties() == null) {
      engineCreateRequest.setProperties(new HashMap<String, String>());
    }
    Map<String, String> configProp =
        engineConnConfigurationService.getConsoleConfiguration(labelList);
    final Map<String, String> props = engineCreateRequest.getProperties();
    if (configProp != null && !configProp.isEmpty()) {
      for (Map.Entry<String, String> keyValue : configProp.entrySet()) {
        if (!props.containsKey(keyValue.getKey())) {
          props.put(keyValue.getKey(), keyValue.getValue());
        }
      }
    }
    final TimeoutEngineResourceRequest timeoutEngineResourceRequest =
        new TimeoutEngineResourceRequest(
            timeout, engineCreateRequest.getUser(), labelList, engineCreateRequest.getProperties());
    final NodeResource resource =
        engineConnResourceFactoryService.createEngineResource(timeoutEngineResourceRequest);

    ResultResource resultResource =
        resourceManager.requestResource(
            LabelUtils.distinctLabel(labelList, emNode.getLabels()), resource, timeout);
    if (resultResource instanceof AvailableResource) {
      AvailableResource availableResource = (AvailableResource) resultResource;
      return Pair.of(availableResource.getTicketId(), resource);
    } else {
      NotEnoughResource notEnoughResource = (NotEnoughResource) resultResource;
      logger.warn("not engough resource: " + notEnoughResource.getReason());
      throw new LinkisRetryException(
          AMConstant.EM_ERROR_CODE, "not engough resource: : " + notEnoughResource.getReason());
    }
  }

  private List<Label<?>> fromEMGetEngineLabels(List<Label<?>> emLabels) {
    return emLabels.stream()
        .filter(label -> label instanceof EngineNodeLabel && !(label instanceof EngineTypeLabel))
        .collect(Collectors.toList());
  }

  private boolean ensuresIdle(EngineNode engineNode, String resourceTicketId) {
    EngineNode engineNodeInfo;
    if (engineNode.getMark().equals(AMConstant.CLUSTER_PROCESS_MARK)) {
      engineNodeInfo = getEngineNodeManager().getEngineNodeInfoByTicketId(resourceTicketId);
    } else {
      engineNodeInfo = getEngineNodeManager().getEngineNodeInfoByDB(engineNode);
    }
    if (null == engineNodeInfo) {
      return false;
    }

    if (engineNodeInfo.getServiceInstance() != null) {
      engineNode.setServiceInstance(engineNodeInfo.getServiceInstance());
    }

    if (NodeStatus.isCompleted(engineNodeInfo.getNodeStatus())) {
      NodeMetrics metrics = nodeMetricManagerPersistence.getNodeMetrics(engineNodeInfo);
      Pair<String, Optional<Boolean>> errorInfo = getStartErrorInfo(metrics.getHeartBeatMsg());
      if (errorInfo.getRight().isPresent()) {
        throw new LinkisRetryException(
            AMConstant.ENGINE_ERROR_CODE,
            String.format(
                "%s  ticketID:%s  Failed to initialize engine, reason: %s ",
                engineNode.getServiceInstance(), resourceTicketId, errorInfo.getKey()));
      }
      throw new AMErrorException(
          AMConstant.EM_ERROR_CODE,
          String.format(
              "%s ticketID: %s Failed to initialize engine, reason: %s",
              engineNode.getServiceInstance(), resourceTicketId, errorInfo.getKey()));
    }
    return NodeStatus.isAvailable(engineNodeInfo.getNodeStatus());
  }

  private Pair<String, Optional<Boolean>> getStartErrorInfo(String msg) {
    if (StringUtils.isNotBlank(msg)) {
      JsonNode jsonNode = null;
      try {
        jsonNode = BDPJettyServerHelper.jacksonJson().readTree(msg);
      } catch (JsonProcessingException e) {
        logger.warn("getStartErrorInfo readTree failed msg: {}", msg, e);
      }
      if (jsonNode != null && jsonNode.has(AMConstant.START_REASON)) {
        String startReason = jsonNode.get(AMConstant.START_REASON).asText();
        if (jsonNode.has(AMConstant.EC_CAN_RETRY)) {
          return Pair.of(startReason, Optional.of(true));
        } else {
          return Pair.of(startReason, Optional.empty());
        }
      }
    }
    return Pair.of(null, Optional.empty());
  }

  /**
   * Need to ensure that the newly created amount ec is available before returning
   *
   * @param engineNode
   * @param resourceTicketId
   * @param timeout
   * @return
   */
  public EngineNode ensureECAvailable(
      EngineNode engineNode, String resourceTicketId, long timeout) {
    try {
      logger.info(
          String.format(
              "Start to wait engineConn(%s) to be available, but only %s left.",
              engineNode, ByteTimeUtils.msDurationToString(timeout)));
      LinkisUtils.waitUntil(
          () -> ensuresIdle(engineNode, resourceTicketId), Duration.ofMillis(timeout));
    } catch (TimeoutException e) {
      logger.info(
          String.format(
              "Waiting for engineNode:%s(%s) initialization TimeoutException , now stop it.",
              engineNode, resourceTicketId));
      EngineStopRequest stopEngineRequest =
          new EngineStopRequest(engineNode.getServiceInstance(), ManagerUtils.getAdminUser());
      engineStopService.asyncStopEngine(stopEngineRequest);
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          String.format(
              "Waiting for engineNode:%s(%s) initialization TimeoutException, already waiting %d ms",
              engineNode, resourceTicketId, timeout));
    } catch (Throwable t) {
      logger.info(
          String.format(
              "Waiting for %s(%s) initialization failure , now stop it.",
              engineNode, resourceTicketId));
      EngineStopRequest stopEngineRequest =
          new EngineStopRequest(engineNode.getServiceInstance(), ManagerUtils.getAdminUser());
      engineStopService.asyncStopEngine(stopEngineRequest);
      if (t instanceof LinkisRetryException) {
        throw (LinkisRetryException) t;
      }
      throw new AMErrorException(
          AMConstant.ENGINE_ERROR_CODE,
          String.format("Waiting for %s(%s) initialization failure", engineNode, resourceTicketId),
          t);
    }
    return engineNode;
  }
}
