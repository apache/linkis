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

import org.apache.linkis.common.exception.LinkisRetryException;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.governance.common.utils.JobUtils;
import org.apache.linkis.manager.am.conf.AMConfiguration;
import org.apache.linkis.manager.am.exception.AMErrorException;
import org.apache.linkis.manager.am.label.EngineReuseLabelChooser;
import org.apache.linkis.manager.am.selector.NodeSelector;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.am.utils.AMUtils;
import org.apache.linkis.manager.common.constant.AMConstant;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.Node;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.common.protocol.engine.EngineReuseRequest;
import org.apache.linkis.manager.common.protocol.engine.EngineStopRequest;
import org.apache.linkis.manager.common.utils.ManagerUtils;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.EngineNodeLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.ReuseExclusionLabel;
import org.apache.linkis.manager.label.entity.node.AliasServiceInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.label.service.UserLabelService;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.rpc.message.annotation.Receiver;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultEngineReuseService extends AbstractEngineService implements EngineReuseService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultEngineReuseService.class);

  @Autowired private NodeSelector nodeSelector;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private UserLabelService userLabelService;

  @Autowired(required = false)
  private List<EngineReuseLabelChooser> engineReuseLabelChoosers;

  @Autowired private EngineStopService engineStopService;

  /**
   * 1. Obtain the EC corresponding to all labels 2. Judging reuse exclusion tags and fixed engine
   * labels 3. Select the EC with the lowest load available 4. Lock the corresponding EC
   *
   * @param engineReuseRequest
   * @param sender
   * @return
   * @throws
   */
  @Receiver
  @Override
  public EngineNode reuseEngine(EngineReuseRequest engineReuseRequest, Sender sender)
      throws LinkisRetryException {
    String taskId = JobUtils.getJobIdFromStringMap(engineReuseRequest.getProperties());
    logger.info("Task " + taskId + " Start to reuse Engine for request: " + engineReuseRequest);
    LabelBuilderFactory labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();
    List<Label<?>> labelList =
        LabelUtils.distinctLabel(
            labelBuilderFactory.getLabels(engineReuseRequest.getLabels()),
            userLabelService.getUserLabels(engineReuseRequest.getUser()));

    String[] exclusionInstances =
        labelList.stream()
            .filter(label -> label instanceof ReuseExclusionLabel)
            .findFirst()
            .map(label -> ((ReuseExclusionLabel) label).getInstances())
            .orElse(new String[0]);

    if (exclusionInstances.length == 1
        && exclusionInstances[0].equals(GovernanceCommonConf.WILDCARD_CONSTANT())) {
      logger.info(
          "Task "
              + taskId
              + " exists ReuseExclusionLabel and the configuration does not choose to reuse EC");
      return null;
    }

    List<Label<?>> filterLabelList =
        new ArrayList<>(
            labelList.stream()
                .filter(label -> label instanceof EngineNodeLabel)
                .collect(Collectors.toList()));

    AliasServiceInstanceLabel engineConnAliasLabel =
        labelBuilderFactory.createLabel(AliasServiceInstanceLabel.class);
    engineConnAliasLabel.setAlias(GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue());
    filterLabelList.add(engineConnAliasLabel);

    // label chooser
    if (engineReuseLabelChoosers != null) {
      for (EngineReuseLabelChooser chooser : engineReuseLabelChoosers) {
        filterLabelList = chooser.chooseLabels(filterLabelList);
      }
    }

    Map<ScoreServiceInstance, List<Label<?>>> instances =
        nodeLabelService.getScoredNodeMapsByLabels(filterLabelList);

    if (instances != null && exclusionInstances.length > 0) {
      ScoreServiceInstance[] instancesKeys =
          instances.keySet().toArray(new ScoreServiceInstance[0]);
      Arrays.stream(instancesKeys)
          .filter(
              instance ->
                  Arrays.stream(exclusionInstances)
                      .anyMatch(
                          excludeInstance ->
                              excludeInstance.equalsIgnoreCase(
                                  instance.getServiceInstance().getInstance())))
          .forEach(
              instance -> {
                logger.info(
                    "will be not reuse "
                        + instance.getServiceInstance()
                        + ", cause use exclusion label");
                instances.remove(instance);
              });
    }
    if (instances == null || instances.isEmpty()) {
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE, "No engine can be reused, cause from db is null");
    }

    ScoreServiceInstance[] scoreServiceInstances =
        instances.keySet().toArray(new ScoreServiceInstance[0]);
    EngineNode[] engineScoreList = getEngineNodeManager().getEngineNodes(scoreServiceInstances);

    if (null == engineScoreList || engineScoreList.length == 0) {
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE, "No engine can be reused, cause from db is null");
    }

    List<EngineNode> engines = Lists.newArrayList();
    long timeout =
        engineReuseRequest.getTimeOut() <= 0
            ? AMConfiguration.ENGINE_REUSE_MAX_TIME.getValue().toLong()
            : engineReuseRequest.getTimeOut();
    int reuseLimit =
        engineReuseRequest.getReuseCount() <= 0
            ? (int) AMConfiguration.ENGINE_REUSE_COUNT_LIMIT.getValue()
            : engineReuseRequest.getReuseCount();

    long startTime = System.currentTimeMillis();
    try {
      MutablePair<Integer, Integer> limitPair = MutablePair.of(1, reuseLimit);
      List<EngineNode> canReuseEcList = new ArrayList<>();
      CollectionUtils.addAll(canReuseEcList, engineScoreList);
      LinkisUtils.waitUntil(
          () -> selectEngineToReuse(limitPair, engines, canReuseEcList),
          Duration.ofMillis(timeout));
    } catch (TimeoutException e) {
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          "Waiting for Engine initialization failure, already waiting " + timeout + " ms");
    } catch (Throwable t) {
      logger.info(
          "Failed to reuse engineConn time taken " + (System.currentTimeMillis() - startTime), t);
      throw new AMErrorException(
          AMConstant.ENGINE_ERROR_CODE,
          "Failed to reuse engineConn time taken " + (System.currentTimeMillis() - startTime));
    }
    EngineNode engine = engines.get(0);
    logger.info(
        "Finished to reuse Engine for request: "
            + engineReuseRequest
            + " get EngineNode "
            + engine
            + " time taken "
            + (System.currentTimeMillis() - startTime));
    List<Map.Entry<ScoreServiceInstance, List<Label<?>>>> engineServiceLabelList =
        new ArrayList<>(instances.entrySet())
            .stream()
                .filter(kv -> kv.getKey().getServiceInstance().equals(engine.getServiceInstance()))
                .collect(Collectors.toList());
    if (!engineServiceLabelList.isEmpty()) {
      engine.setLabels(engineServiceLabelList.get(0).getValue());
    } else {
      logger.info(
          "Get choosen engineNode : "
              + AMUtils.toJSONString(engine)
              + " from engineLabelMap : "
              + AMUtils.toJSONString(instances));
    }

    return engine;
  }

  public boolean selectEngineToReuse(
      MutablePair<Integer, Integer> count2reuseLimit,
      List<EngineNode> engines,
      List<EngineNode> canReuseEcList) {
    if (count2reuseLimit.getLeft() > count2reuseLimit.getRight()) {
      throw new LinkisRetryException(
          AMConstant.ENGINE_ERROR_CODE,
          "Engine reuse exceeds limit: " + count2reuseLimit.getLeft());
    }

    Optional<Node> choseNode = nodeSelector.choseNode(canReuseEcList.toArray(new Node[0]));
    if (!choseNode.isPresent()) {
      throw new LinkisRetryException(AMConstant.ENGINE_ERROR_CODE, "No engine can be reused");
    }
    EngineNode engineNode = (EngineNode) choseNode.get();
    logger.info(
        "prepare to reuse engineNode: {} times {}",
        engineNode.getServiceInstance(),
        count2reuseLimit.getLeft());

    EngineNode reuseEngine =
        LinkisUtils.tryCatch(
            () -> getEngineNodeManager().reuseEngine(engineNode),
            (Throwable t) -> {
              logger.info("Failed to reuse engine " + engineNode.getServiceInstance(), t);
              if (ExceptionUtils.getRootCause(t) instanceof TimeoutException) {
                logger.info(
                    "Failed to reuse " + engineNode.getServiceInstance() + ", now to stop this");
                EngineStopRequest stopEngineRequest =
                    new EngineStopRequest(
                        engineNode.getServiceInstance(), ManagerUtils.getAdminUser());
                engineStopService.asyncStopEngine(stopEngineRequest);
              }
              return null;
            });
    if (Objects.nonNull(reuseEngine)) {
      engines.add(reuseEngine);
    }

    if (CollectionUtils.isEmpty(engines)) {
      count2reuseLimit.setLeft(count2reuseLimit.getLeft() + 1);
      canReuseEcList.remove(choseNode.get());
    }
    return CollectionUtils.isNotEmpty(engines);
  }
}
