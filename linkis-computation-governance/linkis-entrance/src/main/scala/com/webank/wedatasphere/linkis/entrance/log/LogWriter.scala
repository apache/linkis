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

package com.webank.wedatasphere.linkis.entrance.log

import java.io.{Closeable, Flushable, OutputStream}
import java.util

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.storage.FSFactory
import com.webank.wedatasphere.linkis.storage.utils.FileSystemUtils
import org.apache.commons.lang.StringUtils
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream


abstract class LogWriter(charset: String) extends Closeable with Flushable with Logging {

  private var firstWrite = true

  protected val outputStream: OutputStream

  def write(msg: String): Unit = synchronized {
    val log = if (!firstWrite) "\n" + msg else {
      firstWrite = false
      msg
    }
    Utils.tryQuietly({
      outputStream.write(log.getBytes(charset))
      outputStream.flush()
    }, t => {
      warn("error when write query log to outputStream.", t)
      info(msg)
    })
  }



  def flush(): Unit = Utils.tryAndWarnMsg[Unit] {
    outputStream match {
      case hdfs: HdfsDataOutputStream =>
        // todo check
        hdfs.hsync()
      case _ =>
        outputStream.flush()
    }
  }("Error encounters when flush log, ")

  def close(): Unit = {
    info(s" $toString logWriter close")
    flush()
    if (outputStream != null) {
      Utils.tryCatch{
        outputStream.close()
      }{
        case t:Throwable => //ignore
      }
    }
  }
}

abstract class AbstractLogWriter(logPath: String,
                                 user: String,
                                 charset: String) extends LogWriter(charset) {
  if(StringUtils.isBlank(logPath)) throw new EntranceErrorException(20301, "logPath cannot be empty.")
  protected val fileSystem = FSFactory.getFsByProxyUser(new FsPath(logPath), user)
  fileSystem.init(new util.HashMap[String, String]())

  protected val outputStream: OutputStream = {
    FileSystemUtils.createNewFile(new FsPath(logPath), user, true)
    fileSystem.write(new FsPath(logPath), true)
  }

  override def close(): Unit = {
    super.close()
    if (fileSystem != null) Utils.tryQuietly(fileSystem.close(), t => {
      warn("Error encounters when closing fileSystem", t)
    })
  }
}