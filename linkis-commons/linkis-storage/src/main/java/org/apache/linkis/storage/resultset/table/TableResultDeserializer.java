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

import org.apache.linkis.common.io.resultset.ResultDeserializer;
import org.apache.linkis.storage.domain.Column;
import org.apache.linkis.storage.domain.DataType;
import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.exception.StorageWarnException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.PARSING_METADATA_FAILED;

public class TableResultDeserializer extends ResultDeserializer<TableMetaData, TableRecord> {
  private static final Logger logger = LoggerFactory.getLogger(TableResultDeserializer.class);

  private TableMetaData metaData;

  @Override
  public TableMetaData createMetaData(byte[] bytes) {
    int colByteLen = Integer.parseInt(Dolphin.getString(bytes, 0, Dolphin.INT_LEN));
    String colString = Dolphin.getString(bytes, Dolphin.INT_LEN, colByteLen);
    String[] colArray =
        colString.endsWith(Dolphin.COL_SPLIT)
            ? colString.substring(0, colString.length() - 1).split(Dolphin.COL_SPLIT)
            : colString.split(Dolphin.COL_SPLIT);
    int index = Dolphin.INT_LEN + colByteLen;
    if (colArray.length % 3 != 0) {
      throw new StorageWarnException(
          PARSING_METADATA_FAILED.getErrorCode(), PARSING_METADATA_FAILED.getErrorDesc());
    }
    List<Column> columns = new ArrayList<>();
    for (int i = 0; i < colArray.length; i += 3) {
      int len = Integer.parseInt(colArray[i]);
      String colName = Dolphin.getString(bytes, index, len);
      index += len;
      len = Integer.parseInt(colArray[i + 1]);
      String colType = Dolphin.getString(bytes, index, len);
      index += len;
      len = Integer.parseInt(colArray[i + 2]);
      String colComment = Dolphin.getString(bytes, index, len);
      index += len;
      columns.add(new Column(colName, DataType.toDataType(colType), colComment));
    }
    metaData = new TableMetaData(columns.toArray(new Column[0]));
    return metaData;
  }

  /**
   * colByteLen:All column fields are long(所有列字段长 记录的长度) colString：Obtain column
   * length(获得列长)：10，20，21 colArray：Column length array(列长数组) Get data by column length(通过列长获得数据)
   *
   * @param bytes
   * @return
   */
  @Override
  public TableRecord createRecord(byte[] bytes) {
    int colByteLen = Integer.parseInt(Dolphin.getString(bytes, 0, Dolphin.INT_LEN));
    String colString = Dolphin.getString(bytes, Dolphin.INT_LEN, colByteLen);
    String[] colArray;
    if (colString.endsWith(Dolphin.COL_SPLIT)) {
      colArray = colString.substring(0, colString.length() - 1).split(Dolphin.COL_SPLIT);
    } else {
      colArray = colString.split(Dolphin.COL_SPLIT);
    }
    int index = Dolphin.INT_LEN + colByteLen;
    Object[] data = new Object[colArray.length];
    for (int i = 0; i < colArray.length; i++) {
      int len = Integer.parseInt(colArray[i]);
      String res = Dolphin.getString(bytes, index, len);
      index += len;
      if (i >= metaData.columns.length) {
        data[i] = res;
      } else {
        data[i] = DataType.toValue(metaData.columns[i].getDataType(), res);
      }
    }
    return new TableRecord(data);
  }
}
