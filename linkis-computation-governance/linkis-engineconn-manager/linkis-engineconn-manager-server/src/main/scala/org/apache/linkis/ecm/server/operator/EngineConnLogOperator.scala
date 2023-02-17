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

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary._
import org.apache.linkis.ecm.server.conf.ECMConfiguration
import org.apache.linkis.ecm.server.exception.ECMErrorException
import org.apache.linkis.ecm.server.service.{EngineConnListService, LocalDirsHandleService}
import org.apache.linkis.manager.common.operator.Operator
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest

import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.ReversedLinesFileReader
import org.apache.commons.lang3.StringUtils

import java.io.{File, RandomAccessFile}
import java.nio.charset.{Charset, StandardCharsets}
import java.text.MessageFormat
import java.util
import java.util.Collections

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.matching.Regex

class EngineConnLogOperator extends Operator with Logging {

  private var engineConnListService: EngineConnListService = _
  private var localDirsHandleService: LocalDirsHandleService = _

  override def getNames: Array[String] = Array(EngineConnLogOperator.OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    val logPath = getLogPath
    val lastRows = getAs("lastRows", 0)
    val pageSize = getAs("pageSize", 100)
    val fromLine = getAs("fromLine", 1)
    val enableTail = getAs("enableTail", false)
    if (lastRows > EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue) {
      throw new ECMErrorException(
        CANNOT_FETCH_MORE_THAN.getErrorCode,
        MessageFormat.format(
          CANNOT_FETCH_MORE_THAN.getErrorDesc,
          EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue.toString
        )
      )
    } else if (lastRows > 0) {
      val logs = Utils.exec(Array("tail", "-n", lastRows + "", logPath.getPath), 5000).split("\n")
      return Map("logs" -> logs, "rows" -> logs.length)
    }

    val ignoreKeywords = getAs("ignoreKeywords", "")
    val ignoreKeywordList =
      if (StringUtils.isNotEmpty(ignoreKeywords)) ignoreKeywords.split(",")
      else Array.empty[String]
    val onlyKeywords = getAs("onlyKeywords", "")
    val onlyKeywordList =
      if (StringUtils.isNotEmpty(onlyKeywords)) onlyKeywords.split(",") else Array.empty[String]
    var randomReader: RandomAccessFile = null
    var reversedReader: ReversedLinesFileReader = null
    if (enableTail) {
      logger.info("enable log operator from tail to read")
      reversedReader = new ReversedLinesFileReader(logPath, Charset.defaultCharset())
    } else {
      randomReader = new RandomAccessFile(logPath, "r")
    }
    def randomAndReversedReadLine(): String = {
      if (null != randomReader) {
        val line = randomReader.readLine()
        if (line != null) {
          new String(line.getBytes(StandardCharsets.ISO_8859_1), Charset.defaultCharset())
        } else null
      } else {
        reversedReader.readLine()
      }
    }
    val logs = new util.ArrayList[String](pageSize)
    var readLine, skippedLine, lineNum = 0
    var rowIgnore = false
    var ignoreLine = 0
    val linePattern = Option(EngineConnLogOperator.MULTILINE_PATTERN.getValue) match {
      case Some(pattern) => pattern.r
      case _ => null
    }
    val maxMultiline = EngineConnLogOperator.MULTILINE_MAX.getValue
    Utils.tryFinally {
      var line = randomAndReversedReadLine()
      while (readLine < pageSize && line != null) {
        lineNum += 1
        if (skippedLine < fromLine - 1) {
          skippedLine += 1
        } else {
          if (rowIgnore) {
            linePattern match {
              case reg: Regex =>
                if (reg.findFirstIn(line).isDefined) {
                  ignoreLine = 0
                  rowIgnore = !includeLine(line, onlyKeywordList, ignoreKeywordList)
                } else {
                  ignoreLine += 1
                  if (ignoreLine >= maxMultiline) {
                    rowIgnore = false
                  }
                }
              case _ => rowIgnore = !includeLine(line, onlyKeywordList, ignoreKeywordList)
            }
          } else {
            rowIgnore = !includeLine(line, onlyKeywordList, ignoreKeywordList)
          }
          if (!rowIgnore) {
            logs.add(line)
            readLine += 1
          }
        }
        line = randomAndReversedReadLine()
      }
    } {
      IOUtils.closeQuietly(randomReader)
      IOUtils.closeQuietly(reversedReader)
    }
    if (enableTail) Collections.reverse(logs)
    Map("logPath" -> logPath.getPath, "logs" -> logs, "endLine" -> lineNum, "rows" -> readLine)
  }

