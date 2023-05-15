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

package org.apache.linkis.manager.rm.service;

import org.apache.linkis.manager.common.constant.RMConstant;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.rm.domain.RMLabelContainer;
import org.apache.linkis.manager.rm.exception.RMErrorCode;
import org.apache.linkis.manager.rm.utils.RMUtils;
import org.apache.linkis.manager.rm.utils.UserConfiguration;

import org.apache.commons.lang3.tuple.Pair;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.*;

public abstract class RequestResourceService {
  private static final Logger logger = LoggerFactory.getLogger(RequestResourceService.class);

  public final ResourceType resourceType = ResourceType.Default;
  private final Boolean enableRequest = RMUtils.RM_REQUEST_ENABLE.getValue();
  private final LabelResourceService labelResourceService;

  public RequestResourceService(LabelResourceService labelResourceService) {
    this.labelResourceService = labelResourceService;
  }

  public abstract ResourceType resourceType();

  public boolean canRequest(RMLabelContainer labelContainer, NodeResource resource)
      throws RMWarnException {
    if (labelContainer.getCurrentLabel() instanceof EMInstanceLabel) {
      return checkEMResource(
          labelContainer.getUserCreatorLabel().getUser(),
          (EMInstanceLabel) labelContainer.getCurrentLabel(),
          resource);
    }
    NodeResource labelResource =
        labelResourceService.getLabelResource(labelContainer.getCurrentLabel());
    Resource requestResource = resource.getMinResource();
    if (labelContainer
        .getCombinedUserCreatorEngineTypeLabel()
        .equals(labelContainer.getCurrentLabel())) {
      if (labelResource == null) {
        labelResource = new CommonNodeResource();
        labelResource.setResourceType(resource.getResourceType());
        labelResource.setUsedResource(Resource.initResource(resource.getResourceType()));
        labelResource.setLockedResource(Resource.initResource(resource.getResourceType()));
        logger.info("ResourceInit: " + labelContainer.getCurrentLabel().getStringValue() + " ");
      }
      Resource configuredResource =
          UserConfiguration.getUserConfiguredResource(
              resource.getResourceType(),
              labelContainer.getUserCreatorLabel(),
              labelContainer.getEngineTypeLabel());
      logger.debug(
          "Get configured resource "
              + configuredResource
              + " for ["
              + labelContainer.getUserCreatorLabel()
              + "] and ["
              + labelContainer.getEngineTypeLabel()
              + "] ");
      labelResource.setMaxResource(configuredResource);
      labelResource.setMinResource(Resource.initResource(labelResource.getResourceType()));
      labelResource.setLeftResource(
          labelResource
              .getMaxResource()
              .minus(labelResource.getUsedResource())
              .minus(labelResource.getLockedResource()));
      labelResourceService.setLabelResource(
          labelContainer.getCurrentLabel(),
          labelResource,
          labelContainer.getCombinedUserCreatorEngineTypeLabel().getStringValue());
      logger.debug(
          labelContainer.getCurrentLabel()
              + " to request ["
              + requestResource
              + "]  	 labelResource: Max: "
              + labelResource.getMaxResource()
              + "  	 use:  "
              + labelResource.getUsedResource()
              + "  	 locked: "
              + labelResource.getLockedResource());
    }
    logger.debug(
        "Label [" + labelContainer.getCurrentLabel() + "] has resource + [" + labelResource + "]");
    if (labelResource != null) {
      Resource labelAvailableResource = labelResource.getLeftResource();
      Resource labelMaxResource = labelResource.getMaxResource();
      if (labelAvailableResource.less(requestResource) && enableRequest) {
        logger.info(
            "Failed check: "
                + labelContainer.getUserCreatorLabel().getUser()
                + " want to use label ["
                + labelContainer.getCurrentLabel()
                + "] resource["
                + requestResource
                + "] > "
                + "label available resource["
                + labelAvailableResource
                + "]");

        // TODO sendAlert(moduleInstance, user, creator, requestResource,
        // moduleAvailableResource.resource, moduleLeftResource)
        Pair<Integer, String> notEnoughMessage =
            generateNotEnoughMessage(requestResource, labelAvailableResource, labelMaxResource);
        throw new RMWarnException(notEnoughMessage.getKey(), notEnoughMessage.getValue());
      }
      logger.debug(
          "Passed check: "
              + labelContainer.getUserCreatorLabel().getUser()
              + " want to use label ["
              + labelContainer.getCurrentLabel()
              + "] resource["
              + requestResource
              + "] <= "
              + "label available resource["
              + labelAvailableResource
              + "]");
      return true;
    } else {
      logger.warn("No resource available found for label " + labelContainer.getCurrentLabel());
      throw new RMWarnException(
          NO_RESOURCE.getErrorCode(),
          MessageFormat.format(NO_RESOURCE.getErrorDesc(), labelContainer.getCurrentLabel()));
    }
  }

