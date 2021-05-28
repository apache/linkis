/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineconnplugin.flink.executor

import java.io.Closeable
import java.util
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.result.ResultKind
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.sql.operation.{AbstractJobOperation, JobOperation, OperationFactory}
import com.webank.wedatasphere.linkis.engineconnplugin.flink.client.utils.SqlCommandParser
import com.webank.wedatasphere.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import com.webank.wedatasphere.linkis.engineconnplugin.flink.context.FlinkEngineConnContext
import com.webank.wedatasphere.linkis.engineconnplugin.flink.exception.SqlParseException
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.RowsType.RowsType
import com.webank.wedatasphere.linkis.engineconnplugin.flink.listener.{FlinkStatusListener, FlinkStreamingResultSetListener}
import com.webank.wedatasphere.linkis.manager.label.entity.cluster.EnvLabel
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import org.apache.calcite.rel.metadata.{JaninoRelMetadataProvider, RelMetadataQueryBase}
import org.apache.flink.api.common.JobStatus._
import org.apache.flink.table.planner.plan.metadata.FlinkDefaultRelMetadataProvider

import scala.collection.JavaConversions._

/**
  *
  */
class FlinkSQLJobExecutor(id: Long,
                          override protected val flinkEngineConnContext: FlinkEngineConnContext) extends ComputationExecutor with FlinkJobExecutor {

  private var operation: JobOperation = _

  override def executeLine(engineExecutionContext: EngineExecutionContext, code: String): ExecuteResponse = {
    val callOpt = SqlCommandParser.parse(code.trim, true)
    val callSQL = if (!callOpt.isPresent) throw new SqlParseException("Unknown statement: " + code)
      else callOpt.get
    RelMetadataQueryBase.THREAD_PROVIDERS.set(JaninoRelMetadataProvider.of(FlinkDefaultRelMetadataProvider.INSTANCE))
    val operation = OperationFactory.createOperation(callSQL, flinkEngineConnContext)
    operation match {
      case jobOperation: JobOperation =>
        this.operation = jobOperation
        jobOperation.addFlinkListener(new FlinkSQLStatusListener(jobOperation, engineExecutionContext))
        if(getEnvLabel.getEnvType == EnvLabel.DEV && callSQL.command == SqlCommandParser.SqlCommand.SELECT) {
          jobOperation.addFlinkListener(new FlinkSQLStreamingResultSetListener(jobOperation, engineExecutionContext))
          val properties: util.Map[String, String] = engineExecutionContext.getProperties.map {
            case (k, v: String) => (k, v)
            case (k, v) if v != null => (k, v.toString)
            case (k, _) => (k, null)
          }
          jobOperation.addFlinkListener(new DevFlinkSQLStreamingListener(jobOperation, properties))
        }
      case _ =>
    }
    val resultSet = operation.execute
    resultSet.getResultKind match {
      case ResultKind.SUCCESS =>
        new SuccessExecuteResponse
      case ResultKind.SUCCESS_WITH_CONTENT if !operation.isInstanceOf[JobOperation] =>
        FlinkJobExecutor.writeAndSendResultSet(resultSet, engineExecutionContext)
        new SuccessExecuteResponse
      case _ =>
        operation match {
          case jobOperation: AbstractJobOperation =>
            val jobInfo = jobOperation.transformToJobInfo(resultSet)
            setJobID(jobInfo.getJobId.toHexString)
            setApplicationId(jobInfo.getApplicationId)
            setApplicationURL(jobInfo.getWebInterfaceUrl)
            setYarnMode("client")
            jobOperation.getFlinkStatusListeners.get(0) match {
              case listener: FlinkSQLStatusListener => listener.waitForCompleted()
              case _ =>
            }
          case jobOperation: JobOperation =>
            jobOperation.getFlinkListeners.find(_.isInstanceOf[FlinkSQLStatusListener]).foreach { case listener: FlinkSQLStatusListener =>
              listener.waitForCompleted()
            }
        }
    }
    new SuccessExecuteResponse
  }

  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = {
    val newcode = completedLine + code
    info("newcode is " + newcode)
    executeLine(engineExecutorContext, newcode)
  }

  //TODO wait for completed.
  override def progress(): Float = if(operation == null) 0 else operation.getJobStatus match {
    case jobState if jobState.isGloballyTerminalState => 1
    case RUNNING => 0.5f
    case _ => 0
  }

  override def getProgressInfo: Array[JobProgressInfo] = Array.empty

  private var envLabel: EnvLabel = _

  def getEnvLabel: EnvLabel = {
    if(envLabel == null) getExecutorLabels().foreach{
      case l: EnvLabel => envLabel = l
      case _ =>
    }
    envLabel
  }

  override def getId: String = "flinkSQL_"+ id

  override def close(): Unit = {
    if(operation != null) {
      operation.cancelJob()
    }
    flinkEngineConnContext.getExecutionContext.createClusterDescriptor().close()
    flinkEngineConnContext.getExecutionContext.getClusterClientFactory.close()
    super.close()
  }
}

