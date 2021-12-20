package com.alibaba.datax.core.job;

import com.alibaba.datax.common.constant.PluginType;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.AbstractJobPlugin;
import com.alibaba.datax.common.plugin.JobPluginCollector;
import com.alibaba.datax.common.plugin.PluginProcessorLoader;
import com.alibaba.datax.common.spi.Reader;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.statistics.PerfTrace;
import com.alibaba.datax.common.statistics.VMInfo;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.common.util.StrUtil;
import com.alibaba.datax.core.AbstractContainer;
import com.alibaba.datax.core.Engine;
import com.alibaba.datax.core.container.util.HookInvoker;
import com.alibaba.datax.core.container.util.JobAssignUtil;
import com.alibaba.datax.core.job.scheduler.AbstractScheduler;
import com.alibaba.datax.core.job.scheduler.processinner.StandAloneScheduler;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.alibaba.datax.core.statistics.container.communicator.job.StandAloneJobContainerCommunicator;
import com.alibaba.datax.core.statistics.plugin.DefaultJobPluginCollector;
import com.alibaba.datax.core.util.ClassUtil;
import com.alibaba.datax.core.util.ErrorRecordChecker;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.ClassLoaderSwapper;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.alibaba.datax.core.util.container.LoadUtil;
import com.alibaba.datax.dataxservice.face.domain.enums.ExecuteMode;
import com.alibaba.fastjson.JSON;
import com.webank.wedatasphere.exchangis.datax.common.GsonUtil;
import com.webank.wedatasphere.exchangis.datax.common.constant.TransportType;
import com.webank.wedatasphere.exchangis.datax.core.job.meta.MetaSchema;
import com.webank.wedatasphere.exchangis.datax.core.processor.loader.JavaSrcUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobContainer extends AbstractContainer {
    private static final Log LOG = LogFactory.getLog(JobContainer.class);

    private ClassLoaderSwapper classLoaderSwapper = ClassLoaderSwapper
            .newCurrentThreadClassLoaderSwapper();

    private long jobId;

    private String readerPluginName;

    private String[] writerPluginNames;

    /**
     * reader和writer jobContainer的实例
     */
    private Reader.Job jobReader;

    private List<Writer.Job> jobWriters = new ArrayList<>();

    private long startTimeStamp;

    private long endTimeStamp;

    private long startTransferTimeStamp;

    private long endTransferTimeStamp;

    private int needChannelNumber;

    private int totalStage = 1;

    private ErrorRecordChecker errorLimit;

    private TransportType transportType;

    private AbstractScheduler taskGroupScheduler;
    List<Configuration> taskGroupConfigs;

    Communication lastJobContainerCommunication = new Communication();
    Communication scheduleReport;
    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    public JobContainer(Configuration configuration) {
        super(configuration);
        //Adjust configuration
        adjustJobConfiguration(this.configuration);
        this.transportType = TransportType.valueOf(
                this.configuration.getString(CoreConstant.DATAX_JOB_SETTING_TRANSPORT_TYPE, "record").toUpperCase());
        errorLimit = new ErrorRecordChecker(configuration);
    }

    /**
     * jobContainer主要负责的工作全部在start()里面，包括init、prepare、split、scheduler、
     * post以及destroy和statistics
     */
    @Override
    public void start() {
        LOG.info("DataX jobContainer starts job.");

        boolean hasException = false;
        boolean isDryRun = false;
        try {
            this.startTimeStamp = System.currentTimeMillis();
            isDryRun = configuration.getBool(CoreConstant.DATAX_JOB_SETTING_DRYRUN, false);
            if (isDryRun) {
                LOG.info("jobContainer starts to do preCheck ...");
                this.preCheck();
            } else {
//                userConf = configuration.clone();
                LOG.debug("jobContainer starts to do preHandle ...");
                this.preHandle();
                LOG.debug("jobContainer starts to do init ...");
                this.init();
                if(configuration.getBool(CoreConstant.DATAX_JOB_SETTING_SYNCMETA, false)){
                    LOG.info("jobContainer starts to do syncMetaData ...");
                    this.syncMetaData();
                }
                LOG.info("jobContainer starts to do prepare ...");
                this.prepare();
                LOG.info("jobContainer starts to do split ...");
                this.totalStage = this.split();
                generateMetric();
                if(this.totalStage > 0) {
                    LOG.info("jobContainer starts to do schedule ...");
                    this.schedule();
                }
                LOG.debug("jobContainer starts to do post ...");
                this.post();

                LOG.debug("jobContainer starts to do postHandle ...");
                this.postHandle();
                LOG.info(String.format("DataX jobId [%d] completed successfully.", this.jobId));

                this.invokeHooks();
            }
        } catch (Throwable e) {
            LOG.error("Exception when job run", e);

            hasException = true;

            if (e instanceof OutOfMemoryError) {
                try {
                    this.destroy();
                }catch(Exception e1){
                    //ignore
                }
                System.gc();
            }


            if (super.getContainerCommunicator() == null) {
                // 由于 containerCollector 是在 scheduler() 中初始化的，所以当在 scheduler() 之前出现异常时，需要在此处对 containerCollector 进行初始化

                AbstractContainerCommunicator tempContainerCollector;
                // standalone
                tempContainerCollector = new StandAloneJobContainerCommunicator(configuration);

                super.setContainerCommunicator(tempContainerCollector);
            }

            Communication communication = super.getContainerCommunicator().collect();
            // 汇报前的状态，不需要手动进行设置
            // communication.setState(State.FAILED);
            communication.setThrowable(e);
            communication.setTimestamp(this.endTimeStamp);

            Communication tempComm = new Communication();
            tempComm.setTimestamp(this.startTransferTimeStamp);

            Communication reportCommunication = CommunicationTool.getReportCommunication(communication, tempComm, this.totalStage);
            super.getContainerCommunicator().report(reportCommunication);

            throw DataXException.asDataXException(
                    FrameworkErrorCode.RUNTIME_ERROR, e);
        } finally {
            scheduledThreadPool.shutdown();
            if (!isDryRun) {

                this.destroy();
                this.endTimeStamp = System.currentTimeMillis();
                if (!hasException) {
                    //最后打印cpu的平均消耗，GC的统计
                    VMInfo vmInfo = VMInfo.getVmInfo();
                    if (vmInfo != null) {
                        vmInfo.getDelta(false);
                        LOG.info(vmInfo.totalString());
                    }

                    LOG.info(PerfTrace.getInstance().summarizeNoException());
                    this.logStatistics();
                }
            }
        }
    }

    @Override
    public void shutdown() {
        LOG.info("shutdown job container and clean the dirty data");
        if(null != taskGroupScheduler){
            taskGroupScheduler.stopSchedule();
        }
        LOG.info("invoke destroy method");
        this.destroy();

    }

    private void preCheck() {
        this.preCheckInit();
        this.adjustChannelNumber();

        if (this.needChannelNumber <= 0) {
            this.needChannelNumber = 1;
        }
        this.preCheckReader();
        this.preCheckWriter();
        LOG.info("PreCheck通过");
    }

    private void preCheckInit() {
        this.jobId = this.configuration.getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID, -1);

        if (this.jobId < 0) {
            LOG.info("Set jobId = 0");
            this.jobId = 0;
            this.configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID,
                    this.jobId);
        }

        Thread.currentThread().setName("job-" + this.jobId);

        JobPluginCollector jobPluginCollector = new DefaultJobPluginCollector(
                this.getContainerCommunicator());
        this.jobReader = this.preCheckReaderInit(jobPluginCollector);
        this.jobWriters = this.preCheckWriterInit(jobPluginCollector);
    }

    private Reader.Job preCheckReaderInit(JobPluginCollector jobPluginCollector) {
        this.readerPluginName = this.configuration.getString(
                CoreConstant.DATAX_JOB_CONTENT_READER_NAME);
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                PluginType.READER, this.readerPluginName));

        Reader.Job jobReader = (Reader.Job) LoadUtil.loadJobPlugin(
                PluginType.READER, this.readerPluginName);
        // Set reader parameters in configuration
        jobReader.setPluginJobConf(this.configuration.getConfiguration(
                CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER));
        //Set writer parameters in configuration
        List<Configuration> writerConfList = getJobWriterConfigList(this.configuration);
        writerConfList.forEach(jobReader::addPeerPluginJobConf);
        jobReader.setJobPluginCollector(jobPluginCollector);
        classLoaderSwapper.restoreCurrentThreadClassLoader();
        return jobReader;
    }


    private List<Writer.Job> preCheckWriterInit(JobPluginCollector jobPluginCollector) {
        List<Configuration> writerConfList = getJobWriterConfigList(this.configuration);
        List<Writer.Job> writerJobs = new ArrayList<>();
        this.writerPluginNames = new String[writerConfList.size()];
        for(int i = 0; i < writerConfList.size(); i ++){
            Configuration writerConf = writerConfList.get(i);
            this.writerPluginNames[i] = this.configuration.getString(
                    String.format(CoreConstant.DATAX_JOB_CONTENT_WRITER_ARRAY_NAME, i));
            classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                    PluginType.WRITER, this.writerPluginNames[i]
            ));
            Writer.Job jobWriter = (Writer.Job) LoadUtil.loadJobPlugin(
                    PluginType.WRITER, this.writerPluginNames[i]);
            //Set writer parameter in configuration
            jobWriter.setPluginJobConf(writerConf);
            //Set reader parameter in configuration
            jobWriter.addPeerPluginJobConf(this.configuration.getConfiguration(
                    CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER));
            //Set peer plugin name
            jobWriter.addPeerPluginName(this.readerPluginName);
            jobWriter.setJobPluginCollector(jobPluginCollector);
            writerJobs.add(jobWriter);
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        }
        //Remain JVM to collect garbage?
        writerConfList.clear();
        return writerJobs;
    }

    private void preCheckReader() {
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                PluginType.READER, this.readerPluginName));
        LOG.info(String.format("DataX Reader.Job [%s] do preCheck work .",
                this.readerPluginName));
        this.jobReader.preCheck();
        classLoaderSwapper.restoreCurrentThreadClassLoader();
    }

    private void preCheckWriter() {
        LOG.info("DataX Writer.Jobs [" + StringUtils.join(this.writerPluginNames, ",") + "] do preCheck work");
        this.jobWriters.forEach( jobWriter -> {
            classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                    PluginType.WRITER, jobWriter.getPluginName()));
            LOG.info("Writer.Job:[" + jobWriter.getPluginName() + "] start to pre check");
            jobWriter.preCheck();
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        });
    }

    /**
     * reader和writer的初始化
     */
    private void init() {
        this.jobId = this.configuration.getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID, -1);

        if (this.jobId < 0) {
            LOG.info("Set jobId = 0");
            this.jobId = 0;
            this.configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID,
                    this.jobId);
        }

        Thread.currentThread().setName("job-" + this.jobId);

        JobPluginCollector jobPluginCollector = new DefaultJobPluginCollector(
                this.getContainerCommunicator());
        //add shutdown hook to jvm in order to clear dirty data
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        //必须先Reader ，后Writer
        this.jobReader = this.initJobReader(jobPluginCollector);
        this.jobWriters = this.initJobWriter(jobPluginCollector);
    }

    /**
     * Sync metadata
     * Added by davidhua@webankcom
     */
    private void syncMetaData(){
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil
                .getJarLoader(PluginType.READER, this.readerPluginName));
        LOG.info(String.format("Reader.Job [%s] do get meta schema", this.readerPluginName));
        MetaSchema metaSchema = this.jobReader.syncMetaData();
        LOG.info(String.format("Meta schema: [%s]", GsonUtil.toJson(metaSchema)));
        classLoaderSwapper.restoreCurrentThreadClassLoader();
        if(null != metaSchema) {
            this.jobWriters.forEach(jobWriter -> {
                classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil
                        .getJarLoader(PluginType.WRITER, jobWriter.getPluginName()));
                LOG.info(String.format("Writer.Job [%s] do sync meta schema", jobWriter.getPluginName()));
                jobWriter.syncMetaData(metaSchema);
                classLoaderSwapper.restoreCurrentThreadClassLoader();
            });
        }

    }
    private void prepare() {
        this.prepareJobReader();
        this.prepareJobWriter();
    }

    private void preHandle() {
        String handlerPluginTypeStr = this.configuration.getString(
                CoreConstant.DATAX_JOB_PREHANDLER_PLUGINTYPE);
        if (!StringUtils.isNotEmpty(handlerPluginTypeStr)) {
            return;
        }
        PluginType handlerPluginType;
        try {
            handlerPluginType = PluginType.valueOf(handlerPluginTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw DataXException.asDataXException(
                    FrameworkErrorCode.CONFIG_ERROR,
                    String.format("Job preHandler's pluginType(%s) set error, reason(%s)", handlerPluginTypeStr.toUpperCase(), e.getMessage()));
        }

        String handlerPluginName = this.configuration.getString(
                CoreConstant.DATAX_JOB_PREHANDLER_PLUGINNAME);

        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                handlerPluginType, handlerPluginName));

        AbstractJobPlugin handler = LoadUtil.loadJobPlugin(
                handlerPluginType, handlerPluginName);

        JobPluginCollector jobPluginCollector = new DefaultJobPluginCollector(
                this.getContainerCommunicator());
        handler.setJobPluginCollector(jobPluginCollector);

        //todo configuration的安全性，将来必须保证
        handler.preHandler(configuration);
        classLoaderSwapper.restoreCurrentThreadClassLoader();

        LOG.info("After PreHandler: \n" + Engine.filterJobConfiguration(configuration) + "\n");
    }

    private void postHandle() {
        String handlerPluginTypeStr = this.configuration.getString(
                CoreConstant.DATAX_JOB_POSTHANDLER_PLUGINTYPE);

        if (!StringUtils.isNotEmpty(handlerPluginTypeStr)) {
            return;
        }
        PluginType handlerPluginType;
        try {
            handlerPluginType = PluginType.valueOf(handlerPluginTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw DataXException.asDataXException(
                    FrameworkErrorCode.CONFIG_ERROR,
                    String.format("Job postHandler's pluginType(%s) set error, reason(%s)", handlerPluginTypeStr.toUpperCase(), e.getMessage()));
        }

        String handlerPluginName = this.configuration.getString(
                CoreConstant.DATAX_JOB_POSTHANDLER_PLUGINNAME);

        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                handlerPluginType, handlerPluginName));

        AbstractJobPlugin handler = LoadUtil.loadJobPlugin(
                handlerPluginType, handlerPluginName);

        JobPluginCollector jobPluginCollector = new DefaultJobPluginCollector(
                this.getContainerCommunicator());
        handler.setJobPluginCollector(jobPluginCollector);

        handler.postHandler(configuration);
        classLoaderSwapper.restoreCurrentThreadClassLoader();
    }


    /**
     * 执行reader和writer最细粒度的切分，需要注意的是，writer的切分结果要参照reader的切分结果，
     * 达到切分后数目相等，才能满足1：1的通道模型，所以这里可以将reader和writer的配置整合到一起，
     * 然后，为避免顺序给读写端带来长尾影响，将整合的结果shuffler掉
     */
    private int split() {
        this.adjustChannelNumber();

        if (this.needChannelNumber <= 0) {
            this.needChannelNumber = 1;
        }

        List<Configuration> readerTaskConfigs = this
                .doReaderSplit(this.needChannelNumber);
        if(readerTaskConfigs.isEmpty()){
            return 0;
        }
        int taskNumber = readerTaskConfigs.size();
        List<Configuration> writerTaskConfigs = this
                .doWriterSplit(taskNumber);
        //adjust the speed limitation of channel
        if(taskNumber <= needChannelNumber){
            needChannelNumber = taskNumber;
            adjustChannelSpeedByNumber(needChannelNumber);
        }
        LOG.info("Job final Channel-Number: [" + needChannelNumber + "]");
        //change the channel speed when channel speed * taskNumber
        List<Configuration> transformerList = this.configuration.getListConfiguration(CoreConstant.DATAX_JOB_CONTENT_TRANSFORMER);

        LOG.debug("transformer configuration: " + JSON.toJSONString(transformerList));
        //input:  reader parameter list and writer task list(contain properties: parameter, name and processor)
        //output: "content" array
        List<Configuration> contentConfig = mergeReaderAndWriterTaskConfigs(
                readerTaskConfigs, writerTaskConfigs, transformerList);


        LOG.debug("contentConfig configuration: " + JSON.toJSONString(contentConfig));

        this.configuration.set(CoreConstant.DATAX_JOB_CONTENT, contentConfig);

        return contentConfig.size();
    }

    private void adjustChannelNumber() {
        int needChannelNumberByByte = Integer.MAX_VALUE;
        int needChannelNumberByRecord = Integer.MAX_VALUE;

        boolean isByteLimit = (this.configuration.getLong(
                CoreConstant.DATAX_JOB_SETTING_SPEED_BYTE, 0) > 0);
        if (isByteLimit) {
            long globalLimitedByteSpeed = this.configuration.getLong(
                    CoreConstant.DATAX_JOB_SETTING_SPEED_BYTE, 10 * 1024L * 1024L);

            // 在byte流控情况下，单个Channel流量最大值必须设置，否则报错！
            Long channelLimitedByteSpeed = this.configuration
                    .getLong(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE);
            if (channelLimitedByteSpeed == null || channelLimitedByteSpeed <= 0) {
                DataXException.asDataXException(
                        FrameworkErrorCode.CONFIG_ERROR,
                        "在有总bps限速条件下，单个channel的bps值不能为空，也不能为非正数");
            }

            needChannelNumberByByte =
                    (int) (globalLimitedByteSpeed / channelLimitedByteSpeed);
            if(needChannelNumberByByte <= 0){
                needChannelNumberByByte = 1;
                //globalLimitedByteSpeed < channelLimitedByteSpeed
                this.configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE, globalLimitedByteSpeed);
            }
            LOG.info("Job set Max-Byte-Speed to " + globalLimitedByteSpeed + " bytes.");
        }

        boolean isRecordLimit = (this.configuration.getLong(
                CoreConstant.DATAX_JOB_SETTING_SPEED_RECORD, 0)) > 0;
        if (isRecordLimit) {
            long globalLimitedRecordSpeed = this.configuration.getLong(
                    CoreConstant.DATAX_JOB_SETTING_SPEED_RECORD, 100000);

            Long channelLimitedRecordSpeed = this.configuration.getLong(
                    CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD);
            if (channelLimitedRecordSpeed == null || channelLimitedRecordSpeed <= 0) {
                DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR,
                        "在有总tps限速条件下，单个channel的tps值不能为空，也不能为非正数");
            }

            needChannelNumberByRecord =
                    (int) (globalLimitedRecordSpeed / channelLimitedRecordSpeed);
            if(needChannelNumberByRecord <= 0){
                needChannelNumberByRecord = 1;
                //globalLimitedRecordSpeed < channelLimitedRecordSpeed
                this.configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD,  globalLimitedRecordSpeed);
            }
            LOG.info("Job set Max-Record-Speed to " + globalLimitedRecordSpeed + " records.");
        }

        // 取较小值
        this.needChannelNumber = needChannelNumberByByte < needChannelNumberByRecord ?
                needChannelNumberByByte : needChannelNumberByRecord;
        boolean isChannelLimit = (this.configuration.getLong(
                CoreConstant.DATAX_JOB_SETTING_SPEED_CHANNEL, 0) > 0);
        if (isChannelLimit) {
            //limit the max number of channel
            int maxChannelNumber = this.configuration.getInt(
                    CoreConstant.DATAX_JOB_SETTING_SPEED_CHANNEL);
            if(this.needChannelNumber > maxChannelNumber){
                this.needChannelNumber = maxChannelNumber;
            }
        }
        if(needChannelNumber >= Integer.MAX_VALUE) {
            throw DataXException.asDataXException(
                    FrameworkErrorCode.CONFIG_ERROR,
                    "Job运行速度必须设置");
        }
        adjustChannelSpeedByNumber(needChannelNumber);
    }

    /**
     * schedule首先完成的工作是把上一步reader和writer split的结果整合到具体taskGroupContainer中,
     * 同时不同的执行模式调用不同的调度策略，将所有任务调度起来
     */
    private void schedule() {
        /**
         * 这里的全局speed和每个channel的速度设置为B/s
         */
        int channelsPerTaskGroup = this.configuration.getInt(
                CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL, 5);
        int taskNumber = this.configuration.getList(
                CoreConstant.DATAX_JOB_CONTENT).size();

        this.needChannelNumber = Math.min(this.needChannelNumber, taskNumber);
        PerfTrace.getInstance().setChannelNumber(needChannelNumber);

        /**
         * 通过获取配置信息得到每个taskGroup需要运行哪些tasks任务
         */

        taskGroupConfigs = JobAssignUtil.assignFairly(this.configuration,
                this.needChannelNumber, channelsPerTaskGroup);

        LOG.info(String.format("Scheduler starts [%d] taskGroups.", taskGroupConfigs.size()));

        ExecuteMode executeMode = null;
        AbstractScheduler scheduler;
        try {
            executeMode = ExecuteMode.STANDALONE;
            scheduler = initStandaloneScheduler(this.configuration);
            //设置 executeMode 和 transportType
            for (Configuration taskGroupConfig : taskGroupConfigs) {
                taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_MODE, executeMode.getValue());
                taskGroupConfig.set(CoreConstant.DATAX_CORE_TRANSPORT_TYPE, this.configuration.getString(CoreConstant.DATAX_JOB_SETTING_TRANSPORT_TYPE, "record"));
            }

            LOG.info(String.format("Running by %s Mode.", executeMode));

            this.startTransferTimeStamp = System.currentTimeMillis();
            this.taskGroupScheduler = scheduler;
            scheduler.schedule(taskGroupConfigs);

            this.endTransferTimeStamp = System.currentTimeMillis();
        } catch (Exception e) {
            LOG.error(String.format("运行scheduler 模式[%s]出错.", executeMode));
            this.endTransferTimeStamp = System.currentTimeMillis();
            throw DataXException.asDataXException(
                    FrameworkErrorCode.RUNTIME_ERROR, e);
        }

        /**
         * 检查任务执行情况
         */
        this.checkLimit();
    }


    private AbstractScheduler initStandaloneScheduler(Configuration configuration) {
        AbstractContainerCommunicator containerCommunicator = new StandAloneJobContainerCommunicator(configuration);
        super.setContainerCommunicator(containerCommunicator);

        return new StandAloneScheduler(containerCommunicator);
    }

    private void post() {
        this.postJobWriter();
        this.postJobReader();
    }

    private void destroy() {
        if (!this.jobWriters.isEmpty()) {
            jobWriters.removeIf(jobWriter ->{
                classLoaderSwapper.setCurrentThreadClassLoader(
                        LoadUtil.getJarLoader(PluginType.WRITER, jobWriter.getPluginName())
                );
                jobWriter.destroy();
                classLoaderSwapper.restoreCurrentThreadClassLoader();
                return true;
            });
        }
        if (this.jobReader != null) {
            classLoaderSwapper.setCurrentThreadClassLoader(
                    LoadUtil.getJarLoader(PluginType.READER, this.readerPluginName)
            );
            this.jobReader.destroy();
            this.jobReader = null;
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        }
    }

    private void logStatistics() {
        long totalCosts = (this.endTimeStamp - this.startTimeStamp) / 1000;
        long transferCosts = (this.endTransferTimeStamp - this.startTransferTimeStamp) / 1000;
        if (0L == transferCosts) {
            transferCosts = 1L;
        }

        if (super.getContainerCommunicator() == null) {
            return;
        }
        Communication communication = super.getContainerCommunicator().collect();
        communication.setTimestamp(this.endTimeStamp);

        Communication tempComm = new Communication();
        tempComm.setTimestamp(this.startTransferTimeStamp);

        Communication reportCommunication = CommunicationTool.getReportCommunication(communication, tempComm, this.totalStage);

        // 字节速率
        long byteSpeedPerSecond = communication.getLongCounter(CommunicationTool.READ_SUCCEED_BYTES)
                / transferCosts;

        long recordSpeedPerSecond = communication.getLongCounter(CommunicationTool.READ_SUCCEED_RECORDS)
                / transferCosts;

        reportCommunication.setLongCounter(CommunicationTool.BYTE_SPEED, byteSpeedPerSecond);
        reportCommunication.setLongCounter(CommunicationTool.RECORD_SPEED, recordSpeedPerSecond);

        super.getContainerCommunicator().report(reportCommunication);

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        String msg = String.format(
                "\n" + "%-26s: %-18s\n" + "%-26s: %-18s\n" + "%-26s: %19s\n"
                        + "%-26s: %19s\n" + "%-26s: %19s\n" + "%-26s: %19s\n"
                        + "%-26s: %19s\n",
                "任务启动时刻",
                dateFormat.format(startTimeStamp),

                "任务结束时刻",
                dateFormat.format(endTimeStamp),

                "任务总计耗时",
                String.valueOf(totalCosts) + "s",
                "任务平均流量",
                StrUtil.stringify(byteSpeedPerSecond)
                        + "/s",
                "记录写入速度",
                String.valueOf(recordSpeedPerSecond)
                        + "rec/s",
                transportType == TransportType.RECORD?"读出记录总数":"读出数据总数",
                transportType == TransportType.RECORD?
                        String.valueOf(CommunicationTool.getTotalReadRecords(communication)) :
                        String.valueOf(CommunicationTool.getTotalReadBytes(communication)) +"(Bytes)",
                "读写失败总数",
                transportType == TransportType.RECORD?
                        String.valueOf(CommunicationTool.getTotalErrorRecords(communication)):""
        );
        LOG.info(msg);

        if (communication.getLongCounter(CommunicationTool.TRANSFORMER_SUCCEED_RECORDS) > 0
                || communication.getLongCounter(CommunicationTool.TRANSFORMER_FAILED_RECORDS) > 0
                || communication.getLongCounter(CommunicationTool.TRANSFORMER_FILTER_RECORDS) > 0) {
            String tmsg = String.format(
                    "\n" + "%-26s: %19s\n" + "%-26s: %19s\n" + "%-26s: %19s\n",
                    "Transformer成功记录总数",
                    communication.getLongCounter(CommunicationTool.TRANSFORMER_SUCCEED_RECORDS),

                    "Transformer失败记录总数",
                    communication.getLongCounter(CommunicationTool.TRANSFORMER_FAILED_RECORDS),

                    "Transformer过滤记录总数",
                    communication.getLongCounter(CommunicationTool.TRANSFORMER_FILTER_RECORDS)
            );
            LOG.info(tmsg);
        }

     /*   //report to server
        try {
            HttpClientUtil httpClientUtil = HttpClientUtil.getHttpClientUtil();
            Map<String,Object> report =new HashMap<>(10);
            report.put("id", configuration.getLong(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID));
            report.put("byteSpeedPerSecond",byteSpeedPerSecond);
            report.put("recordSpeedPerSecond",recordSpeedPerSecond);
            report.put("totalCosts",totalCosts);
            report.put("totalErrorRecords",CommunicationTool.getTotalErrorRecords(communication));
            report.put("totalReadRecords",CommunicationTool.getTotalReadRecords(communication));
            report.put("totalReadBytes", CommunicationTool.getTotalReadBytes(communication));
            report.put("transformerFailedRecords",communication.getLongCounter(CommunicationTool.TRANSFORMER_FAILED_RECORDS));
            report.put("transformerFilterRecords",communication.getLongCounter(CommunicationTool.TRANSFORMER_FILTER_RECORDS));
            report.put("transformerTotalRecords",communication.getLongCounter(CommunicationTool.TRANSFORMER_SUCCEED_RECORDS));
            StringEntity entity = new StringEntity(JSON.toJSONString(report));
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            HttpPost post = HttpClientUtil.getPostRequest(configuration.getString(CoreConstant.DATAX_CORE_DATAXSERVER_PROTOCOL)
                            + "://" + configuration.getString(CoreConstant.DATAX_CORE_DATAXSERVER_ADDRESS)
                            + configuration.getString(CoreConstant.DATAX_CORE_DATAXSERVER_ENDPOINT_REPORT),
                    entity,
                    "Content-Type", "application/json;charset=UTF-8");
            String response = httpClientUtil.executeAndGet(post, String.class);
            LOG.info(String.format("Send report respone,%s",response));
        }catch (Exception e){
            LOG.error("Post report error",e);
        }*/

    }

    /**
     * reader job的初始化，返回Reader.Job
     *
     * @return
     */
    private Reader.Job initJobReader(
            JobPluginCollector jobPluginCollector) {
        this.readerPluginName = this.configuration.getString(
                CoreConstant.DATAX_JOB_CONTENT_READER_NAME);
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                PluginType.READER, this.readerPluginName));

        Reader.Job jobReader = (Reader.Job) LoadUtil.loadJobPlugin(
                PluginType.READER, this.readerPluginName);
        if(this.transportType == TransportType.STREAM && !jobReader.isSupportStream()){
            throw DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR,
                    this.readerPluginName + " don't support transport type STREAM");
        }
        jobReader.setTransportType(this.transportType);
        // Set reader parameters in configuration
        jobReader.setPluginJobConf(this.configuration.getConfiguration(
                CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER));
        //Set writer parameters in configuration
        List<Configuration> writerConfList = getJobWriterConfigList(this.configuration);
        writerConfList.forEach(jobReader::addPeerPluginJobConf);
        jobReader.setJobPluginCollector(jobPluginCollector);
        jobReader.init();
        classLoaderSwapper.restoreCurrentThreadClassLoader();
        return jobReader;
    }

    /**
     * writer job的初始化，返回Writer.Job
     *
     * @return
     */
    private List<Writer.Job> initJobWriter(
            JobPluginCollector jobPluginCollector) {
        List<Configuration> writerConfList = getJobWriterConfigList(this.configuration);
        List<Writer.Job> writerJobs = new ArrayList<>();
        this.writerPluginNames = new String[writerConfList.size()];
        for(int i = 0; i < writerConfList.size(); i++){
            Configuration writerConf = writerConfList.get(i);
            this.writerPluginNames[i] = this.configuration.getString(
                    String.format(CoreConstant.DATAX_JOB_CONTENT_WRITER_ARRAY_NAME, i));
            classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                    PluginType.WRITER, this.writerPluginNames[i]
            ));
            //Load processor before the writer plugin initialization
            //TODO Use plugin name as namespace
            List<String> processors = doLoadProcessor("");
            Writer.Job jobWriter = (Writer.Job) LoadUtil.loadJobPlugin(
                    PluginType.WRITER, this.writerPluginNames[i]);
            if(this.transportType == TransportType.STREAM && !jobWriter.isSupportStream()){
                throw DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR,
                        this.writerPluginNames[i] + " don't support transport type STREAM");
            }
            jobWriter.setTransportType(transportType);
            //Set writer parameter in configuration
            jobWriter.setPluginJobConf(writerConf);
            //Set reader parameter in configuration
            jobWriter.addPeerPluginJobConf(this.configuration.getConfiguration(
                    CoreConstant.DATAX_JOB_CONTENT_READER_PARAMETER));
            //Set peer plugin name
            jobWriter.addPeerPluginName(this.readerPluginName);
            jobWriter.setJobPluginCollector(jobPluginCollector);
            if(!processors.isEmpty()){
                //Choose the first one
                jobWriter.setProcessor(processors.get(0));
            }
            jobWriter.init();
            writerJobs.add(jobWriter);
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        }
        //Remain JVM to collect garbage?
        writerConfList.clear();
        return writerJobs;
    }

    private void prepareJobReader() {
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                PluginType.READER, this.readerPluginName));
        LOG.info(String.format("DataX Reader.Job [%s] do prepare work .",
                this.readerPluginName));
        this.jobReader.prepare();
        classLoaderSwapper.restoreCurrentThreadClassLoader();
    }

    private void prepareJobWriter() {
        this.jobWriters.forEach(jobWriter -> {
            classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil
                    .getJarLoader(PluginType.WRITER, jobWriter.getPluginName()));
            LOG.info(String.format("DataX Writer.Job [%s] do prepare work .",
                    jobWriter.getPluginName()));
            jobWriter.prepare();
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        });
    }

    private List<Configuration> doReaderSplit(int adviceNumber) {
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                PluginType.READER, this.readerPluginName));
        List<Configuration> readerTaskConfigs = new ArrayList<>();
        List<Configuration> readerSlicesConfigs =
                this.jobReader.split(adviceNumber);
        LOG.info(String.format("DataX Reader.Job [{%s}] splits to [%d] tasks.",
                this.readerPluginName, readerSlicesConfigs.size()));
        //Wrap as task configuration
        readerSlicesConfigs.forEach( readerSlices -> {
            Configuration readerTaskConfig = Configuration.newDefault();
            readerTaskConfig.set(CoreConstant.TASK_NAME, this.readerPluginName);
            readerTaskConfig.set(CoreConstant.TASK_PARAMETER, readerSlices);
            readerTaskConfigs.add(readerTaskConfig);
        });
        classLoaderSwapper.restoreCurrentThreadClassLoader();
        return readerTaskConfigs;
    }

    private List<Configuration> doWriterSplit(int readerTaskNumber) {
        List<Configuration> writerTaskConfigs = new ArrayList<>();
        int[] counter = new int[readerTaskNumber];
        this.jobWriters.forEach(jobWriter ->{
            classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                    PluginType.WRITER, jobWriter.getPluginName()));
            List<Configuration> writerSlicesConfigs = jobWriter.split(readerTaskNumber);
            if (writerSlicesConfigs == null || writerSlicesConfigs.size() <= 0) {
                throw DataXException.asDataXException(
                        FrameworkErrorCode.PLUGIN_SPLIT_ERROR,
                        "writer切分的task不能小于等于0");
            }
            if (writerSlicesConfigs.size() != readerTaskNumber) {
                throw DataXException.asDataXException(
                        FrameworkErrorCode.PLUGIN_SPLIT_ERROR,
                        String.format("reader切分的task数目[%d]不等于writer切分的task数目[%d].",
                                readerTaskNumber, writerSlicesConfigs.size())
                );
            }
            for(int i = 0 ; i < writerSlicesConfigs.size(); i++){
                if(i >= writerTaskConfigs.size()){
                    writerTaskConfigs.add(Configuration.from("[]"));
                }
                Configuration writerTaskConfig = writerTaskConfigs.get(i);
                Configuration writerSlicesConfig = writerSlicesConfigs.get(i);
                Configuration taskConfigElement = Configuration.newDefault();
                //Build writer task configuration
                taskConfigElement.set(CoreConstant.TASK_NAME, jobWriter.getPluginName());
                taskConfigElement.set(CoreConstant.TASK_PARAMETER, writerSlicesConfig);
                taskConfigElement.set(CoreConstant.TASK_PROCESSOR, jobWriter.getProcessors());
                writerTaskConfig.set("[" + counter[i] + "]", taskConfigElement);
                counter[i] ++;
            }
            LOG.info(String.format("DataX Writer.Job [{%s}] splits to [%d] tasks.",
                    jobWriter.getPluginName(), writerSlicesConfigs.size()));
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        });
        return writerTaskConfigs;
    }

    /**
     * 按顺序整合reader和writer的配置，这里的顺序不能乱！ 输入是reader、writer级别的配置，输出是一个完整task的配置
     *
     * @param readerTasksConfigs
     * @param writerTasksConfigs
     * @return
     */
    private List<Configuration> mergeReaderAndWriterTaskConfigs(
            List<Configuration> readerTasksConfigs,
            List<Configuration> writerTasksConfigs) {
        return mergeReaderAndWriterTaskConfigs(readerTasksConfigs, writerTasksConfigs, null);
    }

    private List<Configuration> mergeReaderAndWriterTaskConfigs(
            List<Configuration> readerTasksConfigs,
            List<Configuration> writerTasksConfigs,
            List<Configuration> transformerConfigs) {


        List<Configuration> contentConfigs = new ArrayList<>();
        for (int i = 0; i < readerTasksConfigs.size(); i++) {
            Configuration taskConfig = Configuration.newDefault();
            taskConfig.set(CoreConstant.JOB_READER, readerTasksConfigs.get(i));
            taskConfig.set(CoreConstant.JOB_WRITER, writerTasksConfigs.get(i));
            if (transformerConfigs != null && transformerConfigs.size() > 0) {
                taskConfig.set(CoreConstant.JOB_TRANSFORMER, transformerConfigs);
            }

            taskConfig.set(CoreConstant.TASK_ID, i);
            contentConfigs.add(taskConfig);
        }

        return contentConfigs;
    }

    /**
     * 这里比较复杂，分两步整合 1. tasks到channel 2. channel到taskGroup
     * 合起来考虑，其实就是把tasks整合到taskGroup中，需要满足计算出的channel数，同时不能多起channel
     * <p/>
     * example:
     * <p/>
     * 前提条件： 切分后是1024个分表，假设用户要求总速率是1000M/s，每个channel的速率的3M/s，
     * 每个taskGroup负责运行7个channel
     * <p/>
     * 计算： 总channel数为：1000M/s / 3M/s =
     * 333个，为平均分配，计算可知有308个每个channel有3个tasks，而有25个每个channel有4个tasks，
     * 需要的taskGroup数为：333 / 7 =
     * 47...4，也就是需要48个taskGroup，47个是每个负责7个channel，有4个负责1个channel
     * <p/>
     * 处理：我们先将这负责4个channel的taskGroup处理掉，逻辑是：
     * 先按平均为3个tasks找4个channel，设置taskGroupId为0，
     * 接下来就像发牌一样轮询分配task到剩下的包含平均channel数的taskGroup中
     * <p/>
     * TODO delete it
     *
     * @param averTaskPerChannel
     * @param channelNumber
     * @param channelsPerTaskGroup
     * @return 每个taskGroup独立的全部配置
     */
    @SuppressWarnings("serial")
    private List<Configuration> distributeTasksToTaskGroup(
            int averTaskPerChannel, int channelNumber,
            int channelsPerTaskGroup) {
        Validate.isTrue(averTaskPerChannel > 0 && channelNumber > 0
                        && channelsPerTaskGroup > 0,
                "每个channel的平均task数[averTaskPerChannel]，channel数目[channelNumber]，每个taskGroup的平均channel数[channelsPerTaskGroup]都应该为正数");
        List<Configuration> taskConfigs = this.configuration
                .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT);
        int taskGroupNumber = channelNumber / channelsPerTaskGroup;
        int leftChannelNumber = channelNumber % channelsPerTaskGroup;
        if (leftChannelNumber > 0) {
            taskGroupNumber += 1;
        }

        /**
         * 如果只有一个taskGroup，直接打标返回
         */
        if (taskGroupNumber == 1) {
            final Configuration taskGroupConfig = this.configuration.clone();
            /**
             * configure的clone不能clone出
             */
            taskGroupConfig.set(CoreConstant.DATAX_JOB_CONTENT, this.configuration
                    .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT));
            taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL,
                    channelNumber);
            taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID, 0);
            List<Configuration> result = new ArrayList<>();
            result.add(taskGroupConfig);
            return result;
        }

        List<Configuration> taskGroupConfigs = new ArrayList<Configuration>();
        /**
         * 将每个taskGroup中content的配置清空
         */
        for (int i = 0; i < taskGroupNumber; i++) {
            Configuration taskGroupConfig = this.configuration.clone();
            List<Configuration> taskGroupJobContent = taskGroupConfig
                    .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT);
            taskGroupJobContent.clear();
            taskGroupConfig.set(CoreConstant.DATAX_JOB_CONTENT, taskGroupJobContent);

            taskGroupConfigs.add(taskGroupConfig);
        }

        int taskConfigIndex = 0;
        int channelIndex = 0;
        int taskGroupConfigIndex = 0;

        /**
         * 先处理掉taskGroup包含channel数不是平均值的taskGroup
         */
        if (leftChannelNumber > 0) {
            Configuration taskGroupConfig = taskGroupConfigs.get(taskGroupConfigIndex);
            for (; channelIndex < leftChannelNumber; channelIndex++) {
                for (int i = 0; i < averTaskPerChannel; i++) {
                    List<Configuration> taskGroupJobContent = taskGroupConfig
                            .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT);
                    taskGroupJobContent.add(taskConfigs.get(taskConfigIndex++));
                    taskGroupConfig.set(CoreConstant.DATAX_JOB_CONTENT,
                            taskGroupJobContent);
                }
            }

            taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL,
                    leftChannelNumber);
            taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID,
                    taskGroupConfigIndex++);
        }

        /**
         * 下面需要轮询分配，并打上channel数和taskGroupId标记
         */
        int equalDivisionStartIndex = taskGroupConfigIndex;
        for (; taskConfigIndex < taskConfigs.size()
                && equalDivisionStartIndex < taskGroupConfigs.size(); ) {
            for (taskGroupConfigIndex = equalDivisionStartIndex; taskGroupConfigIndex < taskGroupConfigs
                    .size() && taskConfigIndex < taskConfigs.size(); taskGroupConfigIndex++) {
                Configuration taskGroupConfig = taskGroupConfigs.get(taskGroupConfigIndex);
                List<Configuration> taskGroupJobContent = taskGroupConfig
                        .getListConfiguration(CoreConstant.DATAX_JOB_CONTENT);
                taskGroupJobContent.add(taskConfigs.get(taskConfigIndex++));
                taskGroupConfig.set(
                        CoreConstant.DATAX_JOB_CONTENT, taskGroupJobContent);
            }
        }

        for (taskGroupConfigIndex = equalDivisionStartIndex;
             taskGroupConfigIndex < taskGroupConfigs.size(); ) {
            Configuration taskGroupConfig = taskGroupConfigs.get(taskGroupConfigIndex);
            taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL,
                    channelsPerTaskGroup);
            taskGroupConfig.set(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID,
                    taskGroupConfigIndex++);
        }

        return taskGroupConfigs;
    }

    private void postJobReader() {
        classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                PluginType.READER, this.readerPluginName));
        LOG.info(String.format("DataX Reader.Job [%s] do post work.",
                this.readerPluginName));
        this.jobReader.post();
        classLoaderSwapper.restoreCurrentThreadClassLoader();
    }

    private void postJobWriter() {
        this.jobWriters.forEach( jobWriter -> {
            classLoaderSwapper.setCurrentThreadClassLoader(LoadUtil.getJarLoader(
                    PluginType.WRITER, jobWriter.getPluginName()));
            LOG.info(String.format("DataX Writer.Job [%s] do post work.",
                    jobWriter.getPluginName()));
            jobWriter.post();
            classLoaderSwapper.restoreCurrentThreadClassLoader();
        });
    }

    /**
     * 检查最终结果是否超出阈值，如果阈值设定小于1，则表示百分数阈值，大于1表示条数阈值。
     *
     * @param
     */
    private void checkLimit() {
        Communication communication = super.getContainerCommunicator().collect();
        errorLimit.checkRecordLimit(communication);
        errorLimit.checkPercentageLimit(communication);
    }

    /**
     * 调用外部hook
     */
    private void invokeHooks() {
        AbstractContainerCommunicator tempContainerCollector = super.getContainerCommunicator();
        if (tempContainerCollector == null) {
            tempContainerCollector = new StandAloneJobContainerCommunicator(configuration);
            super.setContainerCommunicator(tempContainerCollector);
        }
        Communication comm = tempContainerCollector.collect();
        HookInvoker invoker = new HookInvoker(CoreConstant.DATAX_HOME + "/hook", configuration, comm.getCounter());
        invoker.invokeAll();
    }

    /**
     * Adjust channel speed by channel number
     * Added by davidhua@webank.com
     * @param channelNumber channel number
     */
    private void adjustChannelSpeedByNumber(int channelNumber){
        long globalLimitedByteSpeed = this.configuration.getLong(
                CoreConstant.DATAX_JOB_SETTING_SPEED_BYTE, 0);
        if(globalLimitedByteSpeed > 0){
            Long channelLimitedByteSpeed = this.configuration
                    .getLong(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE);
            if(channelLimitedByteSpeed * channelNumber < globalLimitedByteSpeed){
                this.configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_BYTE,
                        globalLimitedByteSpeed / channelNumber);
            }
        }
        long globalLimitedRecordSpeed = this.configuration.getLong(
                CoreConstant.DATAX_JOB_SETTING_SPEED_RECORD, 100000);
        if(globalLimitedRecordSpeed > 0){
            Long channelLimitedRecordSpeed = this.configuration.getLong(
                    CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD);
            if(channelLimitedRecordSpeed * channelNumber < globalLimitedRecordSpeed){
                this.configuration.set(CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_SPEED_RECORD,
                        globalLimitedRecordSpeed / channelNumber);
            }
        }
    }

    /**
     * Get Job.Writer configuration list
     * Added by davidhua@webank.com
     * @param configuration root configuration
     * @return list
     */
    private List<Configuration> getJobWriterConfigList(Configuration configuration){
        List<Configuration> writerConfList = new ArrayList<>();
        //Try to get writer list
        List<Object> writerList = configuration.getList(CoreConstant.DATAX_JOB_CONTENT_WRITER);
        for(int i = 0; i < writerList.size(); i++){
            writerConfList.add(this.configuration.getConfiguration(
                    String.format(CoreConstant.DATAX_JOB_CONTENT_WRITER_ARRAY_PARAMETER, i)));
        }
        return writerConfList;
    }

    /**
     * Adjust job configuration to general structure
     * Added by davidhua@webank.com
     * @param configuration job configuration
     */
    private void adjustJobConfiguration(Configuration configuration){
        //Change structure of 'CoreConstant.DATAX_JOB_CONTENT_WRITER' to list
        try {
            configuration.getList(CoreConstant.DATAX_JOB_CONTENT_WRITER);
        }catch(Exception e){
            //Means that "CoreConstant.DATAX_JOB_CONTENT_WRITER" is not a list
            Configuration emptyListConf = Configuration.from("[]");
            emptyListConf.set("[0]", configuration.getConfiguration(CoreConstant.DATAX_JOB_CONTENT_WRITER));
            configuration.set(CoreConstant.DATAX_JOB_CONTENT_WRITER, emptyListConf);
        }
    }
    /**
     * Load processor
     * Added by davidhua@webank.com
     * @param namespace space
     * @return
     */
    private List<String> doLoadProcessor(String namespace){
        List<String> processors = new ArrayList<>();
        PluginProcessorLoader processorLoader = ClassUtil.instantiate(configuration.getString(
                CoreConstant.DATAX_CORE_PROCESSOR_LOADER_PLUGIN_CLASS
        ), PluginProcessorLoader.class);
        String sourcePath = configuration.getString(CoreConstant.DATAX_CORE_PROCESSOR_LODAER_PLUGIN_SOURCEPATH) +
                namespace;
        String packageName = configuration.getString(CoreConstant.DATAX_CORE_PROCESSOR_LOADER_PLUGIN_PACKAGE);
        LOG.info("Loading processors, sourcePath: [" + sourcePath + "]");
        if(new File(sourcePath).exists()) {
            Collection<File> javaSourceFiles = FileUtils.listFiles(new File(sourcePath), FileFileFilter.FILE, FileFileFilter.FILE);
            javaSourceFiles.forEach(javaSourceFile -> {
                try {
                    String javaCode = FileUtils.readFileToString(javaSourceFile);
                    String javaFileName = JavaSrcUtils.parseJavaFileName(javaCode);
                    if (StringUtils.isBlank(javaFileName)) {
                        throw DataXException.asDataXException(FrameworkErrorCode.PROCESSOR_LOAD_ERROR,
                                "Error in loading processor [" + javaSourceFile.getName() + "], cannot find class Name");
                    }
                    javaCode = JavaSrcUtils.addPackageName(javaCode, packageName);
                    String fullClassName = packageName + "." + javaFileName;
                    boolean result = processorLoader.load(fullClassName, javaCode);
                    if (!result) {
                        throw DataXException.asDataXException(FrameworkErrorCode.PROCESSOR_LOAD_ERROR,
                                "Loading processor [" + javaSourceFile.getName() + "] failed");
                    } else {
                        processors.add(fullClassName);
                    }
                } catch (IOException e) {
                    throw DataXException.asDataXException(FrameworkErrorCode.PROCESSOR_LOAD_ERROR, e);
                }
            });
        }
        LOG.info("Loading processors finished, " + JSON.toJSONString(processors));
        return processors;
    }


    @Override
    public void generateMetric() {
        LOG.info("Generate Metric");
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Communication nowJobContainerCommunication = containerCommunicator.collect();
                    nowJobContainerCommunication.setTimestamp(System.currentTimeMillis());
                    scheduleReport = CommunicationTool.getReportCommunication(nowJobContainerCommunication, lastJobContainerCommunication, calculateTaskCount(taskGroupConfigs));
                    lastJobContainerCommunication = nowJobContainerCommunication;
                }catch (Exception e){
                    LOG.error("generateMetric error:----"+e.getMessage());
                }


            }
        },0, 1000,TimeUnit.MILLISECONDS);
    }


    @Override
    public Communication getScheduleReport(){
        return scheduleReport;
    }
    private int calculateTaskCount(List<Configuration> configurations) {
        int totalTasks = 0;
        for (Configuration taskGroupConfiguration : configurations) {
            totalTasks += taskGroupConfiguration.getListConfiguration(
                    CoreConstant.DATAX_JOB_CONTENT).size();
        }

        return totalTasks;
    }



}
