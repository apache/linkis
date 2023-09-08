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

import java.util.Map;

/** Spec that describes a Flink session job. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkSessionJobSpec extends AbstractFlinkSpec {

  /** The name of the target session cluster deployment. */
  private String deploymentName;

  public String getDeploymentName() {
    return deploymentName;
  }

  public void setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
  }

  public static FlinkSessionJobSpecBuilder Builder() {
    return new FlinkSessionJobSpecBuilder();
  }

  public static class FlinkSessionJobSpecBuilder {
    private JobSpec job;
    private Long restartNonce;
    private Map<String, String> flinkConfiguration;
    private String deploymentName;

    private FlinkSessionJobSpecBuilder() {}

    public static FlinkSessionJobSpecBuilder aFlinkSessionJobSpec() {
      return new FlinkSessionJobSpecBuilder();
    }

    public FlinkSessionJobSpecBuilder job(JobSpec job) {
      this.job = job;
      return this;
    }

    public FlinkSessionJobSpecBuilder restartNonce(Long restartNonce) {
      this.restartNonce = restartNonce;
      return this;
    }

    public FlinkSessionJobSpecBuilder flinkConfiguration(Map<String, String> flinkConfiguration) {
      this.flinkConfiguration = flinkConfiguration;
      return this;
    }

    public FlinkSessionJobSpecBuilder deploymentName(String deploymentName) {
      this.deploymentName = deploymentName;
      return this;
    }

    public FlinkSessionJobSpec build() {
      FlinkSessionJobSpec flinkSessionJobSpec = new FlinkSessionJobSpec();
      flinkSessionJobSpec.setJob(job);
      flinkSessionJobSpec.setRestartNonce(restartNonce);
      flinkSessionJobSpec.setFlinkConfiguration(flinkConfiguration);
      flinkSessionJobSpec.setDeploymentName(deploymentName);
      return flinkSessionJobSpec;
    }
  }
}
