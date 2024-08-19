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

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryEngineConfigWithGlobalConfig;
import org.apache.linkis.governance.common.protocol.conf.RequestQueryGlobalConfig;
import org.apache.linkis.governance.common.protocol.conf.ResponseQueryConfig;
import org.apache.linkis.manager.common.conf.RMConfiguration;
import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary;
import org.apache.linkis.manager.common.exception.RMWarnException;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator;
import org.apache.linkis.manager.label.utils.LabelUtils;
import org.apache.linkis.protocol.CacheableProtocol;
import org.apache.linkis.rpc.RPCMapCache;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(UserConfiguration.class);

  private static final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  private static final RPCMapCache<String, String, String> globalMapCache =
      new RPCMapCache<String, String, String>(
          Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue()) {
        @Override
        public CacheableProtocol createRequest(String user) {
          return new RequestQueryGlobalConfig(user);
        }

        @Override
        public Map<String, String> createMap(Object any) {
          ResponseQueryConfig response = (ResponseQueryConfig) any;
          return response.getKeyAndValue();
        }
      };

  private static final RPCMapCache<Map<UserCreatorLabel, EngineTypeLabel>, String, String>
      engineMapCache =
          new RPCMapCache<Map<UserCreatorLabel, EngineTypeLabel>, String, String>(
              Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue()) {
            @Override
            public CacheableProtocol createRequest(
                Map<UserCreatorLabel, EngineTypeLabel> labelTuple) {
              UserCreatorLabel userCreatorLabel = null;
              EngineTypeLabel engineTypeLabel = null;
              for (Map.Entry<UserCreatorLabel, EngineTypeLabel> entry : labelTuple.entrySet()) {
                userCreatorLabel = entry.getKey();
                engineTypeLabel = entry.getValue();
              }
              return new RequestQueryEngineConfigWithGlobalConfig(
                  userCreatorLabel, engineTypeLabel, null);
            }

            @Override
            public Map<String, String> createMap(Object any) {
              ResponseQueryConfig response = (ResponseQueryConfig) any;
              return response.getKeyAndValue();
            }
          };

  public static Map<String, String> getGlobalConfig(String user) {
    return globalMapCache.getCacheMap(user);
  }

  public static int getUserGlobalInstanceLimit(String user) {
    Map<String, String> globalConfig = getGlobalConfig(user);
    return (int) RMConfiguration.USER_AVAILABLE_INSTANCE.getValue(globalConfig);
  }

  private static Map<UserCreatorLabel, EngineTypeLabel> buildRequestLabel(
      String user, String creator, String engineType) {
    UserCreatorLabel userCreateLabel = labelFactory.createLabel(UserCreatorLabel.class);
    userCreateLabel.setUser(user);
    userCreateLabel.setCreator(LabelUtils.COMMON_VALUE);
    EngineTypeLabel engineTypeLabel =
        EngineTypeLabelCreator.createEngineTypeLabel(LabelUtils.COMMON_VALUE);
    if (engineType != null) {
      engineTypeLabel.setEngineType(engineType);
    }
    if (creator != null) {
      userCreateLabel.setCreator(creator);
    }
    Map<UserCreatorLabel, EngineTypeLabel> labelTuple =
        new HashMap<UserCreatorLabel, EngineTypeLabel>();
    labelTuple.put(userCreateLabel, engineTypeLabel);
    return labelTuple;
  }

  public static Resource getUserConfiguredResource(
      ResourceType resourceType,
      UserCreatorLabel userCreatorLabel,
      EngineTypeLabel engineTypeLabel) {
    try {
      Map<UserCreatorLabel, EngineTypeLabel> labelTuple =
          new HashMap<UserCreatorLabel, EngineTypeLabel>();
      labelTuple.put(userCreatorLabel, engineTypeLabel);
      Resource userCreatorAvailableResource =
          generateResource(resourceType, engineMapCache.getCacheMap(labelTuple));
      logger.info(
          userCreatorLabel.getUser()
              + "on creator "
              + userCreatorLabel.getCreator()
              + " available engine "
              + engineTypeLabel.getEngineType()
              + " resource:"
              + userCreatorAvailableResource);
      return userCreatorAvailableResource;
    } catch (RMWarnException e) {
      logger.error("Failed to get user configured resource due to: " + e.getMessage(), e);
      throw e;
    }
  }

  public Resource getUserConfiguredResource(
      ResourceType resourceType, String engineType, String user, String creator) {
    try {
      Map<String, String> userConfiguration =
          engineMapCache.getCacheMap(buildRequestLabel(user, creator, engineType));
      Resource userCreatorAvailableResource = generateResource(resourceType, userConfiguration);
      logger.info(user + " on creator available resource:" + userCreatorAvailableResource);
      return userCreatorAvailableResource;
    } catch (Exception e) {
      logger.warn(e.getMessage());
      throw e;
    }
  }

  public static Resource generateResource(
      ResourceType policy, Map<String, String> userConfiguration) {
    switch (policy) {
      case CPU:
        return new CPUResource(RMConfiguration.USER_AVAILABLE_CPU.getValue(userConfiguration));
      case Memory:
        return new MemoryResource(
            RMConfiguration.USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong());
      case Load:
        return new LoadResource(
            RMConfiguration.USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong(),
            RMConfiguration.USER_AVAILABLE_CPU.getValue(userConfiguration));
      case Instance:
        return new InstanceResource(
            RMConfiguration.USER_AVAILABLE_INSTANCE.getValue(userConfiguration));
      case LoadInstance:
        return new LoadInstanceResource(
            RMConfiguration.USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong(),
            RMConfiguration.USER_AVAILABLE_CPU.getValue(userConfiguration),
            RMConfiguration.USER_AVAILABLE_INSTANCE.getValue(userConfiguration));
      case Yarn:
        return new YarnResource(
            RMConfiguration.USER_AVAILABLE_YARN_INSTANCE_MEMORY
                .getValue(userConfiguration)
                .toLong(),
            RMConfiguration.USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
            RMConfiguration.USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration),
            RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration));
      case DriverAndYarn:
        LoadInstanceResource loadInstanceResource =
            new LoadInstanceResource(
                RMConfiguration.USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong(),
                RMConfiguration.USER_AVAILABLE_CPU.getValue(userConfiguration),
                RMConfiguration.USER_AVAILABLE_INSTANCE.getValue(userConfiguration));
        YarnResource yarnResource =
            new YarnResource(
                RMConfiguration.USER_AVAILABLE_YARN_INSTANCE_MEMORY
                    .getValue(userConfiguration)
                    .toLong(),
                RMConfiguration.USER_AVAILABLE_YARN_INSTANCE_CPU.getValue(userConfiguration),
                RMConfiguration.USER_AVAILABLE_YARN_INSTANCE.getValue(userConfiguration),
                RMConfiguration.USER_AVAILABLE_YARN_QUEUE_NAME.getValue(userConfiguration));
        return new DriverAndYarnResource(loadInstanceResource, yarnResource);
      case Kubernetes:
        return new KubernetesResource(
            RMConfiguration.USER_AVAILABLE_KUBERNETES_INSTANCE_MEMORY
                .getValue(userConfiguration)
                .toLong(),
            RMConfiguration.USER_AVAILABLE_KUBERNETES_INSTANCE_CPU.getValue(userConfiguration),
            RMConfiguration.USER_AVAILABLE_KUBERNETES_INSTANCE_NAMESPACE.getValue(
                userConfiguration));
      case DriverAndKubernetes:
        return new DriverAndKubernetesResource(
            new LoadInstanceResource(
                RMConfiguration.USER_AVAILABLE_MEMORY.getValue(userConfiguration).toLong(),
                RMConfiguration.USER_AVAILABLE_CPU.getValue(userConfiguration),
                RMConfiguration.USER_AVAILABLE_INSTANCE.getValue(userConfiguration)),
            new KubernetesResource(
                RMConfiguration.USER_AVAILABLE_KUBERNETES_INSTANCE_MEMORY
                    .getValue(userConfiguration)
                    .toLong(),
                RMConfiguration.USER_AVAILABLE_KUBERNETES_INSTANCE_CPU.getValue(
                    userConfiguration)));
      case Special:
        return new SpecialResource(new HashMap<String, Object>());
      default:
        throw new RMWarnException(
            ManagerCommonErrorCodeSummary.NOT_RESOURCE_RESULT_TYPE.getErrorCode(),
            ManagerCommonErrorCodeSummary.NOT_RESOURCE_RESULT_TYPE.getErrorDesc());
    }
  }
}
