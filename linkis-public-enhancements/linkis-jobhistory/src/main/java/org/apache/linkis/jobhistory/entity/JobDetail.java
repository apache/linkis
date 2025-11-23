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

import org.apache.linkis.jobhistory.util.QueryUtils;

import java.util.Date;

public class JobDetail {

  private Long id;

  private Long jobHistoryId;

  /*
  separated multi result path
   */

  private String resultLocation;

  /*
  how many result sets
   */

  private Integer resultArraySize;

  /*
  code
   */

  private String executionContent;

  /*
  json of jobGroup
   */

  private String jobGroupInfo;

  private Date createdTime;

  private Date updatedTime;

  private String status;

  private Integer priority;

  private String updatedTimeMills;

  public String getUpdatedTimeMills() {
    return QueryUtils.dateToString(getUpdatedTime());
  }

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

  public String getResultLocation() {
    return resultLocation;
  }

  public void setResultLocation(String resultLocation) {
    this.resultLocation = resultLocation;
  }

  public Integer getResultArraySize() {
    return resultArraySize;
  }

  public void setResultArraySize(Integer resultArraySize) {
    this.resultArraySize = resultArraySize;
  }

  public String getExecutionContent() {
    return executionContent;
  }

  public void setExecutionContent(String executionContent) {
    this.executionContent = executionContent;
  }

  public String getJobGroupInfo() {
    return jobGroupInfo;
  }

  public void setJobGroupInfo(String jobGroupInfo) {
    this.jobGroupInfo = jobGroupInfo;
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

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }
}
