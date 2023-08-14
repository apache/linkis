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

package org.apache.linkis.manager.rm.service.impl;

import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.common.utils.ResourceUtils;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.service.ResourceLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.persistence.ResourceManagerPersistence;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.service.LabelResourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LabelResourceServiceImpl extends LabelResourceService {

  private static final Logger logger = LoggerFactory.getLogger(LabelResourceServiceImpl.class);

  @Autowired private ResourceManagerPersistence resourceManagerPersistence;

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  @Autowired private ResourceLabelService resourceLabelService;

  private final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Override
  public NodeResource getLabelResource(Label<?> label) {
    return resourceLabelService.getResourceByLabel(label);
  }

  @Override
  public void setLabelResource(Label<?> label, NodeResource nodeResource, String source) {
    resourceLabelService.setResourceToLabel(label, nodeResource, source);
  }

  @Override
  public NodeResource[] getResourcesByUser(String user) {
    List<NodeResource> nodeResourceList = new ArrayList<>();
    resourceManagerPersistence
        .getResourceByUser(user)
        .forEach(
            persistenceResource ->
                nodeResourceList.add(ResourceUtils.fromPersistenceResource(persistenceResource)));
    return nodeResourceList.toArray(new NodeResource[0]);
  }

  @Override
  public RMLabelContainer enrichLabels(List<Label<?>> labels) {
    return new RMLabelContainer(labels);
  }

  @Override
  public void removeResourceByLabel(Label<?> label) {
    resourceLabelService.removeResourceByLabel(label);
  }

  @Override
  public void setEngineConnLabelResource(Label<?> label, NodeResource nodeResource, String source) {
    resourceLabelService.setEngineConnResourceToLabel(label, nodeResource, source);
  }

  @Override
  public Label<?>[] getLabelsByResource(PersistenceResource resource) {
    return labelManagerPersistence.getLabelByResource(resource).stream()
        .map(label -> labelFactory.createLabel(label.getLabelKey(), label.getValue()))
        .toArray(Label[]::new);
  }

  @Override
  public PersistenceResource getPersistenceResource(Label<?> label) {
    return resourceLabelService.getPersistenceResourceByLabel(label);
  }
}
