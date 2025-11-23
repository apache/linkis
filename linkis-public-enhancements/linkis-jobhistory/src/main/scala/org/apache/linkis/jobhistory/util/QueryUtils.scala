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

package org.apache.linkis.jobhistory.util

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.conf.Configuration.IS_VIEW_FS_ENV
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.job.{JobRequest, SubJobDetail}
import org.apache.linkis.jobhistory.entity.JobHistory
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.{FileSystemUtils, StorageUtils}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.time.DateFormatUtils

import java.io.{BufferedReader, InputStream, InputStreamReader, OutputStream}
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

object QueryUtils extends Logging {

  private val CODE_STORE_PREFIX =
    CommonVars("wds.linkis.query.store.prefix", "hdfs:///apps-data/bdp-ide/")

  private val CODE_STORE_PREFIX_VIEW_FS =
    CommonVars("wds.linkis.query.store.prefix.viewfs", "hdfs:///apps-data/")

  private val CODE_STORE_SUFFIX = CommonVars("wds.linkis.query.store.suffix", "")
  private val CODE_STORE_LENGTH = CommonVars("wds.linkis.query.code.store.length", 50000)
  private val CHARSET = "utf-8"
  private val CODE_SPLIT = ";"
  private val LENGTH_SPLIT = "#"
  private val NAME_REGEX = "^[a-zA-Z\\-\\d_\\.]+$"
  private val INSTANCE_NAME_REGEX = "^[a-zA-Z\\-\\d_\\.:]+$"
  private val NUMBER_REGEX = "^[0-9]+$"
  private val nameRegexPattern = Pattern.compile(NAME_REGEX)
  private val instanceNameRegexPattern = Pattern.compile(INSTANCE_NAME_REGEX)
  private val numberRegexPattern = Pattern.compile(NUMBER_REGEX)

  private val dateFormatLocal = new ThreadLocal[SimpleDateFormat]() {
    override protected def initialValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
  }

  def storeExecutionCode(jobRequest: JobRequest): Unit = {
    storeExecutionCode(
      jobRequest.getExecuteUser,
      jobRequest.getExecutionCode,
      path => jobRequest.setExecutionCode(path)
    )
  }

  def storeExecutionCode(subJobDetail: SubJobDetail, user: String): Unit = {
    storeExecutionCode(
      user,
      subJobDetail.getExecutionContent,
      path => subJobDetail.setExecutionContent(path)
    )
  }

  def storeExecutionCode(user: String, code: String, pathCallback: String => Unit): Unit = {
    if (null == code || code.getBytes().length < CODE_STORE_LENGTH.getValue) return
    val path: String = getCodeStorePath(user)
    val fsPath: FsPath = new FsPath(path)
    val fileSystem = FSFactory.getFsByProxyUser(fsPath, user).asInstanceOf[FileSystem]
    fileSystem.init(null)
    var os: OutputStream = null
    var position = 0L
    val codeBytes = code.getBytes(CHARSET)
    path.intern() synchronized {
      Utils.tryFinally {
        if (!fileSystem.exists(fsPath)) FileSystemUtils.createNewFile(fsPath, user, true)
        os = fileSystem.write(fsPath, false)
        position = fileSystem.get(path).getLength
        IOUtils.write(codeBytes, os)
      } {
        IOUtils.closeQuietly(os)
        if (fileSystem != null) fileSystem.close()
      }
    }
    val length = codeBytes.length
    pathCallback(path + CODE_SPLIT + position + LENGTH_SPLIT + length)
  }

  // todo exchangeExecutionCode for subJobDetail
  def exchangeExecutionCode(queryTask: JobHistory): Unit = {
    import scala.util.control.Breaks._
    if (
        queryTask.getExecutionCode == null || !queryTask.getExecutionCode.startsWith(
          StorageUtils.HDFS_SCHEMA
        )
    ) {
      return
    }
    val codePath = queryTask.getExecutionCode
    val executionCode: StringBuilder = new StringBuilder
    if (codePath.split(CODE_SPLIT).length >= 2) {
      val path = codePath.substring(0, codePath.lastIndexOf(CODE_SPLIT))
      val codeInfo = codePath.substring(codePath.lastIndexOf(CODE_SPLIT) + 1)
      val infos: Array[String] = codeInfo.split(LENGTH_SPLIT)
      val position = infos(0).toInt
      var lengthLeft = infos(1).toInt
      val tub = new Array[Char](1024)
      val fsPath: FsPath = new FsPath(path)
      val fileSystem =
        FSFactory.getFsByProxyUser(fsPath, queryTask.getExecuteUser).asInstanceOf[FileSystem]
      fileSystem.init(null)
      var is: InputStream = null
      var bufferedReader: BufferedReader = null
      if (!fileSystem.exists(fsPath)) return
      Utils.tryFinally {
        is = fileSystem.read(fsPath)
        bufferedReader = new BufferedReader(new InputStreamReader(is, CHARSET))
        if (position > 0) bufferedReader.skip(position)
        breakable {
          while (lengthLeft > 0) {
            val readed = bufferedReader.read(tub)
            val useful = Math.min(readed, lengthLeft)
            if (useful < 0) break()
            lengthLeft -= useful
            val usefulChars = new Array[Char](useful)
            System.arraycopy(tub, 0, usefulChars, 0, useful)
            executionCode.append(new String(usefulChars))
          }
        }
      } {
        IOUtils.closeQuietly(bufferedReader)
        if (fileSystem != null) Utils.tryAndWarn(fileSystem.close())
      }
    } else {
      logger.error(
        s"Can't read executionCode from HDFS, jobId:${queryTask.getId},error codePath:$codePath "
      )
    }
    queryTask.setExecutionCode(executionCode.toString())
  }

  private def getCodeStorePath(user: String): String = {
    val date: String = DateFormatUtils.format(new Date, "yyyyMMdd")
    val suffix: String =
      DateFormatUtils.format(System.currentTimeMillis, "HH_mm_ss_SSS") + "_scripts"
    if (IS_VIEW_FS_ENV.getValue) {
      s"${CODE_STORE_PREFIX_VIEW_FS.getValue}${user}${CODE_STORE_SUFFIX.getValue}/executionCode/${date}/$suffix"
    } else {
      s"${CODE_STORE_PREFIX.getValue}${user}${CODE_STORE_SUFFIX.getValue}/executionCode/${date}/$suffix"
    }
  }

  def dateToString(date: Date): String = {
    dateFormatLocal.get().format(date)
  }

  def checkNameValid(param: String): Boolean = {
    nameRegexPattern.matcher(param).find()
  }

  def checkInstanceNameValid(param: String): Boolean = {
    instanceNameRegexPattern.matcher(param).find()
  }

  def checkNumberValid(param: String): Boolean = {
    numberRegexPattern.matcher(param).find()
  }

}
