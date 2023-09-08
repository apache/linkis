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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.Pod;

import java.util.Map;

/** Spec that describes a Flink application or session cluster deployment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkDeploymentSpec extends AbstractFlinkSpec {
  /** Flink docker image used to start the Job and TaskManager pods. */
  private String image;

  /** Image pull policy of the Flink docker image. */
  private String imagePullPolicy;

  /** Kubernetes service used by the Flink deployment. */
  private String serviceAccount;

  /** Flink image version. */
  private FlinkVersion flinkVersion;

  /** Ingress specs. */
  private IngressSpec ingress;

  /**
   * Base pod template for job and task manager pods. Can be overridden by the jobManager and
   * taskManager pod templates.
   */
  private Pod podTemplate;

  /** JobManager specs. */
  private JobManagerSpec jobManager;

  /** TaskManager specs. */
  private TaskManagerSpec taskManager;

  /**
   * Log configuration overrides for the Flink deployment. Format logConfigFileName ->
   * configContent.
   */
  private Map<String, String> logConfiguration;

  /** Deployment mode of the Flink cluster, native or standalone. */
  private KubernetesDeploymentMode mode;

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getImagePullPolicy() {
    return imagePullPolicy;
  }

  public void setImagePullPolicy(String imagePullPolicy) {
    this.imagePullPolicy = imagePullPolicy;
  }

  public String getServiceAccount() {
    return serviceAccount;
  }

  public void setServiceAccount(String serviceAccount) {
    this.serviceAccount = serviceAccount;
  }

  public FlinkVersion getFlinkVersion() {
    return flinkVersion;
  }

  public void setFlinkVersion(FlinkVersion flinkVersion) {
    this.flinkVersion = flinkVersion;
  }

  public IngressSpec getIngress() {
    return ingress;
  }

  public void setIngress(IngressSpec ingress) {
    this.ingress = ingress;
  }

  public Pod getPodTemplate() {
    return podTemplate;
  }

  public void setPodTemplate(Pod podTemplate) {
    this.podTemplate = podTemplate;
  }

  public JobManagerSpec getJobManager() {
    return jobManager;
  }

  public void setJobManager(JobManagerSpec jobManager) {
    this.jobManager = jobManager;
  }

  public TaskManagerSpec getTaskManager() {
    return taskManager;
  }

  public void setTaskManager(TaskManagerSpec taskManager) {
    this.taskManager = taskManager;
  }

  public Map<String, String> getLogConfiguration() {
    return logConfiguration;
  }

  public void setLogConfiguration(Map<String, String> logConfiguration) {
    this.logConfiguration = logConfiguration;
  }

  public KubernetesDeploymentMode getMode() {
    return mode;
  }

  public void setMode(KubernetesDeploymentMode mode) {
    this.mode = mode;
  }
}
