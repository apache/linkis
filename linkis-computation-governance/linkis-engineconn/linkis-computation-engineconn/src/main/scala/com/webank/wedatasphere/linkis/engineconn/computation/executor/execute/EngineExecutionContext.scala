/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconn.computation.executor.execute

import java.io.File
import java.util
import java.util.concurrent.atomic.AtomicInteger

import com.webank.wedatasphere.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.cs.client.utils.ContextServiceUtils
import com.webank.wedatasphere.linkis.cs.storage.CSTableResultSetWriter
import com.webank.wedatasphere.linkis.engineconn.acessible.executor.listener.event.{TaskLogUpdateEvent, TaskProgressUpdateEvent, TaskResultCreateEvent, TaskResultSizeCreatedEvent}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import com.webank.wedatasphere.linkis.engineconn.executor.listener.{EngineConnAsyncListenerBus, EngineConnSyncListenerBus, ExecutorListenerBusContext}
import com.webank.wedatasphere.linkis.governance.common.exception.engineconn.EngineConnExecutorErrorException
import com.webank.wedatasphere.linkis.manager.label.entity.Label
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, OutputExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.table.TableResultSet
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateFormatUtils

import scala.collection.mutable.ArrayBuffer


class EngineExecutionContext(executor: ComputationExecutor, executorUser: String = Utils.getJvmUser) extends Logging {

  private val resultSetFactory = ResultSetFactory.getInstance

  private val resultSetWriters = ArrayBuffer[ResultSetWriter[_ <: MetaData, _ <: Record]]()

