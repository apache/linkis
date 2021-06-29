/*
 * <!--
 *   ~ Copyright 2019 WeBank
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License.
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software
 *   ~ distributed under the License is distributed on an "AS IS" BASIS,
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   ~ See the License for the specific language governing permissions and
 *   ~ limitations under the License.
 *   -->
 *
 */

package com.webank.wedatasphere.linkis.governance.common.entity.job;


import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @date 2021/3/5
 * @description
 */
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
