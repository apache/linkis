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

package org.apache.linkis.engineconnplugin.flink.client.shims.crds.lifecycle;

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Enum encapsulating the lifecycle state of a Flink resource. */
public enum ResourceLifecycleState {
  CREATED(false, "The resource was created in Kubernetes but not yet handled by the operator"),
  SUSPENDED(true, "The resource (job) has been suspended"),
  UPGRADING(false, "The resource is being upgraded"),
  DEPLOYED(
      false,
      "The resource is deployed/submitted to Kubernetes, but it’s not yet considered to be stable and might be rolled back in the future"),
  STABLE(true, "The resource deployment is considered to be stable and won’t be rolled back"),
  ROLLING_BACK(false, "The resource is being rolled back to the last stable spec"),
  ROLLED_BACK(true, "The resource is deployed with the last stable spec"),
  FAILED(true, "The job terminally failed");

  @JsonIgnore private final boolean terminal;
  @JsonIgnore private final String description;

  ResourceLifecycleState(boolean terminal, String description) {
    this.terminal = terminal;
    this.description = description;
  }

  public Set<ResourceLifecycleState> getClearedStatesAfterTransition(
      ResourceLifecycleState transitionFrom) {
    if (this == transitionFrom) {
      return Collections.emptySet();
    }

    EnumSet<ResourceLifecycleState> states = EnumSet.allOf(ResourceLifecycleState.class);
    if (terminal) {
      states.remove(this);
      return states;
    }

    if (this == UPGRADING) {
      states.remove(UPGRADING);
      states.remove(transitionFrom);
      return states;
    }

    return Collections.emptySet();
  }
}
