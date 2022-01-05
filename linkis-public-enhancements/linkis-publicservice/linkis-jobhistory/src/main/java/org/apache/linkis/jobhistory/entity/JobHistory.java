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


public class JobHistory {

    private Long id;

    private String job_req_id;

    private String submit_user;

    private String execute_user;

    private String source;

    private String labels;

    private String params;

    private String progress;

    private String status;

    private String log_path;

    private Integer error_code;

    private String error_desc;

    private Date created_time;

    private Date updated_time;

    private String updateTimeMills;

    private String instances;

    private String metrics;

    private String engine_type;

    private String execution_code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJob_req_id() {
        return job_req_id;
    }

    public void setJob_req_id(String job_req_id) {
        this.job_req_id = job_req_id;
    }

    public String getSubmit_user() {
        return submit_user;
    }

    public void setSubmit_user(String submit_user) {
        this.submit_user = submit_user;
    }

    public String getExecute_user() {
        return execute_user;
    }

    public void setExecute_user(String execute_user) {
        this.execute_user = execute_user;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLog_path() {
        return log_path;
    }

    public void setLog_path(String log_path) {
        this.log_path = log_path;
    }

    public Integer getError_code() {
        return error_code;
    }

    public void setError_code(Integer error_code) {
        this.error_code = error_code;
    }

    public String getError_desc() {
        return error_desc;
    }

    public void setError_desc(String error_desc) {
        this.error_desc = error_desc;
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

    public String getInstances() {
        return instances;
    }

    public void setInstances(String instances) {
        this.instances = instances;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public String getEngine_type() {
        return engine_type;
    }

    public void setEngine_type(String engine_type) {
        this.engine_type = engine_type;
    }

    public String getExecution_code() {
        return execution_code;
    }

    public void setExecution_code(String execution_code) {
        this.execution_code = execution_code;
    }

    public String getUpdateTimeMills(){
        return  QueryUtils.dateToString(getUpdated_time());
    }

}