  private boolean checkEMResource(
      String user, EMInstanceLabel emInstanceLabel, NodeResource resource) throws RMWarnException {
    NodeResource labelResource = labelResourceService.getLabelResource(emInstanceLabel);
    Resource requestResource = resource.getMinResource();
    logger.debug("emInstanceLabel resource info " + labelResource);
    if (labelResource != null) {
      Resource labelAvailableResource = labelResource.getLeftResource();
      if (labelAvailableResource.less(requestResource) && enableRequest) {
        logger.info(
            "user want to use resource["
                + requestResource
                + "] > em "
                + emInstanceLabel.getInstance()
                + " available resource["
                + labelAvailableResource
                + "]");

        // TODO sendAlert(moduleInstance, user, creator, requestResource,
        // moduleAvailableResource.resource, moduleLeftResource)
        Pair<Integer, String> notEnoughMessage =
            generateECMNotEnoughMessage(
                requestResource, labelAvailableResource, labelResource.getMaxResource());
        throw new RMWarnException(notEnoughMessage.getKey(), notEnoughMessage.getValue());
      }
      logger.debug(
          "Passed check: resource["
              + requestResource
              + "] want to use em "
              + emInstanceLabel.getInstance()
              + " available resource["
              + labelAvailableResource
              + "]");
      return true;
    } else {
      logger.warn("No resource available found for em " + emInstanceLabel.getInstance());
      throw new RMWarnException(
          NO_RESOURCE_AVAILABLE.getErrorCode(),
          MessageFormat.format(
              NO_RESOURCE_AVAILABLE.getErrorDesc(), emInstanceLabel.getInstance()));
    }
  }

