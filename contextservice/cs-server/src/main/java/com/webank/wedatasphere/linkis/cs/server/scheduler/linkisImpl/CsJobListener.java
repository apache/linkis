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

import com.webank.wedatasphere.linkis.cs.server.protocol.HttpResponseProtocol;
import com.webank.wedatasphere.linkis.cs.server.protocol.RestResponseProtocol;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;
import com.webank.wedatasphere.linkis.scheduler.executer.ErrorExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.listener.JobListener;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;

/**
 * Created by patinousward on 2020/2/22.
 */
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
