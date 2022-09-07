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

package org.apache.linkis.engineconnplugin.flink.ql.impl

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.OperationUtil
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.engineconnplugin.flink.ql.Grammar

import org.apache.flink.table.api.internal.TableEnvironmentInternal

import scala.util.matching.Regex

class CreateTableAsSelectGrammar(context: FlinkEngineConnContext, sql: String)
    extends Grammar
    with Logging {

  def this() = this(null, null)

  override def canParse(sql: String): Boolean =
    CreateTableAsSelectGrammar.CREATE_TABLE_AS_SELECT_GRAMMAR.unapplySeq(sql).isDefined

  /**
   * Execute the command and return the result.
   */
  override def execute(): ResultSet = sql match {
    case CreateTableAsSelectGrammar.CREATE_TABLE_AS_SELECT_GRAMMAR(_, _, tableName, _, _, select) =>
      logger.info(s"Ready to create a table $tableName, the sql is: $select.")
      val function = new java.util.function.Function[TableEnvironmentInternal, Unit] {
        override def apply(t: TableEnvironmentInternal): Unit = {
          val table = t.sqlQuery(select)
          t.createTemporaryView(tableName, table)
        }
      }
      context.getExecutionContext.wrapClassLoader[Unit](function)
      OperationUtil.OK
  }

  override def copy(context: FlinkEngineConnContext, sql: String): CreateTableAsSelectGrammar =
    new CreateTableAsSelectGrammar(context, sql)

}

object CreateTableAsSelectGrammar {

  val CREATE_TABLE_AS_SELECT_GRAMMAR: Regex =
    "(create|CREATE)\\s+(table|TABLE)\\s+([a-zA-Z_][a-zA-Z_0-9]*)\\s+(as|AS)?\\s*(select|SELECT)(.+)".r

}
