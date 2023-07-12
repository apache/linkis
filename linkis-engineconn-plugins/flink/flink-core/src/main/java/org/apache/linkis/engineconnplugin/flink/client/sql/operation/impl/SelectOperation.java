/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.flink.client.sql.operation.impl;

import org.apache.linkis.engineconnplugin.flink.client.context.ExecutionContext;
import org.apache.linkis.engineconnplugin.flink.client.result.AbstractResult;
import org.apache.linkis.engineconnplugin.flink.client.result.BatchResult;
import org.apache.linkis.engineconnplugin.flink.client.result.ChangelogResult;
import org.apache.linkis.engineconnplugin.flink.client.result.ResultUtil;
import org.apache.linkis.engineconnplugin.flink.client.result.TypedResult;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.JobExecutionException;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.AbstractJobOperation;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;

import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.utils.LogicalTypeUtils;
import org.apache.flink.table.types.utils.DataTypeUtils;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.*;

/** Operation for SELECT command. */
public class SelectOperation extends AbstractJobOperation {

  private static final Logger LOG = LoggerFactory.getLogger(SelectOperation.class);

  private final String query;

  private AbstractResult<?, ?> result;

  private TableSchema resultSchema;

  private List<ColumnInfo> columnInfos;

  public SelectOperation(FlinkEngineConnContext context, String query) {
    super(context);
    this.query = query;
    this.noMoreResult = false;
  }

  @Override
  protected JobID submitJob() throws SqlExecutionException {
    JobID jobId = executeQueryInternal(context.getExecutionContext(), query);
    List<TableColumn> resultSchemaColumns = resultSchema.getTableColumns();
    columnInfos = new ArrayList<>();
    for (TableColumn column : resultSchemaColumns) {
      columnInfos.add(ColumnInfo.create(column.getName(), column.getType().getLogicalType()));
    }
    return jobId;
  }

  @Override
  protected void cancelJobInternal() throws JobExecutionException {
    LOG.info("Start to cancel job {} and result retrieval.", getJobId());
    result.close();
    super.cancelJobInternal();
  }

  @Override
  protected Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults()
      throws SqlExecutionException {
    Optional<Tuple2<List<Row>, List<Boolean>>> ret;
    synchronized (lock) {
      if (result == null) {
        LOG.error("The job for this query has been canceled.");
        throw new SqlExecutionException(QUERY_CANCELED.getErrorDesc());
      }

      if (this.result instanceof ChangelogResult) {
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

  private Optional<Tuple2<List<Row>, List<Boolean>>> fetchBatchResult()
      throws SqlExecutionException {
    BatchResult<?> batchResult = (BatchResult<?>) this.result;
    TypedResult<List<Row>> typedResult = batchResult.retrieveChanges();
    if (typedResult.getType() == TypedResult.ResultType.PAYLOAD) {
      List<Row> payload = typedResult.getPayload();
      return Optional.of(Tuple2.of(payload, null));
    } else {
      return Optional.of(Tuple2.of(Collections.emptyList(), null));
    }
  }

  private Optional<Tuple2<List<Row>, List<Boolean>>> fetchStreamingResult()
      throws SqlExecutionException {
    ChangelogResult changLogResult = (ChangelogResult) this.result;
    TypedResult<List<Tuple2<Boolean, Row>>> typedResult = changLogResult.retrieveChanges();
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

  private JobID executeQueryInternal(ExecutionContext executionContext, String query)
      throws SqlExecutionException {
    // create table
    final Table table =
        createTable(executionContext, executionContext.getTableEnvironment(), query);
    boolean isChangelogResult = executionContext.getEnvironment().getExecution().inStreamingMode();
    // initialize result
    resultSchema = removeTimeAttributes(table.getSchema());
    if (isChangelogResult) {
      result =
          ResultUtil.createChangelogResult(
              executionContext.getFlinkConfig(),
              executionContext.getEnvironment(),
              resultSchema,
              executionContext.getExecutionConfig());
    } else {
      executionContext.getExecutionConfig().setExecutionMode(ExecutionMode.BATCH);
      result = ResultUtil.createBatchResult(resultSchema, executionContext.getExecutionConfig());
    }
    result.setFlinkListeners(getFlinkListeners());
    final String tableName =
        String.format("_tmp_table_%s", UUID.randomUUID().toString().replace("-", ""));
    TableResult tableResult;
    try {
      // writing to a sink requires an optimization step that might reference UDFs during code
      // compilation
      tableResult =
          executionContext.wrapClassLoader(
              tableEnv -> {
                tableEnv.registerTableSinkInternal(tableName, result.getTableSink());
                return table.executeInsert(tableName);
              });
    } catch (Exception t) {
      // the result needs to be closed as long as
      // it not stored in the result store
      result.close();
      LOG.error(String.format("Invalid SQL query, sql is %s.", query), t);
      // catch everything such that the query does not crash the executor
      throw new SqlExecutionException(INVALID_SQL_QUERY.getErrorDesc(), t);
    } finally {
      // Remove the temporal table object.
      executionContext.wrapClassLoader(tableEnv -> tableEnv.dropTemporaryTable(tableName));
    }

    return tableResult
        .getJobClient()
        .map(
            jobClient -> {
              JobID jobId = jobClient.getJobID();
              LOG.info("Submit flink job: {} successfully.", jobId);
              // start result retrieval
              result.startRetrieval(jobClient);
              return jobId;
            })
        .orElseThrow(() -> new SqlExecutionException(NOT_JOB_ASD_ADMIN.getErrorDesc()));
  }

  /** Creates a table using the given query in the given table environment. */
  private Table createTable(ExecutionContext context, TableEnvironment tableEnv, String selectQuery)
      throws SqlExecutionException {
    // parse and validate query
    try {
      return context.wrapClassLoader(() -> tableEnv.sqlQuery(selectQuery));
    } catch (Exception t) {
      // catch everything such that the query does not crash the executor
      throw new SqlExecutionException(INVALID_SQL_STATEMENT.getErrorDesc(), t);
    }
  }

  private TableSchema removeTimeAttributes(TableSchema schema) {
    final TableSchema.Builder builder = TableSchema.builder();
    for (int i = 0; i < schema.getFieldCount(); i++) {
      final DataType dataType = schema.getFieldDataTypes()[i];
      final DataType convertedType =
          DataTypeUtils.replaceLogicalType(
              dataType, LogicalTypeUtils.removeTimeAttributes(dataType.getLogicalType()));
      builder.field(schema.getFieldNames()[i], convertedType);
    }
    return builder.build();
  }
}
