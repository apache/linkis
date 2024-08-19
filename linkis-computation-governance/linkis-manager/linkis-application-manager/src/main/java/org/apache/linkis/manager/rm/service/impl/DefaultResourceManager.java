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

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.manager.am.service.engine.EngineStopService;
import org.apache.linkis.manager.am.util.LinkisUtils;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.metrics.NodeMetrics;
import org.apache.linkis.manager.common.entity.node.AMEMNode;
import org.apache.linkis.manager.common.entity.node.AMEngineNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.node.InfoRMNode;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.common.utils.ManagerUtils;
import org.apache.linkis.manager.common.utils.ResourceUtils;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.CombinedLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineInstanceLabel;
import org.apache.linkis.manager.label.service.NodeLabelService;
import org.apache.linkis.manager.persistence.LabelManagerPersistence;
import org.apache.linkis.manager.persistence.NodeManagerPersistence;
import org.apache.linkis.manager.persistence.NodeMetricManagerPersistence;
import org.apache.linkis.manager.persistence.ResourceManagerPersistence;
import org.apache.linkis.manager.rm.AvailableResource;
import org.apache.linkis.manager.rm.NotEnoughResource;
import org.apache.linkis.manager.rm.ResourceInfo;
import org.apache.linkis.manager.rm.ResultResource;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.entity.LabelResourceMapping;
import org.apache.linkis.manager.rm.entity.ResourceOperationType;
import org.apache.linkis.manager.rm.exception.RMErrorCode;
import org.apache.linkis.manager.rm.external.service.ExternalResourceService;
import org.apache.linkis.manager.rm.service.LabelResourceService;
import org.apache.linkis.manager.rm.service.RequestResourceService;
import org.apache.linkis.manager.rm.service.ResourceLockService;
import org.apache.linkis.manager.rm.service.ResourceManager;
import org.apache.linkis.manager.rm.utils.RMUtils;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DefaultResourceManager extends ResourceManager implements InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(DefaultResourceManager.class);

  private LabelBuilderFactory labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

  @Autowired private ResourceManagerPersistence resourceManagerPersistence;

  @Autowired private NodeManagerPersistence nodeManagerPersistence;

  @Autowired private ResourceLockService resourceLockService;

  @Autowired private LabelResourceService labelResourceService;

  @Autowired private ExternalResourceService externalResourceService;

  @Autowired private ResourceLogService resourceLogService;

  @Autowired private LabelManagerPersistence labelManagerPersistence;

  @Autowired private NodeMetricManagerPersistence nodeMetricManagerPersistence;

  @Autowired private NodeLabelService nodeLabelService;

  @Autowired private EngineStopService engineStopService;

  private RequestResourceService[] requestResourceServices;

  @Override
  public void afterPropertiesSet() throws Exception {
    requestResourceServices =
        new RequestResourceService[] {
          new DefaultReqResourceService(labelResourceService),
          new DriverAndYarnReqResourceService(labelResourceService, externalResourceService),
          new DriverAndKubernetesReqResourceService(labelResourceService, externalResourceService)
        };

    // submit force release timeout lock job
    LinkisUtils.defaultScheduler.scheduleAtFixedRate(
        () -> {
          logger.info("Start force release timeout locks");
          resourceLockService.clearTimeoutLock(
              RMConfiguration.LOCK_RELEASE_TIMEOUT.getValue().toLong());
        },
        RMConfiguration.LOCK_RELEASE_CHECK_INTERVAL.getValue().toLong(),
        RMConfiguration.LOCK_RELEASE_CHECK_INTERVAL.getValue().toLong(),
        TimeUnit.MILLISECONDS);
  }

  /**
   * The registration method is mainly used to notify all RM nodes (including the node)
   * 该注册方法，主要是用于通知所有的RM节点（包括本节点）
   */
  @Override
  public void register(ServiceInstance serviceInstance, NodeResource resource) {
    logger.info("Start processing registration of ServiceInstance: " + serviceInstance.toString());
    EMInstanceLabel eMInstanceLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory().createLabel(EMInstanceLabel.class);
    eMInstanceLabel.setServiceName(serviceInstance.getApplicationName());
    eMInstanceLabel.setInstance(serviceInstance.getInstance());

    NodeResource emResource = labelResourceService.getLabelResource(eMInstanceLabel);
    boolean registerResourceFlag = true;
    if (emResource != null) {
      registerResourceFlag = false;
      logger.warn("ECM {} has been registered, resource is {}.", serviceInstance, emResource);
      Resource leftResource = emResource.getLeftResource();
      if (leftResource != null && Resource.getZeroResource(leftResource).moreThan(leftResource)) {
        logger.warn(
            "ECM {} has been registered, but left Resource <0 need to register resource.",
            serviceInstance);
        registerResourceFlag = true;
      }
      Resource usedResource = emResource.getLockedResource().add(emResource.getUsedResource());
      if (usedResource.moreThan(emResource.getMaxResource())) {
        logger.warn(
            "ECM {}  has been registered, but usedResource > MaxResource  need to register resource.",
            serviceInstance);
        registerResourceFlag = true;
      }
      if (!(resource.getMaxResource().equalsTo(emResource.getMaxResource()))) {
        logger.warn(
            "ECM {} has been registered, but inconsistent newly registered resources  need to register resource.",
            serviceInstance);
        registerResourceFlag = true;
      }
    }

    if (!registerResourceFlag) {
      logger.warn("ECM {} has been registered, skip register resource.", serviceInstance);
      return;
    }
    PersistenceLock lock = tryLockOneLabel(eMInstanceLabel, -1, LinkisUtils.getJvmUser());
    try {
      labelResourceService.removeResourceByLabel(eMInstanceLabel);
      labelResourceService.setLabelResource(
          eMInstanceLabel, resource, eMInstanceLabel.getStringValue());
    } catch (Exception exception) {
      resourceLogService.failed(
          ChangeType.ECM_INIT, resource.getMaxResource(), null, eMInstanceLabel, exception);
      throw exception;
    } finally {
      resourceLockService.unLock(lock);
    }
  }

  /**
   * The registration method is mainly used to notify all RM nodes (including the node), and the
   *
   * <p>instance is offline. 该注册方法，主要是用于通知所有的RM节点（包括本节点），下线该实例
   */
  @Override
  public void unregister(ServiceInstance serviceInstance) {

    EMInstanceLabel eMInstanceLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory().createLabel(EMInstanceLabel.class);
    eMInstanceLabel.setServiceName(serviceInstance.getApplicationName());
    eMInstanceLabel.setInstance(serviceInstance.getInstance());
    PersistenceLock lock = tryLockOneLabel(eMInstanceLabel, -1, LinkisUtils.getJvmUser());
    try {
      labelResourceService.removeResourceByLabel(eMInstanceLabel);
    } catch (Exception exception) {
      resourceLogService.failed(
          ChangeType.ECM_CLEAR,
          Resource.initResource(ResourceType.LoadInstance),
          null,
          eMInstanceLabel,
          exception);
    } finally {
      resourceLockService.unLock(lock);
      logger.info("Finished to clear ecm resource:" + serviceInstance.toString());
    }
    logger.info("Finished to clear ec for ecm " + serviceInstance.toString());
  }

  /**
   * Request resources, if not successful, return directly 请求资源，如果不成功，直接返回
   *
   * @param labels
   * @param resource
   * @return
   */
  @Override
  public ResultResource requestResource(List<Label<?>> labels, NodeResource resource) {
    return requestResource(labels, resource, -1);
  }

  /**
   * Request resources and wait for a certain amount of time until the requested resource is met
   * 请求资源，并等待一定的时间，直到满足请求的资源
   *
   * @param labels
   * @param resource
   * @param wait
   * @return
   */
  @Override
  public ResultResource requestResource(List<Label<?>> labels, NodeResource resource, long wait) {
    RMLabelContainer labelContainer = labelResourceService.enrichLabels(labels);

    // check resource with lock
    RequestResourceService requestResourceService =
        getRequestResourceService(resource.getResourceType());
    resource.setLockedResource(resource.getMinResource());
    List<Label<?>> resourceLabels = labelContainer.getResourceLabels();

    List<PersistenceLock> persistenceLocks = new ArrayList<>();
    EMInstanceLabel emInstanceLabel = labelContainer.getEMInstanceLabel();
    CombinedLabel combinedLabel = labelContainer.getCombinedResourceLabel();

    try {
      // check ecm resource if not enough return
      try {
        labelContainer.setCurrentLabel(emInstanceLabel);
        if (!requestResourceService.canRequest(labelContainer, resource)) {
          return new NotEnoughResource(
              String.format("Labels:%s not enough resource", emInstanceLabel.getStringValue()));
        }
      } catch (RMWarnException exception) {
        return new NotEnoughResource(exception.getMessage());
      } catch (Exception exception) {
        throw exception;
      }

      // lock userCreatorEngineTypeLabel
      persistenceLocks.add(
          tryLockOneLabel(combinedLabel, wait, labelContainer.getUserCreatorLabel().getUser()));
      try {
        labelContainer.setCurrentLabel(combinedLabel);
        if (!requestResourceService.canRequest(labelContainer, resource)) {
          return new NotEnoughResource(
              String.format("Labels:%s not enough resource", combinedLabel.getStringValue()));
        }
      } catch (RMWarnException exception) {
        return new NotEnoughResource(exception.getMessage());
      } catch (Exception exception) {
        throw exception;
      }

      // lock ecmLabel
      persistenceLocks.add(
          tryLockOneLabel(emInstanceLabel, wait, labelContainer.getUserCreatorLabel().getUser()));

      for (Label<?> label : resourceLabels) {
        labelContainer.setCurrentLabel(label);
        NodeResource labelResource = labelResourceService.getLabelResource(label);
        if (labelResource != null) {
          labelResource.setLeftResource(
              labelResource.getLeftResource().minus(resource.getLockedResource()));
          labelResource.setLockedResource(
              labelResource.getLockedResource().add(resource.getLockedResource()));
          labelResourceService.setLabelResource(
              label, labelResource, labelContainer.getCombinedResourceLabel().getStringValue());
          logger.info(
              String.format(
                  "ResourceChanged:%s --> %s", label.getStringValue(), labelResource.toString()));
          resourceCheck(label, labelResource);
        }
      }
    } finally {
      persistenceLocks.forEach(resourceLockService::unLock);
    }

    // Add EC Node
    String tickedId = RMUtils.getECTicketID();
    AMEMNode emNode = new AMEMNode();
    emNode.setServiceInstance(labelContainer.getEMInstanceLabel().getServiceInstance());
    AMEngineNode engineNode = new AMEngineNode();
    engineNode.setEMNode(emNode);
    engineNode.setServiceInstance(
        ServiceInstance.apply(labelContainer.getEngineServiceName(), tickedId));
    engineNode.setNodeResource(resource);
    engineNode.setTicketId(tickedId);

    nodeManagerPersistence.addEngineNode(engineNode);

    // Add labels
    EngineInstanceLabel engineInstanceLabel =
        LabelBuilderFactoryContext.getLabelBuilderFactory().createLabel(EngineInstanceLabel.class);
    engineInstanceLabel.setServiceName(labelContainer.getEngineServiceName());
    engineInstanceLabel.setInstance(tickedId);

    nodeLabelService.addLabelToNode(engineNode.getServiceInstance(), engineInstanceLabel);

    // add ec resource
    labelResourceService.setEngineConnLabelResource(
        engineInstanceLabel, resource, labelContainer.getCombinedResourceLabel().getStringValue());
    // record engine locked resource
    labelContainer.getLabels().add(engineInstanceLabel);
    resourceLogService.recordUserResourceAction(
        labelContainer,
        tickedId,
        ChangeType.ENGINE_REQUEST,
        resource.getLockedResource(),
        NodeStatus.Starting,
        "");
    PersistenceLabel persistenceLabel =
        labelFactory.convertLabel(engineInstanceLabel, PersistenceLabel.class);
    PersistenceLabel persistenceEngineLabel =
        labelManagerPersistence.getLabelByKeyValue(
            persistenceLabel.getLabelKey(), persistenceLabel.getStringValue());

    // fire timeout check scheduled job
    if (RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue() > 0) {
      LinkisUtils.defaultScheduler.schedule(
          new UnlockTimeoutResourceRunnable(labels, persistenceEngineLabel, tickedId),
          RMConfiguration.RM_WAIT_EVENT_TIME_OUT.getValue(),
          TimeUnit.MILLISECONDS);
    }
    return new AvailableResource(tickedId);
  }

  public RequestResourceService getRequestResourceService(ResourceType resourceType) {
    Optional<RequestResourceService> requestResourceService =
        Arrays.stream(requestResourceServices)
            .filter(service -> service.resourceType() == resourceType)
            .findFirst();

    return requestResourceService.orElse(
        Arrays.stream(requestResourceServices)
            .filter(service -> service.resourceType() == ResourceType.Default)
            .findFirst()
            .get());
  }

  /**
   * When the resource is instantiated, the total amount of resources actually occupied is returned.
   * 当资源被实例化后，返回实际占用的资源总量
   *
   * @param labels
   * @param usedResource
   */
  @Override
  public void resourceUsed(List<Label<?>> labels, NodeResource usedResource) {
    RMLabelContainer labelContainer = labelResourceService.enrichLabels(labels);
    if (null == labelContainer.getEngineInstanceLabel()) {
      throw new RMWarnException(
          RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode(), "engine instance label is null");
    }
    NodeResource lockedResource = null;
    PersistenceResource persistenceResource = null;
    try {
      persistenceResource =
          labelResourceService.getPersistenceResource(labelContainer.getEngineInstanceLabel());
      lockedResource = ResourceUtils.fromPersistenceResource(persistenceResource);
    } catch (NullPointerException e) {
      logger.error(
          String.format(
              "EngineInstanceLabel [%s] cause NullPointerException",
              labelContainer.getEngineInstanceLabel()));
      throw e;
    }
    EngineNode nodeInstance =
        nodeManagerPersistence.getEngineNode(
            labelContainer.getEngineInstanceLabel().getServiceInstance());
    if (nodeInstance == null) {
      throw new RMWarnException(
          RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode(),
          "No serviceInstance found by engine "
              + labelContainer.getEngineInstanceLabel()
              + ", current label resource "
              + lockedResource);
    }

    if (lockedResource == null
        || lockedResource
            .getLockedResource()
            .less(Resource.initResource(lockedResource.getResourceType()))) {
      throw new RMWarnException(
          RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode(),
          "No locked resource found by engine "
              + labelContainer.getEngineInstanceLabel()
              + ", current label resource "
              + lockedResource);
    }

    logger.info(
        String.format(
            "resourceUsed ready:%s, used resource %s",
            labelContainer.getEMInstanceLabel().getServiceInstance(),
            lockedResource.getLockedResource()));
    Resource addedResource =
        Resource.initResource(lockedResource.getResourceType())
            .add(lockedResource.getLockedResource());

    EngineInstanceLabel engineInstanceLabel = labelContainer.getEngineInstanceLabel();

    try {
      lockedResource.setUsedResource(lockedResource.getLockedResource());
      updateYarnApplicationID(usedResource, lockedResource);
      lockedResource.setLockedResource(
          Resource.getZeroResource(lockedResource.getLockedResource()));
      labelResourceService.setLabelResource(
          engineInstanceLabel,
          lockedResource,
          labelContainer.getCombinedResourceLabel().getStringValue());
      resourceLogService.success(
          ChangeType.ENGINE_INIT, lockedResource.getLockedResource(), engineInstanceLabel, null);
    } catch (Exception exception) {
      logger.error(
          String.format(
              "%s used resource failed!, resource: %s",
              engineInstanceLabel.getStringValue(), lockedResource),
          exception);
    }

    Set<LabelResourceMapping> labelResourceSet = new HashSet<>();
    try {
      List<Label<?>> labelList =
          labelContainer.getResourceLabels().stream()
              .filter(
                  label ->
                      !label
                          .getClass()
                          .isAssignableFrom(labelContainer.getEngineInstanceLabel().getClass()))
              .collect(Collectors.toList());
      for (Label<?> label : labelList) {
        PersistenceLock persistenceLock =
            tryLockOneLabel(label, -1, labelContainer.getUserCreatorLabel().getUser());

        LinkisUtils.tryFinally(
            () -> {
              labelContainer.setCurrentLabel(label);
              NodeResource labelResource = labelResourceService.getLabelResource(label);
              if (labelResource != null) {
                labelResource.setLockedResource(
                    labelResource.getLockedResource().minus(addedResource));
                if (null == labelResource.getUsedResource()) {
                  labelResource.setUsedResource(
                      Resource.initResource(labelResource.getResourceType()));
                }
                labelResource.setUsedResource(labelResource.getUsedResource().add(addedResource));
                labelResourceService.setLabelResource(
                    label,
                    labelResource,
                    labelContainer.getCombinedResourceLabel().getStringValue());
                labelResourceSet.add(
                    new LabelResourceMapping(label, addedResource, ResourceOperationType.USED));
                resourceCheck(label, labelResource);
              }
            },
            () -> {
              resourceLockService.unLock(persistenceLock);
            });

        if (label
            .getClass()
            .isAssignableFrom(labelContainer.getCombinedResourceLabel().getClass())) {
          resourceLogService.recordUserResourceAction(
              labelContainer,
              persistenceResource.getTicketId(),
              ChangeType.ENGINE_INIT,
              addedResource,
              NodeStatus.Running,
              null);
        }
      }
    } catch (Exception exception) {
      resourceRollback(labelResourceSet, labelContainer.getUserCreatorLabel().getUser());
      logger.error(
          String.format(
              "%s used resource failed!, resource: %s",
              labelContainer.getEngineInstanceLabel().getStringValue(), lockedResource),
          exception);
    }
  }

  public void resourceCheck(Label<?> label, NodeResource labelResource) {
    if (labelResource != null && label != null) {
      Resource resourceInit = Resource.initResource(labelResource.getResourceType());
      if (labelResource.getLockedResource().less(resourceInit)
          || labelResource.getUsedResource().less(resourceInit)
          || labelResource.getLeftResource().less(resourceInit)) {
        logger.info(
            String.format(
                "found error resource! resource label:%s, resource:%s, please check!",
                label.getStringValue(), labelResource));
      }
    }
  }

  private void updateYarnApplicationID(NodeResource nodeResource, NodeResource lockedResource) {
    if (lockedResource.getUsedResource() instanceof DriverAndYarnResource) {
      DriverAndYarnResource driverAndYarnResource =
          (DriverAndYarnResource) lockedResource.getUsedResource();
      if (nodeResource.getUsedResource() instanceof DriverAndYarnResource) {
        DriverAndYarnResource resource = (DriverAndYarnResource) nodeResource.getUsedResource();
        YarnResource newYarnResource = resource.getYarnResource();
        String applicationId = null != newYarnResource ? newYarnResource.getApplicationId() : null;
        YarnResource oriYarnResource = driverAndYarnResource.getYarnResource();
        DriverAndYarnResource tmpUsedResource =
            new DriverAndYarnResource(
                driverAndYarnResource.getLoadInstanceResource(),
                new YarnResource(
                    oriYarnResource.getQueueMemory(),
                    oriYarnResource.getQueueCores(),
                    oriYarnResource.getQueueInstances(),
                    oriYarnResource.getQueueName(),
                    applicationId));
        lockedResource.setUsedResource(tmpUsedResource);
      }
    } else if (lockedResource.getUsedResource() instanceof YarnResource) {
      YarnResource yarnResource = (YarnResource) lockedResource.getUsedResource();
      if (nodeResource.getUsedResource() instanceof YarnResource) {
        YarnResource resource = (YarnResource) nodeResource.getUsedResource();
        YarnResource tmpYarnResource =
            new YarnResource(
                yarnResource.getQueueMemory(),
                yarnResource.getQueueCores(),
                yarnResource.getQueueInstances(),
                yarnResource.getQueueName(),
                resource.getApplicationId());
        lockedResource.setUsedResource(tmpYarnResource);
      }
    }
  }

  private void resourceRollback(Set<LabelResourceMapping> labelResourceSet, String user) {
    labelResourceSet.forEach(
        labelResourceMapping -> {
          PersistenceLock persistenceLock =
              tryLockOneLabel(labelResourceMapping.getLabel(), -1, user);
          try {
            NodeResource resource =
                labelResourceService.getLabelResource(labelResourceMapping.getLabel());
            switch (labelResourceMapping.getResourceOperationType()) {
              case LOCK:
                resource.setLeftResource(
                    resource.getLeftResource().add(labelResourceMapping.getResource()));
                resource.setLockedResource(
                    resource.getLockedResource().minus(labelResourceMapping.getResource()));
                break;
              case USED:
                resource.setLockedResource(
                    resource.getLeftResource().add(labelResourceMapping.getResource()));
                resource.setUsedResource(
                    resource.getLockedResource().minus(labelResourceMapping.getResource()));
                break;
              default:
                break;
            }
            labelResourceService.setLabelResource(
                labelResourceMapping.getLabel(),
                resource,
                labelResourceMapping.getResourceOperationType().toString());
          } catch (Exception e) {
            logger.error("Failed to roll back resource " + new ArrayList<>(labelResourceSet), e);
          }
          resourceLockService.unLock(persistenceLock);
        });
  }

  private PersistenceLock tryLockOneLabel(Label<?> label, long timeOut, String user) {
    PersistenceLock persistenceLock = new PersistenceLock();
    persistenceLock.setLockObject(label.getStringValue());
    persistenceLock.setCreateTime(new Date());
    persistenceLock.setCreator(user);
    persistenceLock.setUpdateTime(new Date());
    persistenceLock.setUpdator(user);
    boolean locked = resourceLockService.tryLock(persistenceLock, timeOut);
    if (!locked) {
      throw new RMWarnException(
          RMErrorCode.LOCK_LABEL_FAILED.getErrorCode(),
          MessageFormat.format(
              RMErrorCode.LOCK_LABEL_FAILED.getErrorDesc(),
              label.getStringValue(),
              String.valueOf(timeOut)));
    }
    return persistenceLock;
  }

  /**
   * Method called when the resource usage is released 当资源使用完成释放后，调用的方法
   *
   * @param ecNode
   */
  @Override
  public void resourceReleased(EngineNode ecNode) {
    RMLabelContainer labelContainer = labelResourceService.enrichLabels(ecNode.getLabels());
    if (null == labelContainer.getEngineInstanceLabel()) {
      throw new RMWarnException(
          RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode(), "engine instance label is null");
    }

    PersistenceLock instanceLock =
        tryLockOneLabel(
            labelContainer.getEngineInstanceLabel(),
            RMUtils.RM_RESOURCE_LOCK_WAIT_TIME.getValue(),
            labelContainer.getUserCreatorLabel().getUser());

    LinkisUtils.tryFinally(
        () -> {
          PersistenceResource persistenceResource =
              labelResourceService.getPersistenceResource(labelContainer.getEngineInstanceLabel());
          CommonNodeResource usedResource =
              ResourceUtils.fromPersistenceResource(persistenceResource);
          if (usedResource == null) {
            throw new RMWarnException(
                RMErrorCode.LABEL_RESOURCE_NOT_FOUND.getErrorCode(),
                String.format(
                    "No used resource found by engine %s",
                    labelContainer.getEngineInstanceLabel()));
          }
          logger.info(
              String.format(
                  "resourceRelease ready:%s,current node resource%s",
                  labelContainer.getEngineInstanceLabel().getServiceInstance(), usedResource));

          NodeStatus status =
              ecNode.getNodeStatus() == null
                  ? getNodeStatus(labelContainer.getEngineInstanceLabel())
                  : ecNode.getNodeStatus();

          List<Label<?>> labels =
              labelContainer.getResourceLabels().stream()
                  .filter(label -> !(label instanceof EngineInstanceLabel))
                  .collect(Collectors.toList());

          labels.forEach(
              label -> {
                PersistenceLock persistenceLock =
                    tryLockOneLabel(
                        label,
                        RMUtils.RM_RESOURCE_LOCK_WAIT_TIME.getValue(),
                        labelContainer.getUserCreatorLabel().getUser());

                LinkisUtils.tryFinally(
                    () -> {
                      NodeResource labelResource = labelResourceService.getLabelResource(label);
                      if (labelResource != null) {
                        if (label instanceof EMInstanceLabel) {
                          timeCheck(labelResource, usedResource);
                        }
                        if (usedResource.getUsedResource() != null
                            && usedResource.getUsedResource()
                                != Resource.initResource(usedResource.getResourceType())) {
                          labelResource.setUsedResource(
                              labelResource
                                  .getUsedResource()
                                  .minus(usedResource.getUsedResource()));
                          labelResource.setLeftResource(
                              labelResource.getLeftResource().add(usedResource.getUsedResource()));
                        }
                        if (usedResource.getLockedResource() != null
                            && usedResource.getLockedResource()
                                != Resource.initResource(usedResource.getResourceType())) {
                          labelResource.setLockedResource(
                              labelResource
                                  .getLockedResource()
                                  .minus(usedResource.getLockedResource()));
                          labelResource.setLeftResource(
                              labelResource
                                  .getLeftResource()
                                  .add(usedResource.getLockedResource()));
                        }
                        labelResourceService.setLabelResource(
                            label,
                            labelResource,
                            labelContainer.getCombinedResourceLabel().getStringValue());
                        resourceCheck(label, labelResource);
                      }
                    },
                    () -> resourceLockService.unLock(persistenceLock));

                Resource releasedResource =
                    usedResource.getUsedResource() != null
                        ? usedResource.getUsedResource()
                        : usedResource.getLockedResource();

                String heartbeatMsgMetrics = "";
                NodeMetrics oldMetrics = nodeMetricManagerPersistence.getNodeMetrics(ecNode);
                if (oldMetrics != null && StringUtils.isNotBlank(oldMetrics.getHeartBeatMsg())) {
                  heartbeatMsgMetrics = oldMetrics.getHeartBeatMsg();
                }

                if (label
                    .getClass()
                    .isAssignableFrom(labelContainer.getCombinedResourceLabel().getClass())) {
                  resourceLogService.recordUserResourceAction(
                      labelContainer,
                      persistenceResource.getTicketId(),
                      ChangeType.ENGINE_CLEAR,
                      releasedResource,
                      status,
                      heartbeatMsgMetrics);
                }
              });

          EngineInstanceLabel engineInstanceLabel = labelContainer.getEngineInstanceLabel();

          try {
            labelResourceService.removeResourceByLabel(engineInstanceLabel);
            resourceLogService.success(
                ChangeType.ENGINE_CLEAR, usedResource.getUsedResource(), engineInstanceLabel, null);
          } catch (Exception exception) {
            resourceLogService.failed(
                ChangeType.ENGINE_CLEAR,
                usedResource.getUsedResource(),
                engineInstanceLabel,
                null,
                exception);
            throw exception;
          }
        },
        () -> resourceLockService.unLock(instanceLock));
  }

  public void timeCheck(NodeResource labelResource, NodeResource usedResource) {
    if (labelResource.getCreateTime() != null && usedResource.getCreateTime() != null) {
      if (labelResource.getCreateTime().getTime() > usedResource.getCreateTime().getTime()) {
        throw new RMWarnException(
            ManagerCommonErrorCodeSummary.RESOURCE_LATER_CREATED.getErrorCode(),
            String.format(
                "no need to clear this labelResource, labelResource:%s created time is after than usedResource:%s无需清理该标签的资源,该标签资源的创建时间晚于已用资源的创建时间",
                labelResource, usedResource));
      }
    }
  }

  private NodeStatus getNodeStatus(EngineInstanceLabel engineInstanceLabel) {
    AMEngineNode node = new AMEngineNode();
    node.setServiceInstance(engineInstanceLabel.getServiceInstance());
    NodeMetrics metrics = nodeMetricManagerPersistence.getNodeMetrics(node);
    NodeStatus status;
    if (metrics != null) {
      NodeStatus timeStatus = NodeStatus.values()[metrics.getStatus()];
      if (!NodeStatus.isCompleted(timeStatus)) {
        status = NodeStatus.Failed;
      } else {
        status = timeStatus;
      }
    } else {
      logger.warn(String.format("EC %s status unknown", engineInstanceLabel.getServiceInstance()));
      status = NodeStatus.Failed;
    }
    return status;
  }

  private String engineConnSpringName = GovernanceCommonConf.ENGINE_CONN_SPRING_NAME().getValue();
  private String engineConnManagerSpringName =
      GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME().getValue();

  /**
   * If the IP and port are empty, return the resource status of all modules of a module Return the
   * use of this instance resource if there is an IP and port
   *
   * @param serviceInstances
   * @return
   */
  @Override
  public ResourceInfo getResourceInfo(ServiceInstance[] serviceInstances) {
    ResourceInfo resourceInfo = new ResourceInfo(Lists.newArrayList());
    for (ServiceInstance serviceInstance : serviceInstances) {
      InfoRMNode rmNode = new InfoRMNode();
      NodeResource aggregatedResource;
      if (engineConnSpringName.equals(serviceInstance.getApplicationName())) {
        EngineInstanceLabel engineInstanceLabel =
            LabelBuilderFactoryContext.getLabelBuilderFactory()
                .createLabel(EngineInstanceLabel.class);
        engineInstanceLabel.setServiceName(serviceInstance.getApplicationName());
        engineInstanceLabel.setInstance(serviceInstance.getInstance());
        aggregatedResource = labelResourceService.getLabelResource(engineInstanceLabel);
      } else if (engineConnManagerSpringName.equals(serviceInstance.getApplicationName())) {
        EMInstanceLabel emInstanceLabel =
            LabelBuilderFactoryContext.getLabelBuilderFactory().createLabel(EMInstanceLabel.class);
        emInstanceLabel.setServiceName(serviceInstance.getApplicationName());
        emInstanceLabel.setInstance(serviceInstance.getInstance());
        aggregatedResource = labelResourceService.getLabelResource(emInstanceLabel);
      } else {
        continue;
      }
      rmNode.setServiceInstance(serviceInstance);
      rmNode.setNodeResource(aggregatedResource);
      resourceInfo.getResourceInfo().add(rmNode);
    }
    return resourceInfo;
  }

  @Override
  public void resourceReport(List<Label<?>> labels, NodeResource reportResource) {}

  public class UnlockTimeoutResourceRunnable implements Runnable {
    private List<Label<?>> labels;
    private PersistenceLabel persistenceEngineLabel;
    private String ticketId;

    public UnlockTimeoutResourceRunnable(
        List<Label<?>> labels, PersistenceLabel persistenceEngineLabel, String ticketId) {
      this.labels = labels;
      this.persistenceEngineLabel = persistenceEngineLabel;
      this.ticketId = ticketId;
    }

    @Override
    public void run() {
      LinkisUtils.tryAndWarnMsg(
          () -> {
            logger.info(
                String.format(
                    "check locked resource of %s, ticketId: %s",
                    persistenceEngineLabel.getStringValue(), ticketId));
            PersistenceResource persistResource =
                resourceManagerPersistence.getNodeResourceByTicketId(ticketId);
            if (persistResource == null) {
              logger.info(String.format("ticketId %s relation resource not exists", ticketId));
              return;
            }

            NodeResource usedResource = ResourceUtils.fromPersistenceResource(persistResource);
            if (usedResource != null
                && usedResource.getLockedResource() != null
                && usedResource
                    .getLockedResource()
                    .moreThan(Resource.getZeroResource(usedResource.getLockedResource()))) {

              PersistenceLabel dbEngineInstanceLabel =
                  labelManagerPersistence.getLabel(persistenceEngineLabel.getId());
              PersistenceLabel currnentEngineInstanceLabel =
                  dbEngineInstanceLabel == null ? persistenceEngineLabel : dbEngineInstanceLabel;
              if (currnentEngineInstanceLabel
                  .getLabelKey()
                  .equalsIgnoreCase(LabelKeyConstant.ENGINE_INSTANCE_KEY)) {
                Label<?> realLabel =
                    ManagerUtils.persistenceLabelToRealLabel(currnentEngineInstanceLabel);
                if (realLabel instanceof EngineInstanceLabel) {
                  labels.add(realLabel);
                  logger.warn(
                      String.format(
                          "serviceInstance %s lock resource timeout, clear resource",
                          ((EngineInstanceLabel) realLabel).getServiceInstance()));
                  AMEngineNode ecNode = new AMEngineNode();
                  ecNode.setServiceInstance(((EngineInstanceLabel) realLabel).getServiceInstance());
                  ecNode.setNodeStatus(NodeStatus.Failed);
                  ecNode.setLabels(labels);
                  resourceReleased(ecNode);
                }
              }
            }
            logger.info(String.format("Finished to check unlock resource of %s", ticketId));
          },
          String.format("Failed to UnlockTimeoutResourceRunnable %s", ticketId),
          logger);
    }
  }
}
