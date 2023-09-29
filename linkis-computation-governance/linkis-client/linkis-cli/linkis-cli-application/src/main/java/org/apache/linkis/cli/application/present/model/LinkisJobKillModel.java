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

public class LinkisJobKillModel implements Model {

  private String cid;
  private String jobId;
  private String message;
  private String exception;
  private String cause;

  private String execID;
  private String user;
  private JobStatus jobStatus;

  @Override
  public void buildModel(Object data) {
    if (!(data instanceof LinkisJobDataImpl)) {
      throw new TransformerException(
          "TFM0010",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to init LinkisJobKillModel: "
              + data.getClass().getCanonicalName()
              + "is not instance of \"LinkisJobDataImpl\"");
    }
    this.jobId = ((LinkisJobDataImpl) data).getJobID();
    this.message = ((LinkisJobDataImpl) data).getMessage();
    this.execID = ((LinkisJobDataImpl) data).getExecID();
    this.user = ((LinkisJobDataImpl) data).getUser();
    this.jobStatus = ((LinkisJobDataImpl) data).getJobStatus();
    Exception e = ((LinkisJobDataImpl) data).getException();
    if (e != null) {
      this.exception = ExceptionUtils.getMessage(e);
      this.cause = ExceptionUtils.getRootCauseMessage(e);
    }
  }
}
