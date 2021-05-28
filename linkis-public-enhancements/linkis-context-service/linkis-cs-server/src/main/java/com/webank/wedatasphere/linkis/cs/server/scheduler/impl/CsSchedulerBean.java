package com.webank.wedatasphere.linkis.cs.server.scheduler.impl;

import com.webank.wedatasphere.linkis.cs.server.conf.ContextServerConf;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext;
import com.webank.wedatasphere.linkis.scheduler.queue.fifoqueue.FIFOGroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelSchedulerContextImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import(CsExecutorExecutionManager.class)
public class CsSchedulerBean {

    @Bean
    public SchedulerContext getSchedulerContext(CsExecutorExecutionManager csExecutorManager) {
        ParallelSchedulerContextImpl parallelSchedulerContext = new ParallelSchedulerContextImpl(3000);
        parallelSchedulerContext.setExecutorManager(csExecutorManager);
        if(parallelSchedulerContext.getOrCreateGroupFactory() instanceof FIFOGroupFactory) {
            FIFOGroupFactory groupFactory = (FIFOGroupFactory) parallelSchedulerContext.getOrCreateGroupFactory();
            groupFactory.setDefaultMaxRunningJobs(ContextServerConf.CS_SCHEDULER_MAX_RUNNING_JOBS);
            groupFactory.setDefaultMaxAskExecutorTimes(ContextServerConf.CS_SCHEDULER_MAX_ASK_EXECUTOR_TIMES);
        }
        return parallelSchedulerContext;
    }

    @Bean
    public Scheduler getScheduler(SchedulerContext context) {
        ParallelScheduler parallelScheduler = new ParallelScheduler(context);
        parallelScheduler.init();
        return parallelScheduler;
    }

}
