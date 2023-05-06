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
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultDeserializer;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageResultSetReader<K extends MetaData, V extends Record>
    extends ResultSetReader<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(StorageResultSetReader.class);

  private final ResultSet<K, V> resultSet;
  private final InputStream inputStream;
  private final ResultDeserializer<K, V> deserializer;
  private K metaData;
  private Record row;
  private int colCount = 0;
  private int rowCount = 0;
  private Fs fs;

  private final int READ_CACHE = 1024;
  private final byte[] bytes = new byte[READ_CACHE];

  public StorageResultSetReader(ResultSet<K, V> resultSet, InputStream inputStream) {
    super(resultSet, inputStream);
    this.resultSet = resultSet;
    this.inputStream = inputStream;
    this.deserializer = resultSet.createResultSetDeserializer();
  }

  public StorageResultSetReader(ResultSet<K, V> resultSet, String value) {
    this(resultSet, new ByteArrayInputStream(value.getBytes(Dolphin.CHAR_SET)));
  }

  public void init() throws IOException {
    String resType = Dolphin.getType(inputStream);
    if (!StringUtils.equals(resultSet.resultSetType(), resType)) {
      throw new RuntimeException(
          "File type does not match(文件类型不匹配): "
              + ResultSetFactory.resultSetType.getOrDefault(resType, "TABLE"));
    }
  }

  public byte[] readLine() {
    int rowLen = 0;
    try {
      rowLen = Dolphin.readInt(inputStream);
    } catch (StorageWarnException | IOException e) {
      logger.info("Read finished(读取完毕)");
      return null;
    }

    byte[] rowBuffer = new byte[0];
    int len = 0;

    while (rowLen > 0 && len >= 0) {
      if (rowLen > READ_CACHE) {
        len = StorageUtils.readBytes(inputStream, bytes, READ_CACHE);
      } else {
        len = StorageUtils.readBytes(inputStream, bytes, rowLen);
      }

      if (len > 0) {
        rowLen -= len;
        rowBuffer = Arrays.copyOf(rowBuffer, rowBuffer.length + len);
        System.arraycopy(bytes, 0, rowBuffer, rowBuffer.length - len, len);
      }
    }
    rowCount++;
    return rowBuffer;
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

  public void setFs(Fs fs) {
    this.fs = fs;
  }

  public Fs getFs() {
    return fs;
  }

  @Override
  public MetaData getMetaData() {
    if (metaData == null) {
      try {
        init();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    metaData = deserializer.createMetaData(readLine());
    return metaData;
  }

  @Override
  public int skip(int recordNum) throws IOException {
    if (recordNum < 0) return -1;

    if (metaData == null) getMetaData();
    for (int i = recordNum; i > 0; i--) {
      try {
        inputStream.skip(Dolphin.readInt(inputStream));
      } catch (Throwable t) {
        return recordNum - i;
      }
    }
    return recordNum;
  }

  @Override
  public long getPosition() throws IOException {
    return rowCount;
  }

  @Override
  public boolean hasNext() throws IOException {
    if (metaData == null) getMetaData();
    byte[] line = readLine();
    if (line == null) return false;
    row = deserializer.createRecord(line);
    if (row == null) return false;
    return true;
  }

  @Override
  public long available() throws IOException {
    return inputStream.available();
  }

  @Override
  public void close() throws IOException {
    IOUtils.closeQuietly(inputStream);
    if (this.fs != null) {
      this.fs.close();
    }
  }
}
