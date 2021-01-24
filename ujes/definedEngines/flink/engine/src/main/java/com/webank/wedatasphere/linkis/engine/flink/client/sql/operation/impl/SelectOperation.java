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
import com.webank.wedatasphere.linkis.engine.flink.client.result.BatchResult;
import com.webank.wedatasphere.linkis.engine.flink.client.result.ChangelogResult;
import com.webank.wedatasphere.linkis.engine.flink.client.result.Result;
import com.webank.wedatasphere.linkis.engine.flink.client.result.ResultDescriptor;
import com.webank.wedatasphere.linkis.engine.flink.client.result.ResultUtil;
import com.webank.wedatasphere.linkis.engine.flink.client.result.TypedResult;
import com.webank.wedatasphere.linkis.engine.flink.common.ResultListener;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlExecutionException;
import com.webank.wedatasphere.linkis.engine.flink.exception.SqlGatewayException;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.dag.Pipeline;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.client.gateway.local.ProgramDeployer;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.table.types.logical.utils.LogicalTypeUtils;
import org.apache.flink.table.types.utils.DataTypeUtils;
import org.apache.flink.types.Row;

import org.mortbay.util.ajax.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Operation for SELECT command.
 */
public class SelectOperation extends AbstractJobOperation {
    private static final Logger LOG = LoggerFactory.getLogger(SelectOperation.class);

    private final String query;

    private ResultDescriptor resultDescriptor;

    private List<ColumnInfo> columnInfos;

    private volatile boolean noMoreResult;

    public SelectOperation(SessionContext context, String query) {
        super(context);
        this.query = query;
        this.noMoreResult = false;
    }

    @Override
    public ResultSet execute() {
        resultDescriptor = executeQueryInternal(context.getExecutionContext(), query);
        jobId = resultDescriptor.getJobClient().getJobID();
        List<TableColumn> resultSchemaColumns = resultDescriptor.getResultSchema().getTableColumns();
        columnInfos = new ArrayList<>();
        for (TableColumn column : resultSchemaColumns) {
            columnInfos.add(ColumnInfo.create(column.getName(), column.getType().getLogicalType()));
        }
        String applicationId = this.clusterDescriptorAdapter.getClusterID().toString();
        String webInterfaceUrl = this.clusterDescriptorAdapter.getWebInterfaceUrl();
        return ResultSet.builder()
                .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
                .columns(
                        ColumnInfo.create("jobId", new VarCharType(false, jobId.toString().length())),
                        ColumnInfo.create("applicationId", new VarCharType(false, applicationId.length())),
                        ColumnInfo.create("webInterfaceUrl", new VarCharType(false, webInterfaceUrl.length()))

                )
                .data(Row.of(jobId.toString(), Row.of(applicationId), Row.of(webInterfaceUrl)))
                .build();
    }

    @Override
    protected void cancelJobInternal() {
        LOG.info("Session: {}. Start to cancel job {} and result retrieval.", sessionId, jobId);
        resultDescriptor.getResult().close();
        // ignore if there is no more result
        // the job might has finished earlier. it's hard to say whether it need to be canceled,
        // so the clients should be care for the exceptions ???
        if (noMoreResult) {
            return;
        }

        clusterDescriptorAdapter.cancelJob();
    }

    @Override
    protected Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults() {
        Optional<Tuple2<List<Row>, List<Boolean>>> ret;
        synchronized (lock) {
            if (resultDescriptor == null) {
                LOG.error("Session: {}. The job for this query has been canceled.", sessionId);
                throw new SqlGatewayException("The job for this query has been canceled.");
            }

            if (resultDescriptor.isChangelogResult()) {
                ret = fetchStreamingResult();
            } else {
                ret = fetchBatchResult();
            }
        }
        return ret;
    }

    @Override
    protected List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    private Optional<Tuple2<List<Row>, List<Boolean>>> fetchBatchResult() {
        BatchResult<?> result = (BatchResult<?>) resultDescriptor.getResult();
        TypedResult<List<Row>> typedResult = result.retrieveChanges();
        if (typedResult.getType() == TypedResult.ResultType.PAYLOAD) {
            List<Row> payload = typedResult.getPayload();
            return Optional.of(Tuple2.of(payload, null));
        } else {
            return Optional.of(Tuple2.of(Collections.emptyList(), null));
        }
    }

    private Optional<Tuple2<List<Row>, List<Boolean>>> fetchStreamingResult() {
        ChangelogResult<?> result = (ChangelogResult<?>) resultDescriptor.getResult();
        TypedResult<List<Tuple2<Boolean, Row>>> typedResult = result.retrieveChanges();
        if (typedResult.getType() == TypedResult.ResultType.EOS) {
            return Optional.of(Tuple2.of(Collections.emptyList(), Collections.emptyList()));

        } else if (typedResult.getType() == TypedResult.ResultType.PAYLOAD) {
            List<Tuple2<Boolean, Row>> payload = typedResult.getPayload();
            List<Row> data = new ArrayList<>();
            List<Boolean> changeFlags = new ArrayList<>();
            for (Tuple2<Boolean, Row> tuple : payload) {
                data.add(tuple.f1);
                changeFlags.add(tuple.f0);
            }
            return Optional.of(Tuple2.of(data, changeFlags));
        } else {
            return Optional.of(Tuple2.of(Collections.emptyList(), Collections.emptyList()));
        }
    }

