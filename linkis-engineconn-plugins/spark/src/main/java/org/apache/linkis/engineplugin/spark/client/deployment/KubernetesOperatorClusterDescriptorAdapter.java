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

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.engineplugin.spark.client.context.ExecutionContext;
import org.apache.linkis.engineplugin.spark.client.context.SparkConfig;
import org.apache.linkis.engineplugin.spark.client.deployment.crds.*;
import org.apache.spark.launcher.CustomSparkSubmitLauncher;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class KubernetesOperatorClusterDescriptorAdapter extends ClusterDescriptorAdapter {

  public KubernetesOperatorClusterDescriptorAdapter(ExecutionContext executionContext) {
    super(executionContext);
  }

  public void deployCluster(String mainClass, String args, Map<String, String> confMap) {
    SparkConfig sparkConfig = executionContext.getSparkConfig();

    KubernetesClient client = KubernetesHelper.getKubernetesClient();

    CustomResourceDefinitionList crds = client.apiextensions().v1().customResourceDefinitions().list();

    final String sparkApplicationCRDName = CustomResource.getCRDName(SparkApplication.class);
    List<CustomResourceDefinition> sparkCRDList = crds.getItems().stream().filter(crd -> crd.getMetadata().getName().equals(sparkApplicationCRDName)).collect(Collectors.toList());
    if (CollectionUtils.isEmpty(sparkCRDList)) {
      throw new RuntimeException("spark operator crd 不存在");
    }

    NonNamespaceOperation<SparkApplication, SparkApplicationList, Resource<SparkApplication>> sparkApplicationClient = KubernetesHelper.getSparkApplicationClient(client);
    SparkApplication sparkApplication = KubernetesHelper.getSparkApplication(sparkConfig.getAppName(), sparkConfig.getK8sNamespace());

    SparkPodSpec driver = SparkPodSpec.Builder().cores(sparkConfig.getDriverCores()).memory(sparkConfig.getDriverMemory()).serviceAccount("spark").build();
    SparkPodSpec executor = SparkPodSpec.Builder().cores(sparkConfig.getExecutorCores()).instances(sparkConfig.getNumExecutors()).memory(sparkConfig.getExecutorMemory()).build();
    SparkApplicationSpec sparkApplicationSpec = SparkApplicationSpec.Builder()
            .type("Scala")
            .mode(sparkConfig.getDeployMode())
            .image(sparkConfig.getK8sImage())
            .imagePullPolicy("Always")
            .mainClass(mainClass)
            .mainApplicationFile(sparkConfig.getAppResource())
            .sparkVersion("3.2.1")
            .restartPolicy(new RestartPolicy("Never"))
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

    List<SparkApplication> sparkApplicationList = list.getItems().stream().filter(crd -> crd.getMetadata().getName().equals(sparkConfig.getAppName())).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(sparkApplicationList)){
      SparkApplicationStatus status = sparkApplicationList.get(0).getStatus();
      if (Objects.nonNull(status)){
        System.out.println(status.getApplicationState().getState());
        logger.info("spark operator status: {}",status.getApplicationState().getState());
      }
    }

    client.close();
  }


  public boolean initJobId() {
    this.applicationId = sparkAppHandle.getAppId();
    // When the job is not finished, the appId is monitored; otherwise, the status is
    // monitored(当任务没结束时，监控appId，反之，则监控状态，这里主要防止任务过早结束，导致一直等待)
    return null != getApplicationId() || (jobState != null && jobState.isFinal());
  }
}
