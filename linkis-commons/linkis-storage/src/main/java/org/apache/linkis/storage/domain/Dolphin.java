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

package org.apache.linkis.storage.domain;

import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.FAILED_TO_READ_INTEGER;

public class Dolphin {
  private static final Logger logger = LoggerFactory.getLogger(Dolphin.class);

  public static final Charset CHAR_SET =
      Charset.forName(StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue());
  public static final String MAGIC = "dolphin";

  public static byte[] MAGIC_BYTES = new byte[0];

  static {
    try {
      MAGIC_BYTES = MAGIC.getBytes(StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue());
    } catch (UnsupportedEncodingException e) {
      logger.warn("Dolphin getBytes failed", e);
    }
  }

  public static final int MAGIC_LEN = MAGIC_BYTES.length;

  public static final String DOLPHIN_FILE_SUFFIX = ".dolphin";

  public static final String COL_SPLIT = ",";
  public static final byte[] COL_SPLIT_BYTES = COL_SPLIT.getBytes(Charset.forName("utf-8"));
  public static final int COL_SPLIT_LEN = COL_SPLIT_BYTES.length;

  public static final String NULL = "NULL";
  public static final byte[] NULL_BYTES = "NULL".getBytes(Charset.forName("utf-8"));

  public static final int INT_LEN = 10;

  public static final int FILE_EMPTY = 31;

  public static byte[] getBytes(Object value) {
    return value.toString().getBytes(CHAR_SET);
  }

  /**
   * Convert a bytes array to a String content 将bytes数组转换为String内容
   *
   * @param bytes
   * @param start
   * @param len
   * @return
   */
  public static String getString(byte[] bytes, int start, int len) {
    return new String(bytes, start, len, Dolphin.CHAR_SET);
  }

  /**
   * Read an integer value that converts the array to a byte of length 10 bytes
   * 读取整数值，该值为将数组转换为10字节长度的byte
   *
   * @param inputStream
   * @return
   * @throws IOException
   */
  public static int readInt(InputStream inputStream) throws IOException {
    byte[] bytes = new byte[INT_LEN + 1];
    if (StorageUtils.readBytes(inputStream, bytes, INT_LEN) != INT_LEN) {
      throw new StorageWarnException(
          FAILED_TO_READ_INTEGER.getErrorCode(), FAILED_TO_READ_INTEGER.getErrorDesc());
    }
    return Integer.parseInt(getString(bytes, 0, INT_LEN));
  }

  /**
   * Print integers at a fixed length(将整数按固定长度打印)
   *
   * @param value
   * @return
   */
  public static byte[] getIntBytes(int value) {
    String str = Integer.toString(value);
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < INT_LEN - str.length(); i++) {
      res.append("0");
    }
    res.append(str);
    return Dolphin.getBytes(res.toString());
  }

  public static String getType(InputStream inputStream) throws IOException {
    byte[] bytes = new byte[100];
    int len = StorageUtils.readBytes(inputStream, bytes, Dolphin.MAGIC_LEN + INT_LEN);
    if (len == -1) return null;
    return getType(Dolphin.getString(bytes, 0, len));
  }

  public static String getType(String content) {
    if (content.length() < MAGIC.length() || !content.substring(0, MAGIC.length()).equals(MAGIC)) {
      throw new RuntimeException(
          "File header type must be dolphin, content: " + content + " is not");
    }
    return Integer.toString(
        Integer.parseInt(content.substring(MAGIC.length(), MAGIC.length() + INT_LEN)));
  }
}
