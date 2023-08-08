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

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.WatermarkSpec;
import org.apache.flink.table.types.logical.BooleanType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.NO_TABLE_FOUND;

/** Operation for DESCRIBE TABLE command. */
public class DescribeTableOperation implements NonJobOperation {
  private final ExecutionContext context;
  private final String tableName;

  public DescribeTableOperation(FlinkEngineConnContext context, String tableName) {
    this.context = context.getExecutionContext();
    this.tableName = tableName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ResultSet execute() throws SqlExecutionException {
    // the implementation should be in sync with Flink, see FLINK-17112
    final TableEnvironment tableEnv = context.getTableEnvironment();
    TableSchema schema;
    try {
      schema = context.wrapClassLoader(() -> tableEnv.from(tableName).getSchema());
    } catch (Throwable t) {
      // catch everything such that the query does not crash the executor
      throw new SqlExecutionException(NO_TABLE_FOUND.getErrorDesc(), t);
    }

    Map<String, String> fieldToWatermark = new HashMap<>();
    for (WatermarkSpec spec : schema.getWatermarkSpecs()) {
      fieldToWatermark.put(spec.getRowtimeAttribute(), spec.getWatermarkExpr());
    }

    Map<String, String> fieldToPrimaryKey = new HashMap<>();
    if (schema.getPrimaryKey().isPresent()) {
      List<String> columns = schema.getPrimaryKey().get().getColumns();
      String primaryKey = "PRI(" + String.join(", ", columns) + ")";
      for (String column : columns) {
        fieldToPrimaryKey.put(column, primaryKey);
      }
    }

    List<TableColumn> columns = schema.getTableColumns();
    List<Row> data = new ArrayList<>();
    for (TableColumn column : columns) {
      LogicalType logicalType = column.getType().getLogicalType();

      String name = column.getName();
      String type = StringUtils.removeEnd(logicalType.toString(), " NOT NULL");
      boolean isNullable = logicalType.isNullable();
      String key = fieldToPrimaryKey.getOrDefault(column.getName(), null);
      String computedColumn = column.getName();
      String watermark = fieldToWatermark.getOrDefault(column.getName(), null);

      data.add(Row.of(name, type, isNullable, key, computedColumn, watermark));
    }

    return ResultSet.builder()
        .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
        .columns(
            ColumnInfo.create(ConstantNames.DESCRIBE_NAME, DataTypes.STRING().getLogicalType()),
            ColumnInfo.create(ConstantNames.DESCRIBE_TYPE, DataTypes.STRING().getLogicalType()),
            ColumnInfo.create(ConstantNames.DESCRIBE_NULL, new BooleanType()),
            ColumnInfo.create(ConstantNames.DESCRIBE_KEY, DataTypes.STRING().getLogicalType()),
            ColumnInfo.create(
                ConstantNames.DESCRIBE_COMPUTED_COLUMN, DataTypes.STRING().getLogicalType()),
            ColumnInfo.create(
                ConstantNames.DESCRIBE_WATERMARK, DataTypes.STRING().getLogicalType()))
        .data(data)
        .build();
  }
}
