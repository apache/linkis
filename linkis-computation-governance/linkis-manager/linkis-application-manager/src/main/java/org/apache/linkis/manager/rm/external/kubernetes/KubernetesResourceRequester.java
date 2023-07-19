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

package org.apache.linkis.manager.rm.external.kubernetes;

import org.apache.linkis.manager.common.entity.resource.*;
import org.apache.linkis.manager.rm.external.domain.ExternalAppInfo;
import org.apache.linkis.manager.rm.external.domain.ExternalResourceIdentifier;
import org.apache.linkis.manager.rm.external.domain.ExternalResourceProvider;
import org.apache.linkis.manager.rm.external.request.ExternalResourceRequester;

import java.util.*;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesResourceRequester implements ExternalResourceRequester {
  private static final Logger logger = LoggerFactory.getLogger(KubernetesResourceRequester.class);
  private ExternalResourceProvider provider = null;
  private String k8sMasterUrl;
  private String k8sClientCertData;
  private String k8sClientKeyData;
  private String k8sCaCertData;

  @Override
  public NodeResource requestResourceInfo(
      ExternalResourceIdentifier identifier, ExternalResourceProvider provider) {
    k8sMasterUrl = (String) provider.getConfigMap().get("k8sMasterUrl");
    k8sClientCertData = (String) provider.getConfigMap().get("k8sClientCertData");
    k8sClientKeyData = (String) provider.getConfigMap().get("k8sClientKeyData");
    k8sCaCertData = (String) provider.getConfigMap().get("k8sCaCertData");
    this.provider = provider;
    DefaultKubernetesClient client =
        new DefaultKubernetesClient(
            new ConfigBuilder()
                .withMasterUrl(k8sMasterUrl)
                .withClientCertData(k8sClientCertData)
                .withClientKeyData(k8sClientKeyData)
                .withCaCertData(k8sCaCertData)
                .build());
    long usedMemory = 0;
    long allocatableMemory = 0;
    long usedCPU = 0;
    long allocatableCPU = 0;
    for (NodeMetrics item : client.top().nodes().metrics().getItems()) {
      usedMemory += Long.parseLong(item.getUsage().get("memory").getAmount());
      usedCPU += Long.parseLong(item.getUsage().get("cpu").getAmount()) / 1000000L;
    }
    for (Node item : client.nodes().list().getItems()) {
      allocatableMemory +=
          Long.parseLong(item.getStatus().getAllocatable().get("memory").getAmount());
      allocatableCPU += Long.parseLong(item.getStatus().getAllocatable().get("cpu").getAmount());
    }
    logger.info(
        "usedMemory: {}, usedCPU: {}, allocatableMemory: {}, allocatableCPU: {}",
        usedMemory,
        usedCPU,
        allocatableMemory,
        allocatableCPU);
    CommonNodeResource nodeResource = new CommonNodeResource();
    nodeResource.setMaxResource(
        new KubernetesResource(allocatableMemory * 1024L, allocatableCPU * 1000L));
    nodeResource.setUsedResource(new KubernetesResource(usedMemory * 1024L, usedCPU));
    client.close();
    return nodeResource;
  }

  @Override
  public List<ExternalAppInfo> requestAppInfo(
      ExternalResourceIdentifier identifier, ExternalResourceProvider provider) {
    // TODO
    return null;
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Kubernetes;
  }

  @Override
  public Boolean reloadExternalResourceAddress(ExternalResourceProvider provider) {
    if (null == provider) {
      k8sMasterUrl = null;
      k8sClientCertData = null;
      k8sClientKeyData = null;
      k8sCaCertData = null;
    } else {
      k8sMasterUrl = (String) provider.getConfigMap().get("k8sMasterUrl");
      k8sClientCertData = (String) provider.getConfigMap().get("k8sClientCertData");
      k8sClientKeyData = (String) provider.getConfigMap().get("k8sClientKeyData");
      k8sCaCertData = (String) provider.getConfigMap().get("k8sCaCertData");
    }
    return true;
  }
}
