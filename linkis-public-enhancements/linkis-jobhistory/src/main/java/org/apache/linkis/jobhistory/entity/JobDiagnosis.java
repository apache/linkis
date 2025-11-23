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

package org.apache.linkis.jobhistory.entity;

import java.util.Date;

public class JobDiagnosis {

  private Long id;

  private Long jobHistoryId;

  private String diagnosisContent;

  private Date createdTime;

  private Date updatedTime;
  private String onlyRead;

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getJobHistoryId() {
    return jobHistoryId;
  }

  public void setJobHistoryId(Long jobHistoryId) {
    this.jobHistoryId = jobHistoryId;
  }

  public String getDiagnosisContent() {
    return diagnosisContent;
  }

  public void setDiagnosisContent(String diagnosisContent) {
    this.diagnosisContent = diagnosisContent;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedDate(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  public String getOnlyRead() {
    return onlyRead;
  }

  public void setOnlyRead(String onlyRead) {
    this.onlyRead = onlyRead;
  }

  @Override
  public String toString() {
    return "JobDiagnosis{"
        + "id="
        + id
        + ", jobHistoryId="
        + jobHistoryId
        + ", diagnosisContent='"
        + diagnosisContent
        + '\''
        + ", createdTime="
        + createdTime
        + ", updatedTime="
        + updatedTime
        + '}';
  }
}
