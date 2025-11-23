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

public class JobStatistics {

  private Long id;

  private Integer sumCount;

  private Integer succeedCount;

  private Integer failedCount;

  private Integer cancelledCount;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getSumCount() {
    return sumCount;
  }

  public void setSumCount(Integer sumCount) {
    this.sumCount = sumCount;
  }

  public Integer getSucceedCount() {
    return succeedCount;
  }

  public void setSucceedCount(Integer succeedCount) {
    this.succeedCount = succeedCount;
  }

  public Integer getFailedCount() {
    return failedCount;
  }

  public void setFailedCount(Integer failedCount) {
    this.failedCount = failedCount;
  }

  public Integer getCancelledCount() {
    return cancelledCount;
  }

  public void setCancelledCount(Integer cancelledCount) {
    this.cancelledCount = cancelledCount;
  }

  @Override
  public String toString() {
    return "JobHistory{" + "id=" + id + '}';
  }
}
