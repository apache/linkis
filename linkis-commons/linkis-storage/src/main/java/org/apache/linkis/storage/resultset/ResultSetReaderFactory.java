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

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;
import org.apache.linkis.storage.resultset.table.TableResultSet;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSetReaderFactory {
  private static final Logger logger = LoggerFactory.getLogger(ResultSetReaderFactory.class);

  public static <K extends MetaData, V extends Record> ResultSetReader getResultSetReader(
      ResultSet<K, V> resultSet, InputStream inputStream) {
    return new StorageResultSetReader<>(resultSet, inputStream);
  }

  public static <K extends MetaData, V extends Record> ResultSetReader getResultSetReader(
      ResultSet<K, V> resultSet, String value) {
    return new StorageResultSetReader<>(resultSet, value);
  }

  public static ResultSetReader getResultSetReader(String res) {
    ResultSetFactory rsFactory = ResultSetFactory.getInstance();
    if (rsFactory.isResultSet(res)) {
      ResultSet<? extends MetaData, ? extends Record> resultSet = rsFactory.getResultSet(res);
      return ResultSetReaderFactory.getResultSetReader(resultSet, res);
    } else {
      FsPath resPath = new FsPath(res);
      ResultSet<? extends MetaData, ? extends Record> resultSet =
          rsFactory.getResultSetByPath(resPath);
      try {
        FSFactory.getFs(resPath).init(null);
      } catch (IOException e) {
        logger.warn("ResultSetReaderFactory fs init failed", e);
      }
      ResultSetReader reader = null;
      try {
        reader =
            ResultSetReaderFactory.getResultSetReader(
                resultSet, FSFactory.getFs(resPath).read(resPath));
      } catch (IOException e) {
        logger.warn("ResultSetReaderFactory fs read failed", e);
      }
      if (reader instanceof StorageResultSetReader) {
        ((StorageResultSetReader<?, ?>) reader).setFs(FSFactory.getFs(resPath));
      }
      return (StorageResultSetReader<?, ?>) reader;
    }
  }

  public static ResultSetReader getTableResultReader(String res) {
    ResultSetFactory rsFactory = ResultSetFactory.getInstance();
    if (rsFactory.isResultSet(res)) {
      ResultSet<?, ?> resultSet = rsFactory.getResultSet(res);
      if (!ResultSetFactory.TABLE_TYPE.equals(resultSet.resultSetType())) {
        throw new StorageWarnException(
            LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorCode(),
            LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorDesc());
      }
      return ResultSetReaderFactory.<TableMetaData, TableRecord>getResultSetReader(
          (TableResultSet) resultSet, res);
    } else {
      FsPath resPath = new FsPath(res);
      ResultSet<?, ?> resultSet = rsFactory.getResultSetByPath(resPath);
      if (!ResultSetFactory.TABLE_TYPE.equals(resultSet.resultSetType())) {
        throw new StorageWarnException(
            LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorCode(),
            LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorDesc());
      }

      Fs fs = FSFactory.getFs(resPath);
      logger.info("Try to init Fs with path:{}", resPath.getPath());
      try {
        fs.init(null);
        InputStream read = fs.read(resPath);

        return ResultSetReaderFactory.<TableMetaData, TableRecord>getResultSetReader(
            (TableResultSet) resultSet, read);
      } catch (IOException e) {
        throw new StorageWarnException(
            LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorCode(),
            LinkisStorageErrorCodeSummary.TABLE_ARE_NOT_SUPPORTED.getErrorDesc());
      }
    }
  }
}
