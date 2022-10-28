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

package org.apache.linkis.storage.domain

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.FAILED_TO_READ_INTEGER
import org.apache.linkis.storage.exception.StorageWarnException
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}

import java.io.{InputStream, IOException}

object Dolphin extends Logging {

  val CHAR_SET = StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue
  val MAGIC = "dolphin"

  val MAGIC_BYTES = MAGIC.getBytes(StorageConfiguration.STORAGE_RS_FILE_TYPE.getValue)
  val MAGIC_LEN = MAGIC_BYTES.length

  val DOLPHIN_FILE_SUFFIX = ".dolphin"

  val COL_SPLIT = ","
  val COL_SPLIT_BYTES = COL_SPLIT.getBytes("utf-8")
  val COL_SPLIT_LEN = COL_SPLIT_BYTES.length

  val NULL = "NULL"
  val NULL_BYTES = "NULL".getBytes("utf-8")

  val INT_LEN = 10

  val FILE_EMPTY = 31

  def getBytes(value: Any): Array[Byte] = {
    value.toString.getBytes(CHAR_SET)
  }

  /**
   * Convert a bytes array to a String content 将bytes数组转换为String内容
   * @param bytes
   * @param start
   * @param len
   * @return
   */
  def getString(bytes: Array[Byte], start: Int, len: Int): String =
    new String(bytes, start, len, Dolphin.CHAR_SET)

  /**
   * Read an integer value that converts the array to a byte of length 10 bytes
   * 读取整数值，该值为将数组转换为10字节长度的byte
   * @param inputStream
   * @return
   */
  def readInt(inputStream: InputStream): Int = {
    val bytes = new Array[Byte](INT_LEN + 1)
    if (StorageUtils.readBytes(inputStream, bytes, INT_LEN) != INT_LEN) {
      throw new StorageWarnException(
        FAILED_TO_READ_INTEGER.getErrorCode,
        FAILED_TO_READ_INTEGER.getErrorDesc
      )
    }
    getString(bytes, 0, INT_LEN).toInt
  }

  /**
   * Print integers at a fixed length(将整数按固定长度打印)
   * @param value
   * @return
   */
  def getIntBytes(value: Int): Array[Byte] = {
    val str = value.toString
    val res = "0" * (INT_LEN - str.length) + str
    Dolphin.getBytes(res)
  }

  def getType(inputStream: InputStream): String = {
    val bytes = new Array[Byte](100)
    val len = StorageUtils.readBytes(inputStream, bytes, Dolphin.MAGIC_LEN + INT_LEN)
    if (len == -1) return null
    getType(Dolphin.getString(bytes, 0, len))
  }

  def getType(content: String): String = {
    if (content.length < MAGIC.length || content.substring(0, MAGIC.length) != MAGIC) {
      throw new IOException(s"File header type must be dolphin,content:$content is not")
    }
    content.substring(MAGIC.length, MAGIC.length + INT_LEN).toInt.toString
  }

}
