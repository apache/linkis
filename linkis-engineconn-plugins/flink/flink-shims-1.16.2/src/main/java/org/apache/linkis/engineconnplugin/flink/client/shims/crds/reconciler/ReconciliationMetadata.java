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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds.reconciler;

import org.apache.linkis.engineconnplugin.flink.client.shims.crds.AbstractFlinkResource;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.status.ReconciliationState;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.status.ReconciliationStatus;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.ObjectMeta;

/** Extra metadata to be attached to the reconciled spec. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReconciliationMetadata {

  private String apiVersion;

  private ObjectMeta metadata;

  private boolean firstDeployment;

  public ReconciliationMetadata(String apiVersion, ObjectMeta metadata, boolean firstDeployment) {
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.firstDeployment = firstDeployment;
  }

  public static ReconciliationMetadata from(AbstractFlinkResource<?, ?> resource) {
    ObjectMeta metadata = new ObjectMeta();
    metadata.setGeneration(resource.getMetadata().getGeneration());

    boolean firstDeploy =
        resource.getStatus().getReconciliationStatus().isBeforeFirstDeployment()
            || isFirstDeployment(resource);

    return new ReconciliationMetadata(resource.getApiVersion(), metadata, firstDeploy);
  }

  private static boolean isFirstDeployment(AbstractFlinkResource<?, ?> resource) {
    ReconciliationStatus<?> reconStatus = resource.getStatus().getReconciliationStatus();
    return reconStatus.getState() == ReconciliationState.DEPLOYED
        && reconStatus.deserializeLastReconciledSpecWithMeta().getMeta().firstDeployment;
  }
}
