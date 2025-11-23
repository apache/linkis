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
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.AbstractJobOperation;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;
import org.apache.linkis.engineconnplugin.flink.listener.RowsType;

import org.apache.flink.api.common.JobID;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.types.logical.BigIntType;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;

import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.INVALID_SQL_STATEMENT;

/** Operation for INSERT command. */
public class InsertOperation extends AbstractJobOperation {
  private static final Logger LOG = LoggerFactory.getLogger(InsertOperation.class);

  private final String statement;
  private final List<ColumnInfo> columnInfos;

  private boolean fetched = false;

  public InsertOperation(FlinkEngineConnContext context, String statement, String tableIdentifier) {
    super(context);
    this.statement = statement;
    this.columnInfos =
        Collections.singletonList(ColumnInfo.create(tableIdentifier, new BigIntType(false)));
  }

  @Override
  protected JobID submitJob() throws SqlExecutionException {
    return executeUpdateInternal(context.getExecutionContext());
  }

  @Override
  protected Optional<Tuple2<List<Row>, List<Boolean>>> fetchJobResults() {
    if (fetched) {
      return Optional.empty();
    } else {
      fetched = true;
      return Optional.of(
          Tuple2.of(Collections.singletonList(Row.of((long) Statement.SUCCESS_NO_INFO)), null));
    }
  }

  @Override
  protected List<ColumnInfo> getColumnInfos() {
    return columnInfos;
  }

  private JobID executeUpdateInternal(ExecutionContext executionContext)
      throws SqlExecutionException {
    TableEnvironment tableEnv = executionContext.getTableEnvironment();
    // parse and validate statement
    TableResult tableResult;
    try {
      tableResult = executionContext.wrapClassLoader(() -> tableEnv.executeSql(statement));
    } catch (Exception t) {
      LOG.error(String.format("Invalid SQL query, sql is: %s.", statement), t);
      // catch everything such that the statement does not crash the executor
      throw new SqlExecutionException(INVALID_SQL_STATEMENT.getErrorDesc(), t);
    }
    asyncNotify(tableResult);
    return tableResult.getJobClient().get().getJobID();
  }

  protected void asyncNotify(TableResult tableResult) {
    CompletableFuture.completedFuture(tableResult)
        .thenApply(
            result -> {
              CloseableIterator<Row> iterator = result.collect();
              int affected = 0;
              while (iterator.hasNext()) {
                Row row = iterator.next();
                affected = Integer.parseInt(row.getField(0).toString());
              }
              return affected;
            })
        .whenComplete(
            (affected, throwable) -> {
              if (throwable != null) {
                getFlinkStatusListeners()
                    .forEach(
                        listener ->
                            listener.onFailed(
                                "Error while submitting job.", throwable, RowsType.Affected()));
              } else {
                getFlinkStatusListeners()
                    .forEach(listener -> listener.onSuccess(affected, RowsType.Affected()));
              }
            });
  }
}
