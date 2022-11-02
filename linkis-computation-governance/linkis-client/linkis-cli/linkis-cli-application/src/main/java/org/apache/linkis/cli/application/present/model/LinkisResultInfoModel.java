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

package org.apache.linkis.cli.application.present.model;

import org.apache.linkis.cli.application.interactor.job.data.LinkisResultData;
import org.apache.linkis.cli.common.entity.job.JobStatus;
import org.apache.linkis.cli.common.entity.present.Model;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.TransformerException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;

public class LinkisResultInfoModel implements Model {
  private String jobID;
  private String execID;
  private String user;
  private JobStatus jobStatus;
  private String message;
  private Integer errCode;
  private String errDesc;

  @Override
  public void buildModel(Object data) {
    if (!(data instanceof LinkisResultData)) {
      throw new TransformerException(
          "TFM0010",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to init LinkisResultInfoModel: "
              + data.getClass().getCanonicalName()
              + "is not instance of \"LinkisResultData\"");
    }
    LinkisResultData jobData = (LinkisResultData) data;
    jobID = jobData.getJobID();
    execID = jobData.getExecID();
    user = jobData.getUser();
    jobStatus = jobData.getJobStatus();
    message = jobData.getMessage();
    errCode = jobData.getErrCode();
    errDesc = jobData.getErrDesc();
  }

  public String getJobID() {
    return jobID;
  }

  public String getExecID() {
    return execID;
  }

  public String getUser() {
    return user;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public String getMessage() {
    return message;
  }

  public Integer getErrCode() {
    return errCode;
  }

  public String getErrDesc() {
    return errDesc;
  }
}
