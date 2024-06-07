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

package org.apache.linkis.engineplugin.hive.executor

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{
  ComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.computation.executor.utlis.ProgressUtils
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.executor.entity.ResourceFetchExecutor
import org.apache.linkis.engineplugin.hive.conf.{Counters, HiveEngineConfiguration}
import org.apache.linkis.engineplugin.hive.cs.CSHiveHelper
import org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.{
  COMPILE_HIVE_QUERY_ERROR,
  GET_FIELD_SCHEMAS_ERROR
}
import org.apache.linkis.engineplugin.hive.exception.HiveQueryFailedException
import org.apache.linkis.engineplugin.hive.progress.HiveProgressHelper
import org.apache.linkis.governance.common.constant.job.JobRequestConstants
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, NodeResource}
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.hive.common.HiveInterruptUtils
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.metastore.api.{FieldSchema, Schema}
import org.apache.hadoop.hive.ql.{Driver, QueryPlan}
import org.apache.hadoop.hive.ql.exec.Task.TaskState
import org.apache.hadoop.hive.ql.exec.Utilities
import org.apache.hadoop.hive.ql.exec.mr.HadoopJobExecHelper
import org.apache.hadoop.hive.ql.exec.tez.TezJobExecHelper
import org.apache.hadoop.hive.ql.processors.{
  CommandProcessor,
  CommandProcessorFactory,
  CommandProcessorResponse
}
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.hadoop.mapred.{JobStatus, RunningJob}
import org.apache.hadoop.security.UserGroupInformation

import java.io.ByteArrayOutputStream
import java.security.PrivilegedExceptionAction
import java.util
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import org.slf4j.LoggerFactory

