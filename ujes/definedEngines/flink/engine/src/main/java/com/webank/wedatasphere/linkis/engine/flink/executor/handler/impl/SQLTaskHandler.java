package com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl;

import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.common.io.resultset.ResultSetWriter;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.config.Environment;
import com.webank.wedatasphere.linkis.engine.flink.client.config.entries.ExecutionEntry;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultKind;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.Session;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.IllegalArgumentException;
import com.webank.wedatasphere.linkis.engine.flink.executor.FlinkResultListener;
import com.webank.wedatasphere.linkis.engine.flink.util.ConfigurationParseUtils;
import com.webank.wedatasphere.linkis.scheduler.executer.ErrorExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.executer.SuccessExecuteResponse;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory$;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.*;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.types.Row;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.logging.log4j.util.Strings;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.webank.wedatasphere.linkis.storage.domain.*;
import com.webank.wedatasphere.linkis.storage.resultset.table.*;

import java.io.IOException;
import java.util.*;

/**
 * @program: flink-parent
 * @description:
 * @author: hui zhu
 * @create: 2020-12-16 11:46
 */
public class SQLTaskHandler extends AbstractTaskHandler {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private String sessionId;

    @Override
    public void initFlinkContext(EngineExecutorContext engineExecutorContext, DefaultContext defaultContext, SessionManager sessionManager, Map<String, String> jobParams) throws IllegalArgumentException {
        super.sessionManager        = sessionManager;
        super.engineExecutorContext = engineExecutorContext;
        //第一步: 环境级别配置
        String   jobName                         = jobParams.getOrDefault("flink.app.name", "linkis_flink_sql");
        String[] args                            = jobParams.getOrDefault("flink.app.args", Strings.EMPTY).split(" ");
        String   yarnQueue                       = jobParams.getOrDefault("flink.app.queue", FlinkConfiguration.FLINK_APP_DEFAULT_QUEUE.getValue());
        String   parallelism                     = jobParams.getOrDefault("flink.app.parallelism", FlinkConfiguration.FLINK_APP_DEFAULT_PARALLELISM.getValue());
        String   savePointPath                   = jobParams.getOrDefault("flink.app.savePointPath", null);
        boolean  allowNonRestoredState           = Boolean.parseBoolean(jobParams.getOrDefault("flink.app.allowNonRestoredStatus", FlinkConfiguration.FLINK_APP_DEFAULT_ALLOW_NON_RESTORED_STATUS.getValue()));
        String   jobmanagerMemory                = jobParams.getOrDefault("flink.app.jobmanagerMemory", FlinkConfiguration.FLINK_APP_DEFAULT_JM_MEMORY.getValue());
        String   taskmanagerMemory               = jobParams.getOrDefault("flink.app.taskmanagerMemory", FlinkConfiguration.FLINK_APP_DEFAULT_TM_MEMORY.getValue());
        Integer  numberOfTaskSlots               = Integer.valueOf(jobParams.getOrDefault("flink.app.taskmanager.slot.number", FlinkConfiguration.FLINK_APP_DEFAULT_TASK_SLOT_NUMBER.getValue()));
        String   jobConfigurationString          = jobParams.getOrDefault("flink.app.user.configuration", Strings.EMPTY);
        String   jobCheckPointIntervalMs         = jobParams.getOrDefault("flink.app.checkpoint.interval", FlinkConfiguration.FLINK_APP_CHECKPOINT_INTERVAL.getValue());
        String   jobMinIdleStateRetentionMinutes = jobParams.getOrDefault("flink.app.minIdleStateRetentionMinutes", FlinkConfiguration.FLINK_APP_MIN_STATE_RETENTION_MINUTES.getValue());
        String   jobMaxIdleStateRetentionMinutes = jobParams.getOrDefault("flink.app.maxIdleStateRetentionMinutes", FlinkConfiguration.FLINK_APP_MAX_STATE_RETENTION_MINUTES.getValue());
        String   dependencyLocalLibPaths         = jobParams.getOrDefault("flink.app.job.lib.local.paths", Strings.EMPTY);
        String   dependencyLocalLibDir           = jobParams.getOrDefault("flink.app.job.lib.local.dir", Strings.EMPTY);
        String   connectorLocalLibDir            = jobParams.getOrDefault("flink.app.job.connector.lib.dir", Strings.EMPTY);
        //第二步: 应用级别配置
        //构建应用配置
        Configuration jobConfiguration = defaultContext.getFlinkSystemConfig().clone();

        //构建依赖jar包环境
        List<String> currentShipDirList = Lists.newArrayList();
        currentShipDirList.addAll(defaultContext.getShipDirs());
        if (StringUtils.isNotBlank(dependencyLocalLibDir)) {
            currentShipDirList.add(dependencyLocalLibDir);
        }
        if (Strings.isNotBlank(connectorLocalLibDir)) {
            String[] connectorLibDirs = connectorLocalLibDir.split(",");
            for (String connectorLocalDir : connectorLibDirs) {
                currentShipDirList.add(connectorLocalDir);
            }
        }
        jobConfiguration.set(YarnConfigOptions.SHIP_DIRECTORIES, currentShipDirList);
        //yarn 作业名称
        jobConfiguration.set(YarnConfigOptions.APPLICATION_NAME, jobName);
        //yarn queue
        jobConfiguration.set(YarnConfigOptions.APPLICATION_QUEUE, yarnQueue);
        //设置：资源/并发度
        jobConfiguration.setInteger(CoreOptions.DEFAULT_PARALLELISM, Integer.valueOf(parallelism));
        jobConfiguration.set(JobManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.parse(jobmanagerMemory));
        jobConfiguration.set(TaskManagerOptions.TOTAL_PROCESS_MEMORY, MemorySize.parse(taskmanagerMemory));
        jobConfiguration.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, numberOfTaskSlots);
        //设置启动checkpoint/savepoint路径
        if (StringUtils.isNotBlank(savePointPath)) {
            initSavepointConfig(jobConfiguration, savePointPath, allowNonRestoredState);
        }
        //设置：用户配置
        jobConfiguration.set(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName());
        if (org.apache.commons.lang.StringUtils.isNotBlank(jobConfigurationString)) {
            Configuration userConfiguration = ConfigurationParseUtils.parseConfiguration(jobConfigurationString);
            jobConfiguration.addAll(userConfiguration);
        }
        defaultContext.setFlinkConfig(jobConfiguration);
        //进程级别配置
        String planner = jobParams.getOrDefault("flink.sql.planner", ExecutionEntry.EXECUTION_PLANNER_VALUE_BLINK);
        if (!AVAILABLE_PLANNERS.contains(planner.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Planner must be one of these: " + String.join(", ", AVAILABLE_PLANNERS));
        }
        String executionType = jobParams.getOrDefault("flink.sql.executionType", FlinkConfiguration.FLINK_SQL_EXECUTIONTYPE.defaultValue());

        if (!AVAILABLE_EXECUTION_TYPES.contains(executionType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Execution type must be one of these: " + String.join(", ", AVAILABLE_EXECUTION_TYPES));
        }
        String sessionIdReq = jobParams.get("flink.sql.sessionId");

        //第三步: 创建session
        Map<String, String> properties = Maps.newHashMap();
        properties.put(Environment.EXECUTION_ENTRY.concat(".parallelism"), parallelism);
        properties.put(Environment.EXECUTION_ENTRY.concat(".checkpoint-interval"), jobCheckPointIntervalMs);
        properties.put(Environment.EXECUTION_ENTRY.concat(".min-idle-state-retention"), jobMinIdleStateRetentionMinutes);
        properties.put(Environment.EXECUTION_ENTRY.concat(".max-idle-state-retention"), jobMaxIdleStateRetentionMinutes);
        properties.put(Environment.EXECUTION_ENTRY.concat(".local-lib-path"), dependencyLocalLibPaths);
        if (StringUtils.isBlank(sessionIdReq)) {
            this.sessionId = super.sessionManager.createSession(planner, executionType, properties);
        }
    }

    /**
     * @param allowNonRestoredState 失败则忽略
     */
    private void initSavepointConfig(Configuration configuration, String savePointPath, boolean allowNonRestoredState) {
        SavepointRestoreSettings savepointRestoreSettings = SavepointRestoreSettings.forPath(savePointPath, allowNonRestoredState);
        SavepointRestoreSettings.toConfiguration(savepointRestoreSettings, configuration);
    }

    @Override
    public ExecuteResponse execute() {
        throw new UnsupportedOperationException("the operation is not supported");
    }

    @Override
    public ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL) {
        return this.execute(callSQL, null);
    }

