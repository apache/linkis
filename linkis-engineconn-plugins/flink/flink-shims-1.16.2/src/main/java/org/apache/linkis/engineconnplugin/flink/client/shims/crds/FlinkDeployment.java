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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds;

import org.apache.linkis.engineconnplugin.flink.client.shims.crds.spec.FlinkDeploymentSpec;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.status.FlinkDeploymentStatus;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.Namespaced;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.Group;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.Kind;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.ShortNames;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.Version;

/** Custom resource definition that represents both Application and Session deployments. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize()
@Group(CrdConstants.API_GROUP)
@Version(CrdConstants.API_VERSION)
@ShortNames({"flinkdep"})
@Kind(CrdConstants.KIND_FLINK_DEPLOYMENT)
public class FlinkDeployment
    extends AbstractFlinkResource<FlinkDeploymentSpec, FlinkDeploymentStatus>
    implements Namespaced {

  @Override
  public FlinkDeploymentStatus initStatus() {
    return new FlinkDeploymentStatus();
  }

  @Override
  public FlinkDeploymentSpec initSpec() {
    return new FlinkDeploymentSpec();
  }
}
