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

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.label.LabelManagerUtils;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.linkis.manager.label.service.UserLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DefaultUserLabelService implements UserLabelService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultUserLabelService.class);

  @Autowired LabelManagerPersistence labelManagerPersistence;

  private final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Override
  public void addLabelToUser(String user, List<Label<?>> labels) {
    // 逻辑基本同 addLabelsToNode
    labels.forEach(label -> addLabelToUser(user, label));
  }

  @Override
  public void addLabelToUser(String user, Label<?> label) {
    // instance 存在
    // 1.插入linkis_manager_label 表，这里表应该有唯一约束，key和valueStr　调用Persistence的addLabel即可，忽略duplicateKey异常
    PersistenceLabel persistenceLabel = LabelManagerUtils.convertPersistenceLabel(label);
    try {
      labelManagerPersistence.addLabel(persistenceLabel);
    } catch (Exception e) {
      logger.warn("error adding label: " + e.getMessage());
    }

    // 2.查询出当前label的id
    List<PersistenceLabel> dbLabels = labelManagerPersistence.getLabelsByKey(label.getLabelKey());
    PersistenceLabel dbLabel =
        dbLabels.stream()
            .filter(l -> l.getStringValue().equals(persistenceLabel.getStringValue()))
            .findFirst()
            .orElseThrow(
                () ->
                    new LabelErrorException(
                        LabelCommonErrorCodeSummary.UPDATE_LABEL_FAILED.getErrorCode(),
                        LabelCommonErrorCodeSummary.UPDATE_LABEL_FAILED.getErrorDesc()));

    // 3.根据usr 找出当前关联当前user的所有labels,看下有没和当前key重复的
    List<PersistenceLabel> userRelationLabels = labelManagerPersistence.getLabelsByUser(user);
    Optional<PersistenceLabel> duplicatedKeyLabelOptional =
        userRelationLabels.stream()
            .filter(l -> l.getLabelKey().equals(dbLabel.getLabelKey()))
            .findFirst();

    // 4.找出重复key,删除这个relation
    if (duplicatedKeyLabelOptional.isPresent()) {
      PersistenceLabel duplicatedKeyLabel = duplicatedKeyLabelOptional.get();
      labelManagerPersistence.removeLabelFromUser(
          user, Lists.newArrayList(duplicatedKeyLabel.getId()));
      userRelationLabels.remove(duplicatedKeyLabel);
    }

    // 5.插入新的relation 需要抛出duplicateKey异常，回滚
    labelManagerPersistence.addLabelToUser(user, Lists.newArrayList(dbLabel.getId()));

    // 6.重新查询，确认更新，如果没对上，抛出异常，回滚
    userRelationLabels.add(dbLabel);
    List<PersistenceLabel> newUserRelationLabels = labelManagerPersistence.getLabelsByUser(user);

    if (newUserRelationLabels.size() != userRelationLabels.size()
        || !newUserRelationLabels.stream()
            .map(PersistenceLabel::getId)
            .collect(Collectors.toList())
            .containsAll(
                userRelationLabels.stream()
                    .map(PersistenceLabel::getId)
                    .collect(Collectors.toList()))) {
      throw new LabelErrorException(
          LabelCommonErrorCodeSummary.UPDATE_LABEL_FAILED.getErrorCode(),
          LabelCommonErrorCodeSummary.UPDATE_LABEL_FAILED.getErrorDesc());
    }
  }

  @Override
  public void removeLabelFromUser(String user, List<Label<?>> labels) {
    // 这里前提是表中保证了同个key，只会有最新的value保存在数据库中
    List<PersistenceLabel> dbLabels = labelManagerPersistence.getLabelsByUser(user);

    List<Integer> labelIds = new ArrayList<>();
    for (Label<?> label : labels) {
      Optional<PersistenceLabel> dbLabelOptional =
          dbLabels.stream().filter(l -> l.getLabelKey().equals(label.getLabelKey())).findFirst();

      dbLabelOptional.ifPresent(persistenceLabel -> labelIds.add(persistenceLabel.getId()));
    }
    labelManagerPersistence.removeLabelFromUser(user, labelIds);
  }

  @Override
  public List<String> getUserByLabel(Label<?> label) {
    // TODO: persistence 需要提供 key，valueString 找到相关label的方法
    // 1.找出当前label 对应的数据库的label
    List<PersistenceLabel> labelsByKey =
        labelManagerPersistence.getLabelsByKey(label.getLabelKey());
    List<Integer> labelIds =
        labelsByKey.stream()
            .filter(labelInner -> labelInner.getStringValue().equals(label.getStringValue()))
            .map(PersistenceLabel::getId)
            .collect(Collectors.toList());

    // 2.获取用户并且去重
    return labelManagerPersistence.getUserByLabels(labelIds);
  }

  public List<String> getUserByLabels(List<Label<?>> labels) {
    // 去重
    return labels.stream()
        .map(this::getUserByLabel)
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  public List<Label<?>> getUserLabels(String user) {
    List<PersistenceLabel> labelsByUser = labelManagerPersistence.getLabelsByUser(user);
    List<Label<?>> labels = new ArrayList<>();
    for (PersistenceLabel persistenceLabel : labelsByUser) {
      Label<?> label =
          labelFactory.createLabel(persistenceLabel.getLabelKey(), persistenceLabel.getValue());
      labels.add(label);
    }
    return labels;
  }
}
