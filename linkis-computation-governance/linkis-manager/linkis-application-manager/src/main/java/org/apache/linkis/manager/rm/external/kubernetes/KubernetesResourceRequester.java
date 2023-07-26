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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesResourceRequester implements ExternalResourceRequester {
  private static final Logger logger = LoggerFactory.getLogger(KubernetesResourceRequester.class);
  private ExternalResourceProvider provider = null;
  private DefaultKubernetesClient client = null;
  private String k8sMasterUrl;
  private String k8sClientCertData;
  private String k8sClientKeyData;
  private String k8sCaCertData;

  @Override
  public NodeResource requestResourceInfo(
      ExternalResourceIdentifier identifier, ExternalResourceProvider provider) {
    this.provider = provider;
    if (this.client == null
        || !StringUtils.equals(
            k8sMasterUrl, (String) provider.getConfigMap().get("k8sMasterUrl"))) {
      reloadExternalResourceAddress(provider);
    }

    String namespace = ((KubernetesResourceIdentifier) identifier).getNamespace();
    Pair<KubernetesResource, KubernetesResource> kubernetesResources =
        getResources(this.client, namespace);

    CommonNodeResource nodeResource = new CommonNodeResource();
    nodeResource.setMaxResource(kubernetesResources.getKey());
    nodeResource.setUsedResource(kubernetesResources.getValue());

    return nodeResource;
  }

  public Pair<KubernetesResource, KubernetesResource> getResources(
      DefaultKubernetesClient client, String namespace) {
    long usedMemory = 0;
    long allocatableMemory = 0;
    long usedCPU = 0;
    long allocatableCPU = 0;

    List<ResourceQuota> resourceQuotaList =
        client.resourceQuotas().inNamespace(namespace).list().getItems();

    // Get resource from resourcequota if deployed, otherwise from node status metrics.
    if (CollectionUtils.isNotEmpty(resourceQuotaList)) {
      Map<String, Quantity> usedQuotaResource = resourceQuotaList.get(0).getStatus().getUsed();
      usedCPU = getKubernetesCPUInMilli(usedQuotaResource);
      usedMemory = getKubernetesMemoryInBytes(usedQuotaResource);
      long hardMemory = Long.MAX_VALUE;
      long hardCPU = Long.MAX_VALUE;
      for (ResourceQuota resourceQuota : resourceQuotaList) {
        Map<String, Quantity> hardResource = resourceQuota.getStatus().getHard();
        long c = getKubernetesCPUInMilli(hardResource);
        long m = getKubernetesMemoryInBytes(hardResource);
        if (m < hardMemory) {
          hardMemory = m;
        }
        if (c < hardCPU) {
          hardCPU = c;
        }
      }
      allocatableCPU = hardCPU;
      allocatableMemory = hardMemory;
    } else {
      for (NodeMetrics nodeMetrics : client.top().nodes().metrics().getItems()) {
        usedMemory += getKubernetesMemoryInBytes(nodeMetrics.getUsage());
        usedCPU += getKubernetesCPUInMilli(nodeMetrics.getUsage());
      }
      for (Node node : client.nodes().list().getItems()) {
        allocatableMemory += getKubernetesMemoryInBytes(node.getStatus().getAllocatable());
        allocatableCPU += getKubernetesCPUInMilli(node.getStatus().getAllocatable());
      }
    }

    logger.info(
        "usedMemory: {}, usedCPU: {}, allocatableMemory: {}, allocatableCPU: {}",
        usedMemory,
        usedCPU,
        allocatableMemory,
        allocatableCPU);

    return Pair.of(
        new KubernetesResource(allocatableMemory, allocatableCPU, namespace),
        new KubernetesResource(usedMemory, usedCPU, namespace));
  }

  /**
   * Get the CPU in milli example: 0.5 means 500 milli 500m means 500 milli 1000000n means 1 milli
   * (The cpu would be formated with "n" when query resource from node metrics by fabric8 api)
   *
   * @param resourceMap
   * @return cpu in milli
   */
  private long getKubernetesCPUInMilli(Map<String, Quantity> resourceMap) {
    String cpuKey = resourceMap.containsKey("cpu") ? "cpu" : "requests.cpu";
    return (long) (Quantity.getAmountInBytes(resourceMap.get(cpuKey)).doubleValue() * 1000);
  }

  /**
   * Get the memory in bytes example: 500Ki means 500 * 1024 bytes 500Mi means 500 * 1024 * 1024
   * bytes
   *
   * @param resourceMap
   * @return memory in bytes
   */
  private long getKubernetesMemoryInBytes(Map<String, Quantity> resourceMap) {
    String memoryKey = resourceMap.containsKey("memory") ? "memory" : "requests.memory";
    return Quantity.getAmountInBytes(resourceMap.get(memoryKey)).longValue();
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
      if (client != null) {
        client.close();
        client = null;
      }
    } else {
      k8sMasterUrl = (String) provider.getConfigMap().get("k8sMasterUrl");
      k8sClientCertData = (String) provider.getConfigMap().get("k8sClientCertData");
      k8sClientKeyData = (String) provider.getConfigMap().get("k8sClientKeyData");
      k8sCaCertData = (String) provider.getConfigMap().get("k8sCaCertData");
      if (client != null) {
        client.close();
      }
      client =
          new DefaultKubernetesClient(
              new ConfigBuilder()
                  .withMasterUrl(k8sMasterUrl)
                  .withClientCertData(k8sClientCertData)
                  .withClientKeyData(k8sClientKeyData)
                  .withCaCertData(k8sCaCertData)
                  .build());
    }
    return true;
  }
}
