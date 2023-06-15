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
import org.apache.linkis.cli.application.observer.event.FetchResultEvent;
import org.apache.linkis.cli.application.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOper;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;
import org.apache.linkis.cli.application.utils.SchedulerManager;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultRetriever {
  private static final Logger logger = LoggerFactory.getLogger(ResultRetriever.class);

  private LinkisJobOper linkisJobOperator;
  private ResultData resultData;

  private LinkisClientListener resultListener;
  private LinkisClientEvent fetchResultEvent = new FetchResultEvent();

  public ResultRetriever(
      String user,
      String jobId,
      String execId,
      LinkisJobOper linkisJobOperator,
      LinkisClientListener resultListener) {
    this.linkisJobOperator = linkisJobOperator;
    this.resultListener = resultListener;
    this.resultData = new ResultData(user, jobId, execId);
    registerResultListener(resultListener);
  }

  public void retrieveResultSync() {
    if (resultData.getUser() == null || resultData.getJobID() == null) {
      throw new LinkisClientExecutionException(
          "EXE0036", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "user or jobID is null");
    }
    resultData.updateByOperResult(
        linkisJobOperator.queryJobInfo(resultData.getUser(), resultData.getJobID()));
    if (resultData.getJobStatus() == null) {
      throw new LinkisClientExecutionException(
          "EXE0038", ErrorLevel.ERROR, CommonErrMsg.ExecutionErr, "jobStatus is null");
    }
    if (!resultData.getJobStatus().isJobSuccess()) {
      LoggerManager.getInformationLogger()
          .info(
              "Job status is not success but \'"
                  + resultData.getJobStatus()
                  + "\'. Will not try to retrieve any Result");
      resultData.setResultFin(); // inform listener to stop
      return;
    }
    if (StringUtils.isBlank(resultData.getResultLocation())) {
      throw new LinkisClientExecutionException(
          "EXE0037",
          ErrorLevel.WARN,
          CommonErrMsg.ExecutionErr,
          "Got blank ResultLocation from server. Job may not have result-set. Will not try to retrieve any Result");
    }
    resultData.updateByOperResult(
        linkisJobOperator.queryResultSetPaths(
            resultData.getUser(), resultData.getJobID(), resultData.getResultLocation()));

    if (resultData.getResultSetPaths() == null || resultData.getResultSetPaths().length == 0) {
      String msg = "Your job got no result.";
      logger.warn(msg);
      resultData.setResultFin(); // inform listener to stop
      resultData.setHasResult(false);
      return;
    }

    try {
      resultData.setHasResult(true);
      //            Thread resultConsumer = new Thread(() -> notifyResultListener());
      Thread resultThread = new Thread(() -> queryResultLoop(resultData), "Result-Retrieve-Thread");
      //            SchedulerUtils.getCachedThreadPoolExecutor().execute(resultConsumer);
      SchedulerManager.getCachedThreadPoolExecutor().execute(resultThread);
      notifyResultListener();
    } catch (Exception e) {
      logger.error("Failed to retrieve result", e);
      throw e;
    }
  }

  public void queryResultLoop(ResultData data) {
    boolean hasNext = true;
    int retryCnt = 0;
    final int MAX_RETRY = 30; // continues fails for 250s, then exit
    int idx = 0;
    try {
      while (hasNext) {
        try {
          hasNext = queryOneResult(data, idx);
        } catch (LinkisClientRuntimeException e) {
          logger.error("Cannot get result:", e);
          retryCnt++;
          if (retryCnt >= MAX_RETRY) {
            logger.error(
                "Continuously failing to query result for "
                    + MAX_RETRY * (MAX_RETRY + 2) * 500 / 1000
                    + "s. Will no longer try to query result",
                e);
            return;
          } else {
            hasNext = true;
          }
          CliUtils.doSleepQuietly(500l + 500l * retryCnt); // maybe server problem. sleep longer
          continue;
        }
        idx++;
      }
    } catch (Exception e) {
      logger.error("Something goes wrong. Job Result may be incomplete", e);
      throw e;
    } finally {
      data.setResultFin();
    }
  }

  private boolean queryOneResult(ResultData data, int idxResultSet) {
    Integer curPage = 1;
    boolean hasNextResult = true;
    boolean hasNextPage = true;
    while (hasNextPage) {
      data.updateByOperResult(
          linkisJobOperator.queryResultSetGivenResultSetPath(
              data.getResultSetPaths(),
              idxResultSet,
              data.getUser(),
              curPage,
              CliConstants.RESULTSET_PAGE_SIZE));
      if (data.hasNextResultPage() == null) {
        throw new LinkisClientExecutionException(
            "EXE0040",
            ErrorLevel.ERROR,
            CommonErrMsg.ExecutionResultErr,
            "Something foes wrong. Got null as \'hasNextPage\'.");
      }
      hasNextPage = data.hasNextResultPage();

      curPage++;
      hasNextResult = idxResultSet + 1 < data.getResultSetPaths().length;
    }
    return hasNextResult;
  }

  public void registerResultListener(LinkisClientListener observer) {
    this.fetchResultEvent.register(observer);
  }

  public void notifyResultListener() {
    if (this.fetchResultEvent.isRegistered()) {
      fetchResultEvent.notifyObserver(fetchResultEvent, this.resultData);
    }
  }

  public void setResultFin() {
    this.resultData.setResultFin();
  }
}
