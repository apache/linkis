package com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.linkis.common.io.resultset.ResultSetWriter;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.config.entries.ExecutionEntry;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ColumnInfo;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultKind;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.Session;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.IllegalArgumentException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlParseException;
import com.webank.wedatasphere.linkis.engine.flink.executor.FlinkResultListener;
import com.webank.wedatasphere.linkis.scheduler.executer.ErrorExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse;
import com.webank.wedatasphere.linkis.scheduler.executer.SuccessExecuteResponse;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory$;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.*;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.logging.log4j.util.Strings;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.webank.wedatasphere.linkis.storage.domain.*;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory;
import com.webank.wedatasphere.linkis.storage.resultset.table.*;
import com.webank.wedatasphere.linkis.storage.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.flink.configuration.ConfigOptions.key;

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
    public void initFlinkContext(EngineExecutorContext engineExecutorContext,DefaultContext defaultContext, SessionManager sessionManager, Map<String, String> jobParams) throws IllegalArgumentException {
        super.sessionManager = sessionManager;
        super.engineExecutorContext = engineExecutorContext;
        //第一步: 环境级别配置
        String jobName = jobParams.getOrDefault("flink.app.name", "datastar_flink_sql");
        String[] args = jobParams.getOrDefault("flink.app.args", Strings.EMPTY).split(" ");
        String yarnQueue = jobParams.getOrDefault("flink.app.queue", FlinkConfiguration.FLINK_APP_DEFAULT_QUEUE.getValue());
        Integer parallelism = Integer.valueOf(jobParams.getOrDefault("flink.app.parallelism", FlinkConfiguration.FLINK_APP_DEFAULT_PARALLELISM.getValue()));
        String savePointPath = jobParams.getOrDefault("flink.app.savePointPath", null);
        boolean allowNonRestoredState = Boolean.parseBoolean(jobParams.getOrDefault("flink.app.allowNonRestoredStatus", FlinkConfiguration.FLINK_APP_DEFAULT_ALLOW_NON_RESTORED_STATUS.getValue()));
        String jobmanagerMemory = jobParams.getOrDefault("flink.app.jobmanagerMemory", FlinkConfiguration.FLINK_APP_DEFAULT_JM_MEMORY.getValue());
        String taskmanagerMemory = jobParams.getOrDefault("flink.app.taskmanagerMemory", FlinkConfiguration.FLINK_APP_DEFAULT_TM_MEMORY.getValue());
        Integer numberOfTaskSlots = Integer.valueOf(jobParams.getOrDefault("flink.app.taskmanager.slot.number", FlinkConfiguration.FLINK_APP_DEFAULT_TASK_SLOT_NUMBER.getValue()));
        String jobConfigurationString = jobParams.getOrDefault("flink.app.user.configuration", Strings.EMPTY);

        //第二步: 应用级别配置
        //构建应用配置
        Configuration jobConfiguration = defaultContext.getFlinkConfig();
        //构建依赖jar包环境
        jobConfiguration.set(YarnConfigOptions.SHIP_DIRECTORIES, defaultContext.getShipDirs());
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
        jobConfiguration.set(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName());
        //进程级别配置
        String planner = jobParams.getOrDefault("flink.sql.planner", ExecutionEntry.EXECUTION_PLANNER_VALUE_BLINK);
        if (!AVAILABLE_PLANNERS.contains(planner.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Planner must be one of these: " + String.join(", ", AVAILABLE_PLANNERS));
        }
        String executionType = jobParams.getOrDefault("flink.sql.executionType",FlinkConfiguration.FLINK_SQL_EXECUTIONTYPE.defaultValue());
        if (!AVAILABLE_EXECUTION_TYPES.contains(executionType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Execution type must be one of these: " + String.join(", ", AVAILABLE_EXECUTION_TYPES));
        }
        String sessionIdReq = jobParams.get("flink.sql.sessionId");

        //第三步: 创建session
        Map<String, String> properties = Collections.emptyMap();;
        if(StringUtils.isBlank(sessionIdReq)) {
             this.sessionId = super.sessionManager.createSession(planner,executionType,properties);
         }
    }

    @Override
    public ExecuteResponse execute() {
        throw new UnsupportedOperationException("the operation is not supported");
    }

    @Override
    public ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL) {
        return this.execute(callSQL,null);
    }

    @Override
    public ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL, FlinkResultListener resultListener) {
        try {
            Session session = super.sessionManager.getSession(this.sessionId);
            Tuple2<ResultSet, SqlCommandParser.SqlCommand> tuple2 = session.runStatement(callSQL);
            ResultSet resultSet = tuple2.f0;
            //成功无结果集
            if (resultSet.getResultKind() == ResultKind.SUCCESS) {
                return new SuccessExecuteResponse();
            }
            //成功非SELECT有结果集
            if (callSQL.command != SqlCommandParser.SqlCommand.SELECT && resultSet.getResultKind() == ResultKind.SUCCESS_WITH_CONTENT) {
                ResultSetWriter resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory$.MODULE$.TABLE_TYPE());
                List<Column> columns = new ArrayList<>();
                resultSet.getColumns().forEach(columnInfo -> {
                    columns.add(new Column(columnInfo.getName(), null, null));
                });
                TableMetaData metaData = new TableMetaData(columns.toArray(new Column[columns.size()]));
                resultSetWriter.addMetaData(metaData);
                if(null!=resultSet.getData()) {
                    resultSet.getData().forEach(row -> {
                        List<String> rowList = new ArrayList<>();
                        for (int i = 0; i < row.getArity(); i++) {
                            rowList.add(row.getField(i).toString());
                        }
                        try {
                            resultSetWriter.addRecord(new TableRecord(rowList.toArray(new String[rowList.size()])));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                engineExecutorContext.sendResultSet(resultSetWriter);
                IOUtils.closeQuietly(resultSetWriter);
            }
            if (callSQL.command == SqlCommandParser.SqlCommand.SELECT ||callSQL.command == SqlCommandParser.SqlCommand.INSERT_INTO||callSQL.command == SqlCommandParser.SqlCommand.INSERT_OVERWRITE) {
                this.jobID = resultSet.getData().get(0).toString();
                this.applicationId = resultSet.getData().get(1).toString();
                this.webUrl = resultSet.getData().get(2).toString();
            }
            //获取SELECT数据结果集
            if (null!=resultListener) {
                String jobid = resultSet.getData().get(0).toString();
                List<ResultListener> listeners = new ArrayList<>();
                resultListener.setJobid(jobid);
                resultListener.setSession(session);
                listeners.add(resultListener);
                session.addResultListener(JobID.fromHexString(jobid), listeners);
            }
        } catch (IOException e) {
            return new ErrorExecuteResponse("Fail to run statement",e);
        }
        return new SuccessExecuteResponse();
    }
}

