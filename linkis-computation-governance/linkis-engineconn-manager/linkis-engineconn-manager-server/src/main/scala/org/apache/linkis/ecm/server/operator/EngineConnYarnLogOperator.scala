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

import org.apache.linkis.common.exception.LinkisCommonErrorException
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.ecm.core.conf.ECMErrorCode
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary._
import org.apache.linkis.ecm.server.exception.ECMErrorException

import java.io.File
import java.text.MessageFormat
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

class EngineConnYarnLogOperator extends EngineConnLogOperator {

  override def getNames: Array[String] = Array(EngineConnYarnLogOperator.OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    var result: Map[String, Any] = Map()
    Utils.tryFinally {
      result = super.apply(parameters)
      result
    } {
      result.get("logPath") match {
        case Some(path: String) =>
          val logFile = new File(path)
          if (logFile.exists() && logFile.getName.startsWith(".")) {
            // If is a temporary file, drop it
            logger.info(s"Delete the temporary yarn log file: [$path]")
            if (!logFile.delete()) {
              logger.warn(s"Fail to delete the temporary yarn log file: [$path]")
            }
          }
      }
    }
  }

  override def getLogPath(implicit parameters: Map[String, Any]): File = {
    val (ticketId, engineConnInstance, engineConnLogDir) = getEngineConnInfo(parameters)
    val rootLogDir = new File(engineConnLogDir)
    if (!rootLogDir.exists() || !rootLogDir.isDirectory) {
      throw new ECMErrorException(
        LOG_IS_NOT_EXISTS.getErrorCode,
        MessageFormat.format(LOG_IS_NOT_EXISTS.getErrorDesc, rootLogDir)
      )
    }
    val creator = getAsThrow[String]("creator")
    val applicationId = getAsThrow[String]("yarnApplicationId")
    var logPath = new File(engineConnLogDir, "yarn_" + applicationId)
    if (!logPath.exists()) {
      val tempLogFile =
        s".yarn_${applicationId}_${System.currentTimeMillis()}_${Thread.currentThread().getId}"
      Utils.tryCatch {
        var command = s"yarn logs -applicationId $applicationId >> $rootLogDir/$tempLogFile"
        logger.info(s"Fetch yarn logs to temporary file: [$command]")
        val processBuilder = new ProcessBuilder(sudoCommands(creator, command): _*)
        processBuilder.environment.putAll(sys.env.asJava)
        processBuilder.redirectErrorStream(false)
        val process = processBuilder.start()
        val waitFor = process.waitFor(5, TimeUnit.SECONDS)
        logger.trace(s"waitFor: ${waitFor}, result: ${process.exitValue()}")
        if (waitFor && process.waitFor() == 0) {
          command = s"mv $rootLogDir/$tempLogFile $rootLogDir/yarn_$applicationId"
          logger.info(s"Move and save yarn logs: [$command]")
          Utils.exec(sudoCommands(creator, command))
        } else {
          logPath = new File(engineConnLogDir, tempLogFile)
          if (!logPath.exists()) {
            throw new LinkisCommonErrorException(
              -1,
              s"Fetch yarn logs timeout, log aggregation has not completed or is not enabled"
            )
          }
        }
      } { case e: Exception =>
        throw new LinkisCommonErrorException(
          -1,
          s"Fail to fetch yarn logs application: $applicationId, message: ${e.getMessage}"
        )
      }
    }
    if (!logPath.exists() || !logPath.isFile) {
      throw new ECMErrorException(
        LOGFILE_IS_NOT_EXISTS.getErrorCode,
        MessageFormat.format(LOGFILE_IS_NOT_EXISTS.getErrorDesc, logPath)
      )
    }
    logger.info(
      s"Try to fetch EngineConn(id: $ticketId, instance: $engineConnInstance) yarn logs from ${logPath.getPath} in application id: $applicationId"
    )
    logPath
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
}
