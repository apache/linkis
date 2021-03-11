/**
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.entrance.executor

import java.sql.SQLException
import java.util
import java.util.Objects
import java.util.concurrent.atomic.AtomicReference

import com.facebook.presto.client.{ClientSession, QueryStatusInfo, StatementClient, StatementClientFactory}
import com.facebook.presto.spi.security.SelectedRole
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.{PrestoClientException, PrestoStateInvalidException}
import com.webank.wedatasphere.linkis.entrance.execute.{EngineExecuteAsynReturn, EntranceEngine, EntranceJob, StorePathExecuteRequest}
import com.webank.wedatasphere.linkis.entrance.persistence.EntranceResultSetEngine
import com.webank.wedatasphere.linkis.entrance.utils.SqlCodeParser
import com.webank.wedatasphere.linkis.protocol.engine.{JobProgressInfo, RequestTask}
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import com.webank.wedatasphere.linkis.storage.domain.Column
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import okhttp3.OkHttpClient
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

import scala.collection.JavaConverters._
import scala.collection.mutable


/**
 * Created by yogafire on 2020/4/30
 */
class PrestoEntranceEngineExecutor(id: Long, job: EntranceJob, clientSession: AtomicReference[ClientSession], okHttpClient: OkHttpClient, releaseResource: () => Unit) extends EntranceEngine(id = id) with SingleTaskOperateSupport with SingleTaskInfoSupport with Logging {
  private var statement: StatementClient = _
  private val persistEngine = new EntranceResultSetEngine()
  //execute line number，as alias and progress line
  private var codeLine = 0
  //total line number
  private var totalCodeLineNumber = 0
  private val jobProgressInfos = mutable.ListBuffer[JobProgressInfo]()
  private val serviceInstance: ServiceInstance = ServiceInstance("prestoEngine", "")

  override def getModuleInstance: ServiceInstance = serviceInstance

  override def setServiceInstance(applicationName: String, instance: String): Unit = {}

  override def toString: String = s"${serviceInstance.getApplicationName}Entrance($getId, $getUser, $getCreator, ${serviceInstance.getInstance})"

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    if (StringUtils.isEmpty(executeRequest.code)) {
      return IncompleteExecuteResponse("execute codes can not be empty)")
    }
    val storePath = executeRequest match {
      case storePathExecuteRequest: StorePathExecuteRequest => storePathExecuteRequest.storePath
      case _ => ""
    }

