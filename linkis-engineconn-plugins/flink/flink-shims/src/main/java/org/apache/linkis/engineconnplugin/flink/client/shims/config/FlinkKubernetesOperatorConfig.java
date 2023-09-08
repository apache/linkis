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

package org.apache.linkis.engineconnplugin.flink.client.shims.config;

import java.util.Map;

public class FlinkKubernetesOperatorConfig {
  private String k8sConfigFile;

  private String k8sNamespace;

  private String k8sImage;

  private String k8sServiceAccount;

  private String jar;

  private String flinkAppName;

  private String jobmanagerMemory;

  private String taskmanagerMemory;

  private String jobmanagerCpu;

  private String taskmanagerCpu;

  private Integer parallelism;

  private Map<String, String> flinkConfiguration;

  public String getFlinkAppName() {
    return flinkAppName;
  }

  public String getJobmanagerMemory() {
    return jobmanagerMemory;
  }

  public String getTaskmanagerMemory() {
    return taskmanagerMemory;
  }

  public String getJobmanagerCpu() {
    return jobmanagerCpu;
  }

  public String getTaskmanagerCpu() {
    return taskmanagerCpu;
  }

  public Map<String, String> getFlinkConfiguration() {
    return flinkConfiguration;
  }

  public String getK8sConfigFile() {
    return k8sConfigFile;
  }

  public String getK8sNamespace() {
    return k8sNamespace;
  }

  public String getK8sImage() {
    return k8sImage;
  }

  public String getK8sServiceAccount() {
    return k8sServiceAccount;
  }

  public String getJar() {
    return jar;
  }

  public Integer getParallelism() {
    return parallelism;
  }

  public static FlinkKubernetesOperatorConfigBuilder Builder() {
    return new FlinkKubernetesOperatorConfigBuilder();
  }

  public static class FlinkKubernetesOperatorConfigBuilder {
    private String k8sConfigFile;
    private String k8sNamespace;
    private String k8sImage;
    private String k8sServiceAccount;
    private String jar;
    private String flinkAppName;
    private String jobmanagerMemory;
    private String taskmanagerMemory;
    private String jobmanagerCpu;
    private String taskmanagerCpu;
    private Integer parallelism;
    private Map<String, String> flinkConfiguration;

    private FlinkKubernetesOperatorConfigBuilder() {}

    public static FlinkKubernetesOperatorConfigBuilder aFlinkKubernetesOperatorConfig() {
      return new FlinkKubernetesOperatorConfigBuilder();
    }

    public FlinkKubernetesOperatorConfigBuilder k8sConfigFile(String k8sConfigFile) {
      this.k8sConfigFile = k8sConfigFile;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder k8sNamespace(String k8sNamespace) {
      this.k8sNamespace = k8sNamespace;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder k8sImage(String k8sImage) {
      this.k8sImage = k8sImage;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder k8sServiceAccount(String k8sServiceAccount) {
      this.k8sServiceAccount = k8sServiceAccount;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder jar(String jar) {
      this.jar = jar;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder flinkAppName(String flinkAppName) {
      this.flinkAppName = flinkAppName;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder jobmanagerMemory(String jobmanagerMemory) {
      this.jobmanagerMemory = jobmanagerMemory;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder taskmanagerMemory(String taskmanagerMemory) {
      this.taskmanagerMemory = taskmanagerMemory;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder jobmanagerCpu(String jobmanagerCpu) {
      this.jobmanagerCpu = jobmanagerCpu;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder taskmanagerCpu(String taskmanagerCpu) {
      this.taskmanagerCpu = taskmanagerCpu;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder parallelism(Integer parallelism) {
      this.parallelism = parallelism;
      return this;
    }

    public FlinkKubernetesOperatorConfigBuilder flinkConfiguration(
        Map<String, String> flinkConfiguration) {
      this.flinkConfiguration = flinkConfiguration;
      return this;
    }

    public FlinkKubernetesOperatorConfig build() {
      FlinkKubernetesOperatorConfig flinkKubernetesOperatorConfig =
          new FlinkKubernetesOperatorConfig();
      flinkKubernetesOperatorConfig.taskmanagerMemory = this.taskmanagerMemory;
      flinkKubernetesOperatorConfig.jobmanagerMemory = this.jobmanagerMemory;
      flinkKubernetesOperatorConfig.flinkAppName = this.flinkAppName;
      flinkKubernetesOperatorConfig.parallelism = this.parallelism;
      flinkKubernetesOperatorConfig.taskmanagerCpu = this.taskmanagerCpu;
      flinkKubernetesOperatorConfig.k8sConfigFile = this.k8sConfigFile;
      flinkKubernetesOperatorConfig.k8sImage = this.k8sImage;
      flinkKubernetesOperatorConfig.k8sServiceAccount = this.k8sServiceAccount;
      flinkKubernetesOperatorConfig.jar = this.jar;
      flinkKubernetesOperatorConfig.jobmanagerCpu = this.jobmanagerCpu;
      flinkKubernetesOperatorConfig.k8sNamespace = this.k8sNamespace;
      flinkKubernetesOperatorConfig.flinkConfiguration = this.flinkConfiguration;
      return flinkKubernetesOperatorConfig;
    }
  }
}
