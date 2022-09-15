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

package org.apache.linkis.engineplugin.spark.imexport.util

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.FileSystemUtils

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.time.DateFormatUtils

import java.io.{InputStream, OutputStream}
import java.util.Date

import scala.util.control._

/**
 */
object BackGroundServiceUtils extends Logging {

  private val CODE_STORE_PREFIX =
    CommonVars("linkis.bgservice.store.prefix", "hdfs:///tmp/bdp-ide/")

  private val CODE_STORE_SUFFIX = CommonVars("linkis.bgservice.store.suffix", "")
  private val CHARSET = "utf-8"
  private val CODE_SPLIT = ";"
  private val LENGTH_SPLIT = "#"

  def storeExecutionCode(destination: String, user: String): String = {
    if (destination.length < 60000) return null
    val path: String = getCodeStorePath(user)
    val fsPath: FsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user).asInstanceOf[FileSystem]
    fileSystem.init(null)
    var os: OutputStream = null
    var position = 0L
    val codeBytes = destination.getBytes(CHARSET)
    Utils.tryFinally {
      path.intern() synchronized {
        if (!fileSystem.exists(fsPath)) FileSystemUtils.createNewFile(fsPath, user, true)
        os = fileSystem.write(fsPath, false)
        position = fileSystem.get(path).getLength
        IOUtils.write(codeBytes, os)
      }
    } {
      if (fileSystem != null) fileSystem.close()
      IOUtils.closeQuietly(os)
    }
    val length = codeBytes.length
    path + CODE_SPLIT + position + LENGTH_SPLIT + length
  }

  def exchangeExecutionCode(codePath: String): String = {
    val path = codePath.substring(0, codePath.lastIndexOf(CODE_SPLIT))
    val codeInfo = codePath.substring(codePath.lastIndexOf(CODE_SPLIT) + 1)
    val infos: Array[String] = codeInfo.split(LENGTH_SPLIT)
    val position = infos(0).toInt
    var lengthLeft = infos(1).toInt
    val tub = new Array[Byte](1024)
    val executionCode: StringBuilder = new StringBuilder
    val fsPath: FsPath = new FsPath(path)
    val fileSystem =
      FSFactory.getFsByProxyUser(fsPath, System.getProperty("user.name")).asInstanceOf[FileSystem]
    fileSystem.init(null)
    var is: InputStream = null
    if (!fileSystem.exists(fsPath)) return null
    val loop = new Breaks
    Utils.tryFinally {
      is = fileSystem.read(fsPath)
      if (position > 0) is.skip(position)
      loop.breakable {
        while (lengthLeft > 0) {
          val readed = is.read(tub)
          val useful = Math.min(readed, lengthLeft)
          if (useful < 0) loop.break()
          lengthLeft -= useful
          executionCode.append(new String(tub, 0, useful, CHARSET))
        }
      }
    } {
      if (fileSystem != null) fileSystem.close()
      IOUtils.closeQuietly(is)
    }
    executionCode.toString()
  }

  private def getCodeStorePath(user: String): String = {
    val date: String = DateFormatUtils.format(new Date, "yyyyMMdd")
    s"${CODE_STORE_PREFIX.getValue}${user}${CODE_STORE_SUFFIX.getValue}/executionCode/${date}/_bgservice"
  }

}
