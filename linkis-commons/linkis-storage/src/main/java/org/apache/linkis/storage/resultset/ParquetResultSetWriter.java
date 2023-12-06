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
    extends StorageResultSetWriter {
  private static final Logger logger = LoggerFactory.getLogger(ParquetResultSetWriter.class);

  private Schema schema = null;

  public ParquetResultSetWriter(ResultSet resultSet, long maxCacheSize, FsPath storePath) {
    super(resultSet, maxCacheSize, storePath);
  }

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    if (!moveToWriteRow) {
      rMetaData = metaData;
      SchemaBuilder.FieldAssembler<Schema> fieldAssembler = SchemaBuilder.record("linkis").fields();
      TableMetaData tableMetaData = (TableMetaData) rMetaData;
      for (Column column : tableMetaData.columns) {
        fieldAssembler
            .name(column.getColumnName().replaceAll("\\.", "_").replaceAll("[^a-zA-Z0-9_]", ""))
            .doc(column.getComment())
            .type(column.getDataType().getTypeName().toLowerCase())
            .noDefault();
      }
      schema = fieldAssembler.endRecord();
      moveToWriteRow = true;
    }
  }

  @Override
  public void addRecord(Record record) {
    if (moveToWriteRow) {
      try {
        TableRecord tableRecord = (TableRecord) record;
        Object[] row = tableRecord.row;
        try (ParquetWriter<GenericRecord> writer =
            AvroParquetWriter.<GenericRecord>builder(new Path(storePath.getPath()))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build()) {

          GenericRecord genericRecord = new GenericData.Record(schema);
          for (int i = 0; i < row.length; i++) {
            genericRecord.put(schema.getFields().get(i).name(), row[i]);
          }
          writer.write(genericRecord);
        }
      } catch (Exception e) {
        logger.warn("addMetaDataAndRecordString failed", e);
      }
    }
  }
}
