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

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonGenerator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_CHANGE_FLAGS;
import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_COLUMNS;
import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_DATA;
import static org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet.FIELD_NAME_RESULT_KIND;

/** Json serializer for {@link ResultSet}. */
public class ResultSetJsonSerializer extends StdSerializer<ResultSet> {

  protected ResultSetJsonSerializer() {
    super(ResultSet.class);
  }

  @Override
  public void serialize(
      ResultSet resultSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeStartObject();

    serializerProvider.defaultSerializeField(
        FIELD_NAME_RESULT_KIND, resultSet.getResultKind(), jsonGenerator);
    serializerProvider.defaultSerializeField(
        FIELD_NAME_COLUMNS, resultSet.getColumns(), jsonGenerator);

    jsonGenerator.writeFieldName(FIELD_NAME_DATA);
    jsonGenerator.writeStartArray();
    for (Row row : resultSet.getData()) {
      serializeRow(row, jsonGenerator, serializerProvider);
    }
    jsonGenerator.writeEndArray();

    if (resultSet.getChangeFlags().isPresent()) {
      serializerProvider.defaultSerializeField(
          FIELD_NAME_CHANGE_FLAGS, resultSet.getChangeFlags().get(), jsonGenerator);
    }

    jsonGenerator.writeEndObject();
  }

  private void serializeLocalDate(LocalDate localDate, JsonGenerator jsonGenerator)
      throws IOException {
    jsonGenerator.writeString(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }

  private void serializeLocalTime(LocalTime localTime, JsonGenerator jsonGenerator)
      throws IOException {
    jsonGenerator.writeString(localTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
  }

  private void serializeLocalDateTime(LocalDateTime localDateTime, JsonGenerator jsonGenerator)
      throws IOException {
    jsonGenerator.writeString(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }

  private void serializeRow(
      Row row, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeStartArray();
    for (int i = 0; i < row.getArity(); i++) {
      serializeObject(row.getField(i), jsonGenerator, serializerProvider);
    }
    jsonGenerator.writeEndArray();
  }

  private void serializeObject(
      Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (o instanceof LocalDate) {
      serializeLocalDate((LocalDate) o, jsonGenerator);
    } else if (o instanceof LocalTime) {
      serializeLocalTime((LocalTime) o, jsonGenerator);
    } else if (o instanceof LocalDateTime) {
      serializeLocalDateTime((LocalDateTime) o, jsonGenerator);
    } else if (o instanceof Row) {
      serializeRow((Row) o, jsonGenerator, serializerProvider);
    } else {
      serializerProvider.defaultSerializeValue(o, jsonGenerator);
    }
  }
}
