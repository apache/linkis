/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.server.scheduler.impl;

import org.apache.linkis.cs.server.protocol.HttpResponseProtocol;
import org.apache.linkis.cs.server.protocol.RestResponseProtocol;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.cs.server.scheduler.HttpJob;
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse;
import org.apache.linkis.scheduler.listener.JobListener;
import org.apache.linkis.scheduler.queue.Job;

public class CsJobListener implements JobListener {
  @Override
  public void onJobScheduled(Job job) {
    // nothing to do
  }

  @Override
  public void onJobInited(Job job) {
    // nothing to do
  }

  @Override
  public void onJobWaitForRetry(Job job) {
    // nothing to do
  }

  @Override
  public void onJobRunning(Job job) {
    // nothing to do
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
          if (errorResponse != null) {
            ((RestResponseProtocol) responseProtocol)
                .error(errorResponse.message(), errorResponse.t());
          }
        }
        answerJob.getResponseProtocol().notifyJob();
      }
    }
  }
}
