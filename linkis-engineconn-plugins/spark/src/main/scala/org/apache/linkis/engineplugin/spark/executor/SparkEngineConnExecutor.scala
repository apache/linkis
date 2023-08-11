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

import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.computation.executor.utlis.ProgressUtils
import org.apache.linkis.engineconn.core.exception.ExecutorHookFatalException
import org.apache.linkis.engineconn.executor.entity.{ResourceFetchExecutor, YarnExecutor}
import org.apache.linkis.engineplugin.spark.common.{Kind, SparkDataCalc}
import org.apache.linkis.engineplugin.spark.cs.CSSparkHelper
import org.apache.linkis.engineplugin.spark.extension.{
  SparkPostExecutionHook,
  SparkPreExecutionHook
}
import org.apache.linkis.engineplugin.spark.utils.JobProgressUtil
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.exception.LinkisJobRetryException
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.ExecuteResponse

import org.apache.commons.lang3.StringUtils
import org.apache.spark.SparkContext

import java.util
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

abstract class SparkEngineConnExecutor(val sc: SparkContext, id: Long)
    extends ComputationExecutor
    with Logging
    with YarnExecutor
    with ResourceFetchExecutor {

  private var initialized: Boolean = false

  private var jobGroup: String = _

  val queryNum = new AtomicLong(0)

  private var engineExecutionContext: EngineExecutionContext = _

  private var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private var thread: Thread = _

  private var applicationId: String = sc.applicationId

  override def getApplicationId: String = applicationId

  override def getApplicationURL: String = ""
  override def getYarnMode: String = ""
  override def getQueue: String = ""

  override def init(): Unit = {
    logger.info(s"Ready to change engine state!")
    //    setCodeParser()  // todo check
    super.init()
  }

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = Utils.tryFinally {
    this.engineExecutionContext = engineExecutorContext
    thread = Thread.currentThread()
    if (sc.isStopped) {
      logger.error("Spark application has already stopped, please restart it.")
      transition(NodeStatus.Failed)
      throw new LinkisJobRetryException(
        "Spark application sc has already stopped, please restart it."
      )
    }
    val kind: Kind = getKind
    var preCode = code
    engineExecutorContext.appendStdout(
      LogUtils.generateInfo(s"yarn application id: ${sc.applicationId}")
    )
    // Pre-execution hook
    var executionHook: SparkPreExecutionHook = null
    Utils.tryCatch {
      SparkPreExecutionHook
        .getSparkPreExecutionHooks()
        .foreach(hook => {
          executionHook = hook
          preCode = hook.callPreExecutionHook(engineExecutorContext, preCode)
        })
    } {
      case fatalException: ExecutorHookFatalException =>
        val hookName = getHookName(executionHook)
        logger.error(s"execute preExecution hook : ${hookName} failed.")
        throw fatalException
      case e: Exception =>
        val hookName = getHookName(executionHook)
        logger.error(s"execute preExecution hook : ${hookName} failed.")
    }
    Utils.tryAndWarn(CSSparkHelper.setContextIDInfoToSparkConf(engineExecutorContext, sc))
    val _code = kind match {
      case _: SparkDataCalc => preCode
      case _ => Kind.getRealCode(preCode)
    }
    logger.info(s"Ready to run code with kind $kind.")
    val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)
    val jobGroupId = if (StringUtils.isNotBlank(jobId)) {
      jobId
    } else {
      queryNum.incrementAndGet()
    }
    jobGroup = String.valueOf("linkis-spark-mix-code-" + jobGroupId)
    //    val executeCount = queryNum.get().toInt - 1
    logger.info("Set jobGroup to " + jobGroup)
    sc.setJobGroup(jobGroup, _code, true)

    val response = Utils.tryFinally(runCode(this, _code, engineExecutorContext, jobGroup)) {
      // Utils.tryAndWarn(this.engineExecutionContext.pushProgress(1, getProgressInfo("")))
      jobGroup = null
      sc.clearJobGroup()
    }
    // Post-execution hook
    Utils.tryQuietly(
      SparkPostExecutionHook
        .getSparkPostExecutionHooks()
        .foreach(_.callPostExecutionHook(engineExecutorContext, response, code))
    )
    response
  } {
    this.engineExecutionContext = null
  }

  private def getHookName(executeHook: SparkPreExecutionHook): String = {
    if (null == executeHook) {
      "empty hook"
    } else {
      executeHook.getClass.getName
    }
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = {
    val newcode = completedLine + code
    logger.info("newcode is " + newcode)
    executeLine(engineExecutorContext, newcode)
  }

  override def progress(taskID: String): Float =
    if (jobGroup == null || engineExecutionContext.getTotalParagraph == 0) {
      ProgressUtils.getOldProgress(this.engineExecutionContext)
    } else {
      val newProgress =
        (engineExecutionContext.getCurrentParagraph * 1f - 1f) / engineExecutionContext.getTotalParagraph + JobProgressUtil
          .progress(sc, jobGroup) / engineExecutionContext.getTotalParagraph
      val normalizedProgress =
        if (newProgress >= 1) GovernanceCommonConf.FAKE_PROGRESS else newProgress
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

  override def FetchResource: util.HashMap[String, ResourceWithStatus] = {
    val resourceMap = new util.HashMap[String, ResourceWithStatus]()
    val activeJobs = JobProgressUtil.getActiveJobProgressInfo(sc, jobGroup)
    val applicationStatus =
      if (activeJobs == null || activeJobs.length == 0) "RUNNING" else "COMPLETED"
    getCurrentNodeResource().getUsedResource match {
      case resource: DriverAndYarnResource =>
        resourceMap.put(
          sc.applicationId,
          new ResourceWithStatus(
            resource.getYarnResource.getQueueMemory,
            resource.getYarnResource.getQueueCores,
            resource.getYarnResource.getQueueInstances,
            applicationStatus,
            resource.getYarnResource.getQueueName
          )
        )
      case _ =>
        resourceMap.put(sc.applicationId, new ResourceWithStatus(0, 0, 0, "UNKNOWN", "UNKNOWN"))
    }
    resourceMap
  }

  override def getCurrentNodeResource(): NodeResource = {
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
        "Current actual used resources is driverMem:" + driverMem + ",driverCores:" + sparkDriverCores + ",executorMem:" + executorMem + ",executorCores:" + sparkExecutorCores + ",queue:" + queue
      )
      val uesdResource = new DriverAndYarnResource(
        new LoadInstanceResource(driverMem, sparkDriverCores, 1),
        new YarnResource(executorMem, sparkExecutorCores, 0, queue, sc.applicationId)
      )
      val nodeResource = new CommonNodeResource
      nodeResource.setUsedResource(uesdResource)
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

  override def getId(): String = getExecutorIdPreFix + id

  protected def getExecutorIdPreFix: String

  protected def getKind: Kind

  protected def runCode(
      executor: SparkEngineConnExecutor,
      code: String,
      context: EngineExecutionContext,
      jobGroup: String
  ): ExecuteResponse

  override def killTask(taskID: String): Unit = {
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
    getExecutorLabels().asScala.foreach { l =>
      l match {
        case label: CodeLanguageLabel =>
          runType = label.getCodeType
        case _ =>
      }
    }
    logger.warn(s"Kill running job of ${runType} .")
  }

  override def close(): Unit = {
    super.close()
  }

}
