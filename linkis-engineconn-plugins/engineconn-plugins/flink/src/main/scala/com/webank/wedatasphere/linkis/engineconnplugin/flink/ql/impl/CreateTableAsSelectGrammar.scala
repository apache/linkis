package com.webank.wedatasphere.linkis.engineconnplugin.flink.ql.impl

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.OperationUtil
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultSet
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.ql.Grammar
import org.apache.flink.table.api.internal.TableEnvironmentInternal

import scala.util.matching.Regex

/**
  * Created by enjoyyin on 2021/6/7.
  */
class CreateTableAsSelectGrammar(context: FlinkEngineConnContext, sql: String) extends Grammar with Logging {

  def this() = this(null, null)

  override def canParse(sql: String): Boolean =
    CreateTableAsSelectGrammar.CREATE_TABLE_AS_SELECT_GRAMMAR.unapplySeq(sql).isDefined

  /**
    * Execute the command and return the result.
    */
  override def execute(): ResultSet = sql match {
    case CreateTableAsSelectGrammar.CREATE_TABLE_AS_SELECT_GRAMMAR(_, _, tableName, _, select, sql) =>
      val realSql = select + " " + sql
      info(s"Ready to create a table $tableName, the sql is: $realSql.")
      val function = new java.util.function.Function[TableEnvironmentInternal, Unit] {
        override def apply(t: TableEnvironmentInternal): Unit = {
          val table = t.sqlQuery(realSql)
          t.createTemporaryView(tableName, table)
        }
      }
      context.getExecutionContext.wrapClassLoader[Unit](function)
      OperationUtil.OK
  }

  override def copy(context: FlinkEngineConnContext, sql: String): CreateTableAsSelectGrammar = new CreateTableAsSelectGrammar(context, sql)
}

object CreateTableAsSelectGrammar {

  val CREATE_TABLE_AS_SELECT_GRAMMAR: Regex = "(create|CREATE)\\s+(table|TABLE)\\s+([a-zA-Z_][a-zA-Z_0-9]*)\\s+(as|AS)?\\s*(select|SELECT)(.+)".r

}