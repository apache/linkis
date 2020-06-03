package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl.CsJobListener;
import com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl.CsSchedulerJob;
import com.webank.wedatasphere.linkis.cs.server.service.Service;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.queue.Group;
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by patinousward on 2020/2/21.
 */
@Component
public class DefaultCsScheduler implements CsScheduler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private List<Service> services;

    @Override
    public void addService(Service service) {
        services.add(service);
    }

    @Override
    public Service[] getServices() {
        return this.services.toArray(new Service[]{});
    }

    @Override
    public void sumbit(HttpJob job) throws InterruptedException {
        // TODO: 2020/3/3 参数配置化 
        GroupFactory groupFactory = scheduler.getSchedulerContext().getOrCreateGroupFactory();
        Group group = groupFactory.getOrCreateGroup(job.getRequestProtocol().getUsername());
        if (group instanceof ParallelGroup) {
            ParallelGroup parallelGroup = (ParallelGroup) group;
            if (parallelGroup.getMaxRunningJobs() == 0) {
                parallelGroup.setMaxRunningJobs(100);
            }
            if (parallelGroup.getMaxAskExecutorTimes() == 0) {
                parallelGroup.setMaxAskExecutorTimes(1000);
            }
        }
        //create csJob
        Job csJob = buildJob(job);
        //注册listener
        csJob.setJobListener(new CsJobListener());
        scheduler.submit(csJob);
        if (job instanceof HttpAnswerJob) {
            HttpAnswerJob answerJob = (HttpAnswerJob) job;
            answerJob.getResponseProtocol().waitTimeEnd(5000);
        }
    }

    private Job buildJob(HttpJob job) {
        CsSchedulerJob csJob = new CsSchedulerJob();
        //暂时将groupName给jobid
        csJob.setId(job.getRequestProtocol().getUsername());
        csJob.set(job);
        //从多个serveice中找出一个合适执行的service
        Optional<Service> service = Arrays.stream(getServices()).filter(s -> s.ifAccept(job)).findFirst();
        if (service.isPresent()) {
            logger.info(String.format("find %s service to execute job",service.get().getName()));
            csJob.setConsuemr(service.get()::accept);
        }
        return csJob;
    }

}
