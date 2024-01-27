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

import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.CommonNodeResource;
import org.apache.linkis.manager.common.entity.resource.Resource;
import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.label.builder.CombinedLabelBuilder;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.ResourceManagerPersistence;
import org.apache.linkis.manager.rm.restful.vo.UserCreatorEngineType;
import org.apache.linkis.manager.rm.service.LabelResourceService;
import org.apache.linkis.manager.rm.utils.UserConfiguration;
import org.apache.linkis.server.BDPJettyServerHelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UserResourceService {
  private static final Logger logger = LoggerFactory.getLogger(UserResourceService.class);

  @Autowired private LabelResourceService labelResourceService;

  @Autowired private ResourceManagerPersistence resourceManagerPersistence;

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  private final Gson gson = BDPJettyServerHelper.gson();

  private final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  private final CombinedLabelBuilder combinedLabelBuilder = new CombinedLabelBuilder();

  @Transactional
  public void resetUserResource(Integer resourceId) {
    PersistenceResource resource = resourceManagerPersistence.getNodeResourceById(resourceId);
    List<PersistenceLabel> resourceLabel = labelManagerPersistence.getLabelByResource(resource);
    if (resource != null && !CollectionUtils.isEmpty(resourceLabel)) {
      UserCreatorEngineType userCreatorEngineType =
          gson.fromJson(resourceLabel.get(0).getStringValue(), UserCreatorEngineType.class);
      CommonNodeResource labelResource = new CommonNodeResource();
      ResourceType resourceType = ResourceType.valueOf(resource.getResourceType());
      labelResource.setResourceType(resourceType);
      labelResource.setUsedResource(Resource.initResource(resourceType));
      labelResource.setLockedResource(Resource.initResource(resourceType));
      UserCreatorLabel userCreatorLabel = labelFactory.createLabel(UserCreatorLabel.class);
      userCreatorLabel.setUser(userCreatorEngineType.getUser());
      userCreatorLabel.setCreator(userCreatorEngineType.getCreator());
      EngineTypeLabel engineTypeLabel = labelFactory.createLabel(EngineTypeLabel.class);
      engineTypeLabel.setEngineType(userCreatorEngineType.getEngineType());
      engineTypeLabel.setVersion(userCreatorEngineType.getVersion());
      Resource configuredResource =
          UserConfiguration.getUserConfiguredResource(
              resourceType, userCreatorLabel, engineTypeLabel);
      labelResource.setMaxResource(configuredResource);
      labelResource.setMinResource(Resource.initResource(labelResource.getResourceType()));
      labelResource.setLeftResource(
          labelResource
              .getMaxResource()
              .minus(labelResource.getUsedResource())
              .minus(labelResource.getLockedResource()));
      List<Integer> idList = new ArrayList<>();
      idList.add(resourceId);
      resourceManagerPersistence.deleteResourceById(idList);
      resourceManagerPersistence.deleteResourceRelByResourceId(idList);
      List<Label<?>> labelList = new ArrayList<>();
      labelList.add(userCreatorLabel);
      labelList.add(engineTypeLabel);

      Label<?> combinedLabel = combinedLabelBuilder.build("", labelList);
      labelResourceService.setLabelResource(
          resourceLabel.get(0), labelResource, combinedLabel.getStringValue());
    }
  }

  @Transactional
  public void resetAllUserResource(String combinedLabelKey) {
    List<PersistenceLabelRel> userLabels =
        labelManagerPersistence.getLabelByPattern("%", combinedLabelKey, 0, 0);
    List<Integer> resourceIdList = new ArrayList<>();
    for (PersistenceLabelRel userLabel : userLabels) {
      if (StringUtils.isNotBlank(userLabel.getStringValue())) {
        resourceIdList.add(userLabel.getResourceId());
      }
    }
    resourceManagerPersistence.deleteResourceById(resourceIdList);
    resourceManagerPersistence.deleteResourceRelByResourceId(resourceIdList);
  }
}
