/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl;

import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.scheduler.queue.JobInfo;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by patinousward on 2020/2/18.
 */
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
