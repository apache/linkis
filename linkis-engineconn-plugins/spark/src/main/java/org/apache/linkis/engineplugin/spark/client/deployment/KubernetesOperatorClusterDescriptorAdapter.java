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

import org.apache.linkis.engineplugin.spark.client.context.ExecutionContext;
import org.apache.linkis.engineplugin.spark.client.context.SparkConfig;
import org.apache.linkis.engineplugin.spark.client.deployment.crds.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.launcher.SparkAppHandle;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class KubernetesOperatorClusterDescriptorAdapter extends ClusterDescriptorAdapter {

  protected String taskName;
  protected String namespace;

  public KubernetesOperatorClusterDescriptorAdapter(ExecutionContext executionContext) {
    super(executionContext);
  }

  public void deployCluster(String mainClass, String args, Map<String, String> confMap) {
    SparkConfig sparkConfig = executionContext.getSparkConfig();
    this.taskName = sparkConfig.getAppName();
    this.namespace = sparkConfig.getK8sNamespace();

    KubernetesClient client = KubernetesHelper.getKubernetesClient();
    CustomResourceDefinitionList crds =
        client.apiextensions().v1().customResourceDefinitions().list();

    String sparkApplicationCRDName = CustomResource.getCRDName(SparkApplication.class);
    List<CustomResourceDefinition> sparkCRDList =
        crds.getItems().stream()
            .filter(crd -> crd.getMetadata().getName().equals(sparkApplicationCRDName))
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(sparkCRDList)) {
      throw new RuntimeException("The Spark operator crd does not exist");
    }

    NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>>
        sparkApplicationClient = KubernetesHelper.getSparkApplicationClient(client);
    SparkApplication sparkApplication =
        KubernetesHelper.getSparkApplication(
            sparkConfig.getAppName(), sparkConfig.getK8sNamespace());

    SparkPodSpec driver =
        SparkPodSpec.Builder()
            .cores(sparkConfig.getDriverCores())
            .memory(sparkConfig.getDriverMemory())
            .serviceAccount("spark")
            .build();
    SparkPodSpec executor =
        SparkPodSpec.Builder()
            .cores(sparkConfig.getExecutorCores())
            .instances(sparkConfig.getNumExecutors())
            .memory(sparkConfig.getExecutorMemory())
            .build();
    SparkApplicationSpec sparkApplicationSpec =
        SparkApplicationSpec.Builder()
            .type(sparkConfig.getK8sLanguageType())
            // todo An error occurs when the client mode is used. The cause has not been found
            .mode("cluster")
            .image(sparkConfig.getK8sImage())
            .imagePullPolicy(sparkConfig.getK8sImagePullPolicy())
            .mainClass(mainClass)
            .mainApplicationFile(sparkConfig.getAppResource())
            .sparkVersion(sparkConfig.getK8sSparkVersion())
            .restartPolicy(new RestartPolicy(sparkConfig.getK8sRestartPolicy()))
            .driver(driver)
            .executor(executor)
            .build();

    sparkApplication.setSpec(sparkApplicationSpec);
    SparkApplication created = sparkApplicationClient.createOrReplace(sparkApplication);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {

    }

    SparkApplicationList list = KubernetesHelper.getSparkApplicationClient(client).list();

    List<SparkApplication> sparkApplicationList =
        list.getItems().stream()
            .filter(crd -> crd.getMetadata().getName().equals(sparkConfig.getAppName()))
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(sparkApplicationList)) {
      SparkApplicationStatus status = sparkApplicationList.get(0).getStatus();
      if (Objects.nonNull(status)) {
        logger.info(
            "Spark k8s task: {},status: {}",
            sparkConfig.getAppName(),
            status.getApplicationState().getState());
      }
    }

    client.close();
  }

  public boolean initJobId() {
    KubernetesClient client = KubernetesHelper.getKubernetesClient();
    List<SparkApplication> sparkApplicationList =
        KubernetesHelper.getSparkApplicationClient(client).list().getItems();

    if (CollectionUtils.isNotEmpty(sparkApplicationList)) {
      for (SparkApplication sparkApplication : sparkApplicationList) {
        if (sparkApplication.getMetadata().getNamespace().equals(this.namespace)
            && sparkApplication.getMetadata().getName().equals(this.taskName)) {
          this.applicationId = sparkApplication.getStatus().getSparkApplicationId();
          this.jobState =
              kubernetesStateConvertSparkState(
                  sparkApplication.getStatus().getApplicationState().getState());
        }
      }
    }
    // When the job is not finished, the appId is monitored; otherwise, the status is
    // monitored(当任务没结束时，监控appId，反之，则监控状态，这里主要防止任务过早结束，导致一直等待)
    return null != getApplicationId() || (jobState != null && jobState.isFinal());
  }

  public SparkAppHandle.State kubernetesStateConvertSparkState(String kubernetesState) {
    if (StringUtils.isBlank(kubernetesState)) {
      return SparkAppHandle.State.UNKNOWN;
    }
    switch (kubernetesState) {
      case "PENDING":
        return SparkAppHandle.State.CONNECTED;
      case "RUNNING":
        return SparkAppHandle.State.RUNNING;
      case "COMPLETED":
        return SparkAppHandle.State.FINISHED;
      case "FAILED":
        return SparkAppHandle.State.FAILED;
      default:
        return SparkAppHandle.State.UNKNOWN;
    }
  }
}