    private <C> ResultDescriptor executeQueryInternal(ExecutionContext<C> executionContext, String query) {
        // create table
        final Table table = createTable(executionContext, executionContext.getTableEnvironment(), query);
        boolean isChangelogResult = executionContext.getEnvironment().getExecution().inStreamingMode();
        // initialize result
        final Result<C, ?> result;
        if (isChangelogResult) {
            result = ResultUtil.createChangelogResult(
                    executionContext.getFlinkConfig(),
                    executionContext.getEnvironment(),
                    removeTimeAttributes(table.getSchema()),
                    executionContext.getExecutionConfig(),
                    executionContext.getClassLoader());
        } else {
            result = ResultUtil.createBatchResult(
                    removeTimeAttributes(table.getSchema()),
                    executionContext.getExecutionConfig(),
                    executionContext.getClassLoader());
        }

        String jobName = getJobName(query);
        final String tableName = String.format("_tmp_table_%s", UUID.randomUUID().toString().replace("-", ""));
        final Pipeline pipeline;
        try {
            // writing to a sink requires an optimization step that might reference UDFs during code compilation
            executionContext.wrapClassLoader(() -> {
                executionContext.getTableEnvironment().registerTableSink(tableName, result.getTableSink());
                table.insertInto(tableName);
                return null;
            });
            pipeline = executionContext.createPipeline(jobName);
        } catch (Throwable t) {
            // the result needs to be closed as long as
            // it not stored in the result store
            result.close();
            LOG.error(String.format("Session: %s. Invalid SQL query.", sessionId), t);
            // catch everything such that the query does not crash the executor
            throw new SqlExecutionException("Invalid SQL query.", t);
        } finally {
            // Remove the temporal table object.
            executionContext.wrapClassLoader(() -> {
                executionContext.getTableEnvironment().dropTemporaryTable(tableName);
                return null;
            });
        }

        // create a copy so that we can change settings without affecting the original config
        //Configuration configuration = new Configuration(executionContext.getFlinkConfig());
        // for queries we wait for the job result, so run in attached mode
        //configuration.set(DeploymentOptions.ATTACHED, true);
        // shut down the cluster if the shell is closed
        //configuration.set(DeploymentOptions.SHUTDOWN_IF_ATTACHED, true);
        context.getExecutionContext().getFlinkConfig().setBoolean(DeploymentOptions.ATTACHED, true);
        context.getExecutionContext().getFlinkConfig().setBoolean(DeploymentOptions.SHUTDOWN_IF_ATTACHED, true);
        LOG.info("deployer flink config" + JSON.toString(context.getExecutionContext().getFlinkConfig().toString()));
        final com.webank.wedatasphere.linkis.engine.flink.client.deployment.ProgramDeployer deployer = new com.webank.wedatasphere.linkis.engine.flink.client.deployment.ProgramDeployer(context.getExecutionContext().getFlinkConfig(), jobName, pipeline);
        JobClient jobClient;
        // blocking deployment
        try {
            jobClient = deployer.deploy().get();
        } catch (Exception e) {
            LOG.error(String.format("Session: %s. Error running SQL job.", sessionId), e);
            throw new RuntimeException("Error running SQL job.", e);
        }

        JobID jobID = jobClient.getJobID();
        this.clusterDescriptorAdapter =
                ClusterDescriptorAdapterFactory.create(context.getExecutionContext(), sessionId, jobID);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cluster Descriptor Adapter: {}", clusterDescriptorAdapter);
        }

        LOG.info("Session: {}. Submit flink job: {} successfully, query: ", sessionId, jobID.toString(), query);
        // start result retrieval
        result.startRetrieval(jobClient);
        return new ResultDescriptor(
                result,
                isChangelogResult,
                removeTimeAttributes(table.getSchema()),
                jobClient);
    }


    /**
     * Creates a table using the given query in the given table environment.
     */
    private <C> Table createTable(ExecutionContext<C> context, TableEnvironment tableEnv, String selectQuery) {
        // parse and validate query
        try {
            return context.wrapClassLoader(() -> tableEnv.sqlQuery(selectQuery));
        } catch (Throwable t) {
            // catch everything such that the query does not crash the executor
            throw new SqlExecutionException("Invalid SQL statement.", t);
        }
    }

    private TableSchema removeTimeAttributes(TableSchema schema) {
        final TableSchema.Builder builder = TableSchema.builder();
        for (int i = 0; i < schema.getFieldCount(); i++) {
            final DataType dataType = schema.getFieldDataTypes()[i];
            final DataType convertedType = DataTypeUtils.replaceLogicalType(
                    dataType,
                    LogicalTypeUtils.removeTimeAttributes(dataType.getLogicalType()));
            builder.field(schema.getFieldNames()[i], convertedType);
        }
        return builder.build();
    }

    @Override
    public void addResultListener(List<ResultListener> listenerList) {
        resultDescriptor.getResult().setResultListener(listenerList);
    }
}
