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

package org.apache.linkis.engineconnplugin.flink.client.deployment;

import org.apache.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import org.apache.linkis.engineconnplugin.flink.client.shims.config.FlinkKubernetesOperatorConfig;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException;
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.configuration.*;
import org.apache.flink.kubernetes.configuration.KubernetesConfigOptions;
import org.apache.flink.util.Preconditions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesOperatorClusterDescriptorAdapter
    extends AbstractApplicationClusterDescriptorAdapter {

  private static final Logger logger =
      LoggerFactory.getLogger(KubernetesOperatorClusterDescriptorAdapter.class);

  KubernetesOperatorClusterDescriptorAdapter(ExecutionContext executionContext) {
    super(executionContext);
  }

  public void deployCluster(String[] programArguments, String applicationClassName)
      throws JobExecutionException {
    FlinkKubernetesOperatorConfig flinkKubernetesOperatorConfig =
        convertFlinkConfig(this.executionContext.getFlinkConfig());
    this.executionContext
        .getFlinkShims()
        .deployKubernetesOperator(
            programArguments, applicationClassName, flinkKubernetesOperatorConfig);
  }

  public boolean initJobId() {
    try {
      this.executionContext.getFlinkShims().startFlinkKubernetesOperatorWatcher();
    } catch (Exception e) {
      try {
        // Prevent watch interruption due to network interruption.Restart Watcher.
        Thread.sleep(5000);
        this.executionContext.getFlinkShims().startFlinkKubernetesOperatorWatcher();
      } catch (InterruptedException interruptedException) {
        logger.error("Use k8s watch obtain the status failed");
      }
    }
    return null != this.executionContext.getFlinkShims().getJobId();
  }

  @Override
  public JobID getJobId() {
    return this.executionContext.getFlinkShims().getJobId();
  }

  @Override
  public JobStatus getJobStatus() throws JobExecutionException {
    return this.executionContext.getFlinkShims().getJobStatus();
  }

  @Override
  public void cancelJob() throws JobExecutionException {
    this.executionContext.getFlinkShims().close();
  }

  @Override
  public void close() {
    this.executionContext.getFlinkShims().close();
  }

  @Override
  public boolean isGloballyTerminalState() {
    return false;
  }

  private FlinkKubernetesOperatorConfig convertFlinkConfig(Configuration flinkConfig) {

    List<String> pipelineJars =
        flinkConfig.getOptional(PipelineOptions.JARS).orElse(Collections.emptyList());
    Preconditions.checkArgument(pipelineJars.size() == 1, "Should only have one jar");

    String flinkAppName =
        flinkConfig.getString(
            FlinkEnvConfiguration.FLINK_APP_NAME().key(),
            FlinkEnvConfiguration.FLINK_APP_NAME().defaultValue());

    Map<String, String> map = flinkConfig.toMap();
    map.remove(FlinkEnvConfiguration.FLINK_APP_NAME().key());
    map.remove(KubernetesConfigOptions.NAMESPACE.key());
    map.remove(KubernetesConfigOptions.KUBE_CONFIG_FILE.key());
    map.remove(TaskManagerOptions.BIND_HOST.key());
    map.remove(TaskManagerOptions.HOST.key());
    map.remove(JobManagerOptions.ADDRESS.key());
    map.remove(JobManagerOptions.PORT.key());
    map.remove(JobManagerOptions.BIND_HOST.key());
    map.remove(RestOptions.BIND_ADDRESS.key());
    map.remove(RestOptions.ADDRESS.key());
    map.remove(DeploymentOptions.TARGET.key());
    map.remove(DeploymentOptionsInternal.CONF_DIR.key());

    return FlinkKubernetesOperatorConfig.Builder()
        .k8sNamespace(flinkConfig.getOptional(KubernetesConfigOptions.NAMESPACE).orElse("default"))
        .k8sConfigFile(
            flinkConfig
                .getOptional(KubernetesConfigOptions.KUBE_CONFIG_FILE)
                .orElse(System.getProperty("user.home").concat("/.kube/config")))
        .k8sImage(
            flinkConfig
                .getOptional(KubernetesConfigOptions.CONTAINER_IMAGE)
                .orElse("flink:1.16-scala_2.12-java8"))
        .jobmanagerMemory(
            flinkConfig.getOptional(JobManagerOptions.TOTAL_PROCESS_MEMORY).get().getMebiBytes()
                + "M")
        .taskmanagerMemory(
            flinkConfig.getOptional(TaskManagerOptions.TOTAL_PROCESS_MEMORY).get().getMebiBytes()
                + "M")
        .jobmanagerCpu(
            String.valueOf(flinkConfig.getDouble(KubernetesConfigOptions.JOB_MANAGER_CPU)))
        .taskmanagerCpu(
            String.valueOf(flinkConfig.getDouble(KubernetesConfigOptions.TASK_MANAGER_CPU)))
        .jar(pipelineJars.get(0))
        .flinkConfiguration(map)
        .flinkAppName(flinkAppName.toLowerCase())
        .parallelism(flinkConfig.getOptional(CoreOptions.DEFAULT_PARALLELISM).orElse(1))
        .k8sServiceAccount(
            flinkConfig
                .getOptional(KubernetesConfigOptions.KUBERNETES_SERVICE_ACCOUNT)
                .orElse("default"))
        .build();
  }
}
