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

package com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.impl;

import com.webank.wedatasphere.linkis.engine.flink.client.context.ExecutionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.context.SessionContext;
import com.webank.wedatasphere.linkis.engine.flink.client.deployment.ClusterDescriptorAdapterFactory;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.AbstractJobOperation;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ColumnInfo;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ConstantNames;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultKind;
import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlExecutionException;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.dag.Pipeline;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.client.gateway.local.ProgramDeployer;
import org.apache.flink.table.types.logical.BigIntType;
import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.types.Row;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Operation for INSERT command.
 */
public class InsertOperation extends AbstractJobOperation {
    private static final Logger LOG = LoggerFactory.getLogger(InsertOperation.class);

    private final String statement;
    // insert into sql match pattern
    private static final Pattern INSERT_SQL_PATTERN = Pattern.compile("(INSERT\\s+(INTO|OVERWRITE).*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final List<ColumnInfo> columnInfos;

    private boolean fetched = false;

    public InsertOperation(SessionContext context, String statement, String tableIdentifier) {
        super(context);
        this.statement = statement;

        this.columnInfos = Collections.singletonList(
                ColumnInfo.create(tableIdentifier, new BigIntType(false)));
    }

    @Override
    public ResultSet execute() {
        jobId = executeUpdateInternal(context.getExecutionContext());
        String strJobId = jobId.toString();
        String applicationId = this.clusterDescriptorAdapter.getClusterID().toString();
        String webInterfaceUrl = this.clusterDescriptorAdapter.getWebInterfaceUrl();
        return ResultSet.builder()
                .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
                .columns(

                        ColumnInfo.create("jobId", new VarCharType(false, strJobId.length())),
                        ColumnInfo.create("applicationId", new VarCharType(false, applicationId.length())),
                        ColumnInfo.create("webInterfaceUrl", new VarCharType(false, webInterfaceUrl.length()))

                )
                .data(Row.of(strJobId), Row.of(applicationId), Row.of(webInterfaceUrl))
                .build();
    }

    @Override
    protected Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults() {
        if (fetched) {
            return Optional.empty();
        } else {
            // for session mode, we can get job status from JM, because JM is a long life service.
            // while for per-job mode, JM will be also destroy after the job is finished.
            // so it's hard to say whether the job is finished/canceled
            // or the job status is just inaccessible at that moment.
            // currently only yarn-per-job is supported,
            // and if the exception (thrown when getting job status) contains ApplicationNotFoundException,
            // we can say the job is finished.
            boolean isGloballyTerminalState = clusterDescriptorAdapter.isGloballyTerminalState();
            if (isGloballyTerminalState) {
                // TODO get affected_row_count for batch job
                fetched = true;
                return Optional.of(Tuple2.of(Collections.singletonList(
                        Row.of((long) Statement.SUCCESS_NO_INFO)), null));
            } else {
                // TODO throws exception if the job fails
                return Optional.of(Tuple2.of(Collections.emptyList(), null));
            }
        }
    }

    @Override
    protected List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    @Override
    protected void cancelJobInternal() {
        clusterDescriptorAdapter.cancelJob();
    }

    private <C> JobID executeUpdateInternal(ExecutionContext<C> executionContext) {
        TableEnvironment tableEnv = executionContext.getTableEnvironment();
        // parse and validate statement
        try {
            executionContext.wrapClassLoader(() -> {
                tableEnv.sqlUpdate(statement);
                return null;
            });
        } catch (Throwable t) {
            LOG.error(String.format("Session: %s. Invalid SQL query.", sessionId), t);
            // catch everything such that the statement does not crash the executor
            throw new SqlExecutionException("Invalid SQL update statement.", t);
        }

        //Todo: we should refactor following condition after TableEnvironment has support submit job directly.
        if (!INSERT_SQL_PATTERN.matcher(statement.trim()).matches()) {
            LOG.error("Session: {}. Only insert is supported now.", sessionId);
            throw new SqlExecutionException("Only insert is supported now");
        }

        String jobName = getJobName(statement);
        // create job graph with dependencies
        final Pipeline pipeline;
        try {
            pipeline = executionContext.wrapClassLoader(() -> executionContext.createPipeline(jobName));
        } catch (Throwable t) {
            LOG.error(String.format("Session: %s. Invalid SQL query.", sessionId), t);
            // catch everything such that the statement does not crash the executor
            throw new SqlExecutionException("Invalid SQL statement.", t);
        }

        // create a copy so that we can change settings without affecting the original config
        //Configuration configuration = new Configuration(executionContext.getFlinkConfig());
        // for update queries we don't wait for the job result, so run in detached mode

        context.getExecutionContext().getFlinkConfig().setBoolean(DeploymentOptions.ATTACHED, false);
        // create execution
        final ProgramDeployer deployer = new ProgramDeployer(context.getExecutionContext().getFlinkConfig(), jobName, pipeline);

        // blocking deployment
        try {
            JobClient jobClient = deployer.deploy().get();
            JobID jobID = jobClient.getJobID();
            this.clusterDescriptorAdapter =
                    ClusterDescriptorAdapterFactory.create(context.getExecutionContext(), sessionId, jobID);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cluster Descriptor Adapter: {}", clusterDescriptorAdapter);
            }
            return jobID;
        } catch (Exception e) {
            throw new RuntimeException("Error running SQL job.", e);
        }
    }

    @Override
    public void addResultListener(List<ResultListener> listenerList) {
        //not /support
    }
}
