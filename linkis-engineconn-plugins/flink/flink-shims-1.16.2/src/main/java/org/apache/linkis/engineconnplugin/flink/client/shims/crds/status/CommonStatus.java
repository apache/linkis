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

import org.apache.linkis.engineconnplugin.flink.client.shims.crds.lifecycle.ResourceLifecycleState;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec.AbstractFlinkSpec;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec.JobState;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.PrinterColumn;

import static org.apache.linkis.engineconnplugin.flink.client.shims.crds.lifecycle.ResourceLifecycleState.ROLLING_BACK;
import static org.apache.linkis.engineconnplugin.flink.client.shims.crds.lifecycle.ResourceLifecycleState.UPGRADING;

/** Last observed common status of the Flink deployment/Flink SessionJob. */
public abstract class CommonStatus<SPEC extends AbstractFlinkSpec> {

  /** Last observed status of the Flink job on Application/Session cluster. */
  private JobStatus jobStatus = new JobStatus();

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(JobStatus jobStatus) {
    this.jobStatus = jobStatus;
  }

  /** Error information about the FlinkDeployment/FlinkSessionJob. */
  private String error;

  /** Lifecycle state of the Flink resource (including being rolled back, failed etc.). */
  @PrinterColumn(name = "Lifecycle State")
  // Calculated from the status, requires no setter. The purpose of this is to expose as a printer
  // column.
  private ResourceLifecycleState lifecycleState;

  /**
   * Current reconciliation status of this resource.
   *
   * @return Current {@link ReconciliationStatus}.
   */
  public abstract ReconciliationStatus<SPEC> getReconciliationStatus();

  public ResourceLifecycleState getLifecycleState() {
    ReconciliationStatus reconciliationStatus = getReconciliationStatus();

    if (reconciliationStatus.isBeforeFirstDeployment()) {
      return StringUtils.isEmpty(error)
          ? ResourceLifecycleState.CREATED
          : ResourceLifecycleState.FAILED;
    }

    switch (reconciliationStatus.getState()) {
      case UPGRADING:
        return UPGRADING;
      case ROLLING_BACK:
        return ROLLING_BACK;
    }

    AbstractFlinkSpec lastReconciledSpec = reconciliationStatus.deserializeLastReconciledSpec();
    if (lastReconciledSpec.getJob() != null
        && lastReconciledSpec.getJob().getState() == JobState.SUSPENDED) {
      return ResourceLifecycleState.SUSPENDED;
    }

    String jobState = getJobStatus().getState();
    if (jobState != null && FlinkJobStatus.valueOf(jobState).equals(FlinkJobStatus.FAILED)) {
      return ResourceLifecycleState.FAILED;
    }

    if (reconciliationStatus.getState() == ReconciliationState.ROLLED_BACK) {
      return ResourceLifecycleState.ROLLED_BACK;
    } else if (reconciliationStatus.isLastReconciledSpecStable()) {
      return ResourceLifecycleState.STABLE;
    }

    return ResourceLifecycleState.DEPLOYED;
  }
}
