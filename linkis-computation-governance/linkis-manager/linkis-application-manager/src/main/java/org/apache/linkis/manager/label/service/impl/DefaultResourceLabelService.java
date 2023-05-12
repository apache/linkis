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

import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.common.utils.ResourceUtils;
import org.apache.linkis.manager.label.LabelManagerUtils;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.label.service.ResourceLabelService;
import org.apache.linkis.manager.persistence.ResourceLabelPersistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultResourceLabelService implements ResourceLabelService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultResourceLabelService.class);

  @Autowired private ResourceLabelPersistence resourceLabelPersistence;

  private final LabelBuilderFactory labelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Override
  public List<Label<?>> getResourceLabels(List<Label<?>> labels) {
    List<LabelKeyValue> labelKeyValueList =
        labels.stream()
            .flatMap(
                label -> {
                  PersistenceLabel persistenceLabel =
                      LabelManagerUtils.convertPersistenceLabel(label);
                  Map<String, String> labelMap = persistenceLabel.getValue();
                  return labelMap.entrySet().stream()
                      .map(keyValue -> new LabelKeyValue(keyValue.getKey(), keyValue.getValue()));
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    List<PersistenceLabel> resourceLabels =
        resourceLabelPersistence.getResourceLabels(labelKeyValueList);

    return resourceLabels.stream()
        .map(
            label -> {
              Label<?> labelInner =
                  labelBuilderFactory.createLabel(label.getLabelKey(), label.getValue());
              return labelInner;
            })
        .collect(Collectors.toList());
  }

  @Override
  public void setResourceToLabel(Label<?> label, NodeResource resource, String source) {
    PersistenceResource persistResource = ResourceUtils.toPersistenceResource(resource);
    persistResource.setUpdator(source);
    resourceLabelPersistence.setResourceToLabel(
        LabelManagerUtils.convertPersistenceLabel(label), persistResource);
  }

  /**
   * 通过Label 返回对应的Resource
   *
   * @param label
   * @return
   */
  @Override
  public NodeResource getResourceByLabel(Label<?> label) {
    if (label == null) {
      return null;
    }
    List<PersistenceResource> persistenceResources;
    if (label instanceof PersistenceLabel) {
      persistenceResources = resourceLabelPersistence.getResourceByLabel((PersistenceLabel) label);
    } else {
      persistenceResources =
          resourceLabelPersistence.getResourceByLabel(
              LabelManagerUtils.convertPersistenceLabel(label));
    }
    if (persistenceResources.isEmpty()) {
      return null;
    } else {
      // TODO: 判断取哪个resource
      return ResourceUtils.fromPersistenceResource(persistenceResources.get(0));
    }
  }

  @Override
  public PersistenceResource getPersistenceResourceByLabel(Label<?> label) {
    if (label == null) {
      return null;
    }
    List<PersistenceResource> persistenceResources;
    if (label instanceof PersistenceLabel) {
      persistenceResources = resourceLabelPersistence.getResourceByLabel((PersistenceLabel) label);
    } else {
      persistenceResources =
          resourceLabelPersistence.getResourceByLabel(
              LabelManagerUtils.convertPersistenceLabel(label));
    }
    if (persistenceResources.isEmpty()) {
      return null;
    } else {
      return persistenceResources.get(0);
    }
  }

  @Override
  public void removeResourceByLabel(Label<?> label) {
    resourceLabelPersistence.removeResourceByLabel(
        LabelManagerUtils.convertPersistenceLabel(label));
  }

  @Override
  public void removeResourceByLabels(List<Label<?>> labels) {
    resourceLabelPersistence.removeResourceByLabels(
        labels.stream()
            .map(LabelManagerUtils::convertPersistenceLabel)
            .collect(Collectors.toList()));
  }

  @Override
  public void setEngineConnResourceToLabel(
      Label<?> label, NodeResource nodeResource, String source) {
    if (label instanceof EngineInstanceLabel) {
      PersistenceResource resource = ResourceUtils.toPersistenceResource(nodeResource);
      resource.setTicketId(((EngineInstanceLabel) label).getInstance());
      PersistenceLabel resourceLabel = LabelManagerUtils.convertPersistenceLabel(label);
      resource.setUpdator(source);
      resourceLabelPersistence.setResourceToLabel(resourceLabel, resource);
    }
  }
}
