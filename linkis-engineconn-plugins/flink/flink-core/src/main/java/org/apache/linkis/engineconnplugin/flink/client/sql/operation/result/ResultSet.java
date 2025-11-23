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

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.flink.types.Row;
import org.apache.flink.util.Preconditions;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A set of one statement execution result containing result kind, column infos, rows of data and
 * change flags for streaming mode.
 */
@JsonSerialize(using = ResultSetJsonSerializer.class)
@JsonDeserialize(using = ResultSetJsonDeserializer.class)
public class ResultSet {
  static final String FIELD_NAME_RESULT_KIND = "result_kind";
  static final String FIELD_NAME_COLUMNS = "columns";
  static final String FIELD_NAME_DATA = "data";
  static final String FIELD_NAME_CHANGE_FLAGS = "change_flags";

  private final ResultKind resultKind;
  private final List<ColumnInfo> columns;
  private final List<Row> data;

  // null in batch mode
  //
  // list of boolean in streaming mode,
  // true if the corresponding row is an append row, false if its a retract row
  private final List<Boolean> changeFlags;

  private ResultSet(
      ResultKind resultKind,
      List<ColumnInfo> columns,
      List<Row> data,
      @Nullable List<Boolean> changeFlags) {
    this.resultKind = Preconditions.checkNotNull(resultKind, "resultKind must not be null");
    this.columns = Preconditions.checkNotNull(columns, "columns must not be null");
    this.data = Preconditions.checkNotNull(data, "data must not be null");
    if (!data.isEmpty()) {
      Preconditions.checkArgument(
          columns.size() == data.get(0).getArity(),
          "the size of columns and the number of fields in the row should be equal");
    }
    this.changeFlags = changeFlags;
    if (changeFlags != null) {
      Preconditions.checkArgument(
          data.size() == changeFlags.size(),
          "the size of data and the size of changeFlags should be equal");
    }
  }

  public ResultKind getResultKind() {
    return resultKind;
  }

  public List<ColumnInfo> getColumns() {
    return columns;
  }

  public List<Row> getData() {
    return data;
  }

  public Optional<List<Boolean>> getChangeFlags() {
    return Optional.ofNullable(changeFlags);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultSet resultSet = (ResultSet) o;
    return resultKind.equals(resultSet.resultKind)
        && columns.equals(resultSet.columns)
        && data.equals(resultSet.data)
        && Objects.equals(changeFlags, resultSet.changeFlags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resultKind, columns, data, changeFlags);
  }

  @Override
  public String toString() {
    return "ResultSet{"
        + "resultKind="
        + resultKind
        + ", columns="
        + columns
        + ", data="
        + data
        + ", changeFlags="
        + changeFlags
        + '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link ResultSet}. */
  public static class Builder {
    private ResultKind resultKind = null;
    private List<ColumnInfo> columns = null;
    private List<Row> data = null;
    private List<Boolean> changeFlags = null;

    private Builder() {}

    /** Set {@link ResultKind}. */
    public Builder resultKind(ResultKind resultKind) {
      this.resultKind = resultKind;
      return this;
    }

    /** Set {@link ColumnInfo}s. */
    public Builder columns(ColumnInfo... columns) {
      this.columns = Arrays.asList(columns);
      return this;
    }

    /** Set {@link ColumnInfo}s. */
    public Builder columns(List<ColumnInfo> columns) {
      this.columns = columns;
      return this;
    }

    /** Set data. */
    public Builder data(List<Row> data) {
      this.data = data;
      return this;
    }

    /** Set data. */
    public Builder data(Row... data) {
      this.data = Arrays.asList(data);
      return this;
    }

    /** Set change flags. */
    public Builder changeFlags(List<Boolean> changeFlags) {
      this.changeFlags = changeFlags;
      return this;
    }

    /** Returns a {@link ResultSet} instance. */
    public ResultSet build() {
      return new ResultSet(resultKind, columns, data, changeFlags);
    }
  }
}
