package com.webank.wedatasphere.linkis.cs.server.scheduler.impl;

import com.webank.wedatasphere.linkis.cs.server.protocol.HttpResponseProtocol;
import com.webank.wedatasphere.linkis.cs.server.protocol.RestResponseProtocol;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;
import com.webank.wedatasphere.linkis.scheduler.executer.ErrorExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.listener.JobListener;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;


public class CsJobListener implements JobListener {
    @Override
    public void onJobScheduled(Job job) {
        //nothing to do
    }

    @Override
    public void onJobInited(Job job) {
        //nothing to do
    }

    @Override
    public void onJobWaitForRetry(Job job) {
        //nothing to do
    }

    @Override
    public void onJobRunning(Job job) {
        //nothing to do
    }

    @Override
    public void onJobCompleted(Job job) {
        // TODO: 2020/2/22 正常 /异常
        if (job instanceof CsSchedulerJob) {
            HttpJob httpJob = ((CsSchedulerJob) job).get();
            if (httpJob instanceof HttpAnswerJob) {
                HttpAnswerJob answerJob = (HttpAnswerJob) httpJob;
                HttpResponseProtocol responseProtocol = answerJob.getResponseProtocol();
                if (!job.isSucceed() && responseProtocol instanceof RestResponseProtocol) {
                    ErrorExecuteResponse errorResponse = job.getErrorResponse();
                    ((RestResponseProtocol) responseProtocol).error(errorResponse.message(), errorResponse.t());
                }
                answerJob.getResponseProtocol().notifyJob();
            }
        }
    }
}
