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

package org.apache.linkis.engineconnplugin.flink.client.shims.util;

import org.apache.commons.io.FileUtils;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkKubernetesHelper {
  private static final Logger logger = LoggerFactory.getLogger(FlinkKubernetesHelper.class);

  public static KubernetesClient getKubernetesClientByKubeConfigFile(String kubeConfigFile) {
    final Config config;

    if (kubeConfigFile != null) {
      try {
        config =
            Config.fromKubeconfig(
                null,
                FileUtils.readFileToString(new File(kubeConfigFile), StandardCharsets.UTF_8),
                null);
      } catch (IOException e) {
        throw new KubernetesClientException("Load kubernetes config failed.", e);
      }
    } else {
      config = Config.autoConfigure(null);
    }

    DefaultKubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
    logger.info(
        "KubernetesClient Create success,kubernetesClient masterUrl: {}",
        kubernetesClient.getMasterUrl().toString());
    return kubernetesClient;
  }
}
