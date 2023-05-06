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

package org.apache.linkis.storage.source;

import org.apache.linkis.common.io.*;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.storage.conf.LinkisStorageConf;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.resultset.ResultSetFactory;
import org.apache.linkis.storage.resultset.ResultSetReaderFactory;
import org.apache.linkis.storage.script.ScriptFsReader;
import org.apache.linkis.storage.utils.StorageConfiguration;

import org.apache.commons.math3.util.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE;

public interface FileSource extends Closeable {
  Logger logger = LoggerFactory.getLogger(FileSource.class);

  FileSource shuffle(Function<Record, Record> s);

  FileSource page(int page, int pageSize);

  Pair<Object, List<String[]>>[] collect();

  Pair<Integer, Integer>[] getFileInfo(int needToCountRowNumber);

  <K extends MetaData, V extends Record> void write(FsWriter<K, V> fsWriter);

  FileSource addParams(Map<String, String> params);

  FileSource addParams(String key, String value);

  Map<String, String> getParams();

  int getTotalLine();

  String[] getTypes();

  FileSplit[] getFileSplits();

  String[] fileType = LinkisStorageConf.getFileTypeArr();
  BiFunction<String, String, Boolean> suffixPredicate =
      (path, suffix) -> path.endsWith("." + suffix);

  static boolean isResultSet(String path) {
    return suffixPredicate.apply(path, fileType[0]);
  }

  static boolean isResultSet(FsPath fsPath) {
    return isResultSet(fsPath.getPath());
  }

  /**
   * Currently only supports table multi-result sets
   *
   * @param fsPaths
   * @param fs
   * @return
   */
  static FileSource create(FsPath[] fsPaths, Fs fs) {
    // Filter non-table result sets
    FileSplit[] fileSplits =
        Arrays.stream(fsPaths)
            .map(fsPath -> createResultSetFileSplit(fsPath, fs))
            .filter(FileSource::isTableResultSet)
            .toArray(FileSplit[]::new);
    return new ResultsetFileSource(fileSplits);
  }

  static boolean isTableResultSet(FileSplit fileSplit) {
    return fileSplit.type.equals(ResultSetFactory.TABLE_TYPE);
  }

  static boolean isTableResultSet(FileSource fileSource) {
    // Return true only if all splits are table result sets
    return Arrays.stream(fileSource.getFileSplits()).allMatch(FileSource::isTableResultSet);
  }

  static FileSource create(FsPath fsPath, Fs fs) {
    if (!canRead(fsPath.getPath())) {
      throw new StorageWarnException(
          UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(), UNSUPPORTED_OPEN_FILE_TYPE.getErrorDesc());
    }
    if (isResultSet(fsPath)) {
      return new ResultsetFileSource(new FileSplit[] {createResultSetFileSplit(fsPath, fs)});
    } else {
      return new TextFileSource(new FileSplit[] {createTextFileSplit(fsPath, fs)});
    }
  }

  static FileSource create(FsPath fsPath, InputStream is) {
    if (!canRead(fsPath.getPath())) {
      throw new StorageWarnException(
          UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(), UNSUPPORTED_OPEN_FILE_TYPE.getErrorDesc());
    }
    if (isResultSet(fsPath)) {
      return new ResultsetFileSource(new FileSplit[] {createResultSetFileSplit(fsPath, is)});
    } else {
      return new TextFileSource(new FileSplit[] {createTextFileSplit(fsPath, is)});
    }
  }

  static FileSplit createResultSetFileSplit(FsPath fsPath, InputStream is) {
    ResultSet resultset = ResultSetFactory.getInstance().getResultSetByPath(fsPath);
    ResultSetReader resultsetReader = ResultSetReaderFactory.getResultSetReader(resultset, is);
    return new FileSplit(resultsetReader, resultset.resultSetType());
  }

  static FileSplit createResultSetFileSplit(FsPath fsPath, Fs fs) {
    ResultSet resultset = ResultSetFactory.getInstance().getResultSetByPath(fsPath, fs);
    ResultSetReader resultsetReader = null;
    try {
      resultsetReader = ResultSetReaderFactory.getResultSetReader(resultset, fs.read(fsPath));
    } catch (IOException e) {
      logger.warn("FileSource createResultSetFileSplit failed", e);
    }
    return new FileSplit(resultsetReader, resultset.resultSetType());
  }

  static FileSplit createTextFileSplit(FsPath fsPath, InputStream is) {
    ScriptFsReader scriptFsReader =
        ScriptFsReader.getScriptFsReader(
            fsPath, StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue(), is);
    return new FileSplit(scriptFsReader);
  }

  static FileSplit createTextFileSplit(FsPath fsPath, Fs fs) {
    ScriptFsReader scriptFsReader = null;
    try {
      scriptFsReader =
          ScriptFsReader.getScriptFsReader(
              fsPath, StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue(), fs.read(fsPath));
    } catch (IOException e) {
      logger.warn("FileSource createTextFileSplit failed", e);
    }
    return new FileSplit(scriptFsReader);
  }

  static boolean canRead(String path) {
    return Arrays.stream(fileType).anyMatch(suffix -> path.endsWith("." + suffix));
  }
}
