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

package org.apache.linkis.engineconn.computation.executor.execute

import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.cs.client.utils.ContextServiceUtils
import org.apache.linkis.engineconn.acessible.executor.listener.event.{
  TaskLogUpdateEvent,
  TaskProgressUpdateEvent,
  TaskResultCreateEvent,
  TaskResultSizeCreatedEvent
}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.cs.CSTableResultSetWriter
import org.apache.linkis.engineconn.executor.ExecutorExecutionContext
import org.apache.linkis.engineconn.executor.entity.Executor
import org.apache.linkis.engineconn.executor.listener.{
  EngineConnAsyncListenerBus,
  EngineConnSyncListenerBus,
  ExecutorListenerBusContext
}
import org.apache.linkis.governance.common.exception.engineconn.EngineConnExecutorErrorException
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{AliasOutputExecuteResponse, OutputExecuteResponse}
import org.apache.linkis.storage.{LineMetaData, LineRecord}
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetWriterFactory}
import org.apache.linkis.storage.resultset.table.TableResultSet

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.File
import java.util

class EngineExecutionContext(executor: ComputationExecutor, executorUser: String = Utils.getJvmUser)
    extends ExecutorExecutionContext
    with Logging {

  private val resultSetFactory = ResultSetFactory.getInstance

  private var defaultResultSetWriter
      : org.apache.linkis.common.io.resultset.ResultSetWriter[_ <: MetaData, _ <: Record] = _

  private var resultSize = 0

  private var enableResultsetMetaWithTableName =
    ComputationExecutorConf.HIVE_RESULTSET_USE_TABLE_NAME.getValue

  private val properties: java.util.Map[String, Object] = new util.HashMap[String, Object]()

  private var totalParagraph = 0
  private var currentParagraph = 0

  def getTotalParagraph: Int = totalParagraph

  def setTotalParagraph(totalParagraph: Int): Unit = this.totalParagraph = totalParagraph

  def getCurrentParagraph: Int = currentParagraph

  def setCurrentParagraph(currentParagraph: Int): Unit = this.currentParagraph = currentParagraph

  def pushProgress(progress: Float, progressInfo: Array[JobProgressInfo]): Unit =
    if (!executor.isInternalExecute) {
      val listenerBus = getEngineSyncListenerBus
      getJobId.foreach(jId => {
        listenerBus.postToAll(TaskProgressUpdateEvent(jId, progress, progressInfo))
      })
    }

  /**
   * Note: the writer will be closed at the end of the method
   * @param resultSetWriter
   */
  def sendResultSet(
      resultSetWriter: org.apache.linkis.common.io.resultset.ResultSetWriter[
        _ <: MetaData,
        _ <: Record
      ]
  ): Unit = {
    logger.info("Start to send res to entrance")
    val fileName = new File(resultSetWriter.toFSPath.getPath).getName
    val index = if (fileName.indexOf(".") < 0) fileName.length else fileName.indexOf(".")
    val alias =
      if (fileName.startsWith("_")) fileName.substring(1, index)
      else fileName.substring(0, fileName.indexOf("_"))
    // resultSetWriter.flush()
    Utils.tryFinally(sendResultSet(resultSetWriter.toString(), alias)) {
      IOUtils.closeQuietly(resultSetWriter)
      resultSetWriters synchronized resultSetWriters -= resultSetWriter
    }
  }

  def sendResultSet(output: String): Unit =
    sendResultSet(output, "_" + aliasNum.getAndIncrement())

  def appendTextResultSet(output: String): Unit = {
    if (defaultResultSetWriter == null) aliasNum synchronized {
      if (defaultResultSetWriter == null) {
        defaultResultSetWriter = createDefaultResultSetWriter(ResultSetFactory.TEXT_TYPE)
        defaultResultSetWriter.addMetaData(new LineMetaData())
      }
    }
    defaultResultSetWriter.addRecord(new LineRecord(output))
  }

  private def sendResultSet(output: String, alias: String): Unit = {
    if (StringUtils.isEmpty(output)) return
    if (resultSetFactory.isResultSetPath(output) || resultSetFactory.isResultSet(output)) {
      val listenerBus = getEngineSyncListenerBus
      getJobId.foreach(jId => {
        // TODO executor.getEngineServerContext().getEngineAsyncListenerBus().post(ResultSetCreatedEvent(jId, output, alias))
        listenerBus.postToAll(TaskResultCreateEvent(jId, output, alias))
        resultSize += 1
      })
    } else throw new EngineConnExecutorErrorException(50050, "unknown resultSet: " + output)
  }

  def sendResultSet(outputExecuteResponse: OutputExecuteResponse): Unit =
    outputExecuteResponse match {
      case AliasOutputExecuteResponse(alias, output) => sendResultSet(output, alias)
      case output: OutputExecuteResponse =>
        sendResultSet(output.getOutput, "_" + aliasNum.getAndIncrement())
    }

  def getProperties: java.util.Map[String, Object] = properties

  def addProperty(key: String, value: String): Unit = properties.put(key, value)

  override protected def getResultSetByType(
      resultSetType: String
  ): ResultSet[_ <: MetaData, _ <: Record] =
    resultSetFactory.getResultSetByType(resultSetType)

  override protected def getDefaultResultSetByType: String = resultSetFactory.getResultSetType()(0)

  def newResultSetWriter(
      resultSet: ResultSet[_ <: MetaData, _ <: Record],
      resultSetPath: FsPath,
      alias: String
  ): org.apache.linkis.common.io.resultset.ResultSetWriter[_ <: MetaData, _ <: Record] = {
    // update by 20200402
    resultSet match {
      case result: TableResultSet =>
        val contextIDStr = ContextServiceUtils.getContextIDStrByMap(getProperties)
        val nodeName = ContextServiceUtils.getNodeNameStrByMap(getProperties)
        if (StringUtils.isNotBlank(contextIDStr) && StringUtils.isNotBlank(nodeName)) {
          val csWriter = new CSTableResultSetWriter(
            result,
            ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong,
            resultSetPath,
            contextIDStr,
            nodeName,
            alias
          )
          csWriter.setProxyUser(executorUser)
          csWriter
        } else {
          ResultSetWriterFactory.getResultSetWriter(
            resultSet,
            ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong,
            resultSetPath,
            executorUser
          )
        }
      case _ =>
        ResultSetWriterFactory.getResultSetWriter(
          resultSet,
          ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong,
          resultSetPath,
          executorUser
        )
    }
    // update by 20200402 end
  }

  def appendStdout(log: String): Unit = if (executor.isInternalExecute) {
    logger.info(log)
  } else {
    val listenerBus = getEngineSyncListenerBus
    getJobId.foreach(jId => listenerBus.postToAll(TaskLogUpdateEvent(jId, log)))
  }

  override def close(): Unit = {
    resultSetWriters.toArray.foreach(sendResultSet)
    val listenerBus = getEngineSyncListenerBus
    getJobId.foreach(jId => {
      listenerBus.postToAll(TaskResultSizeCreatedEvent(jId, resultSize))
    })
    resultSetWriters.clear()
  }

  private def getEngineAsyncListenerBus: EngineConnAsyncListenerBus = {
    ExecutorListenerBusContext.getExecutorListenerBusContext.getEngineConnAsyncListenerBus
  }

  private def getEngineSyncListenerBus: EngineConnSyncListenerBus = {
    ExecutorListenerBusContext.getExecutorListenerBusContext().getEngineConnSyncListenerBus
  }

  def getExecutor: Executor = executor

  def getEnableResultsetMetaWithTableName: Boolean = enableResultsetMetaWithTableName

  def setEnableResultsetMetaWithTableName(withTableName: Boolean): Unit =
    this.enableResultsetMetaWithTableName = withTableName

}
