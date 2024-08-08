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

import org.apache.linkis.common.io.resultset.ResultDeserializer
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.conf.LinkisStorageConf
import org.apache.linkis.storage.domain.{Column, DataType, Dolphin}
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.PARSING_METADATA_FAILED
import org.apache.linkis.storage.exception.{
  ColLengthExceedException,
  ColumnIndexExceedException,
  StorageErrorException
}

import org.apache.commons.lang3.StringUtils

import java.text.MessageFormat

import scala.collection.mutable.ArrayBuffer

class TableResultDeserializer extends ResultDeserializer[TableMetaData, TableRecord] with Logging {

  var metaData: TableMetaData = _

  import DataType._

  override def createMetaData(bytes: Array[Byte]): TableMetaData = {
    val colByteLen = Dolphin.getString(bytes, 0, Dolphin.INT_LEN).toInt
    val colString = Dolphin.getString(bytes, Dolphin.INT_LEN, colByteLen)
    val colArray =
      if (colString.endsWith(Dolphin.COL_SPLIT)) {
        colString.substring(0, colString.length - 1).split(Dolphin.COL_SPLIT)
      } else colString.split(Dolphin.COL_SPLIT)
    var index = Dolphin.INT_LEN + colByteLen
    if (colArray.length % 3 != 0) {
      throw new StorageErrorException(
        PARSING_METADATA_FAILED.getErrorCode,
        PARSING_METADATA_FAILED.getErrorDesc
      )
    }
    val columns = new ArrayBuffer[Column]()
    for (i <- 0 until (colArray.length, 3)) {
      var len = colArray(i).toInt
      val colName = Dolphin.toStringValue(Dolphin.getString(bytes, index, len))
      index += len
      len = colArray(i + 1).toInt
      val colType = Dolphin.toStringValue(Dolphin.getString(bytes, index, len))
      index += len
      len = colArray(i + 2).toInt
      val colComment = Dolphin.toStringValue(Dolphin.getString(bytes, index, len))
      index += len
      columns += Column(colName, colType, colComment)
    }
    metaData = new TableMetaData(columns.toArray)
    metaData
  }

  /**
   * colByteLen:All column fields are long(所有列字段长 记录的长度) colString：Obtain column
   * length(获得列长)：10，20，21 colArray：Column length array(列长数组) Get data by column length(通过列长获得数据)
   * @param bytes
   * @return
   */
  override def createRecord(bytes: Array[Byte]): TableRecord = {
    val colByteLen = Dolphin.getString(bytes, 0, Dolphin.INT_LEN).toInt
    val colString = Dolphin.getString(bytes, Dolphin.INT_LEN, colByteLen)
    val colArray =
      if (colString.endsWith(Dolphin.COL_SPLIT)) {
        colString.substring(0, colString.length - 1).split(Dolphin.COL_SPLIT)
      } else colString.split(Dolphin.COL_SPLIT)
    var index = Dolphin.INT_LEN + colByteLen
    var enableLimit: Boolean = false
    if (StringUtils.isNotBlank(LinkisStorageConf.enableLimitThreadLocal.get())) {
      enableLimit = true
    }
    val columnIndices: Array[Int] = LinkisStorageConf.columnIndicesThreadLocal.get()

    val lastIndex =
      if (columnIndices != null && columnIndices.length > 0) columnIndices(columnIndices.length - 1)
      else 0
    var realValueSize = colArray.size

    if (enableLimit && metaData.columns.size <= columnIndices(0)) {
      throw new ColumnIndexExceedException(
        LinkisStorageErrorCodeSummary.RESULT_COLUMN_INDEX_OUT_OF_BOUNDS.getErrorCode,
        MessageFormat.format(
          LinkisStorageErrorCodeSummary.RESULT_COLUMN_INDEX_OUT_OF_BOUNDS.getErrorDesc,
          columnIndices(0).asInstanceOf[Object],
          metaData.columns.size.asInstanceOf[Object]
        )
      )
    }

    if (enableLimit && metaData.columns.size > lastIndex) {
      realValueSize = columnIndices.length
    } else if (enableLimit && metaData.columns.size <= lastIndex) {
      realValueSize = metaData.columns.size % columnIndices.length
    }

    val columnSize = colArray.size
    val rowArray = new Array[Any](realValueSize)

    var colIdx = 0
    for (i <- 0 until columnSize) {
      val len = colArray(i).toInt
      val res = Dolphin.getString(bytes, index, len)
      if (res.length > LinkisStorageConf.LINKIS_RESULT_COL_LENGTH && enableLimit) {
        throw new ColLengthExceedException(
          LinkisStorageErrorCodeSummary.RESULT_COL_LENGTH.getErrorCode,
          MessageFormat.format(
            LinkisStorageErrorCodeSummary.RESULT_COL_LENGTH.getErrorDesc,
            res.length.asInstanceOf[Object],
            LinkisStorageConf.LINKIS_RESULT_COL_LENGTH.asInstanceOf[Object]
          )
        )
      }
      index += len
      // 如果enableLimit为true，则采取的是列分页
      if (enableLimit) {
        if (columnIndices.contains(i)) {
          rowArray(colIdx) = res
          colIdx += 1
        }
      } else {
        if (i >= metaData.columns.length) rowArray(i) = res
        else {
          rowArray(i) = toValue(metaData.columns(i).dataType, res)
        }
      }
    }
    new TableRecord(rowArray)
  }

}
