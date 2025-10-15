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

package org.apache.linkis.manager.persistence.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.dao.LabelManagerMapper;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.manager.entity.Tunple;
import org.apache.linkis.manager.exception.PersistenceWarnException;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.util.PersistenceUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.errorcode.LinkisManagerPersistenceErrorCodeSummary.BEANUTILS_POPULATE_FAILED;

public class DefaultLabelManagerPersistence implements LabelManagerPersistence {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private LabelManagerMapper labelManagerMapper;

  private NodeManagerMapper nodeManagerMapper;

  public LabelManagerMapper getLabelManagerMapper() {
    return labelManagerMapper;
  }

  public void setLabelManagerMapper(LabelManagerMapper labelManagerMapper) {
    this.labelManagerMapper = labelManagerMapper;
  }

  public NodeManagerMapper getNodeManagerMapper() {
    return nodeManagerMapper;
  }

  public void setNodeManagerMapper(NodeManagerMapper nodeManagerMapper) {
    this.nodeManagerMapper = nodeManagerMapper;
  }

  @Override
  public List<PersistenceLabelRel> getLabelByPattern(
      String labelValuePattern, String labelKey, Integer page, Integer size) {
    return labelManagerMapper.listLabelBySQLPattern(labelValuePattern, labelKey);
  }

  @Override
  public void addLabel(PersistenceLabel persistenceLabel) {
    labelManagerMapper.registerLabel(persistenceLabel);
    int labelId = persistenceLabel.getId();

    // 此处需要修正，要拿到 label_value_key label_value_content  labelValue中有多对参数
    Map<String, String> labelValueKeyAndContent = persistenceLabel.getValue();

    // replaceIntoLabelKeyValues(labelValueKeyAndContent, labelId);
  }

  @Override
  public void removeLabel(int id) {
    labelManagerMapper.deleteUserById(id);
    labelManagerMapper.deleteLabelKeyVaules(id);
    labelManagerMapper.deleteLabel(id);
  }

  @Override
  public void removeLabel(PersistenceLabel persistenceLabel) {
    String labelKey = persistenceLabel.getLabelKey();
    String labelStringValue = persistenceLabel.getStringValue();
    int labelId = persistenceLabel.getId();
    if (labelId <= 0) {
      PersistenceLabel labelByKeyValue =
          labelManagerMapper.getLabelByKeyValue(labelKey, labelStringValue);
      if (null == labelByKeyValue) {
        logger.warn("Can not find label labelKey {}, label value {}", labelKey, labelStringValue);
        return;
      }
      labelId = labelByKeyValue.getId();
    }
    if (labelId > 0) {
      labelManagerMapper.deleteLabel(labelId);
      labelManagerMapper.deleteLabelKeyVaules(labelId);
    }
  }

  @Override
  public void updateLabel(int id, PersistenceLabel persistenceLabel) {
    persistenceLabel.setUpdateTime(new Date());
    labelManagerMapper.updateLabel(id, persistenceLabel);
    labelManagerMapper.deleteLabelKeyVaules(id);
    /* if (!persistenceLabel.getValue().isEmpty()) {
        replaceIntoLabelKeyValues(persistenceLabel.getValue(), id);
    }*/
  }

