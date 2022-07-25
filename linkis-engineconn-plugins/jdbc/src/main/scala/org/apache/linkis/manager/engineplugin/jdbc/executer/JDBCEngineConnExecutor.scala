/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.jdbc.executer

import java.sql.{Connection, ResultSet, SQLException, Statement}
import java.util
import org.apache.linkis.common.utils.{OverloadUtils, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{ConcurrentComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.engineplugin.jdbc.ConnectionManager
import org.apache.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.rpc.{RPCMapCache, Sender}
import org.apache.linkis.scheduler.executer.{AliasOutputExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, ResponseQueryConfig}
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.CacheableProtocol
import org.springframework.util.CollectionUtils
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.manager.engineplugin.jdbc.constant.JDBCEngineConnConstant

import scala.collection.JavaConversions._

import scala.collection.mutable.ArrayBuffer

class JDBCEngineConnExecutor(override val outputPrintLimit: Int, val id: Int) extends ConcurrentComputationExecutor(outputPrintLimit) {


  private val connectionManager = ConnectionManager.getInstance()
  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)


  override def init(): Unit = {
    logger.info("jdbc executor start init.")
    setCodeParser(new SQLCodeParser)
    super.init()
    if (JDBCConfiguration.JDBC_KERBEROS_ENABLE.getValue) {
      connectionManager.startRefreshKerberosLoginStatusThread()
    }
  }

  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val execSqlUser = getExecSqlUser(engineExecutorContext)
    val realCode = code.trim()
    val taskId = engineExecutorContext.getJobId.get
    val properties = engineExecutorContext.getProperties.asInstanceOf[util.Map[String, String]]
    var dataSourceName = properties.getOrDefault(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS, "")
    val dataSourceQuerySystemParam = properties.getOrDefault(JDBCEngineConnConstant.JDBC_ENGINE_RUN_TIME_DS_SYSTEM_QUERY_PARAM, "")

    if (properties.get(JDBCEngineConnConstant.JDBC_URL) == null) {
      logger.info(s"The jdbc url is empty, adding now...")
      val globalConfig: util.Map[String, String] = Utils.tryAndWarn(JDBCEngineConfig.getCacheMap(engineExecutorContext.getLabels))
      if (StringUtils.isNotBlank(dataSourceName)) {
        logger.info("Start getting data source connection parameters from the data source hub.")
        Utils.tryCatch {
          val dataSourceInfo = JDBCMultiDatasourceParser.queryDatasourceInfoByName(dataSourceName, execSqlUser, dataSourceQuerySystemParam)
          if (dataSourceInfo != null && !dataSourceInfo.isEmpty) {
            globalConfig.putAll(dataSourceInfo)
          }
        } {
          e: Throwable => return ErrorExecuteResponse(s"Failed to get datasource info about [$dataSourceName] from datasource server.", e)
        }
      }
      properties.put(JDBCEngineConnConstant.JDBC_URL, globalConfig.get(JDBCEngineConnConstant.JDBC_URL))
      properties.put(JDBCEngineConnConstant.JDBC_DRIVER, globalConfig.get(JDBCEngineConnConstant.JDBC_DRIVER))
      properties.put(JDBCEngineConnConstant.JDBC_USERNAME, globalConfig.get(JDBCEngineConnConstant.JDBC_USERNAME))
      properties.put(JDBCEngineConnConstant.JDBC_PASSWORD, globalConfig.get(JDBCEngineConnConstant.JDBC_PASSWORD))
      properties.put(JDBCEngineConnConstant.JDBC_POOL_VALIDATION_QUERY, globalConfig.getOrDefault(JDBCEngineConnConstant.JDBC_POOL_VALIDATION_QUERY, JDBCEngineConnConstant.JDBC_POOL_DEFAULT_VALIDATION_QUERY))
      properties.put(JDBCEngineConnConstant.JDBC_AUTH_TYPE, globalConfig.get(JDBCEngineConnConstant.JDBC_AUTH_TYPE))
      properties.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL, globalConfig.get(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_PRINCIPAL))
      properties.put(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION, globalConfig.get(JDBCEngineConnConstant.JDBC_KERBEROS_AUTH_TYPE_KEYTAB_LOCATION))
      properties.put(JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY, globalConfig.getOrDefault(JDBCEngineConnConstant.JDBC_PROXY_USER_PROPERTY, ""))
      properties.put(JDBCEngineConnConstant.JDBC_PROXY_USER, globalConfig.getOrDefault(JDBCEngineConnConstant.JDBC_PROXY_USER, execSqlUser))
      properties.put(JDBCEngineConnConstant.JDBC_SCRIPTS_EXEC_USER, execSqlUser)
    }
    if (StringUtils.isBlank(dataSourceName)) {
      dataSourceName = JDBCEngineConnConstant.JDBC_DEFAULT_DATASOURCE_TAG;
    }
    logger.info(s"The data source name is [$dataSourceName], and the jdbc client begins to run jdbc code:\n ${realCode.trim}")
    var connection: Connection = null
    var statement: Statement = null
    var resultSet: ResultSet = null
    logger.info(s"The data source properties is $properties")
    Utils.tryCatch({
      connection = connectionManager.getConnection(dataSourceName, properties)
      logger.info("The jdbc connection has created successfully!")
    }) {
      e: Throwable =>
        logger.error(s"created data source connection error! $e")
        return ErrorExecuteResponse("created data source connection error!", e)
    }

    try {
      statement = connection.createStatement()
      statement.setQueryTimeout(JDBCConfiguration.JDBC_QUERY_TIMEOUT.getValue)
      statement.setFetchSize(outputPrintLimit)
      statement.setMaxRows(outputPrintLimit)
      logger.info(s"create statement is:  $statement")
      connectionManager.saveStatement(taskId, statement)
      val isResultSetAvailable = statement.execute(code)
      logger.info(s"Is ResultSet available ? : $isResultSetAvailable")
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
          Utils.tryCatch({ resultSet.close() }) { case e: SQLException => logger.warn(e.getMessage) }
        }
        if (statement != null) {
          Utils.tryCatch({ statement.close() }) { case e: SQLException => logger.warn(e.getMessage) }
        }
      }
    } catch {
      case e: Throwable =>
        logger.error(s"Cannot run $code", e)
        return ErrorExecuteResponse(e.getMessage, e)
    } finally {
      if (connection != null) {
        try {
          if (!connection.getAutoCommit) connection.commit()
          connection.close()
        } catch {
          case e: SQLException => logger.warn("close connection error.", e)
        }
      }
      connectionManager.removeStatement(taskId)
    }
    SuccessExecuteResponse()
  }

  private def getExecResultSetOutput(engineExecutorContext: EngineExecutionContext, statement: Statement, resultSet: ResultSet): ExecuteResponse = {
    if (isDDLCommand(statement.getUpdateCount, resultSet.getMetaData.getColumnCount)) {
      logger.info(s"current result is a ResultSet Object , but there are no more results!")
      engineExecutorContext.appendStdout("Query executed successfully.")
      SuccessExecuteResponse()
    } else {
      val md = resultSet.getMetaData
      val metaArrayBuffer = new ArrayBuffer[(String, String)]()
      for (i <- 1 to md.getColumnCount) {
        metaArrayBuffer.add(Tuple2(md.getColumnName(i), JDBCHelper.getTypeStr(md.getColumnType(i))))
      }
      val columns = metaArrayBuffer.map { c => Column(c._1, DataType.toDataType(c._2), "") }.toArray[Column]
      val metaData = new TableMetaData(columns)
      val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
      resultSetWriter.addMetaData(metaData)
      var count = 0
      Utils.tryCatch({
        while (count < outputPrintLimit && resultSet.next()) {
          val r: Array[Any] = columns.indices.map { i =>
            val data = resultSet.getObject(i + 1) match {
              case value: Array[Byte] => new String(resultSet.getObject(i + 1).asInstanceOf[Array[Byte]])
              case value: Any => resultSet.getString(i + 1)
              case _ => null
            }
            data
          }.toArray
          resultSetWriter.addRecord(new TableRecord(r))
          count += 1
        }
      }) {
        case e: Exception => return ErrorExecuteResponse("query jdbc failed", e)
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
    val userCreatorLabel = engineExecutionContext.getLabels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
    userCreatorLabel.getUser
  }

  protected def isDDLCommand(updatedCount: Int, columnCount: Int): Boolean = {
    updatedCount < 0 && columnCount <= 0
  }

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = Array.empty[JobProgressInfo]

  override protected def callback(): Unit = {}

  override def progress(taskID: String): Float = 0

  override def close(): Unit = {
    logger.info("Start closing the jdbc engine.")
    connectionManager.close()
    if (JDBCConfiguration.JDBC_KERBEROS_ENABLE.getValue) {
      connectionManager.shutdownRefreshKerberosLoginService()
    }
    logger.info("The jdbc engine has closed successfully.")
  }

  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = null

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (!CollectionUtils.isEmpty(labels)) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = null

  override def getCurrentNodeResource(): NodeResource = {
    val properties = EngineConnObject.getEngineCreationContext.getOptions
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith(JDBCEngineConnConstant.JDBC_ENGINE_MEMORY_UNIT)) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, settingClientMemory + JDBCEngineConnConstant.JDBC_ENGINE_MEMORY_UNIT)
      }
    }
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


object JDBCEngineConfig extends RPCMapCache[Array[Label[_]], String, String](Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue) {

  override protected def createRequest(labels: Array[Label[_]]): CacheableProtocol = {
    val userCreatorLabel = labels.find(_.isInstanceOf[UserCreatorLabel]).get.asInstanceOf[UserCreatorLabel]
    val engineTypeLabel = labels.find(_.isInstanceOf[EngineTypeLabel]).get.asInstanceOf[EngineTypeLabel]
    RequestQueryEngineConfig(userCreatorLabel, engineTypeLabel)
  }

  override protected def createMap(any: Any): util.Map[String, String] = any match {
    case response: ResponseQueryConfig => response.getKeyAndValue
  }
}