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

package org.apache.linkis.engineconnplugin.flink.client.sql.operation.result;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParseException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonToken;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationContext;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.flink.table.types.logical.DateType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.logical.TimeType;
import org.apache.flink.table.types.logical.TimestampType;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_CHANGE_FLAGS;
import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_COLUMNS;
import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_DATA;
import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_RESULT_KIND;

/** Json deserializer for {@link ResultSet}. */
public class ResultSetJsonDeserializer extends StdDeserializer<ResultSet> {

  protected ResultSetJsonDeserializer() {
    super(ResultSet.class);
  }

  @Override
  public ResultSet deserialize(JsonParser jsonParser, DeserializationContext ctx)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    ResultKind resultKind;
    List<ColumnInfo> columns;
    List<Boolean> changeFlags = null;
    List<Row> data;

    JsonNode resultKindNode = node.get(FIELD_NAME_RESULT_KIND);
    if (resultKindNode != null) {
      JsonParser resultKindParser = node.get(FIELD_NAME_RESULT_KIND).traverse();
      resultKindParser.nextToken();
      resultKind = ctx.readValue(resultKindParser, ResultKind.class);
    } else {
      throw new JsonParseException(jsonParser, "Field resultKind must be provided");
    }

    JsonNode columnNode = node.get(FIELD_NAME_COLUMNS);
    if (columnNode != null) {
      JsonParser columnParser = node.get(FIELD_NAME_COLUMNS).traverse();
      columnParser.nextToken();
      columns = Arrays.asList(ctx.readValue(columnParser, ColumnInfo[].class));
    } else {
      throw new JsonParseException(jsonParser, "Field column must be provided");
    }

    JsonNode changeFlagNode = node.get(FIELD_NAME_CHANGE_FLAGS);
    if (changeFlagNode != null) {
      JsonParser changeFlagParser = changeFlagNode.traverse();
      changeFlagParser.nextToken();
      changeFlags = Arrays.asList(ctx.readValue(changeFlagParser, Boolean[].class));
    }

    JsonNode dataNode = node.get(FIELD_NAME_DATA);
    if (dataNode != null) {
      data = deserializeRows(columns, dataNode, ctx);
    } else {
      throw new JsonParseException(jsonParser, "Field data must be provided");
    }

    return ResultSet.builder()
        .resultKind(resultKind)
        .columns(columns)
        .data(data)
        .changeFlags(changeFlags)
        .build();
  }

  private List<Row> deserializeRows(
      List<ColumnInfo> columns, JsonNode dataNode, DeserializationContext ctx) throws IOException {
    if (!dataNode.isArray()) {
      throw new JsonParseException(
          dataNode.traverse(), "Expecting data to be an array but it's not");
    }

    List<RowType.RowField> fields = new ArrayList<>();
    for (ColumnInfo column : columns) {
      fields.add(new RowType.RowField(column.getName(), column.getLogicalType()));
    }
    RowType rowType = new RowType(fields);

    List<Row> data = new ArrayList<>();
    for (JsonNode rowNode : dataNode) {
      data.add(deserializeRow(rowType, rowNode, ctx));
    }
    return data;
  }

  private LocalDate deserializeLocalDate(JsonParser parser, DeserializationContext ctx)
      throws IOException {
    return LocalDate.parse(ctx.readValue(parser, String.class));
  }

  private LocalTime deserializeLocalTime(JsonParser parser, DeserializationContext ctx)
      throws IOException {
    return LocalTime.parse(ctx.readValue(parser, String.class));
  }

  private LocalDateTime deserializeLocalDateTime(JsonParser parser, DeserializationContext ctx)
      throws IOException {
    return LocalDateTime.parse(ctx.readValue(parser, String.class));
  }

  private Row deserializeRow(RowType type, JsonNode node, DeserializationContext ctx)
      throws IOException {
    if (!node.isArray()) {
      throw new JsonParseException(node.traverse(), "Expecting row to be an array but it's not");
    }

    int fieldCount = type.getFieldCount();
    List<RowType.RowField> fields = type.getFields();
    Row row = new Row(fieldCount);

    int i = 0;
    for (JsonNode fieldNode : node) {
      if (i >= fieldCount) {
        throw new JsonParseException(
            node.traverse(), "Number of columns in the row is not consistent with column infos");
      }
      row.setField(i, deserializeObject(fields.get(i).getType(), fieldNode, ctx));
      i++;
    }
    if (i != fieldCount) {
      throw new JsonParseException(
          node.traverse(), "Number of columns in the row is not consistent with column infos");
    }

    return row;
  }

  private Object deserializeObject(LogicalType type, JsonNode node, DeserializationContext ctx)
      throws IOException {
    if (type instanceof RowType) {
      return deserializeRow((RowType) type, node, ctx);
    }

    JsonParser parser = node.traverse();
    parser.nextToken();
    if (parser.currentToken() == JsonToken.VALUE_NULL) {
      // we have to manually parse null value
      // as jackson refuses to deserialize null value to java objects
      return null;
    }

    if (type instanceof DateType) {
      return deserializeLocalDate(parser, ctx);
    } else if (type instanceof TimeType) {
      return deserializeLocalTime(parser, ctx);
    } else if (type instanceof TimestampType) {
      return deserializeLocalDateTime(parser, ctx);
    } else {
      return ctx.readValue(parser, type.getDefaultConversion());
    }
  }
}
