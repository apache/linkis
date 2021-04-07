/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.flink.client.sql.session;

import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import org.apache.flink.table.client.config.entries.*;
import com.webank.wedatasphere.linkis.engine.flink.client.context.SessionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.JobOperation;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.Operation;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.OperationFactory;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser;
import com.webank.wedatasphere.linkis.engine.flink.client.utils.SqlCommandParser.SqlCommandCall;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlParseException;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.planner.plan.metadata.FlinkDefaultRelMetadataProvider;

import org.apache.calcite.rel.metadata.JaninoRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class Session {
    private static final Logger LOG = LoggerFactory.getLogger(Session.class);

    private final SessionContext context;
    private final String sessionId;
    private long lastVisitedTime;
    private final Map<JobID, JobOperation> jobOperations;

    public Session(SessionContext context) {
        this.context = context;
        this.sessionId = context.getSessionId();
        this.lastVisitedTime = System.currentTimeMillis();
        this.jobOperations = new ConcurrentHashMap<>();
    }

    public void touch() {
        lastVisitedTime = System.currentTimeMillis();
    }

    public long getLastVisitedTime() {
        return lastVisitedTime;
    }

    public SessionContext getContext() {
        return context;
    }

    public Tuple2<ResultSet, SqlCommandParser.SqlCommand> runStatement(SqlCommandCall call) {
        // TODO: This is a temporary fix to avoid NPE.
        //  In SQL gateway, TableEnvironment is created and used by different threads, thus causing this problem.
        RelMetadataQuery.THREAD_PROVIDERS
                .set(JaninoRelMetadataProvider.of(FlinkDefaultRelMetadataProvider.INSTANCE()));
        Operation operation = OperationFactory.createOperation(call, context);
        ResultSet resultSet = operation.execute();

        if (operation instanceof JobOperation) {
            JobOperation jobOperation = (JobOperation) operation;
            jobOperations.put(jobOperation.getJobId(), jobOperation);
        }
        return Tuple2.of(resultSet, call.command);
    }

    public JobStatus getJobStatus(JobID jobId) {
        LOG.info("Session: {}, get status for job: {}", sessionId, jobId);
        return getJobOperation(jobId).getJobStatus();
    }

    public void cancelJob(JobID jobId) {
        LOG.info("Session: {}, cancel job: {}", sessionId, jobId);
        getJobOperation(jobId).cancelJob();
        jobOperations.remove(jobId);
    }

    public Optional<ResultSet> getJobResult(JobID jobId) {
        LOG.info("Session: {}, get result for job: {}",
                sessionId, jobId);
        return getJobOperation(jobId).getJobResult();
    }

    private JobOperation getJobOperation(JobID jobId) {
        JobOperation jobOperation = jobOperations.get(jobId);
        if (jobOperation == null) {
            String msg = String.format("Job: %s does not exist in current session: %s.", jobId, sessionId);
            LOG.error(msg);
            throw new SqlGatewayException(msg);
        } else {
            return jobOperation;
        }
    }

    public  void addResultListener(JobID jobId, List<ResultListener> resultListenerList){
        getJobOperation(jobId).addResultListener(resultListenerList);
    }

}