class HiveEngineConnExecutor(
    id: Int,
    sessionState: SessionState,
    ugi: UserGroupInformation,
    hiveConf: HiveConf,
    baos: ByteArrayOutputStream = null
) extends ComputationExecutor
    with ResourceFetchExecutor {

  private val LOG = LoggerFactory.getLogger(getClass)

  private val namePrefix: String = "HiveEngineExecutor_"

  private var proc: CommandProcessor = _

  private var map: Int = 0

  private var reduce: Int = 0

  private val totalTask = 200.0f

  private var singleLineProgress: Float = 0.0f

  private var engineExecutorContext: EngineExecutionContext = _

  private val singleCodeCompleted: AtomicBoolean = new AtomicBoolean(false)

  private var numberOfMRJobs: Int = 0

  private var currentSqlProgress: Float = 0.0f

  private val singleSqlProgressMap: util.Map[String, Float] = new util.HashMap[String, Float]()

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private var driver: HiveDriverProxy = _

  private var thread: Thread = _

  private val applicationStringName = "application"

  private val splitter = "_"

  override def init(): Unit = {
    LOG.info(s"Ready to change engine state!")
    if (HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) {
      System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }
    setCodeParser(new SQLCodeParser)
    super.init()
  }

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    this.engineExecutorContext = engineExecutorContext
    CSHiveHelper.setContextIDInfoToHiveConf(engineExecutorContext, hiveConf)
    singleSqlProgressMap.clear()
    singleCodeCompleted.set(false)
    currentSqlProgress = 0.0f
    val realCode = code.trim()
    LOG.info(s"hive client begins to run hql code:\n ${realCode.trim}")
    val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)

    if (StringUtils.isNotBlank(jobId)) {
      val jobTags = JobUtils.getJobSourceTagsFromObjectMap(engineExecutorContext.getProperties)
      val tags = if (StringUtils.isAsciiPrintable(jobTags)) {
        s"LINKIS_$jobId,$jobTags"
      } else {
        s"LINKIS_$jobId"
      }
      LOG.info(s"set mapreduce.job.tags=$tags")
      hiveConf.set("mapreduce.job.tags", tags)
    }

    if (realCode.trim.length > 500) {
      engineExecutorContext.appendStdout(s"$getId >> ${realCode.trim.substring(0, 500)} ...")
    } else engineExecutorContext.appendStdout(s"$getId >> ${realCode.trim}")
    val tokens = realCode.trim.split("""\s+""")
    SessionState.setCurrentSessionState(sessionState)
    sessionState.setLastCommand(code)
    if (
        engineExecutorContext.getCurrentParagraph == 1 && engineExecutorContext.getProperties
          .containsKey(JobRequestConstants.LINKIS_JDBC_DEFAULT_DB)
    ) {
      val defaultDB =
        engineExecutorContext.getProperties
          .get(JobRequestConstants.LINKIS_JDBC_DEFAULT_DB)
          .asInstanceOf[String]
      logger.info(s"set default DB to $defaultDB")
      sessionState.setCurrentDatabase(defaultDB)
    }
    val proc = CommandProcessorFactory.get(tokens, hiveConf)
    this.proc = proc
    LOG.debug("ugi is " + ugi.getUserName)
    Utils.tryFinally {
      ugi.doAs(new PrivilegedExceptionAction[ExecuteResponse]() {
        override def run(): ExecuteResponse = {
          proc match {
            case any if HiveDriverProxy.isDriver(any) =>
              logger.info(s"driver is $any")
              thread = Thread.currentThread()
              driver = new HiveDriverProxy(any)
              executeHQL(realCode, driver)
            case _ =>
              val resp = proc.run(realCode.substring(tokens(0).length).trim)
              val result = new String(baos.toByteArray)
              logger.info("RESULT => {}", result)
              engineExecutorContext.appendStdout(result)
              baos.reset()
              if (resp.getResponseCode != 0) {
                clearCurrentProgress()
                HiveProgressHelper.clearHiveProgress()
                onComplete()
                singleSqlProgressMap.clear()
                HiveProgressHelper.storeSingleSQLProgress(0.0f)
                throw resp.getException
              }
              HiveProgressHelper.clearHiveProgress()
              HiveProgressHelper.storeSingleSQLProgress(0.0f)
              onComplete()
              singleSqlProgressMap.clear()
              SuccessExecuteResponse()
          }
        }
      })
    } {
      if (this.driver != null) {
        Utils.tryQuietly {
          driver.close()
          this.driver = null
          val ss = SessionState.get()
          if (ss != null) {
            ss.deleteTmpOutputFile()
            ss.deleteTmpErrOutputFile()
          }
        }
      }
    }
  }

  private def executeHQL(realCode: String, driver: HiveDriverProxy): ExecuteResponse = {
    var needRetry: Boolean = true
    var tryCount: Int = 0
    var hasResult: Boolean = false
    var rows: Int = 0
    var columnCount: Int = 0
    while (needRetry) {
      needRetry = false
      driver.setTryCount(tryCount + 1)
      val startTime = System.currentTimeMillis()
      try {
        val hiveResponse: CommandProcessorResponse =
          if (!HiveDriverProxy.isIDriver(driver.getDriver())) {
            var compileRet = -1
            Utils.tryCatch {
              compileRet = driver.compile(realCode)
              logger.info(
                s"driver compile realCode : \n ${realCode} \n finished, status : ${compileRet}"
              )
              if (0 != compileRet) {
                logger.warn(s"compile realCode : \n ${realCode} \n error status : ${compileRet}")
                throw HiveQueryFailedException(
                  COMPILE_HIVE_QUERY_ERROR.getErrorCode,
                  COMPILE_HIVE_QUERY_ERROR.getErrorDesc
                )
              }
              val queryPlan = driver.getPlan()
              val numberOfJobs = Utilities.getMRTasks(queryPlan.getRootTasks).size
              numberOfMRJobs = numberOfJobs
              logger.info(s"there are ${numberOfMRJobs} jobs.")
            } {
              case e: Exception => logger.warn("obtain hive execute query plan failed,", e)
              case t: Throwable => logger.warn("obtain hive execute query plan failed,", t)
            }
            if (numberOfMRJobs > 0) {
              engineExecutorContext.appendStdout(s"Your hive sql has $numberOfMRJobs MR jobs to do")
            }
            if (thread.isInterrupted) {
              logger.error(
                "The thread of execution has been interrupted and the task should be terminated"
              )
              return ErrorExecuteResponse(
                "The thread of execution has been interrupted and the task should be terminated",
                null
              )
            }
            driver.run(realCode, compileRet == 0)
          } else {
            driver.run(realCode)
          }
        if (hiveResponse.getResponseCode != 0) {
          LOG.error("Hive query failed, response code is {}", hiveResponse.getResponseCode)
          return ErrorExecuteResponse(hiveResponse.getErrorMessage, hiveResponse.getException)
        }
        engineExecutorContext.appendStdout(
          s"Time taken: ${ByteTimeUtils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results."
        )
        LOG.info(
          s"$getId >> Time taken: ${ByteTimeUtils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results."
        )

        val fieldSchemas =
          if (hiveResponse.getSchema != null) hiveResponse.getSchema.getFieldSchemas
          else if (driver.getSchema != null) {
            driver.getSchema.getFieldSchemas
          } else {
            throw HiveQueryFailedException(
              GET_FIELD_SCHEMAS_ERROR.getErrorCode,
              GET_FIELD_SCHEMAS_ERROR.getErrorDesc
            )
          }
        LOG.debug("fieldSchemas are " + fieldSchemas)
        if (fieldSchemas == null || isNoResultSql(realCode)) {
          // IOUtils.closeQuietly(resultSetWriter)
          numberOfMRJobs = -1
          singleCodeCompleted.set(true)
          onComplete()
          singleSqlProgressMap.clear()
          return SuccessExecuteResponse()
        }
        // get column data
        val metaData: TableMetaData =
          getResultMetaData(fieldSchemas, engineExecutorContext.getEnableResultsetMetaWithTableName)
        // send result
        rows = sendResultSet(engineExecutorContext, driver, metaData)
        columnCount = if (fieldSchemas != null) fieldSchemas.size() else 0
        hasResult = true
      } catch {
        case e if HiveDriverProxy.isCommandNeedRetryException(e) =>
          tryCount += 1
          needRetry = true
          HiveProgressHelper.clearHiveProgress()
          onComplete()
          singleSqlProgressMap.clear()
          clearCurrentProgress()
          HiveProgressHelper.storeSingleSQLProgress(0.0f)
          LOG.warn("Retry hive query with a different approach...")
        case t: Throwable =>
          LOG.error(s"query failed, reason : ", t)
          HiveProgressHelper.clearHiveProgress()
          clearCurrentProgress()
          HiveProgressHelper.storeSingleSQLProgress(0.0f)
          singleCodeCompleted.set(true)
          numberOfMRJobs = -1
          onComplete()
          singleSqlProgressMap.clear()
          return ErrorExecuteResponse(t.getMessage, t)
      }
    }
    if (hasResult) {
      engineExecutorContext.appendStdout(s"Fetched  $columnCount col(s) : $rows row(s) in hive")
      LOG.info(s"$getId >> Fetched  $columnCount col(s) : $rows row(s) in hive")
    }
    clearCurrentProgress()
    HiveProgressHelper.clearHiveProgress()
    HiveProgressHelper.storeSingleSQLProgress(0.0f)
    singleCodeCompleted.set(true)
    numberOfMRJobs = -1
    onComplete()
    singleSqlProgressMap.clear()
    SuccessExecuteResponse()
  }

  private def sendResultSet(
      engineExecutorContext: EngineExecutionContext,
      driver: HiveDriverProxy,
      metaData: TableMetaData
  ): Int = {
    val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    resultSetWriter.addMetaData(metaData)
    val colLength = metaData.columns.length
    val result = new util.ArrayList[String]()
    var rows = 0
    while (driver.getResults(result)) {
      val scalaResult: mutable.Buffer[String] = result.asScala
      scalaResult foreach { s =>
        val arr: Array[String] = s.split("\t")
        val arrAny: ArrayBuffer[Any] = new ArrayBuffer[Any]()
        if (arr.length > colLength) {
          logger.error(
            s"""There is a \t tab in the result of hive code query, hive cannot cut it, please use spark to execute(查询的结果中有\t制表符，hive不能进行切割,请使用spark执行)"""
          )
          throw new ErrorException(
            60078,
            """There is a \t tab in the result of your query, hive cannot cut it, please use spark to execute(您查询的结果中有\t制表符，hive不能进行切割,请使用spark执行)"""
          )
        }
        if (arr.length == colLength) arr foreach arrAny.asJava.add
        else if (arr.length == 0) for (i <- 1 to colLength) arrAny.asJava add ""
        else {
          val i = colLength - arr.length
          arr foreach arrAny.asJava.add
          for (i <- 1 to i) arrAny.asJava add ""
        }
        resultSetWriter.addRecord(new TableRecord(arrAny.toArray.asInstanceOf[Array[AnyRef]]))
      }
      rows += result.size
      result.clear()
    }
    engineExecutorContext.sendResultSet(resultSetWriter)
    rows
  }

  private def getResultMetaData(
      fieldSchemas: util.List[FieldSchema],
      useTableName: Boolean
  ): TableMetaData = {
    var results: util.List[FieldSchema] = null
    val nameSet = new mutable.HashSet[String]()
    val cleanSchema = new util.ArrayList[FieldSchema]()
    fieldSchemas.asScala foreach { fieldSchema =>
      val name = fieldSchema.getName
      if (name.split('.').length == 2) {
        nameSet.add(name.split('.')(1))
        cleanSchema.asScala += new FieldSchema(
          name.split('.')(1),
          fieldSchema.getType,
          fieldSchema.getComment
        )
      }
    }
    if (nameSet.size < fieldSchemas.asScala.length) {
      results = fieldSchemas
    } else {
      if (useTableName) {
        results = fieldSchemas
      } else {
        results = cleanSchema
      }
    }

    val columns = results.asScala
      .map(result =>
        new Column(
          result.getName,
          DataType.toDataType(result.getType.toLowerCase()),
          result.getComment
        )
      )
      .toArray[Column]
    val metaData = new TableMetaData(columns)
    metaData
  }

  private def isNoResultSql(sql: String): Boolean = {
    if (sql.trim.startsWith("create table") || sql.trim.startsWith("drop table")) true else false
  }

  /**
   * Before the job is completed, all the remaining contents of the singleSqlProgressMap should be
   * changed to success
   */
  private def onComplete(): Unit = {}

  private def clearCurrentProgress(): Unit = {
    reduce = 0
    map = 0
    singleLineProgress = 0.0f
  }

  private def justFieldName(schemaName: String): String = {
    LOG.debug("schemaName: " + schemaName)
    val arr = schemaName.split("\\.")
    if (arr.length == 2) arr(1) else schemaName
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = {
    val completeCode = code + completedLine
    executeLine(engineExecutorContext, completeCode)
  }

  override def close(): Unit = {
    singleSqlProgressMap.clear()
    Utils.tryAndWarnMsg(sessionState.close())("close session failed")
    super.close()
  }

  override def FetchResource: util.HashMap[String, ResourceWithStatus] = {
    val resourceMap = new util.HashMap[String, ResourceWithStatus]()
    val queue = hiveConf.get("mapreduce.job.queuename")
    HadoopJobExecHelper.runningJobs.asScala.foreach(yarnJob => {
      val counters = yarnJob.getCounters
      if (counters != null) {
        val millsMap = counters.getCounter(Counters.MILLIS_MAPS)
        val millsReduces = counters.getCounter(Counters.MILLIS_REDUCES)
        val totalMapCores = counters.getCounter(Counters.VCORES_MILLIS_MAPS)
        val totalReducesCores = counters.getCounter(Counters.VCORES_MILLIS_REDUCES)
        val totalMapMBMemory = counters.getCounter(Counters.MB_MILLIS_MAPS)
        val totalReducesMBMemory = counters.getCounter(Counters.MB_MILLIS_REDUCES)
        var avgCores = 0
        var avgMemory = 0L
        if (millsMap > 0 && millsReduces > 0) {
          avgCores = Math.ceil(totalMapCores / millsMap + totalReducesCores / millsReduces).toInt
          avgMemory = Math
            .ceil(
              totalMapMBMemory * 1024 * 1024 / millsMap + totalReducesMBMemory.toLong * 1024 * 1024 / millsReduces
            )
            .toLong
          val yarnResource = new ResourceWithStatus(
            avgMemory,
            avgCores,
            0,
            JobStatus.getJobRunState(yarnJob.getJobStatus.getRunState),
            queue
          )
          val applicationId =
            applicationStringName + splitter + yarnJob.getID.getJtIdentifier + splitter + yarnJob.getID.getId
          resourceMap.put(applicationId, yarnResource)
        }
      }
    })
    resourceMap
  }

  override def progress(taskID: String): Float = {
    if (engineExecutorContext != null) {
      val totalSQLs = engineExecutorContext.getTotalParagraph
      val currentSQL = engineExecutorContext.getCurrentParagraph
      val currentBegin = (currentSQL - 1) / totalSQLs.asInstanceOf[Float]
      val finishedStage =
        if (null != driver && null != driver.getPlan() && !driver.getPlan().getRootTasks.isEmpty) {
          Utils.tryQuietly(
            Utilities
              .getMRTasks(driver.getPlan().getRootTasks)
              .asScala
              .count(task => task.isMapRedTask && task.getTaskState == TaskState.FINISHED)
          )
        } else {
          0
        }
      var totalProgress: Float = 0.0f
      if (!HadoopJobExecHelper.runningJobs.isEmpty) {
        val runningJob = HadoopJobExecHelper.runningJobs.get(0)
        val _progress = Utils.tryCatch(runningJob.reduceProgress() + runningJob.mapProgress()) {
          case e: Exception =>
            logger.info(s"Failed to get job(${runningJob.getJobName}) progress ", e)
            0.2f
        }
        if (!_progress.isNaN) {
          totalProgress = _progress / 2
        }
      }
      logger.info(
        s"Running stage  progress is $totalProgress, and finished stage is $finishedStage"
      )
      val hiveRunJobs = if (numberOfMRJobs <= 0) 1 else numberOfMRJobs
      if (finishedStage <= hiveRunJobs) {
        totalProgress = totalProgress + finishedStage
      }
      try {
        totalProgress = totalProgress / (hiveRunJobs * totalSQLs)
      } catch {
        case e: Exception => totalProgress = 0.0f
        case _ => totalProgress = 0.0f
      }

      val newProgress =
        if (totalProgress.isNaN || totalProgress.isInfinite) currentBegin
        else totalProgress + currentBegin
      logger.info(s"Hive progress is $newProgress, and finished stage is $finishedStage")
      val oldProgress = ProgressUtils.getOldProgress(this.engineExecutorContext)
      if (newProgress < oldProgress) oldProgress
      else {
        ProgressUtils.putProgress(newProgress, this.engineExecutorContext)
        newProgress
      }
    } else 0.0f
  }

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = {
    val arrayBuffer: ArrayBuffer[JobProgressInfo] = new ArrayBuffer[JobProgressInfo]()
    singleSqlProgressMap synchronized {
      val set = singleSqlProgressMap.keySet()
      val tempSet = new util.HashSet[RunningJob](HadoopJobExecHelper.runningJobs)
      import scala.collection.JavaConverters._
      set.asScala foreach { key =>
        if (!tempSet.contains(key)) {
          arrayBuffer += JobProgressInfo(key, 200, 0, 0, 200)
        }
      }
    }

    HadoopJobExecHelper.runningJobs synchronized {
      HadoopJobExecHelper.runningJobs.asScala foreach { runningJob =>
        val succeedTask =
          ((runningJob.mapProgress() + runningJob.reduceProgress()) * 100).asInstanceOf[Int]
        if (
            succeedTask.equals(
              totalTask.asInstanceOf[Int]
            ) || runningJob.isComplete || runningJob.isSuccessful
        ) {
          arrayBuffer += JobProgressInfo(
            runningJob.getID.toString,
            totalTask.asInstanceOf[Int],
            0,
            0,
            totalTask.asInstanceOf[Int]
          )
        } else {
          arrayBuffer += JobProgressInfo(
            runningJob.getID.toString,
            totalTask.asInstanceOf[Int],
            1,
            0,
            succeedTask
          )
        }
      }
    }
    arrayBuffer.toArray
  }

  override def killTask(taskID: String): Unit = {
    LOG.info(s"hive begins to kill job with id : ${taskID}")
    // Configure the engine through the wds.linkis.hive.engine.type parameter to control the way the task is killed
    LOG.info(s"hive engine type :${HiveEngineConfiguration.HIVE_ENGINE_TYPE}")
    HiveEngineConfiguration.HIVE_ENGINE_TYPE match {
      case "mr" =>
        HadoopJobExecHelper.killRunningJobs()
        Utils.tryQuietly(HiveInterruptUtils.interrupt())
        Utils.tryAndWarn(driver.close())
        if (null != thread) {
          Utils.tryAndWarn(thread.interrupt())
        }
      case "tez" =>
        Utils.tryQuietly(TezJobExecHelper.killRunningJobs())
        driver.close()
    }
    clearCurrentProgress()
    singleSqlProgressMap.clear()
    HiveProgressHelper.clearHiveProgress()
    LOG.info("hive killed job successfully")
    super.killTask(taskID)
  }

  override def supportCallBackLogs(): Boolean = {
    // todo
    true
  }

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (null != labels) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = {
    null
  }

  override def getCurrentNodeResource(): NodeResource = {
    val resource = new CommonNodeResource
    resource.setUsedResource(
      NodeResourceUtils
        .applyAsLoadInstanceResource(EngineConnObject.getEngineCreationContext.getOptions)
    )
    resource
  }

  override def getId(): String = namePrefix + id

}

