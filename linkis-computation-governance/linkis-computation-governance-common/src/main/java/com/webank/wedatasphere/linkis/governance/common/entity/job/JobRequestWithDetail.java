package com.webank.wedatasphere.linkis.governance.common.entity.job;

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
