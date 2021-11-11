/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cli.application.interactor.job;

import org.apache.linkis.cli.application.constants.LinkisClientKeys;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.BuilderException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.JobManSubType;
import org.apache.linkis.cli.core.interactor.job.JobBuilder;
import org.apache.linkis.cli.core.interactor.var.VarAccess;


public class LinkisJobManBuilder extends JobBuilder {


    @Override
    public Job build() {
        String submitUsr = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_SUBMIT_USER);
        String proxyUsr = stdVarAccess.getVar(String.class, LinkisClientKeys.JOB_COMMON_PROXY_USER);
        String jobId = null;
        if (stdVarAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_KILL_OPT)) {
            targetObj.setSubExecutionType(JobManSubType.KILL);
            jobId = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_KILL_OPT);
        } else if (stdVarAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_STATUS_OPT)) {
            targetObj.setSubExecutionType(JobManSubType.STATUS);
            jobId = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_STATUS_OPT);
        } else if (stdVarAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_DESC_OPT)) {
            targetObj.setSubExecutionType(JobManSubType.DESC);
            jobId = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_DESC_OPT);
        } else if (stdVarAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_LOG_OPT)) {
            targetObj.setSubExecutionType(JobManSubType.LOG);
            jobId = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_LOG_OPT);
        } else if (stdVarAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_RESULT_OPT)) {
            targetObj.setSubExecutionType(JobManSubType.RESULT);
            jobId = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_RESULT_OPT);
        } else if (stdVarAccess.hasVar(LinkisClientKeys.LINKIS_CLIENT_LIST_OPT)) {
            targetObj.setSubExecutionType(JobManSubType.LIST);
            jobId = stdVarAccess.getVar(String.class, LinkisClientKeys.LINKIS_CLIENT_LIST_OPT);
        } else {
            throw new BuilderException("BLD0009", ErrorLevel.ERROR, CommonErrMsg.BuilderBuildErr, "unknown subExecutionType for JobMan");
        }
        ((LinkisJobMan) targetObj).setJobId(jobId);
        targetObj.setSubmitUser(submitUsr);
        targetObj.setProxyUser(proxyUsr);

        return super.build();
    }


    @Override
    public LinkisJobManBuilder setStdVarAccess(VarAccess varAccess) {
        return (LinkisJobManBuilder) super.setStdVarAccess(varAccess);
    }

    @Override
    public LinkisJobManBuilder setSysVarAccess(VarAccess varAccess) {
        return (LinkisJobManBuilder) super.setSysVarAccess(varAccess);
    }

    @Override
    public LinkisJobMan getTargetNewInstance() {
        return new LinkisJobMan();
    }
}
