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
import org.apache.linkis.engineconn.computation.executor.conf.ComputationExecutorConf
import org.apache.linkis.engineconn.computation.executor.execute.{
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineconn.executor.entity.{ConcurrentExecutor, ResourceFetchExecutor}
import org.apache.linkis.engineplugin.hive.conf.{Counters, HiveEngineConfiguration}
import org.apache.linkis.engineplugin.hive.creation.HiveEngineConnFactory
import org.apache.linkis.engineplugin.hive.cs.CSHiveHelper
import org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.COMPILE_HIVE_QUERY_ERROR
import org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.GET_FIELD_SCHEMAS_ERROR
import org.apache.linkis.engineplugin.hive.exception.HiveQueryFailedException
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.governance.common.utils.JobUtils
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadInstanceResource,
  NodeResource
}
import org.apache.linkis.manager.common.protocol.resource.ResourceWithStatus
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{
  CompletedExecuteResponse,
  ErrorExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}

import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.metastore.api.{FieldSchema, Schema}
import org.apache.hadoop.hive.ql.exec.Utilities
import org.apache.hadoop.hive.ql.exec.mr.HadoopJobExecHelper
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
import java.util.concurrent.{
  Callable,
  ConcurrentHashMap,
  LinkedBlockingQueue,
  ThreadPoolExecutor,
  TimeUnit
}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory

