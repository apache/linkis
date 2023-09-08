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

package org.apache.linkis.engineconnplugin.flink.client.shims.deployment;

import org.apache.linkis.engineconnplugin.flink.client.shims.config.FlinkKubernetesOperatorConfig;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.FlinkDeployment;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.FlinkDeploymentList;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec.*;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.status.FlinkDeploymentStatus;
import org.apache.linkis.engineconnplugin.flink.client.shims.util.FlinkKubernetesHelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.ObjectMeta;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.CustomResource;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.Watcher;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.WatcherException;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkDeploymentOperatorClusterDescriptorAdapter implements Closeable {
  private static final Logger logger =
      LoggerFactory.getLogger(FlinkDeploymentOperatorClusterDescriptorAdapter.class);

  private FlinkKubernetesOperatorConfig config;

  private KubernetesClient client;

  private JobID jobId;

  private JobStatus jobStatus;

  public FlinkDeploymentOperatorClusterDescriptorAdapter(FlinkKubernetesOperatorConfig config) {
    this.config = config;
    this.client =
        FlinkKubernetesHelper.getKubernetesClientByKubeConfigFile(config.getK8sConfigFile());
  }

  public void deployCluster(String[] programArguments, String applicationClassName) {
    logger.info("The flink k8s operator task start，k8sNamespace: {}", config.getK8sNamespace());
    CustomResourceDefinitionList crds =
        client.apiextensions().v1().customResourceDefinitions().list();

    String FlinkDeploymentCRDName = CustomResource.getCRDName(FlinkDeployment.class);
    List<CustomResourceDefinition> flinkCRDList =
        crds.getItems().stream()
            .filter(crd -> crd.getMetadata().getName().equals(FlinkDeploymentCRDName))
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(flinkCRDList)) {
      throw new RuntimeException("The flink operator crd does not exist");
    }

    NonNamespaceOperation<
            FlinkDeployment,
            FlinkDeploymentList,
            org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.dsl.Resource<
                FlinkDeployment>>
        flinkDeploymentClient = getFlinkDeploymentClient();

    FlinkDeployment flinkDeployment =
        getFlinkDeployment(config.getFlinkAppName(), config.getK8sNamespace());

    FlinkDeploymentSpec flinkDeploymentSpec = new FlinkDeploymentSpec();
    flinkDeploymentSpec.setFlinkVersion(FlinkVersion.v1_16);
    flinkDeploymentSpec.setImage(config.getK8sImage());
    flinkDeploymentSpec.setFlinkConfiguration(config.getFlinkConfiguration());
    flinkDeployment.setSpec(flinkDeploymentSpec);
    flinkDeploymentSpec.setServiceAccount(config.getK8sServiceAccount());
    JobManagerSpec jobManagerSpec = new JobManagerSpec();
    jobManagerSpec.setResource(
        new Resource(
            Double.valueOf(config.getJobmanagerCpu()),
            config.getJobmanagerMemory(),
            config.getJobmanagerMemory()));
    flinkDeploymentSpec.setJobManager(jobManagerSpec);
    TaskManagerSpec taskManagerSpec = new TaskManagerSpec();
    taskManagerSpec.setResource(
        new Resource(
            Double.valueOf(config.getTaskmanagerCpu()),
            config.getTaskmanagerMemory(),
            config.getTaskmanagerMemory()));
    flinkDeploymentSpec.setTaskManager(taskManagerSpec);
    flinkDeployment
        .getSpec()
        .setJob(
            JobSpec.Builder()
                .jarURI(config.getJar())
                .parallelism(config.getParallelism())
                .upgradeMode(UpgradeMode.STATELESS)
                .build());

    logger.info("Flink k8s operator task parameters: {}", flinkDeploymentSpec);
    flinkDeployment.setSpec(flinkDeploymentSpec);

    FlinkDeployment created = flinkDeploymentClient.createOrReplace(flinkDeployment);
    logger.info("Preparing to submit the Flink k8s operator Task: {}", created);

    // Wait three seconds to get the status
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {

    }

    FlinkDeploymentList list = getFlinkDeploymentClient().list();

    List<FlinkDeployment> flinkDeployments =
        list.getItems().stream()
            .filter(crd -> crd.getMetadata().getName().equals(config.getFlinkAppName()))
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(flinkDeployments)) {
      FlinkDeploymentStatus status = flinkDeployments.get(0).getStatus();
      if (Objects.nonNull(status)) {
        jobId = JobID.fromHexString(status.getJobStatus().getJobId());
        jobStatus = JobStatus.valueOf(status.getJobStatus().getState());
        logger.info(
            "Flink k8s operator task: {},status: {}",
            config.getFlinkAppName(),
            jobStatus.toString());
      }
    }
  }

  public void startFlinkKubernetesOperatorWatcher() {
    getFlinkDeploymentClient()
        .inNamespace(this.config.getK8sNamespace())
        .withName(this.config.getFlinkAppName())
        .watch(
            new Watcher<FlinkDeployment>() {
              @Override
              public void eventReceived(Action action, FlinkDeployment FlinkDeployment) {
                if (Objects.nonNull(FlinkDeployment.getStatus())) {
                  jobStatus =
                      JobStatus.valueOf(FlinkDeployment.getStatus().getJobStatus().getState());
                  jobId =
                      JobID.fromHexString(FlinkDeployment.getStatus().getJobStatus().getJobId());
                  logger.info(
                      "Flink kubernetes operator task name:{},jobId:{},state:{}",
                      config.getFlinkAppName(),
                      jobId,
                      jobStatus);
                }
              }

              @Override
              public void onClose(WatcherException e) {
                // Invoked when the watcher closes due to an Exception.Restart Watcher.
                logger.error("Use k8s watch obtain the status failed", e);
                startFlinkKubernetesOperatorWatcher();
              }
            });
  }

  @Override
  public void close() {
    logger.info("Start to close job {}.", config.getFlinkAppName());
    FlinkDeployment FlinkDeployment =
        getFlinkDeployment(config.getFlinkAppName(), config.getK8sNamespace());
    getFlinkDeploymentClient().delete(FlinkDeployment);
    client.close();
  }

  public JobID getJobId() {
    return jobId;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public MixedOperation<
          FlinkDeployment,
          FlinkDeploymentList,
          org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.client.dsl.Resource<
              FlinkDeployment>>
      getFlinkDeploymentClient() {
    return client.customResources(FlinkDeployment.class, FlinkDeploymentList.class);
  }

  public FlinkDeployment getFlinkDeployment(String name, String namespace) {
    FlinkDeployment FlinkDeployment = new FlinkDeployment();
    ObjectMeta metadata = new ObjectMeta();
    metadata.setName(name);
    metadata.setNamespace(namespace);
    FlinkDeployment.setMetadata(metadata);
    return FlinkDeployment;
  }
}
