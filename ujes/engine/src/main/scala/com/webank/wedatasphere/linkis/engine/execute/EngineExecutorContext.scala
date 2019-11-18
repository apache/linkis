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

package com.webank.wedatasphere.linkis.engine.execute

import java.io.File
import java.util
import java.util.concurrent.atomic.AtomicInteger

import com.webank.wedatasphere.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import com.webank.wedatasphere.linkis.common.io.{FsPath, MetaData, Record}
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.conf.EngineConfiguration.{ENGINE_RESULT_SET_MAX_CACHE, ENGINE_RESULT_SET_STORE_PATH}
import com.webank.wedatasphere.linkis.engine.exception.EngineErrorException
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{AliasOutputExecuteResponse, OutputExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateFormatUtils

import scala.collection.mutable.ArrayBuffer

class EngineExecutorContext(engineExecutor: EngineExecutor) extends Logging{

  private val resultSetFactory = ResultSetFactory.getInstance
  private val resultSetWriters = ArrayBuffer[ResultSetWriter[_ <: MetaData, _ <: Record]]()
  private var defaultResultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record] = _
  private var resultSize = 0
  private var interrupted = false

  private var jobId: Option[String] = None
  private val aliasNum = new AtomicInteger(0)
  protected var storePath: Option[String] = None

  private val properties:java.util.Map[String,Object] = new util.HashMap[String, Object]()

  private var totalParagraph = 0
  private var currentParagraph = 0

  def kill(): Unit = interrupted = true
  def isKilled: Boolean = interrupted

  def getTotalParagraph: Int = totalParagraph
  def setTotalParagraph(totalParagraph: Int): Unit = this.totalParagraph = totalParagraph
  def getCurrentParagraph: Int = currentParagraph
  def setCurrentParagraph(currentParagraph: Int): Unit = this.currentParagraph = currentParagraph

  def pushProgress(progress: Float, progressInfo: Array[JobProgressInfo]): Unit =
    engineExecutor.getJobProgressListener.foreach(l => jobId.foreach(l.onProgressUpdate(_, progress, progressInfo)))

  def sendResultSet(resultSetWriter: ResultSetWriter[_ <: MetaData, _ <: Record]): Unit = {
    val fileName = new File(resultSetWriter.toFSPath.getPath).getName
    val index = if(fileName.indexOf(".") < 0) fileName.length else fileName.indexOf(".")
    val alias = if(fileName.startsWith("_")) fileName.substring(1, index) else fileName.substring(0, fileName.indexOf("_"))
//    resultSetWriter.flush()
    Utils.tryFinally(sendResultSet(resultSetWriter.toString(), alias)){
      IOUtils.closeQuietly(resultSetWriter)
      resultSetWriters synchronized resultSetWriters -= resultSetWriter
    }
  }

  def sendResultSet(output: String): Unit = sendResultSet(output, "_" + aliasNum.getAndIncrement())

  def appendTextResultSet(output: String): Unit = {
    if(defaultResultSetWriter == null) aliasNum synchronized {
      if(defaultResultSetWriter == null) {
        defaultResultSetWriter = createDefaultResultSetWriter(ResultSetFactory.TEXT_TYPE)
        defaultResultSetWriter.addMetaData(new LineMetaData())
        resultSetWriters += defaultResultSetWriter
      }
    }
    defaultResultSetWriter.addRecord(new LineRecord(output))
  }

  private def sendResultSet(output: String, alias: String): Unit = {
    if (StringUtils.isEmpty(output)) return
    if (resultSetFactory.isResultSetPath(output))
      engineExecutor.getResultSetListener.foreach{ l =>
        jobId.foreach(l.onResultSetCreated(_, output, alias))
        resultSize += 1
      }
    else if (resultSetFactory.isResultSet(output))
      engineExecutor.getResultSetListener.foreach{ l =>
        jobId.foreach(l.onResultSetCreated(_, output, alias))
        resultSize += 1
      }
    else throw new EngineErrorException(50050, "unknown resultSet: " + output)
  }

  def setJobId(jobId: String) = this.jobId = Option(jobId)
  def getJobId = jobId
  def setStorePath(storePath: String) = this.storePath = Option(storePath)

  def sendResultSet(outputExecuteResponse: OutputExecuteResponse): Unit = outputExecuteResponse match {
    case AliasOutputExecuteResponse(alias, output) => sendResultSet(output, alias)
    case output: OutputExecuteResponse => sendResultSet(output.getOutput, "_" + aliasNum.getAndIncrement())
  }

  def getProperties:java.util.Map[String, Object] = properties

  def addProperty(key:String, value:String):Unit = properties.put(key, value)

  protected def getDefaultStorePath: String = {
    val path = ENGINE_RESULT_SET_STORE_PATH.getValue
    (if(path.endsWith("/")) path else path + "/") + "user" + "/" +
      DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd") + "/" + Sender.getThisServiceInstance.getApplicationName +
      "/" + System.nanoTime
  }

  def createDefaultResultSetWriter(): ResultSetWriter[_ <: MetaData, _ <: Record] = createResultSetWriter(resultSetFactory.getResultSetByType(engineExecutor.getDefaultResultSetType))

  def createDefaultResultSetWriter(alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSetFactory.getResultSetByType(engineExecutor.getDefaultResultSetType), alias)

  def createResultSetWriter(resultSetType: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSetFactory.getResultSetByType(resultSetType), null)

  def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record]): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSet, null)

  def createResultSetWriter(resultSetType: String, alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] =
    createResultSetWriter(resultSetFactory.getResultSetByType(resultSetType), alias)

  def createResultSetWriter(resultSet: ResultSet[_ <: MetaData, _ <: Record], alias: String): ResultSetWriter[_ <: MetaData, _ <: Record] = {
    val filePath = storePath.getOrElse(getDefaultStorePath)
    val fileName = if(StringUtils.isEmpty(alias)) "_" + aliasNum.getAndIncrement() else alias + "_" + aliasNum.getAndIncrement()
    val resultSetPath = resultSet.getResultSetPath(new FsPath(filePath), fileName)
    val resultSetWriter = ResultSetWriter.getResultSetWriter(resultSet, ENGINE_RESULT_SET_MAX_CACHE.getValue.toLong, resultSetPath)
    resultSetWriters synchronized resultSetWriters += resultSetWriter
    resultSetWriter
  }

  def appendStdout(log: String): Unit = if(!engineExecutor.isEngineInitialized)
    engineExecutor.info(log) else engineExecutor.getLogListener.foreach(ll => jobId.foreach(ll.onLogUpdate(_, log)))

  def close(): Unit = {
    resultSetWriters.toArray.foreach(sendResultSet)
    engineExecutor.getResultSetListener.foreach(l => jobId.foreach(l.onResultSizeCreated(_, resultSize)))
    resultSetWriters.clear()
  }

}
