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

package org.apache.linkis.engineplugin.spark.executor

import org.apache.linkis.common.exception.FatalException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.computation.executor.utlis.ProgressUtils
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineplugin.spark.datacalc.{DataCalcExecution, DataCalcTempData}
import org.apache.linkis.engineplugin.spark.datacalc.model.{DataCalcArrayData, DataCalcGroupData}
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.extension.SparkPostExecutionHook
import org.apache.linkis.engineplugin.spark.utils.{EngineUtils, JobProgressUtil}
import org.apache.linkis.governance.common.exception.LinkisJobRetryException
import org.apache.linkis.governance.common.paser.EmptyCodeParser
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{
  CompletedExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}

import org.apache.arrow.memory.OutOfMemoryException
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.orc.storage.common.io.Allocator.AllocatorOutOfMemoryException

import java.lang.reflect.InvocationTargetException
import java.util
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class SparkDataCalcExecutor(sparkEngineSession: SparkEngineSession, id: Long)
    extends ComputationExecutor
    with Logging {

  private var jobGroup: String = _

  val queryNum = new AtomicLong(0)

  private var engineExecutionContext: EngineExecutionContext = _

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private var thread: Thread = _

  override def init(): Unit = {
    setCodeParser(new EmptyCodeParser)
    logger.info(s"Ready to change engine state!")
    super.init()
    logger.info("spark data-calc executor start")
  }

  override def executeLine(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = Utils.tryFinally {
    val sc = sparkEngineSession.sparkContext
    this.engineExecutionContext = engineExecutionContext
    thread = Thread.currentThread()
    if (sc.isStopped) {
      logger.error("Spark application has already stopped, please restart it.")
      transition(NodeStatus.Failed)
      throw new LinkisJobRetryException(
        "Spark application sc has already stopped, please restart it."
      )
    }
    engineExecutionContext.appendStdout(
      LogUtils.generateInfo(s"yarn application id: ${sc.applicationId}")
    )
    logger.info(s"Ready to run code with kind spark data-calc.")
    jobGroup = String.valueOf("linkis-spark-data_calc-code-" + queryNum.incrementAndGet())
    logger.info("Set jobGroup to " + jobGroup)
    sc.setJobGroup(jobGroup, code, true)

    val execType =
      engineExecutionContext.getProperties.getOrDefault("exec-type", "array").toString
    val response = Utils.tryFinally(runCode(code, execType, jobGroup)) {
      jobGroup = null
      sc.clearJobGroup()
    }
    Utils.tryQuietly(
      SparkPostExecutionHook
        .getSparkPostExecutionHooks()
        .foreach(_.callPostExecutionHook(engineExecutionContext, response, code))
    )
    response
  } {
    this.engineExecutionContext = null
  }

  def runCode(code: String, execType: String, jobGroup: String): ExecuteResponse = {
    logger.info("DataCalcExecutor run query: " + code)
    engineExecutionContext.appendStdout(s"${EngineUtils.getName} >> $code")
    Utils.tryCatch {
      if ("group" == execType) {
        val (sources, transformations, sinks) =
          DataCalcExecution.getPlugins(DataCalcGroupData.getData(code))
        DataCalcExecution.execute(sparkEngineSession.sparkSession, sources, transformations, sinks)
      } else {
        val plugins = DataCalcExecution.getPlugins(DataCalcArrayData.getData(code))
        DataCalcExecution.execute(sparkEngineSession.sparkSession, plugins)
      }
      SuccessExecuteResponse().asInstanceOf[CompletedExecuteResponse]
    } {
      case e: InvocationTargetException =>
        logger.error("execute sparkDataCalc has InvocationTargetException!", e)
        var cause = ExceptionUtils.getCause(e)
        if (cause == null) cause = e
        ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(e), cause)
      case e: OutOfMemoryException =>
        getErrorResponse(e, true)
      case e: AllocatorOutOfMemoryException =>
        getErrorResponse(e, true)
      case e: FatalException =>
        getErrorResponse(e, true)
      case e: Exception =>
        getErrorResponse(e, false)
      case err: OutOfMemoryError =>
        getErrorResponse(err, true)
      case err: VirtualMachineError =>
        getErrorResponse(err, true)
      case err: Error =>
        getErrorResponse(err, false)
    }
  }

  def getErrorResponse(throwable: Throwable, needToStopEC: Boolean): ErrorExecuteResponse = {
    if (needToStopEC) {
      logger.error(
        s"execute sparkSQL has ${throwable.getClass.getName} now to set status to shutdown!",
        throwable
      )
      ExecutorManager.getInstance.getReportExecutor.tryShutdown()
    } else {
      logger.error(s"execute sparkSQL has ${throwable.getClass.getName}!", throwable)
    }
    ErrorExecuteResponse(ExceptionUtils.getRootCauseMessage(throwable), throwable)
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = {
    val newCode = completedLine + code
    logger.info("newCode is " + newCode)
    executeLine(engineExecutorContext, newCode)
  }

  override def progress(taskID: String): Float =
    if (jobGroup == null || engineExecutionContext.getTotalParagraph == 0) {
      ProgressUtils.getOldProgress(this.engineExecutionContext)
    } else {
      val sc = sparkEngineSession.sparkContext
      val newProgress =
        (engineExecutionContext.getCurrentParagraph * 1f - 1f) / engineExecutionContext.getTotalParagraph +
          JobProgressUtil.progress(sc, jobGroup) / engineExecutionContext.getTotalParagraph
      val normalizedProgress = if (newProgress >= 1) newProgress - 0.1f else newProgress
      val oldProgress = ProgressUtils.getOldProgress(this.engineExecutionContext)
      if (normalizedProgress < oldProgress) oldProgress
      else {
        ProgressUtils.putProgress(normalizedProgress, this.engineExecutionContext)
        normalizedProgress
      }
    }

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = if (jobGroup == null) {
    Array.empty
  } else {
    logger.debug("request new progress info for jobGroup is " + jobGroup)
    val progressInfoArray = ArrayBuffer[JobProgressInfo]()
    val sc = sparkEngineSession.sparkContext
    progressInfoArray ++= JobProgressUtil.getActiveJobProgressInfo(sc, jobGroup)
    progressInfoArray ++= JobProgressUtil.getCompletedJobProgressInfo(sc, jobGroup)
    progressInfoArray.toArray
  }

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = this.executorLabels = labels

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = {
    // todo check
    null
  }

  override def getCurrentNodeResource(): NodeResource = {
    val sc = sparkEngineSession.sparkContext
    logger.info("Begin to get actual used resources!")
    Utils.tryCatch({
      val executorNum: Int = sc.getConf.get("spark.executor.instances").toInt
      val executorMem: Long =
        ByteTimeUtils.byteStringAsBytes(sc.getConf.get("spark.executor.memory")) * executorNum
      val driverMem: Long = ByteTimeUtils.byteStringAsBytes(sc.getConf.get("spark.driver.memory"))
      val sparkExecutorCores = sc.getConf.get("spark.executor.cores", "2").toInt * executorNum
      val sparkDriverCores = sc.getConf.get("spark.driver.cores", "1").toInt
      val queue = sc.getConf.get("spark.yarn.queue")
      logger.info(
        s"Current actual used resources is driverMem:$driverMem,driverCores:$sparkDriverCores,executorMem:$executorMem,executorCores:$sparkExecutorCores,queue:$queue"
      )
      val usedResource = new DriverAndYarnResource(
        new LoadInstanceResource(driverMem, sparkDriverCores, 1),
        new YarnResource(executorMem, sparkExecutorCores, 0, queue, sc.applicationId)
      )
      val nodeResource = new CommonNodeResource
      nodeResource.setUsedResource(usedResource)
      nodeResource
    })(t => {
      logger.warn("Get actual used resource exception", t)
      null
    })
  }

  override def supportCallBackLogs(): Boolean = {
    // todo
    true
  }

  override def getId(): String = "SparkDataCalcExecutor_" + id

  override def killTask(taskID: String): Unit = {
    val sc = sparkEngineSession.sparkContext
    if (!sc.isStopped) {
      sc.cancelAllJobs
      if (null != thread) {
        Utils.tryAndWarn(thread.interrupt())
      }
      killRunningTask()
    }
    super.killTask(taskID)
  }

  protected def killRunningTask(): Unit = {
    var runType: String = ""
    getExecutorLabels().asScala.foreach {
      case label: CodeLanguageLabel =>
        runType = label.getCodeType
      case _ =>
    }
    logger.warn(s"Kill running job of ${runType} .")
  }

  override def close(): Unit = {
    super.close()
  }

}
