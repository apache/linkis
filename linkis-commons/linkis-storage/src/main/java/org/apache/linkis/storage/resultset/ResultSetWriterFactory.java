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
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.resultset.table.TableResultSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetWriterFactory {
  private static final Logger logger = LoggerFactory.getLogger(ResultSetWriterFactory.class);

  public static <K extends MetaData, V extends Record> ResultSetWriter<K, V> getResultSetWriter(
      ResultSet<K, V> resultSet, long maxCacheSize, FsPath storePath) {
    String engineResultType = LinkisStorageConf.ENGINE_RESULT_TYPE;
    ResultSetWriter<K, V> writer = null;
    if (engineResultType.equals(LinkisStorageConf.PARQUET) && resultSet instanceof TableResultSet) {
      writer = new ParquetResultSetWriter<>(resultSet, maxCacheSize, storePath);
    } else if (engineResultType.equals(LinkisStorageConf.ORC)
        && resultSet instanceof TableResultSet) {
      writer = new OrcResultSetWriter<>(resultSet, maxCacheSize, storePath);
    } else {
      writer = new StorageResultSetWriter<>(resultSet, maxCacheSize, storePath);
    }
    return writer;
  }

  public static <K extends MetaData, V extends Record> ResultSetWriter<K, V> getResultSetWriter(
      ResultSet<K, V> resultSet, long maxCacheSize, FsPath storePath, String proxyUser) {
    String engineResultType = LinkisStorageConf.ENGINE_RESULT_TYPE;
    ResultSetWriter<K, V> writer = null;
    if (engineResultType.equals(LinkisStorageConf.PARQUET) && resultSet instanceof TableResultSet) {
      writer = new ParquetResultSetWriter<>(resultSet, maxCacheSize, storePath);
    } else if (engineResultType.equals(LinkisStorageConf.ORC)
        && resultSet instanceof TableResultSet) {
      writer = new OrcResultSetWriter<>(resultSet, maxCacheSize, storePath);
    } else {
      writer = new StorageResultSetWriter<>(resultSet, maxCacheSize, storePath);
      StorageResultSetWriter storageResultSetWriter = (StorageResultSetWriter) writer;
      storageResultSetWriter.setProxyUser(proxyUser);
    }
    return writer;
  }

  public static Record[] getRecordByWriter(
      ResultSetWriter<? extends MetaData, ? extends Record> writer, long limit) throws IOException {
    String res = writer.toString();
    return getRecordByRes(res, limit);
  }

  public static Record[] getRecordByRes(String res, long limit) throws IOException {
    ResultSetReader reader = ResultSetReaderFactory.getResultSetReader(res);
    int count = 0;
    List<Record> records = new ArrayList<>();
    reader.getMetaData();
    while (reader.hasNext() && count < limit) {
      records.add(reader.getRecord());
      count++;
    }
    return records.toArray(new Record[0]);
  }

  public static Record getLastRecordByRes(String res) throws IOException {
    ResultSetReader reader = ResultSetReaderFactory.getResultSetReader(res);
    Record record = null;
    try {
      reader.getMetaData();
      while (reader.hasNext()) {
        record = reader.getRecord();
      }
    } catch (IOException e) {
      logger.warn("ResultSetWriter getLastRecordByRes failed", e);
    }
    return record;
  }
}
