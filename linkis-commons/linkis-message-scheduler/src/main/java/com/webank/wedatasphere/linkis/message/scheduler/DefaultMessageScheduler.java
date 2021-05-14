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

import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.builder.MessageJobListener;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorManager;
import com.webank.wedatasphere.linkis.scheduler.queue.Group;
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelSchedulerContextImpl;

/**
 * @date 2020/7/17
 */
public class DefaultMessageScheduler implements MessageScheduler {

    // TODO: 2020/7/22 configuration
    private static final int MAX_RUNING_JOB = Runtime.getRuntime().availableProcessors() * 2;

    private static final int MAX_PARALLELISM_USERS = Runtime.getRuntime().availableProcessors();

    private static final int MAX_ASK_EXECUTOR_TIMES = 1000;

    private static final String GROUP_NAME = "message-scheduler";

    private final Scheduler linkisScheduler;

    {
        MessageExecutorExecutionManager messageExecutorManager = new MessageExecutorExecutionManager();
        linkisScheduler = new ParallelScheduler(
                new ParallelSchedulerContextImpl(MAX_PARALLELISM_USERS) {
                    @Override
                    public ExecutorManager getOrCreateExecutorManager() {
                        return messageExecutorManager;
                    }
                });
        linkisScheduler.init();
        GroupFactory groupFactory = linkisScheduler.getSchedulerContext().getOrCreateGroupFactory();
        //one consumer group is enough
        Group group = groupFactory.getOrCreateGroup(GROUP_NAME);
        if (group instanceof ParallelGroup) {
            ParallelGroup parallelGroup = (ParallelGroup) group;
            if (parallelGroup.getMaxRunningJobs() == 0) {
                parallelGroup.setMaxRunningJobs(MAX_RUNING_JOB);
            }
            if (parallelGroup.getMaxAskExecutorTimes() == 0) {
                parallelGroup.setMaxAskExecutorTimes(MAX_ASK_EXECUTOR_TIMES);
            }
        }
    }

    @Override
    public void submit(MessageJob messageJob) {
        if (messageJob instanceof Job) {
            ((Job) messageJob).setId(GROUP_NAME);
            ((Job) messageJob).setJobListener(new MessageJobListener());
            linkisScheduler.submit((Job) messageJob);
        }
    }


}
