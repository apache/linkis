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

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.orc.storage.ql.exec.vector.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrcResultSetReader<K extends MetaData, V extends Record>
    extends ResultSetReader<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(OrcResultSetReader.class);

  private FsPath fsPath;

  private final ResultSet<K, V> resultSet;

  private final InputStream inputStream;

  private MetaData metaData;

  private Record row;

  private RecordReader rows;

  private Reader reader;

  public OrcResultSetReader(ResultSet resultSet, InputStream inputStream, FsPath fsPath)
      throws IOException {
    super(resultSet, inputStream);
    this.resultSet = resultSet;
    this.inputStream = inputStream;
    this.fsPath = fsPath;
    this.reader =
        OrcFile.createReader(
            new Path(fsPath.getPath()), OrcFile.readerOptions(new Configuration()));
    this.rows = reader.rows();
  }

  @Override
  public MetaData getMetaData() {
    if (metaData == null) {
      try {
        List<String> fieldNames = reader.getSchema().getFieldNames();
        List<TypeDescription> typeDescriptions = reader.getSchema().getChildren();
        List<Column> columnList = new ArrayList<>();
        for (int i = 0; i < fieldNames.size(); i++) {
          Column column =
              new Column(
                  fieldNames.get(i),
                  DataType.toDataType(typeDescriptions.get(i).getCategory().getName()),
                  "");
          columnList.add(column);
        }

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
        hasNext();
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
    if (rows == null) return false;
    VectorizedRowBatch batch =
        reader.getSchema().createRowBatch(TypeDescription.RowBatchVersion.ORIGINAL, 1);
    TableMetaData tableMetaData = (TableMetaData) metaData;

    if (rows.nextBatch(batch)) {
      int rowNum = 0;
      Object[] rowData = new Object[tableMetaData.getColumns().length];
      for (int i = 0; i < batch.numCols; i++) {
        ColumnVector columnVector = batch.cols[i];
        if (columnVector instanceof BytesColumnVector) {
          BytesColumnVector vector = (BytesColumnVector) columnVector;
          rowData[i] = vector.toString(rowNum);
        } else if (columnVector instanceof Decimal64ColumnVector) {
          Decimal64ColumnVector vector = (Decimal64ColumnVector) columnVector;
          rowData[i] = vector.vector[rowNum];
        } else if (columnVector instanceof DecimalColumnVector) {
          DecimalColumnVector vector = (DecimalColumnVector) columnVector;
          rowData[i] = vector.vector[rowNum];
        } else if (columnVector instanceof DoubleColumnVector) {
          DoubleColumnVector vector = (DoubleColumnVector) columnVector;
          rowData[i] = vector.vector[rowNum];
        } else if (columnVector instanceof ListColumnVector) {
          ListColumnVector vector = (ListColumnVector) columnVector;
          StringBuilder builder = new StringBuilder();
          vector.stringifyValue(builder, rowNum);
          rowData[i] = builder.toString();
        } else if (columnVector instanceof IntervalDayTimeColumnVector) {
          IntervalDayTimeColumnVector vector = (IntervalDayTimeColumnVector) columnVector;
          StringBuilder builder = new StringBuilder();
          vector.stringifyValue(builder, rowNum);
          rowData[i] = builder.toString();
        } else if (columnVector instanceof LongColumnVector) {
          LongColumnVector vector = (LongColumnVector) columnVector;
          rowData[i] = vector.vector[rowNum];
        } else if (columnVector instanceof MapColumnVector) {
          MapColumnVector vector = (MapColumnVector) columnVector;
          StringBuilder builder = new StringBuilder();
          vector.stringifyValue(builder, rowNum);
          rowData[i] = builder.toString();
        } else if (columnVector instanceof MultiValuedColumnVector) {
          MultiValuedColumnVector vector = (MultiValuedColumnVector) columnVector;
          StringBuilder builder = new StringBuilder();
          vector.stringifyValue(builder, rowNum);
          rowData[i] = builder.toString();
        } else if (columnVector instanceof StructColumnVector) {
          StructColumnVector vector = (StructColumnVector) columnVector;
          StringBuilder builder = new StringBuilder();
          vector.stringifyValue(builder, rowNum);
          rowData[i] = builder.toString();
        } else if (columnVector instanceof TimestampColumnVector) {
          TimestampColumnVector vector = (TimestampColumnVector) columnVector;
          rowData[i] = vector.time[rowNum];
        } else if (columnVector instanceof UnionColumnVector) {
          UnionColumnVector vector = (UnionColumnVector) columnVector;
          StringBuilder builder = new StringBuilder();
          vector.stringifyValue(builder, rowNum);
          rowData[i] = builder.toString();
        }
      }
      row = new TableRecord(rowData);
    } else {
      return false;
    }
    return row != null;
  }

  @Override
  public Record getRecord() {
    if (metaData == null) throw new RuntimeException("Must read metadata first(必须先读取metadata)");
    if (row == null) {
      throw new RuntimeException(
          "Can't get the value of the field, maybe the IO stream has been read or has been closed!(拿不到字段的值，也许IO流已读取完毕或已被关闭！)");
    }
    return row;
  }

  @Override
  public void close() throws IOException {
    IOUtils.closeQuietly(inputStream);
    rows.close();
    reader.close();
  }
}
