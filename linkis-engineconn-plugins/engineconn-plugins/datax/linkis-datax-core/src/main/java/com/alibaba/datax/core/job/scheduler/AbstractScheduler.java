package com.alibaba.datax.core.job.scheduler;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.alibaba.datax.core.util.ErrorRecordChecker;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.alibaba.datax.enums.State;
import org.apache.linkis.datax.core.job.scheduler.speed.DefaultVariableTaskGroupSpeedStrategy;
import org.apache.linkis.datax.core.job.scheduler.speed.VariableTaskGroupSpeedStrategy;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractScheduler {
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractScheduler.class);

    private ErrorRecordChecker errorLimit;

    private AbstractContainerCommunicator containerCommunicator;

    private VariableTaskGroupSpeedStrategy variableTaskGroupSpeedStrategy;

    private Long jobId;

    private boolean scheduable;

    public Long getJobId() {
        return jobId;
    }

    public AbstractScheduler(AbstractContainerCommunicator containerCommunicator) {
        this(containerCommunicator, new DefaultVariableTaskGroupSpeedStrategy());
    }

    public AbstractScheduler(AbstractContainerCommunicator containerCommunicator,
                             VariableTaskGroupSpeedStrategy variableTaskGroupSpeedStrategy){
        this.containerCommunicator = containerCommunicator;
        this.variableTaskGroupSpeedStrategy = variableTaskGroupSpeedStrategy;
    }

    public void schedule(List<Configuration> configurations) {
        Validate.notNull(configurations,
                "scheduler配置不能为空");
        int jobReportIntervalInMillSec = configurations.get(0).getInt(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_REPORTINTERVAL, 30000);
        int jobSleepIntervalInMillSec = configurations.get(0).getInt(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_SLEEPINTERVAL, 10000);

        this.jobId = configurations.get(0).getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);

        errorLimit = new ErrorRecordChecker(configurations.get(0));

        /**
         * 给 taskGroupContainer 的 Communication 注册
         */
        this.containerCommunicator.registerCommunication(configurations);

        int totalTasks = calculateTaskCount(configurations);
        startAllTaskGroup(configurations);

        Communication lastJobContainerCommunication = new Communication();

        long lastReportTimeStamp = System.currentTimeMillis();
        scheduable = true;
        try {
            while (scheduable) {
                /*
                  step 1: collect job stat
                  step 2: getReport info, then report it
                  step 3: errorLimit do check
                  step 4: dealSucceedStat();
                  step 5: dealKillingStat();
                  step 6: dealFailedStat();
                  step 7: refresh last job stat, and then sleep for next while

                  above steps, some ones should report info to DS

                 */
                Communication nowJobContainerCommunication = this.containerCommunicator.collect();
                nowJobContainerCommunication.setTimestamp(System.currentTimeMillis());
                LOG.debug(nowJobContainerCommunication.toString());
                //reporting cycle
                long now = System.currentTimeMillis();
                if (now - lastReportTimeStamp > jobReportIntervalInMillSec) {
                    Communication reportCommunication = CommunicationTool
                            .getReportCommunication(nowJobContainerCommunication, lastJobContainerCommunication, totalTasks);

                    this.containerCommunicator.report(reportCommunication);
                    lastReportTimeStamp = now;
                    lastJobContainerCommunication = nowJobContainerCommunication;
                }

                errorLimit.checkRecordLimit(nowJobContainerCommunication);

                if (nowJobContainerCommunication.getState() == State.SUCCEEDED) {
                    LOG.info("Scheduler accomplished all tasks.");
                    break;
                }

                if (isJobKilling(this.getJobId())) {
                    dealKillingStat(this.containerCommunicator, totalTasks);
                } else if (nowJobContainerCommunication.getState() == State.FAILED) {
                    dealFailedStat(this.containerCommunicator, nowJobContainerCommunication.getThrowable());
                }
                Configuration configuration = this.containerCommunicator.getConfiguration();
                boolean adjust = variableTaskGroupSpeedStrategy.adjustSpeed(nowJobContainerCommunication, configuration);
                if(adjust){
                    adjustTaskGroupSpeed(configuration.getLong(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE, 0),
                            configuration.getLong(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD, 0));
                }
                Thread.sleep(jobSleepIntervalInMillSec);
            }
        } catch (InterruptedException e) {
            LOG.error("Catch InterruptedException exception", e);
            Thread.currentThread().interrupt();
            throw DataXException.asDataXException(
                    FrameworkErrorCode.RUNTIME_ERROR, e);
        }

    }

    /**
     * stop schedule
     */
    public  void stopSchedule(){
        scheduable = false;
        cancelAllTaskGroup();
    }

    /**
     * start all task groups
     * @param configurations
     */
    protected abstract void startAllTaskGroup(List<Configuration> configurations);

    /**
     * cancel all task groups
     */
    protected abstract void cancelAllTaskGroup();

    /**
     * deal the fail status
     * @param frameworkCollector
     * @param throwable
     */
    protected abstract void dealFailedStat(AbstractContainerCommunicator frameworkCollector, Throwable throwable);

    /**
     * deal the kill status
     * @param frameworkCollector
     * @param totalTasks
     */
    protected abstract void dealKillingStat(AbstractContainerCommunicator frameworkCollector, int totalTasks);

    /**
     * adjust speed
     * @param byteSpeed
     * @param recordSpeed
     */
    protected abstract void adjustTaskGroupSpeed(long byteSpeed, long recordSpeed);


    /**
     * if the job has been killed
     * @param jobId
     * @return
     */
    protected abstract boolean isJobKilling(Long jobId);

    private int calculateTaskCount(List<Configuration> configurations) {
        int totalTasks = 0;
        for (Configuration taskGroupConfiguration : configurations) {
            totalTasks += taskGroupConfiguration.getListConfiguration(
                    CoreConstant.DATAX_JOB_CONTENT).size();
        }

        return totalTasks;
    }

}
