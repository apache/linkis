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

package org.apache.linkis.cli.application.interactor.job.builder;

import org.apache.linkis.cli.application.constants.AppKeys;
import org.apache.linkis.cli.application.interactor.job.LinkisManageJob;
import org.apache.linkis.cli.application.interactor.job.data.LinkisJobData;
import org.apache.linkis.cli.application.interactor.job.data.LinkisJobDataImpl;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisJobManDesc;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.operator.ujes.LinkisJobOperator;
import org.apache.linkis.cli.application.utils.ExecutionUtils;
import org.apache.linkis.cli.common.entity.present.PresentWay;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.job.JobBuilder;
import org.apache.linkis.cli.core.operator.JobOperatorFactory;
import org.apache.linkis.cli.core.present.PresentModeImpl;
import org.apache.linkis.cli.core.present.PresentWayImpl;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisManageJobBuilder extends JobBuilder {
  private static Logger logger = LoggerFactory.getLogger(LinkisSubmitJobBuilder.class);

  LinkisClientListener logListener;

  public LinkisManageJobBuilder setLogListener(LinkisClientListener observer) {
    this.logListener = observer;
    return this;
  }

  @Override
  protected LinkisJobManDesc buildJobDesc() {
    LinkisJobManDesc desc = new LinkisJobManDesc();
    String osUser = sysVarAccess.getVar(String.class, AppKeys.LINUX_USER_KEY);
    String[] adminUsers = StringUtils.split(AppKeys.ADMIN_USERS, ',');
    Set<String> adminSet = new HashSet<>();
    for (String admin : adminUsers) {
      adminSet.add(admin);
    }
    String submitUsr = ExecutionUtils.getSubmitUser(stdVarAccess, osUser, adminSet);

    String jobId = null;
    if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_KILL_OPT)) {
      jobId = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_KILL_OPT);
    } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_STATUS_OPT)) {
      jobId = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_STATUS_OPT);
    } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_DESC_OPT)) {
      jobId = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_DESC_OPT);
    } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_LOG_OPT)) {
      jobId = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_LOG_OPT);
    } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_RESULT_OPT)) {
      jobId = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_RESULT_OPT);
    } else if (stdVarAccess.hasVar(AppKeys.LINKIS_CLIENT_LIST_OPT)) {
      jobId = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_LIST_OPT);
    }

    desc.setJobId(jobId);
    desc.setUser(submitUsr);
    return desc;
  }

  @Override
  protected LinkisJobData buildJobData() {
    LinkisJobDataImpl data = new LinkisJobDataImpl();
    if (logListener == null) {
      logger.warn("logListener is not registered, will not be able to display log");
    } else {
      data.registerincLogListener(logListener);
    }
    return data;
  }

  @Override
  protected LinkisJobOperator buildJobOperator() {
    LinkisJobOperator oper;
    try {
      oper = (LinkisJobOperator) JobOperatorFactory.getReusable(AppKeys.REUSABLE_UJES_CLIENT);
    } catch (Exception e) {
      throw new LinkisClientRuntimeException(
          "BLD0012",
          ErrorLevel.ERROR,
          CommonErrMsg.BuilderBuildErr,
          "Failed to get a valid operator.",
          e);
    }
    return oper;
  }

  @Override
  protected PresentWay buildPresentWay() {
    PresentWayImpl presentWay = new PresentWayImpl();
    String outputPath = stdVarAccess.getVar(String.class, AppKeys.LINKIS_CLIENT_COMMON_OUTPUT_PATH);

    presentWay.setPath(outputPath);
    presentWay.setMode(PresentModeImpl.STDOUT);
    if (StringUtils.isNotBlank(outputPath)) {
      presentWay.setMode(PresentModeImpl.TEXT_FILE);
    }

    return presentWay;
  }

  @Override
  protected LinkisManageJob getTargetNewInstance() {
    return new LinkisManageJob();
  }

  @Override
  public LinkisManageJob build() {
    ((LinkisManageJob) targetObj).setJobDesc(buildJobDesc());
    ((LinkisManageJob) targetObj).setJobData(buildJobData());
    targetObj.setOperator(buildJobOperator());
    targetObj.setPresentWay(buildPresentWay());
    return (LinkisManageJob) super.build();
  }
}
