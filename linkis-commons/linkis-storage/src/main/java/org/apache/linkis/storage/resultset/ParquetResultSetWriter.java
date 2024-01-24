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
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParquetResultSetWriter<K extends MetaData, V extends Record>
    extends ResultSetWriter<K, V> {
  private static final Logger logger = LoggerFactory.getLogger(ParquetResultSetWriter.class);

  private Schema schema;

  private ParquetWriter<GenericRecord> parquetWriter;

  private boolean moveToWriteRow = false;

  private MetaData metaData = null;

  private final FsPath storePath;

  private final long maxCacheSize;

  private final ResultSet<K, V> resultSet;

  public ParquetResultSetWriter(ResultSet resultSet, long maxCacheSize, FsPath storePath) {
    super(resultSet, maxCacheSize, storePath);
    this.resultSet = resultSet;
    this.maxCacheSize = maxCacheSize;
    this.storePath = storePath;
  }

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    if (!moveToWriteRow) {
      this.metaData = metaData;
      SchemaBuilder.FieldAssembler<Schema> fieldAssembler = SchemaBuilder.record("linkis").fields();
      TableMetaData tableMetaData = (TableMetaData) this.metaData;
      for (Column column : tableMetaData.columns) {
        fieldAssembler
            .name(column.getColumnName().replaceAll("\\.", "_").replaceAll("[^a-zA-Z0-9_]", ""))
            .doc(column.getComment())
            .type(column.getDataType().getTypeName().toLowerCase())
            .noDefault();
      }
      schema = fieldAssembler.endRecord();
      moveToWriteRow = true;
      if (parquetWriter == null) {
        parquetWriter =
            AvroParquetWriter.<GenericRecord>builder(new Path(storePath.getPath()))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();
      }
    }
  }

  @Override
  public void addRecord(Record record) {
    if (moveToWriteRow) {
      TableRecord tableRecord = (TableRecord) record;
      try {
        Object[] row = tableRecord.row;
        GenericRecord genericRecord = new GenericData.Record(schema);
        for (int i = 0; i < row.length; i++) {
          genericRecord.put(schema.getFields().get(i).name(), row[i]);
        }
        parquetWriter.write(genericRecord);
      } catch (IOException e) {
        logger.warn("addMetaDataAndRecordString failed", e);
      }
    }
  }

  @Override
  public FsPath toFSPath() {
    return storePath;
  }

  @Override
  public String toString() {
    return storePath.getSchemaPath();
  }

  @Override
  public void addMetaDataAndRecordString(String content) {}

  @Override
  public void addRecordString(String content) {}

  @Override
  public void close() throws IOException {
    parquetWriter.close();
  }

  @Override
  public void flush() throws IOException {}
}
