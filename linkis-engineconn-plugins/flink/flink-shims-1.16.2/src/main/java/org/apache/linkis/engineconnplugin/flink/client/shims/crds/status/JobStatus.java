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

import org.apache.flink.kubernetes.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.model.annotation.PrinterColumn;

/** Last observed status of the Flink job within an application deployment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobStatus {
  /** Name of the job. */
  private String jobName;

  /** Flink JobId of the Job. */
  private String jobId;

  /** Last observed state of the job. */
  @PrinterColumn(name = "Job Status")
  private String state;

  /** Start time of the job. */
  private String startTime;

  /** Update time of the job. */
  private String updateTime;

  /** Information about pending and last savepoint for the job. */
  private SavepointInfo savepointInfo = new SavepointInfo();

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

  public SavepointInfo getSavepointInfo() {
    return savepointInfo;
  }

  public void setSavepointInfo(SavepointInfo savepointInfo) {
    this.savepointInfo = savepointInfo;
  }
}
