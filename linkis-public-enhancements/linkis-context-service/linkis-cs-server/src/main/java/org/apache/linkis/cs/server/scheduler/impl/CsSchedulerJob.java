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
 
package org.apache.linkis.cs.server.scheduler.impl;

import org.apache.linkis.cs.server.scheduler.HttpJob;
import org.apache.linkis.scheduler.executer.ExecuteRequest;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.scheduler.queue.JobInfo;

import java.io.IOException;
import java.util.function.Consumer;


public class CsSchedulerJob extends Job implements JobToExecuteRequestConsumer<HttpJob> {

    private HttpJob httpJob;

    private Consumer<HttpJob> jobConsumer;

    @Override
    public HttpJob get() {
        return this.httpJob;
    }

    @Override
    public void set(HttpJob httpJob) {
        this.httpJob = httpJob;
    }

    @Override
    public Consumer<HttpJob> getConsumer() {
        return this.jobConsumer;
    }

    @Override
    public void setConsuemr(Consumer<HttpJob> jobConsumer) {
        this.jobConsumer = jobConsumer;
    }


    @Override
    public void init() {
        // TODO: 2020/2/18
    }

    @Override
    public ExecuteRequest jobToExecuteRequest() {
        CsExecuteRequest request = new CsExecuteRequest();
        request.set(httpJob);
        request.setConsuemr(jobConsumer);
        return request;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public JobInfo getJobInfo() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }
}
