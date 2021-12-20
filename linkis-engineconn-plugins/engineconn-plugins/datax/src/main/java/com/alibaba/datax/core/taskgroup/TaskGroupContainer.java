package com.alibaba.datax.core.taskgroup;

import com.alibaba.datax.common.constant.PluginType;
import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.RecordSender;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.statistics.PerfRecord;
import com.alibaba.datax.common.statistics.PerfTrace;
import com.alibaba.datax.common.statistics.VMInfo;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.AbstractContainer;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.statistics.container.communicator.taskgroup.StandaloneTGContainerCommunicator;
import com.alibaba.datax.core.statistics.plugin.task.AbstractTaskPluginCollector;
import com.alibaba.datax.core.taskgroup.runner.AbstractRunner;
import com.alibaba.datax.core.taskgroup.runner.ReaderRunner;
import com.alibaba.datax.core.taskgroup.runner.WriterRunner;
import com.alibaba.datax.core.transport.channel.RecordChannel;
import com.alibaba.datax.core.transport.exchanger.BufferedRecordExchanger;
import com.alibaba.datax.core.transport.exchanger.BufferedRecordTransformerExchanger;
import com.alibaba.datax.core.transport.transformer.TransformerExecution;
import com.alibaba.datax.core.util.ClassUtil;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.TransformerUtil;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.alibaba.datax.core.util.container.LoadUtil;
import com.alibaba.datax.dataxservice.face.domain.enums.State;
import com.alibaba.fastjson.JSON;
import com.webank.wedatasphere.exchangis.datax.common.constant.TransportType;
import com.webank.wedatasphere.exchangis.datax.core.transport.channel.StreamChannel;
import com.webank.wedatasphere.exchangis.datax.core.transport.stream.ChannelInput;
import com.webank.wedatasphere.exchangis.datax.core.transport.stream.ChannelOutput;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskGroupContainer extends AbstractContainer {
    private static final Log LOG = LogFactory.getLog(TaskGroupContainer.class);

    /**
     * 当前taskGroup所属jobId
     */
    private long jobId;

    /**
     * 当前taskGroupId
     */
    private int taskGroupId;

    /**
     * 使用的record channel类
     */
    private String recordChannelClazz;

    private String streamChannelClazz;
    /**
     * task收集器使用的类
     */
    private String taskCollectorClass;

    private TransportType transportType;

    private TaskMonitor taskMonitor = TaskMonitor.getInstance();

    private volatile boolean isShutdown = false;
    /**
     * running task
     */
    private CopyOnWriteArrayList<TaskExecutor> runTasks;


    Communication lastTaskGroupContainerCommunication = new Communication();
    Communication scheduleReport;
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);


    public TaskGroupContainer(Configuration configuration) {
        super(configuration);
        initCommunicator(configuration);

        this.jobId = this.configuration.getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);
        this.taskGroupId = this.configuration.getInt(
                CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);

        this.recordChannelClazz = this.configuration.getString(
                CoreConstant.DATAX_CORE_TRANSPORT_RECORD_CHANNEL_CLASS);

        this.streamChannelClazz = this.configuration.getString(
                CoreConstant.DATAX_CORE_TRANSPORT_STREAM_CHANNEL_CLASS);

        this.taskCollectorClass = this.configuration.getString(
                CoreConstant.DATAX_CORE_STATISTICS_COLLECTOR_PLUGIN_TASKCLASS);

        this.transportType = TransportType.valueOf(this.configuration.getString(
                CoreConstant.DATAX_CORE_TRANSPORT_TYPE).toUpperCase());

    }

    private void initCommunicator(Configuration configuration) {
        super.setContainerCommunicator(new StandaloneTGContainerCommunicator(configuration));

    }

    public long getJobId() {
        return jobId;
    }

    public int getTaskGroupId() {
        return taskGroupId;
    }

    @Override
    public void start() {
        try {
            /**
             * 状态check时间间隔，较短，可以把任务及时分发到对应channel中
             */
            int sleepIntervalInMillSec = this.configuration.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_SLEEPINTERVAL, 100);
            /**
             * 状态汇报时间间隔，稍长，避免大量汇报
             */
            long reportIntervalInMillSec = this.configuration.getLong(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_REPORTINTERVAL,
                    10000);
            /**
             * 2分钟汇报一次性能统计
             */

            // 获取channel数目
            int channelNumber = this.configuration.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL);

            int taskMaxRetryTimes = this.configuration.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASK_FAILOVER_MAXRETRYTIMES, 1);

            long taskRetryIntervalInMsec = this.configuration.getLong(
                    CoreConstant.DATAX_CORE_CONTAINER_TASK_FAILOVER_RETRYINTERVALINMSEC, 10000);

            long taskMaxWaitInMsec = this.configuration.getLong(CoreConstant.DATAX_CORE_CONTAINER_TASK_FAILOVER_MAXWAITINMSEC, 60000);

            List<Configuration> taskConfigs = this.configuration
                    .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT);

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("taskGroup[%d]'s task configs[%s]", this.taskGroupId,
                        JSON.toJSONString(taskConfigs)));
            }

            int taskCountInThisTaskGroup = taskConfigs.size();
            LOG.info(String.format(
                    "taskGroupId=[%d] start [%d] channels for [%d] tasks.",
                    this.taskGroupId, channelNumber, taskCountInThisTaskGroup));

            this.containerCommunicator.registerCommunication(taskConfigs);

            runTasks = new CopyOnWriteArrayList<>();
            //taskId与task配置
            Map<Integer, Configuration> taskConfigMap = buildTaskConfigMap(taskConfigs);
            //待运行task列表
            List<Configuration> taskQueue = buildRemainTasks(taskConfigs);
            //taskId与上次失败实例
            Map<Integer, TaskExecutor> taskFailedExecutorMap = new HashMap<>(8);
            //任务开始时间
            Map<Integer, Long> taskStartTimeMap = new HashMap<>(8);

            long lastReportTimeStamp = 0;
            Communication lastTaskGroupContainerCommunication = new Communication();

            while (!isShutdown) {
                //1.判断task状态
                boolean failedOrKilled = false;
                Map<Integer, Communication> communicationMap = containerCommunicator.getCommunicationMap();
                for (Map.Entry<Integer, Communication> entry : communicationMap.entrySet()) {
                    Integer taskId = entry.getKey();
                    Communication taskCommunication = entry.getValue();
                    if (!taskCommunication.isFinished()) {
                        continue;
                    }
                    TaskExecutor taskExecutor = removeTask(runTasks, taskId);

                    //上面从runTasks里移除了，因此对应在monitor里移除
                    taskMonitor.removeTask(taskId);

                    //失败，看task是否支持failover，重试次数未超过最大限制
                    if (taskCommunication.getState() == State.FAILED) {
                        taskFailedExecutorMap.put(taskId, taskExecutor);
                        if (null != taskExecutor &&
                                taskExecutor.supportFailOver() && taskExecutor.getAttemptCount() < taskMaxRetryTimes) {
                            taskExecutor.shutdown(); //关闭老的executor
                            //将task的状态重置
                            containerCommunicator.resetCommunication(taskId);
                            Configuration taskConfig = taskConfigMap.get(taskId);
                            //重新加入任务列表
                            taskQueue.add(taskConfig);
                        } else {
                            failedOrKilled = true;
                            break;
                        }
                    } else if (taskCommunication.getState() == State.KILLED) {
                        failedOrKilled = true;
                        break;
                    } else if (taskCommunication.getState() == State.SUCCEEDED) {
                        Long taskStartTime = taskStartTimeMap.get(taskId);
                        if (taskStartTime != null) {
                            Long usedTime = System.currentTimeMillis() - taskStartTime;
                            LOG.info(String.format("taskGroup[%d] taskId[%d] is successed, used[%d]ms",
                                    this.taskGroupId, taskId, usedTime));
                            //usedTime*1000*1000 转换成PerfRecord记录的ns，这里主要是简单登记，进行最长任务的打印。因此增加特定静态方法
                            PerfRecord.addPerfRecord(taskGroupId, taskId, PerfRecord.PHASE.TASK_TOTAL, taskStartTime, usedTime * 1000L * 1000L);
                            taskStartTimeMap.remove(taskId);
                            taskConfigMap.remove(taskId);
                        }
                    }
                }

                // 2.发现该taskGroup下taskExecutor的总状态失败则汇报错误
                if (failedOrKilled) {
                    lastTaskGroupContainerCommunication = reportTaskGroupCommunication(
                            lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);

                    throw DataXException.asDataXException(
                            FrameworkErrorCode.PLUGIN_RUNTIME_ERROR, lastTaskGroupContainerCommunication.getThrowable());
                }

                //3.有任务未执行，且正在运行的任务数小于最大通道限制
                Iterator<Configuration> iterator = taskQueue.iterator();
                while (iterator.hasNext() && null != runTasks && runTasks.size() < channelNumber) {
                    Configuration taskConfig = iterator.next();
                    Integer taskId = taskConfig.getInt(CoreConstant.TASK_ID);
                    int attemptCount = 1;
                    TaskExecutor lastExecutor = taskFailedExecutorMap.get(taskId);
                    if (lastExecutor != null) {
                        attemptCount = lastExecutor.getAttemptCount() + 1;
                        long now = System.currentTimeMillis();
                        long failedTime = lastExecutor.getTimeStamp();
                        //未到等待时间，继续留在队列
                        if (now - failedTime < taskRetryIntervalInMsec) {
                            continue;
                        }
                        //上次失败的task仍未结束
                        if (!lastExecutor.isShutdown()) {
                            if (now - failedTime > taskMaxWaitInMsec) {
                                markCommunicationFailed(taskId);
                                reportTaskGroupCommunication(lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);
                                throw DataXException.asDataXException(CommonErrorCode.WAIT_TIME_EXCEED, "task failover等待超时");
                            } else {
                                lastExecutor.shutdown(); //再次尝试关闭
                                continue;
                            }
                        } else {
                            LOG.info(String.format("taskGroup[%d] taskId[%d] attemptCount[%d] has already shutdown",
                                    this.taskGroupId, taskId, lastExecutor.getAttemptCount()));
                        }
                    }
                    Configuration taskConfigForRun = taskMaxRetryTimes > 1 ? taskConfig.clone() : taskConfig;
                    TaskExecutor taskExecutor = new TaskExecutor(taskConfigForRun, attemptCount);
                    taskStartTimeMap.put(taskId, System.currentTimeMillis());
                    iterator.remove();
                    //first to add into the list of running task, then start
                    runTasks.add(taskExecutor);
                    taskExecutor.doStart();
                    //上面，增加task到runTasks列表，因此在monitor里注册。
                    taskMonitor.registerTask(taskId, this.containerCommunicator.getCommunication(taskId));

                    taskFailedExecutorMap.remove(taskId);
                    LOG.info(String.format("taskGroup[%d] taskId[%d] attemptCount[%d] is started",
                            this.taskGroupId, taskId, attemptCount));
                }
                //4.任务列表为空，executor已结束, 搜集状态为success--->成功
                if (taskQueue.isEmpty() && isAllTaskDone(runTasks) && containerCommunicator.collectState() == State.SUCCEEDED) {
                    // 成功的情况下，也需要汇报一次。否则在任务结束非常快的情况下，采集的信息将会不准确
                    lastTaskGroupContainerCommunication = reportTaskGroupCommunication(
                            lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);

                    LOG.info(String.format("taskGroup[%d] completed it's tasks.", this.taskGroupId));
                    break;
                }
                // 5.如果当前时间已经超出汇报时间的interval，那么我们需要马上汇报
                long now = System.currentTimeMillis();
                if (now - lastReportTimeStamp > reportIntervalInMillSec) {
                    lastTaskGroupContainerCommunication = reportTaskGroupCommunication(
                            lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);

                    lastReportTimeStamp = now;

                    //taskMonitor对于正在运行的task，每reportIntervalInMillSec进行检查
                    for (TaskExecutor taskExecutor : runTasks) {
                        taskMonitor.report(taskExecutor.getTaskId(), this.containerCommunicator.getCommunication(taskExecutor.getTaskId()));
                    }

                }

                Thread.sleep(sleepIntervalInMillSec);
            }

            //6.最后还要汇报一次
            reportTaskGroupCommunication(lastTaskGroupContainerCommunication, taskCountInThisTaskGroup);


        } catch (Throwable e) {
            Communication nowTaskGroupContainerCommunication = this.containerCommunicator.collect();

            if (nowTaskGroupContainerCommunication.getThrowable() == null) {
                nowTaskGroupContainerCommunication.setThrowable(e);
            }
            nowTaskGroupContainerCommunication.setState(State.FAILED);
            this.containerCommunicator.report(nowTaskGroupContainerCommunication);

            throw DataXException.asDataXException(
                    FrameworkErrorCode.RUNTIME_ERROR, e);
        } finally {
            if (!PerfTrace.getInstance().isJob()) {
                //最后打印cpu的平均消耗，GC的统计
                VMInfo vmInfo = VMInfo.getVmInfo();
                if (vmInfo != null) {
                    vmInfo.getDelta(false);
                    LOG.info(vmInfo.totalString());
                }

                LOG.info(PerfTrace.getInstance().summarizeNoException());
            }
        }
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        if(null != runTasks && !runTasks.isEmpty()){
            runTasks.forEach(taskExecutor -> {
                taskExecutor.shutdown();
                runTasks.remove(taskExecutor);
            });
            if(runTasks.size() > 0){
                //maybe have new task executors
                runTasks.forEach(TaskExecutor::shutdown);
                runTasks.clear();
            }
        }
    }

    public void adjustSpeed(long byteSpeed, long recordSpeed){
        //first to update configuration
        configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE, byteSpeed);
        configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD, recordSpeed);
        //adjust dynamically
        runTasks.forEach(runTask -> runTask.adjustChannelSpeed(byteSpeed, recordSpeed));
    }

    private Map<Integer, Configuration> buildTaskConfigMap(List<Configuration> configurations) {
        Map<Integer, Configuration> map = new HashMap<>();
        for (Configuration taskConfig : configurations) {
            int taskId = taskConfig.getInt(CoreConstant.TASK_ID);
            map.put(taskId, taskConfig);
        }
        return map;
    }

    private List<Configuration> buildRemainTasks(List<Configuration> configurations) {
        List<Configuration> remainTasks = new LinkedList<>();
        for (Configuration taskConfig : configurations) {
            remainTasks.add(taskConfig);
        }
        return remainTasks;
    }

    private TaskExecutor removeTask(List<TaskExecutor> taskList, int taskId) {
        for(TaskExecutor taskExecutor : taskList){
            if(taskExecutor.getTaskId() == taskId){
                taskList.remove(taskExecutor);
                return taskExecutor;
            }
        }
        return null;
    }

    private boolean isAllTaskDone(List<TaskExecutor> taskList) {
        for (TaskExecutor taskExecutor : taskList) {
            if (!taskExecutor.isTaskFinished()) {
                return false;
            }
        }
        return true;
    }

    private Communication reportTaskGroupCommunication(Communication lastTaskGroupContainerCommunication, int taskCount) {
        Communication nowTaskGroupContainerCommunication = this.containerCommunicator.collect();
        nowTaskGroupContainerCommunication.setTimestamp(System.currentTimeMillis());
        Communication reportCommunication = CommunicationTool.getReportCommunication(nowTaskGroupContainerCommunication,
                lastTaskGroupContainerCommunication, taskCount);
        //mark the number of channel running
        reportCommunication.setLongCounter(CommunicationTool.CHANNEL_RUNNING, runTasks.size());
        this.containerCommunicator.report(reportCommunication);
        return reportCommunication;
    }

    private void markCommunicationFailed(Integer taskId) {
        Communication communication = containerCommunicator.getCommunication(taskId);
        communication.setState(State.FAILED);
    }

    /**
     * TaskExecutor是一个完整task的执行器
     * 其中包括1：1的reader和writerlastTaskGroupContainerCommunication
     */
    class TaskExecutor {
        private Configuration taskConfig;

        private int taskId;

        private int attemptCount;

        private RecordChannel recordChannel;

        private StreamChannel streamChannel;

        private Thread readerThread;

        private List<Thread> writerThreads = new ArrayList<>();

        private ReaderRunner readerRunner;

        /**
         * Support multiply writer runners in executor
         */
        private List<WriterRunner> writerRunners = new ArrayList<>();

        /**
         * 该处的taskCommunication在多处用到：
         * 1. recordChannel
         * 2. readerRunner和writerRunner
         * 3. reader和writer的taskPluginCollector
         */
        private Communication taskCommunication;

        public TaskExecutor(Configuration taskConf, int attemptCount) {
            // 获取该taskExecutor的配置
            this.taskConfig = taskConf;
            Validate.isTrue(null != this.taskConfig.getConfiguration(CoreConstant.JOB_READER)
                            && null != this.taskConfig.getConfiguration(CoreConstant.JOB_WRITER),
                    "[reader|writer]的插件参数不能为空!");

            // 得到taskId
            this.taskId = this.taskConfig.getInt(CoreConstant.TASK_ID);
            this.attemptCount = attemptCount;

            /**
             * 由taskId得到该taskExecutor的Communication
             * 要传给readerRunner和writerRunner，同时要传给channel作统计用
             */
            this.taskCommunication = containerCommunicator
                    .getCommunication(taskId);
            Validate.notNull(this.taskCommunication,
                    String.format("taskId[%d]的Communication没有注册过", taskId));
            this.recordChannel = ClassUtil.instantiate(recordChannelClazz,
                    RecordChannel.class, configuration);
            this.streamChannel = ClassUtil.instantiate(streamChannelClazz,
                    StreamChannel.class, configuration);
            this.recordChannel.setCommunication(this.taskCommunication);
            this.streamChannel.setCommunication(this.taskCommunication);

            /**
             * 获取transformer的参数
             */

            List<TransformerExecution> transformerInfoExecs = TransformerUtil.buildTransformerInfo(taskConfig);

            /**
             * 生成writerThread
             */
            List<Configuration> writerTaskList = this.taskConfig.getListConfiguration(CoreConstant.JOB_WRITER);
            writerTaskList.forEach(writerTask -> {
                String writerPluginName = writerTask.getString(CoreConstant.TASK_NAME);
                Configuration writerTaskConf = writerTask.getConfiguration(CoreConstant.TASK_PARAMETER);
                System.out.println("writer: " + writerTaskConf.toJSON());
                WriterRunner writerRunner = (WriterRunner)generateRunner(PluginType.WRITER, writerPluginName,
                        writerTaskConf);
                writerRunner.setProcessor(writerTask.getString(CoreConstant.TASK_PROCESSOR));
                Thread writerThread = new Thread(writerRunner, String.format("%d-%d-%d-%s",
                        jobId, taskGroupId, this.taskId, writerPluginName));
                //Set thread's contextClassLoader
                writerThread.setContextClassLoader(LoadUtil.getJarLoader(PluginType.WRITER, writerPluginName));
                writerRunners.add(writerRunner);
                writerThreads.add(writerThread);
            });
            /**
             * 生成readerThread
             */
            String readPluginName = this.taskConfig.getString(CoreConstant.JOB_READER_NAME);
            Configuration readerTaskConf = this.taskConfig.getConfiguration(
                    CoreConstant.JOB_READER_PARAMETER);
            readerRunner = (ReaderRunner) generateRunner(PluginType.READER, readPluginName, readerTaskConf, transformerInfoExecs);
            this.readerThread = new Thread(readerRunner,
                    String.format("%d-%d-%d-%s",
                            jobId, taskGroupId, this.taskId, readPluginName));
            /**
             * 通过设置thread的contextClassLoader，即可实现同步和主程序不通的加载器
             */
            this.readerThread.setContextClassLoader(LoadUtil.getJarLoader(
                    PluginType.READER, this.taskConfig.getString(
                            CoreConstant.JOB_READER_NAME)));
        }

        public void doStart() {
            //Run writer threads
            this.writerThreads.forEach(Thread::start);

            this.writerThreads.forEach(writerThread ->{
                if(!writerThread.isAlive() || this.taskCommunication.getState() == State.FAILED){
                    throw DataXException.asDataXException(FrameworkErrorCode.RUNTIME_ERROR,
                            this.taskCommunication.getThrowable());
                }
            });

            //Run reader thread
            this.readerThread.start();

            // 这里reader可能很快结束
            if (!this.readerThread.isAlive() && this.taskCommunication.getState() == State.FAILED) {
                // 这里有可能出现Reader线上启动即挂情况 对于这类情况 需要立刻抛出异常
                throw DataXException.asDataXException(
                        FrameworkErrorCode.RUNTIME_ERROR,
                        this.taskCommunication.getThrowable());
            }
        }


        private AbstractRunner generateRunner(PluginType pluginType, String pluginName, Configuration taskConf) {
            return generateRunner(pluginType, pluginName, taskConf, null);
        }

        private AbstractRunner generateRunner(PluginType pluginType, String pluginName, Configuration taskConf, List<TransformerExecution> transformerInfoExecs) {
            AbstractRunner newRunner = null;
            TaskPluginCollector pluginCollector;

            switch (pluginType) {
                case READER:
                    newRunner = LoadUtil.loadPluginRunner(pluginType, pluginName);
                    newRunner.setJobConf(taskConf);
                    if(null != transportType && transportType.equals(TransportType.STREAM)){
                        ChannelOutput outputStream = new ChannelOutput(streamChannel);
                        ((ReaderRunner)newRunner).setChannelOutput(outputStream);
                    }else {
                        pluginCollector = ClassUtil.instantiate(
                                taskCollectorClass, AbstractTaskPluginCollector.class,
                                configuration, this.taskCommunication,
                                PluginType.READER);

                        RecordSender recordSender;
                        if (transformerInfoExecs != null && transformerInfoExecs.size() > 0) {
                            recordSender = new BufferedRecordTransformerExchanger(taskGroupId, this.taskId, this.recordChannel, this.taskCommunication, pluginCollector, transformerInfoExecs);
                        } else {
                            recordSender = new BufferedRecordExchanger(this.recordChannel, pluginCollector);
                        }
                        /**
                         * 设置taskPlugin的collector，用来处理脏数据和job/task通信
                         */
                        newRunner.setTaskPluginCollector(pluginCollector);
                        ((ReaderRunner) newRunner).setRecordSender(recordSender);
                    }
                    break;
                case WRITER:
                    newRunner = LoadUtil.loadPluginRunner(pluginType, pluginName);
                    newRunner.setJobConf(taskConf);
                    if(null != transportType && transportType.equals(TransportType.STREAM)){
                        ChannelInput inputStream = new ChannelInput(streamChannel);
                        ((WriterRunner)newRunner).setChannelInput(inputStream);
                        //Increase consumer
                        streamChannel.incConsumer();
                    }else {
                        pluginCollector  = ClassUtil.instantiate(
                                taskCollectorClass, AbstractTaskPluginCollector.class,
                                configuration, this.taskCommunication,
                                PluginType.WRITER);
                        ((WriterRunner) newRunner).setRecordReceiver(new BufferedRecordExchanger(
                                this.recordChannel, pluginCollector));
                        /**
                         * 设置taskPlugin的collector，用来处理脏数据和job/task通信
                         */
                        newRunner.setTaskPluginCollector(pluginCollector);
                        //Increase consumer
                        this.recordChannel.incConsumer();
                    }
                    break;
                default:
                    throw DataXException.asDataXException(FrameworkErrorCode.ARGUMENT_ERROR, "Cant generateRunner for:" + pluginType);
            }

            newRunner.setTaskGroupId(taskGroupId);
            newRunner.setTaskId(this.taskId);
            newRunner.setRunnerCommunication(this.taskCommunication);

            return newRunner;
        }

        /**
         *  检查任务是否结束
         */
        private boolean isTaskFinished() {
            // 如果reader 或 writer没有完成工作，那么直接返回工作没有完成
            if (readerThread.isAlive()) {
                return false;
            }
            for(Thread writerThread : writerThreads){
                if(writerThread.isAlive()){
                    return false;
                }
            }
            return taskCommunication != null && taskCommunication.isFinished();
        }

        private int getTaskId() {
            return taskId;
        }

        private long getTimeStamp() {
            return taskCommunication.getTimestamp();
        }

        private int getAttemptCount() {
            return attemptCount;
        }

        private boolean supportFailOver() {
            for(WriterRunner runner : writerRunners){
                if(!runner.supportFailOver()){
                    return false;
                }
            }
            return true;
        }

        private void adjustChannelSpeed(long byteSpeed, long dataSpeed){
            this.recordChannel.adjustRateLimit(byteSpeed, dataSpeed);
            this.streamChannel.adjustRateLimit(byteSpeed, dataSpeed);
        }
        private void shutdown() {
            writerRunners.forEach(WriterRunner::shutdown);
            readerRunner.shutdown();
            writerThreads.forEach(writerThread -> {
                if(writerThread.isAlive()){
                    writerThread.interrupt();
                }
            });
            if (readerThread.isAlive()) {
                readerThread.interrupt();
            }
        }

        private boolean isShutdown() {
            if(readerThread.isAlive()){
                return false;
            }
            for(Thread writerThread : writerThreads){
                if(writerThread.isAlive()){
                    return false;
                }
            }
            return true;
        }
    }
    @Override
    public void generateMetric() {
        LOG.info("Generate Metric");
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Communication nowTaskGroupContainerCommunication = containerCommunicator.collect();
                    nowTaskGroupContainerCommunication.setTimestamp(System.currentTimeMillis());
                    Communication reportCommunication = CommunicationTool.getReportCommunication(nowTaskGroupContainerCommunication,
                            lastTaskGroupContainerCommunication, configuration.getListConfiguration(CoreConstant.DATAX_JOB_CONTENT).size());
                    //mark the number of channel running
                    reportCommunication.setLongCounter(CommunicationTool.CHANNEL_RUNNING, runTasks.size());
                    scheduleReport = reportCommunication;
                }catch (Exception e){
                    LOG.error("generateMetric error:----"+e.getMessage());
                }
            }
        },0, 1000, TimeUnit.MILLISECONDS);
    }


    @Override
    public Communication getScheduleReport(){
        return scheduleReport;
    }
}
