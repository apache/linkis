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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds.status;

import org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec.FlinkDeploymentSpec;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/** Last observed status of the Flink deployment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkDeploymentStatus extends CommonStatus<FlinkDeploymentSpec> {

  /** Information from running clusters. */
  private Map<String, String> clusterInfo = new HashMap<>();

  /** Last observed status of the JobManager deployment. */
  private JobManagerDeploymentStatus jobManagerDeploymentStatus =
      JobManagerDeploymentStatus.MISSING;

  /** Status of the last reconcile operation. */
  private FlinkDeploymentReconciliationStatus reconciliationStatus =
      new FlinkDeploymentReconciliationStatus();

  /** Information about the TaskManagers for the scale subresource. */
  private TaskManagerInfo taskManager;

  public Map<String, String> getClusterInfo() {
    return clusterInfo;
  }

  public void setClusterInfo(Map<String, String> clusterInfo) {
    this.clusterInfo = clusterInfo;
  }

  public JobManagerDeploymentStatus getJobManagerDeploymentStatus() {
    return jobManagerDeploymentStatus;
  }

  public void setJobManagerDeploymentStatus(JobManagerDeploymentStatus jobManagerDeploymentStatus) {
    this.jobManagerDeploymentStatus = jobManagerDeploymentStatus;
  }

  @Override
  public FlinkDeploymentReconciliationStatus getReconciliationStatus() {
    return reconciliationStatus;
  }

  public void setReconciliationStatus(FlinkDeploymentReconciliationStatus reconciliationStatus) {
    this.reconciliationStatus = reconciliationStatus;
  }

  public TaskManagerInfo getTaskManager() {
    return taskManager;
  }

  public void setTaskManager(TaskManagerInfo taskManager) {
    this.taskManager = taskManager;
  }
}
