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

import org.apache.linkis.cli.application.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.application.observer.event.LogStartEvent;
import org.apache.linkis.cli.application.observer.event.TriggerEvent;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.observer.listener.TriggerEventListener;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.common.entity.job.JobStatus;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class LinkisJobDataImpl
    implements LinkisJobData, LinkisLogData, LinkisResultData, Cloneable {

  private String jobID;
  private String user;
  private JobStatus jobStatus = null;
  private String message;
  private Exception exception;
  private String execID;
  private float progress = 0.0f;
  private Boolean incLogMode;
  private LinkedBlockingDeque<String> logBuffer = new LinkedBlockingDeque();
  private String logPath; // remote path for job log
  private Integer nextLogLineIdx;
  private Boolean hasNextLogLine;
  private String resultLocation;
  private String[] resultSetPaths = null; // remote paths for job result set
  private LinkedBlockingDeque<LinkisResultSet> resultContent = new LinkedBlockingDeque<>();
  private Boolean hasNextResultPage;
  private Integer errCode = null;
  private String errDesc = null;
  private boolean success = false;
  private String instance;
  private String umUser;
  private String simpleExecId;
  private String executionCode;
  private String engineType;
  private String runType;
  private Long costTime;
  private Date createdTime;
  private Date updatedTime;
  private Date engineStartTime;
  private String executeApplicationName;
  private String requestApplicationName;

  private LinkisClientEvent logstartEvent = new LogStartEvent();
  private TriggerEvent logFinevent = new TriggerEvent();
  private TriggerEventListener logFinListener = new TriggerEventListener();
  private TriggerEvent resultFinEvent = new TriggerEvent();
  private TriggerEventListener resultFinListener = new TriggerEventListener();

  private boolean hasResult = true;

  {
    logFinevent.register(logFinListener);
    resultFinEvent.register(resultFinListener);
  }

  @Override
  public String getJobID() {
    return jobID;
  }

  public void setJobId(String jobId) {
    this.jobID = jobId;
  }

  @Override
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(JobStatus jobStatus) {
    this.jobStatus = jobStatus;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public Exception getException() {
    return exception;
  }

  @Override
  public void setException(Exception exception) {
    this.exception = exception;
  }

  @Override
  public final String getExecID() {
    return execID;
  }

  public final void setExecID(String execID) {
    this.execID = execID;
  }

  @Override
  public final float getJobProgress() {
    return progress;
  }

  public final void setJobProgress(float progress) {
    this.progress = progress;
  }

  @Override
  public final String getLogPath() {
    return logPath;
  }

  public final void setLogPath(String logPath) {
    this.logPath = logPath;
  }

  @Override
  public final String getResultLocation() {
    return resultLocation;
  }

  public final void setResultLocation(String resultLocation) {
    this.resultLocation = resultLocation;
  }

  @Override
  public String[] getResultSetPaths() {
    return resultSetPaths;
  }

  public final void setResultSetPaths(String[] resultSetPaths) {
    this.resultSetPaths = resultSetPaths;
  }

  @Override
  public Integer getErrCode() {
    return errCode;
  }

  public void setErrCode(Integer errCode) {
    this.errCode = errCode;
  }

  @Override
  public String getErrDesc() {
    return errDesc;
  }

  public void setErrDesc(String errDesc) {
    this.errDesc = errDesc;
  }

  @Override
  public void registerincLogListener(LinkisClientListener observer) {
    this.logstartEvent.register(observer);
  }

  @Override
  public void notifyLogListener() {
    if (this.logstartEvent.isRegistered()) {
      logstartEvent.notifyObserver(logstartEvent, this);
    }
  }

  @Override
  public boolean isIncLogMode() {
    return this.incLogMode;
  }

  @Override
  public void setIncLogMode(boolean incLogMode) {
    this.incLogMode = incLogMode;
  }

  @Override
  public String consumeLog() {
    List<String> logs = new LinkedList<>();
    this.logBuffer.drainTo(logs, this.logBuffer.size());
    StringBuilder tmp = new StringBuilder();
    for (String str : logs) {
      tmp.append(str);
    }
    return tmp.toString();
  }

  public void appendLog(String log) {
    this.logBuffer.add(log);
  }

  @Override
  public Integer getNextLogLineIdx() {
    return nextLogLineIdx;
  }

  public void setNextLogLineIdx(Integer nextLogLineIdx) {
    this.nextLogLineIdx = nextLogLineIdx;
  }

  @Override
  public Boolean hasNextLogLine() {
    return hasNextLogLine;
  }

  @Override
  public void setHasNextLogLine(Boolean hasNextLogLine) {
    this.hasNextLogLine = hasNextLogLine;
  }

  @Override
  public List<LinkisResultSet> consumeResultContent() {
    List<LinkisResultSet> ret = new LinkedList<>();
    resultContent.drainTo(ret, resultContent.size());
    return ret;
  }

  public void appendResultContent(LinkisResultSet resultContent) {
    this.resultContent.add(resultContent);
  }

  @Override
  public Boolean hasNextResultPage() {
    return hasNextResultPage;
  }

  public void setHasNextResultPage(Boolean hasNextResultPage) {
    this.hasNextResultPage = hasNextResultPage;
  }

  @Override
  public void sendLogFin() {
    if (this.logFinevent != null && this.logFinevent.isRegistered()) {
      this.logFinevent.notifyObserver(resultFinEvent, null);
    }
  }

  @Override
  public boolean logFinReceived() {
    return this.logFinListener.isTriggered();
  }

  @Override
  public void sendResultFin() {
    if (this.resultFinEvent != null && this.resultFinEvent.isRegistered()) {
      this.resultFinEvent.notifyObserver(resultFinEvent, null);
    }
  }

  @Override
  public boolean resultFinReceived() {
    return this.resultFinListener.isTriggered();
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
  public boolean isSuccess() {
    return success;
  }

  @Override
  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getInstance() {
    return instance;
  }

  public void setInstance(String instance) {
    this.instance = instance;
  }

  public String getUmUser() {
    return umUser;
  }

  public void setUmUser(String umUser) {
    this.umUser = umUser;
  }

  public String getSimpleExecId() {
    return simpleExecId;
  }

  public void setSimpleExecId(String simpleExecId) {
    this.simpleExecId = simpleExecId;
  }

  public String getExecutionCode() {
    return executionCode;
  }

  public void setExecutionCode(String executionCode) {
    this.executionCode = executionCode;
  }

  public String getEngineType() {
    return engineType;
  }

  public void setEngineType(String engineType) {
    this.engineType = engineType;
  }

  public String getRunType() {
    return runType;
  }

  public void setRunType(String runType) {
    this.runType = runType;
  }

  public Long getCostTime() {
    return costTime;
  }

  public void setCostTime(Long costTime) {
    this.costTime = costTime;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  public Date getEngineStartTime() {
    return engineStartTime;
  }

  public void setEngineStartTime(Date engineStartTime) {
    this.engineStartTime = engineStartTime;
  }

  public String getExecuteApplicationName() {
    return executeApplicationName;
  }

  public void setExecuteApplicationName(String executeApplicationName) {
    this.executeApplicationName = executeApplicationName;
  }

  public String getRequestApplicationName() {
    return requestApplicationName;
  }

  public void setRequestApplicationName(String requestApplicationName) {
    this.requestApplicationName = requestApplicationName;
  }

  @Override
  public void updateByOperResult(LinkisOperResultAdapter adapter) {
    if (adapter.getJobID() != null) {
      setJobId(adapter.getJobID());
    }
    if (adapter.getUser() != null) {
      setUser(adapter.getUser());
    }
    if (adapter.getJobStatus() != null) {
      setJobStatus(adapter.getJobStatus());
    }
    if (adapter.getStrongerExecId() != null) {
      setExecID(adapter.getStrongerExecId());
    }
    if (adapter.getJobProgress() != null) {
      setJobProgress(adapter.getJobProgress());
    }
    if (adapter.getLogPath() != null) {
      setLogPath(adapter.getLogPath());
    }
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
    if (adapter.getLog() != null
        && adapter.getNextLogLine() != null
        && adapter.hasNextLogLine() != null) {
      setNextLogLineIdx(adapter.getNextLogLine());
      setHasNextLogLine(adapter.hasNextLogLine());
      appendLog(adapter.getLog());
    }
    if (adapter.getResultContent() != null && adapter.resultHasNextPage() != null) {
      setHasNextResultPage(adapter.resultHasNextPage());
      appendResultContent(adapter.getResultContent());
    }
    if (adapter.getInstance() != null) {
      setInstance(adapter.getInstance());
    }
    if (adapter.getUmUser() != null) {
      setUmUser(adapter.getUmUser());
    }
    if (adapter.getSimpleExecId() != null) {
      setSimpleExecId(adapter.getSimpleExecId());
    }
    if (adapter.getExecutionCode() != null) {
      setExecutionCode(adapter.getExecutionCode());
    }
    if (adapter.getEngineType() != null) {
      setEngineType(adapter.getEngineType());
    }
    if (adapter.getRunType() != null) {
      setRunType(adapter.getRunType());
    }
    if (adapter.getCostTime() != null) {
      setCostTime(adapter.getCostTime());
    }
    if (adapter.getCreatedTime() != null) {
      setCreatedTime(adapter.getCreatedTime());
    }
    if (adapter.getUpdatedTime() != null) {
      setUpdatedTime(adapter.getUpdatedTime());
    }
    if (adapter.getEngineStartTime() != null) {
      setEngineStartTime(adapter.getEngineStartTime());
    }
    if (adapter.getExecuteApplicationName() != null) {
      setExecuteApplicationName(adapter.getExecuteApplicationName());
    }
    if (adapter.getRequestApplicationName() != null) {
      setRequestApplicationName(adapter.getRequestApplicationName());
    }
  }

  @Override
  public LinkisJobDataImpl clone() throws CloneNotSupportedException {
    LinkisJobDataImpl ret = (LinkisJobDataImpl) super.clone();
    if (logBuffer != null) {
      ret.logBuffer = new LinkedBlockingDeque(this.logBuffer);
    }
    if (this.resultContent != null) {
      ret.resultContent = new LinkedBlockingDeque<>();
      for (LinkisResultSet r1 : resultContent) {
        ret.resultContent.add(r1.clone());
      }
    }
    if (this.resultSetPaths != null) {
      ret.setResultSetPaths(Arrays.copyOf(this.resultSetPaths, this.resultSetPaths.length));
    }
    /*
    These  be shared and hence should not be deep copied.
    */
    ret.logFinevent = this.logFinevent;
    ret.logFinListener = this.logFinListener;
    ret.resultFinEvent = this.resultFinEvent;
    ret.resultFinListener = this.resultFinListener;

    return ret;
  }
}
