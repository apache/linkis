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

package org.apache.linkis.engineplugin.spark.client.deployment;

import org.apache.linkis.engineplugin.spark.client.deployment.crds.SparkApplication;
import org.apache.linkis.engineplugin.spark.client.deployment.crds.SparkApplicationList;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class KubernetesHelper {

  public static KubernetesClient getKubernetesClient() {
    Config config =
        new ConfigBuilder()
            .withMasterUrl("http://192.168.217.140:30880")
            .withUsername("admin")
            .withPassword("1QAZ2wsx")
            .build();
    return new DefaultKubernetesClient(config);
  }

  public static NonNamespaceOperation<
          SparkApplication, SparkApplicationList, Resource<SparkApplication>>
      getSparkApplicationClient(KubernetesClient client) {
    return client.customResources(SparkApplication.class, SparkApplicationList.class);
  }

  public static SparkApplication getSparkApplication(String sparkOperatorName, String namespace) {
    SparkApplication sparkApplication = new SparkApplication();
    ObjectMeta metadata = new ObjectMeta();
    metadata.setName(sparkOperatorName);
    metadata.setNamespace(namespace);
    sparkApplication.setMetadata(metadata);
    return sparkApplication;
  }
}
