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
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.dao.LabelManagerMapper;
import org.apache.linkis.manager.dao.NodeManagerMapper;
import org.apache.linkis.manager.dao.ResourceManagerMapper;
import org.apache.linkis.manager.exception.PersistenceErrorException;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.persistence.ResourceManagerPersistence;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultResourceManagerPersistence implements ResourceManagerPersistence {

  private ResourceManagerMapper resourceManagerMapper;

  private NodeManagerMapper nodeManagerMapper;

  private LabelManagerMapper labelManagerMapper;

  public ResourceManagerMapper getResourceManagerMapper() {
    return resourceManagerMapper;
  }

  public void setResourceManagerMapper(ResourceManagerMapper resourceManagerMapper) {
    this.resourceManagerMapper = resourceManagerMapper;
  }

  public NodeManagerMapper getNodeManagerMapper() {
    return nodeManagerMapper;
  }

  public void setNodeManagerMapper(NodeManagerMapper nodeManagerMapper) {
    this.nodeManagerMapper = nodeManagerMapper;
  }

  public LabelManagerMapper getLabelManagerMapper() {
    return labelManagerMapper;
  }

  public void setLabelManagerMapper(LabelManagerMapper labelManagerMapper) {
    this.labelManagerMapper = labelManagerMapper;
  }

  @Override
  public void registerResource(PersistenceResource persistenceResource)
      throws PersistenceErrorException {
    // 直接注册
    resourceManagerMapper.registerResource(persistenceResource);
  }

  @Override
  public void registerResource(
      ServiceInstance serviceInstance, PersistenceResource persistenceResource)
      throws PersistenceErrorException {
    // 保存资源
    resourceManagerMapper.registerResource(persistenceResource);
    // 保存标签与资源的关系表
    int resourceId = persistenceResource.getId();

    // int instanceId = nodeManagerMapper.getNodeInstanceId(serviceInstance.getInstance(),
    // serviceInstance.getApplicationName());
    List<Integer> labelIds =
        labelManagerMapper.getLabelIdsByInstance(serviceInstance.getInstance());

    labelManagerMapper.addLabelsAndResource(resourceId, labelIds);
  }

  @Override
  public List<PersistenceResource> getResourceByLabel(Label label)
      throws PersistenceErrorException {
    String labelKey = label.getLabelKey();
    String stringValue = label.getStringValue();
    List<PersistenceResource> persistenceResourceList =
        labelManagerMapper.getResourcesByLabel(labelKey, stringValue);
    return persistenceResourceList;
  }

  @Override
  public List<PersistenceResource> getResourceByLabels(List<? extends Label> labels) {
    if (CollectionUtils.isNotEmpty(labels)) {
      return labelManagerMapper.getResourcesByLabels(labels);
    } else {
      return new ArrayList<PersistenceResource>();
    }
  }

  @Override
  public List<PersistenceResource> getResourceByUser(String user) {
    List<PersistenceResource> persistenceResourceList =
        resourceManagerMapper.getResourceByUserName(user);
    return persistenceResourceList;
  }

  @Override
  public List<PersistenceResource> getResourceByServiceInstance(
      ServiceInstance serviceInstance, String resourceType) throws PersistenceErrorException {
    List<PersistenceResource> persistenceResourceList =
        resourceManagerMapper.getResourceByInstanceAndResourceType(
            serviceInstance.getInstance(), resourceType);
    return persistenceResourceList;
  }

  @Override
  public List<PersistenceResource> getResourceByServiceInstance(ServiceInstance serviceInstance)
      throws PersistenceErrorException {
    List<PersistenceResource> persistenceResourceList =
        resourceManagerMapper.getResourceByServiceInstance(serviceInstance.getInstance());
    return persistenceResourceList;
  }

  @Override
  public void deleteServiceInstanceResource(ServiceInstance serviceInstance)
      throws PersistenceErrorException {
    // 移除资源
    resourceManagerMapper.deleteResourceByInstance(serviceInstance.getInstance());
    // 移除关系
    resourceManagerMapper.deleteResourceAndLabelId(serviceInstance.getInstance());
  }

  @Override
  public void deleteExpiredTicketIdResource(String ticketId) throws PersistenceErrorException {
    // 关联表-标签资源表 删除
    labelManagerMapper.deleteLabelResourceByByTicketId(ticketId);
    // 删除资源表
    resourceManagerMapper.deleteResourceByTicketId(ticketId);
  }

  @Override
  public void nodeResourceUpdate(
      ServiceInstance serviceInstance, PersistenceResource persistenceResource)
      throws PersistenceErrorException {
    int resourceId =
        resourceManagerMapper.getNodeResourceUpdateResourceId(serviceInstance.getInstance());
    resourceManagerMapper.nodeResourceUpdateByResourceId(resourceId, persistenceResource);
  }

  @Override
  public PersistenceResource getNodeResourceByTicketId(String ticketId) {
    PersistenceResource persistenceResource =
        resourceManagerMapper.getNodeResourceByTicketId(ticketId);
    return persistenceResource;
  }

  @Override
  public void nodeResourceUpdate(String ticketId, PersistenceResource persistenceResource) {
    resourceManagerMapper.nodeResourceUpdate(ticketId, persistenceResource);
  }

  @Override
  public List<PersistenceLabel> getLabelsByTicketId(String ticketId) {
    List<PersistenceLabel> persistenceLabelList =
        resourceManagerMapper.getLabelsByTicketId(ticketId);
    return persistenceLabelList;
  }

  @Override
  public void lockResource(List<Integer> labelIds, PersistenceResource persistenceResource) {
    resourceManagerMapper.registerResource(persistenceResource);
    int resourceId = persistenceResource.getId();
    labelManagerMapper.addLabelsAndResource(resourceId, labelIds);
  }

  @Override
  public void deleteResourceById(List<Integer> id) {
    resourceManagerMapper.deleteResourceById(id);
  }

  @Override
  public void deleteResourceRelByResourceId(List<Integer> id) {
    resourceManagerMapper.deleteResourceRelByResourceId(id);
  }

  @Override
  public PersistenceResource getNodeResourceById(Integer id) {
    PersistenceResource resource = resourceManagerMapper.getResourceById(id);
    return resource;
  }
}
