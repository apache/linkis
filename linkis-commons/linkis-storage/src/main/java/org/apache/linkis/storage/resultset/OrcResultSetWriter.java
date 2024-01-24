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
import org.apache.linkis.storage.utils.OrcUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.apache.orc.storage.ql.exec.vector.VectorizedRowBatch;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrcResultSetWriter<K extends MetaData, V extends Record>
    extends ResultSetWriter<K, V> {
  private static final Logger logger = LoggerFactory.getLogger(OrcResultSetWriter.class);

  private TypeDescription schema;

  private Writer writer;

  private boolean moveToWriteRow = false;

  private MetaData metaData = null;

  private final FsPath storePath;

  private final long maxCacheSize;

  private final ResultSet<K, V> resultSet;

  public OrcResultSetWriter(ResultSet resultSet, long maxCacheSize, FsPath storePath) {
    super(resultSet, maxCacheSize, storePath);
    this.resultSet = resultSet;
    this.maxCacheSize = maxCacheSize;
    this.storePath = storePath;
  }

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    if (!moveToWriteRow) {
      this.metaData = metaData;
      if (this.schema == null) {
        this.schema = TypeDescription.createStruct();
      }
      TableMetaData tableMetaData = (TableMetaData) this.metaData;
      for (Column column : tableMetaData.columns) {
        schema.addField(column.getColumnName(), OrcUtils.dataTypeToOrcType(column.getDataType()));
      }
      moveToWriteRow = true;
      if (writer == null) {
        writer =
            OrcFile.createWriter(
                new Path(storePath.getPath()),
                OrcFile.writerOptions(new Configuration())
                    .setSchema(schema)
                    .compress(CompressionKind.ZLIB)
                    .version(OrcFile.Version.V_0_12));
      }
    }
  }

  @Override
  public void addRecord(Record record) {
    if (moveToWriteRow) {
      TableRecord tableRecord = (TableRecord) record;
      TableMetaData tableMetaData = (TableMetaData) metaData;
      try {
        Object[] row = tableRecord.row;
        VectorizedRowBatch batch = schema.createRowBatch();
        int rowCount = batch.size++;

        for (int i = 0; i < row.length; i++) {
          OrcUtils.setColumn(
              rowCount, batch.cols[i], tableMetaData.columns[i].getDataType(), row[i]);
          if (batch.size == batch.getMaxSize()) {
            writer.addRowBatch(batch);
            batch.reset();
          }
        }
        writer.addRowBatch(batch);

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
    writer.close();
  }

  @Override
  public void flush() throws IOException {}
}
