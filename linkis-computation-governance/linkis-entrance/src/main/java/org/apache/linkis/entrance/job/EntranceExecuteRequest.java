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
 
package org.apache.linkis.entrance.job;

import org.apache.linkis.entrance.execute.LabelExecuteRequest;
import org.apache.linkis.entrance.execute.RuntimePropertiesExecuteRequest;
import org.apache.linkis.entrance.execute.UserExecuteRequest;
import org.apache.linkis.governance.common.entity.job.SubJobInfo;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.scheduler.executer.ExecuteRequest;
import org.apache.linkis.scheduler.executer.JobExecuteRequest;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EntranceExecuteRequest implements ExecuteRequest, LabelExecuteRequest, JobExecuteRequest, RuntimePropertiesExecuteRequest, UserExecuteRequest {

    private final static Logger logger = LoggerFactory.getLogger(EntranceExecuteRequest.class);

    public EntranceExecuteRequest(EntranceExecutionJob job) {
        setJob(job);
    }

    private SubJobInfo subJobInfo;
    private List<Label<?>> lables;

    public SubJobInfo getSubJobInfo() {
        return subJobInfo;
    }

    public void setSubJobInfo(SubJobInfo subJobInfo) {
        this.subJobInfo = subJobInfo;
    }

    public List<Label<?>> getLables() {
        return lables;
    }

    public void setLables(List<Label<?>> lables) {
        this.lables = lables;
    }

    public EntranceExecutionJob getJob() {
        return job;
    }

    public void setJob(EntranceExecutionJob job) {
        this.job = job;
    }

    private EntranceExecutionJob job;

    public void setExecutionCode(int index) {
        SubJobInfo[] jobGroupInfo = job.getJobGroups();
        if (null != jobGroupInfo && index >= 0 && index < jobGroupInfo.length) {
            subJobInfo = jobGroupInfo[index];
        } else {
            logger.warn("Invalid index : {} in jobRequest : {}. ", index, BDPJettyServerHelper.gson().toJson(jobGroupInfo));
        }
    }

    @Override
    public String code() {
        if (null != subJobInfo) {
            return subJobInfo.getCode();
        } else {
            logger.error("SubJobInfo is null!");
            return null;
        }
    }

    @Override
    public String jobId() {
        if (null != subJobInfo && null != subJobInfo.getSubJobDetail()) {
            return String.valueOf(subJobInfo.getSubJobDetail().getId());
        } else {
            logger.error("JobDetail is null!");
            return null;
        }
    }


    @Override
    public Map<String, Object> properties() {
        return job.getParams();
    }

    @Override
    public List<Label<?>> labels() {
        if (null == lables || lables.isEmpty()) {
            if (null != job.getJobRequest()) {
                return job.getJobRequest().getLabels();
            } else {
                return new ArrayList<>(0);
            }
        } else {
            return new ArrayList<>(0);
        }
    }

    @Override
    public String submitUser() {
        return job.getJobRequest().getSubmitUser();
    }

    @Override
    public String executeUser() {
        return job.getJobRequest().getExecuteUser();
    }
}