  protected def getLogPath(implicit parameters: Map[String, Any]): File = {
    val (ticketId, engineConnInstance, engineConnLogDir) = getEngineConnInfo(parameters)
    val logPath =
      new File(engineConnLogDir, getAs("logType", EngineConnLogOperator.LOG_FILE_NAME.getValue));
    if (!logPath.exists() || !logPath.isFile) {
      throw new ECMErrorException(
        LOGFILE_IS_NOT_EXISTS.getErrorCode,
        MessageFormat.format(LOGFILE_IS_NOT_EXISTS.getErrorDesc, logPath)
      )
    }
    logger.info(
      s"Try to fetch EngineConn(id: $ticketId, instance: $engineConnInstance) logs from ${logPath.getPath}."
    )
    logPath
  }

  protected def getEngineConnInfo(implicit
      parameters: Map[String, Any]
  ): (String, String, String) = {
    if (engineConnListService == null) {
      engineConnListService =
        DataWorkCloudApplication.getApplicationContext.getBean(classOf[EngineConnListService])
      localDirsHandleService =
        DataWorkCloudApplication.getApplicationContext.getBean(classOf[LocalDirsHandleService])
    }
    val logDIrSuffix = getAs("logDirSuffix", "")
    val (engineConnLogDir, engineConnInstance, ticketId) =
      if (StringUtils.isNotBlank(logDIrSuffix)) {
        val ecLogPath = ECMConfiguration.ENGINECONN_ROOT_DIR + File.separator + logDIrSuffix
        val ticketId = getAs("ticketId", "")
        (ecLogPath, "", ticketId)
      } else {
        val engineConnInstance = getAs(
          ECMOperateRequest.ENGINE_CONN_INSTANCE_KEY,
          getAs[String]("engineConnInstance", null)
        )
        Option(engineConnInstance)
          .flatMap { instance =>
            engineConnListService.getEngineConns.asScala.find(
              _.getServiceInstance.getInstance == instance
            )
          }
          .map(engineConn =>
            (
              engineConn.getEngineConnManagerEnv.engineConnLogDirs,
              engineConnInstance,
              engineConn.getTickedId
            )
          )
          .getOrElse {
            val ticketId = getAs("ticketId", "")
            if (StringUtils.isBlank(ticketId)) {
              throw new ECMErrorException(
                BOTH_NOT_EXISTS.getErrorCode,
                s"the parameters of ${ECMOperateRequest.ENGINE_CONN_INSTANCE_KEY}, engineConnInstance and ticketId are both not exists."
              )
            }
            val logDir = engineConnListService
              .getEngineConn(ticketId)
              .map(_.getEngineConnManagerEnv.engineConnLogDirs)
              .getOrElse {
                val creator = getAsThrow[String]("creator")
                val engineConnType = getAsThrow[String]("engineConnType")
                localDirsHandleService.getEngineConnLogDir(creator, ticketId, engineConnType)
              }
            (logDir, engineConnInstance, ticketId)
          }
      }
    (ticketId, engineConnInstance, engineConnLogDir)
  }

  private def includeLine(
      line: String,
      onlyKeywordList: Array[String],
      ignoreKeywordList: Array[String]
  ): Boolean = {
    var accept: Boolean = ignoreKeywordList.isEmpty || !ignoreKeywordList.exists(line.contains)
    if (accept) {
      accept = onlyKeywordList.isEmpty || onlyKeywordList.exists(line.contains)
    }
    accept
  }

}

object EngineConnLogOperator {
  val OPERATOR_NAME = "engineConnLog"
  val LOG_FILE_NAME = CommonVars("linkis.engineconn.log.filename", "stdout")
  val MAX_LOG_FETCH_SIZE = CommonVars("linkis.engineconn.log.fetch.lines.max", 5000)

  val MAX_LOG_TAIL_START_SIZE = CommonVars("linkis.engineconn.log.tail.start.size", 20000)

  // yyyy-MM-dd HH:mm:ss.SSS
  val MULTILINE_PATTERN = CommonVars(
    "linkis.engineconn.log.multiline.pattern",
    "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"
  )

  val MULTILINE_MAX = CommonVars("linkis.engineconn.log.multiline.max", 500)
}
