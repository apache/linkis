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
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.NonJobOperation;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ConstantNames;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext;

import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.types.Row;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.INVALID_SQL_STATEMENT;

/** Operation for EXPLAIN command. */
public class ExplainOperation implements NonJobOperation {
  private final ExecutionContext context;
  private final String statement;

  public ExplainOperation(FlinkEngineConnContext context, String statement) {
    this.context = context.getExecutionContext();
    this.statement = statement;
  }

  @Override
  public ResultSet execute() throws SqlExecutionException {
    final TableEnvironment tableEnv = context.getTableEnvironment();
    // translate
    try {
      String explanation = context.wrapClassLoader(() -> tableEnv.explainSql(statement));
      return ResultSet.builder()
          .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
          .columns(
              ColumnInfo.create(
                  ConstantNames.EXPLAIN_RESULT, new VarCharType(false, explanation.length())))
          .data(Row.of(explanation))
          .build();
    } catch (SqlExecutionException t) {
      throw t;
    } catch (Exception t) {
      // catch everything such that the query does not crash the executor
      throw new SqlExecutionException(INVALID_SQL_STATEMENT.getErrorDesc(), t);
    }
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
}