    @Override
    public ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL, FlinkResultListener resultListener) {
        try {
            Session                                        session   = super.sessionManager.getSession(this.sessionId);
            Tuple2<ResultSet, SqlCommandParser.SqlCommand> tuple2    = session.runStatement(callSQL);
            ResultSet                                      resultSet = tuple2.f0;
            LOG.info("the result of RunStatement {} is {}", JSON.toString(callSQL), JSON.toString(resultSet));
            //成功非SELECT有结果集
            if (callSQL.command != SqlCommandParser.SqlCommand.SELECT && resultSet.getResultKind() == ResultKind.SUCCESS_WITH_CONTENT) {
                ResultSetWriter resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory$.MODULE$.TABLE_TYPE());
                List<Column>    columns         = new ArrayList<>();
                resultSet.getColumns().forEach(columnInfo -> columns.add(new Column(columnInfo.getName(), null, null)));
                TableMetaData metaData = new TableMetaData(columns.toArray(new Column[columns.size()]));
                resultSetWriter.addMetaData(metaData);
                if (CollectionUtils.isNotEmpty(resultSet.getData())) {
                    resultSet.getData().forEach(row -> {
                        List<String> rowList = new ArrayList<>();
                        for (int i = 0; i < row.getArity(); i++) {
                            rowList.add(row.getField(i).toString());
                        }
                        try {
                            resultSetWriter.addRecord(new TableRecord(rowList.toArray(new String[rowList.size()])));
                        } catch (IOException e) {
                            LOG.error("resultWriter add record error!", e);
                        }
                    });
                }
                engineExecutorContext.sendResultSet(resultSetWriter);
                IOUtils.closeQuietly(resultSetWriter);
            }

            if (callSQL.command == SqlCommandParser.SqlCommand.SELECT
                    || callSQL.command == SqlCommandParser.SqlCommand.INSERT_INTO) {
                List<Row> list = resultSet.getData();
                this.jobID         = list.get(0).getField(0).toString();
                this.applicationId = list.get(0).getField(1).toString();
                this.webUrl        = list.get(0).getField(2).toString();
            }
            if (null != resultListener && StringUtils.isNotBlank(this.jobID)) {
                List<ResultListener> listeners = new ArrayList<>();
                resultListener.setJobid(this.jobID);
                resultListener.setSession(session);
                listeners.add(resultListener);
                session.addResultListener(JobID.fromHexString(this.jobID), listeners);
            }
            //成功无结果集
            if (resultSet.getResultKind() == ResultKind.SUCCESS) {
                return new SuccessExecuteResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorExecuteResponse("Fail to run statement", e);
        }
        return new SuccessExecuteResponse();
    }
}

