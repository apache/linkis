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
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.common.io.resultset.ResultSetReader;
import org.apache.linkis.common.io.resultset.ResultSetWriter;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.hadoop.common.conf.HadoopConf;
import org.apache.linkis.storage.LineMetaData;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.resultset.ResultSetFactory;
import org.apache.linkis.storage.resultset.ResultSetReaderFactory;
import org.apache.linkis.storage.resultset.ResultSetWriterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.utils.CloseableUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.CONFIGURATION_NOT_READ;

public class StorageUtils {
  private static final Logger logger = LoggerFactory.getLogger(StorageUtils.class);

  public static final String HDFS = "hdfs";
  public static final String FILE = "file";
  public static final String OSS = "oss";
  public static final String S3 = "s3";

  public static final String FILE_SCHEMA = "file://";
  public static final String HDFS_SCHEMA = "hdfs://";
  public static final String OSS_SCHEMA = "oss://";
  public static final String S3_SCHEMA = "s3://";

  private static final NumberFormat nf = NumberFormat.getInstance();

  static {
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits((int) StorageConfiguration.DOUBLE_FRACTION_LEN.getValue());
  }

  public static String doubleToString(double value) {
    return nf.format(value);
  }

  public static <T> Map<String, T> loadClass(String classStr, Function<T, String> op) {
    String[] _classes = classStr.split(",");
    LinkedHashMap<String, T> classes = new LinkedHashMap<>();
    for (String clazz : _classes) {
      try {
        T obj = Utils.getClassInstance(clazz.trim());
        classes.put(op.apply(obj), obj);
      } catch (Exception e) {
        logger.warn("StorageUtils loadClass failed", e);
      }
    }
    return classes;
  }

  public static <T> Map<String, Class<T>> loadClasses(
      String classStr, String pge, Function<Class<T>, String> op) {
    String[] _classes =
        StringUtils.isEmpty(pge)
            ? classStr.split(",")
            : Stream.of(StringUtils.split(classStr, ','))
                .map(value -> pge + "." + value)
                .toArray(String[]::new);
    Map<String, Class<T>> classes = new LinkedHashMap<>();
    for (String clazz : _classes) {
      try {
        Class<T> _class =
            (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(clazz.trim());
        classes.put(op.apply(_class), _class);
      } catch (Exception e) {
        logger.warn("StorageUtils loadClasses failed", e);
      }
    }
    return classes;
  }

  public static String pathToSuffix(String path) {
    String fileName = new File(path).getName();
    if (fileName.length() > 0) {
      int dot = fileName.lastIndexOf('.');
      if (dot > -1 && dot < fileName.length() - 1) {
        return fileName.substring(dot + 1);
      }
    }
    return fileName;
  }

  public static Object invoke(Object obj, Method method, Object[] args) throws Exception {
    return method.invoke(obj, args);
  }

  /**
   * Serialized string is a result set of type Text(序列化字符串为Text类型的结果集)
   *
   * @param value
   * @return
   */
  public static String serializerStringToResult(String value) throws IOException {
    ResultSet resultSet =
        ResultSetFactory.getInstance().getResultSetByType(ResultSetFactory.TEXT_TYPE);
    ResultSetWriter writer =
        ResultSetWriterFactory.getResultSetWriter(resultSet, Long.MAX_VALUE, null);
    LineMetaData metaData = new LineMetaData(null);
    LineRecord record = new LineRecord(value);
    writer.addMetaData(metaData);
    writer.addRecord(record);
    String res = writer.toString();
    IOUtils.closeQuietly(writer);
    return res;
  }

  /**
   * The result set of serialized text is a string(序列化text的结果集为字符串)
   *
   * @param result
   * @return
   */
  public static String deserializerResultToString(String result) throws IOException {
    ResultSet resultSet =
        ResultSetFactory.getInstance().getResultSetByType(ResultSetFactory.TEXT_TYPE);
    ResultSetReader reader = ResultSetReaderFactory.getResultSetReader(resultSet, result);
    StringBuilder sb = new StringBuilder();
    while (reader.hasNext()) {
      LineRecord record = (LineRecord) reader.getRecord();
      sb.append(record.getLine());
    }
    reader.close();
    return sb.toString();
  }

  public static void close(OutputStream outputStream) {
    close(outputStream, null, null);
  }

  public static void close(InputStream inputStream) {
    close(null, inputStream, null);
  }

  public static void close(Fs fs) {
    close(null, null, fs);
  }

  public static void close(OutputStream outputStream, InputStream inputStream, Fs fs) {
    try {
      if (outputStream != null) outputStream.close();
    } catch (IOException e) {
      // ignore exception
    }
    try {
      if (inputStream != null) inputStream.close();
    } catch (IOException e) {
      // ignore exception
    }
    try {
      if (fs != null) fs.close();
    } catch (IOException e) {
      // ignore exception
    }
  }

  public static void close(Closeable closeable) {
    CloseableUtils.closeQuietly(closeable);
  }

  public static String getJvmUser() {
    return System.getProperty("user.name");
  }

  public static boolean isHDFSNode() {
    File confPath = new File(HadoopConf.hadoopConfDir());
    // TODO IO-client mode need return false
    if (!confPath.exists() || confPath.isFile()) {
      throw new StorageWarnException(
          CONFIGURATION_NOT_READ.getErrorCode(), CONFIGURATION_NOT_READ.getErrorDesc());
    } else return true;
  }

  /**
   * Returns the FsPath by determining whether the path is a schema. By default, the FsPath of the
   * file is returned.
   *
   * @param path
   * @return
   */
  public static FsPath getFsPath(String path) {
    if (path.startsWith(FILE_SCHEMA) || path.startsWith(HDFS_SCHEMA)) {
      return new FsPath(path);
    } else {
      return new FsPath(FILE_SCHEMA + path);
    }
  }

  public static int readBytes(InputStream inputStream, byte[] bytes, int len) {
    int readLen = 0;
    try {
      int count = 0;
      // 当使用s3存储结果文件时时，com.amazonaws.services.s3.model.S3InputStream无法正确读取.dolphin文件。需要在循环条件添加:
      // readLen >= 0
      // To resolve the issue when using S3 to store result files and
      // com.amazonaws.services.s3.model.S3InputStream to read .dolphin files, you need to add the
      // condition readLen >= 0 in the loop.
      while (readLen < len && readLen >= 0) {
        count = inputStream.read(bytes, readLen, len - readLen);

        if (count == -1 && inputStream.available() < 1) {
          return readLen;
        }
        readLen += count;
      }
    } catch (IOException e) {
      logger.warn("FileSystemUtils readBytes failed", e);
    }
    return readLen;
  }

  public static String colToString(Object col, String nullValue) {
    if (col == null) {
      return nullValue;
    } else if (col instanceof Double) {
      return doubleToString((Double) col);
    } else if ("NULL".equals(col) || "".equals(col)) {
      return nullValue;
    } else {
      return col.toString();
    }
  }

  public static String colToString(Object col) {
    return colToString(col, "NULL");
  }

  public static boolean isIOProxy() {
    return (boolean) StorageConfiguration.ENABLE_IO_PROXY.getValue();
  }

  public static byte[] mergeByteArrays(byte[] arr1, byte[] arr2) {
    byte[] mergedArray = new byte[arr1.length + arr2.length];
    System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
    System.arraycopy(arr2, 0, mergedArray, arr1.length, arr2.length);
    return mergedArray;
  }
}
