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
import org.apache.linkis.common.io.resultset.ResultSet
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.domain.Dolphin
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.{
  THE_FILE_IS_EMPTY,
  UNSUPPORTED_RESULT
}
import org.apache.linkis.storage.exception.{StorageErrorException, StorageWarnException}
import org.apache.linkis.storage.utils.{StorageConfiguration, StorageUtils}

import org.apache.commons.lang3.StringUtils

import java.text.MessageFormat
import java.util
import java.util.Locale

class DefaultResultSetFactory extends ResultSetFactory with Logging {

  private val resultClasses: Map[String, Class[ResultSet[ResultMetaData, ResultRecord]]] =
    StorageUtils.loadClasses(
      StorageConfiguration.STORAGE_RESULT_SET_CLASSES.getValue,
      StorageConfiguration.STORAGE_RESULT_SET_PACKAGE.getValue,
      t => t.newInstance().resultSetType().toLowerCase(Locale.getDefault)
    )

  val resultTypes = ResultSetFactory.resultSetType.keys.toArray

  override def getResultSetByType(resultSetType: String): ResultSet[_ <: MetaData, _ <: Record] = {
    if (!resultClasses.contains(resultSetType)) {
      throw new StorageErrorException(
        UNSUPPORTED_RESULT.getErrorCode,
        MessageFormat.format(UNSUPPORTED_RESULT.getErrorDesc, resultSetType)
      )
    }
    resultClasses(resultSetType).newInstance()
  }

  override def getResultSetByPath(fsPath: FsPath): ResultSet[_ <: MetaData, _ <: Record] = {
    getResultSetByPath(fsPath, StorageUtils.getJvmUser)
  }

  override def getResultSetByContent(content: String): ResultSet[_ <: MetaData, _ <: Record] = {
    getResultSetByType(Dolphin.getType(content))
  }

  override def exists(resultSetType: String): Boolean = resultClasses.contains(resultSetType)

  override def isResultSetPath(path: String): Boolean = {
    path.endsWith(Dolphin.DOLPHIN_FILE_SUFFIX)
  }

  override def isResultSet(content: String): Boolean =
    Utils.tryCatch(resultClasses.contains(Dolphin.getType(content))) { t =>
      logger.info("Wrong result Set: " + t.getMessage)
      false
    }

  override def getResultSet(output: String): ResultSet[_ <: MetaData, _ <: Record] =
    getResultSet(output, StorageUtils.getJvmUser)

  override def getResultSetType: Array[String] = resultTypes

  override def getResultSetByPath(fsPath: FsPath, fs: Fs): ResultSet[_ <: MetaData, _ <: Record] = {
    val inputStream = fs.read(fsPath)
    val resultSetType = Dolphin.getType(inputStream)
    if (StringUtils.isEmpty(resultSetType)) {
      throw new StorageWarnException(
        THE_FILE_IS_EMPTY.getErrorCode,
        MessageFormat.format(THE_FILE_IS_EMPTY.getErrorDesc, fsPath.getPath)
      )
    }
    Utils.tryQuietly(inputStream.close())
    // Utils.tryQuietly(fs.close())
    getResultSetByType(resultSetType)
  }

  override def getResultSetByPath(
      fsPath: FsPath,
      proxyUser: String
  ): ResultSet[_ <: MetaData, _ <: Record] = {
    if (fsPath == null) return null
    logger.info("Get Result Set By Path:" + fsPath.getPath)
    val fs = FSFactory.getFsByProxyUser(fsPath, proxyUser)
    fs.init(new util.HashMap[String, String]())
    val inputStream = fs.read(fsPath)
    val resultSetType = Dolphin.getType(inputStream)
    if (StringUtils.isEmpty(resultSetType)) {
      throw new StorageWarnException(
        THE_FILE_IS_EMPTY.getErrorCode,
        s"The file (${fsPath.getPath}) is empty(文件(${fsPath.getPath}) 为空)"
      )
    }
    Utils.tryQuietly(inputStream.close())
    Utils.tryQuietly(fs.close())
    getResultSetByType(resultSetType)
  }

  override def getResultSet(
      output: String,
      proxyUser: String
  ): ResultSet[_ <: MetaData, _ <: Record] = {
    if (isResultSetPath(output)) {
      getResultSetByPath(new FsPath(output), proxyUser)
    } else if (isResultSet(output)) {
      getResultSetByContent(output)
    } else null
  }

}
