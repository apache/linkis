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

import org.apache.linkis.common.io.FsReader;
import org.apache.linkis.common.io.FsWriter;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.storage.LineMetaData;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.resultset.table.TableMetaData;
import org.apache.linkis.storage.resultset.table.TableRecord;
import org.apache.linkis.storage.script.Parser;
import org.apache.linkis.storage.script.ScriptMetaData;
import org.apache.linkis.storage.script.Variable;
import org.apache.linkis.storage.script.VariableParser;
import org.apache.linkis.storage.script.reader.StorageScriptFsReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSplit implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(FileSplit.class);

  private FsReader<? extends MetaData, ? extends Record> fsReader;
  protected String type = "script/text";
  private int start = 0;
  private int end = -1;
  private int count = 0;
  private int totalLine = 0;
  protected Function<Record, Record> shuffler;
  private boolean pageTrigger = false;
  protected Map<String, String> params = new HashMap<>();

  public FileSplit(FsReader<? extends MetaData, ? extends Record> fsReader) {
    this.fsReader = fsReader;
  }

  public FileSplit(FsReader<? extends MetaData, ? extends Record> fsReader, String type) {
    this.fsReader = fsReader;
    this.type = type;
  }

  public void page(int page, int pageSize) {
    if (!pageTrigger) {
      start = (page - 1) * pageSize;
      end = pageSize * page - 1;
      pageTrigger = true;
    }
  }

  public String getType() {
    return type;
  }

  public void addParams(Map<String, String> params) {
    this.params.putAll(params);
  }

  public void addParams(String key, String value) {
    this.params.put(key, value);
  }

  public Map<String, String> getParams() {
    return params;
  }

  public int getTotalLine() {
    return totalLine;
  }

  public <M> M whileLoop(Function<MetaData, M> metaDataFunction, Consumer<Record> recordConsumer) {
    M m = null;
    try {
      MetaData metaData = fsReader.getMetaData();
      m = metaDataFunction.apply(metaData);
      if (pageTrigger) {
        fsReader.skip(start);
      }
      count = start;
      boolean hasRemovedFlag = false;
      while (fsReader.hasNext() && ifContinueRead()) {
        Record record = fsReader.getRecord();
        boolean needRemoveFlag = false;
        if (!hasRemovedFlag && fsReader instanceof StorageScriptFsReader) {
          Parser parser = ((StorageScriptFsReader) fsReader).getScriptParser();
          Variable[] meta = ((ScriptMetaData) metaData).getMetaData();
          if (meta != null
              && meta.length > 0
              && parser != null
              && parser.getAnnotationSymbol().equals(record.toString())) {
            needRemoveFlag = true;
            hasRemovedFlag = true;
          }
        }
        if (!needRemoveFlag) {
          recordConsumer.accept(shuffler.apply(record));
          totalLine++;
          count++;
        }
      }
    } catch (IOException e) {
      logger.warn("FileSplit forEach failed", e);
      throw new StorageWarnException(
          LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(),
          LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorMessage());
    }
    return m;
  }

  public void biConsumerWhileLoop(
      Consumer<MetaData> metaDataFunction, Consumer<Record> recordConsumer) {
    try {
      MetaData metaData = fsReader.getMetaData();
      metaDataFunction.accept(metaData);
      if (pageTrigger) {
        fsReader.skip(start);
      }
      count = start;
      boolean hasRemovedFlag = false;
      while (fsReader.hasNext() && ifContinueRead()) {
        Record record = fsReader.getRecord();
        boolean needRemoveFlag = false;
        if (!hasRemovedFlag && fsReader instanceof StorageScriptFsReader) {
          Parser parser = ((StorageScriptFsReader) fsReader).getScriptParser();
          Variable[] meta = ((ScriptMetaData) metaData).getMetaData();
          if (meta != null
              && meta.length > 0
              && parser != null
              && parser.getAnnotationSymbol().equals(record.toString())) {
            needRemoveFlag = true;
            hasRemovedFlag = true;
          }
        }
        if (!needRemoveFlag) {
          recordConsumer.accept(shuffler.apply(record));
          totalLine++;
          count++;
        }
      }
    } catch (IOException e) {
      logger.warn("FileSplit forEach failed", e);
      throw new StorageWarnException(
          LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(),
          LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorMessage());
    }
  }

  public Pair<Integer, Integer> getFileInfo(int needToCountRowNumber) {
    int colNumber = 0;
    int rowNumber = 0;
    MetaData metaData = null;
    try {
      metaData = fsReader.getMetaData();
      colNumber =
          metaData instanceof TableMetaData ? ((TableMetaData) metaData).getColumns().length : 1;
      rowNumber =
          needToCountRowNumber == -1
              ? fsReader.skip(Integer.MAX_VALUE)
              : fsReader.skip(needToCountRowNumber);
    } catch (IOException e) {
      logger.warn("FileSplit getFileInfo failed", e);
      throw new StorageWarnException(
          LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(),
          LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorMessage());
    }
    return new Pair<>(colNumber, rowNumber);
  }

  public <K extends MetaData, V extends Record> void write(FsWriter<K, V> fsWriter) {
    biConsumerWhileLoop(
        metaData -> {
          try {
            fsWriter.addMetaData(metaData);
          } catch (IOException e) {
            logger.warn("FileSplit addMetaData failed", e);
            throw new StorageWarnException(
                LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(),
                LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorMessage());
          }
        },
        record -> {
          try {
            fsWriter.addRecord(record);
          } catch (IOException e) {
            logger.warn("FileSplit addRecord failed", e);
            throw new StorageWarnException(
                LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorCode(),
                LinkisStorageErrorCodeSummary.UNSUPPORTED_OPEN_FILE_TYPE.getErrorMessage());
          }
        });
  }

  public Pair<Object, List<String[]>> collect() {
    List<String[]> recordList = new ArrayList<>();
    Object metaData =
        whileLoop(
            collectMetaData -> collectMetaData(collectMetaData),
            r -> recordList.add(collectRecord(r)));
    return new Pair<>(metaData, recordList);
  }

  public String[] collectRecord(Record record) {
    if (record instanceof TableRecord) {
      TableRecord tableRecord = (TableRecord) record;
      return Arrays.stream(tableRecord.row).map(DataType::valueToString).toArray(String[]::new);
    } else if (record instanceof LineRecord) {
      LineRecord lineRecord = (LineRecord) record;
      return new String[] {lineRecord.getLine()};
    } else {
      throw new IllegalArgumentException("Unknown record type");
    }
  }

  public Object collectMetaData(MetaData metaData) {
    if (metaData instanceof ScriptMetaData) {
      ScriptMetaData scriptMetaData = (ScriptMetaData) metaData;
      return VariableParser.getMap(scriptMetaData.getMetaData());
    } else if (metaData instanceof LineMetaData) {
      LineMetaData lineMetaData = (LineMetaData) metaData;
      return lineMetaData.getMetaData();
    } else if (metaData instanceof TableMetaData) {
      TableMetaData tableMetaData = (TableMetaData) metaData;
      return Arrays.stream(tableMetaData.getColumns())
          .map(this::columnToMap)
          .collect(Collectors.toList());
    } else {
      throw new IllegalArgumentException("Unknown metadata type");
    }
  }

  private Map<String, String> columnToMap(Column column) {
    Map<String, String> stringMap = new HashMap<>();
    stringMap.put("columnName", column.getColumnName());
    stringMap.put("comment", column.getComment());
    stringMap.put("dataType", column.getDataType().getTypeName());
    return stringMap;
  }

  public boolean ifContinueRead() {
    return !pageTrigger || count <= end;
  }

  public boolean ifStartRead() {
    return !pageTrigger || count >= start;
  }

  @Override
  public void close() {
    IOUtils.closeQuietly(fsReader);
  }
}