class HiveEngineConcurrentConnExecutor(
    id: Int,
    sessionState: SessionState,
    ugi: UserGroupInformation,
    hiveConf: HiveConf,
    baos: ByteArrayOutputStream = null
) extends ConcurrentComputationExecutor
    with ResourceFetchExecutor {

  private val LOG = LoggerFactory.getLogger(getClass)

  private val namePrefix: String = "HiveEngineExecutor_"

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private val driverCache: util.Map[String, HiveDriverProxy] =
    new ConcurrentHashMap[String, HiveDriverProxy]()

  private val applicationStringName = "application"

  private val splitter = "_"

  private var backgroundOperationPool: ThreadPoolExecutor = _

  override def init(): Unit = {
    LOG.info(s"Ready to change engine state!")
    if (HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) {
      System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }
    setCodeParser(new SQLCodeParser)

    val queue = new LinkedBlockingQueue[Runnable](100)
    backgroundOperationPool = new ThreadPoolExecutor(
      100,
      100,
      10,
      TimeUnit.SECONDS,
      queue,
      new ThreadFactoryBuilder().setNameFormat("Hive-Background-Pool-%d").build
    )
    backgroundOperationPool.allowCoreThreadTimeOut(true)
    super.init()
  }

  override def executeLine(
      engineExecutorContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    LOG.info(s"HiveEngineConcurrentConnExecutor Ready to executeLine: $code")
    val taskId: String = engineExecutorContext.getJobId.get
    CSHiveHelper.setContextIDInfoToHiveConf(engineExecutorContext, hiveConf)

    val realCode = code.trim()

    LOG.info(s"hive client begins to run hql code:\n ${realCode.trim}")
    val jobId = JobUtils.getJobIdFromMap(engineExecutorContext.getProperties)
    if (StringUtils.isNotBlank(jobId)) {
      LOG.info(s"set mapreduce.job.tags=LINKIS_$jobId")
      hiveConf.set("mapreduce.job.tags", s"LINKIS_$jobId")
    }
    if (realCode.trim.length > 500) {
      engineExecutorContext.appendStdout(s"$getId >> ${realCode.trim.substring(0, 500)} ...")
    } else engineExecutorContext.appendStdout(s"$getId >> ${realCode.trim}")
    val tokens = realCode.trim.split("""\s+""")

    val operation = new Callable[ExecuteResponse] {
      override def call(): ExecuteResponse = {
        SessionState.setCurrentSessionState(sessionState)
        sessionState.setLastCommand(code)

        val proc = CommandProcessorFactory.get(tokens, hiveConf)
        LOG.debug("ugi is " + ugi.getUserName)
        ugi.doAs(new PrivilegedExceptionAction[ExecuteResponse]() {
          override def run(): ExecuteResponse = {
            proc match {
              case any if HiveDriverProxy.isDriver(any) =>
                logger.info(s"driver is $any")

                val driver = new HiveDriverProxy(any)
                driverCache.put(taskId, driver)
                executeHQL(
                  engineExecutorContext.getJobId.get,
                  engineExecutorContext,
                  realCode,
                  driver
                )
              case _ =>
                val resp = proc.run(realCode.substring(tokens(0).length).trim)
                val result = new String(baos.toByteArray)
                logger.info("RESULT => {}", result)
                engineExecutorContext.appendStdout(result)
                baos.reset()
                if (resp.getResponseCode != 0) {
                  onComplete()
                  throw resp.getException
                }
                onComplete()
                SuccessExecuteResponse()
            }
          }
        })
      }
    }

    val future = backgroundOperationPool.submit(operation)
    future.get()
  }

  def logMemoryCache(): Unit = {
    logger.info(s"logMemoryCache running driver number: ${driverCache.size()}")
    for (driverEntry <- driverCache.asScala) {
      logger.info(s"running driver with taskId : ${driverEntry._1} .")
    }
  }

  private def executeHQL(
      taskId: String,
      engineExecutorContext: EngineExecutionContext,
      realCode: String,
      driver: HiveDriverProxy
  ): ExecuteResponse = {
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
              if (numberOfJobs > 0) {
                engineExecutorContext.appendStdout(
                  s"Your hive taskId: $taskId has $numberOfJobs MR jobs to do"
                )
              }

              logger.info(s"there are ${numberOfJobs} jobs.")
            } {
              case e: Exception => logger.warn("obtain hive execute query plan failed,", e)
              case t: Throwable => logger.warn("obtain hive execute query plan failed,", t)
            }

            driver.run(realCode, compileRet == 0)
          } else {
            driver.run(realCode)
          }
        if (hiveResponse.getResponseCode != 0) {
          LOG.error("Hive query failed, response code is {}", hiveResponse.getResponseCode)
          // todo check uncleared context ?
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
          onComplete()

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
          onComplete()

          LOG.warn("Retry hive query with a different approach...")
        case t: Throwable =>
          LOG.error(s"query failed, reason : ", t)
          onComplete()
          return ErrorExecuteResponse(t.getMessage, t)
      } finally {
        driverCache.remove(taskId)
        logMemoryCache()
      }
    }
    if (hasResult) {
      engineExecutorContext.appendStdout(s"Fetched  $columnCount col(s) : $rows row(s) in hive")
      LOG.info(s"$getId >> Fetched  $columnCount col(s) : $rows row(s) in hive")
    }

    onComplete()
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

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = {
    val completeCode = code + completedLine
    executeLine(engineExecutorContext, completeCode)
  }

  override def close(): Unit = {
    killAll()

    if (backgroundOperationPool != null) {
      backgroundOperationPool.shutdown()
      try backgroundOperationPool.awaitTermination(10, TimeUnit.SECONDS)
      catch {
        case e: InterruptedException =>
          LOG.warn(
            "HIVE_SERVER2_ASYNC_EXEC_SHUTDOWN_TIMEOUT = " + 10 + " seconds has been exceeded. RUNNING background operations will be shut down",
            e
          )
      }
      backgroundOperationPool = null
    }
    super.close()
  }

  override def FetchResource: util.HashMap[String, ResourceWithStatus] = {
    val resourceMap = new util.HashMap[String, ResourceWithStatus]()
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
            "default"
          )
          val applicationId =
            applicationStringName + splitter + yarnJob.getID.getJtIdentifier + splitter + yarnJob.getID.getId
          resourceMap.put(applicationId, yarnResource)
        }
      }
    })
    resourceMap
  }

  override def progress(taskID: String): Float = 0.0f

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] =
    Array.empty[JobProgressInfo]

  override def killTask(taskID: String): Unit = {
    cleanup(taskID)
    super.killTask(taskID)
  }

  override def getConcurrentLimit: Int = HiveEngineConfiguration.HIVE_ENGINE_CONCURRENT_LIMIT

  override def killAll(): Unit = {
    val iterator = driverCache.entrySet().iterator()
    while (iterator.hasNext) {
      val entry = iterator.next()
      val taskID = entry.getKey
      cleanup(taskID)

      super.killTask(taskID)
      iterator.remove()
    }

    sessionState.deleteTmpOutputFile()
    sessionState.deleteTmpErrOutputFile()
    sessionState.close()
  }

  private def cleanup(taskID: String) = {
    val driver = driverCache.get(taskID)
    if (driver == null) LOG.warn(s"do cleanup taskId :${taskID} driver is null.")
    else {
      driver.close()
      driverCache.remove(taskID)
    }

    LOG.info(s"hive begins to kill job with id : ${taskID}")
    // Configure the engine through the wds.linkis.hive.engine.type parameter to control the way the task is killed
    LOG.info(s"hive engine type :${HiveEngineConfiguration.HIVE_ENGINE_TYPE}")
    LOG.info("hive killed job successfully")
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
