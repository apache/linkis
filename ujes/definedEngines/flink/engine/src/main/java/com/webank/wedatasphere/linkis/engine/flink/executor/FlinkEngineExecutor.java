/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.flink.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutor;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.codeparser.FlinkCodeParser;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlParseException;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.TaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl.AbstractTaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl.DataStreamTaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl.SQLTaskHandler;
import com.webank.wedatasphere.linkis.hadoop.common.utils.HDFSUtils;
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;
import com.webank.wedatasphere.linkis.resourcemanager.LoadInstanceResource;
import com.webank.wedatasphere.linkis.resourcemanager.Resource;
import com.webank.wedatasphere.linkis.rpc.Sender;
import com.webank.wedatasphere.linkis.scheduler.executer.*;
import com.webank.wedatasphere.linkis.storage.FSFactory;
import com.webank.wedatasphere.linkis.storage.fs.FileSystem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.util.Strings;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by liangqilang on 01 20, 2021
 */
public class FlinkEngineExecutor extends EngineExecutor implements SingleTaskOperateSupport, SingleTaskInfoSupport {
    private Logger LOG = LoggerFactory.getLogger(getClass());
    private String nameSuffix = "_FlinkEngineExecutor";
    private String name = Sender.getThisServiceInstance().getInstance();
    private Map<String, String> DWCOptionMap;
    private DefaultContext defaultContext;
    private SessionManager sessionManager;
    private LinkedBlockingQueue<ResultListener> progressList = new LinkedBlockingQueue<>();
    private AbstractTaskHandler taskHandler;
    private EngineExecutorContext engineExecutorContext;
    private FileSystem hdfsFileSystem;
    private  FileSystem localFileSystem;

    public FlinkEngineExecutor(int outputPrintLimit, boolean isSupportParallelism, Map<String, String> DWCOptionMap, DefaultContext defaultContext, SessionManager sessionManager) {
        super(outputPrintLimit, isSupportParallelism);
        this.DWCOptionMap = DWCOptionMap;
        this.defaultContext = defaultContext;
        this.sessionManager = sessionManager;
    }

    @Override
    public String getName() {
        return this.name;
    }


    @Override
    public void init() {
        transition(ExecutorState.Idle());
        LOG.info("Ready to change engine state!");
        setCodeParser(new FlinkCodeParser());
        super.init();
    }