class HiveDriverProxy(driver: Any) extends Logging {

  def getDriver(): Any = driver

  def compile(command: String): Int = {
    driver.getClass
      .getMethod("compile", classOf[String])
      .invoke(driver, command.asInstanceOf[AnyRef])
      .asInstanceOf[Int]
  }

  def getPlan(): QueryPlan = {
    driver.getClass.getMethod("getPlan").invoke(driver).asInstanceOf[QueryPlan]
  }

  def getSchema(): Schema = {
    driver.getClass.getMethod("getSchema").invoke(driver).asInstanceOf[Schema]
  }

  def run(): CommandProcessorResponse = {
    driver.getClass.getMethod("run").invoke(driver).asInstanceOf[CommandProcessorResponse]
  }

  def run(command: String): CommandProcessorResponse = {
    driver.getClass
      .getMethod("run", classOf[String])
      .invoke(driver, command.asInstanceOf[AnyRef])
      .asInstanceOf[CommandProcessorResponse]
  }

  def run(command: String, alreadyCompiled: Boolean): CommandProcessorResponse = {
    driver.getClass
      .getMethod("run", classOf[String], classOf[Boolean])
      .invoke(driver, command.asInstanceOf[AnyRef], alreadyCompiled.asInstanceOf[AnyRef])
      .asInstanceOf[CommandProcessorResponse]
  }

