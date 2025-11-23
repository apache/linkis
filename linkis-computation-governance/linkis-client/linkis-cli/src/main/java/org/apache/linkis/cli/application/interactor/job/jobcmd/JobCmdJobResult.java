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

import org.apache.linkis.cli.application.entity.job.JobResult;

import java.util.Map;

public class JobCmdJobResult implements JobResult {
  private Boolean success;
  private String message;
  private Map<String, String> extraMessage;

  public JobCmdJobResult(Boolean success, String message, Map<String, String> extraMessage) {
    this.success = success;
    this.message = message;
    this.extraMessage = extraMessage;
  }

  @Override
  public Boolean isSuccess() {
    return success;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public Map<String, String> getExtraMessage() {
    return extraMessage;
  }

  public void setExtraMessage(Map<String, String> extraMessage) {
    this.extraMessage = extraMessage;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }
}
