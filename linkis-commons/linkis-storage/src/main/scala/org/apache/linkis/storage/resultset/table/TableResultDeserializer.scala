/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.storage.resultset.table

import org.apache.linkis.common.io.resultset.ResultDeserializer
import org.apache.linkis.storage.domain.{Column, DataType, Dolphin}
import org.apache.linkis.storage.exception.StorageErrorException

import scala.collection.mutable.ArrayBuffer


class TableResultDeserializer extends ResultDeserializer[TableMetaData, TableRecord]{

  var metaData: TableMetaData = _

  import DataType._

  override def createMetaData(bytes: Array[Byte]): TableMetaData = {
    val colByteLen = Dolphin.getString(bytes, 0, Dolphin.INT_LEN).toInt
    val colString = Dolphin.getString(bytes, Dolphin.INT_LEN, colByteLen)
    val colArray = if(colString.endsWith(Dolphin.COL_SPLIT)) colString.substring(0, colString.length -1).split(Dolphin.COL_SPLIT) else colString.split(Dolphin.COL_SPLIT)
    var index = Dolphin.INT_LEN + colByteLen
    if(colArray.length % 3 != 0) throw new StorageErrorException(52001,"Parsing metadata failed(解析元数据失败)")
    val columns = new  ArrayBuffer[Column]()
    for(i <-  0 until (colArray.length, 3)){
      var len = colArray(i).toInt
      val colName = Dolphin.getString(bytes, index, len)
      index += len
      len = colArray(i + 1).toInt
      val colType = Dolphin.getString(bytes, index, len)
      index += len
      len = colArray(i + 2).toInt
      val colComment = Dolphin.getString(bytes, index, len)
      index += len
      columns += Column(colName, colType, colComment)
    }
    metaData = new TableMetaData(columns.toArray)
    metaData
  }

  /**
    * colByteLen:All column fields are long(所有列字段长 记录的长度)
    * colString：Obtain column length(获得列长)：10，20，21
    * colArray：Column length array(列长数组)
    * Get data by column length(通过列长获得数据)
    * @param bytes
    * @return
    */
  override def createRecord(bytes: Array[Byte]): TableRecord = {
    val colByteLen = Dolphin.getString(bytes, 0, Dolphin.INT_LEN).toInt
    val colString = Dolphin.getString(bytes, Dolphin.INT_LEN, colByteLen)
    val colArray = if(colString.endsWith(Dolphin.COL_SPLIT)) colString.substring(0, colString.length -1).split(Dolphin.COL_SPLIT) else colString.split(Dolphin.COL_SPLIT)
    var index = Dolphin.INT_LEN + colByteLen
    val data = colArray.indices.map { i =>
      val len = colArray(i).toInt
      val res = Dolphin.getString(bytes, index, len)
      index += len
      if(i >= metaData.columns.length) res
      else
        toValue(metaData.columns(i).dataType,res)
    }.toArray
    new TableRecord(data)
  }
}
