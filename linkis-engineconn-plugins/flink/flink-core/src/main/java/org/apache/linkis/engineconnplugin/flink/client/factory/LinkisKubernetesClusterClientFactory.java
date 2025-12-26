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

package org.apache.linkis.engineconnplugin.flink.client.factory;

import org.apache.linkis.engineconnplugin.flink.client.config.FlinkVersionThreadLocal;
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.kubernetes.KubernetesClusterClientFactory;
import org.apache.flink.kubernetes.KubernetesClusterDescriptor;
import org.apache.flink.kubernetes.configuration.KubernetesConfigOptions;
import org.apache.flink.kubernetes.kubeclient.FlinkKubeClient;
import org.apache.flink.kubernetes.utils.Constants;
import org.apache.flink.util.AbstractID;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.flink.util.Preconditions.checkNotNull;

public class LinkisKubernetesClusterClientFactory extends KubernetesClusterClientFactory
    implements Closeable {

  private static final String CLUSTER_ID_PREFIX = "flink-cluster-";

  private Configuration configuration;

  private FlinkKubeClient flinkKubeClient;

  private String clusterId;

  private static final Logger LOG =
      LoggerFactory.getLogger(LinkisKubernetesClusterClientFactory.class);

  @Override
  public KubernetesClusterDescriptor createClusterDescriptor(Configuration configuration) {
    this.configuration = configuration;

    checkNotNull(configuration);
    if (!configuration.contains(KubernetesConfigOptions.CLUSTER_ID)) {
      this.clusterId = generateClusterId();
      configuration.setString(KubernetesConfigOptions.CLUSTER_ID, clusterId);
    }
    if (FlinkVersionThreadLocal.getFlinkVersion()
        .equals(FlinkEnvConfiguration.FLINK_1_12_2_VERSION())) {
      try {
        Class<?> clazz =
            Class.forName("org.apache.flink.kubernetes.kubeclient.DefaultKubeClientFactory");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        Object obj = constructor.newInstance();
        Method method = clazz.getDeclaredMethod("fromConfiguration", Configuration.class);
        method.setAccessible(true);
        this.flinkKubeClient = (FlinkKubeClient) method.invoke(obj, configuration);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (FlinkVersionThreadLocal.getFlinkVersion()
        .equals(FlinkEnvConfiguration.FLINK_1_16_2_VERSION())) {
      try {
        Class<?> clazz =
            Class.forName("org.apache.flink.kubernetes.kubeclient.FlinkKubeClientFactory");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        Object obj = constructor.newInstance();
        Method method =
            clazz.getDeclaredMethod("fromConfiguration", Configuration.class, String.class);
        method.setAccessible(true);
        this.flinkKubeClient = (FlinkKubeClient) method.invoke(obj, configuration, "client");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return new KubernetesClusterDescriptor(configuration, flinkKubeClient);
  }

  @Override
  public void close() throws IOException {
    try {
      flinkKubeClient.stopAndCleanupCluster(clusterId);
    } catch (Exception e) {
      LOG.error("Could not kill Kubernetes cluster " + clusterId);
    }

    try {
      flinkKubeClient.close();
    } catch (Exception e) {
      LOG.error("failed to close client, exception {}", e.toString());
    }
  }

  private String generateClusterId() {
    final String randomID = new AbstractID().toString();
    return (CLUSTER_ID_PREFIX + randomID).substring(0, Constants.MAXIMUM_CHARACTERS_OF_CLUSTER_ID);
  }
}
