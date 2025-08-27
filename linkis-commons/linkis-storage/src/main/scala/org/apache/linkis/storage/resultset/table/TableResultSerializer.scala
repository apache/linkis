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

package org.apache.linkis.storage.resultset.table

import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.io.resultset.ResultSerializer
import org.apache.linkis.storage.conf.LinkisStorageConf
import org.apache.linkis.storage.domain.Dolphin

import scala.collection.mutable.ArrayBuffer

class TableResultSerializer extends ResultSerializer {

  override def metaDataToBytes(metaData: MetaData): Array[Byte] = {
    val tableMetaData = metaData.asInstanceOf[TableMetaData]
    lineToBytes(tableMetaData.columns.map(_.toArray).reduce((a1, a2) => a1 ++ a2))
  }

  override def recordToBytes(record: Record): Array[Byte] = {
    val tableRecord = record.asInstanceOf[TableRecord]
    lineToBytes(tableRecord.row)
  }

  /**
   * Convert a row of data to an array of Bytes Convert the data to byte and get the corresponding
   * total byte length to write to the file Data write format: line length (fixed length) column
   * length (fixed length) field index comma segmentation real data For example:
   * 000000004900000000116,10,3,4,5,peace1johnnwang1101true11.51 The length of the line does not
   * include its own length 将一行数据转换为Bytes的数组 对数据转换为byte，并获取相应的总byte长度写入文件 数据写入格式：行长(固定长度) 列长(固定长度)
   * 字段索引逗号分割 真实数据 如：000000004900000000116,10,3,4,5,peace1johnnwang1101true11.51 其中行长不包括自身长度
   * @param line
   */
  def lineToBytes(line: Array[Any]): Array[Byte] = {
    val dataBytes = ArrayBuffer[Array[Byte]]()
    val colIndex = ArrayBuffer[Array[Byte]]()
    var colByteLen = 0
    var length = 0
    line.foreach { data =>
      val bytes = if (data == null) {
        if (!LinkisStorageConf.LINKIS_RESULT_ENABLE_NULL) {
          Dolphin.LINKIS_NULL_BYTES
        } else {
          Dolphin.NULL_BYTES
        }
      } else {
        Dolphin.getBytes(data)
      }
      dataBytes += bytes
      val colBytes = Dolphin.getBytes(bytes.length)
      colIndex += colBytes += Dolphin.COL_SPLIT_BYTES
      colByteLen += colBytes.length + Dolphin.COL_SPLIT_LEN
      length += bytes.length
    }
    length += colByteLen + Dolphin.INT_LEN
    toByteArray(length, colByteLen, colIndex, dataBytes)
  }

  /**
   * Splice a row of data into a byte array(将一行的数据拼接成byte数组)
   * @param length
   *   The total length of the line data byte, excluding its own length(行数据byte总长度，不包括自身的长度)
   * @param colByteLen
   *   Record field index byte column length(记录字段索引byte的列长)
   * @param colIndex
   *   Field index, including separator comma(字段索引，包括分割符逗号)
   * @param dataBytes
   *   Byte of real data(真实数据的byte)
   * @return
   */
  def toByteArray(
      length: Int,
      colByteLen: Int,
      colIndex: ArrayBuffer[Array[Byte]],
      dataBytes: ArrayBuffer[Array[Byte]]
  ): Array[Byte] = {
    val row = ArrayBuffer[Byte]()
    colIndex ++= dataBytes
    row.appendAll(Dolphin.getIntBytes(length))
    row.appendAll(Dolphin.getIntBytes(colByteLen))
    colIndex.foreach(row.appendAll(_))
    row.toArray
  }

}
