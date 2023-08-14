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

package org.apache.linkis.cli.application.interactor.job.jobcmd;

import java.util.Map;

public class JobCmdDesc {
  private String jobId;
  private String user;
  private JobCmdSubType subType;

  private Map<String, Object> params;

  public JobCmdSubType getSubType() {
    return subType;
  }

  public void setSubType(JobCmdSubType subType) {
    this.subType = subType;
  }

  public String getJobID() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }
}
