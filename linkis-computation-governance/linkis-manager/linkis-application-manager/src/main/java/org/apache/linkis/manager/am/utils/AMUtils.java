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

package org.apache.linkis.manager.am.utils;

import org.apache.linkis.manager.am.vo.AMEngineNodeVo;
import org.apache.linkis.manager.am.vo.EMNodeVo;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.manager.common.entity.node.EMNode;
import org.apache.linkis.manager.common.entity.node.EngineNode;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.rpc.Sender;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMUtils {
  private static final Logger logger = LoggerFactory.getLogger(AMUtils.class);

  private static final AtomicInteger idCreator = new AtomicInteger();
  private static String idPrefix = Sender.getThisServiceInstance().getInstance();

  private static Gson GSON =
      new GsonBuilder()
          .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
          .serializeNulls()
          .registerTypeAdapter(
              Double.class,
              new JsonSerializer<Double>() {
                @Override
                public JsonElement serialize(
                    Double src, Type typeOfSrc, JsonSerializationContext context) {
                  if (src == src.longValue()) {
                    return new JsonPrimitive(src.longValue());
                  } else {
                    return new JsonPrimitive(src);
                  }
                }
              })
          .create();

  public static final ObjectMapper mapper = new ObjectMapper();

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

  public static List<EMNodeVo> copyToEMVo(EMNode[] EMNodes) {
    List<EMNodeVo> EMNodeVos = new ArrayList<>();
    for (EMNode node : EMNodes) {
      EMNodeVo EMNodeVo = new EMNodeVo();
      EMNodeVo.setLabels(node.getLabels());
      EMNodeVo.setApplicationName(node.getServiceInstance().getApplicationName());
      EMNodeVo.setInstance(node.getServiceInstance().getInstance());
      if (node.getStartTime() != null) {
        EMNodeVo.setStartTime(node.getStartTime());
      }
      try {
        if (node.getNodeResource() != null) {
          if (node.getNodeResource().getResourceType() != null) {
            EMNodeVo.setResourceType(node.getNodeResource().getResourceType());
          }
          if (node.getNodeResource().getMaxResource() != null) {
            EMNodeVo.setMaxResource(
                mapper.readValue(
                    mapper.writeValueAsString(node.getNodeResource().getMaxResource()),
                    new TypeReference<Map<String, Object>>() {}));
          }
          if (node.getNodeResource().getMinResource() != null) {
            EMNodeVo.setMinResource(
                mapper.readValue(
                    mapper.writeValueAsString(node.getNodeResource().getMinResource()),
                    new TypeReference<Map<String, Object>>() {}));
          }
          if (node.getNodeResource().getUsedResource() != null) {
            EMNodeVo.setUsedResource(
                mapper.readValue(
                    mapper.writeValueAsString(node.getNodeResource().getUsedResource()),
                    new TypeReference<Map<String, Object>>() {}));
          } else {
            EMNodeVo.setUsedResource(
                mapper.readValue(
                    mapper.writeValueAsString(Resource.initResource(ResourceType.Default)),
                    new TypeReference<Map<String, Object>>() {}));
          }
          if (node.getNodeResource().getLockedResource() != null) {
            EMNodeVo.setLockedResource(
                mapper.readValue(
                    mapper.writeValueAsString(node.getNodeResource().getLockedResource()),
                    new TypeReference<Map<String, Object>>() {}));
          }
          if (node.getNodeResource().getExpectedResource() != null) {
            EMNodeVo.setExpectedResource(
                mapper.readValue(
                    mapper.writeValueAsString(node.getNodeResource().getExpectedResource()),
                    new TypeReference<Map<String, Object>>() {}));
          }
          if (node.getNodeResource().getLeftResource() != null) {
            EMNodeVo.setLeftResource(
                mapper.readValue(
                    mapper.writeValueAsString(node.getNodeResource().getLeftResource()),
                    new TypeReference<Map<String, Object>>() {}));
          }
        }
      } catch (JsonProcessingException e) {
        logger.warn("copyToEMVo writeValueAsString failed", e);
      }

      EMNodeVo.setOwner(node.getOwner());
      if (node.getNodeTaskInfo() != null) {
        if (node.getNodeTaskInfo().getRunningTasks() >= 0) {
          EMNodeVo.setRunningTasks(node.getNodeTaskInfo().getRunningTasks());
        }
        if (node.getNodeTaskInfo().getPendingTasks() >= 0) {
          EMNodeVo.setPendingTasks(node.getNodeTaskInfo().getPendingTasks());
        }
        if (node.getNodeTaskInfo().getSucceedTasks() >= 0) {
          EMNodeVo.setSucceedTasks(node.getNodeTaskInfo().getSucceedTasks());
        }
        if (node.getNodeTaskInfo().getFailedTasks() >= 0) {
          EMNodeVo.setFailedTasks(node.getNodeTaskInfo().getFailedTasks());
        }
      }
      if (node.getNodeOverLoadInfo() != null) {
        if (node.getNodeOverLoadInfo().getMaxMemory() != null) {
          EMNodeVo.setMaxMemory(node.getNodeOverLoadInfo().getMaxMemory());
        }
        if (node.getNodeOverLoadInfo().getUsedMemory() != null) {
          EMNodeVo.setUsedMemory(node.getNodeOverLoadInfo().getUsedMemory());
        }
        if (node.getNodeOverLoadInfo().getSystemCPUUsed() != null) {
          EMNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo().getSystemCPUUsed());
        }
        if (node.getNodeOverLoadInfo().getSystemLeftMemory() != null) {
          EMNodeVo.setSystemLeftMemory(node.getNodeOverLoadInfo().getSystemLeftMemory());
        }
      }
      if (node.getNodeHealthyInfo() != null) {
        if (node.getNodeHealthyInfo().getNodeHealthy() != null) {
          EMNodeVo.setNodeHealthy(node.getNodeHealthyInfo().getNodeHealthy());
        }
        if (node.getNodeHealthyInfo().getMsg() != null) {
          EMNodeVo.setMsg(node.getNodeHealthyInfo().getMsg());
        }
      }
      EMNodeVos.add(EMNodeVo);
    }
    return EMNodeVos;
  }

  public static List<AMEngineNodeVo> copyToAMEngineNodeVo(List<EngineNode> AMEngineNode) {
    List<AMEngineNodeVo> AMEngineNodeVos = new ArrayList<>();
    if (!AMEngineNode.isEmpty()) {
      for (EngineNode node : AMEngineNode) {
        AMEngineNodeVo AMEngineNodeVo = new AMEngineNodeVo();
        AMEngineNodeVo.setLabels(node.getLabels());
        AMEngineNodeVo.setApplicationName(node.getServiceInstance().getApplicationName());
        AMEngineNodeVo.setInstance(node.getServiceInstance().getInstance());
        if (node.getEMNode() != null) {
          AMEngineNodeVo.setEmInstance(node.getEMNode().getServiceInstance().getInstance());
        }

        if (!node.getLabels().isEmpty()) {
          EngineTypeLabel engineTypeLabel = null;
          for (Label label : node.getLabels()) {
            if (label instanceof EngineTypeLabel) {
              engineTypeLabel = (EngineTypeLabel) label;
              break;
            }
          }
          if (engineTypeLabel != null) {
            AMEngineNodeVo.setEngineType(engineTypeLabel.getEngineType());
          }
        }
        if (node.getStartTime() != null) {
          AMEngineNodeVo.setStartTime(node.getStartTime());
        }
        if (node.getNodeStatus() != null) {
          AMEngineNodeVo.setNodeStatus(node.getNodeStatus());
        } else {
          AMEngineNodeVo.setNodeStatus(NodeStatus.Starting);
        }
        if (node.getLock() != null) {
          AMEngineNodeVo.setLock(node.getLock());
        }
        try {
          if (node.getNodeResource() != null) {
            if (node.getNodeResource().getResourceType() != null) {
              AMEngineNodeVo.setResourceType(node.getNodeResource().getResourceType());
            }
            if (node.getNodeResource().getMaxResource() != null) {
              AMEngineNodeVo.setMaxResource(createUnlimitedResource());
            }
            if (node.getNodeResource().getMinResource() != null) {
              AMEngineNodeVo.setMinResource(createZeroResource());
            }
            if (node.getNodeResource().getUsedResource() != null) {
              Resource realResource = null;
              if (node.getNodeResource().getUsedResource() instanceof DriverAndYarnResource) {
                DriverAndYarnResource dy =
                    (DriverAndYarnResource) node.getNodeResource().getUsedResource();
                realResource = dy.getLoadInstanceResource();
              } else {
                realResource = node.getNodeResource().getUsedResource();
              }
              AMEngineNodeVo.setUsedResource(
                  mapper.readValue(
                      mapper.writeValueAsString(realResource),
                      new TypeReference<Map<String, Object>>() {}));
            } else {
              AMEngineNodeVo.setUsedResource(
                  mapper.readValue(
                      mapper.writeValueAsString(Resource.initResource(ResourceType.Default)),
                      new TypeReference<Map<String, Object>>() {}));
            }
            if (node.getNodeResource().getLockedResource() != null) {
              AMEngineNodeVo.setLockedResource(
                  mapper.readValue(
                      mapper.writeValueAsString(node.getNodeResource().getLockedResource()),
                      new TypeReference<Map<String, Object>>() {}));
            }
            if (node.getNodeResource().getExpectedResource() != null) {
              AMEngineNodeVo.setExpectedResource(
                  mapper.readValue(
                      mapper.writeValueAsString(node.getNodeResource().getExpectedResource()),
                      new TypeReference<Map<String, Object>>() {}));
            }
            if (node.getNodeResource().getLeftResource() != null) {
              AMEngineNodeVo.setLeftResource(
                  mapper.readValue(
                      mapper.writeValueAsString(node.getNodeResource().getLeftResource()),
                      new TypeReference<Map<String, Object>>() {}));
            }
          }
        } catch (Exception e) {
          logger.warn("copyToAMEngineNodeVo writeValueAsString failed", e);
        }
        AMEngineNodeVo.setOwner(node.getOwner());
        if (node.getNodeTaskInfo() != null) {
          if (node.getNodeTaskInfo().getRunningTasks() >= 0) {
            AMEngineNodeVo.setRunningTasks(node.getNodeTaskInfo().getRunningTasks());
          }
          if (node.getNodeTaskInfo().getPendingTasks() >= 0) {
            AMEngineNodeVo.setPendingTasks(node.getNodeTaskInfo().getPendingTasks());
          }
          if (node.getNodeTaskInfo().getSucceedTasks() >= 0) {
            AMEngineNodeVo.setSucceedTasks(node.getNodeTaskInfo().getSucceedTasks());
          }
          if (node.getNodeTaskInfo().getFailedTasks() >= 0) {
            AMEngineNodeVo.setFailedTasks(node.getNodeTaskInfo().getFailedTasks());
          }
        }
        if (node.getNodeOverLoadInfo() != null) {
          if (node.getNodeOverLoadInfo().getMaxMemory() != null) {
            AMEngineNodeVo.setMaxMemory(node.getNodeOverLoadInfo().getMaxMemory());
          }
          if (node.getNodeOverLoadInfo().getUsedMemory() != null) {
            AMEngineNodeVo.setUsedMemory(node.getNodeOverLoadInfo().getUsedMemory());
          }
          if (node.getNodeOverLoadInfo().getSystemCPUUsed() != null) {
            AMEngineNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo().getSystemCPUUsed());
          }
          if (node.getNodeOverLoadInfo().getSystemLeftMemory() != null) {
            AMEngineNodeVo.setSystemCPUUsed(node.getNodeOverLoadInfo().getSystemCPUUsed());
          }
        }
        if (node.getNodeHealthyInfo() != null) {
          if (node.getNodeHealthyInfo().getNodeHealthy() != null) {
            AMEngineNodeVo.setNodeHealthy(node.getNodeHealthyInfo().getNodeHealthy());
          }
          if (node.getNodeHealthyInfo().getMsg() != null) {
            AMEngineNodeVo.setMsg(node.getNodeHealthyInfo().getMsg());
          }
        }
        AMEngineNodeVos.add(AMEngineNodeVo);
      }
    }
    return AMEngineNodeVos;
  }

  public static Map<String, Long> createUnlimitedResource() {
    Map<String, Long> map = new HashMap<String, Long>();
    map.put("core", 128L);
    map.put("memory", 512L * 1024L * 1024L * 1024L);
    map.put("instance", 512L);
    return map;
  }

  public static Map<String, Long> createZeroResource() {
    Map<String, Long> map = new HashMap<String, Long>();
    map.put("core", 1L);
    map.put("memory", 512L * 1024L * 1024L);
    map.put("instance", 0L);
    return map;
  }

  public static boolean isJson(String str) {
    try {
      GSON.fromJson(str, JsonObject.class);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static String getAsyncId() {
    return idPrefix + "_" + idCreator.getAndIncrement();
  }
}