  private var defaultResultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record] = _

  private var resultSize = 0

  private var interrupted = false

  private var jobId: Option[String] = None
  private val aliasNum = new AtomicInteger(0)
  protected var storePath: Option[String] = None

  private val properties: java.util.Map[String, Object] = new util.HashMap[String, Object]()

  private var labels: Array[Label[_]] = new Array[Label[_]](0)

  private var totalParagraph = 0
  private var currentParagraph = 0

  def kill(): Unit = interrupted = true

  def isKilled: Boolean = interrupted

  def getTotalParagraph: Int = totalParagraph

  def setTotalParagraph(totalParagraph: Int): Unit = this.totalParagraph = totalParagraph

  def getCurrentParagraph: Int = currentParagraph

  def setCurrentParagraph(currentParagraph: Int): Unit = this.currentParagraph = currentParagraph

  def pushProgress(progress: Float, progressInfo: Array[JobProgressInfo]): Unit = {
    val listenerBus = getEngineSyncListenerBus
    jobId.foreach(jId => {
      listenerBus.postToAll(TaskProgressUpdateEvent(jId, progress, progressInfo))
    })
  }

  def sendResultSet(resultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record]): Unit = {
    info("Start to send res to entrance")
    val fileName = new File(resultSetWriter.toFSPath.getPath).getName
    val index = if (fileName.indexOf(".") < 0) fileName.length else fileName.indexOf(".")
    val alias = if (fileName.startsWith("_")) fileName.substring(1, index) else fileName.substring(0, fileName.indexOf("_"))
    Utils.tryFinally(sendResultSet(resultSetWriter.toString(), alias)) {
      IOUtils.closeQuietly(resultSetWriter)
      resultSetWriters synchronized resultSetWriters -= resultSetWriter
    }
  }

  def sendResultSet(output: String): Unit = sendResultSet(output, "_" + aliasNum.getAndIncrement())

  def appendTextResultSet(output: String): Unit = {
    if (defaultResultSetWriter == null) aliasNum synchronized {
      if (defaultResultSetWriter == null) {
        defaultResultSetWriter = createDefaultResultSetWriter(ResultSetFactory.TEXT_TYPE)
        defaultResultSetWriter.addMetaData(new LineMetaData())
        resultSetWriters += defaultResultSetWriter
      }
    }
    defaultResultSetWriter.addRecord(new LineRecord(output))
  }

  private def sendResultSet(output: String, alias: String): Unit = {
    if (StringUtils.isEmpty(output)) return
    if (resultSetFactory.isResultSetPath(output) || resultSetFactory.isResultSet(output)) {
      val listenerBus = getEngineSyncListenerBus
      jobId.foreach(jId => {
        //TODO peacewong executor.getEngineServerContext().getEngineAsyncListenerBus().post(ResultSetCreatedEvent(jId, output, alias))
        listenerBus.postToAll(TaskResultCreateEvent(jId, output, alias))
        resultSize += 1
      })
    } else throw new EngineConnExecutorErrorException(50050, "unknown resultSet: " + output)
  }

  def setJobId(jobId: String) = this.jobId = Option(jobId)

  def getJobId = jobId

  def setStorePath(storePath: String) = this.storePath = Option(storePath)

  def getLables: Array[Label[_]] = labels

  def setLabels(labels: Array[Label[_]]): Unit = this.labels = labels

  def sendResultSet(outputExecuteResponse: OutputExecuteResponse): Unit = outputExecuteResponse match {
    case AliasOutputExecuteResponse(alias, output) => sendResultSet(output, alias)
    case output: OutputExecuteResponse => sendResultSet(output.getOutput, "_" + aliasNum.getAndIncrement())
  }

  def getProperties: java.util.Map[String, Object] = properties

  def addProperty(key: String, value: String): Unit = properties.put(key, value)

  protected def getDefaultStorePath: String = {
    val path = ComputationExecutorConf.ENGINECONN_RESULT_SET_STORE_PATH.getValue
    (if (path.endsWith("/")) path else path + "/") + Utils.getJvmUser + "/" +
      DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd") + "/" + getJobId.get + "/" + System.nanoTime
  }

  def createDefaultResultSetWriter(): ResultSetWriter[_ <: MetaData, _ <: Record] = {
    //    createResultSetWriter(resultSetFactory.getResultSetByType(engine.getDefaultResultSetType))
    createResultSetWriter(resultSetFactory.getResultSetByType(resultSetFactory.getResultSetType(0))) // todo check
  }

  def createDefaultResultSetWriter(alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSetFactory.getResultSetByType(resultSetFactory.getResultSetType(0)), alias) // todo check

  def createResultSetWriter(resultSetType: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSetFactory.getResultSetByType(resultSetType), null)

  def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record]): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSet, null)

  def createResultSetWriter(resultSetType: String, alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSetFactory.getResultSetByType(resultSetType), alias)

  def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record], alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] = {
    val filePath = storePath.getOrElse(getDefaultStorePath)
    val fileName = if (StringUtils.isEmpty(alias)) "_" + aliasNum.getAndIncrement() else alias + "_" + aliasNum.getAndIncrement()
    val resultSetPath = resultSet.getResultSetPath(new FsPath(filePath), fileName)
    //update by peaceWong 20200402
    val resultSetWriter = resultSet match {
      case result: TableResultSet =>
        val contextIDStr = ContextServiceUtils.getContextIDStrByMap(getProperties)
        val nodeName = ContextServiceUtils.getNodeNameStrByMap(getProperties)
        if (StringUtils.isNotBlank(contextIDStr) && StringUtils.isNotBlank(nodeName)) {
          val csWriter = new CSTableResultSetWriter(result, ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, contextIDStr, nodeName, alias)
          csWriter.setProxyUser(executorUser)
          csWriter
        } else {
          ResultSetWriter.getResultSetWriter(resultSet, ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, executorUser)
        }
      case _ => ResultSetWriter.getResultSetWriter(resultSet, ComputationExecutorConf.ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath, executorUser)
    }
    //update by peaceWong 20200402 end
    resultSetWriters synchronized resultSetWriters += resultSetWriter
    resultSetWriter
  }

  def appendStdout(log: String): Unit = if (!executor.isEngineInitialized) {
    executor.info(log)
  } else {
    val listenerBus = getEngineSyncListenerBus
    // jobId.foreach(jId => listenerBus.post(TaskLogUpdateEvent(jId, log)))
    jobId.foreach(jId => listenerBus.postToAll(TaskLogUpdateEvent(jId, log)))
  }

  def sendProgress(progress: Float, progressInfos: Array[JobProgressInfo]): Unit = {
    if (executor.isEngineInitialized) {
      val listenerBus = getEngineSyncListenerBus
      // jobId.foreach(jId => listenerBus.post(TaskProgressUpdateEvent(jId, progress, progressInfos)))
      jobId.foreach(jId => listenerBus.postToAll(TaskProgressUpdateEvent(jId, progress, progressInfos)))
    }
  }

  def close(): Unit = {
    resultSetWriters.toArray.foreach(sendResultSet)
    val listenerBus = getEngineSyncListenerBus
    jobId.foreach(jId => {
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
}