  def setTryCount(retry: Int): Unit = {
    if (HiveDriverProxy.HAS_COMMAND_NEED_RETRY_EXCEPTION) {
      driver.getClass
        .getMethod("setTryCount", classOf[Int])
        .invoke(driver, retry.asInstanceOf[AnyRef])
    }
  }

  def getResults(res: util.List[_]): Boolean = {
    driver.getClass
      .getMethod("getResults", classOf[util.List[_]])
      .invoke(driver, res.asInstanceOf[AnyRef])
      .asInstanceOf[Boolean]
  }

  def close(): Unit = {
    logger.info("start to close driver")
    driver.getClass.getMethod("close").invoke(driver)
    driver.getClass.getMethod("destroy").invoke(driver)
    logger.info("Finished to close driver")
  }

}

object HiveDriverProxy extends Logging {

  private val COMMAND_NEED_RETRY_EXCEPTION_CLASS_STR =
    "org.apache.hadoop.hive.ql.CommandNeedRetryException"

  private val IDRIVER_CLASS_STR = "org.apache.hadoop.hive.ql.IDriver"

  val COMMAND_NEED_RETRY_EXCEPTION_CLASS = Utils.tryQuietly {
    Thread
      .currentThread()
      .getContextClassLoader
      .loadClass(COMMAND_NEED_RETRY_EXCEPTION_CLASS_STR)
  }

  val IDRIVER_CLASS = Utils.tryQuietly {
    Thread
      .currentThread()
      .getContextClassLoader
      .loadClass(IDRIVER_CLASS_STR)
  }

  val HAS_COMMAND_NEED_RETRY_EXCEPTION: Boolean = COMMAND_NEED_RETRY_EXCEPTION_CLASS != null
  val HAS_IDRIVER: Boolean = IDRIVER_CLASS != null

  def isIDriver(any: Any): Boolean = HAS_IDRIVER && IDRIVER_CLASS.isInstance(any)

  def isDriver(any: Any): Boolean = isIDriver(any) || any.isInstanceOf[Driver]

  def isCommandNeedRetryException(any: Any): Boolean =
    HAS_COMMAND_NEED_RETRY_EXCEPTION && COMMAND_NEED_RETRY_EXCEPTION_CLASS.isInstance(any)

}
