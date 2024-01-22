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

package org.apache.linkis.storage.resultset;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetResultSetReader<K extends MetaData, V extends Record>
    extends ResultSetReader<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(ParquetResultSetReader.class);

  private FsPath fsPath;

  private final ResultSet<K, V> resultSet;

  private final InputStream inputStream;

  private MetaData metaData;

  private Record row;

  private ParquetReader<GenericRecord> parquetReader;

  private GenericRecord record;

  public ParquetResultSetReader(ResultSet resultSet, InputStream inputStream, FsPath fsPath)
      throws IOException {
    super(resultSet, inputStream);
    this.resultSet = resultSet;
    this.inputStream = inputStream;
    this.fsPath = fsPath;
    this.parquetReader =
        AvroParquetReader.<GenericRecord>builder(new Path(fsPath.getPath())).build();
    this.record = parquetReader.read();
  }

  @Override
  public MetaData getMetaData() {
    if (metaData == null) {
      try {
        List<Schema.Field> fields = record.getSchema().getFields();
        List<Column> columnList =
            fields.stream()
                .map(
                    field ->
                        new Column(
                            field.name(),
                            DataType.toDataType(field.schema().getType().getName()),
                            ""))
                .collect(Collectors.toList());

        metaData = new TableMetaData(columnList.toArray(new Column[0]));
      } catch (Exception e) {
        throw new RuntimeException("Failed to read parquet schema", e);
      }
    }
    return metaData;
  }

  @Override
  public int skip(int recordNum) throws IOException {
    if (recordNum < 0) return -1;

    for (int i = recordNum; i > 0; i--) {
      try {
        this.record = parquetReader.read();
      } catch (Throwable t) {
        return recordNum - i;
      }
    }
    return recordNum;
  }

  @Override
  public long getPosition() throws IOException {
    throw new UnsupportedOperationException("Storeage Unsupported type: getPosition");
  }

  @Override
  public long available() throws IOException {
    throw new UnsupportedOperationException("Storeage Unsupported type: available");
  }

  @Override
  public boolean hasNext() throws IOException {
    if (metaData == null) getMetaData();
    if (record == null) return false;
    ArrayList<Object> resultList = new ArrayList<>();
    TableMetaData tableMetaData = (TableMetaData) metaData;
    int length = tableMetaData.getColumns().length;
    for (int i = 0; i < length; i++) {
      resultList.add(record.get(i));
    }
    row = new TableRecord(resultList.toArray(new Object[0]));
    if (row == null) return false;
    return record != null;
  }

  @Override
  public Record getRecord() {
    if (metaData == null) throw new RuntimeException("Must read metadata first(必须先读取metadata)");
    if (row == null) {
      throw new RuntimeException(
          "Can't get the value of the field, maybe the IO stream has been read or has been closed!(拿不到字段的值，也许IO流已读取完毕或已被关闭！)");
    }
    try {
      this.record = parquetReader.read();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read parquet record", e);
    }
    return row;
  }

  @Override
  public void close() throws IOException {
    IOUtils.closeQuietly(inputStream);
    parquetReader.close();
  }
}
