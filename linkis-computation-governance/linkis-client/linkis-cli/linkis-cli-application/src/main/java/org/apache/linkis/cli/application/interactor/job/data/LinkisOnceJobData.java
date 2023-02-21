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

package org.apache.linkis.cli.application.interactor.job.data;

import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.common.entity.job.JobStatus;

import java.util.List;

public class LinkisOnceJobData implements LinkisJobData, LinkisLogData, LinkisResultData {

  private SimpleOnceJobAdapter onceJobAdapter;

  private boolean hasResult = true;

  public SimpleOnceJobAdapter getOnceJobAdapter() {
    return onceJobAdapter;
  }

  public void setOnceJobAdapter(SimpleOnceJobAdapter onceJobAdapter) {
    this.onceJobAdapter = onceJobAdapter;
  }

  @Override
  public void registerincLogListener(LinkisClientListener observer) {
    onceJobAdapter.registerincLogListener(observer);
  }

  @Override
  public void notifyLogListener() {
    onceJobAdapter.notifyLogListener();
  }

  @Override
  public boolean isIncLogMode() {
    return onceJobAdapter.isIncLogMode();
  }

  @Override
  public void setIncLogMode(boolean incLogMode) {
    onceJobAdapter.setIncLogMode(incLogMode);
  }

  @Override
  public String consumeLog() {
    return onceJobAdapter.consumeLog();
  }

  public void appendLog(String log) {
    onceJobAdapter.appendLog(log);
  }

  @Override
  public final String getLogPath() {
    return onceJobAdapter.getLogPath();
  }

  public final void setLogPath(String logPath) {
    onceJobAdapter.setLogPath(logPath);
  }

  @Override
  public Integer getNextLogLineIdx() {
    return onceJobAdapter.getNextLogLineIdx();
  }

  public void setNextLogLineIdx(Integer nextLogLineIdx) {
    onceJobAdapter.setNextLogLineIdx(nextLogLineIdx);
  }

  @Override
  public Boolean hasNextLogLine() {
    return onceJobAdapter.hasNextLogLine();
  }

  @Override
  public void setHasNextLogLine(Boolean hasNextLogLine) {
    onceJobAdapter.setHasNextLogLine(hasNextLogLine);
  }

  @Override
  public List<LinkisResultSet> consumeResultContent() {
    return onceJobAdapter.consumeResultContent();
  }

  public void appendResultContent(LinkisResultSet resultContent) {
    onceJobAdapter.appendResultContent(resultContent);
  }

  @Override
  public Boolean hasNextResultPage() {
    return onceJobAdapter.hasNextResultPage();
  }

  public void setHasNextResultPage(Boolean hasNextResultPage) {
    onceJobAdapter.setHasNextResultPage(hasNextResultPage);
  }

  @Override
  public final String getResultLocation() {
    return onceJobAdapter.getResultLocation();
  }

  public final void setResultLocation(String resultLocation) {
    onceJobAdapter.setResultLocation(resultLocation);
  }

  @Override
  public String[] getResultSetPaths() {
    return onceJobAdapter.getResultSetPaths();
  }

  public final void setResultSetPaths(String[] resultSetPaths) {
    onceJobAdapter.setResultSetPaths(resultSetPaths);
  }

  @Override
  public void sendLogFin() {
    onceJobAdapter.sendLogFin();
  }

  @Override
  public boolean logFinReceived() {
    return onceJobAdapter.logFinReceived();
  }

  @Override
  public void sendResultFin() {
    onceJobAdapter.sendResultFin();
  }

  @Override
  public boolean resultFinReceived() {
    return onceJobAdapter.resultFinReceived();
  }

  @Override
  public boolean hasResult() {
    return hasResult;
  }

  @Override
  public void setHasResult(boolean hasResult) {
    this.hasResult = hasResult;
  }

  @Override
  public JobStatus getJobStatus() {
    return onceJobAdapter.getJobStatus();
  }

  public void setJobStatus(JobStatus jobStatus) {
    onceJobAdapter.setJobStatus(jobStatus);
  }

  @Override
  public String getJobID() {
    return onceJobAdapter.getJobID();
  }

  @Override
  public String getUser() {
    return onceJobAdapter.getUser();
  }

  @Override
  public String getMessage() {
    return onceJobAdapter.getMessage();
  }

  @Override
  public void setMessage(String message) {
    onceJobAdapter.setMessage(message);
  }

  @Override
  public Exception getException() {
    return onceJobAdapter.getException();
  }

  @Override
  public void setException(Exception e) {
    onceJobAdapter.setException(e);
  }

  @Override
  public String getExecID() {
    return onceJobAdapter.getJobID();
  } // No Need

  @Override
  public float getJobProgress() {
    return 0;
  }

  @Override
  public Integer getErrCode() {
    return onceJobAdapter.getErrCode();
  }

  @Override
  public String getErrDesc() {
    return onceJobAdapter.getErrDesc();
  }

  @Override
  public boolean isSuccess() {
    return onceJobAdapter.isSuccess();
  }

  @Override
  public void setSuccess(boolean success) {
    onceJobAdapter.setSuccess(success);
  }

  @Override
  public void updateByOperResult(LinkisOperResultAdapter adapter) {
    // No need
  }

  @Override
  public LinkisOnceJobData clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
    //        return null;
  }
}
