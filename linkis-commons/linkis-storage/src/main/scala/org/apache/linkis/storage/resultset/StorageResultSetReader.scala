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

package org.apache.linkis.storage.resultset

import org.apache.linkis.common.io.{Fs, MetaData, Record}
import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetReader}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.conf.LinkisStorageConf
import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.exception.{
  StorageErrorCode,
  StorageErrorException,
  StorageWarnException
}
import org.apache.linkis.storage.resultset.table.TableMetaData
import org.apache.linkis.storage.utils.StorageUtils

import java.io.{ByteArrayInputStream, InputStream, IOException}

import scala.collection.mutable.ArrayBuffer

class StorageResultSetReader[K <: MetaData, V <: Record](
    resultSet: ResultSet[K, V],
    inputStream: InputStream
) extends ResultSetReader[K, V](resultSet, inputStream)
    with Logging {

  private val deserializer = resultSet.createResultSetDeserializer
  private var metaData: K = _
  private var row: Record = _
  private var rowCount = 0

  private var fs: Fs = _

  def this(resultSet: ResultSet[K, V], value: String) = {
    this(resultSet, new ByteArrayInputStream(value.getBytes(Dolphin.CHAR_SET)))
  }

  def init(): Unit = {
    val resType = Dolphin.getType(inputStream)
    if (resultSet.resultSetType != resType) {
      throw new IOException(
        "File type does not match(文件类型不匹配): " + ResultSetFactory.resultSetType
          .getOrElse(resType, "TABLE")
      )
    }
  }

  /**
   * Read a row of data Read the line length first Get the entire row of data by the length of the
   * line, first obtain the column length in the entire row of data, and then divide into column
   * length to split the data 读取一行数据 先读取行长度 通过行长度获取整行数据，在整行数据中先获取列长度，进而分割成列长度从而分割数据
   * @return
   */
  def readLine(): Array[Byte] = {

    var rowLen = 0
    try rowLen = Dolphin.readInt(inputStream)
    catch {
      case _: StorageWarnException => logger.info(s"Read finished(读取完毕)"); return null
      case t: Throwable => throw t
    }

    var bytes: Array[Byte] = null
    try {
      bytes = new Array[Byte](rowLen)
    } catch {
      case e: OutOfMemoryError =>
        logger.error("Result set read oom, read size {} Byte", rowLen)
        throw new StorageErrorException(
          StorageErrorCode.FS_OOM.getCode,
          StorageErrorCode.FS_OOM.getMessage,
          e
        )
    }
    val len = StorageUtils.readBytes(inputStream, bytes, rowLen)
    if (len != rowLen) {
      throw new StorageErrorException(
        StorageErrorCode.INCONSISTENT_DATA.getCode,
        String.format(StorageErrorCode.INCONSISTENT_DATA.getMessage, len.toString, rowLen.toString)
      )
    }
    rowCount = rowCount + 1
    bytes
  }

  @scala.throws[IOException]
  override def getRecord: Record = {
    if (metaData == null) throw new IOException("Must read metadata first(必须先读取metadata)")
    if (row == null) {
      throw new IOException(
        "Can't get the value of the field, maybe the IO stream has been read or has been closed!(拿不到字段的值，也许IO流已读取完毕或已被关闭！)"
      )
    }
    row
  }

  def setFs(fs: Fs): Unit = this.fs = fs
  def getFs: Fs = this.fs

  @scala.throws[IOException]
  override def getMetaData: MetaData = {
    if (metaData == null) init()
    metaData = deserializer.createMetaData(readLine())
    metaData
  }

  @scala.throws[IOException]
  override def skip(recordNum: Int): Int = {
    if (recordNum < 0) return -1

    if (metaData == null) getMetaData
    for (i <- recordNum until (0, -1)) {
      try inputStream.skip(Dolphin.readInt(inputStream))
      catch {
        case t: Throwable =>
          return recordNum - i
      }
    }
    recordNum
  }

  @scala.throws[IOException]
  override def getPosition: Long = rowCount

  @scala.throws[IOException]
  override def hasNext: Boolean = {
    if (metaData == null) getMetaData
    metaData match {
      case tableMetaData: TableMetaData =>
        if (tableMetaData.columns.size > LinkisStorageConf.LINKIS_RESULT_COLUMN_SIZE) {
          logger.warn(
            s"result set columns size ${tableMetaData.columns.size} > ${LinkisStorageConf.LINKIS_RESULT_COLUMN_SIZE}"
          )
          return false
        }
      case _ =>
    }
    val line = readLine()
    if (line == null) return false
    row = deserializer.createRecord(line)
    if (row == null) return false
    true
  }

  @scala.throws[IOException]
  override def available: Long = inputStream.available()

  override def close(): Unit = {
    inputStream.close()
    if (this.fs != null) Utils.tryQuietly(this.fs.close())
  }

}
