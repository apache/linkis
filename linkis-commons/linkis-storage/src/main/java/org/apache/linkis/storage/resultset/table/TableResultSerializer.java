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

package org.apache.linkis.storage.resultset.table;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSerializer;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.Dolphin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableResultSerializer extends ResultSerializer {

  @Override
  public byte[] metaDataToBytes(MetaData metaData) {
    TableMetaData tableMetaData = (TableMetaData) metaData;
    Object[] objects =
        Arrays.stream(tableMetaData.columns).map(Column::toArray).flatMap(Arrays::stream).toArray();
    return lineToBytes(objects);
  }

  @Override
  public byte[] recordToBytes(Record record) {
    TableRecord tableRecord = (TableRecord) record;
    return lineToBytes(tableRecord.row);
  }

  /**
   * Convert a row of data to an array of Bytes Convert the data to byte and get the corresponding
   * total byte length to write to the file Data write format: line length (fixed length) column
   * length (fixed length) field index comma segmentation real data For example:
   * 000000004900000000116,10,3,4,5,peace1johnnwang1101true11.51 The length of the line does not
   * include its own length 将一行数据转换为Bytes的数组 对数据转换为byte，并获取相应的总byte长度写入文件 数据写入格式：行长(固定长度) 列长(固定长度)
   * 字段索引逗号分割 真实数据 如：000000004900000000116,10,3,4,5,peace1johnnwang1101true11.51 其中行长不包括自身长度
   *
   * @param line
   */
  private byte[] lineToBytes(Object[] line) {
    // Data cache(数据缓存)
    List<byte[]> dataBytes = new ArrayList<>();
    // Column cache(列缓存)
    List<byte[]> colIndex = new ArrayList<>();
    int colByteLen = 0;
    int length = 0;
    for (Object data : line) {
      byte[] bytes = data == null ? Dolphin.NULL_BYTES : Dolphin.getBytes(data);
      dataBytes.add(bytes);
      byte[] colBytes = Dolphin.getBytes(bytes.length);
      colIndex.add(colBytes);
      colIndex.add(Dolphin.COL_SPLIT_BYTES);
      colByteLen += colBytes.length + Dolphin.COL_SPLIT_LEN;
      length += bytes.length;
    }
    length += colByteLen + Dolphin.INT_LEN;
    return toByteArray(length, colByteLen, colIndex, dataBytes);
  }

  /**
   * Splice a row of data into a byte array(将一行的数据拼接成byte数组)
   *
   * @param length The total length of the line data byte, excluding its own
   *     length(行数据byte总长度，不包括自身的长度)
   * @param colByteLen Record field index byte column length(记录字段索引byte的列长)
   * @param colIndex Field index, including separator comma(字段索引，包括分割符逗号)
   * @param dataBytes Byte of real data(真实数据的byte)
   * @return
   */
  public static byte[] toByteArray(
      int length, int colByteLen, List<byte[]> colIndex, List<byte[]> dataBytes) {
    List<Byte> row = new ArrayList<>();
    colIndex.addAll(dataBytes);

    for (byte intByte : Dolphin.getIntBytes(length)) {
      row.add(intByte);
    }

    for (byte colByte : Dolphin.getIntBytes(colByteLen)) {
      row.add(colByte);
    }

    colIndex.forEach(
        bytes -> {
          for (byte b : bytes) {
            row.add(b);
          }
        });
    byte[] result = new byte[row.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = row.get(i);
    }
    return result;
  }
}
