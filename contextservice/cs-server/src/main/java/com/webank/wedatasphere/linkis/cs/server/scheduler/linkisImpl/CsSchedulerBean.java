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
