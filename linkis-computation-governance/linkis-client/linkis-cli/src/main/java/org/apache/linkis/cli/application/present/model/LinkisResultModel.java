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
import org.apache.linkis.cli.application.interactor.job.common.ResultData;
import org.apache.linkis.cli.application.interactor.job.common.ResultSet;

import java.util.List;

public class LinkisResultModel implements Model {
  private ResultData data;

  @Override
  public void buildModel(Object data) {
    if (!(data instanceof ResultData)) {
      throw new TransformerException(
          "TFM0010",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          "Failed to init LinkisResultModel: "
              + data.getClass().getCanonicalName()
              + "is not instance of \"LinkisResultData\"");
    }
    this.data = (ResultData) data;
  }

  public List<ResultSet> consumeResultContent() {
    return data.consumeResultContent();
  }

  public boolean resultFinReceived() {
    return data.isResultFin();
  }

  public JobStatus getJobStatus() {
    return data.getJobStatus();
  }

  public String getJobID() {
    return data.getJobID();
  }

  public String getUser() {
    return data.getUser();
  }

  public boolean hasResult() {
    return data.hasResult();
  }

  public String getExecID() {
    return data.getExecID();
  }

  public Integer getErrCode() {
    return data.getErrCode();
  }

  public String getErrDesc() {
    return data.getErrDesc();
  }

  public String getExtraMessage() {
    return data.getExtraMessage();
  }
}
