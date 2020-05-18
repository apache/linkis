package com.webank.wedatasphere.linkis.entrance.executor

import java.sql.SQLException
import java.util.Objects

import com.facebook.presto.client.{ClientSession, QueryStatusInfo, StatementClient, StatementClientFactory}
import com.webank.wedatasphere.linkis.common.ServiceInstance
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration
import com.webank.wedatasphere.linkis.entrance.exception.PrestoStateInvalidException
import com.webank.wedatasphere.linkis.entrance.execute.{EngineExecuteAsynReturn, EntranceEngine, EntranceJob, StorePathExecuteRequest}
import com.webank.wedatasphere.linkis.entrance.persistence.EntranceResultSetEngine
import com.webank.wedatasphere.linkis.entrance.utils.SqlCodeParser
import com.webank.wedatasphere.linkis.protocol.engine.{JobProgressInfo, RequestTask}
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.domain.Column
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import okhttp3.OkHttpClient
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConverters._


/**
 * Created by yogafire on 2020/4/30
 */
class PrestoEntranceEngineExecutor(job: EntranceJob, clientSession: ClientSession, okHttpClient: OkHttpClient) extends EntranceEngine(id = 0) with SingleTaskOperateSupport with SingleTaskInfoSupport with Logging {
  private var statement: StatementClient = _
  private val persistEngine = new EntranceResultSetEngine()
  //execute line number，as alias and progress line
  private var codeLine = 0
  //total line number
  private var totalCodeLineNumber = 0

  override def getModuleInstance: ServiceInstance = ServiceInstance("prestoEngine", "")

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
      codeLine = 0
      codes.foreach(code => {
        try {
          val executeRes = executeLine(code, storePath, codeLine.toString)
          executeRes match {
            case aliasOutputExecuteResponse: AliasOutputExecuteResponse =>
              persistEngine.persistResultSet(job, aliasOutputExecuteResponse)
            case SuccessExecuteResponse() =>
              info(s"sql execute successfully : $code")
            case _ =>
              warn("no matching exception")
          }
          codeLine = codeLine + 1
        } catch {
          case e: Exception =>
            totalCodeLineNumber = 0
            error("Presto Exception", e)
            job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateERROR(e.getMessage)))
            return ErrorExecuteResponse("Presto execute exception", e)
          case t: Throwable =>
            totalCodeLineNumber = 0
            error("Presto execute failed", t)
            job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateERROR(t.getMessage)))
            return ErrorExecuteResponse("Presto execute failed", t)
        } finally {
          Utils.tryQuietly({
            statement.close()
          })
        }
      })
    }
    totalCodeLineNumber = 0
    job.setResultSize(0)
    SuccessExecuteResponse()
  }

  private def executeLine(code: String, storePath: String, alias: String): ExecuteResponse = {
    val realCode = code.trim
    info(s"presto client begins to run psql code:\n $realCode")

    statement = StatementClientFactory.newStatementClient(okHttpClient, clientSession, realCode)

    initialStatusUpdates(statement)

    var response: ExecuteResponse = SuccessExecuteResponse()
    if (statement.isRunning || (statement.isFinished && statement.finalStatusInfo().getError == null)) {
      if (statement.currentStatusInfo().getUpdateType == null) {
        response = queryOutput(statement, storePath, alias)
      } else {
        while (statement.isRunning) {
          statement.advance()
        }
      }
    }
    verifyServerError(statement)
    response
  }

  override def kill(): Boolean = {
    close()
    true
  }

  override def close(): Unit = {
    statement.close()
  }

  override def progress(): Float = (codeLine.toFloat / totalCodeLineNumber) * statement.getStats.getProgressPercentage.getAsDouble.toFloat

  override def getProgressInfo: Array[JobProgressInfo] = {
    val totalSplits = statement.getStats.getTotalSplits
    val runningSplits = statement.getStats.getRunningSplits
    val completedSplits = statement.getStats.getCompletedSplits
    Array[JobProgressInfo](JobProgressInfo(statement.currentStatusInfo().getId, totalSplits, runningSplits, 0, completedSplits))
  }


  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = ???

  override def log(): String = "Presto entrance is running"

  override def pause(): Boolean = ???

  override def resume(): Boolean = ???

  def initialStatusUpdates(statement: StatementClient): Unit = {
    while (statement.isRunning && statement.currentData().getData == null) {
      statement.advance()
    }
  }

  def queryOutput(statement: StatementClient, storePath: String, alias: String): AliasOutputExecuteResponse = {
    var columnCount = 0
    var rows = 0
    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TABLE_TYPE)
    val resultSetPath = resultSet.getResultSetPath(new FsPath(storePath), alias)
    val resultSetWriter = ResultSetWriter.getResultSetWriter(resultSet, PrestoConfiguration.PRESTO_RESULTS_MAX_CACHE.getValue.toLong, resultSetPath, job.asInstanceOf[EntranceJob].getUser)
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
        statement.advance()
      }
    })(IOUtils.closeQuietly(resultSetWriter))

    info(s"Fetched $columnCount col(s) : $rows row(s) in presto")
    job.getLogListener.foreach(_.onLogUpdate(job, LogUtils.generateInfo(s"Fetched $columnCount col(s) : $rows row(s) in presto")))
    val output = if (resultSetWriter != null) resultSetWriter.toString else null
    AliasOutputExecuteResponse(alias, output)
  }

  // check presto error
  def verifyServerError(statement: StatementClient): Unit = {
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
    } else {
      throw PrestoStateInvalidException("Presto status error. execute is not finished.")
    }
  }
}
