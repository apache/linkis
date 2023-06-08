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

import org.apache.linkis.engineplugin.spark.client.deployment.crds.*;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkOperatorCRDExample {

  private static final Logger logger = LoggerFactory.getLogger(SparkOperatorCRDExample.class);

  public static void main(String[] args) {
    submitSparkCRD("spark-operator-test", "default");
    //        deleteSparkCRD("spark-operator-test","default");

    //        getLog("spark-operator-test", "default");

    //        getSparkCRD("spark-operator-test","default");

  }

  private static void getSparkCRD(String sparkOperatorName, String namespace) {

    KubernetesClient client = KubernetesHelper.getKubernetesClient();

    SparkApplication SparkApplication =
        KubernetesHelper.getSparkApplication(sparkOperatorName, namespace);

    SparkApplicationList list = KubernetesHelper.getSparkApplicationClient(client).list();
    List<SparkApplication> items = list.getItems();

    for (SparkApplication item : items) {
      System.out.println(item);
    }

    client.close();
  }

  private static void getLog(String sparkOperatorName, String namespace) {

    KubernetesClient client = KubernetesHelper.getKubernetesClient();
    client
        .pods()
        .inNamespace(namespace)
        .withName(sparkOperatorName + "-driver")
        .watchLog(System.out);
    client.close();
  }

  private static void deleteSparkCRD(String sparkOperatorName, String namespace) {

    KubernetesClient client = KubernetesHelper.getKubernetesClient();
    SparkApplication SparkApplication =
        KubernetesHelper.getSparkApplication(sparkOperatorName, namespace);
    KubernetesHelper.getSparkApplicationClient(client).delete(SparkApplication);
    client.close();
  }

  private static void test(String sparkOperatorName, String namespace) {

    KubernetesClient client = KubernetesHelper.getKubernetesClient();
    SparkApplication SparkApplication =
        KubernetesHelper.getSparkApplication(sparkOperatorName, namespace);
    //        KubernetesHelper.getSparkApplicationClient(client).
  }

  private static void submitSparkCRD(String sparkOperatorName, String namespace) {
    KubernetesClient client = KubernetesHelper.getKubernetesClient();

    CustomResourceDefinitionList crds =
        client.apiextensions().v1().customResourceDefinitions().list();

    final String sparkApplicationCRDName = CustomResource.getCRDName(SparkApplication.class);
    List<CustomResourceDefinition> sparkCRDList =
        crds.getItems().stream()
            .filter(crd -> crd.getMetadata().getName().equals(sparkApplicationCRDName))
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(sparkCRDList)) {
      throw new RuntimeException("spark operator crd 不存在");
    }

    NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>>
        sparkApplicationClient = KubernetesHelper.getSparkApplicationClient(client);

    SparkApplicationList SparkApplicationList = sparkApplicationClient.list();
    List<SparkApplication> items = SparkApplicationList.getItems();
    System.out.println("  found " + items.size() + " spark crd");
    for (SparkApplication item : items) {
      System.out.println("    " + item);
    }

    SparkApplication sparkApplication =
        KubernetesHelper.getSparkApplication(sparkOperatorName, namespace);

    SparkPodSpec driver =
        SparkPodSpec.Builder()
            .cores(1)
            .coreLimit("1200m")
            .memory("512m")
            .serviceAccount("spark")
            .build();
    SparkPodSpec executor = SparkPodSpec.Builder().cores(1).instances(1).memory("512m").build();
    SparkApplicationSpec sparkApplicationSpec =
        SparkApplicationSpec.Builder()
            .type("Scala")
            .mode("cluster")
            .image("apache/spark:v3.2.1")
            .imagePullPolicy("Always")
            .mainClass("org.apache.spark.examples.SparkPi")
            .mainApplicationFile("local:///opt/spark/examples/jars/spark-examples_2.12-3.2.1.jar")
            .sparkVersion("3.2.1")
            .restartPolicy(new RestartPolicy("Never"))
            .driver(driver)
            .executor(executor)
            .build();

    sparkApplication.setSpec(sparkApplicationSpec);
    SparkApplication created = sparkApplicationClient.createOrReplace(sparkApplication);
    //        sparkApplicationClient.createOrReplace(created);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {

    }

    SparkApplicationList list = KubernetesHelper.getSparkApplicationClient(client).list();

    List<SparkApplication> sparkApplicationList =
        list.getItems().stream()
            .filter(crd -> crd.getMetadata().getName().equals(sparkOperatorName))
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(sparkApplicationList)) {
      SparkApplicationStatus status = sparkApplicationList.get(0).getStatus();
      if (Objects.nonNull(status)) {
        System.out.println(status.getApplicationState().getState());
        logger.info("spark operator status: {}", status.getApplicationState().getState());
      }
    }

    client.close();
  }
}
