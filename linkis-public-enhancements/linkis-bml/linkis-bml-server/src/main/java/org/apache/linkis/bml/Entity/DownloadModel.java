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
 
package org.apache.linkis.bml.Entity;

import java.util.Date;

public class DownloadModel {
    private int id;
    private Date startTime;
    private Date endTime;
    private String clientIp;
    /**
     * state 表示下载是否成功
     * 0 表示成功
     * 1 表示失败
     */
    private Integer state;
    private String resourceId;
    private String version;
    private String downloader;

    public DownloadModel(){

    }

    public DownloadModel(String resourceId, String version,String downloader, String clientIp){
        this.resourceId = resourceId;
        this.version=version;
        this.startTime = new Date(System.currentTimeMillis());
        this.downloader = downloader;
        this.clientIp = clientIp;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDownloader() {
        return downloader;
    }

    public void setDownloader(String downloader) {
        this.downloader = downloader;
    }

    @Override
    public String toString() {
        return "DownloadModel{" +
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", clientIp='" + clientIp + '\'' +
                ", state=" + state +
                ", resourceId='" + resourceId + '\'' +
                ", downloader='" + downloader + '\'' +
                '}';
    }
}
