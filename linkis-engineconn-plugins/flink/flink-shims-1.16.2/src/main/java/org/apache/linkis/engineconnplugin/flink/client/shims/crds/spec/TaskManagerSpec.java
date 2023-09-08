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

import org.apache.linkis.engineconnplugin.flink.client.shims.crds.diff.DiffType;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.diff.Diffable;
import org.apache.linkis.engineconnplugin.flink.client.shims.crds.diff.SpecDiff;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.Pod;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.SpecReplicas;

/** TaskManager spec. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskManagerSpec implements Diffable<TaskManagerSpec> {
  /** Resource specification for the TaskManager pods. */
  private Resource resource;

  /** Number of TaskManager replicas. If defined, takes precedence over parallelism */
  @SpecDiff(DiffType.SCALE)
  @SpecReplicas
  private Integer replicas;

  /** TaskManager pod template. It will be merged with FlinkDeploymentSpec.podTemplate. */
  private Pod podTemplate;

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public Integer getReplicas() {
    return replicas;
  }

  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }

  public Pod getPodTemplate() {
    return podTemplate;
  }

  public void setPodTemplate(Pod podTemplate) {
    this.podTemplate = podTemplate;
  }
}
