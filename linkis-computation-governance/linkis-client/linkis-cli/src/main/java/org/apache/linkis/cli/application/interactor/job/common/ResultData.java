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

package org.apache.linkis.cli.application.interactor.job.common;

import org.apache.linkis.cli.application.entity.job.JobStatus;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class ResultData {

  private final String user;
  private final String jobId;
  private final String execId;
  private final LinkedBlockingDeque<ResultSet> resultContent = new LinkedBlockingDeque<>();
  private String extraMessage;
  private JobStatus jobStatus = null;
  private String resultLocation;
  private String[] resultSetPaths = null; // remote paths for job result set
  private Boolean hasNextResultPage;
  private Integer errCode = null;
  private String errDesc = null;
  private boolean hasResult = true;

  private volatile Boolean resultFin = false;

  public ResultData(String user, String jobId, String execId) {
    this.user = user;
    this.jobId = jobId;
    this.execId = execId;
  }

  public String getJobID() {
    return jobId;
  }

  public String getUser() {
    return user;
  }

  public final String getExecID() {
    return execId;
  }

  public final String getResultLocation() {
    return resultLocation;
  }

  public final void setResultLocation(String resultLocation) {
    this.resultLocation = resultLocation;
  }

  public String[] getResultSetPaths() {
    return resultSetPaths;
  }

  public final void setResultSetPaths(String[] resultSetPaths) {
    this.resultSetPaths = resultSetPaths;
  }

  public Integer getErrCode() {
    return errCode;
  }

  public void setErrCode(Integer errCode) {
    this.errCode = errCode;
  }

  public String getErrDesc() {
    return errDesc;
  }

  public void setErrDesc(String errDesc) {
    this.errDesc = errDesc;
  }

  public List<ResultSet> consumeResultContent() {
    List<ResultSet> ret = new LinkedList<>();
    resultContent.drainTo(ret, resultContent.size());
    return ret;
  }

  public void appendResultContent(ResultSet resultContent) {
    this.resultContent.add(resultContent);
  }

  public Boolean hasNextResultPage() {
    return hasNextResultPage;
  }

  public void setHasNextResultPage(Boolean hasNextResultPage) {
    this.hasNextResultPage = hasNextResultPage;
  }

  public void setResultFin() {
    this.resultFin = true;
  }

  public boolean isResultFin() {
    return this.resultFin;
  }

  public boolean hasResult() {
    return hasResult;
  }

  public void setHasResult(boolean hasResult) {
    this.hasResult = hasResult;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(JobStatus jobStatus) {
    this.jobStatus = jobStatus;
  }

  public String getExtraMessage() {
    return extraMessage;
  }

  public void setExtraMessage(String extraMessage) {
    this.extraMessage = extraMessage;
  }

  public void updateByOperResult(LinkisOperResultAdapter adapter) {
    if (adapter.getResultLocation() != null) {
      setResultLocation(adapter.getResultLocation());
    }
    if (adapter.getResultSetPaths() != null) {
      setResultSetPaths(adapter.getResultSetPaths());
    }
    if (adapter.getErrCode() != null) {
      setErrCode(adapter.getErrCode());
    }
    if (adapter.getErrDesc() != null) {
      setErrDesc(adapter.getErrDesc());
    }
    if (adapter.getResultContent() != null && adapter.resultHasNextPage() != null) {
      setHasNextResultPage(adapter.resultHasNextPage());
      appendResultContent(adapter.getResultContent());
    }
    if (adapter.getJobStatus() != null) {
      setJobStatus(adapter.getJobStatus());
    }
  }
}
