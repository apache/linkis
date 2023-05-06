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

import org.apache.linkis.common.io.*;
import org.apache.linkis.common.io.resultset.*;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.utils.*;
import org.apache.linkis.storage.*;
import org.apache.linkis.storage.conf.*;
import org.apache.linkis.storage.domain.*;
import org.apache.linkis.storage.utils.*;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageResultSetWriter<K extends MetaData, V extends Record>
    extends ResultSetWriter<K, V> {
  private static final Logger logger = LoggerFactory.getLogger(StorageResultSetWriter.class);

  private final ResultSet<K, V> resultSet;
  private final long maxCacheSize;
  private final FsPath storePath;

  private final ResultSerializer serializer;
  private boolean moveToWriteRow = false;
  private OutputStream outputStream = null;
  private int rowCount = 0;
  private final List<Byte> buffer = new ArrayList<Byte>();
  private Fs fs = null;
  private MetaData rMetaData = null;
  private String proxyUser = StorageUtils.getJvmUser();
  private boolean fileCreated = false;
  private boolean closed = false;
  private final Object WRITER_LOCK_CREATE = new Object();
  private final Object WRITER_LOCK_CLOSE = new Object();

  public StorageResultSetWriter(ResultSet<K, V> resultSet, long maxCacheSize, FsPath storePath) {
    super(resultSet, maxCacheSize, storePath);
    this.resultSet = resultSet;
    this.maxCacheSize = maxCacheSize;
    this.storePath = storePath;

    this.serializer = resultSet.createResultSetSerializer();
  }

  public MetaData getMetaData() {
    return rMetaData;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  public boolean isEmpty() {
    return rMetaData == null && buffer.size() <= Dolphin.FILE_EMPTY;
  }

  public void init() {
    try {
      writeLine(resultSet.getResultSetHeader(), true);
    } catch (IOException e) {
      logger.warn("StorageResultSetWriter init failed", e);
    }
  }

  public void createNewFile() {
    if (!fileCreated) {
      synchronized (WRITER_LOCK_CREATE) {
        if (!fileCreated) {
          if (storePath != null && outputStream == null) {
            logger.info("Try to create a new file:{}, with proxy user:{}", storePath, proxyUser);
            fs = FSFactory.getFsByProxyUser(storePath, proxyUser);
            try {
              fs.init(null);
              FileSystemUtils.createNewFile(storePath, proxyUser, true);
              outputStream = fs.write(storePath, true);
            } catch (IOException e) {
              logger.warn("StorageResultSetWriter createNewFile failed", e);
            }
            logger.info("Succeed to create a new file:{}", storePath);
            fileCreated = true;
          }
        }
      }
    } else if (storePath != null && outputStream == null) {
      logger.warn("outputStream had been set null, but createNewFile() was called again.");
    }
  }

  public void writeLine(byte[] bytes, boolean cache) throws IOException {
    if (closed) {
      logger.warn("the writer had been closed, but writeLine() was still called.");
      return;
    }
    if (bytes.length > LinkisStorageConf.ROW_BYTE_MAX_LEN) {
      throw new IOException(
          String.format(
              "A single row of data cannot exceed %s", LinkisStorageConf.ROW_BYTE_MAX_LEN_STR));
    }
    if (buffer.size() > maxCacheSize && !cache) {
      if (outputStream == null) {
        createNewFile();
      }
      flush();
      outputStream.write(bytes);
    } else {
      for (byte b : bytes) {
        buffer.add(b);
      }
    }
  }

  @Override
  public String toString() {
    if (outputStream == null) {
      if (isEmpty()) {
        return "";
      }

      byte[] byteArray = getBytes();
      return new String(byteArray, Dolphin.CHAR_SET);
    }
    return storePath.getSchemaPath();
  }

  private byte[] getBytes() {
    byte[] byteArray = new byte[buffer.size()];
    for (int i = 0; i < buffer.size(); i++) {
      byteArray[i] = buffer.get(i);
    }
    return byteArray;
  }

  @Override
  public FsPath toFSPath() {
    return storePath;
  }

  @Override
  public void addMetaDataAndRecordString(String content) {
    if (!moveToWriteRow) {
      byte[] bytes = content.getBytes(Dolphin.CHAR_SET);
      try {
        writeLine(bytes, false);
      } catch (IOException e) {
        logger.warn("addMetaDataAndRecordString failed", e);
      }
    }
    moveToWriteRow = true;
  }

  @Override
  public void addRecordString(String content) {}

  @Override
  public void addMetaData(MetaData metaData) throws IOException {
    if (!moveToWriteRow) {
      rMetaData = metaData;
      init();
      if (metaData == null) {
        writeLine(serializer.metaDataToBytes(metaData), true);
      } else {
        writeLine(serializer.metaDataToBytes(metaData), false);
      }
      moveToWriteRow = true;
    }
  }

  @Override
  public void addRecord(Record record) {
    if (moveToWriteRow) {
      rowCount++;
      try {
        writeLine(serializer.recordToBytes(record), false);
      } catch (IOException e) {
        logger.warn("addMetaDataAndRecordString failed", e);
      }
    }
  }

  public void closeFs() {
    if (fs != null) {
      IOUtils.closeQuietly(fs);
      fs = null;
    }
  }

  @Override
  public void close() {
    if (closed) {
      logger.warn("the writer had been closed, but close() was still called.");
      return;
    }
    synchronized (WRITER_LOCK_CLOSE) {
      if (!closed) {
        closed = true;
      } else {
        return;
      }
    }
    try {
      if (outputStream != null) {
        flush();
      }
    } finally {
      if (outputStream != null) {
        IOUtils.closeQuietly(outputStream);
        outputStream = null;
      }
      closeFs();
    }
  }

  @Override
  public void flush() {
    createNewFile();
    if (outputStream != null) {
      try {
        if (!buffer.isEmpty()) {
          outputStream.write(getBytes());
          buffer.clear();
        }
        if (outputStream instanceof HdfsDataOutputStream) {
          ((HdfsDataOutputStream) outputStream).hflush();
        } else {
          outputStream.flush();
        }
      } catch (IOException e) {
        logger.warn("Error encountered when flush result set", e);
      }
    }
    if (closed && logger.isDebugEnabled()) {
      logger.debug("the writer had been closed, but flush() was still called.");
    }
  }
}