    @Override
    public Resource getActualUsedResources() {
        return new LoadInstanceResource(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), 2, 1);
    }


    @Override
    public ExecuteResponse execute(ExecuteRequest executeRequest) {
        if (StringUtils.isEmpty(executeRequest.code())) {
            return new IncompleteExecuteResponse("Your code is incomplete, it may be that only comments are selected for execution(您的代码不完整，可能是仅仅选中了注释进行执行)");
        }
        this.engineExecutorContext = this.createEngineExecutorContext(executeRequest);
        ExecuteResponse executeResponse;
        LOG.info("execute properties，runtime params" + JSON.toString(this.engineExecutorContext.getProperties()));
        LOG.info("execute code:" + JSON.toString(executeRequest.code()));
        ObjectMapper objectMapper = new ObjectMapper();
        Object streamJobExecuteInfoObj = this.engineExecutorContext.getProperties().get("stream");
        Map<String, String> jobExecuteParams = new HashMap<>();
        try {
            if (null != streamJobExecuteInfoObj) {
                jobExecuteParams = objectMapper.readValue((String) streamJobExecuteInfoObj, Map.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String runType = this.engineExecutorContext.getProperties().get("runType").toString();
        //用户开发和生产分流处理
        String envType = jobExecuteParams.getOrDefault("flink.env.type", FlinkConfiguration.FLINK_ENV_TYPE.getValue());
        try {
            //datastream模式
            if (runType.equals("flink")) {
                this.taskHandler = new DataStreamTaskHandler();
                this.taskHandler.initFlinkContext(this.engineExecutorContext, this.defaultContext, this.sessionManager, jobExecuteParams);
                executeResponse = taskHandler.execute();
            }
            //SQL模式
            if (runType.equals("fql")) {
                this.taskHandler = new SQLTaskHandler();
                this.taskHandler.initFlinkContext(this.engineExecutorContext, this.defaultContext, this.sessionManager, jobExecuteParams);
                FlinkCodeParser flinkCodeParser = new FlinkCodeParser();
                String[] codes = flinkCodeParser.parse(executeRequest.code(), this.engineExecutorContext);
                this.engineExecutorContext.setTotalParagraph(codes.length);
                List<SqlCommandParser.SqlCommandCall> allList = Arrays.stream(codes).map(code->{
                    //remove the \t\r\n to support the influxdb metric
                    code = code.replaceAll("\t|\r|\n", " ").trim();
                    SqlCommandParser.SqlCommandCall callSQL;
                    Optional<SqlCommandParser.SqlCommandCall> callOpt = SqlCommandParser.parse(code, true);
                    if (!callOpt.isPresent()) {
                        throw new SqlParseException("Unknown statement: " + code);
                    } else {
                        callSQL = callOpt.get();
                    }
                    return callSQL;
                }).collect(Collectors.toList());

                //合并insert SQL，多个SQL提交到一个任务
                List<String> insertlist = allList.stream().filter(callSQL->(callSQL.command==SqlCommandParser.SqlCommand.INSERT_OVERWRITE||callSQL.command==SqlCommandParser.SqlCommand.INSERT_INTO))
                        .map(callSQL-> callSQL.operands[0]).collect(Collectors.toList());
                List<SqlCommandParser.SqlCommandCall> executeSQLList = allList.stream().filter(callSQL->callSQL.command!=SqlCommandParser.SqlCommand.INSERT_OVERWRITE&&callSQL.command!=SqlCommandParser.SqlCommand.INSERT_INTO).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(insertlist)) {
                    String[] operands = insertlist.toArray(new String[insertlist.size()]);
                    SqlCommandParser.SqlCommandCall insertSql = new SqlCommandParser.SqlCommandCall(SqlCommandParser.SqlCommand.INSERT_INTO, operands);
                    executeSQLList.add(insertSql);
                }
                executeSQLList.forEach(executeSQL-> this.executeSQL(this.engineExecutorContext, taskHandler, executeSQL, envType));
            }
        } catch (Exception e) {
            LOG.error("deploy error！", e);
            return new ErrorExecuteResponse("submit flink job error!", e);
        }
        try {
            this.engineExecutorContext.close();
            executeResponse = new SuccessExecuteResponse();
        } catch (Exception e) {
            executeResponse = new ErrorExecuteResponse("send resultSet to entrance failed!", e);
        }
        return executeResponse;
    }

    public ExecuteResponse executeSQL(EngineExecutorContext engineExecutorContext, TaskHandler taskHandler, SqlCommandParser.SqlCommandCall callSQL, String envType) {
        ExecuteResponse  executeResponse;
        LOG.info("execute SQL {}",JSON.toString(callSQL.operands));
        //开发环境SELECT 需要等待结果集。
        if (callSQL.command == SqlCommandParser.SqlCommand.SELECT && envType.equals("dev")) {
            FlinkResultListener flinkResultListener = new FlinkResultListener(engineExecutorContext);
            progressList.add(flinkResultListener);
            executeResponse = taskHandler.execute(callSQL, flinkResultListener);
            //等待结果集
            while (!flinkResultListener.isFinished()) {
                try {
                    Thread.sleep(5000);
                    LOG.info("wait for the result...");
                } catch (InterruptedException e) {
                }
            }
            return executeResponse;
        } else {
            return taskHandler.execute(callSQL);
        }
    }

    @Override
    public ExecuteResponse executeLine(EngineExecutorContext engineExecutorContext, String code) {
        throw new UnsupportedOperationException("the operation is not supported");
    }

    @Override
    public ExecuteResponse executeCompletely(EngineExecutorContext engineExecutorContext, String code, String completedLine) {
        throw new UnsupportedOperationException("the operation is not supported");
    }


    @Override
    public float progress() {
        return 0;
    }

    @Override
    public JobProgressInfo[] getProgressInfo() {
        return new JobProgressInfo[0];
    }

    @Override
    public String log() {
        return "";
    }

    @Override
    public boolean kill() {
        progressList.forEach(resultListener->{
            resultListener.cancel();
        });
        return true;
    }

    @Override
    public boolean pause() {
        return true;
    }

    @Override
    public boolean resume() {
        return true;
    }

    @Override
    public void close() throws IOException {

    }
}
