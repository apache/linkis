package com.webank.wedatasphere.linkis.engine.flink.executor.handler;

import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.DefaultContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.session.SessionManager;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.exception.IllegalArgumentException;
import com.webank.wedatasphere.linkis.engine.flink.executor.FlinkResultListener;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteResponse;
import org.apache.flink.api.java.tuple.Tuple2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: flink-parent
 * @description:
 * @author: hui zhu
 * @create: 2020-12-16 11:45
 */
public interface TaskHandler {

     void initFlinkContext(EngineExecutorContext engineExecutorContext,DefaultContext defaultContext, SessionManager sessionManager,  Map<String, String> jobExecuteParams) throws IllegalArgumentException;

     ExecuteResponse execute();

     ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL) ;

     ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL, FlinkResultListener flinkResultListener) ;

}