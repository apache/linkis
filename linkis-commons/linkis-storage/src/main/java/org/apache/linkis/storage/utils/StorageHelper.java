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

package org.apache.linkis.storage.utils;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.resultset.ResultSetFactory;
import org.apache.linkis.storage.resultset.ResultSetReaderFactory;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * 工具类,用于做storage jar包打出来做测试用 Tool class, which is used to print the storage jar package for testing
 */
public class StorageHelper {
  private static final Log logger = LogFactory.getLog(StorageHelper.class);

  public static void main(String[] args) {
    if (args.length < 2) logger.info("Usage method params eg:getTableResLines path");
    String method = args[0];
    String[] params = Arrays.copyOfRange(args, 1, args.length);
    try {
      Thread.sleep(10000L);
    } catch (InterruptedException e) {
    }

    switch (method) {
      case "getTableResLines":
        getTableResLines(params);
        break;
      case "getTableRes":
        getTableRes(params);
        break;
      case "createNewFile":
        createNewFile(params);
        break;
      default:
        logger.info("There is no such method");
    }
  }

  /**
   * Get the number of table result set file lines(获得表格结果集文件行数)
   *
   * @param args
   */
  public static void getTableResLines(String[] args) {
    ResultSetReader resultSetReader = null;
    try {
      FsPath resPath = StorageUtils.getFsPath(args[0]);
      ResultSetFactory resultSetFactory = ResultSetFactory.getInstance();

      ResultSet<? extends MetaData, ? extends Record> resultSet =
          resultSetFactory.getResultSetByType(ResultSetFactory.TABLE_TYPE);
      Fs fs = FSFactory.getFs(resPath);
      fs.init(null);
      resultSetReader = ResultSetReaderFactory.getResultSetReader(resultSet, fs.read(resPath));
      TableMetaData metaData = (TableMetaData) resultSetReader.getMetaData();
      Arrays.stream(metaData.getColumns()).forEach(column -> logger.info(column.toString()));
      int num = 0;
      Thread.sleep(10000L);
      while (resultSetReader.hasNext()) {
        resultSetReader.getRecord();
        num++;
      }
      logger.info(Integer.toString(num));
    } catch (Exception e) {
      logger.error("getTableResLines error:", e);
    } finally {
      if (resultSetReader != null) {
        try {
          resultSetReader.close();
        } catch (IOException e) {
          logger.error("Failed to close ResultSetReader", e);
        }
      }
    }
  }

  public static void getTableRes(String[] args) {
    try {
      int len = Integer.parseInt(args[1]);
      int max = len + 10;
      FsPath resPath = StorageUtils.getFsPath(args[0]);
      ResultSetFactory resultSetFactory = ResultSetFactory.getInstance();
      ResultSet<? extends MetaData, ? extends Record> resultSet =
          resultSetFactory.getResultSetByType(ResultSetFactory.TABLE_TYPE);
      Fs fs = FSFactory.getFs(resPath);

      fs.init(null);

      ResultSetReader reader =
          ResultSetReaderFactory.getResultSetReader(resultSet, fs.read(resPath));
      MetaData rmetaData = reader.getMetaData();
      Arrays.stream(((TableMetaData) rmetaData).getColumns())
          .forEach(column -> logger.info(column.toString()));
      Arrays.stream(((TableMetaData) rmetaData).getColumns())
          .map(column -> column.getColumnName() + ",")
          .forEach(column -> logger.info(column));
      int num = 0;
      while (reader.hasNext()) {
        num++;
        if (num > max) return;
        if (num > len) {
          Record record = reader.getRecord();
          Arrays.stream(((TableRecord) record).row)
              .forEach(
                  value -> {
                    logger.info(value.toString());
                    logger.info(",");
                  });
          logger.info("\n");
        }
      }
    } catch (IOException e) {
      logger.warn("StorageHelper getTableRes failed", e);
    }
  }

  public static void createNewFile(String[] args) {
    FsPath resPath = StorageUtils.getFsPath(args[0]);
    String proxyUser = StorageUtils.getJvmUser();
    try {
      FileSystemUtils.createNewFile(resPath, proxyUser, true);
    } catch (Exception e) {
      logger.warn("StorageHelper createNewFile failed", e);
    }
    logger.info("success");
  }
}
