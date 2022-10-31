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

package org.apache.linkis.entrance.log

import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary.LOGPATH_NOT_NULL
import org.apache.linkis.entrance.exception.EntranceErrorException
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.FileSystemUtils

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream

import java.io.{Closeable, Flushable, OutputStream}
import java.util

abstract class LogWriter(charset: String) extends Closeable with Flushable with Logging {

  private var firstWrite = true

  protected var outputStream: OutputStream

  def write(msg: String): Unit = synchronized {
    val log =
      if (!firstWrite) "\n" + msg
      else {
        logger.info(s"$toString write first one line log")
        firstWrite = false
        msg
      }
    Utils.tryAndWarnMsg {
      outputStream.write(log.getBytes(charset))
      outputStream.flush()
    }(s"$toString error when write query log to outputStream.")
  }

  def flush(): Unit = Utils.tryAndWarnMsg[Unit] {
    outputStream match {
      case hdfs: HdfsDataOutputStream =>
        // todo check
        hdfs.hflush()
      case _ =>
        outputStream.flush()
    }
  }(s"$toString Error encounters when flush log, ")

  def close(): Unit = {
    logger.info(s" $toString logWriter close")
    flush()
    if (outputStream != null) {
      Utils.tryQuietly(outputStream.close())
      outputStream = null
    }
  }

}

abstract class AbstractLogWriter(logPath: String, user: String, charset: String)
    extends LogWriter(charset) {

  if (StringUtils.isBlank(logPath)) {
    throw new EntranceErrorException(LOGPATH_NOT_NULL.getErrorCode, LOGPATH_NOT_NULL.getErrorDesc)
  }

  protected var fileSystem =
    FSFactory.getFsByProxyUser(new FsPath(logPath), user).asInstanceOf[FileSystem]

  fileSystem.init(new util.HashMap[String, String]())

  override protected var outputStream: OutputStream = {
    FileSystemUtils.createNewFileWithFileSystem(fileSystem, new FsPath(logPath), user, true)
    fileSystem.write(new FsPath(logPath), true)
  }

  override def close(): Unit = {
    super.close()
    if (fileSystem != null) Utils.tryAndWarnMsg {
      fileSystem.close()
      fileSystem = null
    }(s"$toString Error encounters when closing fileSystem")
  }

  override def toString: String = logPath
}