  private void replaceIntoLabelKeyValues(Map<String, String> labelValueKeyAndContent, int labelId) {
    if (null != labelValueKeyAndContent && labelId > 0) {
      Iterator<Map.Entry<String, String>> iterator = labelValueKeyAndContent.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, String> labelKeyValue = iterator.next();
        if (StringUtils.isNotBlank(labelKeyValue.getKey())) {
          labelManagerMapper.replaceIntoLabelKeyValue(
              labelKeyValue.getKey(), labelKeyValue.getValue(), labelId);
        }
      }
    }
  }

  @Retryable(
      value = {CannotGetJdbcConnectionException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 10000))
  @Override
  public PersistenceLabel getLabel(int id) {
    PersistenceLabel persistenceLabel = labelManagerMapper.getLabel(id);
    if (null != persistenceLabel) {
      PersistenceUtils.setValue(persistenceLabel);
    }
    return persistenceLabel;
  }

  @Override
  public List<PersistenceLabel> getLabelByServiceInstance(ServiceInstance serviceInstance) {
    List<PersistenceLabel> persistenceLabelList =
        labelManagerMapper.getLabelByServiceInstance(serviceInstance.getInstance());
    persistenceLabelList.forEach(PersistenceUtils::setValue);
    return persistenceLabelList;
  }

  @Override
  public List<PersistenceLabel> getLabelByResource(PersistenceResource persistenceResource) {
    List<PersistenceLabel> persistenceLabelList =
        labelManagerMapper.getLabelByResource(persistenceResource);
    return persistenceLabelList;
  }

  @Override
  public void addLabelToNode(ServiceInstance serviceInstance, List<Integer> labelIds) {
    if (!CollectionUtils.isEmpty(labelIds)) {
      labelManagerMapper.addLabelServiceInstance(serviceInstance.getInstance(), labelIds);
    }
  }

  @Override
  public List<PersistenceLabel> getLabelsByValue(
      Map<String, String> value, Label.ValueRelation valueRelation) {
    return getLabelsByValueList(Collections.singletonList(value), valueRelation);
  }

  @Override
  @Deprecated
  public List<PersistenceLabel> getLabelsByValueList(
      List<Map<String, String>> valueList, Label.ValueRelation valueRelation) {
    if (PersistenceUtils.valueListIsEmpty(valueList)) return Collections.emptyList();
    if (valueRelation == null) valueRelation = Label.ValueRelation.ALL;
    return null;
  }

  @Override
  public PersistenceLabel getLabelsByKeyValue(
      String labelKey, Map<String, String> value, Label.ValueRelation valueRelation) {
    List<PersistenceLabel> labelsByValueList =
        getLabelsByKeyValueMap(Collections.singletonMap(labelKey, value), valueRelation);
    return labelsByValueList.isEmpty() ? null : labelsByValueList.get(0);
  }

  @Override
  public List<PersistenceLabel> getLabelsByKeyValueMap(
      Map<String, Map<String, String>> keyValueMap, Label.ValueRelation valueRelation) {
    if (PersistenceUtils.KeyValueMapIsEmpty(keyValueMap)) return Collections.emptyList();
    if (valueRelation == null) valueRelation = Label.ValueRelation.ALL;
    return labelManagerMapper
        .dimListLabelByKeyValueMap(
            PersistenceUtils.filterEmptyKeyValueMap(keyValueMap), valueRelation.name())
        .stream()
        .map(PersistenceUtils::setValue)
        .collect(Collectors.toList());
  }

  @Override
  public List<PersistenceLabel> getLabelsByKey(String labelKey) {
    List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelsByLabelKey(labelKey);
    return persistenceLabelList;
  }

  //    public List<PersistenceLabel> getLabelsByValue(Map<String, String> labelKeyValues) {
  //        //先查id再由id得到标签
  //        List<Integer> labelIds = labelManagerMapper.getLabelByLabelKeyValues(labelKeyValues);
  //        List<PersistenceLabel> persistenceLabelList =
  // labelManagerMapper.getLabelsByLabelIds(labelIds);
  //        return persistenceLabelList;
  //    }

  @Override
  public void removeNodeLabels(ServiceInstance serviceInstance, List<Integer> labelIds) {
    String instance = serviceInstance.getInstance();
    if (null != labelIds && !labelIds.isEmpty()) {
      labelManagerMapper.deleteLabelIdsAndInstance(instance, labelIds);
    }
  }

  @Override
  public List<ServiceInstance> getNodeByLabel(int labelId) {
    List<PersistenceNode> persistenceNodeList = labelManagerMapper.getInstanceByLabelId(labelId);
    List<ServiceInstance> serviceInstanceList = new ArrayList<>();
    for (PersistenceNode persistenceNode : persistenceNodeList) {
      ServiceInstance serviceInstance = new ServiceInstance();
      serviceInstance.setInstance(persistenceNode.getInstance());
      serviceInstance.setApplicationName(persistenceNode.getName());
      serviceInstanceList.add(serviceInstance);
    }
    return serviceInstanceList;
  }

  @Override
  public List<ServiceInstance> getNodeByLabels(List<Integer> labelIds) {
    if (labelIds == null || labelIds.isEmpty()) return Collections.emptyList();
    List<String> instances = labelManagerMapper.getInstanceIdsByLabelIds(labelIds);
    List<PersistenceNode> persistenceNodeList = nodeManagerMapper.getNodesByInstances(instances);

    List<ServiceInstance> serviceInstanceList = new ArrayList<>();
    for (PersistenceNode persistenceNode : persistenceNodeList) {
      ServiceInstance serviceInstance = new ServiceInstance();
      serviceInstance.setInstance(persistenceNode.getInstance());
      serviceInstance.setApplicationName(persistenceNode.getName());
      serviceInstanceList.add(serviceInstance);
    }
    return serviceInstanceList;
  }

  @Override
  public void addLabelToUser(String userName, List<Integer> labelIds) {
    labelManagerMapper.addLabelsByUser(userName, labelIds);
  }

  @Override
  public void removeLabelFromUser(String userName, List<Integer> labelIds) {
    labelManagerMapper.deleteLabelIdsByUser(userName, labelIds);
  }

  @Override
  public List<String> getUserByLabel(int label) {
    List<String> userNames = labelManagerMapper.getUserNameByLabelId(label);
    return userNames;
  }

  @Override
  public List<String> getUserByLabels(List<Integer> labelIds) {
    List<String> userNames = labelManagerMapper.getUserNamesByLabelIds(labelIds);
    return userNames;
  }

  @Override
  public List<PersistenceLabel> getLabelsByUser(String userName) {
    List<PersistenceLabel> persistenceLabelList = labelManagerMapper.getLabelsByUser(userName);
    // to do sure actual type
    return persistenceLabelList;
  }

  @Override
  public Map<PersistenceLabel, List<ServiceInstance>> getNodeRelationsByLabels(
      List<PersistenceLabel> persistenceLabels) {
    // 空集合过滤& 转换
    if (PersistenceUtils.persistenceLabelListIsEmpty(persistenceLabels))
      return Collections.emptyMap();
    try {
      /*Map<String, Map<String, String>> keyValueMap = PersistenceUtils.filterEmptyPersistenceLabelList(persistenceLabels).stream().collect(Collectors.toMap(PersistenceLabel::getLabelKey, PersistenceLabel::getValue));
      String dimType = Label.ValueRelation.ALL.name();
      List<Map<String, Object>> nodeRelationsByLabels = labelManagerMapper.dimListNodeRelationsByKeyValueMap(keyValueMap, dimType);*/
      List<Map<String, Object>> nodeRelationsByLabels =
          labelManagerMapper.getNodeRelationsByLabels(persistenceLabels);
      List<Tunple<PersistenceLabel, ServiceInstance>> arrays =
          new ArrayList<Tunple<PersistenceLabel, ServiceInstance>>();
      for (Map<String, Object> nodeRelationsByLabel : nodeRelationsByLabels) {
        ServiceInstance serviceInstance = new ServiceInstance();
        PersistenceLabel persistenceLabel = new PersistenceLabel();
        BeanUtils.populate(serviceInstance, nodeRelationsByLabel);
        BeanUtils.populate(persistenceLabel, nodeRelationsByLabel);
        PersistenceUtils.setValue(persistenceLabel);
        arrays.add(new Tunple(persistenceLabel, serviceInstance));
      }
      return arrays.stream()
          .collect(Collectors.groupingBy(Tunple::getKey))
          .entrySet()
          .stream()
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey,
                  f -> f.getValue().stream().map(Tunple::getValue).collect(Collectors.toList())));
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new PersistenceWarnException(
          BEANUTILS_POPULATE_FAILED.getErrorCode(), BEANUTILS_POPULATE_FAILED.getErrorDesc(), e);
    }
  }

  @Override
  public Map<ServiceInstance, List<PersistenceLabel>> getLabelRelationsByServiceInstance(
      List<ServiceInstance> serviceInstances) {
    if (CollectionUtils.isEmpty(serviceInstances)) return Collections.emptyMap();
    Map<ServiceInstance, List<PersistenceLabel>> resultMap = new HashMap<>();
    List<Map<String, Object>> nodeRelationsByLabels =
        listLabelRelationByServiceInstance(
            serviceInstances, RMConfiguration.LABEL_SERVICE_PARTITION_NUM.getValue());
    logger.info("list label relation end, with size: {}", nodeRelationsByLabels.size());
    Map<String, List<Map<String, Object>>> groupByInstanceMap =
        nodeRelationsByLabels.stream()
            .collect(
                Collectors.groupingBy(
                    nodeRelationsByLabel -> nodeRelationsByLabel.get("instance").toString()));
    serviceInstances.stream()
        .filter(serviceInstance -> groupByInstanceMap.containsKey(serviceInstance.getInstance()))
        .forEach(
            serviceInstance -> {
              List<PersistenceLabel> persistenceLabelList = new ArrayList<>();
              groupByInstanceMap
                  .get(serviceInstance.getInstance())
                  .forEach(
                      map -> {
                        try {
                          PersistenceLabel persistenceLabel = new PersistenceLabel();
                          BeanUtils.populate(persistenceLabel, map);
                          PersistenceUtils.setValue(persistenceLabel);
                          persistenceLabelList.add(persistenceLabel);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                          throw new PersistenceWarnException(
                              BEANUTILS_POPULATE_FAILED.getErrorCode(),
                              BEANUTILS_POPULATE_FAILED.getErrorDesc(),
                              e);
                        }
                      });
              resultMap.put(serviceInstance, persistenceLabelList);
            });
    return resultMap;
  }

  @Override
  public PersistenceLabel getLabelByKeyValue(String labelKey, String stringValue) {
    return labelManagerMapper.getLabelByKeyValue(labelKey, stringValue);
  }

  @Override
  public List<ServiceInstance> getNodeByLabelKeyValue(String labelKey, String stringValue) {
    return labelManagerMapper.getNodeByLabelKeyValue(labelKey, stringValue);
  }

  public List<Map<String, Object>> listLabelRelationByServiceInstance(
      List<ServiceInstance> nodes, int batchSize) {

    return Lists.partition(nodes, batchSize).stream()
        .map(batch -> labelManagerMapper.listLabelRelationByServiceInstance(batch))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }
}
