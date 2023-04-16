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

package org.apache.linkis.manager.rm.utils;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.apache.linkis.manager.common.constant.RMConstant;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.serializer.NodeResourceSerializer$;
import org.apache.linkis.manager.label.entity.engine.EngineType;
import org.apache.linkis.manager.rm.conf.ResourceStatus;
import org.apache.linkis.manager.rm.restful.vo.UserResourceVo;
import org.apache.linkis.server.BDPJettyServerHelper;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json4s.DefaultFormats;
import org.json4s.DefaultFormats$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RMUtils {

  public static final Logger logger = LoggerFactory.getLogger(RMUtils.class);

  public static final CommonVars<String> MANAGER_KILL_ENGINE_EAIT =
      CommonVars.apply("wds.linkis.manager.rm.kill.engine.wait", "30s");
  public static final CommonVars<Boolean> RM_REQUEST_ENABLE =
      CommonVars.apply("wds.linkis.manager.rm.request.enable", true);
  public static final CommonVars<Long> RM_RESOURCE_LOCK_WAIT_TIME =
      CommonVars.apply("wds.linkis.manager.rm.lock.wait", 5 * 60 * 1000L);
  public static final CommonVars<Boolean> RM_DEBUG_ENABLE =
      CommonVars.apply("wds.linkis.manager.rm.debug.enable", false);
  public static final CommonVars<String> RM_DEBUG_LOG_PATH =
      CommonVars.apply("wds.linkis.manager.rm.debug.log.path", "file:///tmp/linkis/rmLog");
  // TODO
  public static final CommonVars<String> ENGINE_TYPE =
      CommonVars.apply(
          "wds.linkis.configuration.engine.type",
          EngineType.getAllEngineTypes().stream()
              .map(engineType -> engineType.toString())
              .collect(Collectors.joining(",", "", "")));

  public static final CommonVars RM_RESOURCE_ACTION_RECORD =
      CommonVars.apply("wds.linkis.manager.rm.resource.action.record", true);
  public static final CommonVars EXTERNAL_RESOURCE_REFRESH_TIME =
      CommonVars.apply("wds.linkis.manager.rm.external.resource.regresh.time", "30m");

  public static final ObjectMapper mapper = BDPJettyServerHelper.jacksonJson();
  public static final DefaultFormats formats =
      DefaultFormats$.MODULE$
          .$plus(ResourceSerializer$.MODULE$)
          .$plus(NodeResourceSerializer$.MODULE$);

  public static Resource deserializeResource(String plainResource) {
    return read(plainResource, Resource.class);
  }

  public static String serializeResource(Resource resource) {
    return write(resource, formats);
  }

  public static UserResourceVo toUserResourceVo(UserResource userResource) {
    UserResourceVo userResourceVo = new UserResourceVo();
    if (userResource.getCreator() != null) {
      userResourceVo.setCreator(userResource.getCreator());
    }
    if (userResource.getEngineType() != null) {
      userResourceVo.setEngineTypeWithVersion(
          userResource.getEngineType() + "-" + userResource.getVersion());
    }
    if (userResource.getUsername() != null) {
      userResourceVo.setUsername(userResource.getUsername());
    }
    if (userResource.getCreateTime() != null) {
      userResourceVo.setCreateTime(userResource.getCreateTime());
    }
    if (userResource.getUpdateTime() != null) {
      userResourceVo.setUpdateTime(userResource.getUpdateTime());
    }
    if (userResource.getId() != null) {
      userResourceVo.setId(userResource.getId());
    }
    if (userResource.getUsedResource() != null) {
      userResourceVo.setUsedResource(
          (Map<String, Object>) mapper.readValue(o(userResource.getUsedResource()), Map.class));
    }
    if (userResource.getLeftResource() != null) {
      userResourceVo.setLeftResource(
          (Map<String, Object>) mapper.readValue(write(userResource.getLeftResource()), Map.class));
    }
    if (userResource.getLockedResource() != null) {
      userResourceVo.setLockedResource(
          (Map<String, Object>)
              mapper.readValue(write(userResource.getLockedResource()), Map.class));
    }
    if (userResource.getMaxResource() != null) {
      userResourceVo.setMaxResource(
          (Map<String, Object>) mapper.readValue(write(userResource.getMaxResource()), Map.class));
    }
    if (userResource.getMinResource() != null) {
      userResourceVo.setMinResource(
          (Map<String, Object>) mapper.readValue(write(userResource.getMinResource()), Map.class));
    }
    if (userResource.getResourceType() != null) {
      userResourceVo.setResourceType(userResource.getResourceType());
    }
    if (userResource.getLeftResource() != null && userResource.getMaxResource() != null) {
      if (userResource.getResourceType().equals(ResourceType.DriverAndYarn)) {
        DriverAndYarnResource leftResource = (DriverAndYarnResource) userResource.getLeftResource();
        LoadInstanceResource leftDriverResource = leftResource.loadInstanceResource();
        YarnResource leftYarnResource = leftResource.yarnResource();

        DriverAndYarnResource maxResource = (DriverAndYarnResource) userResource.getMaxResource();
        LoadInstanceResource maxDriverResource = maxResource.loadInstanceResource();
        YarnResource maxYarnResource = maxResource.yarnResource();

        userResourceVo.setLoadResourceStatus(
            ResourceStatus.measure(leftDriverResource, maxDriverResource));
        userResourceVo.setQueueResourceStatus(
            ResourceStatus.measure(leftYarnResource, maxYarnResource));
      } else {
        userResourceVo.setLoadResourceStatus(
            ResourceStatus.measure(userResource.getLeftResource(), userResource.getMaxResource()));
      }
    }
    return userResourceVo;
  }

  public PersistenceResource toPersistenceResource(NodeResource nodeResource) {
    PersistenceResource persistenceResource = new PersistenceResource();
    if (nodeResource.getMaxResource() != null) {
      persistenceResource.setMaxResource(serializeResource(nodeResource.getMaxResource()));
    }
    if (nodeResource.getMinResource() != null) {
      persistenceResource.setMinResource(serializeResource(nodeResource.getMinResource()));
    }
    if (nodeResource.getLockedResource() != null) {
      persistenceResource.setLockedResource(serializeResource(nodeResource.getLockedResource()));
    }
    if (nodeResource.getExpectedResource() != null) {
      persistenceResource.setExpectedResource(
          serializeResource(nodeResource.getExpectedResource()));
    }
    if (nodeResource.getLeftResource() != null) {
      persistenceResource.setLeftResource(serializeResource(nodeResource.getLeftResource()));
    }
    persistenceResource.setResourceType(nodeResource.getResourceType().toString());
    return persistenceResource;
  }

  public CommonNodeResource aggregateNodeResource(
      NodeResource firstNodeResource, NodeResource secondNodeResource) {
    if (firstNodeResource != null && secondNodeResource != null) {
      CommonNodeResource aggregatedNodeResource = new CommonNodeResource();
      aggregatedNodeResource.setResourceType(firstNodeResource.getResourceType());
      aggregatedNodeResource.setMaxResource(
          aggregateResource(
              firstNodeResource.getMaxResource(), secondNodeResource.getMaxResource()));
      aggregatedNodeResource.setMinResource(
          aggregateResource(
              firstNodeResource.getMinResource(), secondNodeResource.getMinResource()));
      aggregatedNodeResource.setUsedResource(
          aggregateResource(
              firstNodeResource.getUsedResource(), secondNodeResource.getUsedResource()));
      aggregatedNodeResource.setLockedResource(
          aggregateResource(
              firstNodeResource.getLockedResource(), secondNodeResource.getLockedResource()));
      aggregatedNodeResource.setLeftResource(
          aggregateResource(
              firstNodeResource.getLeftResource(), secondNodeResource.getLeftResource()));
      return aggregatedNodeResource;
    }
    if (firstNodeResource == null && secondNodeResource == null) {
      return null;
    }
    if (firstNodeResource == null) {
      return (CommonNodeResource) secondNodeResource;
    } else {
      return (CommonNodeResource) firstNodeResource;
    }
  }

  public Resource aggregateResource(Resource firstResource, Resource secondResource) {
    if (firstResource == null && secondResource == null) {
      return null;
    }
    if (firstResource == null) {
      return secondResource;
    }
    if (secondResource == null) {
      return firstResource;
    }
    if (firstResource.getClass().equals(secondResource.getClass())) {
      return firstResource.add(secondResource);
    }
    return null;
  }

  public String getResourceInfoMsg(
      String resourceType,
      String unitType,
      Object requestResource,
      Object availableResource,
      Object maxResource) {
    String reqMsg =
        (requestResource == null)
            ? "null" + unitType
            : dealMemory(resourceType, unitType, requestResource);
    String availMsg =
        (availableResource == null)
            ? "null" + unitType
            : dealMemory(resourceType, unitType, availableResource.toString());
    String maxMsg =
        (maxResource == null)
            ? "null" + unitType
            : dealMemory(resourceType, unitType, maxResource.toString());
    return " user "
        + resourceType
        + ", requestResource : "
        + reqMsg
        + " > availableResource : "
        + availMsg
        + ", maxResource : "
        + maxMsg
        + ".";
  }

  private String dealMemory(String resourceType, String unitType, Object requestResource) {
    String dealMemory = "";
    if (RMConstant.MEMORY.equals(resourceType) && RMConstant.MEMORY_UNIT_BYTE.equals(unitType)) {
      try {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Will change " + requestResource.toString() + " from " + unitType + " to GB");
        }
        dealMemory =
            ByteTimeUtils.byteStringAsGb(requestResource.toString() + "b").toString() + "GB";
      } catch (Exception e) {
        logger.error("Cannot convert " + requestResource + " to Gb, " + e.getMessage());
        dealMemory = requestResource.toString() + unitType;
      }
    } else {
      dealMemory = requestResource.toString() + unitType;
    }
    return dealMemory;
  }
}
