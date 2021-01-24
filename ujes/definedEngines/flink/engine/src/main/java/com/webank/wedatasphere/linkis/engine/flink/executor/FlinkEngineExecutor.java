package com.webank.wedatasphere.linkis.engine.flink.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutor;
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.codeparser.FlinkCodeParser;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.engine.flink.conf.FlinkConfiguration;
import com.webank.wedatasphere.linkis.engine.flink.exception.IllegalArgumentException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlParseException;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.TaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl.AbstractTaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl.DataStreamTaskHandler;
import com.webank.wedatasphere.linkis.engine.flink.executor.handler.impl.SQLTaskHandler;
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;
import com.webank.wedatasphere.linkis.protocol.stream.ResponsePersistTask;
import com.webank.wedatasphere.linkis.resourcemanager.LoadInstanceResource;
import com.webank.wedatasphere.linkis.resourcemanager.Resource;
import com.webank.wedatasphere.linkis.rpc.Sender;
import com.webank.wedatasphere.linkis.scheduler.executer.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @program: linkis
 * @description:
 * @author: hui zhu
 * @create: 2020-07-28 17:16
 */
public class FlinkEngineExecutor extends EngineExecutor implements SingleTaskOperateSupport, SingleTaskInfoSupport {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private String nameSuffix = "_FlinkEngineExecutor";

    private Sender streamManagerSender;

    private String name = Sender.getThisServiceInstance().getInstance();

    private Map<String, String> DWCOptionMap;

    private DefaultContext defaultContext;

    private SessionManager sessionManager;

    private LinkedBlockingQueue<ResultListener> progressList = new LinkedBlockingQueue<>();

    private AbstractTaskHandler taskHandler;

    private EngineExecutorContext engineExecutorContext;

    public FlinkEngineExecutor(int outputPrintLimit, boolean isSupportParallelism, Map<String, String> DWCOptionMap, DefaultContext defaultContext, SessionManager sessionManager) {
        super(outputPrintLimit, isSupportParallelism);
        this.DWCOptionMap = DWCOptionMap;
        this.streamManagerSender = Sender.getSender(FlinkConfiguration.STREAMMANAGER_SPRING_APPLICATION_NAME.getValue().toString());
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
        //TODO 系统初始化
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
        //TODO hui.zhu 临时测试参数
        //jobExecuteParams.put("flink.sql.executionType", "streaming");
        String runType = this.engineExecutorContext.getProperties().get("runType").toString();
        //用户开发和生产分流处理
        String envType = jobExecuteParams.getOrDefault("flink.env.type", FlinkConfiguration.FLINK_ENV_TYPE.getValue());
        String streamTaskId = jobExecuteParams.getOrDefault("stream.task.id", Strings.EMPTY);
        try {
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
                for(int index=0;index<codes.length;index++){
                    if(this.engineExecutorContext.isKilled()) {
                        return new ErrorExecuteResponse("Job is killed by user!", null);
                    }
                    this.engineExecutorContext.setCurrentParagraph(index + 1);
                    executeResponse = this.executeSQL(this.engineExecutorContext, taskHandler, codes[index], envType);
                }
            }
            if(envType.equals("prod")) {
                ResponsePersistTask responsePersistTask = new ResponsePersistTask();
                responsePersistTask.setTaskID(Long.valueOf(streamTaskId));
                responsePersistTask.setJobID(this.taskHandler.getJobID());
                responsePersistTask.setApplicationId(this.taskHandler.getApplicationId());
                responsePersistTask.setWebUrl(this.taskHandler.getWebUrl());
                streamManagerSender.ask(responsePersistTask);
            }
        } catch (Exception e) {
            if(envType.equals("prod")) {
                ResponsePersistTask responsePersistTask = new ResponsePersistTask();
                responsePersistTask.setTaskID(Long.valueOf(streamTaskId));
                streamManagerSender.ask(responsePersistTask);
            }
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

    public ExecuteResponse executeSQL(EngineExecutorContext engineExecutorContext, TaskHandler taskHandler, String code, String envType) {
        SqlCommandParser.SqlCommandCall callSQL;
        ExecuteResponse  executeResponse;
        Optional<SqlCommandParser.SqlCommandCall> callOpt = SqlCommandParser.parse(code.trim(), true);
        if (!callOpt.isPresent()) {
            throw new SqlParseException("Unknown statement: " + code);
        } else {
            callSQL = callOpt.get();
        }
        //开发环境SELECT 需要等待结果集。
        if (callSQL.command == SqlCommandParser.SqlCommand.SELECT && envType.equals("dev")) {
            FlinkResultListener flinkResultListener = new FlinkResultListener(engineExecutorContext);
            progressList.add(flinkResultListener);
            executeResponse = taskHandler.execute(callSQL, flinkResultListener);
            //等待结果集
            while (!flinkResultListener.isFinished()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        } else {
            executeResponse = taskHandler.execute(callSQL);
        }
        return executeResponse;
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
        float totalSQLs =  engineExecutorContext.getTotalParagraph();
        float currentSQL = engineExecutorContext.getCurrentParagraph();
        float currentBegin = (currentSQL - 1) / totalSQLs;
        return currentBegin;
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
