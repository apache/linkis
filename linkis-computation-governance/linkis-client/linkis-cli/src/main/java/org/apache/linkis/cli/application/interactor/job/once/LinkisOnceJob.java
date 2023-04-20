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

package org.apache.linkis.cli.application.interactor.job.once;

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.entity.job.Job;
import org.apache.linkis.cli.application.entity.job.JobResult;
import org.apache.linkis.cli.application.entity.job.JobStatus;
import org.apache.linkis.cli.application.operator.OperManager;
import org.apache.linkis.cli.application.operator.once.OnceJobOper;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;
import org.apache.linkis.cli.application.utils.SchedulerManager;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisOnceJob implements Job {

  private static final Logger logger = LoggerFactory.getLogger(LinkisOnceJob.class);

  private Boolean isAsync = false;
  private OnceJobOper oper;

  @Override
  public void build(CliCtx ctx) {
    this.isAsync =
        ctx.getVarAccess().getVarOrDefault(Boolean.class, CliKeys.LINKIS_CLIENT_ASYNC_OPT, false);
    oper = (OnceJobOper) OperManager.getNew(CliKeys.LINKIS_ONCE, ctx);
  }

  @Override
  public JobResult run() {
    StringBuilder infoBuilder = new StringBuilder();
    infoBuilder.append("connecting to linkis gateway:").append(oper.getServerUrl());
    LoggerManager.getInformationLogger().info(infoBuilder.toString());

    /** submit */
    oper.submit();
    JobStatus jobStatus = oper.getStatus();
    infoBuilder.setLength(0);
    infoBuilder.append("JobId:").append(oper.getJobID()).append(System.lineSeparator());
    LoggerManager.getPlaintTextLogger().info(infoBuilder.toString());
    if (isAsync && jobStatus != null && jobStatus.isJobSubmitted()) {
      return new OnceJobResult(true, "Submit Success!!!", new HashMap<>());
    }

    /** getLog */
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Thread logConsumer = new Thread(() -> ProcessLog(latch), "Log-Consumer");
      SchedulerManager.getCachedThreadPoolExecutor().execute(logConsumer);
    } catch (Exception e) {
      logger.warn("Failed to retrieve log", e);
    }

    /** wait complete */
    oper.waitForComplete();
    try {
      latch.await();
    } catch (Exception e) {
      // ignore
    }

    JobStatus finalStatus = oper.getStatus();

    if (finalStatus.isJobSuccess()) {
      return new OnceJobResult(true, "Execute Success!!!", new HashMap<>());
    } else {
      return new OnceJobResult(false, "Execute Failure!!!", new HashMap<>());
    }
  }

  @Override
  public void onDestroy() {
    oper.kill();
  }

  private void ProcessLog(CountDownLatch latch) {
    while (!oper.isLogFin()) {
      String log = oper.getCurrentLog();
      LoggerManager.getPlaintTextLogger().info(log);
      CliUtils.doSleepQuietly(2000l);
    }
    latch.countDown();
  }
}