    val codes = SqlCodeParser.parse(executeRequest.code)
    if (!codes.isEmpty) {
      totalCodeLineNumber = codes.length
      transition(ExecutorState.Busy)
      codes.foreach(code => {
        try {
          val executeRes = executeLine(code, storePath, s"_$codeLine")
          executeRes match {
            case aliasOutputExecuteResponse: AliasOutputExecuteResponse =>
              persistEngine.persistResultSet(job, aliasOutputExecuteResponse)
            case SuccessExecuteResponse() =>
              info(s"sql execute successfully : $code")
            case _ =>
              warn("no matching exception")
          }
          codeLine += 1
        } catch {
          case t: Throwable =>
            error("Presto execute failed", t)
            job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateERROR(ExceptionUtils.getFullStackTrace(t))))
            close()
            return ErrorExecuteResponse("Presto execute failed", t)
        }
      })
    }
    close()
    SuccessExecuteResponse()
  }

  private def executeLine(code: String, storePath: String, alias: String): ExecuteResponse = {
    val realCode = code.trim
    info(s"presto client begins to run psql code:\n $realCode")

    statement = StatementClientFactory.newStatementClient(okHttpClient, clientSession.get(), realCode)

    initialStatusUpdates(statement)

    var response: ExecuteResponse = SuccessExecuteResponse()
    if (statement.isRunning || (statement.isFinished && statement.finalStatusInfo().getError == null)) {
      response = queryOutput(statement, storePath, alias)
    }

    verifyServerError(statement)

    updateSession(statement)

    response
  }

  override def kill(): Boolean = {
    close()
    true
  }

  override def close(): Unit = {
    statement.close()
    totalCodeLineNumber = 0
    transition(ExecutorState.Dead)
    job.setResultSize(0)
    releaseResource.apply()
  }

  def shutdown(): Unit = {
    close()
    job.afterStateChanged(job.getState, SchedulerEventState.Cancelled)
  }

  override def progress(): Float = {
    Utils.tryCatch({
      val progress = (codeLine.toFloat + (statement.getStats.getProgressPercentage.orElse(0) * 0.01f).toFloat) / totalCodeLineNumber
      return progress
    }) {
      t: Throwable => warn(s"get presto progress error. ${t.getMessage}")
    }
    0
  }

  override def getProgressInfo: Array[JobProgressInfo] = {
    Utils.tryCatch({
      var statusInfo: QueryStatusInfo = null
      if (statement.isFinished) {
        statusInfo = statement.finalStatusInfo()
      } else {
        statusInfo = statement.currentStatusInfo()
      }
      val progressInfo = JobProgressInfo(statusInfo.getId, statement.getStats.getTotalSplits, statement.getStats.getRunningSplits, 0, statement.getStats.getCompletedSplits)
      if (jobProgressInfos.size > codeLine) {
        jobProgressInfos(codeLine) = progressInfo
      } else {
        jobProgressInfos += progressInfo
      }
    }) {
      t: Throwable => warn(s"update presto progressInfo error. ${t.getMessage}")
    }
    jobProgressInfos.toArray
  }


  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = ???

  override def log(): String = ""

  override def pause(): Boolean = ???

  override def resume(): Boolean = ???

  def initialStatusUpdates(statement: StatementClient): Unit = {
    while (statement.isRunning
      && (statement.currentData().getData == null || statement.currentStatusInfo().getUpdateType != null)) {
      job.getProgressListener.foreach(_.onProgressUpdate(job, progress(), getProgressInfo))
      statement.advance()
    }
  }

  private def queryOutput(statement: StatementClient, storePath: String, alias: String): AliasOutputExecuteResponse = {
    var columnCount = 0
    var rows = 0
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TABLE_TYPE)
    val resultSetPath = resultSet.getResultSetPath(new FsPath(storePath), alias)
    val resultSetWriter = ResultSetWriter.getResultSetWriter(resultSet, PrestoConfiguration.ENTRANCE_RESULTS_MAX_CACHE.getValue.toLong, resultSetPath, job.getUser)
    Utils.tryFinally({
      var results: QueryStatusInfo = null
      if (statement.isRunning) {
        results = statement.currentStatusInfo()
      } else {
        results = statement.finalStatusInfo()
      }
      if (results.getColumns == null) {
        throw new RuntimeException("presto columns is null.")
      }
      val columns = results.getColumns.asScala
        .map(column => Column(column.getName, column.getType, "")).toArray[Column]
      columnCount = columns.length
      resultSetWriter.addMetaData(new TableMetaData(columns))
      while (statement.isRunning) {
        val data = statement.currentData().getData
        if (data != null) for (row <- data.asScala) {
          val rowArray = row.asScala.map(r => String.valueOf(r))
          resultSetWriter.addRecord(new TableRecord(rowArray.toArray))
          rows += 1
        }
        job.getProgressListener.foreach(_.onProgressUpdate(job, progress(), getProgressInfo))
        statement.advance()
      }
    })(IOUtils.closeQuietly(resultSetWriter))

    info(s"Fetched $columnCount col(s) : $rows row(s) in presto")
    job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateInfo(s"Fetched $columnCount col(s) : $rows row(s) in presto")))
    val output = if (resultSetWriter != null) resultSetWriter.toString else null
    AliasOutputExecuteResponse(alias, output)
  }

  // check presto error
  private def verifyServerError(statement: StatementClient): Unit = {
    job.getProgressListener.foreach(_.onProgressUpdate(job, progress(), getProgressInfo))
    if (statement.isFinished) {
      val info: QueryStatusInfo = statement.finalStatusInfo()
      if (info.getError != null) {
        val error = Objects.requireNonNull(info.getError);
        val message: String = s"Presto execute failed (#${info.getId}): ${error.getMessage}"
        var cause: Throwable = null
        if (error.getFailureInfo != null) {
          cause = error.getFailureInfo.toException
        }
        throw new SQLException(message, error.getSqlState, error.getErrorCode, cause)
      }
    } else if (statement.isClientAborted) {
      warn(s"Presto statement is killed by ${job.getUser}")
    } else if (statement.isClientError) {
      throw PrestoClientException("Presto client error.")
    } else {
      throw PrestoStateInvalidException("Presto status error. Statement is not finished.")
    }
  }

  private def updateSession(statement: StatementClient): Unit = {
    var newSession = clientSession.get()
    // update catalog and schema if present
    if (statement.getSetCatalog.isPresent || statement.getSetSchema.isPresent) {
      newSession = ClientSession.builder(newSession)
        .withCatalog(statement.getSetCatalog.orElse(newSession.getCatalog))
        .withSchema(statement.getSetSchema.orElse(newSession.getSchema))
        .build
    }

    // update transaction ID if necessary
    if (statement.isClearTransactionId) newSession = ClientSession.stripTransactionId(newSession)

    var builder: ClientSession.Builder = ClientSession.builder(newSession)

    if (statement.getStartedTransactionId != null) builder = builder.withTransactionId(statement.getStartedTransactionId)

    // update session properties if present
    if (!statement.getSetSessionProperties.isEmpty || !statement.getResetSessionProperties.isEmpty) {
      val sessionProperties: util.Map[String, String] = new util.HashMap[String, String](newSession.getProperties)
      sessionProperties.putAll(statement.getSetSessionProperties)
      sessionProperties.keySet.removeAll(statement.getResetSessionProperties)
      builder = builder.withProperties(sessionProperties)
    }

    // update session roles
    if (!statement.getSetRoles.isEmpty) {
      val roles: util.Map[String, SelectedRole] = new util.HashMap[String, SelectedRole](newSession.getRoles)
      roles.putAll(statement.getSetRoles)
      builder = builder.withRoles(roles)
    }

    // update prepared statements if present
    if (!statement.getAddedPreparedStatements.isEmpty || !statement.getDeallocatedPreparedStatements.isEmpty) {
      val preparedStatements: util.Map[String, String] = new util.HashMap[String, String](newSession.getPreparedStatements)
      preparedStatements.putAll(statement.getAddedPreparedStatements)
      preparedStatements.keySet.removeAll(statement.getDeallocatedPreparedStatements)
      builder = builder.withPreparedStatements(preparedStatements)
    }

    clientSession.set(newSession)
  }
}
