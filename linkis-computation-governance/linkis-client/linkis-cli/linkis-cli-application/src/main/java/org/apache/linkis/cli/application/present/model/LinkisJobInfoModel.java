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

import org.apache.linkis.cli.application.interactor.job.data.LinkisJobDataImpl;
import org.apache.linkis.cli.common.entity.job.JobStatus;
import org.apache.linkis.cli.common.entity.present.Model;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.TransformerException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Date;

public class LinkisJobInfoModel implements Model {

  private String cid;
  private String jobId;
  private String message;
  private String exception;
  private String cause;

  private String taskID;
  private String instance;
  private String simpleExecId;
  private String execId;
  private String umUser;
  private String executionCode;
  private String logPath;
  private JobStatus status;
  private String engineType;
  private String runType;
  private Long costTime;
  private Date createdTime;
  private Date updatedTime;
  private Date engineStartTime;
  private Integer errCode;
  private String errMsg;
  private String executeApplicationName;
  private String requestApplicationName;
  private Float progress;

  @Override
  public void buildModel(Object data) {
    if (!(data instanceof LinkisJobDataImpl)) {
      throw new TransformerException(
          "TFM0010",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to init LinkisJobInfoModel: "
              + data.getClass().getCanonicalName()
              + "is not instance of \"LinkisJobDataImpl\"");
    }
    this.jobId = ((LinkisJobDataImpl) data).getJobID();
    this.message = ((LinkisJobDataImpl) data).getMessage();
    this.taskID = ((LinkisJobDataImpl) data).getJobID();
    this.instance = ((LinkisJobDataImpl) data).getInstance();
    this.simpleExecId = ((LinkisJobDataImpl) data).getSimpleExecId();
    this.execId = ((LinkisJobDataImpl) data).getExecID();
    this.umUser = ((LinkisJobDataImpl) data).getUmUser();
    this.executionCode = ((LinkisJobDataImpl) data).getExecutionCode();
    this.logPath = ((LinkisJobDataImpl) data).getLogPath();
    this.status = ((LinkisJobDataImpl) data).getJobStatus();
    this.engineType = ((LinkisJobDataImpl) data).getEngineType();
    this.runType = ((LinkisJobDataImpl) data).getRunType();
    this.costTime = ((LinkisJobDataImpl) data).getCostTime();
    this.createdTime = ((LinkisJobDataImpl) data).getCreatedTime();
    this.updatedTime = ((LinkisJobDataImpl) data).getUpdatedTime();
    this.engineStartTime = ((LinkisJobDataImpl) data).getEngineStartTime();
    this.errCode = ((LinkisJobDataImpl) data).getErrCode();
    this.errMsg = ((LinkisJobDataImpl) data).getErrDesc();
    this.executeApplicationName = ((LinkisJobDataImpl) data).getExecuteApplicationName();
    this.requestApplicationName = ((LinkisJobDataImpl) data).getRequestApplicationName();
    this.progress = ((LinkisJobDataImpl) data).getJobProgress();
    Exception e = ((LinkisJobDataImpl) data).getException();
    if (e != null) {
      this.exception = ExceptionUtils.getMessage(e);
      this.cause = ExceptionUtils.getRootCauseMessage(e);
    }
  }
}
