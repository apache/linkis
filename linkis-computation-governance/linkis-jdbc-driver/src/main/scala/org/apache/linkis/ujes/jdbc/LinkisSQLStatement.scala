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

package org.apache.linkis.ujes.jdbc

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.ujes.client.request.OpenLogAction
import org.apache.linkis.ujes.client.response.JobExecuteResult
import org.apache.linkis.ujes.jdbc.hook.JDBCDriverPreExecutionHook

import org.apache.commons.lang3.StringUtils

import java.sql.{Connection, ResultSet, SQLException, SQLWarning, Statement}
import java.util
import java.util.concurrent.TimeUnit

import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration

class LinkisSQLStatement(private[jdbc] val ujesSQLConnection: LinkisSQLConnection)
    extends Statement
    with Logging {

  private var jobExecuteResult: JobExecuteResult = _
  private var resultSet: UJESSQLResultSet = _
  private var closed = false
  private var maxRows: Int = 0
  private var fetchSize = 100
  private var queryTimeout = 0

  private var logPath: String = null

  private var queryEnd = false

  private var logFromLen = 0
  private val logSize = 100

  private[jdbc] def throwWhenClosed[T](op: => T): T = ujesSQLConnection.throwWhenClosed {
    if (isClosed) throw new LinkisSQLException(LinkisSQLErrorCode.STATEMENT_CLOSED)
    else op
  }

  override def executeQuery(sql: String): UJESSQLResultSet = {
    if (!execute(sql)) throw new LinkisSQLException(LinkisSQLErrorCode.RESULTSET_NULL)
    resultSet
  }

  override def executeUpdate(sql: String): Int = {
    execute(sql)
    0
  }

  override def close(): Unit = {
    closed = true
    clearQuery()
  }

  def clearQuery(): Unit = {
    if (jobExecuteResult != null && !queryEnd) {
      Utils.tryAndWarn(ujesSQLConnection.ujesClient.kill(jobExecuteResult))
      jobExecuteResult = null
    }
    if (resultSet != null) {
      Utils.tryAndWarn(resultSet.close())
      resultSet = null
    }
  }

  override def getMaxFieldSize: Int = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
    "getMaxFieldSize not supported"
  )

  override def setMaxFieldSize(max: Int): Unit = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
    "setMaxFieldSize not supported"
  )

  override def getMaxRows: Int = maxRows

  override def setMaxRows(max: Int): Unit = this.maxRows = max

  override def setEscapeProcessing(enable: Boolean): Unit = if (enable) {
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "setEscapeProcessing not supported"
    )
  }

  override def getQueryTimeout: Int = queryTimeout

  override def setQueryTimeout(seconds: Int): Unit = throwWhenClosed(queryTimeout = seconds * 1000)

  override def cancel(): Unit = throwWhenClosed(clearQuery())

  override def getWarnings: SQLWarning = null

  override def clearWarnings(): Unit = {}

  override def setCursorName(name: String): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "setCursorName not supported"
    )

  override def execute(sql: String): Boolean = throwWhenClosed {
    var parsedSQL = sql
    JDBCDriverPreExecutionHook.getPreExecutionHooks.foreach { preExecution =>
      parsedSQL = preExecution.callPreExecutionHook(parsedSQL, !ujesSQLConnection.isTableau())
    }
    logger.info(s"begin to execute sql ${parsedSQL}")
    queryEnd = false
    logPath = null
    try {
      jobExecuteResult = ujesSQLConnection.toSubmit(parsedSQL)
      val atMost =
        if (queryTimeout > 0) Duration(queryTimeout, TimeUnit.MILLISECONDS) else Duration.Inf
      var jobInfo = ujesSQLConnection.ujesClient.getJobInfo(jobExecuteResult)
      logPath = jobInfo.getRequestPersistTask.getLogPath
      if (!ExecutionNodeStatus.isCompleted(ExecutionNodeStatus.valueOf(jobInfo.getJobStatus))) {
        Utils.tryThrow {
          Utils.waitUntil(
            () => {
              jobInfo = ujesSQLConnection.ujesClient.getJobInfo(jobExecuteResult)
              ExecutionNodeStatus.isCompleted(
                ExecutionNodeStatus.valueOf(jobInfo.getJobStatus)
              ) || closed
            },
            atMost,
            100,
            10000
          )
        } {
          case t: TimeoutException =>
            if (queryTimeout > 0) clearQuery()
            logPath = jobInfo.getRequestPersistTask.getLogPath
            new LinkisSQLException(LinkisSQLErrorCode.QUERY_TIMEOUT, "query has been timeout!")
              .initCause(t)
          case t => t
        }
      }
      logPath = jobInfo.getRequestPersistTask.getLogPath
      if (!ExecutionNodeStatus.isSucceed(ExecutionNodeStatus.valueOf(jobInfo.getJobStatus))) {
        throw new LinkisSQLException(
          jobInfo.getRequestPersistTask.getErrDesc,
          jobInfo.getRequestPersistTask.getErrCode.toString
        )
      }

      logger.info(s"end to execute sql ${parsedSQL}")
      val resultSetList = jobInfo.getResultSetList(ujesSQLConnection.ujesClient)
      logger.info(s"resultSetList is ${resultSetList.mkString(",")}")
      if (resultSetList != null && resultSetList.nonEmpty) {
        resultSet = new UJESSQLResultSet(resultSetList, this, maxRows, fetchSize)
        true
      } else {
        false
      }
    } catch {
      case sqlException: SQLException =>
        throw sqlException
      case throwable: Throwable =>
        val exception =
          new LinkisSQLException(LinkisSQLErrorCode.UNKNOWN_ERROR, throwable.getMessage)
        exception.initCause(throwable)
        throw exception
    } finally {
      queryEnd = true
    }
  }

  def getJobExcuteResult: JobExecuteResult = jobExecuteResult

  override def getResultSet: UJESSQLResultSet = resultSet

  override def getUpdateCount: Int = throwWhenClosed(-1)

  override def getMoreResults: Boolean = false

  override def setFetchDirection(direction: Int): Unit =
    throwWhenClosed(if (direction != ResultSet.FETCH_FORWARD) {
      throw new LinkisSQLException(
        LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
        "only FETCH_FORWARD is supported."
      )
    })

  override def getFetchDirection: Int = throwWhenClosed(ResultSet.FETCH_FORWARD)

  override def setFetchSize(rows: Int): Unit = this.fetchSize = rows

  override def getFetchSize: Int = fetchSize

  override def getResultSetConcurrency: Int = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
    "getResultSetConcurrency not supported."
  )

  override def getResultSetType: Int = throwWhenClosed(ResultSet.TYPE_FORWARD_ONLY)

  override def addBatch(sql: String): Unit =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT, "addBatch not supported.")

  override def clearBatch(): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "clearBatch not supported."
    )

  override def executeBatch(): Array[Int] =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "executeBatch not supported."
    )

  override def getConnection: Connection = throwWhenClosed(ujesSQLConnection)

  override def getMoreResults(current: Int): Boolean = false

  override def getGeneratedKeys: ResultSet = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
    "getGeneratedKeys not supported."
  )

  override def executeUpdate(sql: String, autoGeneratedKeys: Int): Int =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "executeUpdate with autoGeneratedKeys not supported."
    )

  override def executeUpdate(sql: String, columnIndexes: Array[Int]): Int =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "executeUpdate with columnIndexes not supported."
    )

  override def executeUpdate(sql: String, columnNames: Array[String]): Int =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "executeUpdate with columnNames not supported."
    )

  override def execute(sql: String, autoGeneratedKeys: Int): Boolean =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "execute with autoGeneratedKeys not supported."
    )

  override def execute(sql: String, columnIndexes: Array[Int]): Boolean =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "execute with columnIndexes not supported."
    )

  override def execute(sql: String, columnNames: Array[String]): Boolean =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "execute with columnNames not supported."
    )

  override def getResultSetHoldability: Int = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
    "getResultSetHoldability not supported"
  )

  override def isClosed: Boolean = closed

  override def setPoolable(poolable: Boolean): Unit =
    throw new LinkisSQLException(
      LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
      "setPoolable not supported"
    )

  override def isPoolable: Boolean = false

  override def closeOnCompletion(): Unit = throw new LinkisSQLException(
    LinkisSQLErrorCode.NOSUPPORT_STATEMENT,
    "closeOnCompletion not supported"
  )

  override def isCloseOnCompletion: Boolean = false

  override def unwrap[T](iface: Class[T]): T =
    throw new LinkisSQLException(LinkisSQLErrorCode.NOSUPPORT_STATEMENT, "unwrap not supported")

  override def isWrapperFor(iface: Class[_]): Boolean = false

  /**
   * log[0] error log[1] warn log[2] info log[3] all (info + warn + error)
   * @return
   */
  def getAllLog(): Array[String] = {
    if (queryEnd && StringUtils.isNotBlank(logPath)) {
      val openLogAction =
        OpenLogAction.newBuilder().setLogPath(logPath).setProxyUser(ujesSQLConnection.user).build()
      ujesSQLConnection.ujesClient.openLog(openLogAction).getLog
    } else {
      Array.empty[String]
    }
  }

  /**
   * log[0] error log[1] warn log[2] info log[3] all (info + warn + error)
   * @return
   */
  def getIncrementalLog(): util.List[String] = {
    if (null != jobExecuteResult && !queryEnd) {
      val logObj = ujesSQLConnection.ujesClient.log(jobExecuteResult, logFromLen, logSize)
      logFromLen = logObj.fromLine
      logObj.getLog
    } else {
      new util.ArrayList[String]
    }
  }

}
