/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.message.scheduler;

import static com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf.MAX_PARALLELISM_CONSUMERS;
import static com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf.MAX_QUEUE_CAPACITY;
import static com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf.MAX_RUNNING_JOB;

import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.builder.MessageJobListener;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOGroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelConsumerManager;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelSchedulerContextImpl;


public class DefaultMessageScheduler implements MessageScheduler {

    private static final int MAX_ASK_EXECUTOR_TIMES = 1000;

    private final Scheduler linkisScheduler;

    private static final String GROUP_NAME = "message-scheduler";

    public Scheduler getLinkisScheduler() {
        return linkisScheduler;
    }

    public DefaultMessageScheduler() {
        this(null);
    }

    public DefaultMessageScheduler(GroupFactory groupFactory){
        ParallelSchedulerContextImpl schedulerContext = new ParallelSchedulerContextImpl(MAX_PARALLELISM_CONSUMERS);
        schedulerContext.setConsumerManager(new ParallelConsumerManager(MAX_PARALLELISM_CONSUMERS, "RpcMessageScheduler"));
        schedulerContext.setExecutorManager(new MessageExecutorExecutionManager());
        if(groupFactory != null) {
            schedulerContext.setGroupFactory(groupFactory);
        } else {
            groupFactory = schedulerContext.getOrCreateGroupFactory();
            if(groupFactory instanceof FIFOGroupFactory) {
                FIFOGroupFactory fifoGroupFactory = (FIFOGroupFactory) groupFactory;
                fifoGroupFactory.setDefaultMaxRunningJobs(MAX_RUNNING_JOB);
                fifoGroupFactory.setDefaultMaxAskExecutorTimes(MAX_ASK_EXECUTOR_TIMES);
                fifoGroupFactory.setDefaultMaxCapacity(MAX_QUEUE_CAPACITY);
            }
        }
        linkisScheduler = new ParallelScheduler(schedulerContext);
        linkisScheduler.init();
    }

    @Override
    public void submit(MessageJob messageJob) {
        if (messageJob instanceof Job) {
            if (null == messageJob.getMethodContext().getSender()) {
                ((Job) messageJob).setId(GROUP_NAME);
            } else {
                ((Job) messageJob).setId(messageJob.getMethodContext().getSender().toString());
            }

            ((Job) messageJob).setJobListener(new MessageJobListener());
            linkisScheduler.submit((Job) messageJob);
        }
    }


}
