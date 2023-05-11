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

package org.apache.linkis.manager.common.utils;

import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.common.entity.resource.*;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {
  private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
  }

  public static <T> String toJSONString(T entity) {
    String json = null;

    try {
      json = mapper.writeValueAsString(entity);
    } catch (JsonProcessingException e) {
      logger.error("to json string met with exception", e);
    }

    return json;
  }

  public static <T> T parse(String json, TypeReference<T> valueTypeRef) {
    try {
      return mapper.readValue(json, valueTypeRef);
    } catch (JsonProcessingException e) {
      logger.error("json parse met with exception", e);
    }

    return null;
  }

  public static <T> T parse(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      logger.error("json parse met with exception", e);
    }

    return null;
  }

  public static Resource deserializeResource(String plainResource, ResourceType resourceType) {
    try {
      switch (resourceType) {
        case CPU:
          return mapper.readValue(plainResource, CPUResource.class);
        case DriverAndYarn:
          return mapper.readValue(plainResource, DriverAndYarnResource.class);
        case Instance:
          return mapper.readValue(plainResource, InstanceResource.class);
        case LoadInstance:
          return mapper.readValue(plainResource, LoadInstanceResource.class);
        case Load:
          return mapper.readValue(plainResource, LoadResource.class);
        case Memory:
          return mapper.readValue(plainResource, MemoryResource.class);
        case Special:
          return mapper.readValue(plainResource, SpecialResource.class);
        case Yarn:
          return mapper.readValue(plainResource, YarnResource.class);
        default:
          return mapper.readValue(plainResource, LoadResource.class);
      }
    } catch (JsonProcessingException e) {
      logger.warn("ResourceUtils deserializeResource failed", e);
    }
    return null;
  }

  public static String serializeResource(Resource resource) {
    try {
      return mapper.writeValueAsString(resource);
    } catch (JsonProcessingException e) {
      logger.warn("ResourceUtils deserializeResource failed", e);
    }
    return null;
  }

  public static PersistenceResource toPersistenceResource(NodeResource nodeResource) {
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
    if (nodeResource.getUsedResource() != null) {
      persistenceResource.setUsedResource(serializeResource(nodeResource.getUsedResource()));
    }
    persistenceResource.setResourceType(nodeResource.getResourceType().toString());
    return persistenceResource;
  }

  public static CommonNodeResource fromPersistenceResource(
      PersistenceResource persistenceResource) {
    if (persistenceResource == null) return null;
    CommonNodeResource nodeResource = new CommonNodeResource();
    ResourceType resourceType = ResourceType.valueOf(persistenceResource.getResourceType());
    nodeResource.setId(persistenceResource.getId());
    if (persistenceResource.getMaxResource() != null) {
      nodeResource.setMaxResource(
          deserializeResource(persistenceResource.getMaxResource(), resourceType));
    }
    if (persistenceResource.getMinResource() != null) {
      nodeResource.setMinResource(
          deserializeResource(persistenceResource.getMinResource(), resourceType));
    }
    if (persistenceResource.getLockedResource() != null) {
      nodeResource.setLockedResource(
          deserializeResource(persistenceResource.getLockedResource(), resourceType));
    }
    if (persistenceResource.getExpectedResource() != null) {
      nodeResource.setExpectedResource(
          deserializeResource(persistenceResource.getExpectedResource(), resourceType));
    }
    if (persistenceResource.getLeftResource() != null) {
      nodeResource.setLeftResource(
          deserializeResource(persistenceResource.getLeftResource(), resourceType));
    }
    if (persistenceResource.getUsedResource() != null) {
      nodeResource.setUsedResource(
          deserializeResource(persistenceResource.getUsedResource(), resourceType));
    }
    if (persistenceResource.getCreateTime() != null) {
      nodeResource.setCreateTime(persistenceResource.getCreateTime());
    }
    if (persistenceResource.getUpdateTime() != null) {
      nodeResource.setUpdateTime(persistenceResource.getUpdateTime());
    }
    nodeResource.setResourceType(resourceType);
    return nodeResource;
  }

  public static UserResource fromPersistenceResourceAndUser(
      PersistenceResource persistenceResource) {
    if (persistenceResource == null) return null;
    UserResource nodeResource = new UserResource();
    nodeResource.setId(persistenceResource.getId());
    ResourceType resourceType = ResourceType.valueOf(persistenceResource.getResourceType());

    if (persistenceResource.getMaxResource() != null) {
      nodeResource.setMaxResource(
          deserializeResource(persistenceResource.getMaxResource(), resourceType));
    }
    if (persistenceResource.getMinResource() != null) {
      nodeResource.setMinResource(
          deserializeResource(persistenceResource.getMinResource(), resourceType));
    }
    if (persistenceResource.getLockedResource() != null) {
      nodeResource.setLockedResource(
          deserializeResource(persistenceResource.getLockedResource(), resourceType));
    }
    if (persistenceResource.getExpectedResource() != null) {
      nodeResource.setExpectedResource(
          deserializeResource(persistenceResource.getExpectedResource(), resourceType));
    }
    if (persistenceResource.getLeftResource() != null) {
      nodeResource.setLeftResource(
          deserializeResource(persistenceResource.getLeftResource(), resourceType));
    }
    if (persistenceResource.getUsedResource() != null) {
      nodeResource.setUsedResource(
          deserializeResource(persistenceResource.getUsedResource(), resourceType));
    }
    if (persistenceResource.getCreateTime() != null) {
      nodeResource.setCreateTime(persistenceResource.getCreateTime());
    }
    if (persistenceResource.getUpdateTime() != null) {
      nodeResource.setUpdateTime(persistenceResource.getUpdateTime());
    }
    nodeResource.setResourceType(resourceType);
    return nodeResource;
  }

  public static ResourceType getResourceTypeByResource(Resource resource) {
    if (resource instanceof LoadResource) {
      return ResourceType.Load;
    } else if (resource instanceof InstanceResource) {
      return ResourceType.Instance;
    } else if (resource instanceof CPUResource) {
      return ResourceType.CPU;
    } else if (resource instanceof LoadInstanceResource) {
      return ResourceType.LoadInstance;
    } else if (resource instanceof YarnResource) {
      return ResourceType.Yarn;
    } else if (resource instanceof DriverAndYarnResource) {
      return ResourceType.DriverAndYarn;
    } else if (resource instanceof SpecialResource) {
      return ResourceType.Special;
    } else {
      return ResourceType.LoadInstance;
    }
  }

  public static NodeResource convertTo(NodeResource nodeResource, ResourceType resourceType) {
    if (nodeResource.getResourceType().equals(resourceType)) return nodeResource;
    if (resourceType.equals(ResourceType.LoadInstance)) {
      if (nodeResource.getResourceType().equals(ResourceType.DriverAndYarn)) {
        nodeResource.setResourceType(resourceType);
        if (nodeResource.getMaxResource() != null) {
          DriverAndYarnResource maxResource = (DriverAndYarnResource) nodeResource.getMaxResource();
          nodeResource.setMaxResource(maxResource.getLoadInstanceResource());
        }
        if (nodeResource.getMinResource() != null) {
          DriverAndYarnResource minResource = (DriverAndYarnResource) nodeResource.getMinResource();
          nodeResource.setMinResource(minResource.getLoadInstanceResource());
        }
        if (nodeResource.getUsedResource() != null) {
          DriverAndYarnResource usedResource =
              (DriverAndYarnResource) nodeResource.getUsedResource();
          nodeResource.setUsedResource(usedResource.getLoadInstanceResource());
        }
        if (nodeResource.getLockedResource() != null) {
          DriverAndYarnResource lockedResource =
              (DriverAndYarnResource) nodeResource.getLockedResource();
          nodeResource.setLockedResource(lockedResource.getLoadInstanceResource());
        }
        if (nodeResource.getExpectedResource() != null) {
          DriverAndYarnResource lockedResource =
              (DriverAndYarnResource) nodeResource.getExpectedResource();
          nodeResource.setExpectedResource(lockedResource.getLoadInstanceResource());
        }
        if (nodeResource.getLeftResource() != null
            && nodeResource.getLeftResource() instanceof DriverAndYarnResource) {
          DriverAndYarnResource leftResource =
              (DriverAndYarnResource) nodeResource.getLeftResource();
          nodeResource.setLeftResource(leftResource.getLoadInstanceResource());
        }
        return nodeResource;
      }
    }
    return nodeResource;
  }

  /**
   * Get the proportion of left resources, and return the smallest CPU, memory, and instance
   *
   * @param leftResource
   * @param maxResource
   * @return
   */
  public static float getLoadInstanceResourceRate(Resource leftResource, Resource maxResource) {
    if (null == leftResource) return 0;
    if (null == maxResource) return 1;
    if (leftResource instanceof LoadInstanceResource) {
      if (maxResource instanceof LoadInstanceResource) {
        LoadInstanceResource leftLoadInstanceResource = (LoadInstanceResource) leftResource;
        LoadInstanceResource maxLoadInstanceResource = (LoadInstanceResource) maxResource;
        float cpuRate =
            maxLoadInstanceResource.getCores() > 0
                ? (leftLoadInstanceResource.getCores() * 1.0f) / maxLoadInstanceResource.getCores()
                : 1f;
        float memoryRate =
            maxLoadInstanceResource.getMemory() > 0
                ? (leftLoadInstanceResource.getMemory() * 1.0f)
                    / maxLoadInstanceResource.getMemory()
                : 1f;
        float instanceRate =
            maxLoadInstanceResource.getInstances() > 0
                ? (leftLoadInstanceResource.getInstances() * 1.0f)
                    / maxLoadInstanceResource.getInstances()
                : 1f;

        return Math.min(Math.min(cpuRate, memoryRate), instanceRate);
      } else {
        return 1f;
      }
    } else {
      return 1f;
    }
  }
}
