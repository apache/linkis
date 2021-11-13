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

package org.apache.linkis.engineconn.acessible.executor.operator.impl

import java.io.{File, RandomAccessFile}
import java.util

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.acessible.executor.operator.Operator
import org.apache.linkis.engineconn.common.conf.EngineConnConf
import org.apache.linkis.engineconn.common.exception.EngineConnException

class EngineConnLogOperator extends Operator with Logging {

  private val logPath = new File(EngineConnConf.getLogDir, EngineConnLogOperator.LOG_FILE_NAME.getValue)
  info(s"Try to fetch EngineConn logs from ${logPath.getPath}.")

  override def getNames: Array[String] = Array(EngineConnLogOperator.OPERATOR_NAME)

  override def apply(implicit parameters: Map[String, Any]): Map[String, Any] = {
    val lastRows = getAs("lastRows", 0)
    if(lastRows > EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue)
      throw EngineConnException(20305, s"Cannot fetch more than ${EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue} lines of logs.")
    else if(lastRows > 0) {
      val logs = Utils.exec(Array("tail", "-f", logPath.getPath), 5000).split("\n")
      return Map("logs" -> logs, "rows" -> logs.length)
    }
    val pageSize = getAs("pageSize", 100)
    val fromLine = getAs("fromLine", 1)
    val ignoreKeywords = getAs("ignoreKeywords", "")
    val ignoreKeywordList = if(StringUtils.isNotEmpty(ignoreKeywords)) ignoreKeywords.split(",") else Array.empty
    val onlyKeywords = getAs("onlyKeywords", "")
    val onlyKeywordList = if(StringUtils.isNotEmpty(onlyKeywords)) onlyKeywords.split(",") else Array.empty
    val reader = new RandomAccessFile(logPath, "r")
    val logs = new util.ArrayList[String](pageSize)
    var readLine, skippedLine = 0
    Utils.tryFinally {
      var line = reader.readLine()
      while (readLine < pageSize && line != null) {
        if(skippedLine < fromLine) {
          skippedLine += 1
        } else if(onlyKeywordList.nonEmpty && onlyKeywordList.exists(line.contains)) {
          logs.add(line)
          readLine += 1
        } else if(ignoreKeywordList.nonEmpty && !ignoreKeywordList.exists(line.contains)) {
          logs.add(line)
          readLine += 1
        } else if(onlyKeywordList.isEmpty && ignoreKeywordList.isEmpty) {
          logs.add(line)
          readLine += 1
        }
        line = reader.readLine
      }
    }(IOUtils.closeQuietly(reader))
    Map("logPath" -> logPath.getPath, "logs" -> logs, "rows" -> readLine)
  }
}
object EngineConnLogOperator {
  val OPERATOR_NAME = "engineConnLog"
  val LOG_FILE_NAME = CommonVars("linkis.engineconn.log.filename", "stdout")
  val MAX_LOG_FETCH_SIZE = CommonVars("linkis.engineconn.log.fetch.lines.max", 5000)
}