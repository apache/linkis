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

package org.apache.linkis.engineconnplugin.flink.client.sql.operation;

import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ColumnInfo;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ConstantNames;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind;
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet;

import org.apache.flink.table.types.logical.VarCharType;
import org.apache.flink.types.Row;

import java.util.ArrayList;
import java.util.List;

public class OperationUtil {

  public static final ResultSet OK =
      ResultSet.builder()
          .resultKind(ResultKind.SUCCESS)
          .columns(ColumnInfo.create(ConstantNames.RESULT, new VarCharType(2)))
          .data(Row.of(ConstantNames.OK))
          .build();

  public static ResultSet singleStringToResultSet(String str, String columnName) {
    boolean isNullable;
    int length;

    if (str == null) {
      isNullable = true;
      length = VarCharType.DEFAULT_LENGTH;
    } else {
      isNullable = false;
      length = str.length();
    }

    return ResultSet.builder()
        .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
        .columns(ColumnInfo.create(columnName, new VarCharType(isNullable, length)))
        .data(Row.of(str))
        .build();
  }

  public static ResultSet stringListToResultSet(List<String> strings, String columnName) {
    List<Row> data = new ArrayList<>();
    boolean isNullable = false;
    int maxLength = VarCharType.DEFAULT_LENGTH;

    for (String str : strings) {
      if (str == null) {
        isNullable = true;
      } else {
        maxLength = Math.max(str.length(), maxLength);
        data.add(Row.of(str));
      }
    }

    return ResultSet.builder()
        .resultKind(ResultKind.SUCCESS_WITH_CONTENT)
        .columns(ColumnInfo.create(columnName, new VarCharType(isNullable, maxLength)))
        .data(data)
        .build();
  }
}
