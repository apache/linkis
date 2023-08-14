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

package org.apache.linkis.cli.application.operator.once;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.entity.job.JobStatus;
import org.apache.linkis.cli.application.entity.operator.JobOper;
import org.apache.linkis.cli.application.entity.var.VarAccess;
import org.apache.linkis.cli.application.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.common.LinkisJobStatus;
import org.apache.linkis.cli.application.operator.ujes.UJESClientFactory;
import org.apache.linkis.computation.client.LinkisJobBuilder$;
import org.apache.linkis.computation.client.once.simple.SimpleOnceJob;
import org.apache.linkis.computation.client.once.simple.SimpleOnceJobBuilder;
import org.apache.linkis.computation.client.once.simple.SubmittableSimpleOnceJob;
import org.apache.linkis.computation.client.operator.impl.EngineConnLogOperator;
import org.apache.linkis.computation.client.operator.impl.EngineConnLogs;

import org.apache.commons.lang3.StringUtils;

public class OnceJobOper implements JobOper {

  EngineConnLogOperator logOperator = null;
  private SimpleOnceJob onceJob;
  private String serverUrl;
  private String engineTypeForECM;
  private Boolean isLogFin = false;

  public void init(OnceJobDesc desc) {

    VarAccess varAccess = desc.getVarAccess();

    serverUrl = varAccess.getVar(String.class, CliKeys.LINKIS_COMMON_GATEWAY_URL);

    LinkisJobBuilder$.MODULE$.setDefaultClientConfig(
        UJESClientFactory.generateDWSClientConfig(varAccess));
    LinkisJobBuilder$.MODULE$.setDefaultUJESClient(UJESClientFactory.getReusable(varAccess));

    String engineTypeRaw = (String) desc.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE);
    engineTypeForECM = engineTypeRaw;

    if (StringUtils.isNotBlank(engineTypeRaw)) {
      engineTypeForECM = StringUtils.split(engineTypeRaw, "-")[0];
    } else {
      engineTypeForECM = "";
    } // TODO: remove parsing and let server side parse engineType

    onceJob =
        new SimpleOnceJobBuilder()
            .setCreateService(CliConstants.LINKIS_CLI)
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
    if (!getStatus().isJobFinishedState()) {
      onceJob.kill();
    }
  }

  public String getJobID() {
    return onceJob.getId();
  }

  public String getUser() {
    return "TODO";
  }

  public JobStatus getStatus() {
    panicIfNull(onceJob);
    String status = onceJob.getStatus();
    return LinkisJobStatus.convertFromNodeStatusString(status);
  }

  public void waitForComplete() {
    panicIfNull(onceJob);
    onceJob.waitForCompleted();
  }

  public String getCurrentLog() {
    panicIfNull(onceJob);
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
    String status = onceJob.getStatus();
    LinkisJobStatus jobStatus = LinkisJobStatus.convertFromNodeStatusString(status);
    if ((logs.logs() == null || logs.logs().size() <= 0) && jobStatus.isJobFinishedState()) {
      isLogFin = true;
    }
    return logBuilder.toString();
    //        System.out.println(logs.logs().size());
  }

  public Boolean isLogFin() {
    return isLogFin;
  }
}
