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

/** Flink job spec. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobSpec implements Diffable<JobSpec> {

  /**
   * Optional URI of the job jar within the Flink docker container. For example:
   * local:///opt/flink/examples/streaming/StateMachineExample.jar. If not specified the job jar
   * should be available in the system classpath.
   */
  private String jarURI;

  /** Parallelism of the Flink job. */
  @SpecDiff(DiffType.SCALE)
  private int parallelism;

  /** Fully qualified main class name of the Flink job. */
  private String entryClass;

  /** Arguments for the Flink job main class. */
  private String[] args = new String[0];

  /** Desired state for the job. */
  private JobState state = JobState.RUNNING;

  /**
   * Nonce used to manually trigger savepoint for the running job. In order to trigger a savepoint,
   * change the number to anything other than the current value.
   */
  @SpecDiff(DiffType.IGNORE)
  private Long savepointTriggerNonce;

  /**
   * Savepoint path used by the job the first time it is deployed. Upgrades/redeployments will not
   * be affected.
   */
  @SpecDiff(DiffType.IGNORE)
  private String initialSavepointPath;

  /** Upgrade mode of the Flink job. */
  @SpecDiff(DiffType.IGNORE)
  private UpgradeMode upgradeMode = UpgradeMode.STATELESS;

  /** Allow checkpoint state that cannot be mapped to any job vertex in tasks. */
  @SpecDiff(DiffType.IGNORE)
  private Boolean allowNonRestoredState;

  public String getJarURI() {
    return jarURI;
  }

  public void setJarURI(String jarURI) {
    this.jarURI = jarURI;
  }

  public int getParallelism() {
    return parallelism;
  }

  public void setParallelism(int parallelism) {
    this.parallelism = parallelism;
  }

  public String getEntryClass() {
    return entryClass;
  }

  public void setEntryClass(String entryClass) {
    this.entryClass = entryClass;
  }

  public String[] getArgs() {
    return args;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }

  public JobState getState() {
    return state;
  }

  public void setState(JobState state) {
    this.state = state;
  }

  public Long getSavepointTriggerNonce() {
    return savepointTriggerNonce;
  }

  public void setSavepointTriggerNonce(Long savepointTriggerNonce) {
    this.savepointTriggerNonce = savepointTriggerNonce;
  }

  public String getInitialSavepointPath() {
    return initialSavepointPath;
  }

  public void setInitialSavepointPath(String initialSavepointPath) {
    this.initialSavepointPath = initialSavepointPath;
  }

  public UpgradeMode getUpgradeMode() {
    return upgradeMode;
  }

  public void setUpgradeMode(UpgradeMode upgradeMode) {
    this.upgradeMode = upgradeMode;
  }

  public Boolean getAllowNonRestoredState() {
    return allowNonRestoredState;
  }

  public void setAllowNonRestoredState(Boolean allowNonRestoredState) {
    this.allowNonRestoredState = allowNonRestoredState;
  }

  public static JobSpecBuilder Builder() {
    return new JobSpecBuilder();
  }

  public static class JobSpecBuilder {
    private String jarURI;
    private int parallelism;
    private String entryClass;
    private String[] args;
    private JobState state;
    private Long savepointTriggerNonce;
    private String initialSavepointPath;
    private UpgradeMode upgradeMode;
    private Boolean allowNonRestoredState;

    private JobSpecBuilder() {}

    public static JobSpecBuilder aJobSpec() {
      return new JobSpecBuilder();
    }

    public JobSpecBuilder jarURI(String jarURI) {
      this.jarURI = jarURI;
      return this;
    }

    public JobSpecBuilder parallelism(int parallelism) {
      this.parallelism = parallelism;
      return this;
    }

    public JobSpecBuilder entryClass(String entryClass) {
      this.entryClass = entryClass;
      return this;
    }

    public JobSpecBuilder args(String[] args) {
      this.args = args;
      return this;
    }

    public JobSpecBuilder state(JobState state) {
      this.state = state;
      return this;
    }

    public JobSpecBuilder savepointTriggerNonce(Long savepointTriggerNonce) {
      this.savepointTriggerNonce = savepointTriggerNonce;
      return this;
    }

    public JobSpecBuilder initialSavepointPath(String initialSavepointPath) {
      this.initialSavepointPath = initialSavepointPath;
      return this;
    }

    public JobSpecBuilder upgradeMode(UpgradeMode upgradeMode) {
      this.upgradeMode = upgradeMode;
      return this;
    }

    public JobSpecBuilder allowNonRestoredState(Boolean allowNonRestoredState) {
      this.allowNonRestoredState = allowNonRestoredState;
      return this;
    }

    public JobSpec build() {
      JobSpec jobSpec = new JobSpec();
      jobSpec.setJarURI(jarURI);
      jobSpec.setParallelism(parallelism);
      jobSpec.setEntryClass(entryClass);
      jobSpec.setArgs(args);
      jobSpec.setState(state);
      jobSpec.setSavepointTriggerNonce(savepointTriggerNonce);
      jobSpec.setInitialSavepointPath(initialSavepointPath);
      jobSpec.setUpgradeMode(upgradeMode);
      jobSpec.setAllowNonRestoredState(allowNonRestoredState);
      return jobSpec;
    }
  }
}
