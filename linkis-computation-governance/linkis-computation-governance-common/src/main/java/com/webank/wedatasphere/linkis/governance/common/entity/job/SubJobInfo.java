package com.webank.wedatasphere.linkis.governance.common.entity.job;


import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SubJobInfo {


  private String code;
  private String  status;
  private SubJobDetail subJobDetail;

  private volatile float progress = 0f;

  private Map<String, JobProgressInfo> progressInfoMap = new ConcurrentHashMap<>();

  private JobRequest jobReq;

  public SubJobDetail getSubJobDetail() {
   return subJobDetail;
  }

  public void setSubJobDetail(SubJobDetail subJobDetail) {
    this.subJobDetail = subJobDetail;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    if (null != status) {
      this.status = status;
      if (null != getSubJobDetail()) {
        getSubJobDetail().setStatus(status);
      }
    }
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public JobRequest getJobReq() {
    return jobReq;
  }

  public void setJobReq(JobRequest jobReq) {
    this.jobReq = jobReq;
  }

  public float getProgress() {
    return progress;
  }

  public void setProgress(float progress) {
    this.progress = progress;
  }

  public Map<String, JobProgressInfo> getProgressInfoMap() {
    return progressInfoMap;
  }

  public void setProgressInfoMap(Map<String, JobProgressInfo> progressInfoMap) {
    this.progressInfoMap = progressInfoMap;
  }
}
