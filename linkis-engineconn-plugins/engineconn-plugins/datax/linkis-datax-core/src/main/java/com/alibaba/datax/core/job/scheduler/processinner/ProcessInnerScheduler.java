package com.alibaba.datax.core.job.scheduler.processinner;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.job.scheduler.AbstractScheduler;
import com.alibaba.datax.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.alibaba.datax.core.taskgroup.TaskGroupContainer;
import com.alibaba.datax.core.taskgroup.runner.TaskGroupContainerRunner;
import com.alibaba.datax.core.util.FrameworkErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class ProcessInnerScheduler extends AbstractScheduler {

    private ExecutorService taskGroupContainerExecutorService;

    private ThreadPoolExecutor threadPoolExecutor;

    private List<TaskGroupContainer> taskGroupContainers = new ArrayList<>();

    ProcessInnerScheduler(AbstractContainerCommunicator containerCommunicator) {
        super(containerCommunicator);
    }

    @Override
    public void startAllTaskGroup(List<Configuration> configurations) {
        this.taskGroupContainerExecutorService = Executors
                .newFixedThreadPool(configurations.size());

        for (Configuration taskGroupConfiguration : configurations) {
            TaskGroupContainerRunner taskGroupContainerRunner = newTaskGroupContainerRunner(taskGroupConfiguration);
            //store task group containers
            taskGroupContainers.add(taskGroupContainerRunner.getTaskGroupContainer());
            this.taskGroupContainerExecutorService.execute(taskGroupContainerRunner);
        }

        this.taskGroupContainerExecutorService.shutdown();
    }

    @Override
    public void dealFailedStat(AbstractContainerCommunicator frameworkCollector, Throwable throwable) {
        this.taskGroupContainerExecutorService.shutdownNow();
        throw DataXException.asDataXException(
                FrameworkErrorCode.PLUGIN_RUNTIME_ERROR, throwable);
    }


    @Override
    public void dealKillingStat(AbstractContainerCommunicator frameworkCollector, int totalTasks) {
        this.taskGroupContainerExecutorService.shutdownNow();
    }

    @Override
    protected void cancelAllTaskGroup() {
        //shutdown each task group container
        taskGroupContainers.forEach(TaskGroupContainer::shutdown);
        //then to close the thread pool
        this.taskGroupContainerExecutorService.shutdownNow();
    }

    @Override
    protected void adjustTaskGroupSpeed(long byteSpeed, long recordSpeed) {
        taskGroupContainers.forEach(taskGroupContainer -> taskGroupContainer.adjustSpeed(byteSpeed, recordSpeed));
    }

    private TaskGroupContainerRunner newTaskGroupContainerRunner(
            Configuration configuration) {
        TaskGroupContainer taskGroupContainer = new TaskGroupContainer(configuration);
        return new TaskGroupContainerRunner(taskGroupContainer);
    }

}
