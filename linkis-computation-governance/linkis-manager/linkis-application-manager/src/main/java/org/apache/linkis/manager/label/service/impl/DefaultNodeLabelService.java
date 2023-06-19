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

package org.apache.linkis.manager.label.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.node.ScoreServiceInstance;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.utils.ManagerUtils;
import org.apache.linkis.manager.label.LabelManagerUtils;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.conf.LabelManagerConf;
import org.apache.linkis.manager.label.entity.Feature;
import org.apache.linkis.manager.label.entity.InheritableLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.score.LabelScoreServiceInstance;
import org.apache.linkis.manager.label.score.NodeLabelScorer;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultNodeLabelService implements NodeLabelService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultNodeLabelService.class);

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  private final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Autowired private NodeLabelScorer nodeLabelScorer;

  /**
   * Attach labels to node instance TODO 该方法需要优化,应该batch插入
   *
   * @param instance node instance
   * @param labels label list
   */
  @Transactional(rollbackFor = {Exception.class})
  @Override
  public void addLabelsToNode(ServiceInstance instance, List<Label<?>> labels) {
    if (null != labels && !labels.isEmpty()) {
      labels.forEach(
          label -> {
            addLabelToNode(instance, label);
          });
    }
  }

  @Transactional(rollbackFor = {Exception.class})
  @Override
  public void addLabelToNode(ServiceInstance instance, Label<?> label) {
    PersistenceLabel persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label);
    // Try to add
    int labelId = tryToAddLabel(persistenceLabel);
    if (labelId > 0) {
      List<PersistenceLabel> serviceRelationLabels =
          labelManagerPersistence.getLabelByServiceInstance(instance);
      if (!serviceRelationLabels.stream()
          .anyMatch(labelEntity -> labelEntity.getId().equals(labelId))) {
        labelManagerPersistence.addLabelToNode(instance, Collections.singletonList(labelId));
      }
    }
  }

  @Transactional(rollbackFor = {Exception.class})
  @Override
  public void updateLabelToNode(ServiceInstance instance, Label<?> label) {
    PersistenceLabel persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label);
    // Try to add
    int labelId = tryToAddLabel(persistenceLabel);
    if (labelId <= 0) return;
    List<PersistenceLabel> nodeLabels =
        this.labelManagerPersistence.getLabelByServiceInstance(instance);

    boolean needUpdate = true;
    List<Integer> needRemoveIds = new ArrayList();
    for (PersistenceLabel nodeLabel : nodeLabels) {
      if (nodeLabel.getLabelKey().equals(label.getLabelKey())) {
        if (nodeLabel.getId().equals(labelId)) {
          needUpdate = false;
        } else {
          needRemoveIds.add(nodeLabel.getId());
        }
      }
    }
    if (null != needRemoveIds && !needRemoveIds.isEmpty()) {
      this.labelManagerPersistence.removeNodeLabels(instance, needRemoveIds);
    }
    if (needUpdate) {
      List<Integer> labelIds = new ArrayList();
      labelIds.add(labelId);
      this.labelManagerPersistence.addLabelToNode(instance, labelIds);
    }
  }

  @Override
  public void updateLabelsToNode(ServiceInstance instance, List<Label<?>> labels) {
    List<String> newKeyList = labels.stream().map(Label::getLabelKey).collect(Collectors.toList());
    List<PersistenceLabel> nodeLabels = labelManagerPersistence.getLabelByServiceInstance(instance);

    List<String> oldKeyList =
        nodeLabels.stream().map(InheritableLabel::getLabelKey).collect(Collectors.toList());
    List<String> willBeDelete = new ArrayList<>(oldKeyList);
    willBeDelete.removeAll(newKeyList);

    List<String> willBeAdd = new ArrayList<>(newKeyList);
    willBeAdd.removeAll(oldKeyList);

    List<String> willBeUpdate = new ArrayList<>(oldKeyList);
    willBeUpdate.removeAll(willBeDelete);

    Set<String> modifiableKeyList = LabelUtils.listAllUserModifiableLabel();
    if (!willBeDelete.isEmpty()) {
      nodeLabels.forEach(
          nodeLabel -> {
            if (modifiableKeyList.contains(nodeLabel.getLabelKey())
                && willBeDelete.contains(nodeLabel.getLabelKey())) {
              List<Integer> labelIds = new ArrayList<>();
              labelIds.add(nodeLabel.getId());
              labelManagerPersistence.removeNodeLabels(instance, labelIds);
            }
          });
    }

    /**
     * update step: 1.delete relations of old labels 2.add new relation between new labels and
     * instance
     */
    if (willBeUpdate != null && !willBeUpdate.isEmpty()) {
      labels.forEach(
          label -> {
            if (modifiableKeyList.contains(label.getLabelKey())
                && willBeUpdate.contains(label.getLabelKey())) {
              nodeLabels.stream()
                  .filter(nodeLabel -> nodeLabel.getLabelKey().equals(label.getLabelKey()))
                  .forEach(
                      oldLabel -> {
                        PersistenceLabel persistenceLabel =
                            LabelManagerUtils.convertPersistenceLabel(label);
                        List<Integer> labelIds = new ArrayList<>();
                        labelIds.add(oldLabel.getId());
                        labelManagerPersistence.removeNodeLabels(instance, labelIds);
                        int newLabelId = tryToAddLabel(persistenceLabel);
                        labelIds.remove((Integer) oldLabel.getId());
                        labelIds.add(newLabelId);
                        labelManagerPersistence.addLabelToNode(instance, labelIds);
                      });
            }
          });
    }
    if (willBeAdd != null && !willBeAdd.isEmpty()) {
      labels.stream()
          .filter(label -> willBeAdd.contains(label.getLabelKey()))
          .forEach(
              label -> {
                if (modifiableKeyList.contains(label.getLabelKey())) {
                  PersistenceLabel persistenceLabel =
                      LabelManagerUtils.convertPersistenceLabel(label);
                  int labelId = tryToAddLabel(persistenceLabel);
                  if (labelId > 0) {
                    List<Integer> labelIds = new ArrayList<>();
                    labelIds.add(labelId);
                    labelManagerPersistence.addLabelToNode(instance, labelIds);
                  }
                }
              });
    }
  }

  /**
   * Remove the labels related by node instance
   *
   * @param instance node instance
   * @param labels labels
   */
  @Transactional(rollbackFor = {Exception.class})
  @Override
  public void removeLabelsFromNode(ServiceInstance instance, List<Label<?>> labels) {
    List<PersistenceLabel> labelList = labelManagerPersistence.getLabelByServiceInstance(instance);
    Map<String, PersistenceLabel> dbLabels =
        labelList.stream()
            .collect(
                Collectors.toMap(
                    PersistenceLabel::getLabelKey,
                    Function.identity(),
                    (existingValue, newValue) -> newValue));

    List<Integer> labelIds =
        labels.stream()
            .map(label -> dbLabels.get(label.getLabelKey()).getId())
            .collect(Collectors.toList());
    labelManagerPersistence.removeNodeLabels(instance, labelIds);
  }

  @Transactional(rollbackFor = {Exception.class})
  @Override
  public void removeLabelsFromNode(ServiceInstance instance, boolean isEngine) {
    List<PersistenceLabel> labels = labelManagerPersistence.getLabelByServiceInstance(instance);

    List<PersistenceLabel> removeLabels =
        isEngine
            ? labels
            : labels.stream()
                .filter(label -> !LabelManagerConf.LONG_LIVED_LABEL.contains(label.getLabelKey()))
                .collect(Collectors.toList());

    labelManagerPersistence.removeNodeLabels(
        instance, removeLabels.stream().map(PersistenceLabel::getId).collect(Collectors.toList()));
  }

  /**
   * Get node instances by labels
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  @Override
  public List<ServiceInstance> getNodesByLabels(List<Label<?>> labels) {
    return labels.stream()
        .flatMap(label -> getNodesByLabel(label).stream())
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public List<ServiceInstance> getNodesByLabel(Label<?> label) {
    Label<?> persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label);
    return labelManagerPersistence
        .getNodeByLabelKeyValue(persistenceLabel.getLabelKey(), persistenceLabel.getStringValue())
        .stream()
        .distinct()
        .collect(Collectors.toList());
  }

  @Override
  public List<Label<?>> getNodeLabels(ServiceInstance instance) {
    return labelManagerPersistence.getLabelByServiceInstance(instance).stream()
        .map(
            label -> {
              Label<?> realLabel =
                  labelFactory.createLabel(
                      label.getLabelKey(),
                      !CollectionUtils.isEmpty(label.getValue())
                          ? label.getValue()
                          : label.getStringValue());
              return realLabel;
            })
        .collect(Collectors.toList());
  }

  /**
   * Get scored node instances
   *
   * @param labels searchableLabel or other normal labels
   * @return
   */
  @Override
  public List<ScoreServiceInstance> getScoredNodesByLabels(List<Label<?>> labels) {
    return getScoredNodeMapsByLabels(labels).entrySet().stream()
        .map(entry -> entry.getKey())
        .collect(Collectors.toList());
  }

  /**
   * 1. Get the key value of the label 2.
   *
   * @param labels
   * @return
   */
  @Override
  public Map<ScoreServiceInstance, List<Label<?>>> getScoredNodeMapsByLabels(
      List<Label<?>> labels) {
    // Try to convert the label list to key value list
    if (labels != null && !labels.isEmpty()) {
      // Get the persistence labels by kvList
      List<Label<?>> requireLabels =
          labels.stream().filter(l -> l.getFeature() == Feature.CORE).collect(Collectors.toList());

      // Extra the necessary labels whose feature equals Feature.CORE or Feature.SUITABLE
      List<PersistenceLabel> necessaryLabels =
          requireLabels.stream()
              .map(LabelManagerUtils::convertPersistenceLabel)
              .collect(Collectors.toList());
      List<PersistenceLabel> inputLabels =
          labels.stream()
              .map(LabelManagerUtils::convertPersistenceLabel)
              .collect(Collectors.toList());
      return getScoredNodeMapsByLabels(inputLabels, necessaryLabels);
    }
    return new HashMap<>();
  }

  /**
   * 1. Get the relationship between the incoming label and node 2. get all instances by input
   * labels 3. get instance all labels 4. Judge labels
   *
   * @param labels
   * @param necessaryLabels
   * @return
   */
  private Map<ScoreServiceInstance, List<Label<?>>> getScoredNodeMapsByLabels(
      List<PersistenceLabel> labels, List<PersistenceLabel> necessaryLabels) {
    // Get the in-degree relations ( Label -> Nodes )
    Map<PersistenceLabel, List<ServiceInstance>> inNodeDegree =
        labelManagerPersistence.getNodeRelationsByLabels(
            necessaryLabels.isEmpty() ? labels : necessaryLabels);

    if (inNodeDegree.isEmpty()) {
      return new HashMap<>();
    }

    // serviceInstance --> labels
    Map<ServiceInstance, List<Label<?>>> instanceLabels = new HashMap<>();
    for (Map.Entry<PersistenceLabel, List<ServiceInstance>> keyValue : inNodeDegree.entrySet()) {
      keyValue
          .getValue()
          .forEach(
              (instance) -> {
                List<Label<?>> labels1 =
                    instanceLabels.computeIfAbsent(instance, (k) -> new ArrayList<>());
                labels1.add(ManagerUtils.persistenceLabelToRealLabel(keyValue.getKey()));
              });
    }

    // getAll instances
    Set<ServiceInstance> instances;
    if (!necessaryLabels.isEmpty()) {
      // Cut the in-degree relations, drop inconsistent nodes
      instances =
          instanceLabels.entrySet().stream()
              .filter((entry) -> entry.getValue().size() >= necessaryLabels.size())
              .map(Map.Entry::getKey)
              .collect(Collectors.toSet());
    } else {
      instances = instanceLabels.keySet();
    }

    Map<ScoreServiceInstance, List<Label<?>>> matchInstancesAndLabels = new HashMap<>();

    // Get the out-degree relations ( Node -> Label )
    Map<ServiceInstance, List<PersistenceLabel>> outNodeDegree =
        labelManagerPersistence.getLabelRelationsByServiceInstance(new ArrayList<>(instances));
    // outNodeDegree cannot be empty
    if (!outNodeDegree.isEmpty()) {
      Set<String> necessaryLabelKeys =
          necessaryLabels.stream().map(PersistenceLabel::getLabelKey).collect(Collectors.toSet());
      if (null == necessaryLabels || necessaryLabels.isEmpty()) {
        outNodeDegree.forEach(
            (node, iLabels) -> {
              matchInstancesAndLabels.put(
                  new LabelScoreServiceInstance(node),
                  iLabels.stream().map(label -> (Label<?>) label).collect(Collectors.toList()));
            });
      } else {
        outNodeDegree.forEach(
            (node, iLabels) -> {
              // The core tag must be exactly the same
              if (!necessaryLabels.isEmpty()) {
                Set<String> coreLabelKeys =
                    iLabels.stream()
                        .map(ManagerUtils::persistenceLabelToRealLabel)
                        .filter(l -> l.getFeature() == Feature.CORE)
                        .map(Label::getLabelKey)
                        .collect(Collectors.toSet());
                if (necessaryLabelKeys.containsAll(coreLabelKeys)
                    && coreLabelKeys.size() == necessaryLabelKeys.size()) {
                  matchInstancesAndLabels.put(
                      new LabelScoreServiceInstance(node),
                      iLabels.stream().map(label -> (Label<?>) label).collect(Collectors.toList()));
                }
              }
            });
      }
    }
    // Remove nodes with mismatched labels
    if (matchInstancesAndLabels.isEmpty()) {
      logger.info(
          "The entered labels {} do not match the labels of the node itself", necessaryLabels);
    }
    return matchInstancesAndLabels;
  }

  private int tryToAddLabel(PersistenceLabel persistenceLabel) {
    if (persistenceLabel.getId() <= 0) {
      PersistenceLabel label =
          labelManagerPersistence.getLabelByKeyValue(
              persistenceLabel.getLabelKey(), persistenceLabel.getStringValue());
      if (label == null) {
        persistenceLabel.setLabelValueSize(persistenceLabel.getValue().size());
        try {
          labelManagerPersistence.addLabel(persistenceLabel);
        } catch (Exception t) {
          logger.warn("Failed to add label {}", t.getClass().getName());
        }
      } else {
        persistenceLabel.setId(label.getId());
      }
    }
    return persistenceLabel.getId();
  }

  @Override
  public HashMap<String, List<Label<?>>> getNodeLabelsByInstanceList(
      List<ServiceInstance> serviceInstanceList) {
    HashMap<String, List<Label<?>>> resultMap = new HashMap<>();
    Map<ServiceInstance, List<PersistenceLabel>> map =
        labelManagerPersistence.getLabelRelationsByServiceInstance(serviceInstanceList);

    serviceInstanceList.forEach(
        serviceInstance -> {
          List<Label<?>> labelList =
              map.get(serviceInstance).stream()
                  .map(
                      label -> {
                        Label<?> realLabel =
                            labelFactory.createLabel(
                                label.getLabelKey(),
                                CollectionUtils.isEmpty(label.getValue())
                                    ? label.getStringValue()
                                    : label.getValue());
                        return realLabel;
                      })
                  .collect(Collectors.toList());

          resultMap.put(serviceInstance.toString(), labelList);
        });

    return resultMap;
  }
}
