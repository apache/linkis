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
import org.apache.linkis.common.utils.{ByteTimeUtils, CodeAndRunTypeUtils, Logging, Utils}
import org.apache.linkis.engineconn.common.conf.{EngineConnConf, EngineConnConstant}
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.entity.EngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.computation.executor.utlis.{
  ComputationEngineConstant,
  ProgressUtils
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.core.exception.ExecutorHookFatalException
import org.apache.linkis.engineconn.executor.entity.{ResourceFetchExecutor, YarnExecutor}
import org.apache.linkis.engineplugin.spark.common.{Kind, SparkDataCalc}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.cs.CSSparkHelper
import org.apache.linkis.engineplugin.spark.errorcode.SparkErrorCodeSummary
import org.apache.linkis.engineplugin.spark.exception.RuleCheckFailedException
import org.apache.linkis.engineplugin.spark.extension.{
  SparkPostExecutionHook,
  SparkPreExecutionHook
}
import org.apache.linkis.engineplugin.spark.utils.JobProgressUtil
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.exception.LinkisJobRetryException
import org.apache.linkis.governance.common.exception.engineconn.{
  EngineConnExecutorErrorCode,
  EngineConnExecutorErrorException
}
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus
import org.apache.linkis.manager.common.entity.resource._
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.CodeLanguageLabel
import org.apache.linkis.manager.label.utils.{LabelUtil, LabelUtils}
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.ExecuteResponse
import org.apache.linkis.server.toJavaMap

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

  private val closeThreadEnable =
    SparkConfiguration.SPARK_SCALA_KILL_COLSE_THREAD_ENABLE.getValue

  private var thread: Thread = _

  private var applicationId: String = sc.applicationId

  private var sparkTmpConf = Map[String, String]()
  override def getApplicationId: String = applicationId

  override def getApplicationURL: String = ""
  override def getYarnMode: String = ""
  override def getQueue: String = ""

  override def init(): Unit = {
    logger.info(s"Ready to change engine state!")
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

    val isFirstParagraph = (engineExecutorContext.getCurrentParagraph == 1)
    if (isFirstParagraph == true) {
      var yarnUrl = ""
      val engineContext = EngineConnObject.getEngineCreationContext
      if (null != engineContext) {
        engineContext
          .getLabels()
          .asScala
          .foreach(label => {
            if (label.getLabelKey.equals(LabelKeyConstant.YARN_CLUSTER_KEY)) {
              yarnUrl = EngineConnConf.JOB_YARN_CLUSTER_TASK_URL.getValue
            } else {
              yarnUrl = EngineConnConf.JOB_YARN_TASK_URL.getValue
            }
          })
      }
      engineExecutorContext.appendStdout(
        LogUtils.generateInfo(EngineConnConstant.YARN_LOG_URL + yarnUrl + s"${sc.applicationId}")
      )
    }

    // 正则匹配校验
    val ready = EngineConnObject.isReady
    val jobId: String = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)
    val udfNames: String = System.getProperty(ComputationExecutorConf.ONLY_SQL_USE_UDF_KEY, "")
    if (ready && StringUtils.isNotBlank(udfNames) && StringUtils.isNotBlank(jobId)) {
      val codeType: String = LabelUtil.getCodeType(engineExecutorContext.getLabels.toList.asJava)
      val languageType: String = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)
      // sql 或者 python
      if (!ComputationExecutorConf.SUPPORT_SPECIAL_UDF_LANGUAGES.getValue.contains(languageType)) {
        val udfNames: String = ComputationExecutorConf.SPECIAL_UDF_NAMES.getValue
        if (StringUtils.isNotBlank(udfNames)) {
          val funcNames: Array[String] = udfNames.split(",")
          funcNames.foreach(funcName => {
            if (code.contains(funcName)) {
              logger.info("contains specific functionName: {}", udfNames)
              throw new RuleCheckFailedException(
                SparkErrorCodeSummary.NOT_SUPPORT_FUNCTION.getErrorCode,
                SparkErrorCodeSummary.NOT_SUPPORT_FUNCTION.getErrorDesc
              )
            }
          })
        }
      }
    }

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
        logger.info(s"execute preExecution hook : ${hookName} failed.")
    }
    Utils.tryAndWarn(CSSparkHelper.setContextIDInfoToSparkConf(engineExecutorContext, sc))
    val _code = kind match {
      case _: SparkDataCalc => preCode
      case _ => Kind.getRealCode(preCode)
    }
    logger.info(s"Ready to run code with kind $kind.")
    val jobGroupId = if (StringUtils.isNotBlank(jobId)) {
      jobId
    } else {
      queryNum.incrementAndGet()
    }
    jobGroup = String.valueOf("linkis-spark-mix-code-" + jobGroupId)
    //    val executeCount = queryNum.get().toInt - 1
    logger.info("Set jobGroup to " + jobGroup)
    sc.setJobGroup(jobGroup, _code, true)

    // print job configuration, only the first paragraph or retry
    val errorIndex: Integer = Integer.valueOf(
      engineExecutionContext.getProperties.getOrDefault("execute.error.code.index", "-1").toString
    )
    if (isFirstParagraph || (errorIndex + 1 == engineExecutorContext.getCurrentParagraph)) {
      Utils.tryCatch({
        val executorNum: Int = sc.getConf.get("spark.executor.instances").toInt
        val executorMem: Long =
          ByteTimeUtils.byteStringAsGb(sc.getConf.get("spark.executor.memory"))
        val driverMem: Long = ByteTimeUtils.byteStringAsGb(sc.getConf.get("spark.driver.memory"))
        val sparkExecutorCores = sc.getConf.get("spark.executor.cores", "2").toInt
        val sparkDriverCores = sc.getConf.get("spark.driver.cores", "1").toInt
        val queue = sc.getConf.get("spark.yarn.queue")
        // with unit if set configuration with unit
        // if not set sc get will get the value of spark.yarn.executor.memoryOverhead such as 512(without unit)
        val memoryOverhead = sc.getConf.get("spark.executor.memoryOverhead", "1G")
        val pythonVersion = SparkConfiguration.SPARK_PYTHON_VERSION.getValue(
          EngineConnObject.getEngineCreationContext.getOptions
        )
        var engineType = ""
        val labels = engineExecutorContext.getLabels
        if (labels.length > 0) {
          engineType = LabelUtil.getEngineTypeLabel(labels.toList.asJava).getStringValue
        }
        val sb = new StringBuilder
        sb.append(s"spark.executor.instances=$executorNum\n")
        sb.append(s"spark.executor.memory=${executorMem}G\n")
        sb.append(s"spark.driver.memory=${driverMem}G\n")
        sb.append(s"spark.executor.cores=$sparkExecutorCores\n")
        sb.append(s"spark.driver.cores=$sparkDriverCores\n")
        sb.append(s"spark.yarn.queue=$queue\n")
        sb.append(s"spark.executor.memoryOverhead=${memoryOverhead}\n")
        sb.append(s"spark.python.version=$pythonVersion\n")
        sb.append(s"spark.engineType=$engineType\n")
        val dynamicAllocation: String = sc.getConf.get("spark.dynamicAllocation.enabled", "false")
        if ("true".equals(dynamicAllocation)) {
          val shuffleEnabled: String = sc.getConf.get("spark.shuffle.service.enabled", "false")
          val minExecutors: Int = sc.getConf.get("spark.dynamicAllocation.minExecutors", "1").toInt
          val maxExecutors: Int =
            sc.getConf.get("spark.dynamicAllocation.maxExecutors", "50").toInt
          sb.append("spark.dynamicAllocation.enabled=true\n")
          sb.append(s"spark.shuffle.service.enabled=$shuffleEnabled\n")
          sb.append(s"spark.dynamicAllocation.minExecutors=$minExecutors\n")
          sb.append(s"spark.dynamicAllocation.maxExecutors=$maxExecutors\n")
        }
        sb.append("\n")
        engineExecutionContext.appendStdout(
          LogUtils.generateInfo(s" Your spark job exec with configs:\n${sb.toString()}")
        )
      })(t => {
        logger.warn("Get actual used resource exception", t)
      })
    }

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
        val threadName = thread.getName
        if (closeThreadEnable) {
          if (threadName.contains(ComputationEngineConstant.TASK_EXECUTION_THREAD)) {
            logger.info(s"try to force stop thread:${threadName}")
            // force to stop scala thread
            Utils.tryAndWarn(thread.stop())
          } else {
            logger.info(s"skip to force stop thread:${threadName}")
          }
        }
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

  override protected def beforeExecute(engineConnTask: EngineConnTask): Unit = {
    super.beforeExecute(engineConnTask)
    if (
        EngineConnConf.ENGINE_CONF_REVENT_SWITCH.getValue && sparkTmpConf.isEmpty && this
          .isInstanceOf[SparkSqlExecutor]
    ) {
      val sqlContext = this.asInstanceOf[SparkSqlExecutor].getSparkEngineSession.sqlContext
      sparkTmpConf = sqlContext.getAllConfs
      // 维护spark扩展配置,防止不同版本的sprk 默认配置与用户配置匹配不上，导致配置无法回滚
      SparkConfiguration.SPARK_ENGINE_EXTENSION_CONF
        .split(',')
        .foreach(keyValue => {
          val key = keyValue.split("=")(0).trim
          val value = keyValue.split("=")(1).trim
          if (!sparkTmpConf.containsKey(key)) {
            sparkTmpConf += key -> value
          }
        })
    }
  }

  override protected def afterExecute(
      engineConnTask: EngineConnTask,
      executeResponse: ExecuteResponse
  ): Unit = {
    try {
      if (
          EngineConnConf.ENGINE_CONF_REVENT_SWITCH.getValue
          && sparkTmpConf.nonEmpty
          && this.isInstanceOf[SparkSqlExecutor]
      ) {

        val sqlExecutor = this.asInstanceOf[SparkSqlExecutor]
        Option(sqlExecutor.getSparkEngineSession)
          .flatMap(session => Option(session.sqlContext))
          .foreach { sqlContext =>
            sparkTmpConf.foreach { case (key, value) =>
              if (value != null && !value.equals(sqlContext.getConf(key))) {
                sqlContext.setConf(key, value)
              }
            }
            // 清理多出来的配置
            sqlContext.getAllConfs.keys.foreach { key =>
              if (!sparkTmpConf.contains(key)) {
                logger.info(s"Clearing extra configuration key: $key")
                sqlContext.setConf(key, "")
              }
            }
          }
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error in afterExecute for task ${engineConnTask.getTaskId}", e)
    } finally {
      super.afterExecute(engineConnTask, executeResponse)
    }
  }

}