  public Pair<Integer, String> generateECMNotEnoughMessage(
      Resource requestResource, Resource availableResource, Resource maxResource) {
    LoadInstanceResource loadRequestResource = null;
    if (requestResource instanceof LoadInstanceResource) {
      loadRequestResource = (LoadInstanceResource) requestResource;
    } else if (requestResource instanceof DriverAndYarnResource) {
      loadRequestResource = ((DriverAndYarnResource) requestResource).getLoadInstanceResource();
    }

    if (loadRequestResource != null) {
      LoadInstanceResource loadInstanceAvailable = (LoadInstanceResource) availableResource;
      LoadInstanceResource loadInstanceMax = (LoadInstanceResource) maxResource;
      if (loadRequestResource.getCores() > loadInstanceAvailable.getCores()) {
        return Pair.of(
            RMErrorCode.ECM_CPU_INSUFFICIENT.getErrorCode(),
            RMErrorCode.ECM_CPU_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.CPU,
                    RMConstant.CPU_UNIT,
                    loadRequestResource.getCores(),
                    loadInstanceAvailable.getCores(),
                    loadInstanceMax.getCores()));
      } else if (loadRequestResource.getMemory() > loadInstanceAvailable.getMemory()) {
        return Pair.of(
            RMErrorCode.ECM_MEMORY_INSUFFICIENT.getErrorCode(),
            RMErrorCode.ECM_MEMORY_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.MEMORY,
                    RMConstant.MEMORY_UNIT_BYTE,
                    loadRequestResource.getMemory(),
                    loadInstanceAvailable.getMemory(),
                    loadInstanceMax.getMemory()));
      } else {
        return Pair.of(
            RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getErrorCode(),
            RMErrorCode.ECM_INSTANCES_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.APP_INSTANCE,
                    RMConstant.INSTANCE_UNIT,
                    loadRequestResource.getInstances(),
                    loadInstanceAvailable.getInstances(),
                    loadInstanceMax.getInstances()));
      }
    } else {
      return Pair.of(
          RMErrorCode.ECM_RESOURCE_INSUFFICIENT.getErrorCode(),
          RMErrorCode.ECM_RESOURCE_INSUFFICIENT.getErrorDesc()
              + " Unusual insufficient queue memory.");
    }
  }

  public Pair<Integer, String> generateNotEnoughMessage(
      Resource requestResource, Resource availableResource, Resource maxResource) {
    if (requestResource instanceof MemoryResource) {
      MemoryResource m = (MemoryResource) requestResource;
      MemoryResource avail = (MemoryResource) availableResource;
      MemoryResource max = (MemoryResource) maxResource;
      return Pair.of(
          RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode(),
          RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc()
              + RMUtils.getResourceInfoMsg(
                  RMConstant.MEMORY,
                  RMConstant.MEMORY_UNIT_BYTE,
                  m.getMemory(),
                  avail.getMemory(),
                  max.getMemory()));

    } else if (requestResource instanceof InstanceResource) {
      InstanceResource i = (InstanceResource) requestResource;
      InstanceResource avail = (InstanceResource) availableResource;
      InstanceResource max = (InstanceResource) maxResource;
      return Pair.of(
          RMErrorCode.INSTANCES_INSUFFICIENT.getErrorCode(),
          RMErrorCode.INSTANCES_INSUFFICIENT.getErrorDesc()
              + RMUtils.getResourceInfoMsg(
                  RMConstant.APP_INSTANCE,
                  RMConstant.INSTANCE_UNIT,
                  i.getInstances(),
                  avail.getInstances(),
                  max.getInstances()));

    } else if (requestResource instanceof CPUResource) {
      CPUResource c = (CPUResource) requestResource;
      CPUResource avail = (CPUResource) availableResource;
      CPUResource max = (CPUResource) maxResource;
      return Pair.of(
          RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode(),
          RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc()
              + RMUtils.getResourceInfoMsg(
                  RMConstant.CPU,
                  RMConstant.CPU_UNIT,
                  c.getCores(),
                  avail.getCores(),
                  max.getCores()));

    } else if (requestResource instanceof LoadResource) {
      LoadResource l = (LoadResource) requestResource;
      LoadResource loadAvailable = (LoadResource) availableResource;
      LoadResource avail = (LoadResource) availableResource;
      LoadResource max = (LoadResource) maxResource;
      if (l.getCores() > loadAvailable.getCores()) {
        return Pair.of(
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode(),
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.CPU,
                    RMConstant.CPU_UNIT,
                    l.getCores(),
                    avail.getCores(),
                    max.getCores()));
      } else {
        return Pair.of(
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode(),
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.MEMORY,
                    RMConstant.MEMORY_UNIT_BYTE,
                    l.getMemory(),
                    avail.getMemory(),
                    max.getMemory()));
      }

    } else if (requestResource instanceof LoadInstanceResource) {
      LoadInstanceResource li = (LoadInstanceResource) requestResource;
      LoadInstanceResource loadInstanceAvailable = (LoadInstanceResource) availableResource;
      LoadInstanceResource avail = (LoadInstanceResource) availableResource;
      LoadInstanceResource max = (LoadInstanceResource) maxResource;
      if (li.getCores() > loadInstanceAvailable.getCores()) {
        return Pair.of(
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorCode(),
            RMErrorCode.DRIVER_CPU_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.CPU,
                    RMConstant.CPU_UNIT,
                    li.getCores(),
                    avail.getCores(),
                    max.getCores()));
      } else if (li.getMemory() > loadInstanceAvailable.getMemory()) {
        return Pair.of(
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorCode(),
            RMErrorCode.DRIVER_MEMORY_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.MEMORY,
                    RMConstant.MEMORY_UNIT_BYTE,
                    li.getMemory(),
                    avail.getMemory(),
                    max.getMemory()));
      } else {
        return Pair.of(
            RMErrorCode.INSTANCES_INSUFFICIENT.getErrorCode(),
            RMErrorCode.INSTANCES_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.APP_INSTANCE,
                    RMConstant.INSTANCE_UNIT,
                    li.getInstances(),
                    avail.getInstances(),
                    max.getInstances()));
      }

    } else if (requestResource instanceof YarnResource) {
      YarnResource yarn = (YarnResource) requestResource;
      YarnResource yarnAvailable = (YarnResource) availableResource;
      YarnResource avail = (YarnResource) availableResource;
      YarnResource max = (YarnResource) maxResource;
      if (yarn.getQueueCores() > yarnAvailable.getQueueCores()) {
        return Pair.of(
            RMErrorCode.QUEUE_CPU_INSUFFICIENT.getErrorCode(),
            RMErrorCode.QUEUE_CPU_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.CPU,
                    RMConstant.CPU_UNIT,
                    yarn.getQueueCores(),
                    avail.getQueueCores(),
                    max.getQueueCores()));
      } else if (yarn.getQueueMemory() > yarnAvailable.getQueueMemory()) {
        return Pair.of(
            RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getErrorCode(),
            RMErrorCode.QUEUE_MEMORY_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.MEMORY,
                    RMConstant.MEMORY_UNIT_BYTE,
                    yarn.getQueueMemory(),
                    avail.getQueueMemory(),
                    max.getQueueMemory()));
      } else {
        return Pair.of(
            RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getErrorCode(),
            RMErrorCode.QUEUE_INSTANCES_INSUFFICIENT.getErrorDesc()
                + RMUtils.getResourceInfoMsg(
                    RMConstant.APP_INSTANCE,
                    RMConstant.INSTANCE_UNIT,
                    yarn.getQueueInstances(),
                    avail.getQueueInstances(),
                    max.getQueueInstances()));
      }

    } else if (requestResource instanceof DriverAndYarnResource) {
      DriverAndYarnResource dy = (DriverAndYarnResource) requestResource;
      DriverAndYarnResource dyAvailable = (DriverAndYarnResource) availableResource;
      DriverAndYarnResource dyMax = (DriverAndYarnResource) maxResource;
      if (dy.getLoadInstanceResource().getMemory()
              > dyAvailable.getLoadInstanceResource().getMemory()
          || dy.getLoadInstanceResource().getCores()
              > dyAvailable.getLoadInstanceResource().getCores()
          || dy.getLoadInstanceResource().getInstances()
              > dyAvailable.getLoadInstanceResource().getInstances()) {
        return generateNotEnoughMessage(
            dy.getLoadInstanceResource(),
            dyAvailable.getLoadInstanceResource(),
            dyMax.getLoadInstanceResource());
      } else {
        return generateNotEnoughMessage(
            dy.getYarnResource(), dyAvailable.getYarnResource(), dyMax.getYarnResource());
      }

    } else if (requestResource instanceof SpecialResource) {
      throw new RMWarnException(
          NOT_RESOURCE_TYPE.getErrorCode(),
          MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), requestResource.getClass()));
    } else if (requestResource instanceof Resource) {
      throw new RMWarnException(
          NOT_RESOURCE_TYPE.getErrorCode(),
          MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), requestResource.getClass()));
    }

    throw new RMWarnException(
        NOT_RESOURCE_TYPE.getErrorCode(),
        MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), requestResource.getClass()));
  }
}
