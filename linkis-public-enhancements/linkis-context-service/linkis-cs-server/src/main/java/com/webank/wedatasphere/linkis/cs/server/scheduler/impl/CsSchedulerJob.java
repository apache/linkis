package com.webank.wedatasphere.linkis.cs.server.scheduler.impl;

import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.scheduler.queue.JobInfo;

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
