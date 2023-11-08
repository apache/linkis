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
import org.apache.linkis.engineplugin.spark.client.deployment.util.KubernetesHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.spark.launcher.CustomSparkSubmitLauncher;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.PodResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesApplicationClusterDescriptorAdapter extends ClusterDescriptorAdapter {
  private static final Logger logger =
      LoggerFactory.getLogger(KubernetesApplicationClusterDescriptorAdapter.class);

  protected SparkConfig sparkConfig;
  protected KubernetesClient client;
  protected String driverPodName;
  protected String namespace;

  public KubernetesApplicationClusterDescriptorAdapter(ExecutionContext executionContext) {
    super(executionContext);
    this.sparkConfig = executionContext.getSparkConfig();
    this.client =
        KubernetesHelper.getKubernetesClient(
            this.sparkConfig.getK8sConfigFile(),
            this.sparkConfig.getK8sMasterUrl(),
            this.sparkConfig.getK8sUsername(),
            this.sparkConfig.getK8sPassword());
  }

  public void deployCluster(String mainClass, String args, Map<String, String> confMap)
      throws IOException {
    SparkConfig sparkConfig = executionContext.getSparkConfig();
    sparkLauncher = new CustomSparkSubmitLauncher();
    sparkLauncher
        .setJavaHome(sparkConfig.getJavaHome())
        .setSparkHome(sparkConfig.getSparkHome())
        .setMaster(sparkConfig.getK8sMasterUrl())
        .setDeployMode("cluster")
        .setAppName(sparkConfig.getAppName())
        .setVerbose(true);
    this.driverPodName = generateDriverPodName(sparkConfig.getAppName());
    this.namespace = sparkConfig.getK8sNamespace();
    setConf(sparkLauncher, "spark.app.name", sparkConfig.getAppName());
    setConf(sparkLauncher, "spark.ui.port", sparkConfig.getK8sSparkUIPort());
    setConf(sparkLauncher, "spark.kubernetes.namespace", this.namespace);
    setConf(sparkLauncher, "spark.kubernetes.container.image", sparkConfig.getK8sImage());
    setConf(sparkLauncher, "spark.kubernetes.driver.pod.name", this.driverPodName);
    setConf(
        sparkLauncher,
        "spark.kubernetes.driver.request.cores",
        sparkConfig.getK8sDriverRequestCores());
    setConf(
        sparkLauncher,
        "spark.kubernetes.executor.request.cores",
        sparkConfig.getK8sExecutorRequestCores());
    setConf(
        sparkLauncher,
        "spark.kubernetes.container.image.pullPolicy",
        sparkConfig.getK8sImagePullPolicy());
    setConf(
        sparkLauncher,
        "spark.kubernetes.authenticate.driver.serviceAccountName",
        sparkConfig.getK8sServiceAccount());
    if (confMap != null) confMap.forEach((k, v) -> sparkLauncher.setConf(k, v));

    addSparkArg(sparkLauncher, "--jars", sparkConfig.getJars());
    addSparkArg(sparkLauncher, "--packages", sparkConfig.getPackages());
    addSparkArg(sparkLauncher, "--exclude-packages", sparkConfig.getExcludePackages());
    addSparkArg(sparkLauncher, "--repositories", sparkConfig.getRepositories());
    addSparkArg(sparkLauncher, "--files", sparkConfig.getFiles());
    addSparkArg(sparkLauncher, "--archives", sparkConfig.getArchives());
    addSparkArg(sparkLauncher, "--driver-memory", sparkConfig.getDriverMemory());
    addSparkArg(sparkLauncher, "--driver-java-options", sparkConfig.getDriverJavaOptions());
    addSparkArg(sparkLauncher, "--driver-library-path", sparkConfig.getDriverLibraryPath());
    addSparkArg(sparkLauncher, "--driver-class-path", sparkConfig.getDriverClassPath());
    addSparkArg(sparkLauncher, "--executor-memory", sparkConfig.getExecutorMemory());
    addSparkArg(sparkLauncher, "--proxy-user", sparkConfig.getProxyUser());
    addSparkArg(sparkLauncher, "--driver-cores", sparkConfig.getDriverCores().toString());
    addSparkArg(sparkLauncher, "--total-executor-cores", sparkConfig.getTotalExecutorCores());
    addSparkArg(sparkLauncher, "--executor-cores", sparkConfig.getExecutorCores().toString());
    addSparkArg(sparkLauncher, "--num-executors", sparkConfig.getNumExecutors().toString());
    addSparkArg(sparkLauncher, "--principal", sparkConfig.getPrincipal());
    addSparkArg(sparkLauncher, "--keytab", sparkConfig.getKeytab());
    addSparkArg(sparkLauncher, "--py-files", sparkConfig.getPyFiles());
    sparkLauncher.setAppResource(sparkConfig.getAppResource());
    sparkLauncher.setMainClass(mainClass);
    Arrays.stream(args.split("\\s+"))
        .filter(StringUtils::isNotBlank)
        .forEach(arg -> sparkLauncher.addAppArgs(arg));
    sparkAppHandle =
        sparkLauncher.startApplication(
            new SparkAppHandle.Listener() {
              @Override
              public void stateChanged(SparkAppHandle sparkAppHandle) {}

              @Override
              public void infoChanged(SparkAppHandle sparkAppHandle) {}
            });
    sparkLauncher.setSparkAppHandle(sparkAppHandle);
  }

  private void addSparkArg(SparkLauncher sparkLauncher, String key, String value) {
    if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
      sparkLauncher.addSparkArg(key, value);
    }
  }

  private void setConf(SparkLauncher sparkLauncher, String key, String value) {
    if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
      sparkLauncher.setConf(key, value);
    }
  }

  public boolean initJobId() {
    Pod sparkDriverPod = getSparkDriverPod();
    if (null == sparkDriverPod) {
      return false;
    }
    String sparkDriverPodPhase = sparkDriverPod.getStatus().getPhase();
    String sparkApplicationId = sparkDriverPod.getMetadata().getLabels().get("spark-app-selector");

    if (Strings.isNotBlank(sparkApplicationId)) {
      this.applicationId = sparkApplicationId;
    }
    if (Strings.isNotBlank(sparkDriverPodPhase)) {
      this.jobState = kubernetesPodStateConvertSparkState(sparkDriverPodPhase);
    }

    // When the job is not finished, the appId is monitored; otherwise, the status is
    // monitored(当任务没结束时，监控appId，反之，则监控状态，这里主要防止任务过早结束，导致一直等待)
    return null != getApplicationId() || (jobState != null && jobState.isFinal());
  }

  protected Pod getSparkDriverPod() {
    return client.pods().inNamespace(namespace).withName(driverPodName).get();
  }

  public String getSparkUIUrl() {
    Pod sparkDriverPod = getSparkDriverPod();
    if (null != sparkDriverPod) {
      String sparkDriverPodIP = sparkDriverPod.getStatus().getPodIP();
      if (StringUtils.isNotBlank(sparkDriverPodIP)) {
        return sparkDriverPodIP + ":" + this.sparkConfig.getK8sSparkUIPort();
      } else {
        logger.info("spark driver pod IP is null, the application may be pending");
      }
    } else {
      logger.info("spark driver pod is not exist");
    }
    return "";
  }

  @Override
  public SparkAppHandle.State getJobState() {
    Pod sparkDriverPod = getSparkDriverPod();
    if (null != sparkDriverPod) {
      String sparkDriverPodPhase = sparkDriverPod.getStatus().getPhase();
      this.jobState = kubernetesPodStateConvertSparkState(sparkDriverPodPhase);
      logger.info("Job {} state is {}.", getApplicationId(), this.jobState);
      return this.jobState;
    }
    return null;
  }

  @Override
  public void close() {
    logger.info("Start to close job {}.", getApplicationId());
    client.close();
    if (isDisposed()) {
      logger.info("Job has finished, close action return.");
      return;
    }
    PodResource<Pod> sparkDriverPodResource =
        client.pods().inNamespace(namespace).withName(driverPodName);
    if (null != sparkDriverPodResource.get()) {
      sparkDriverPodResource.delete();
    }
  }

  @Override
  public boolean isDisposed() {
    return this.jobState.isFinal();
  }

  public SparkAppHandle.State kubernetesPodStateConvertSparkState(String kubernetesState) {
    if (StringUtils.isBlank(kubernetesState)) {
      return SparkAppHandle.State.UNKNOWN;
    }
    switch (kubernetesState.toUpperCase()) {
      case "PENDING":
        return SparkAppHandle.State.CONNECTED;
      case "RUNNING":
        return SparkAppHandle.State.RUNNING;
      case "SUCCEEDED":
        return SparkAppHandle.State.FINISHED;
      case "FAILED":
        return SparkAppHandle.State.FAILED;
      default:
        return SparkAppHandle.State.UNKNOWN;
    }
  }

  public String generateDriverPodName(String appName) {
    return appName + "-" + UUID.randomUUID().toString().replace("-", "") + "-driver";
  }
}
