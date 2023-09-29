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

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.interactor.job.LinkisJobStatus;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisOnceDesc;
import org.apache.linkis.cli.application.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.application.observer.event.LogStartEvent;
import org.apache.linkis.cli.application.observer.event.TriggerEvent;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.observer.listener.TriggerEventListener;
import org.apache.linkis.cli.application.operator.once.OnceJobConstants;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.application.operator.ujes.UJESClientFactory;
import org.apache.linkis.cli.common.entity.job.JobStatus;
import org.apache.linkis.cli.common.entity.var.VarAccess;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.computation.client.LinkisJobBuilder$;
import org.apache.linkis.computation.client.once.simple.SimpleOnceJob;
import org.apache.linkis.computation.client.once.simple.SimpleOnceJobBuilder;
import org.apache.linkis.computation.client.once.simple.SubmittableSimpleOnceJob;
import org.apache.linkis.computation.client.operator.impl.EngineConnLogOperator;
import org.apache.linkis.computation.client.operator.impl.EngineConnLogs;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class SimpleOnceJobAdapter implements LinkisLogData {
  LinkisJobStatus jobStatus = LinkisJobStatus.UNSUBMITTED;
  EngineConnLogOperator logOperator = null;
  private String serverUrl;
  private SimpleOnceJob onceJob;
  private String engineTypeForECM;
  private String message;
  private Exception exception;
  private boolean success;
  private Boolean incLogMode;
  private LinkedBlockingDeque<String> logBuffer = new LinkedBlockingDeque();
  //    private String logPath; // remote path for job log
  //    private Integer nextLogLineIdx = 0;
  private Boolean hasNextLogLine = true;
  private String resultLocation;
  private String[] resultSetPaths = null; // remote paths for job result set
  private LinkedBlockingDeque<LinkisResultSet> resultContent = new LinkedBlockingDeque<>();
  private Boolean hasNextResultPage;
  private LinkisClientEvent logstartEvent = new LogStartEvent();
  private TriggerEvent logFinEvent = new TriggerEvent();
  private TriggerEventListener logFinListener = new TriggerEventListener();
  private TriggerEvent resultFinEvent = new TriggerEvent();
  private TriggerEventListener resultFinListener = new TriggerEventListener();

  {
    logFinEvent.register(logFinListener);
    resultFinEvent.register(resultFinListener);
  }

  public void init(LinkisOnceDesc desc) {
    VarAccess stdVarAccess = desc.getStdVarAccess();
    VarAccess sysVarAccess = desc.getSysVarAccess();

    serverUrl = stdVarAccess.getVar(String.class, AppKeys.LINKIS_COMMON_GATEWAY_URL);

    LinkisJobBuilder$.MODULE$.setDefaultClientConfig(
        UJESClientFactory.generateDWSClientConfig(stdVarAccess, sysVarAccess));
    LinkisJobBuilder$.MODULE$.setDefaultUJESClient(
        UJESClientFactory.getReusable(stdVarAccess, sysVarAccess));

    String engineTypeRaw = (String) desc.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE);
    engineTypeForECM = engineTypeRaw;

    if (StringUtils.isNotBlank(engineTypeRaw)) {
      engineTypeForECM = StringUtils.split(engineTypeRaw, "-")[0];
    } else {
      engineTypeForECM = "";
    } // TODO: remove parsing and let server side parse engineType

    onceJob =
        new SimpleOnceJobBuilder()
            .setCreateService(AppConstants.LINKIS_CLI)
            .addExecuteUser(desc.getProxyUser())
            .setStartupParams(desc.getParamConfMap())
            .setLabels(desc.getLabelMap())
            .setRuntimeParams(desc.getParamRunTimeMap())
            .setSource(desc.getSourceMap())
            .setVariableMap(desc.getParamVarsMap())
            .setJobContent(desc.getJobContentMap())
            .build();
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public SimpleOnceJob getOnceJob() {
    return onceJob;
  }

  public void setOnceJob(SimpleOnceJob onceJob) {
    this.onceJob = onceJob;
  }

  private void panicIfNull(Object obj) {
    if (obj == null) {
      throw new LinkisClientExecutionException(
          "EXE0040",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "Instance of " + obj.getClass().getCanonicalName() + " is null");
    }
  }

  public void submit() {
    panicIfNull(onceJob);
    if (!(onceJob instanceof SubmittableSimpleOnceJob)) {
      throw new LinkisClientExecutionException(
          "EXE0041",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionErr,
          "onceJob is not properly initiated");
    }
    ((SubmittableSimpleOnceJob) onceJob).submit();
  }

  public void kill() {
    panicIfNull(onceJob);
    onceJob.kill();
  }

  public String getJobID() {
    return onceJob.getId();
  }

  @Override
  public String getUser() {
    return "TODO";
  }

  public void updateStatus() {
    panicIfNull(onceJob);
    String status = onceJob.getStatus();
    panicIfNull(status);
    jobStatus = LinkisJobStatus.convertFromNodeStatusString(onceJob.getStatus());
  }

  public LinkisJobStatus getJobStatus() {
    return this.jobStatus;
  }

  public void setJobStatus(JobStatus jobStatus) {
    this.jobStatus = (LinkisJobStatus) jobStatus;
  }

  public void waitForComplete() {
    panicIfNull(onceJob);
    onceJob.waitForCompleted();
  }

  public void queryJobLogOneIteration() {
    panicIfNull(onceJob);
    updateStatus();
    if (logOperator == null) {
      logOperator =
          (EngineConnLogOperator) onceJob.getOperator(EngineConnLogOperator.OPERATOR_NAME());
      logOperator.setECMServiceInstance(
          ((SubmittableSimpleOnceJob) onceJob).getECMServiceInstance());
      logOperator.setEngineConnType(engineTypeForECM);
      //        logOperator.setPageSize(OnceJobConstants.MAX_LOG_SIZE_ONCE);
      logOperator.setIgnoreKeywords(OnceJobConstants.LOG_IGNORE_KEYWORDS);
    }
    EngineConnLogs logs =
        (EngineConnLogs) logOperator.apply(); // for some reason we have to add type conversion,
    // otherwise mvn testCompile fails
    StringBuilder logBuilder = new StringBuilder();
    for (String log : logs.logs()) {
      logBuilder.append(log).append(System.lineSeparator());
    }
    appendLog(logBuilder.toString());
    if ((logs.logs() == null || logs.logs().size() <= 0) && jobStatus.isJobFinishedState()) {
      setHasNextLogLine(false);
    }
    //        System.out.println(logs.logs().size());
  }

  public void registerincLogListener(LinkisClientListener observer) {
    this.logstartEvent.register(observer);
  }

  public void notifyLogListener() {
    if (this.logstartEvent.isRegistered()) {
      logstartEvent.notifyObserver(logstartEvent, this);
    }
  }

  public boolean isIncLogMode() {
    return this.incLogMode;
  }

  public void setIncLogMode(boolean incLogMode) {
    this.incLogMode = incLogMode;
  }

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

  public final String getLogPath() {
    return null;
  }

  public final void setLogPath(String logPath) {
    return;
  }

  public Integer getNextLogLineIdx() {
    return null;
  }

  public void setNextLogLineIdx(Integer nextLogLineIdx) {
    return;
  }

  public Boolean hasNextLogLine() {
    return hasNextLogLine;
  }

  public void setHasNextLogLine(Boolean hasNextLogLine) {
    this.hasNextLogLine = hasNextLogLine;
  }

  public List<LinkisResultSet> consumeResultContent() {
    List<LinkisResultSet> ret = new LinkedList<>();
    resultContent.drainTo(ret, resultContent.size());
    return ret;
  }

  public void appendResultContent(LinkisResultSet resultContent) {
    this.resultContent.add(resultContent);
  }

  public Boolean hasNextResultPage() {
    return hasNextResultPage;
  }

  public void setHasNextResultPage(Boolean hasNextResultPage) {
    this.hasNextResultPage = hasNextResultPage;
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

  public void sendLogFin() {
    if (this.logFinEvent != null && this.logFinEvent.isRegistered()) {
      this.logFinEvent.notifyObserver(resultFinEvent, null);
    }
  }

  public boolean logFinReceived() {
    return this.logFinListener.isTriggered();
  }

  public void sendResultFin() {
    if (this.resultFinEvent != null && this.resultFinEvent.isRegistered()) {
      this.resultFinEvent.notifyObserver(resultFinEvent, null);
    }
  }

  public boolean resultFinReceived() {
    return this.resultFinListener.isTriggered();
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
  public void setException(Exception e) {
    this.exception = e;
  }

  @Override
  public String getExecID() {
    return getJobID();
  } // No Need

  @Override
  public float getJobProgress() {
    return 0;
  }

  @Override
  public Integer getErrCode() {
    return null;
  }

  @Override
  public String getErrDesc() {
    return null;
  }

  @Override
  public boolean isSuccess() {
    return success;
  }

  @Override
  public void setSuccess(boolean success) {
    this.success = success;
  }

  @Override
  public void updateByOperResult(LinkisOperResultAdapter adapter) {
    // No need
  }

  @Override
  public LinkisLogData clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
    //        return null;
  }
}
