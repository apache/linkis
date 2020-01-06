/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.storage.resultset

import java.io.{IOException, OutputStream}
import java.util

import com.webank.wedatasphere.linkis.common.io.resultset.{ResultSerializer, ResultSet, ResultSetWriter}
import com.webank.wedatasphere.linkis.common.io.{Fs, FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.domain.Dolphin
import com.webank.wedatasphere.linkis.storage.exception.StorageErrorException
import com.webank.wedatasphere.linkis.storage.utils.{FileSystemUtils, StorageUtils}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by johnnwang on 10/17/18.
  */
class StorageResultSetWriter[K <: MetaData, V <: Record](resultSet: ResultSet[K,V], maxCacheSize: Long,
                           storePath: FsPath) extends ResultSetWriter[K,V](resultSet = resultSet, maxCacheSize = maxCacheSize, storePath = storePath) with Logging{





  private val serializer: ResultSerializer = resultSet.createResultSetSerializer

  private var moveToWriteRow = false

  private var outputStream: OutputStream = _

  private var rowCount = 0

  private val buffer = new ArrayBuffer[Byte]()

  private var fs: Fs = _

  private var rMetaData: MetaData = _

  private var proxyUser:String = StorageUtils.getJvmUser

  def setProxyUser(proxyUser:String): Unit = {
    this.proxyUser = proxyUser
  }

  def isEmpty:Boolean = {
   rMetaData == null && buffer.length <= Dolphin.FILE_EMPTY
  }

  def init(): Unit = {
    writeLine(resultSet.getResultSetHeader)
  }

  def createNewFile: Unit = {
    if(storePath != null && outputStream == null){
       fs = FSFactory.getFsByProxyUser(storePath,proxyUser)
      fs.init(null)
      FileSystemUtils.createNewFile(storePath, proxyUser,true)
      outputStream = fs.write(storePath, false)
      info(s"Succeed to create a new file:$storePath")
    }
  }

  def writeLine(bytes: Array[Byte]): Unit = {
    if(buffer.length  > maxCacheSize) {
      if(outputStream == null) {
        createNewFile
      }
      flush()
      outputStream.write(bytes)
    } else {
      buffer.appendAll(bytes)
    }
  }
  override def toString: String = {
   if(outputStream == null){
     if(isEmpty) return ""
      new String(buffer.toArray,Dolphin.CHAR_SET)
    } else {
     storePath.getSchemaPath
    }
  }

  override def toFSPath: FsPath = storePath

  override def addMetaDataAndRecordString(content: String): Unit = {
    if(!moveToWriteRow){
      val bytes = content.getBytes(Dolphin.CHAR_SET)
      writeLine(bytes)
    }
    moveToWriteRow = true
  }

  override def addRecordString(content: String): Unit = {}

  @scala.throws[IOException]
  override def addMetaData(metaData: MetaData): Unit = {
    if(!moveToWriteRow) {
      {
        rMetaData = metaData
        init()
        writeLine(serializer.metaDataToBytes(metaData))
      }
      moveToWriteRow = true
    }
  }

  @scala.throws[IOException]
  override def addRecord(record: Record): Unit = {
    if(moveToWriteRow) {
      rowCount = rowCount + 1
      writeLine(serializer.recordToBytes(record))
    }
  }

  def closeFs: Unit = {
    if(fs != null)
      fs.close()
  }
  override def close(): Unit = {
    Utils.tryFinally(if(outputStream != null ) flush()){
      closeFs
      if(outputStream != null){
      outputStream.close()
    }}
  }

  override def flush(): Unit = {
    createNewFile
    if(outputStream != null) {
      if(buffer.nonEmpty) {
        outputStream.write(buffer.toArray)
        buffer.clear()
      }
      outputStream.flush()
    }
  }
}
