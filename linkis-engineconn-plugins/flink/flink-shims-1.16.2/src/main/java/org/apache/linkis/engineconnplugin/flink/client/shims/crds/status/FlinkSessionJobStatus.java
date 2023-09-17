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

import org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec.FlinkSessionJobSpec;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Last observed status of the Flink Session job. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkSessionJobStatus extends CommonStatus<FlinkSessionJobSpec> {

  /** Status of the last reconcile operation. */
  private FlinkSessionJobReconciliationStatus reconciliationStatus =
      new FlinkSessionJobReconciliationStatus();

  @Override
  public FlinkSessionJobReconciliationStatus getReconciliationStatus() {
    return reconciliationStatus;
  }

  public void setReconciliationStatus(FlinkSessionJobReconciliationStatus reconciliationStatus) {
    this.reconciliationStatus = reconciliationStatus;
  }
}