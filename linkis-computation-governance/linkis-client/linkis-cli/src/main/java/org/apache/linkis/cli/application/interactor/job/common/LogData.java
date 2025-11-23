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

import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class LogData {
  private String user;
  private String jobId;
  private String execId;
  // log will be fetched and stored in LinkedBlockingDeque, while logRetriever consumes log in
  // another thread
  private LinkedBlockingDeque<String> logBuffer = new LinkedBlockingDeque();
  private String logPath; // remote path for job log
  private Integer nextLogLineIdx; // index of next log line to be fetched
  private Boolean hasNextLogLine; // if there is still log to be retrieve
  private volatile Boolean logFin = false; // if all log is finished

  public LogData(String user, String jobId, String execId) {
    this.user = user;
    this.jobId = jobId;
    this.execId = execId;
  }

  public String getUser() {
    return user;
  }

  public String getJobID() {
    return jobId;
  }

  public String getExecID() {
    return execId;
  }

  public final String getLogPath() {
    return logPath;
  }

  public final void setLogPath(String logPath) {
    this.logPath = logPath;
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

  public Integer getNextLogLineIdx() {
    return nextLogLineIdx;
  }

  public void setNextLogLineIdx(Integer nextLogLineIdx) {
    this.nextLogLineIdx = nextLogLineIdx;
  }

  public Boolean hasNextLogLine() {
    return hasNextLogLine;
  }

  public void setHasNextLogLine(Boolean hasNextLogLine) {
    this.hasNextLogLine = hasNextLogLine;
  }

  public void updateLog(LinkisOperResultAdapter adapter) {
    if (adapter.getLogPath() != null) {
      setLogPath(adapter.getLogPath());
    }
    if (adapter.getLog() != null
        && adapter.getNextLogLine() != null
        && adapter.hasNextLogLine() != null) {
      setNextLogLineIdx(adapter.getNextLogLine());
      setHasNextLogLine(adapter.hasNextLogLine());
      appendLog(adapter.getLog());
    }
  }

  public void setLogFin() {
    this.logFin = true;
  }

  public Boolean isLogFin() {
    return logFin;
  }
}
