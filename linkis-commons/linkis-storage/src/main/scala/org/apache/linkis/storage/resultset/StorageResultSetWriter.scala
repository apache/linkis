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

import org.apache.linkis.common.io.{Fs, FsPath, MetaData, Record}
import org.apache.linkis.common.io.resultset.{ResultSerializer, ResultSet, ResultSetWriter}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.conf.LinkisStorageConf
import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.utils.{FileSystemUtils, StorageUtils}

import org.apache.commons.io.IOUtils
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream

import java.io.{IOException, OutputStream}

import scala.collection.mutable.ArrayBuffer

class StorageResultSetWriter[K <: MetaData, V <: Record](
    resultSet: ResultSet[K, V],
    maxCacheSize: Long,
    storePath: FsPath
) extends ResultSetWriter[K, V](
      resultSet = resultSet,
      maxCacheSize = maxCacheSize,
      storePath = storePath
    )
    with Logging {

  private val serializer: ResultSerializer = resultSet.createResultSetSerializer

  private var moveToWriteRow = false

  private var outputStream: OutputStream = _

  private var rowCount = 0

  private val buffer = new ArrayBuffer[Byte]()

  private var fs: Fs = _

  private var rMetaData: MetaData = _

  private var proxyUser: String = StorageUtils.getJvmUser

  private var fileCreated = false

  private var closed = false

  private val WRITER_LOCK_CREATE = new Object()

  private val WRITER_LOCK_CLOSE = new Object()

  def getMetaData: MetaData = rMetaData

  def setProxyUser(proxyUser: String): Unit = {
    this.proxyUser = proxyUser
  }

  def isEmpty: Boolean = {
    rMetaData == null && buffer.length <= Dolphin.FILE_EMPTY
  }

  def init(): Unit = {
    writeLine(resultSet.getResultSetHeader, true)
  }

  def createNewFile: Unit = {
    if (!fileCreated) {
      WRITER_LOCK_CREATE.synchronized {
        if (!fileCreated) {
          if (storePath != null && outputStream == null) {
            fs = FSFactory.getFsByProxyUser(storePath, proxyUser)
            fs.init(null)
            FileSystemUtils.createNewFile(storePath, proxyUser, true)
            outputStream = fs.write(storePath, true)
            logger.info(s"Succeed to create a new file:$storePath")
            fileCreated = true
          }
        }
      }
    } else if (null != storePath && null == outputStream) {
      logger.warn("outputStream had been set null, but createNewFile() was called again.")
    }
  }

  def writeLine(bytes: Array[Byte], cache: Boolean = false): Unit = {
    if (closed) {
      logger.warn("the writer had been closed, but writeLine() was still called.")
      return
    }
    if (bytes.length > LinkisStorageConf.ROW_BYTE_MAX_LEN) {
      throw new IOException(
        s"A single row of data cannot exceed ${LinkisStorageConf.ROW_BYTE_MAX_LEN_STR}"
      )
    }
    if (buffer.length > maxCacheSize && !cache) {
      if (outputStream == null) {
        createNewFile
      }
      flush()
      outputStream.write(bytes)
    } else {
      buffer.appendAll(bytes)
    }
  }

  override def toString: String = {
    if (outputStream == null) {
      if (isEmpty) return ""
      new String(buffer.toArray, Dolphin.CHAR_SET)
    } else {
      storePath.getSchemaPath
    }
  }

  override def toFSPath: FsPath = storePath

  override def addMetaDataAndRecordString(content: String): Unit = {
    if (!moveToWriteRow) {
      val bytes = content.getBytes(Dolphin.CHAR_SET)
      writeLine(bytes)
    }
    moveToWriteRow = true
  }

  override def addRecordString(content: String): Unit = {}

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    if (!moveToWriteRow) {
      {
        rMetaData = metaData
        init()
        if (null == metaData) {
          writeLine(serializer.metaDataToBytes(metaData), true)
        } else {
          writeLine(serializer.metaDataToBytes(metaData))
        }
      }
      moveToWriteRow = true
    }
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    if (moveToWriteRow) {
      rowCount = rowCount + 1
      writeLine(serializer.recordToBytes(record))
    }
  }

  def closeFs: Unit = {
    if (fs != null) {
      IOUtils.closeQuietly(fs)
      fs = null
    }
  }

  override def close(): Unit = {
    if (closed) {
      logger.warn("the writer had been closed, but close() was still called.")
      return
    } else {
      WRITER_LOCK_CLOSE.synchronized {
        if (!closed) {
          closed = true
        } else {
          return
        }
      }
    }
    Utils.tryFinally(if (outputStream != null) flush()) {
      closeFs
      if (outputStream != null) {
        IOUtils.closeQuietly(outputStream)
        outputStream = null
      }
    }
  }

  override def flush(): Unit = {
    createNewFile
    if (outputStream != null) {
      if (buffer.nonEmpty) {
        outputStream.write(buffer.toArray)
        buffer.clear()
      }
      Utils.tryAndWarnMsg[Unit] {
        outputStream match {
          case hdfs: HdfsDataOutputStream =>
            hdfs.hflush()
          case _ =>
            outputStream.flush()
        }
      }(s"Error encounters when flush result set ")
    }
    if (closed) {
      if (logger.isDebugEnabled()) {
        logger.debug("the writer had been closed, but flush() was still called.")
      }
    }
  }

}
