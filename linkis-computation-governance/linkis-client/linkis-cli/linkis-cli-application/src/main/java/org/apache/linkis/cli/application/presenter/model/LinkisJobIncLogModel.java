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
 
package org.apache.linkis.cli.application.presenter.model;

import org.apache.linkis.cli.core.presenter.model.JobExecModel;


public class LinkisJobIncLogModel extends JobExecModel {

    private final StringBuilder incLogBuilder = new StringBuilder();
    private String taskID;
    private String execID;
    private String user;
    private int fromLine = 0;
    private String logPath;
    private double progress;


    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getExecID() {
        return execID;
    }

    public void setExecID(String execID) {
        this.execID = execID;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public final int getFromLine() {
        return fromLine;
    }

    public final void setFromLine(int fromLine) {
        this.fromLine = fromLine;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public double getJobProgress() {
        return progress;
    }

    public void setJobProgress(double progress) {
        this.progress = progress;
    }

    public final String readAndClearIncLog() {
        String ret = incLogBuilder.toString();
        incLogBuilder.setLength(0);
        return ret;
    }

    public final void writeIncLog(String incLog) {
        incLogBuilder.append(incLog);
    }


}