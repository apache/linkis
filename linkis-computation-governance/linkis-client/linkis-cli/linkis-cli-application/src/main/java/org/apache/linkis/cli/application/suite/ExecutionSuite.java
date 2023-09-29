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

package org.apache.linkis.cli.application.suite;

import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.result.ResultHandler;

import java.util.Map;

public class ExecutionSuite {
  Execution execution;
  Map<String, Job> jobs;
  ResultHandler[] resultHandlers;

  public ExecutionSuite(
      Execution execution, Map<String, Job> jobs, ResultHandler... resultHandlers) {
    this.execution = execution;
    this.jobs = jobs;
    this.resultHandlers = resultHandlers;
  }

  public Execution getExecution() {
    return execution;
  }

  public void setExecution(Execution execution) {
    this.execution = execution;
  }

  public Map<String, Job> getJobs() {
    return jobs;
  }

  public void setJobs(Map<String, Job> jobs) {
    this.jobs = jobs;
  }

  public ResultHandler[] getResultHandlers() {
    return resultHandlers;
  }

  public void setResultHandlers(ResultHandler[] resultHandlers) {
    this.resultHandlers = resultHandlers;
  }
}
