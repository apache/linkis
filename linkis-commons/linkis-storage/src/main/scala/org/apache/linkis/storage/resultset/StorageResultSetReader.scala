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
 
package org.apache.linkis.storage.resultset

import java.io.{ByteArrayInputStream, IOException, InputStream}

import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetReader}
import org.apache.linkis.common.io.{MetaData, Record}
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.exception.StorageWarnException
import org.apache.linkis.storage.utils.StorageUtils

import scala.collection.mutable.ArrayBuffer



class StorageResultSetReader[K <: MetaData, V <: Record](resultSet: ResultSet[K, V], inputStream: InputStream) extends ResultSetReader[K, V](resultSet, inputStream) with Logging{

  private val deserializer = resultSet.createResultSetDeserializer
  private var metaData: K = _
  private var row: Record = _
  private var colCount = 0
  private var rowCount = 0

  private val READ_CACHE = 1024
  private val bytes = new Array[Byte](READ_CACHE)


  def this(resultSet: ResultSet[K, V], value: String) = {
    this(resultSet, new ByteArrayInputStream(value.getBytes(Dolphin.CHAR_SET)))
  }

  def init(): Unit = {
    val resType = Dolphin.getType(inputStream)
    if (resultSet.resultSetType != resType) throw new IOException("File type does not match(文件类型不匹配): " + ResultSetFactory.resultSetType.getOrElse(resType, "TABLE"))
  }

  /**
    * Read a row of data
   Read the line length first
  * Get the entire row of data by the length of the line, first obtain the column length in the entire row of data,
    * and then divide into column length to split the data
    * 读取一行数据
    * 先读取行长度
    * 通过行长度获取整行数据，在整行数据中先获取列长度，进而分割成列长度从而分割数据
    * @return
    */
  def readLine(): Array[Byte] = {

    var rowLen = 0
    try rowLen = Dolphin.readInt(inputStream)
    catch {
      case t:StorageWarnException => info(s"Read finished(读取完毕)") ; return null
      case t: Throwable => throw t
    }

    val rowBuffer = ArrayBuffer[Byte]()
    var len = 0

    //Read the entire line, except for the data of the line length(读取整行，除了行长的数据)
    while (rowLen > 0 && len >= 0) {
      if (rowLen > READ_CACHE)
        len = StorageUtils.readBytes(inputStream,bytes, READ_CACHE)
      else
        len = StorageUtils.readBytes(inputStream,bytes, rowLen)

      if (len > 0) {
        rowLen -= len
        rowBuffer ++= bytes.slice(0, len)
      }
    }
    rowCount = rowCount + 1
    rowBuffer.toArray
  }

  @scala.throws[IOException]
  override def getRecord: Record = {
    if (metaData == null) throw new IOException("Must read metadata first(必须先读取metadata)")
    if (row ==  null) throw new IOException("Can't get the value of the field, maybe the IO stream has been read or has been closed!(拿不到字段的值，也许IO流已读取完毕或已被关闭！)")
    row
  }

  @scala.throws[IOException]
  override def getMetaData: MetaData = {
    if(metaData == null) init()
    metaData = deserializer.createMetaData(readLine())
    metaData
  }

  @scala.throws[IOException]
  override def skip(recordNum: Int): Int = {
    if(recordNum < 0 ) return -1

    if(metaData == null) getMetaData
    for(i <- recordNum until (0, -1)){
      try inputStream.skip(Dolphin.readInt(inputStream)) catch { case t: Throwable => return -1}
    }
    recordNum
  }

  @scala.throws[IOException]
  override def getPosition: Long = rowCount

  @scala.throws[IOException]
  override def hasNext: Boolean = {
    if(metaData == null) getMetaData
    val line = readLine()
    if(line == null) return  false
    row = deserializer.createRecord(line)
    if(row == null) return  false
    true
  }

  @scala.throws[IOException]
  override def available: Long = inputStream.available()

  override def close(): Unit = inputStream.close()
}