class FlinkSQLStatusListener(jobOperation: JobOperation, engineExecutionContext: EngineExecutionContext) extends FlinkStatusListener {

  private var resp: ExecuteResponse = _
  private val startTime = System.currentTimeMillis

  override def onSuccess(rows: Int, rowsType: RowsType): Unit = {
    engineExecutionContext.appendStdout(s"Time taken: ${ByteTimeUtils.msDurationToString(System.currentTimeMillis - startTime)}, $rowsType $rows row(s).")
    Utils.tryCatch{
      FlinkJobExecutor.writeAndSendResultSet(jobOperation.getJobResult.get(), engineExecutionContext)
      resp = new SuccessExecuteResponse
    }{ e =>
      resp = ErrorExecuteResponse("Fail to run statement",e)
    }
    synchronized(notify())
  }

  override def onFailed(message: String, t: Throwable): Unit = {
    resp = ErrorExecuteResponse(message, t)
    synchronized(notify())
  }

  def getResponse: ExecuteResponse = resp

  def waitForCompleted(maxWaitTime: Long): Unit = synchronized {
    if(maxWaitTime < 0) wait() else wait(maxWaitTime)
  }

  def waitForCompleted(): Unit = waitForCompleted(-1)
}

class FlinkSQLStreamingResultSetListener(jobOperation: JobOperation,
                                         engineExecutionContext: EngineExecutionContext)
  extends FlinkStreamingResultSetListener with Closeable with Logging {

  private val resultSetWriter = engineExecutionContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)

  override def onResultSetPulled(rows: Int): Unit = {
    info(s"$rows resultSets has pulled.")
    FlinkJobExecutor.writeResultSet(jobOperation.getJobResult.get(), resultSetWriter)
  }

  override def close(): Unit = engineExecutionContext.sendResultSet(resultSetWriter)
}

class DevFlinkSQLStreamingListener(jobOperation: JobOperation,
                                   maxWrittenLines: Int,
                                   maxWaitForResultTime: Long) extends FlinkStreamingResultSetListener with Logging {

  def this(jobOperation: JobOperation) =
    this(jobOperation,
      FlinkEnvConfiguration.FLINK_SQL_DEV_SELECT_MAX_LINES.getValue,
      FlinkEnvConfiguration.FLINK_SQL_DEV_RESULT_MAX_WAIT_TIME.getValue.toLong
    )

  def this(jobOperation: JobOperation, properties: util.Map[String, String]) =
    this(jobOperation,
      FlinkEnvConfiguration.FLINK_SQL_DEV_SELECT_MAX_LINES.getValue(properties),
      FlinkEnvConfiguration.FLINK_SQL_DEV_RESULT_MAX_WAIT_TIME.getValue(properties).toLong
    )

  private var lastPulledTime = System.currentTimeMillis
  private var writtenLines = 0

  override def onResultSetPulled(rows: Int): Unit = {
    lastPulledTime = System.currentTimeMillis
    writtenLines += rows
    if(writtenLines >= maxWrittenLines) {
      warn(s"The returned resultSet reached max lines $writtenLines, now kill the job automatic. Notice: only the dev environment will touch off the automatic kill.")
      stopJobOperation()
    }
  }

  private val future = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = if(System.currentTimeMillis - lastPulledTime > maxWaitForResultTime) {
      warn(s"Job killed since reached the max time ${ByteTimeUtils.msDurationToString(maxWaitForResultTime)} of waiting for resultSet. Notice: only the dev environment will touch off the automatic kill.")
      stopJobOperation()
    }
  }, maxWaitForResultTime, maxWaitForResultTime, TimeUnit.MILLISECONDS)

  def stopJobOperation(): Unit = {
    Utils.tryFinally(jobOperation.cancelJob()) {
      jobOperation.getFlinkListeners.foreach {
        case listener: FlinkStreamingResultSetListener with Closeable =>
          listener.close()
      }
      future.cancel(false)
    }
  }
}