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

package org.apache.linkis.ecm.server.operator

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.io.FsPath
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary._
import org.apache.linkis.ecm.server.exception.ECMErrorException
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.StorageUtils

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.text.MessageFormat
import java.util
import java.util.concurrent.{Callable, ConcurrentHashMap, ExecutorService, Future, TimeUnit}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class EngineConnYarnLogOperator extends EngineConnLogOperator {

  private implicit val fs: FileSystem =
    FSFactory.getFs(StorageUtils.FILE).asInstanceOf[FileSystem]

  /**
   * Yarn log fetchers
   */
  private def yarnLogFetchers: ConcurrentHashMap[String, Future[String]] =
    new ConcurrentHashMap[String, Future[String]]()

  override def getNames: Array[String] = Array(EngineConnYarnLogOperator.OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    var result: Map[String, Any] = Map()
    Utils.tryFinally {
      result = super.apply(parameters)
      result
    } {}
  }

  override def getLogPath(implicit parameters: Map[String, Any]): File = {
    val applicationId = getAsThrow[String]("yarnApplicationId", parameters)
    val (ticketId, engineConnInstance, engineConnLogDir) = getEngineConnInfo(parameters)
    val rootLogPath = EngineConnYarnLogOperator.YARN_LOG_STORAGE_PATH.getValue match {
      case storePath if StringUtils.isNotBlank(storePath) =>
        val logPath = new FsPath(StorageUtils.FILE_SCHEMA + storePath + "/" + applicationId)
        // Invoke to create directory
        fs.mkdir(logPath)
        // 777 permission
        fs.setPermission(logPath, "rwxrwxrwx")
        logPath
      case _ => new FsPath(StorageUtils.FILE_SCHEMA + engineConnLogDir)
    }
    if (!fs.exists(rootLogPath) || !rootLogPath.toFile.isDirectory) {
      throw new ECMErrorException(
        LOG_IS_NOT_EXISTS.getErrorCode,
        MessageFormat.format(LOG_IS_NOT_EXISTS.getErrorDesc, rootLogPath.getPath)
      )
    }
    val creator = getAsThrow[String]("creator", parameters)
    var logPath = new FsPath(
      StorageUtils.FILE_SCHEMA + rootLogPath.getPath + "/yarn_" + applicationId
    )
    if (!fs.exists(logPath)) {
      val fetcher = yarnLogFetchers.computeIfAbsent(
        applicationId,
        new util.function.Function[String, Future[String]] {
          override def apply(v1: String): Future[String] =
            requestToFetchYarnLogs(creator, applicationId, rootLogPath.getPath)
        }
      )
      // Just wait 5 seconds
      Option(fetcher.get(5, TimeUnit.SECONDS)) match {
        case Some(path) => logPath = new FsPath(StorageUtils.FILE_SCHEMA + path)
        case _ =>
      }

    }
    if (!fs.exists(logPath) || logPath.toFile.isDirectory) {
      throw new ECMErrorException(
        LOGFILE_IS_NOT_EXISTS.getErrorCode,
        MessageFormat.format(LOGFILE_IS_NOT_EXISTS.getErrorDesc, logPath.getPath)
      )
    }
    logger.info(
      s"Try to fetch EngineConn(id: $ticketId, instance: $engineConnInstance) yarn logs from ${logPath.getPath} in application id: $applicationId"
    )
    logPath.toFile
  }

  /**
   * Not support line pattern in yarn log
   * @return
   */
  override protected def getLinePattern: Regex = null

  /**
   * Request the log fetcher
   *
   * @param creator
   *   creator
   * @param applicationId
   *   application id
   * @param logPath
   *   log path
   * @return
   */
  private def requestToFetchYarnLogs(
      creator: String,
      applicationId: String,
      yarnLogDir: String
  ): Future[String] = {
    EngineConnYarnLogOperator.YARN_LOG_FETCH_SCHEDULER.submit(new Callable[String] {
      override def call(): String = {
        val logPath = new FsPath(StorageUtils.FILE_SCHEMA + yarnLogDir + "/yarn_" + applicationId)
        if (!fs.exists(logPath)) {
          val tempLogFile =
            s".yarn_${applicationId}_${System.currentTimeMillis()}_${Thread.currentThread().getId}"
          Utils.tryCatch {
            var command =
              s"yarn logs -applicationId $applicationId >> $yarnLogDir/$tempLogFile"
            logger.info(s"Fetch yarn logs to temporary file: [$command]")
            val processBuilder = new ProcessBuilder(sudoCommands(creator, command): _*)
            processBuilder.environment.putAll(sys.env.asJava)
            processBuilder.redirectErrorStream(false)
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            logger.trace(s"Finish to fetch yan logs to temporary file, result: ${exitCode}")
            if (exitCode == 0) {
              command = s"mv $yarnLogDir/$tempLogFile $yarnLogDir/yarn_$applicationId"
              logger.info(s"Move and save yarn logs(${applicationId}): [$command]")
              Utils.exec(sudoCommands(creator, command))
            }
          } { e: Throwable =>
            logger.error(
              s"Fail to fetch yarn logs application: $applicationId, message: ${e.getMessage}"
            )
          }
          val tmpFile = new File(yarnLogDir, tempLogFile)
          if (tmpFile.exists()) {
            logger.info(s"Delete temporary file: [${tempLogFile}] in yarn logs fetcher")
            tmpFile.delete()
          }
        }
        // Remove future
        yarnLogFetchers.remove(applicationId)
        if (fs.exists(logPath)) logPath.getPath else null
      }
    })
  }

  private def sudoCommands(creator: String, command: String): Array[String] = {
    Array(
      "/bin/bash",
      "-c",
      "sudo su " + creator + " -c \"source ~/.bashrc 2>/dev/null; " + command + "\""
    )
  }

}

object EngineConnYarnLogOperator {
  val OPERATOR_NAME = "engineConnYarnLog"

  // Specific the path to store the yarn logs
  val YARN_LOG_STORAGE_PATH: CommonVars[String] =
    CommonVars("linkis.engineconn.log.yarn.storage-path", "")

  val YARN_LOG_FETCH_THREAD: CommonVars[Int] =
    CommonVars("linkis.engineconn.log.yarn.fetch.thread-num", 5)

  val YARN_LOG_FETCH_SCHEDULER: ExecutorService =
    Utils.newFixedThreadPool(YARN_LOG_FETCH_THREAD.getValue + 1, "yarn_logs_fetch", false)

}
