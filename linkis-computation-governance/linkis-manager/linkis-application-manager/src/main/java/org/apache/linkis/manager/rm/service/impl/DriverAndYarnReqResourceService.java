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

import org.apache.linkis.manager.common.constant.RMConstant;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.exception.RMErrorCode;
import org.apache.linkis.manager.rm.external.service.ExternalResourceService;
import org.apache.linkis.manager.rm.external.yarn.YarnResourceIdentifier;
import org.apache.linkis.manager.rm.service.LabelResourceService;
import org.apache.linkis.manager.rm.service.RequestResourceService;
import org.apache.linkis.manager.rm.utils.RMUtils;

import org.apache.commons.math3.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.common.entity.resource.ResourceType.DriverAndYarn;

public class DriverAndYarnReqResourceService extends RequestResourceService {
  private static final Logger logger =
      LoggerFactory.getLogger(DriverAndYarnReqResourceService.class);

  private LabelResourceService labelResourceService;
  private ExternalResourceService externalResourceService;

  private final ResourceType resourceType = DriverAndYarn;

  public DriverAndYarnReqResourceService(
      LabelResourceService labelResourceService, ExternalResourceService externalResourceService) {
    super(labelResourceService);
    this.labelResourceService = labelResourceService;
    this.externalResourceService = externalResourceService;
  }

  @Override
  public ResourceType resourceType() {
    return this.resourceType;
  }

  @Override
  public boolean canRequest(RMLabelContainer labelContainer, NodeResource resource)
      throws RMWarnException {
    if (!super.canRequest(labelContainer, resource)) {
      return false;
    }
    DriverAndYarnResource requestedDriverAndYarnResource =
        (DriverAndYarnResource) resource.getMaxResource();
    YarnResource requestedYarnResource = requestedDriverAndYarnResource.getYarnResource();

    YarnResourceIdentifier yarnIdentifier =
        new YarnResourceIdentifier(requestedYarnResource.getQueueName());
    NodeResource providedYarnResource =
        externalResourceService.getResource(ResourceType.Yarn, labelContainer, yarnIdentifier);
    Resource maxCapacity = providedYarnResource.getMaxResource();
    Resource usedCapacity = providedYarnResource.getUsedResource();

    logger.info(
        "This queue: "
            + requestedYarnResource.getQueueName()
            + " used resource:"
            + usedCapacity
            + " and max resource: "
            + maxCapacity);
    Resource queueLeftResource = maxCapacity.minus(usedCapacity);
    logger.info(
        "queue: "
            + requestedYarnResource.getQueueName()
            + " left "
            + queueLeftResource
            + ", this request requires: "
            + requestedYarnResource);
    if (queueLeftResource.less(requestedYarnResource)) {
      logger.info(
          "user: "
              + labelContainer.getUserCreatorLabel().getUser()
              + " request queue resource "
              + requestedYarnResource
              + " > left resource "
              + queueLeftResource);
      Pair<Integer, String> notEnoughMessage =
          generateQueueNotEnoughMessage(requestedYarnResource, queueLeftResource, maxCapacity);
      throw new RMWarnException(notEnoughMessage.getKey(), notEnoughMessage.getValue());
    } else {
      return true;
    }
  }

  public Pair<Integer, String> generateQueueNotEnoughMessage(
      Resource requestResource, Resource availableResource, Resource maxResource) {
    if (requestResource instanceof YarnResource) {
      YarnResource yarn = (YarnResource) requestResource;
      YarnResource yarnAvailable = (YarnResource) availableResource;
      YarnResource maxYarn = (YarnResource) maxResource;
      if (yarn.getQueueCores() > yarnAvailable.getQueueCores()) {
        return Pair.create(
            RMErrorCode.CLUSTER_QUEUE_CPU_INSUFFICIENT.getErrorCode(),
            RMErrorCode.CLUSTER_QUEUE_CPU_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.CPU,
                    RMConstant.CPU_UNIT,
                    yarn.getQueueCores(),
                    yarnAvailable.getQueueCores(),
                    maxYarn.getQueueCores()));
      } else if (yarn.getQueueMemory() > yarnAvailable.getQueueMemory()) {
        return Pair.create(
            RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorCode(),
            RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.MEMORY,
                    RMConstant.MEMORY_UNIT_BYTE,
                    yarn.getQueueMemory(),
                    yarnAvailable.getQueueMemory(),
                    maxYarn.getQueueMemory()));
      } else {
        return Pair.create(
            RMErrorCode.CLUSTER_QUEUE_INSTANCES_INSUFFICIENT.getErrorCode(),
            RMErrorCode.CLUSTER_QUEUE_INSTANCES_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.APP_INSTANCE,
                    RMConstant.INSTANCE_UNIT,
                    yarn.getQueueInstances(),
                    yarnAvailable.getQueueInstances(),
                    maxYarn.getQueueInstances()));
      }
    } else {
      return Pair.create(
          RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorCode(),
          RMErrorCode.CLUSTER_QUEUE_MEMORY_INSUFFICIENT.getErrorDesc()
              + " Unusual insufficient queue memory.");
    }
  }
}
