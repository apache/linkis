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
 
package org.apache.linkis.governance.common.entity.job;

import java.util.List;

/**
 * @date 2021/5/17
 * @description
 */
public class JobRequestWithDetail extends JobRequest {


    public JobRequestWithDetail() {}

    public JobRequestWithDetail(JobRequest jobRequest) {
        setId(jobRequest.getId());
        setReqId(jobRequest.getReqId());
        setSubmitUser(jobRequest.getSubmitUser());
        setExecuteUser(jobRequest.getExecuteUser());
        setSource(jobRequest.getSource());
        setExecutionCode(jobRequest.getExecutionCode());
        setLabels(jobRequest.getLabels());
        setParams(jobRequest.getParams());
        setProgress(jobRequest.getProgress());
        setStatus(jobRequest.getStatus());
        setLogPath(jobRequest.getLogPath());
        setErrorCode(jobRequest.getErrorCode());
        setErrorDesc(jobRequest.getErrorDesc());
        setCreatedTime(jobRequest.getCreatedTime());
        setUpdatedTime(jobRequest.getUpdatedTime());
        setInstances(jobRequest.getInstances());
        setMetrics(jobRequest.getMetrics());
    }

    private List<SubJobDetail> subJobDetailList;

    public List<SubJobDetail> getSubJobDetailList() {
        return subJobDetailList;
    }

    public JobRequestWithDetail setSubJobDetailList(List<SubJobDetail> subJobDetailList) {
        this.subJobDetailList = subJobDetailList;
        return this;
    }
}
