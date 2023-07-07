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

import org.apache.linkis.cli.application.entity.job.JobStatus;
import org.apache.linkis.cli.application.entity.present.Model;
import org.apache.linkis.cli.application.exception.TransformerException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;

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
    if (!(data instanceof LinkisOperResultAdapter)) {
      throw new TransformerException(
          "TFM0010",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to init LinkisJobInfoModel: "
              + data.getClass().getCanonicalName()
              + "is not instance of \"LinkisOperResultAdapter\"");
    }
    this.jobId = ((LinkisOperResultAdapter) data).getJobID();
    this.taskID = ((LinkisOperResultAdapter) data).getJobID();
    this.instance = ((LinkisOperResultAdapter) data).getInstance();
    this.simpleExecId = ((LinkisOperResultAdapter) data).getSimpleExecId();
    this.execId = ((LinkisOperResultAdapter) data).getStrongerExecId();
    this.umUser = ((LinkisOperResultAdapter) data).getUmUser();
    this.executionCode = ((LinkisOperResultAdapter) data).getExecutionCode();
    this.logPath = ((LinkisOperResultAdapter) data).getLogPath();
    this.status = ((LinkisOperResultAdapter) data).getJobStatus();
    this.engineType = ((LinkisOperResultAdapter) data).getEngineType();
    this.runType = ((LinkisOperResultAdapter) data).getRunType();
    this.costTime = ((LinkisOperResultAdapter) data).getCostTime();
    this.createdTime = ((LinkisOperResultAdapter) data).getCreatedTime();
    this.updatedTime = ((LinkisOperResultAdapter) data).getUpdatedTime();
    this.engineStartTime = ((LinkisOperResultAdapter) data).getEngineStartTime();
    this.errCode = ((LinkisOperResultAdapter) data).getErrCode();
    this.errMsg = ((LinkisOperResultAdapter) data).getErrDesc();
    this.executeApplicationName = ((LinkisOperResultAdapter) data).getExecuteApplicationName();
    this.requestApplicationName = ((LinkisOperResultAdapter) data).getRequestApplicationName();
    this.progress = ((LinkisOperResultAdapter) data).getJobProgress();
  }
}
