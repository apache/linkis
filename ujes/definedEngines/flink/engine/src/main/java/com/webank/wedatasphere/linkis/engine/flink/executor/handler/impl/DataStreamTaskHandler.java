package com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.deployment.ClusterDescriptorAdapter;
import com.webank.wedatasphere.linkis.engine.flink.client.deployment.ClusterDescriptorAdapterFactory;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.IllegalArgumentException;
import com.webank.wedatasphere.linkis.engine.flink.exception.JobExecutionException;
import com.webank.wedatasphere.linkis.engine.flink.executor.FlinkResultListener;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.TaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.util.ConfigurationParseUtils;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.executer.SuccessExecuteResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterClientServiceLoader;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.configuration.*;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.logging.log4j.util.Strings;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: flink-parent
 * @description:
 * @author: hui zhu
 * @create: 2020-12-16 11:46
 */
public class DataStreamTaskHandler extends AbstractTaskHandler {

    private String[] programArguments;

    private String applicationClassName;

    @Override
    public void initFlinkContext(EngineExecutorContext engineExecutorContext,DefaultContext defaultContext, SessionManager sessionManager, Map<String, String> jobParams)  {
        //第一步: 解析应用配置
        String entryPointJarPath = jobParams.getOrDefault("flink.app.entry.point.jar.path", Strings.EMPTY);
        String entryPointClass = jobParams.getOrDefault("flink.app.entry.point.class", Strings.EMPTY);
        String jobName = jobParams.getOrDefault("flink.app.name", "datastar_default_app");
        String[] args = jobParams.getOrDefault("flink.app.args", Strings.EMPTY).split(" ");
        String[] providedLibDirs = jobParams.getOrDefault("flink.app.job.lib.dirs", Strings.EMPTY).split(",");
        String[] classpaths = jobParams.getOrDefault("flink.app.classpaths", Strings.EMPTY).split(",");
        String yarnQueue = jobParams.getOrDefault("flink.app.queue", FlinkConfiguration.FLINK_APP_DEFAULT_QUEUE.getValue());
        Integer parallelism = Integer.valueOf(jobParams.getOrDefault("flink.app.parallelism", FlinkConfiguration.FLINK_APP_DEFAULT_PARALLELISM.getValue()));
        String savePointPath = jobParams.getOrDefault("flink.app.savePointPath", null);
        boolean allowNonRestoredState = Boolean.parseBoolean(jobParams.getOrDefault("flink.app.allowNonRestoredStatus", FlinkConfiguration.FLINK_APP_DEFAULT_ALLOW_NON_RESTORED_STATUS.getValue()));
        String jobmanagerMemory = jobParams.getOrDefault("flink.app.jobmanagerMemory", FlinkConfiguration.FLINK_APP_DEFAULT_JM_MEMORY.getValue());
        String taskmanagerMemory = jobParams.getOrDefault("flink.app.taskmanagerMemory", FlinkConfiguration.FLINK_APP_DEFAULT_TM_MEMORY.getValue());
        Integer numberOfTaskSlots = Integer.valueOf(jobParams.getOrDefault("flink.app.taskmanager.slot.number", FlinkConfiguration.FLINK_APP_DEFAULT_TASK_SLOT_NUMBER.getValue()));
        String jobConfigurationString = jobParams.getOrDefault("flink.app.user.configuration", Strings.EMPTY);
        this.programArguments = args;
        this.applicationClassName = entryPointClass;

        //第二步: 构建应用配置
        Configuration jobConfiguration = defaultContext.getFlinkConfig();
        //构建依赖jar包环境
        List<String> providedLibDirList = Lists.newArrayList(providedLibDirs).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        providedLibDirList.add(defaultContext.getFlinkLibRemotePath());
        jobConfiguration.set(YarnConfigOptions.PROVIDED_LIB_DIRS, providedLibDirList);
        //设置：用户入口jar：可以远程，只能设置1个jar
        jobConfiguration.set(PipelineOptions.JARS, Collections.singletonList(entryPointJarPath));
        //设置：classpaths：本地，不建议设置
        jobConfiguration.set(PipelineOptions.CLASSPATHS, Arrays.asList(classpaths));
        //yarn 作业名称
        jobConfiguration.set(YarnConfigOptions.APPLICATION_NAME, jobName);
        //yarn queue
        jobConfiguration.set(YarnConfigOptions.APPLICATION_QUEUE, yarnQueue);
        //设置：资源/并发度
        jobConfiguration.setInteger(CoreOptions.DEFAULT_PARALLELISM, parallelism);
        jobConfiguration.set(JobManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.parse(jobmanagerMemory));
        jobConfiguration.set(TaskManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.parse(taskmanagerMemory));
        jobConfiguration.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, numberOfTaskSlots);
        //设置：用户配置
        jobConfiguration.set(DeploymentOptions.TARGET, YarnDeploymentTarget.APPLICATION.getName());
        if(StringUtils.isNotBlank(jobConfigurationString)){
            Configuration userConfiguration = ConfigurationParseUtils.parseConfiguration(jobConfigurationString);
            jobConfiguration.addAll(userConfiguration);
        }
        //第三步：初始化   ExecutionContext
        if (StringUtils.isNotBlank(savePointPath)) {
            this.initSavepointConfig(savePointPath, allowNonRestoredState);
        }

        //构建作业执行环境上下文
        this.executionContext =  ExecutionContext.builder(
                defaultContext.getDefaultEnv(),
                null,
                defaultContext.getDependencies(),
                jobConfiguration,
                defaultContext.getClusterClientServiceLoader()
                ).build();
    }

    @Override
    public ExecuteResponse execute() {
        ClusterDescriptorAdapter clusterDescriptor = ClusterDescriptorAdapterFactory.create(this.executionContext, null, null);
        clusterDescriptor.deployCluster(this.programArguments,this.applicationClassName);
        if(null==clusterDescriptor.getJobId()||null==clusterDescriptor.getClusterID()) {
            throw new JobExecutionException("The task " + applicationClassName + " failed,no result was returned.");
        }
        this.jobID = clusterDescriptor.getJobId().toHexString();
        this.applicationId = clusterDescriptor.getClusterID().toString();
        this.webUrl = clusterDescriptor.getWebInterfaceUrl();
        return new SuccessExecuteResponse();
    }

    @Override
    public ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL) {
       throw new UnsupportedOperationException("the operation is not supported");
    }

    @Override
    public ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL, FlinkResultListener flinkResultListener) {
        throw new UnsupportedOperationException("the operation is not supported");
    }


    /**
         * @param savePointPath
         * @param allowNonRestoredState 失败则忽略
         */
    private void initSavepointConfig(String savePointPath, boolean allowNonRestoredState) {
        SavepointRestoreSettings savepointRestoreSettings = SavepointRestoreSettings.forPath(savePointPath, allowNonRestoredState);
        SavepointRestoreSettings.toConfiguration(savepointRestoreSettings, executionContext.getFlinkConfig());
    }
}
