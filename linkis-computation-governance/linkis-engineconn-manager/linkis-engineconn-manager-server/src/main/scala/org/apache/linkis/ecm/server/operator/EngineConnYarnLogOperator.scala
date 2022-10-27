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
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.ecm.core.conf.ECMErrorCode
import org.apache.linkis.ecm.server.exception.ECMErrorException

import java.io.File
import java.util
import java.util.concurrent.{Callable, ConcurrentHashMap, ExecutorService, Future, TimeUnit}

import scala.collection.JavaConverters._

class EngineConnYarnLogOperator extends EngineConnLogOperator {

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
    val (ticketId, engineConnInstance, engineConnLogDir) = getEngineConnInfo(parameters)
    val rootLogDir = new File(engineConnLogDir)
    if (!rootLogDir.exists() || !rootLogDir.isDirectory) {
      throw new ECMErrorException(
        ECMErrorCode.EC_FETCH_LOG_FAILED,
        s"Log directory $rootLogDir is not exists."
      )
    }
    val creator = getAsThrow[String]("creator")
    val applicationId = getAsThrow[String]("yarnApplicationId")
    var logPath = new File(engineConnLogDir, "yarn_" + applicationId)
    if (!logPath.exists()) {
      val fetcher = yarnLogFetchers.computeIfAbsent(
        applicationId,
        new util.function.Function[String, Future[String]] {
          override def apply(v1: String): Future[String] =
            requestToFetchYarnLogs(creator, applicationId, engineConnLogDir)
        }
      )
      // Just wait 5 seconds
      Option(fetcher.get(5, TimeUnit.SECONDS)) match {
        case Some(path) => logPath = new File(path)
        case _ =>
      }

    }
    if (!logPath.exists() || !logPath.isFile) {
      throw new ECMErrorException(
        ECMErrorCode.EC_FETCH_LOG_FAILED,
        s"LogFile $logPath is not exists or is not a file."
      )
    }
    logger.info(
      s"Try to fetch EngineConn(id: $ticketId, instance: $engineConnInstance) yarn logs from ${logPath.getPath} in application id: $applicationId"
    )
    logPath
  }

  /**
   * Request the log fetcher
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
      engineConnLogDir: String
  ): Future[String] = {
    EngineConnYarnLogOperator.YARN_LOG_FETCH_SCHEDULER.submit(new Callable[String] {
      override def call(): String = {
        val logFile = new File(engineConnLogDir, "yarn_" + applicationId)
        if (!logFile.exists()) {
          val tempLogFile =
            s".yarn_${applicationId}_${System.currentTimeMillis()}_${Thread.currentThread().getId}"
          Utils.tryCatch {
            var command =
              s"yarn logs -applicationId $applicationId >> $engineConnLogDir/$tempLogFile"
            logger.info(s"Fetch yarn logs to temporary file: [$command]")
            val processBuilder = new ProcessBuilder(sudoCommands(creator, command): _*)
            processBuilder.environment.putAll(sys.env.asJava)
            processBuilder.redirectErrorStream(false)
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            logger.trace(s"Finish to fetch yan logs to temporary file, result: ${exitCode}")
            if (exitCode == 0) {
              command = s"mv $engineConnLogDir/$tempLogFile $engineConnLogDir/yarn_$applicationId"
              logger.info(s"Move and save yarn logs(${applicationId}): [$command]")
              Utils.exec(sudoCommands(creator, command))
            }
          } { e: Throwable =>
            logger.error(
              s"Fail to fetch yarn logs application: $applicationId, message: ${e.getMessage}"
            )
          }
          val tmpFile = new File(engineConnLogDir, tempLogFile)
          if (tmpFile.exists()) {
            logger.info(s"Delete temporary file: [${tempLogFile}] in yarn logs fetcher")
            tmpFile.delete()
          }
        }
        // Remove future
        yarnLogFetchers.remove(applicationId)
        if (logFile.exists()) logFile.getPath else null
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

  val YARN_LOG_FETCH_THREAD: CommonVars[Int] =
    CommonVars("linkis.engineconn.log.yarn.fetch.thread-num", 5)

  val YARN_LOG_FETCH_SCHEDULER: ExecutorService =
    Utils.newFixedThreadPool(YARN_LOG_FETCH_THREAD.getValue + 1, "yarn_logs_fetch", false)

}
