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

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.application.observer.event.LogStartEvent;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOper;
import org.apache.linkis.cli.application.operator.ujes.LinkisOperResultAdapter;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;
import org.apache.linkis.cli.application.utils.SchedulerManager;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log retrieval logic: 1. LogRetriever polls to obtain real-time logs, and if the task is
 * completed, it retrieves persistent logs 2. Organized by org.apache.inkis.cli.application.
 * interactor.job. com LogRetriever # sendLogFin decides whether to continue polling logs 3.
 * getNextLogLine is the FromLine returned by the log interface 4. The return of persistent logs is
 * OpenLogResult2
 */
public class LogRetriever {
  private static final Logger logger = LoggerFactory.getLogger(LogRetriever.class);

  private LinkisJobOper linkisJobOperator;
  private LogData logData;

  private Boolean incLogMode;

  private LinkisClientListener logListener;
  private LinkisClientEvent logStartEvent = new LogStartEvent();

  public LogRetriever(
      String user,
      String jobId,
      String execId,
      Boolean incLogMode,
      LinkisJobOper linkisJobOperator,
      LinkisClientListener logListener) {
    this.linkisJobOperator = linkisJobOperator;
    this.logListener = logListener;
    this.incLogMode = incLogMode;
    this.logData = new LogData(user, jobId, execId);
    registerLogListener(logListener);
  }

  public void retrieveLogAsync() {
    if (logData.getUser() == null || logData.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    try {
      Thread logConsumer = new Thread(() -> notifyLogListener(), "Log-Consumer");
      Thread logRetriever = new Thread(() -> queryLogLoop(logData), "Log-Retriever");
      SchedulerManager.getCachedThreadPoolExecutor().execute(logRetriever);
      SchedulerManager.getCachedThreadPoolExecutor().execute(logConsumer);
    } catch (Exception e) {
      logger.warn("Failed to retrieve log", e);
    }
  }

  public void waitIncLogComplete() {
    int retry = 0;
    int MAX_RETRY = 300; // wait for 10 minutes after job finish
    while (retry++ < MAX_RETRY) {
      if (this.logFinReceived()) {
        return;
      }
      CliUtils.doSleepQuietly(CliConstants.JOB_QUERY_SLEEP_MILLS);
    }
    String msg =
        "Job is in Finished state(SUCCEED/FAILED/CANCELED) but client keep querying inclog for "
            + (MAX_RETRY * CliConstants.JOB_QUERY_SLEEP_MILLS / 1000)
            + "seconds. Execution ends forcefully. Next will try handle execution result.";
    logger.warn(msg);
    LoggerManager.getInformationLogger().warn(msg);
  }

  public void queryLogLoop(LogData data) {
    int curLogIdx;
    int nextLogIdx;
    boolean hasNext = true;
    int retryCnt = 0;
    // continues fails for 90s, then exit thread
    final int MAX_RETRY = 12;
    try {
      while (hasNext) {
        curLogIdx = data.getNextLogLineIdx() == null ? 0 : data.getNextLogLineIdx();
        try {
          queryJobLogFromLine(data, curLogIdx);
        } catch (Exception e) {
          logger.error("Cannot get inc-log:", e);
          // and yes sometimes server may not be able to prepare persisted-log
          retryCnt++;
          if (retryCnt >= MAX_RETRY) {
            logger.error(
                "Continuously failing to query inc-log for "
                    + MAX_RETRY * (MAX_RETRY + 2) * 500 / 1000
                    + "s. Will no longer try to query log",
                e);
            break;
          }
          CliUtils.doSleepQuietly(500L + 500L * retryCnt);
          continue;
        }
        retryCnt = 0;
        nextLogIdx = data.getNextLogLineIdx() == null ? curLogIdx : data.getNextLogLineIdx();
        if (incLogMode) {
          hasNext = data.hasNextLogLine() == null ? curLogIdx < nextLogIdx : data.hasNextLogLine();
        } else {
          hasNext = curLogIdx < nextLogIdx;
        }
        if (curLogIdx >= nextLogIdx) {
          String msg =
              MessageFormat.format(
                  "Retrieving log, curLogIdx={}, hasNext={0}, nextLogIdx={1}",
                  curLogIdx, hasNext, nextLogIdx);
          logger.info(msg);
        }
        CliUtils.doSleepQuietly(CliConstants.JOB_QUERY_SLEEP_MILLS);
      }
    } catch (Exception e) {
      logger.error("Something goes wrong. Job Log may be incomplete", e);
    } finally {
      sendLogFin();
    }
  }

  private void queryJobLogFromLine(LogData data, int fromLine) throws LinkisClientRuntimeException {

    LinkisOperResultAdapter jobInfoResult =
        linkisJobOperator.queryJobInfo(data.getUser(), data.getJobID());
    data.updateLog(jobInfoResult);
    if (!jobInfoResult.getJobStatus().isJobFinishedState()) {
      data.updateLog(
          linkisJobOperator.queryRunTimeLogFromLine(
              data.getUser(), data.getJobID(), data.getExecID(), fromLine));
    } else {
      data.updateLog(
          linkisJobOperator.queryPersistedLogFromLine(
              data.getLogPath(), data.getUser(), data.getJobID(), fromLine));
    }
  }

  public Boolean isIncLogMode() {
    return incLogMode;
  }

  public void registerLogListener(LinkisClientListener observer) {
    this.logStartEvent.register(observer);
  }

  public void notifyLogListener() {
    if (this.logStartEvent.isRegistered()) {
      logStartEvent.notifyObserver(logStartEvent, this.logData);
    }
  }

  public void sendLogFin() {
    this.logData.setLogFin();
  }

  public boolean logFinReceived() {
    return this.logData.isLogFin();
  }
}
