package com.webank.wedatasphere.linkis.governance.common.entity.job;

import java.util.Date;

/**
 * @date 2021/3/5
 * @description
 */
public class SubJobDetail {

  /*
  db自增id
   */

   Long id;
  /*
  jobGroup id
   */

   Long jobGroupId;
  /*
  结果集地址
   */

   String resultLocation;
  /*
  结果集数量
   */

   Integer resultSize;
  /*
  执行参数 json
   */

   String executionContent;

  /*
  jobGroup信息
   */

   String jobGroupInfo;

   Date createdTime = new Date(System.currentTimeMillis());

   Date updatedTime = new Date(System.currentTimeMillis());

  /*
  任务状态
   */

   String status;


   Integer priority ;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getJobGroupId() {
      return jobGroupId;
   }

   public void setJobGroupId(Long jobGroupId) {
      this.jobGroupId = jobGroupId;
   }

   public String getResultLocation() {
      return resultLocation;
   }

   public void setResultLocation(String resultLocation) {
      this.resultLocation = resultLocation;
   }

   public Integer getResultSize() {
      return resultSize;
   }

   public void setResultSize(Integer resultSize) {
      this.resultSize = resultSize;
   }

   public String getExecutionContent() {
      return executionContent;
   }

   public void setExecutionContent(String executionContent) {
      this.executionContent = executionContent;
   }

   public String getJobGroupInfo() {
      return jobGroupInfo;
   }

   public void setJobGroupInfo(String jobGroupInfo) {
      this.jobGroupInfo = jobGroupInfo;
   }

   public Date getCreatedTime() {
      return createdTime;
   }

   public void setCreatedTime(Date createdTime) {
      this.createdTime = createdTime;
   }

   public Date getUpdatedTime() {
      return updatedTime;
   }

   public void setUpdatedTime(Date updatedTime) {
      this.updatedTime = updatedTime;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public Integer getPriority() {
      return priority;
   }

   public void setPriority(Integer priority) {
      this.priority = priority;
   }
}
