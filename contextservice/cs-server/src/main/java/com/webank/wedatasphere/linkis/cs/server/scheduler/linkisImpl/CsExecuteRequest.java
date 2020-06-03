package com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl;

import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;

import java.util.function.Consumer;

/**
 * Created by patinousward on 2020/2/18.
 */
public class CsExecuteRequest implements ExecuteRequest, JobToExecuteRequestConsumer<HttpJob> {

    private HttpJob httpJob;

    // TODO: 2020/3/3 变量名修改
    private Consumer<HttpJob> jobConsumer;

    @Override
    public String code() {
        return null;
    }


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
}
