/*
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
package com.webank.wedatasphere.linkis.manager.engineplugin.jdbc.executer

import java.sql.{Connection, Statement}
import java.util

import com.webank.wedatasphere.linkis.common.utils.{OverloadUtils, Utils}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.{ConcurrentComputationExecutor, EngineExecutionContext}
import com.webank.wedatasphere.linkis.engineconn.core.EngineConnObject
import com.webank.wedatasphere.linkis.manager.common.entity.resource.{CommonNodeResource, LoadResource, NodeResource}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import com.webank.wedatasphere.linkis.manager.engineplugin.jdbc.ConnectionManager
import com.webank.wedatasphere.linkis.manager.engineplugin.jdbc.conf.JDBCConfiguration
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.domain.{Column, DataType}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.springframework.util.CollectionUtils

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class JDBCEngineConnExecutor(override val outputPrintLimit: Int, val id: Int) extends ConcurrentComputationExecutor(outputPrintLimit) {


  private val connectionManager = ConnectionManager.getInstance()

  private var statement: Statement = null

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)
  private var connection: Connection = null

  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val realCode = code.trim()
    val properties = engineExecutorContext.getProperties.asInstanceOf[util.Map[String, String]]
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

  override def getProgressInfo: Array[JobProgressInfo] = Array.empty[JobProgressInfo]

  override protected def callback(): Unit = {}

  override def progress(): Float = {
    0
  }

  override def close(): Unit = {
    if (statement != null) {
      statement.close()
    }
    if (connection != null) {
      connection.close()
    }
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

