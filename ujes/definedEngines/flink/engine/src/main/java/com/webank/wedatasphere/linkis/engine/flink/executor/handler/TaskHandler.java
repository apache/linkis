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
 * Created by liangqilang on 01 20, 2021
 */
public interface TaskHandler {

     void initFlinkContext(EngineExecutorContext engineExecutorContext,DefaultContext defaultContext, SessionManager sessionManager,  Map<String, String> jobExecuteParams) throws IllegalArgumentException;

     ExecuteResponse execute();

     ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL) ;

     ExecuteResponse execute(SqlCommandParser.SqlCommandCall callSQL, FlinkResultListener flinkResultListener) ;

}