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

import java.sql.{Connection, Statement}
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
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.governance.common.protocol.conf.{RequestQueryEngineConfig, ResponseQueryConfig}
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.protocol.CacheableProtocol
import org.springframework.util.CollectionUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class JDBCEngineConnExecutor(override val outputPrintLimit: Int, val id: Int) extends ConcurrentComputationExecutor(outputPrintLimit) {


  private val connectionManager = ConnectionManager.getInstance()

  private var statement: Statement = null

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)
  private var connection: Connection = null

  override def init(): Unit = {
    super.init()
    connectionManager.startRefreshKerberosLoginStatusThread()
  }

  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val realCode = code.trim()
    val properties = engineExecutorContext.getProperties.asInstanceOf[util.Map[String, String]]

    if (properties.get("jdbc.url") == null) {
      info(s"jdbc url is empty, adding now...")
      val globalConfig = Utils.tryAndWarn(JDBCEngineConfig.getCacheMap(engineExecutorContext.getLabels))
      properties.put("jdbc.url", globalConfig.get("wds.linkis.jdbc.connect.url"))
      properties.put("jdbc.username", globalConfig.get("wds.linkis.jdbc.username"))
      properties.put("jdbc.password", globalConfig.get("wds.linkis.jdbc.password"))
      properties.put("jdbc.auth.type", globalConfig.get("wds.linkis.jdbc.auth.type"))
      properties.put("jdbc.principal", globalConfig.get("wds.linkis.jdbc.principal"))
      properties.put("jdbc.keytab.location", globalConfig.get("wds.linkis.jdbc.keytab.location"))
      properties.put("jdbc.proxy.user.property", globalConfig.getOrDefault("wds.linkis.jdbc.proxy.user.property", ""))
      properties.put("jdbc.proxy.user", globalConfig.getOrDefault("wds.linkis.jdbc.proxy.user", EngineConnObject.getEngineCreationContext.getUser))
    }

    info(s"jdbc client begins to run jdbc code:\n ${realCode.trim}")
    connection = connectionManager.getConnection(properties)
    statement = connection.createStatement()
    info(s"create statement is:  $statement")
    val isResultSetAvailable = statement.execute(code)
    info(s"Is ResultSet available ? : $isResultSetAvailable")
    if (isResultSetAvailable) {
      info("ResultSet is available")
      val JDBCResultSet = statement.getResultSet
      if (isDDLCommand(statement.getUpdateCount(), JDBCResultSet.getMetaData().getColumnCount)) {
        info(s"current result is a ResultSet Object , but there are no more results :${code} ")
        Utils.tryQuietly {
          JDBCResultSet.close()
          statement.close()
          connection.close()
        }
        SuccessExecuteResponse()
      } else {
        val md = JDBCResultSet.getMetaData
        val metaArrayBuffer = new ArrayBuffer[Tuple2[String, String]]()
        for (i <- 1 to md.getColumnCount) {
          metaArrayBuffer.add(Tuple2(md.getColumnName(i), JDBCHelper.getTypeStr(md.getColumnType(i))))
        }
        val columns = metaArrayBuffer.map { c => Column(c._1, DataType.toDataType(c._2), "") }.toArray[Column]
        val metaData = new TableMetaData(columns)
        val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
        resultSetWriter.addMetaData(metaData)
        var count = 0
        Utils.tryCatch({
          while (count < outputPrintLimit && JDBCResultSet.next()) {
            val r: Array[Any] = columns.indices.map { i =>
              val data = JDBCResultSet.getObject(i + 1) match {
                case value: Any => value.toString
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
          JDBCResultSet.close()
          statement.close()
          connection.close()
          IOUtils.closeQuietly(resultSetWriter)
        }
        info("sql execute completed")
        AliasOutputExecuteResponse(null, output)
      }
    } else {
      info(s"only return affect rows : ${statement.getUpdateCount}")
      Utils.tryQuietly {
        statement.close()
        connection.close()
      }
      SuccessExecuteResponse()
    }
  }

  protected def isDDLCommand(updatedCount: Int, columnCount: Int): Boolean = {
    if (updatedCount < 0 && columnCount <= 0) {
      true
    } else {
      false
    }
  }

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = Array.empty[JobProgressInfo]

  override protected def callback(): Unit = {}

  override def progress(taskID: String): Float = {
    0
  }

  override def close(): Unit = {
    if (statement != null) {
      statement.close()
    }
    if (connection != null) {
      connection.close()
    }
    connectionManager.shutdownRefreshKerberosLoginService()
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
      if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, settingClientMemory + "g")
      }
    }
    val resource = new CommonNodeResource
    val usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
    resource.setUsedResource(usedResource)
    resource
  }

  override def supportCallBackLogs(): Boolean = false

  override def getId(): String = Sender.getThisServiceInstance.getInstance + s"_$id"

  override def getConcurrentLimit: Int = JDBCConfiguration.JDBC_CONCURRENT_LIMIT.getValue

  override def killAll(): Unit = {

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