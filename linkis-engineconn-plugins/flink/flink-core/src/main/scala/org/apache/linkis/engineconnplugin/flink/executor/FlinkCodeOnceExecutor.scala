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

package org.apache.linkis.engineconnplugin.flink.executor

import org.apache.linkis.common.utils.{ByteTimeUtils, Utils, VariableUtils}
import org.apache.linkis.engineconn.once.executor.OnceExecutorExecutionContext
import org.apache.linkis.engineconnplugin.flink.client.deployment.YarnPerJobClusterDescriptorAdapter
import org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary._
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.{
  FlinkInitFailedException,
  SqlParseException
}
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.OperationFactory
import org.apache.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind.SUCCESS_WITH_CONTENT
import org.apache.linkis.engineconnplugin.flink.client.sql.parser.{SqlCommand, SqlCommandParser}
import org.apache.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import org.apache.linkis.governance.common.paser.{CodeParserFactory, CodeType}
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.scheduler.executer.ErrorExecuteResponse

import org.apache.calcite.rel.metadata.{JaninoRelMetadataProvider, RelMetadataQueryBase}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.client.deployment.ClusterClientJobClientAdapter
import org.apache.flink.client.program.{ClusterClient, ClusterClientProvider}
import org.apache.flink.table.api.{ResultKind, TableResult}
import org.apache.flink.table.planner.plan.metadata.FlinkDefaultRelMetadataProvider
import org.apache.hadoop.yarn.api.records.ApplicationId

import java.text.MessageFormat
import java.util
import java.util.concurrent.Future
import java.util.function.Supplier

class FlinkCodeOnceExecutor(
    override val id: Long,
    override protected val flinkEngineConnContext: FlinkEngineConnContext
) extends FlinkOnceExecutor[YarnPerJobClusterDescriptorAdapter] {

  private var codes: String = _
  private var future: Future[_] = _

  override def doSubmit(
      onceExecutorExecutionContext: OnceExecutorExecutionContext,
      options: Map[String, String]
  ): Unit = {
    codes = options(TaskConstant.CODE)
    options(TaskConstant.RUNTYPE) match {
      case "sql" =>
        if (StringUtils.isBlank(codes)) {
          throw new FlinkInitFailedException(SQL_CODE_EMPTY.getErrorDesc)
        }
        logger.info(s"Ready to submit flink application, sql is: $codes.")
        val variableMap =
          if (onceExecutorExecutionContext.getOnceExecutorContent.getVariableMap != null) {
            onceExecutorExecutionContext.getOnceExecutorContent.getVariableMap
              .asInstanceOf[util.Map[String, Any]]
          } else new util.HashMap[String, Any]
        codes = VariableUtils.replace(codes, variableMap)
        logger.info(s"After variable replace, sql is: $codes.")
      case runType =>
        // Now, only support sql code.
        throw new FlinkInitFailedException(
          MessageFormat.format(NOT_SUPPORT_RUNTYPE.getErrorDesc, runType)
        )
    }
    future = Utils.defaultScheduler.submit(new Runnable {
      override def run(): Unit = {
        logger.info("Try to execute codes.")
        RelMetadataQueryBase.THREAD_PROVIDERS.set(
          JaninoRelMetadataProvider.of(FlinkDefaultRelMetadataProvider.INSTANCE)
        )
        Utils.tryCatch(
          CodeParserFactory
            .getCodeParser(CodeType.SQL)
            .parse(codes)
            .filter(StringUtils.isNotBlank)
            .foreach(runCode)
        ) { t =>
          logger.error("Run code failed!", t)
          setResponse(ErrorExecuteResponse("Run code failed!", t))
          tryFailed()
          return
        }
        logger.info("All codes completed, now stop FlinkEngineConn.")
        trySucceed()
      }
    })
    this synchronized wait()
  }

  /**
   * Only support to execute sql in order, so it is problematic if more than one insert sql is
   * submitted.
   */
  protected def runCode(code: String): Unit = {
    if (isClosed) return
    val trimmedCode = StringUtils.trim(code)
    logger.info(s"$getId >> " + trimmedCode)
    val startTime = System.currentTimeMillis
    val callOpt = SqlCommandParser.getSqlCommandParser.parse(code.trim, true)
    if (!callOpt.isPresent) {
      throw new SqlParseException(MessageFormat.format(UNKNOWN_STATEMENT.getErrorDesc, code))
    }
    val resultSet = callOpt.get().command match {
      case SqlCommand.SET | SqlCommand.USE_CATALOG | SqlCommand.USE | SqlCommand.SHOW_MODULES |
          SqlCommand.DESCRIBE_TABLE | SqlCommand.EXPLAIN =>
        val operation = OperationFactory.getOperationFactory.createOperation(
          callOpt.get(),
          flinkEngineConnContext
        )
        Some(operation.execute())
      case command if command.toString.startsWith("SHOW_") =>
        val operation = OperationFactory.getOperationFactory.createOperation(
          callOpt.get(),
          flinkEngineConnContext
        )
        Some(operation.execute())
      case _ => None
    }
    resultSet.foreach(r =>
      r.getResultKind match {
        case SUCCESS_WITH_CONTENT =>
          logger.info(r.toString)
          return
        case _ => return
      }
    )
    val tableResult =
      flinkEngineConnContext.getExecutionContext.wrapClassLoader(new Supplier[TableResult] {
        override def get(): TableResult =
          flinkEngineConnContext.getExecutionContext.getTableEnvironment.executeSql(trimmedCode)
      })
    if (tableResult.getJobClient.isPresent) {
      val jobClient = tableResult.getJobClient.get
      jobClient match {
        case adaptor: ClusterClientJobClientAdapter[ApplicationId] =>
          logger.info(s"jobId is ${jobClient.getJobID.toHexString}")
          clusterDescriptor.deployCluster(
            jobClient.getJobID,
            FlinkCodeOnceExecutor.getClusterClient(adaptor)
          )
      }
      this synchronized notify()
      tableResult.await()
    }
    tableResult.getResultKind match {
      case ResultKind.SUCCESS_WITH_CONTENT =>
        tableResult.print()
      case _ =>
    }
    logger.info(
      s"Costs ${ByteTimeUtils.msDurationToString(System.currentTimeMillis - startTime)} to complete."
    )
  }

  override def close(): Unit = {
    this synchronized notify()
    if (!future.isDone) future.cancel(true)
    super.close()
  }

}

object FlinkCodeOnceExecutor {

  private val clusterClientProviderField = classOf[ClusterClientJobClientAdapter[ApplicationId]]
    .getDeclaredField("clusterClientProvider")

  clusterClientProviderField.setAccessible(true)

  def getClusterClient(
      adaptor: ClusterClientJobClientAdapter[ApplicationId]
  ): ClusterClient[ApplicationId] = {
    clusterClientProviderField.get(adaptor) match {
      case provider: ClusterClientProvider[ApplicationId] => provider.getClusterClient
    }
  }

}
