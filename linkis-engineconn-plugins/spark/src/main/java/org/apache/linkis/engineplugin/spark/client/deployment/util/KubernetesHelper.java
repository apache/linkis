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

package org.apache.linkis.engineplugin.spark.client.deployment.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

import io.fabric8.kubernetes.client.*;

public class KubernetesHelper {

  public static KubernetesClient getKubernetesClientByUrl(
      String k8sMasterUrl, String k8sUsername, String k8sPassword) {
    Config config =
        new ConfigBuilder()
            .withMasterUrl(k8sMasterUrl)
            .withUsername(k8sUsername)
            .withPassword(k8sPassword)
            .build();
    return new DefaultKubernetesClient(config);
  }

  public static KubernetesClient getKubernetesClientByUrl(String k8sMasterUrl) {
    Config config = new ConfigBuilder().withMasterUrl(k8sMasterUrl).build();
    return new DefaultKubernetesClient(config);
  }

  public static KubernetesClient getKubernetesClient(
      String kubeConfigFile, String k8sMasterUrl, String k8sUsername, String k8sPassword) {
    // The ConfigFile mode is preferred
    if (StringUtils.isNotBlank(kubeConfigFile)) {
      return getKubernetesClientByKubeConfigFile(kubeConfigFile);
    }

    if (StringUtils.isNotBlank(k8sMasterUrl)
        && StringUtils.isNotBlank(k8sUsername)
        && StringUtils.isNotBlank(k8sPassword)) {
      return getKubernetesClientByUrl(k8sMasterUrl, k8sUsername, k8sPassword);
    }

    if (StringUtils.isNotBlank(k8sMasterUrl)) {
      return getKubernetesClientByUrl(k8sMasterUrl);
    }

    throw new KubernetesClientException(
        "Both kubeConfigFile and k8sMasterUrl are empty. Initializing KubernetesClient failed.");
  }

  public static KubernetesClient getKubernetesClientByKubeConfigFile(String kubeConfigFile) {
    final Config config;

    if (kubeConfigFile != null) {
      try {
        config =
            Config.fromKubeconfig(null, FileUtils.readFileUtf8(new File(kubeConfigFile)), null);
      } catch (IOException e) {
        throw new KubernetesClientException("Load kubernetes config failed.", e);
      }
    } else {
      config = Config.autoConfigure(null);
    }

    return new DefaultKubernetesClient(config);
  }
}
