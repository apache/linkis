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
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.resource.DriverAndYarnResource;
import org.apache.linkis.manager.common.entity.resource.LoadInstanceResource;
import org.apache.linkis.manager.common.entity.resource.Resource;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.common.protocol.engine.EngineConnReleaseRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineSuicideRequest;
import org.apache.linkis.manager.dao.NodeMetricManagerMapper;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.label.service.impl.DefaultNodeLabelRemoveService;
import org.apache.linkis.manager.rm.exception.RMErrorCode;
import org.apache.linkis.manager.rm.service.impl.DefaultResourceManager;
import org.apache.linkis.protocol.label.NodeLabelRemoveRequest;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineStopService extends AbstractEngineService implements EngineStopService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineStopService.class);

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private DefaultResourceManager resourceManager;

  @Autowired private DefaultNodeLabelRemoveService nodeLabelRemoveService;

  @Autowired private EngineInfoService engineInfoService;

  @Autowired private EngineStopService engineStopService;

  @Autowired private NodeMetricManagerMapper nodeMetricManagerMapper;

  private ExecutorService EXECUTOR =
      LinkisUtils.newFixedThreadPool(
          AMConfiguration.ASYNC_STOP_ENGINE_MAX_THREAD_SIZE,
          "AsyncStopEngineService-Thread-",
          true);

  @Receiver
  public void stopEngine(EngineStopRequest engineStopRequest, Sender sender) {
    engineStopRequest
        .getServiceInstance()
        .setApplicationName(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue());
    logger.info(
        String.format(
            "user %s prepare to stop engine %s",
            engineStopRequest.getUser(), engineStopRequest.getServiceInstance()));
    EngineNode node = getEngineNodeManager().getEngineNode(engineStopRequest.getServiceInstance());
    if (Objects.isNull(node)) {
      logger.info(String.format("engineConn does not exist in db: %s", (engineStopRequest)));
      return;
    }

    node.setLabels(nodeLabelService.getNodeLabels(engineStopRequest.getServiceInstance()));

    // 1. request em to kill ec
    logger.info(
        String.format("Start to kill engine invoke enginePointer %s", node.getServiceInstance()));
    try {
      getEMService().stopEngine(node, node.getEMNode());
      logger.info("Finished to kill engine invoke enginePointer {}", node.getServiceInstance());
    } catch (Exception e) {
      logger.warn(String.format("Failed to stop engine %s", node.getServiceInstance()));
    }

    if (node.getNodeStatus() == null) {
      node.setNodeStatus(NodeStatus.ShuttingDown);
    }
    engineConnInfoClear(node);
    logger.info(
        String.format(
            "user %s finished to stop engine %s",
            engineStopRequest.getUser(), engineStopRequest.getServiceInstance()));
  }

  @Override
  public Map<String, Object> stopUnlockEngineByECM(String ecmInstance, String operatorName) {
    Map<String, Object> resultMap = new HashMap<>();
    int killEngineNum = 0;

    // get all unlock ec node of the specified ecm
    ServiceInstance ecmInstanceService = new ServiceInstance();
    ecmInstanceService.setInstance(ecmInstance);
    ecmInstanceService.setApplicationName(
        GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue());

    AMEMNode emNode = new AMEMNode();
    emNode.setServiceInstance(ecmInstanceService);

    List<EngineNode> engineNodes = engineInfoService.listEMEngines(emNode);

    List<EngineNode> unlockEngineNodes =
        engineNodes.stream()
            .filter(node -> NodeStatus.Unlock.equals(node.getNodeStatus()))
            .filter(node -> !node.getLabels().isEmpty())
            .collect(Collectors.toList());

    logger.info(
        String.format(
            "get ec node total num:%d and unlock node num:%d of ecm:%s",
            engineNodes.size(), unlockEngineNodes.size(), ecmInstance));

    LoadInstanceResource loadInstanceResourceTotal = new LoadInstanceResource(0, 0, 0);

    for (EngineNode node : unlockEngineNodes) {
      if (logger.isDebugEnabled()) {
        try {
          logger.debug("ec node:" + AMUtils.mapper.writeValueAsString(node));
        } catch (JsonProcessingException e) {
          logger.debug("convert jobReq to string with error:" + e.getMessage());
        }
      }

      EngineTypeLabel engineTypeLabel =
          node.getLabels().stream()
              .filter(label -> label instanceof EngineTypeLabel)
              .map(label -> (EngineTypeLabel) label)
              .findFirst()
              .orElse(null);

      String engineTypeStr = engineTypeLabel != null ? engineTypeLabel.getEngineType() : null;
      boolean isAllowKill = AMConfiguration.isAllowKilledEngineType(engineTypeStr);

      if (!isAllowKill) {
        logger.info(
            String.format(
                "skipped to kill engine node:%s, engine type:%s",
                node.getServiceInstance().getInstance(), engineTypeStr));
      } else {
        // calculate the resources that can be released
        if (node.getNodeResource().getUsedResource() != null) {
          Resource usedResource = node.getNodeResource().getUsedResource();
          Resource realResource = usedResource;
          if (usedResource instanceof DriverAndYarnResource) {
            realResource = ((DriverAndYarnResource) usedResource).getLoadInstanceResource();
          }
          loadInstanceResourceTotal =
              loadInstanceResourceTotal.add(node.getNodeResource().getUsedResource());
        }

        logger.info(
            String.format(
                "try to asyn kill engine node:%s", node.getServiceInstance().getInstance()));
        killEngineNum++;
        // asyn to stop
        EngineStopRequest stopEngineRequest =
            new EngineStopRequest(node.getServiceInstance(), operatorName);
        asyncStopEngineWithUpdateMetrics(stopEngineRequest);
      }
    }

    resultMap.put("killEngineNum", killEngineNum);
    resultMap.put("memory", loadInstanceResourceTotal.getMemory());
    resultMap.put("cores", loadInstanceResourceTotal.getCores());
    resultMap.put("batchKillEngineType", AMConfiguration.ALLOW_BATCH_KILL_ENGINE_TYPES.getValue());

    return resultMap;
  }

  /**
   * to clear rm info 2. to clear label info 3. to clear am info
   *
   * @param ecNode
   */
  @Override
  public void engineConnInfoClear(EngineNode ecNode) {
    logger.info(String.format("Start to clear ec info %s", ecNode));
    // 1. to clear engine resource
    try {
      resourceManager.resourceReleased(ecNode);
    } catch (Exception e) {
      if (e instanceof RMWarnException) {
        RMWarnException exception = (RMWarnException) e;
        if (exception.getErrCode() != RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode()) {
          throw exception;
        }
      }
    }

    // 2. to clear Label
    NodeLabelRemoveRequest instanceLabelRemoveRequest =
        new NodeLabelRemoveRequest(ecNode.getServiceInstance(), true);
    nodeLabelRemoveService.removeNodeLabel(instanceLabelRemoveRequest);

    // 3. to clear engine node info
    getEngineNodeManager().deleteEngineNode(ecNode);
    logger.info(String.format("Finished to clear ec info %s", ecNode));
  }

  @Override
  @Receiver
  public void engineSuicide(EngineSuicideRequest engineSuicideRequest, Sender sender) {
    logger.info(
        String.format(
            "Will ask engine : %s of user : %s to suicide.",
            engineSuicideRequest.getServiceInstance().toString(), engineSuicideRequest.getUser()));
    EngineStopService.askEngineToSuicide(engineSuicideRequest);
  }

  @Override
  @Receiver
  public void dealEngineRelease(EngineConnReleaseRequest engineConnReleaseRequest, Sender sender) {
    logger.info(
        String.format(
            "Start to kill engine, with msg: %s, %s",
            engineConnReleaseRequest.getMsg(),
            engineConnReleaseRequest.getServiceInstance().toString()));
    if (engineConnReleaseRequest.getServiceInstance() == null) {
      logger.warn("Invalid empty serviceInstance, will not kill engine.");
      return;
    }
    EngineNode engineNode =
        getEngineNodeManager().getEngineNode(engineConnReleaseRequest.getServiceInstance());
    if (engineNode != null) {
      logger.info(
          String.format(
              "Send stop engine request %s",
              engineConnReleaseRequest.getServiceInstance().toString()));
      engineNode.setLabels(nodeLabelService.getNodeLabels(engineNode.getServiceInstance()));
      engineNode.setNodeStatus(engineConnReleaseRequest.getNodeStatus());
      engineConnInfoClear(engineNode);
    } else {
      logger.warn(
          String.format(
              "Cannot find valid engineNode from serviceInstance: %s",
              engineConnReleaseRequest.getServiceInstance().toString()));
    }
  }

  @Override
  public void asyncStopEngine(EngineStopRequest engineStopRequest) {
    CompletableFuture.runAsync(
        () -> {
          logger.info(String.format("Start to async stop engineFailed %s", engineStopRequest));
          LinkisUtils.tryAndErrorMsg(
              () ->
                  stopEngine(engineStopRequest, Sender.getSender(Sender.getThisServiceInstance())),
              String.format("async stop engineFailed %s", engineStopRequest),
              logger);
        },
        EXECUTOR);
  }

  @Override
  public void asyncStopEngineWithUpdateMetrics(EngineStopRequest engineStopRequest) {
    CompletableFuture.runAsync(
        () -> {
          try {
            logger.info(String.format("Start to async stop engine node:%s", engineStopRequest));
            // 1. set ec node Metrics status Unlock to ShuttingDown
            // 2. ec node metircs report ignore update Shutingdown node
            String instance = engineStopRequest.getServiceInstance().getInstance();
            logger.info(
                String.format(
                    "Try to update ec node:%s status Unlock --> ShuttingDown", engineStopRequest));
            int ok =
                nodeMetricManagerMapper.updateNodeStatus(
                    instance, NodeStatus.ShuttingDown.ordinal(), NodeStatus.Unlock.ordinal());
            if (ok > 0) {
              logger.info(String.format("Try to do stop ec node %s action", engineStopRequest));
              stopEngine(engineStopRequest, Sender.getSender(Sender.getThisServiceInstance()));
            } else {
              logger.info(
                  String.format(
                      "ec node:%s status update failed! maybe the status is not unlock. will skip to kill this ec node",
                      instance));
            }
          } catch (Exception e) {
            logger.error(
                String.format("asyncStopEngineWithUpdateMetrics with error: %s", e.getMessage()),
                e);
          }
        },
        EXECUTOR);
  }
}
