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

package org.apache.linkis.manager.engineplugin.jdbc.executor

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.{
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.governance.common.protocol.conf.{
  RequestQueryEngineConfig,
  ResponseQueryConfig
}
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.engineplugin.jdbc.ConnectionManager
import org.apache.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration
import org.apache.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration.{
  NOT_SUPPORT_LIMIT_DBS,
  SUPPORT_CONN_PARAM_EXECUTE_ENABLE
}
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant
import org.apache.linkis.manager.engineplugin.jdbc.errorcode.JDBCErrorCodeSummary.JDBC_GET_DATASOURCEINFO_ERROR
import org.apache.linkis.manager.engineplugin.jdbc.exception.{
  JDBCGetDatasourceInfoException,
  JDBCParamsIllegalException
}
import org.apache.linkis.manager.engineplugin.jdbc.monitor.ProgressMonitor
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.CacheableProtocol
import org.apache.linkis.protocol.constants.TaskConstant
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.{RPCMapCache, Sender}
import org.apache.linkis.scheduler.executer.{
  AliasOutputExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.commons.collections.MapUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import org.springframework.util.CollectionUtils

import java.sql.{Connection, ResultSet, Statement}
import java.util
import java.util.concurrent.ConcurrentHashMap

import scala.collection.mutable.ArrayBuffer

class JDBCEngineConnExecutor(override val outputPrintLimit: Int, val id: Int)
    extends ConcurrentComputationExecutor(outputPrintLimit) {

  private val connectionManager = ConnectionManager.getInstance()
  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val progressMonitors: util.Map[String, ProgressMonitor[_]] =
    new ConcurrentHashMap[String, ProgressMonitor[_]]()

  private val connectionCache: util.Map[String, Connection] = new util.HashMap[String, Connection]()

  override def init(): Unit = {
    logger.info("jdbc executor start init.")
    setCodeParser(new SQLCodeParser)
    super.init()
    if (JDBCConfiguration.JDBC_KERBEROS_ENABLE.getValue) {
      connectionManager.startRefreshKerberosLoginStatusThread()
    }
  }

  override def execute(engineConnTask: EngineConnTask): ExecuteResponse = {
    val executeResponse = super.execute(engineConnTask)
    if (StringUtils.isNotBlank(engineConnTask.getTaskId)) {
      val connection = connectionCache.remove(engineConnTask.getTaskId)
      logger.info(s"remove task ${engineConnTask.getTaskId} connection")
      Utils.tryAndWarn(connection.close())
    }
    executeResponse
  }

  private def getConnection(engineExecutorContext: EngineExecutionContext): Connection = {

    val taskId = engineExecutorContext.getJobId.orNull
    if (StringUtils.isNotBlank(taskId) && connectionCache.containsKey(taskId)) {
      logger.info(
        s"Task ${taskId}  paragraph ${engineExecutorContext.getCurrentParagraph} from cache get connection"
      )
      return connectionCache.get(taskId)
    }
    val properties: util.Map[String, String] = getJDBCRuntimeParams(engineExecutorContext)
    logger.info(s"The jdbc properties is: $properties")
    val dataSourceName = properties.get(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS)
    val dataSourceMaxVersionId =
      properties.get(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_MAX_VERSION_ID)
    logger.info(
      s"The data source name is [$dataSourceName], and the jdbc client begins to run task ${taskId}"
    )
    logger.info(s"The data source properties is $properties")
    /* url + user as the cache key */
    val jdbcUrl: String = properties.get(JDBCEngineConnConstant.JDBC_URL)
    val execUser: String = properties.get(JDBCEngineConnConstant.JDBC_SCRIPTS_EXEC_USER)
    val proxyUser: String = properties.get(JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY)
    var dataSourceIdentifier = s"$jdbcUrl-$execUser-$proxyUser"
    /* If datasource is used, use datasource name as the cache key */
    if (StringUtils.isNotBlank(dataSourceName)) {
      dataSourceIdentifier = s"$dataSourceName-$dataSourceMaxVersionId"
    }
    // deal with url param for ds conn
    parseJdbcUrl(jdbcUrl, properties)
    val connection = connectionManager.getConnection(dataSourceIdentifier, properties)
    if (StringUtils.isNotBlank(taskId)) {
      connectionCache.put(taskId, connection)
    }
    connection
  }

  def parseJdbcUrl(jdbcUrl: String, parameters: util.Map[String, String]): Unit = {
    if (StringUtils.isEmpty(jdbcUrl)) {
      return None
    }
    val queryIndex = jdbcUrl.indexOf('?')
    if (queryIndex != -1) {
      val query = jdbcUrl.substring(queryIndex + 1)
      val pairs = query.split("&")

      pairs.foreach { pair =>
        try {
          val keyValue = pair.split("=", 2)
          if (keyValue.length == 2) {
            val key = keyValue(0)
            val value = keyValue(1)
            parameters.put(key, value)
          }
        } catch {
          case e: Exception =>
            logger.info(s"wrong link parameters: ${pair}")
        }
      }
    }
  }

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {

    val taskId = engineExecutorContext.getJobId.get
    val connection: Connection = getConnection(engineExecutorContext)
    var statement: Statement = null
    var resultSet: ResultSet = null

    try {
      statement = connection.createStatement()
      if (statement.getQueryTimeout == 0) {
        statement.setQueryTimeout(JDBCConfiguration.JDBC_QUERY_TIMEOUT.getValue)
      }
      statement.setFetchSize(outputPrintLimit)

      val monitor = ProgressMonitor.attachMonitor(statement)
      if (monitor != null) {
        monitor.callback(new Runnable {
          override def run(): Unit = {
            engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId))
          }
        })
        progressMonitors.put(taskId, monitor)
      }
      logger.info(s"create statement is:  $statement")
      connectionManager.saveStatement(taskId, statement)
      val properties: util.Map[String, String] = getJDBCRuntimeParams(engineExecutorContext)
      val jdbcUrl: String = properties.get(JDBCEngineConnConstant.JDBC_URL)
      var newCode = code
      val dbs: Array[String] = NOT_SUPPORT_LIMIT_DBS.split(",")
      if (StringUtils.isNotBlank(jdbcUrl) && dbs.length > 0) {
        dbs.foreach(dbName => {
          if (jdbcUrl.toLowerCase().contains(dbName.toLowerCase())) {
            newCode = code.replaceAll("(?i)limit[^;]*;?$", "").trim
          }
        })
      }
      val isResultSetAvailable = statement.execute(newCode)
      logger.info(s"Is ResultSet available ? : $isResultSetAvailable")
      if (monitor != null) {
        /* refresh progress */
        engineExecutorContext.pushProgress(progress(taskId), getProgressInfo(taskId))
      }
      try {
        if (isResultSetAvailable) {
          logger.info("ResultSet is available")
          resultSet = statement.getResultSet
          return getExecResultSetOutput(engineExecutorContext, statement, resultSet)
        } else {
          val updateCount = statement.getUpdateCount
          logger.info(s"only return affect rows : $updateCount")
          engineExecutorContext.appendStdout(s"only return affect rows : $updateCount")
          return SuccessExecuteResponse()
        }
      } finally {
        if (resultSet != null) {
          Utils.tryAndWarn(resultSet.close())
        }
        if (statement != null) {
          Utils.tryAndWarn(statement.close())
        }
      }
    } catch {
      case e: Throwable =>
        logger.error(s"Cannot run $code", e)
        return ErrorExecuteResponse(e.getMessage, e)
    } finally {
      connectionManager.removeStatement(taskId)
    }
    SuccessExecuteResponse()
  }

  private def getJDBCRuntimeParams(
      engineExecutorContext: EngineExecutionContext
  ): util.Map[String, String] = {
    // todo Improve the more detailed configuration of jdbc parameters, such as: connection pool parameters, etc.
    val execSqlUser = getExecSqlUser(engineExecutorContext)
    // jdbc parameters specified at runtime
    var executorProperties =
      engineExecutorContext.getProperties.asInstanceOf[util.Map[String, String]]
    if (executorProperties == null) {
      executorProperties = new util.HashMap[String, String]()
    }

    // global jdbc engine params by console
    val globalConfig: util.Map[String, String] =
      Utils.tryAndWarn(JDBCEngineConfig.getCacheMap(engineExecutorContext.getLabels))

    // jdbc params by datasource info
    var dataSourceInfo: util.Map[String, String] = new util.HashMap[String, String]()
    var dataSourceName =
      executorProperties.getOrDefault(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS, "")
    val dataSourceQuerySystemParam = executorProperties.getOrDefault(
      JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_SYSTEM_QUERY_PARAM,
      ""
    )

    if (StringUtils.isNotBlank(dataSourceName)) {
      logger.info("Start getting data source connection parameters from the data source hub.")
      Utils.tryCatch {
        dataSourceInfo = JDBCMultiDatasourceParser.queryDatasourceInfoByName(
          dataSourceName,
          execSqlUser,
          dataSourceQuerySystemParam
        )
      } { e: Throwable =>
        e match {
          case jpe: JDBCParamsIllegalException =>
            throw new JDBCGetDatasourceInfoException(jpe.getErrCode, jpe.getDesc, e)
          case _ =>
            throw new JDBCGetDatasourceInfoException(
              JDBC_GET_DATASOURCEINFO_ERROR.getErrorCode,
              e.getMessage,
              e
            )
        }

      }
    }
    if (StringUtils.isBlank(dataSourceName)) {
      dataSourceName = JDBCEngineConnConstant.JDBC_DEFAULT_DATASOURCE_TAG
    }
    if (MapUtils.isEmpty(dataSourceInfo) && SUPPORT_CONN_PARAM_EXECUTE_ENABLE) {
      val connHost: String =
        executorProperties.getOrDefault(
          JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_PARAM_HOST,
          ""
        )
      val connPort: String =
        executorProperties.getOrDefault(
          JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_PARAM_PORT,
          ""
        )
      val connDsType: String =
        executorProperties.getOrDefault(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_TYPE, "")
      val submitUser: String = executorProperties.getOrDefault(TaskConstant.SUBMIT_USER, "")
      val executeUser: String =
        executorProperties.getOrDefault(TaskConstant.EXECUTE_USER, submitUser)

      if (
          StringUtils.isNotBlank(connHost) && StringUtils
            .isNotBlank(connPort) && StringUtils
            .isNotBlank(connDsType) && StringUtils.isNotBlank(executeUser)
      ) {
        logger.info(
          s"use conn ip and port get dataSourceInfo: executeUser:${execSqlUser} ip:${connHost}, " +
            s"port:${connPort}, dsType:${connDsType}, " +
            s"createUser:${submitUser} connUser: ${executeUser}"
        )
        dataSourceInfo = JDBCMultiDatasourceParser.queryDatasourceInfoByConnParams(
          executeUser,
          executeUser,
          connHost,
          connPort,
          connDsType
        )
      }
    }

    // runtime jdbc params > jdbc datasource info > jdbc engine global config
    if (dataSourceInfo != null && !dataSourceInfo.isEmpty) {
      globalConfig.putAll(dataSourceInfo)
    }

    if (!executorProperties.isEmpty) {
      globalConfig.putAll(executorProperties)
    }
    globalConfig.put(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS, dataSourceName)
    globalConfig.put(JDBCEngineConnConstant.JDBC_SCRIPTS_EXEC_USER, execSqlUser)
    globalConfig.put(
      JDBCEngineConnConstant.JDBC_PROXY_USER,
      globalConfig.getOrDefault(JDBCEngineConnConstant.JDBC_PROXY_USER, execSqlUser)
    )
    globalConfig
  }

  private def getExecResultSetOutput(
      engineExecutorContext: EngineExecutionContext,
      statement: Statement,
      resultSet: ResultSet
  ): ExecuteResponse = {
    if (isDDLCommand(statement.getUpdateCount, resultSet.getMetaData.getColumnCount)) {
      logger.info(s"current result is a ResultSet Object , but there are no more results!")
      SuccessExecuteResponse()
    } else {
      val md = resultSet.getMetaData
      val metaArrayBuffer = new ArrayBuffer[(String, String)]()
      for (i <- 1 to md.getColumnCount) {
        metaArrayBuffer.append(
          Tuple2(md.getColumnName(i), JDBCHelper.getTypeStr(md.getColumnType(i)))
        )
      }
      val columns =
        metaArrayBuffer.map { c => new Column(c._1, DataType.toDataType(c._2), "") }.toArray[Column]
      val metaData = new TableMetaData(columns)
      val resultSetWriter =
        engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
      resultSetWriter.addMetaData(metaData)
      Utils.tryCatch({
        while (resultSet.next()) {
          val r: Array[Any] = columns.indices.map { i =>
            val data = resultSet.getObject(i + 1) match {
              case value: Array[Byte] =>
                new String(resultSet.getObject(i + 1).asInstanceOf[Array[Byte]])
              case value: Any => resultSet.getString(i + 1)
              case _ => null
            }
            data
          }.toArray
          resultSetWriter.addRecord(new TableRecord(r.asInstanceOf[Array[Any]]))
        }
      }) { case e: Exception =>
        return ErrorExecuteResponse("query jdbc failed", e)
      }
      val output = if (resultSetWriter != null) resultSetWriter.toString else null
      Utils.tryQuietly {
        IOUtils.closeQuietly(resultSetWriter)
      }
      logger.info("sql executed completed.")
      AliasOutputExecuteResponse(null, output)
    }
  }

  private def getExecSqlUser(engineExecutionContext: EngineExecutionContext): String = {
    val userCreatorLabel = engineExecutionContext.getLabels
      .find(_.isInstanceOf[UserCreatorLabel])
      .get
      .asInstanceOf[UserCreatorLabel]
    userCreatorLabel.getUser
  }

  protected def isDDLCommand(updatedCount: Int, columnCount: Int): Boolean = {
    updatedCount < 0 && columnCount <= 0
  }

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = {
    val monitor = progressMonitors.get(taskID)
    if (monitor != null) {
      Array(monitor.jobProgressInfo(taskID))
    } else {
      Array.empty[JobProgressInfo]
    }
  }

  override protected def callback(): Unit = {}

  override def progress(taskID: String): Float = {
    val monitor = progressMonitors.get(taskID)
    if (monitor != null) {
      monitor.getSqlProgress
    } else {
      0
    }
  }

  override def close(): Unit = {
    logger.info("Start closing the jdbc engine.")
    Utils.tryQuietly(killAll())
    connectionManager.close()
    if (JDBCConfiguration.JDBC_KERBEROS_ENABLE.getValue) {
      connectionManager.shutdownRefreshKerberosLoginService()
    }

    logger.info("The jdbc engine has closed successfully.")
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = null

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (!CollectionUtils.isEmpty(labels)) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = null

  override def getCurrentNodeResource(): NodeResource = {
    NodeResourceUtils.appendMemoryUnitIfMissing(
      EngineConnObject.getEngineCreationContext.getOptions
    )

    val resource = new CommonNodeResource
    val usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
    resource.setUsedResource(usedResource)
    resource
  }

  override def supportCallBackLogs(): Boolean = false

  override def getId: String = Sender.getThisServiceInstance.getInstance + s"_$id"

  override def getConcurrentLimit: Int = JDBCConfiguration.JDBC_CONCURRENT_LIMIT.getValue

  override def killAll(): Unit = {
    logger.info("Killing all query task.")
    connectionManager.initTaskStatementMap()
    logger.info("All query task has killed successfully.")
  }

  override def killTask(taskId: String): Unit = {
    logger.info(s"Killing jdbc query task $taskId")
    connectionManager.cancelStatement(taskId)
    super.killTask(taskId)
    logger.info(s"The query task $taskId has killed successfully.")
  }

}

object JDBCEngineConfig
    extends RPCMapCache[Array[Label[_]], String, String](
      Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue
    ) {

  override protected def createRequest(labels: Array[Label[_]]): CacheableProtocol = {
    val userCreatorLabel =
      labels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
    val engineTypeLabel =
      labels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
    RequestQueryEngineConfig(userCreatorLabel, engineTypeLabel)
  }

  override protected def createMap(any: Any): util.Map[String, String] = any match {
    case response: ResponseQueryConfig => response.getKeyAndValue
  }

}
