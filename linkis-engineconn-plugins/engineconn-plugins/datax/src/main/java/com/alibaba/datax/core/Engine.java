package com.alibaba.datax.core;

import com.alibaba.datax.common.element.ColumnCast;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.spi.ErrorCode;
import com.alibaba.datax.common.statistics.PerfTrace;
import com.alibaba.datax.common.statistics.VMInfo;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.job.JobContainer;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.taskgroup.TaskGroupContainer;
import com.alibaba.datax.core.util.ConfigParser;
import com.alibaba.datax.core.util.ConfigurationValidate;
import com.alibaba.datax.core.util.ExceptionTracker;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.CoreConstant;
import com.alibaba.datax.core.util.container.LoadUtil;
import com.webank.wedatasphere.exchangis.datax.core.ThreadLocalSecurityManager;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine {
    private static final Log LOG = LogFactory.getLog(Engine.class);

    private static String RUNTIME_MODE;
    private static AbstractContainer container;
    private static String jobExecId = "-1";

    /* check job model (job/task) first */
    public void start(Configuration allConf) {

        // 绑定column转换信息
        ColumnCast.bind(allConf);

        /**
         * 初始化PluginLoader，可以获取各种插件配置
         */
        LoadUtil.bind(allConf);

        boolean isJob = !("taskGroup".equalsIgnoreCase(allConf
                .getString(CoreConstant.DATAX_CORE_CONTAINER_MODEL)));
        //JobContainer会在schedule后再行进行设置和调整值
        int channelNumber =0;
        long instanceId;
        int taskGroupId = -1;
        if (isJob) {
            allConf.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_MODE, RUNTIME_MODE);
            container = new JobContainer(allConf);
            instanceId = allConf.getLong(
                    CoreConstant.DATAX_CORE_CONTAINER_JOB_ID, 0);

        } else {
            container = new TaskGroupContainer(allConf);
            instanceId = allConf.getLong(
                    CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);
            taskGroupId = allConf.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);
            channelNumber = allConf.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_CHANNEL);
        }

        //缺省打开perfTrace
        boolean traceEnable = allConf.getBool(CoreConstant.DATAX_CORE_CONTAINER_TRACE_ENABLE, true);
        boolean perfReportEnable = allConf.getBool(CoreConstant.DATAX_CORE_REPORT_DATAX_PERFLOG, true);

        //standlone模式的datax shell任务不进行汇报
        if(instanceId == -1){
            perfReportEnable = false;
        }

        int priority = 0;
        try {
            priority = Integer.parseInt(System.getenv("SKYNET_PRIORITY"));
        }catch (NumberFormatException e){
            LOG.warn("prioriy set to 0, because NumberFormatException, the value is: "+System.getProperty("PROIORY"));
        }

        Configuration jobInfoConfig = allConf.getConfiguration(CoreConstant.DATAX_JOB_JOBINFO);
        //初始化PerfTrace
        PerfTrace perfTrace = PerfTrace.getInstance(isJob, instanceId, taskGroupId, priority, traceEnable);
        perfTrace.setJobInfo(jobInfoConfig,perfReportEnable,channelNumber);
        container.start();

    }


    // 注意屏蔽敏感信息
    public static String filterJobConfiguration(final Configuration configuration) {
        Configuration jobConfWithSetting = configuration.getConfiguration("job").clone();

        Configuration jobContent = jobConfWithSetting.getConfiguration("content");

        filterSensitiveConfiguration(jobContent);

        jobConfWithSetting.set("content",jobContent);

        return jobConfWithSetting.beautify();
    }

    public static Configuration filterSensitiveConfiguration(Configuration configuration){
        Set<String> keys = configuration.getKeys();
        for (final String key : keys) {
            boolean isSensitive = StringUtils.endsWithIgnoreCase(key, "password")
                    || StringUtils.endsWithIgnoreCase(key, "accessKey");
            if (isSensitive && configuration.get(key) instanceof String) {
                configuration.set(key, configuration.getString(key).replaceAll("[\\s\\S]", "*"));
            }
        }
        return configuration;
    }

    public static void entry(final String[] args) throws Throwable {
        Options options = new Options();
        options.addOption("job", true, "Job config.");
        options.addOption("jobid", true, "Job unique id.");
        options.addOption("mode", true, "Job runtime mode.");

        BasicParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);

        String jobPath = cl.getOptionValue("job");

        // 如果用户没有明确指定jobid, 则 datax.py 会指定 jobid 默认值为-1
        String jobIdString = cl.getOptionValue("jobid");
        RUNTIME_MODE = cl.getOptionValue("mode");

        Configuration configuration = ConfigParser.parse(jobPath);

        long jobId;
        if (!"-1".equalsIgnoreCase(jobIdString)) {
            jobId = Long.parseLong(jobIdString);
        } else {
            // only for dsc & ds & datax 3 update
            String dscJobUrlPatternString = "/instance/(\\d{1,})/config.xml";
            String dsJobUrlPatternString = "/inner/job/(\\d{1,})/config";
            String dsTaskGroupUrlPatternString = "/inner/job/(\\d{1,})/taskGroup/";
            List<String> patternStringList = Arrays.asList(dscJobUrlPatternString,
                    dsJobUrlPatternString, dsTaskGroupUrlPatternString);
            jobId = parseJobIdFromUrl(patternStringList, jobPath);
        }

        jobExecId = String.valueOf(jobId);
        boolean isStandAloneMode = "standalone".equalsIgnoreCase(RUNTIME_MODE);
        if (!isStandAloneMode && jobId == -1) {
            // 如果不是 standalone 模式，那么 jobId 一定不能为-1
            throw DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR, "非 standalone 模式必须在 URL 中提供有效的 jobId.");
        }
        configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID, jobId);

        //打印vmInfo
        VMInfo vmInfo = VMInfo.getVmInfo();
        if (vmInfo != null) {
            LOG.info(vmInfo.toString());
        }

        LOG.info("\n" + filterJobConfiguration(configuration) + "\n");

        LOG.debug(configuration.toJSON());

        ConfigurationValidate.doValidate(configuration);
        Engine engine = new Engine();
        engine.start(configuration);
    }


    /**
     * -1 表示未能解析到 jobId
     *
     *  only for dsc & ds & datax 3 update
     */
    private static long parseJobIdFromUrl(List<String> patternStringList, String url) {
        long result = -1;
        for (String patternString : patternStringList) {
            result = doParseJobIdFromUrl(patternString, url);
            if (result != -1) {
                return result;
            }
        }
        return result;
    }

    private static long doParseJobIdFromUrl(String patternString, String url) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        return -1;
    }

    /**
     * Save PID file
     * Added by davidhua@webank.com
     * @param workDir
     * @throws IOException
     */
    private static void savePID(String workDir) throws IOException{
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0].trim();
        File pidFile = new File(workDir + "/pid");
        FileUtils.write(pidFile, pid);
    }

    /**
     * Delete PID file
     * Added by davidhua@webank.com
     * @param workDir
     * @throws IOException
     */
    private static void removePID(String workDir) throws IOException{
        FileUtils.forceDelete(new File(workDir + "/pid"));
    }

    public static int main(String[] args) {
        int exitCode = 0;
        LOG.info("start to run");
        String workDir = System.getProperty("user.dir");
        if(StringUtils.isBlank(workDir)){
            exitCode = 1;
        }else {
            try {
                //Write current process's pid into file
                savePID(workDir);
                //Set security manager
                System.setSecurityManager(new ThreadLocalSecurityManager());
                entry(args);
                removePID(workDir);
            } catch (Throwable e) {
                exitCode = 1;
                LOG.error("\n\n该任务最可能的错误原因是:\n" + ExceptionTracker.trace(e));
                if (e instanceof DataXException) {
                    DataXException tempException = (DataXException) e;
                    ErrorCode errorCode = tempException.getErrorCode();
                    if (errorCode instanceof FrameworkErrorCode) {
                        FrameworkErrorCode tempErrorCode = (FrameworkErrorCode) errorCode;
                        exitCode = tempErrorCode.toExitValue();
                    }
                }
                //Throw OutOfMemoryError
                Throwable cause = e;
                try {
                    while (null != cause) {
                        if (cause instanceof OutOfMemoryError) {
                            throw (OutOfMemoryError) cause;
                        }
                        cause = cause.getCause();
                    }
                }finally{
                    return exitCode;
                }
            }
        }
        return exitCode;
    }
    public static void close(){
        if(container != null){
            container.shutdown();
        }
    }


    public static Float progress() {
        try {
            if(container == null){
                return 0.0f;
            }
            Communication scheduleReport = container.getScheduleReport();
            if (scheduleReport != null) {
                return scheduleReport.getCounter().getOrDefault("percentage",0.0).floatValue();
            }
        } catch (Exception e) {
            LOG.error("Progress-->"+e);
        }
        return 0.0f;
    }

    public static Map<String, Integer> getProgressInfo() {
        Map<String,Integer> infoMap = new HashMap<>();
        Communication scheduleReport = container.getScheduleReport();
        try {
            if(container == null){
                return infoMap;
            }
            if(scheduleReport!=null){
                Map<String, Number> counter = scheduleReport.getCounter();
                if(counter.get("taskRunningWriters")!=null){
                    infoMap.put("totalTasks", counter.get("taskRunningWriters").intValue());
                    infoMap.put("runningTasks", counter.get("channelRunning").intValue());
                    infoMap.put("failedTasks", 0);
                    infoMap.put("succeedTasks", counter.get("stage").intValue());
                }
            }
            return infoMap;
        }catch (Exception e){
            LOG.error("getProgressInfo->"+e);
            return infoMap;
        }
    }

    public static Map<String,Long> getMetrics(){
        Map<String,Long> metrics = new HashMap<>();
        if(container == null){
            return metrics;
        }
        Communication scheduleReport = container.getScheduleReport();
        String[] types = {"readSucceedRecords", "totalErrorBytes", "writeSucceedBytes","byteSpeed","totalErrorRecords","recordSpeed",
                "waitReaderTime","writeReceivedBytes","waitWriterTime","waitWriterTime",
                "taskRunningWriters","totalReadRecords","writeReceivedRecords","readSucceedBytes","totalReadBytes"};
        try {
            if(scheduleReport != null) {
                for (int i = 0; i < types.length; i++) {
                    metrics.put(types[i],scheduleReport.getCounter().get(types[i]).longValue());
                }
            }
        } catch (Exception e) {
            LOG.error("getMetrics-->"+e);
            return metrics;
        }
        return metrics;
    }

    public static Map<String,List<String>> getDiagnosis(){
        Map<String,List<String>> diagnosis = new HashMap<>();
        if(container == null){
            return diagnosis;
        }
        Communication scheduleReport = container.getScheduleReport();
        try {
            if(scheduleReport != null){
                return scheduleReport.getMessage();
            }
            return diagnosis;
        }catch (Exception e){
            LOG.error("getDiagnosis->"+e);
        }
        return diagnosis;
    }
    public static String getJobId(){
        return jobExecId;
    }

}
