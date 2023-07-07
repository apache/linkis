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

package org.apache.linkis.cli.application.interactor.job.jobcmd;

import org.apache.linkis.cli.application.constants.CliKeys;
import org.apache.linkis.cli.application.entity.context.CliCtx;
import org.apache.linkis.cli.application.utils.CliUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class JobCmdDescBuilder {
  public static JobCmdDesc build(CliCtx ctx) {
    JobCmdDesc desc = new JobCmdDesc();
    String osUser = System.getProperty(CliKeys.LINUX_USER_KEY);
    String[] adminUsers = StringUtils.split(CliKeys.ADMIN_USERS, ',');
    Set<String> adminSet = new HashSet<>();
    for (String admin : adminUsers) {
      adminSet.add(admin);
    }
    String submitUsr = CliUtils.getSubmitUser(ctx.getVarAccess(), osUser, adminSet);

    JobCmdSubType subType = null;

    String jobId = null;
    if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_KILL_OPT)) {
      jobId = ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_KILL_OPT);
      subType = JobCmdSubType.KILL;
    } else if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_STATUS_OPT)) {
      jobId = ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_STATUS_OPT);
      subType = JobCmdSubType.STATUS;
    } else if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_DESC_OPT)) {
      jobId = ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_DESC_OPT);
      subType = JobCmdSubType.DESC;
    } else if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_LOG_OPT)) {
      jobId = ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_LOG_OPT);
      subType = JobCmdSubType.LOG;
    } else if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_RESULT_OPT)) {
      jobId = ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_RESULT_OPT);
      subType = JobCmdSubType.RESULT;
    } else if (ctx.getVarAccess().hasVar(CliKeys.LINKIS_CLIENT_LIST_OPT)) {
      jobId = ctx.getVarAccess().getVar(String.class, CliKeys.LINKIS_CLIENT_LIST_OPT);
      subType = JobCmdSubType.LIST;
    }
    desc.setSubType(subType);
    desc.setJobId(jobId);
    desc.setUser(submitUsr);
    return desc;
  }
}
