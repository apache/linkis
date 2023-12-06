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

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.LogReadFailedException
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.StorageUtils

import java.io.{InputStream, IOException}
import java.util

class CacheLogReader(logPath: String, charset: String, sharedCache: Cache, user: String)
    extends LogReader(charset: String) {

  val lock: Object = new Object

  def getCache: Cache = sharedCache

  var fileSystem: Fs = _

  var closed = false

  private def createInputStream: InputStream = {
    if (!logPath.contains(user)) {
      throw new LogReadFailedException(
        s"${user} does not have permission to read the path $logPath"
      )
    }
    val fsPath = new FsPath(logPath)
    if (fileSystem == null) lock synchronized {
      if (fileSystem == null) {

        fileSystem =
          if (StorageUtils.isHDFSPath(fsPath) && EntranceConfiguration.ENABLE_HDFS_JVM_USER) {
            FSFactory.getFs(new FsPath(logPath)).asInstanceOf[FileSystem]
          } else {
            FSFactory.getFsByProxyUser(new FsPath(logPath), user).asInstanceOf[FileSystem]
          }

        fileSystem.init(new util.HashMap[String, String]())
      }
    }
    val inputStream: InputStream = fileSystem.read(fsPath)
    inputStream
  }

  override def getInputStream: InputStream = {
    createInputStream
  }

  override protected def readLog(deal: String => Unit, fromLine: Int, size: Int): Int = {
    if (!sharedCache.cachedLogs.nonEmpty) return super.readLog(deal, fromLine, size)
    val min = sharedCache.cachedLogs.min
    val max = sharedCache.cachedLogs.max
    if (fromLine > max) return 0
    val from = fromLine
    val to = if (fromLine >= min) {
      if (size >= 0 && max >= fromLine + size) fromLine + size else max + 1
    } else {
      // If you are getting it from a file, you don't need to read the cached data again. In this case, you can guarantee that the log will not be missing.
      val read = super.readLog(deal, fromLine, size)
      return read
    }

    (from until to) map sharedCache.cachedLogs.get foreach deal
    to - fromLine
  }

  @throws[IOException]
  override def close(): Unit = {
    if (fileSystem != null) {
      Utils.tryQuietly(
        fileSystem.close(),
        t => {
          logger.warn("Error encounters when closing fileSystem.", t)
        }
      )
      fileSystem = null
    }
    closed = true
  }

}
