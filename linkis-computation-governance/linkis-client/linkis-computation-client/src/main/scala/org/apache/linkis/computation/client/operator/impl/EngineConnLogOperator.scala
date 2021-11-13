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

package org.apache.linkis.computation.client.operator.impl

import java.util

import org.apache.commons.lang.StringUtils
import org.apache.linkis.computation.client.once.action.EngineConnOperateAction
import org.apache.linkis.computation.client.once.result.EngineConnOperateResult
import org.apache.linkis.computation.client.operator.OnceJobOperator


class EngineConnLogOperator extends OnceJobOperator[EngineConnLogs]  {

  private var pageSize = 100
  private var fromLine = 1
  private var ignoreKeywords: String = _
  private var onlyKeywords: String = _
  private var lastRows = 0

  def setPageSize(pageSize: Int): Unit = this.pageSize = pageSize

  def setFromLine(fromLine: Int): Unit = this.fromLine = fromLine

  def setIgnoreKeywords(ignoreKeywords: String): Unit = this.ignoreKeywords = ignoreKeywords

  def setOnlyKeywords(onlyKeywords: String): Unit = this.onlyKeywords = onlyKeywords

  def setLastRows(lastRows: Int): Unit = this.lastRows = lastRows

  override protected def addParameters(builder: EngineConnOperateAction.Builder): Unit = {
    builder.addParameter("pageSize", pageSize)
    builder.addParameter("fromLine", fromLine)
    if(StringUtils.isNotEmpty(ignoreKeywords))
      builder.addParameter("ignoreKeywords", ignoreKeywords)
    if(StringUtils.isNotEmpty(onlyKeywords))
      builder.addParameter("onlyKeywords", onlyKeywords)
    if(lastRows > 0)
      builder.addParameter("lastRows", lastRows)
  }

  override protected def resultToObject(result: EngineConnOperateResult): EngineConnLogs = {
    val rows: Int = result.getAs("rows")
    fromLine += rows
    EngineConnLogs(result.getAs("logPath"), result.getAs("logs"),  rows)
  }

  override def getName: String = EngineConnLogOperator.OPERATOR_NAME

}

object EngineConnLogOperator {
  val OPERATOR_NAME = "engineConnLog"
}

case class EngineConnLogs(logPath: String, logs: util.ArrayList[String], rows: Int)