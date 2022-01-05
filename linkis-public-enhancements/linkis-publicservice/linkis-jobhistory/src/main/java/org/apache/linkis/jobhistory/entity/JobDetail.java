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
 
package org.apache.linkis.jobhistory.entity;

import java.util.Date;
import org.apache.linkis.jobhistory.util.QueryUtils;


public class JobDetail {


  private Long  id;


  private Long job_history_id;

  /*
  separated multi result path
   */

   private String result_location;

  /*
  how many result sets
   */

   private Integer result_array_size;

  /*
  code
   */

   private String execution_content;

  /*
  json of jobGroup
   */

   private String job_group_info;


   private Date created_time;


   private Date updated_time;



   private String status;


   private Integer priority;

    private String updatedTimeMills;

    public String getUpdatedTimeMills() {
        return  QueryUtils.dateToString(getUpdated_time());
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJob_history_id() {
        return job_history_id;
    }

    public void setJob_history_id(Long job_history_id) {
        this.job_history_id = job_history_id;
    }

    public String getResult_location() {
        return result_location;
    }

    public void setResult_location(String result_location) {
        this.result_location = result_location;
    }

    public Integer getResult_array_size() {
        return result_array_size;
    }

    public void setResult_array_size(Integer result_array_size) {
        this.result_array_size = result_array_size;
    }

    public String getExecution_content() {
        return execution_content;
    }

    public void setExecution_content(String execution_content) {
        this.execution_content = execution_content;
    }

    public String getJob_group_info() {
        return job_group_info;
    }

    public void setJob_group_info(String job_group_info) {
        this.job_group_info = job_group_info;
    }

    public Date getCreated_time() {
        return created_time;
    }

    public void setCreated_time(Date created_time) {
        this.created_time = created_time;
    }

    public Date getUpdated_time() {
        return updated_time;
    }

    public void setUpdated_time(Date updated_time) {
        this.updated_time = updated_time;
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
