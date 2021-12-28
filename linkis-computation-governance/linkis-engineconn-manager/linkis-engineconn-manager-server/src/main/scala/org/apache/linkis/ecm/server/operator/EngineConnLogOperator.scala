/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.ecm.server.operator

import java.io.{File, RandomAccessFile}
import java.util

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.server.exception.{ECMErrorCode, ECMErrorException}
import org.apache.linkis.ecm.server.service.{EngineConnListService, LocalDirsHandleService}
import org.apache.linkis.manager.common.operator.Operator
import org.apache.linkis.manager.common.protocol.em.ECMOperateRequest
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.matching.Regex

class EngineConnLogOperator extends Operator with Logging {

  private var engineConnListService: EngineConnListService = _
  private var localDirsHandleService: LocalDirsHandleService = _

  override def getNames: Array[String] = Array(EngineConnLogOperator.OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    val logPath = getLogPath
    val lastRows = getAs("lastRows", 0)
    if (lastRows > EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue) {
      throw new ECMErrorException(ECMErrorCode.EC_FETCH_LOG_FAILED, s"Cannot fetch more than ${EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue} lines of logs.")
    } else if (lastRows > 0) {
      val logs = Utils.exec(Array("tail", "-f", logPath.getPath), 5000).split("\n")
      return Map("logs" -> logs, "rows" -> logs.length)
    }
    val pageSize = getAs("pageSize", 100)
    val fromLine = getAs("fromLine", 1)
    val ignoreKeywords = getAs("ignoreKeywords", "")
    val ignoreKeywordList = if (StringUtils.isNotEmpty(ignoreKeywords)) ignoreKeywords.split(",") else Array.empty[String]
    val onlyKeywords = getAs("onlyKeywords", "")
    val onlyKeywordList = if (StringUtils.isNotEmpty(onlyKeywords)) onlyKeywords.split(",") else Array.empty[String]
    val reader = new RandomAccessFile(logPath, "r")
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
      var line = reader.readLine()
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
        line = reader.readLine
      }
    }(IOUtils.closeQuietly(reader))
    Map("logPath" -> logPath.getPath, "logs" -> logs, "endLine" -> lineNum, "rows" -> readLine)
  }

  private def includeLine(line: String,
                          onlyKeywordList: Array[String], ignoreKeywordList: Array[String]): Boolean = {
    if (onlyKeywordList.nonEmpty && onlyKeywordList.exists(line.contains)) {
      true
    } else if (ignoreKeywordList.nonEmpty && !ignoreKeywordList.exists(line.contains)) {
      true
    } else if (onlyKeywordList.isEmpty && ignoreKeywordList.isEmpty) {
      true
    } else {
      false
    }
  }
  private def getLogPath(implicit parameters: Map[String, Any]): File = {
    if (engineConnListService == null) {
      engineConnListService = DataWorkCloudApplication.getApplicationContext.getBean(classOf[EngineConnListService])
      localDirsHandleService = DataWorkCloudApplication.getApplicationContext.getBean(classOf[LocalDirsHandleService])
    }
    val engineConnInstance = getAs(ECMOperateRequest.ENGINE_CONN_INSTANCE_KEY, getAs[String]("engineConnInstance", null))
    val (engineConnLogDir, ticketId) = Option(engineConnInstance).flatMap { instance =>
      engineConnListService.getEngineConns.asScala.find(_.getServiceInstance.getInstance == instance)
    }.map(engineConn => (engineConn.getEngineConnManagerEnv.engineConnLogDirs, engineConn.getTickedId)).getOrElse {
      val ticketId = getAs("ticketId", "")
      if (StringUtils.isBlank(ticketId)) {
        throw new ECMErrorException(ECMErrorCode.EC_FETCH_LOG_FAILED, s"the parameters of ${ECMOperateRequest.ENGINE_CONN_INSTANCE_KEY}, engineConnInstance and ticketId are both not exists.")
      }
      val logDir = engineConnListService.getEngineConn(ticketId).map(_.getEngineConnManagerEnv.engineConnLogDirs)
        .getOrElse {
          val creator = getAsThrow[String]("creator")
          val engineConnType = getAsThrow[String]("engineConnType")
          localDirsHandleService.getEngineConnLogDir(creator, ticketId)
        }
      (logDir, ticketId)
    }
    val logPath = new File(engineConnLogDir, EngineConnLogOperator.LOG_FILE_NAME.getValue)
    if(!logPath.exists() || !logPath.isFile) {
      throw new ECMErrorException(ECMErrorCode.EC_FETCH_LOG_FAILED, s"LogFile $logPath is not exists or is not a file.")
    }
    info(s"Try to fetch EngineConn(id: $ticketId, instance: $engineConnInstance) logs from ${logPath.getPath}.")
    logPath
  }
}

object EngineConnLogOperator {
  val OPERATOR_NAME = "engineConnLog"
  val LOG_FILE_NAME = CommonVars("linkis.engineconn.log.filename", "stdout")
  val MAX_LOG_FETCH_SIZE = CommonVars("linkis.engineconn.log.fetch.lines.max", 5000)
  // yyyy-MM-dd HH:mm:ss.SSS
  val MULTILINE_PATTERN = CommonVars("linkis.engineconn.log.multiline.pattern", "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")
  val MULTILINE_MAX = CommonVars("linkis.engineconn.log.multiline.max", 500)
}