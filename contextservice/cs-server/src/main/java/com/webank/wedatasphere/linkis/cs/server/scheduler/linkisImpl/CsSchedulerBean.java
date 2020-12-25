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

import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorManager;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelSchedulerContextImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by patinousward on 2020/2/18.
 */
@Configuration
@Import(CsExecutorManager.class)
public class CsSchedulerBean {

    @Bean
    public SchedulerContext getSchedulerContext(CsExecutorManager csExecutorManager) {
        return new ParallelSchedulerContextImpl(3000) {
            @Override
            public ExecutorManager getOrCreateExecutorManager() {
                return csExecutorManager;
            }
        };
    }

    @Bean
    public Scheduler getScheduler(SchedulerContext context) {
        ParallelScheduler parallelScheduler = new ParallelScheduler(context);
        parallelScheduler.init();
        return parallelScheduler;
    }

}
