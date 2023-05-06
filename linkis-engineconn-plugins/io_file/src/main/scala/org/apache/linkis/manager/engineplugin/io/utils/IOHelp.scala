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

package org.apache.linkis.manager.engineplugin.io.utils

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.storage.domain.{MethodEntity, MethodEntitySerializer}
import org.apache.linkis.storage.errorcode.LinkisIoFileErrorCodeSummary.{
  CANNOT_BE_EMPTY,
  PARAMETER_CALLS
}
import org.apache.linkis.storage.exception.StorageErrorException
import org.apache.linkis.storage.resultset.{
  ResultSetFactory,
  ResultSetReaderFactory,
  ResultSetWriterFactory
}
import org.apache.linkis.storage.resultset.io.{IOMetaData, IORecord}
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}

import org.apache.commons.io.IOUtils

object IOHelp {

  private val maxPageSize = StorageConfiguration.IO_PROXY_READ_FETCH_SIZE.getValue.toLong

  /**
   * 读出内容后，通过将bytes数组转换为base64加密的字符串 现在ujes之前不能直接传输bytes，所以通过base64保真
   * @param fs
   * @param method
   * @return
   */
  def read(fs: Fs, method: MethodEntity): String = {
    if (method.getParams == null || method.getParams.isEmpty) {
      throw new StorageErrorException(CANNOT_BE_EMPTY.getErrorCode, CANNOT_BE_EMPTY.getErrorDesc)
    }
    val dest = MethodEntitySerializer.deserializerToJavaObject(
      method.getParams()(0).asInstanceOf[String],
      classOf[FsPath]
    )
    val inputStream = fs.read(dest)
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.IO_TYPE)
    val writer = ResultSetWriterFactory.getResultSetWriter(resultSet, Long.MaxValue, null)
    Utils.tryFinally {
      if (method.getParams.length == 1) {
        val bytes = IOUtils.toByteArray(inputStream)
        val ioMetaData = new IOMetaData(0, bytes.length)
        val ioRecord = new IORecord(bytes)
        writer.addMetaData(ioMetaData)
        writer.addRecord(ioRecord)
        writer.toString()
      } else if (method.getParams.length == 3) {
        val position =
          if (method.getParams()(1).toString.toInt < 0) {
            0
          } else {
            method.getParams()(1).toString.toInt
          }
        val fetchSize =
          if (method.getParams()(2).toString.toInt > maxPageSize) {
            maxPageSize.toInt
          } else {
            method.getParams()(2).toString.toInt
          }
        if (position > 0) {
          inputStream.skip(position)
        }
        val bytes = new Array[Byte](fetchSize)
        val len = StorageUtils.readBytes(inputStream, bytes, fetchSize)
        val ioMetaData = new IOMetaData(0, len)
        val ioRecord = new IORecord(bytes.slice(0, len))
        writer.addMetaData(ioMetaData)
        writer.addRecord(ioRecord)
        writer.toString()
      } else {
        throw new StorageErrorException(PARAMETER_CALLS.getErrorCode, PARAMETER_CALLS.getErrorDesc)
      }
    }(IOUtils.closeQuietly(inputStream))
  }

  /**
   * 将穿过来的base64加密的内容转换为bytes数组写入文件
   * @param fs
   * @param method
   */
  def write(fs: Fs, method: MethodEntity): Unit = {
    if (method.getParams == null || method.getParams.isEmpty) {
      throw new StorageErrorException(PARAMETER_CALLS.getErrorCode, PARAMETER_CALLS.getErrorDesc)
    }
    val dest = MethodEntitySerializer.deserializerToJavaObject(
      method.getParams()(0).asInstanceOf[String],
      classOf[FsPath]
    )
    val overwrite = method.getParams()(1).asInstanceOf[Boolean]
    val outputStream = fs.write(dest, overwrite)
    val content = method.getParams()(2).asInstanceOf[String]
    Utils.tryFinally {
      val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.IO_TYPE)
      val reader = ResultSetReaderFactory.getResultSetReader(resultSet, content)
      while (reader.hasNext) {
        IOUtils.write(reader.getRecord.asInstanceOf[IORecord].value, outputStream)
      }
    }(IOUtils.closeQuietly(outputStream))
  }

}
