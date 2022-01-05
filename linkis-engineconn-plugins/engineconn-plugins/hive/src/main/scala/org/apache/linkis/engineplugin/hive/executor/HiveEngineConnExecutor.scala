/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineplugin.hive.executor

import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{ComputationExecutor, EngineExecutionContext}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.engineplugin.hive.cs.CSHiveHelper
import org.apache.linkis.engineplugin.hive.exception.HiveQueryFailedException
import org.apache.linkis.engineplugin.hive.progress.HiveProgressHelper
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.manager.common.entity.resource.{CommonNodeResource, LoadInstanceResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EngineConnPluginConf
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{ErrorExecuteResponse, ExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.storage.domain.{Column, DataType}
import org.apache.linkis.storage.resultset.ResultSetFactory
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.apache.hadoop.hive.common.HiveInterruptUtils
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.conf.HiveConf.ConfVars
import org.apache.hadoop.hive.metastore.api.{FieldSchema, Schema}
import org.apache.hadoop.hive.ql.Driver
import org.apache.hadoop.hive.ql.exec.mr.HadoopJobExecHelper
import org.apache.hadoop.hive.ql.processors.{CommandProcessor, CommandProcessorFactory, CommandProcessorResponse}
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.hadoop.mapred.RunningJob
import org.apache.hadoop.security.UserGroupInformation
import org.slf4j.LoggerFactory

import java.io.ByteArrayOutputStream
import java.security.PrivilegedExceptionAction
import java.util
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import org.apache.linkis.hadoop.common.conf.HadoopConf

class HiveEngineConnExecutor(id: Int,
                             sessionState: SessionState,
                             ugi: UserGroupInformation,
                             hiveConf: HiveConf,
                             baos: ByteArrayOutputStream = null) extends ComputationExecutor {

  private val LOG = LoggerFactory.getLogger(getClass)

  private val namePrefix: String = "HiveEngineExecutor_"

  private var proc: CommandProcessor = _

  private var map: Int = 0

  private var reduce: Int = 0

  private val totalTask = 200.0f

  private var singleLineProgress: Float = 0.0f

  private var stage: Int = 0

  private var engineExecutorContext: EngineExecutionContext = _

  private val singleCodeCompleted: AtomicBoolean = new AtomicBoolean(false)

  private var numberOfMRJobs: Int = 0

  private var currentSqlProgress: Float = 0.0f

  private val singleSqlProgressMap: util.Map[String, Float] = new util.HashMap[String, Float]()

  private val executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]]()

  private var driver: HiveDriverProxy = _

  private var thread: Thread = _

  override def init(): Unit = {
    LOG.info(s"Ready to change engine state!")
    if (HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) {
      System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }
    setCodeParser(new SQLCodeParser)
    super.init()
  }

  override def executeLine(engineExecutorContext: EngineExecutionContext, code: String): ExecuteResponse = {
    this.engineExecutorContext = engineExecutorContext
    if (engineExecutorContext.getEnableResultsetMetaWithTableName) {
      hiveConf.setBoolVar(ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES, true)
      info("set HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES true")
    } else {
      hiveConf.setBoolVar(ConfVars.HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES, false)
    }
    CSHiveHelper.setContextIDInfoToHiveConf(engineExecutorContext, hiveConf)
    singleSqlProgressMap.clear()
    singleCodeCompleted.set(false)
    currentSqlProgress = 0.0f
    val realCode = code.trim()
    LOG.info(s"hive client begins to run hql code:\n ${realCode.trim}")
    if (realCode.trim.length > 500) {
      engineExecutorContext.appendStdout(s"$getId >> ${realCode.trim.substring(0, 500)} ...")
    } else engineExecutorContext.appendStdout(s"$getId >> ${realCode.trim}")
    val tokens = realCode.trim.split("""\s+""")
    SessionState.setCurrentSessionState(sessionState)

    val proc = CommandProcessorFactory.get(tokens, hiveConf)
    this.proc = proc
    LOG.debug("ugi is " + ugi.getUserName)
    ugi.doAs(new PrivilegedExceptionAction[ExecuteResponse]() {
      override def run(): ExecuteResponse = {
        proc match {
          case any if HiveDriverProxy.isDriver(any) =>
            info(s"driver is $any")
            thread = Thread.currentThread()
            driver = new HiveDriverProxy(any)
            executeHQL(realCode, driver)
          case _ => val resp = proc.run(realCode.substring(tokens(0).length).trim)
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
        if (numberOfMRJobs > 0) engineExecutorContext.appendStdout(s"Your hive sql has $numberOfMRJobs MR jobs to do")
        val hiveResponse: CommandProcessorResponse = driver.run(realCode)
        if (hiveResponse.getResponseCode != 0) {
          LOG.error("Hive query failed, response code is {}", hiveResponse.getResponseCode)
          // todo check uncleared context ?
          return ErrorExecuteResponse(hiveResponse.getErrorMessage, hiveResponse.getException)
        }
        engineExecutorContext.appendStdout(s"Time taken: ${Utils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results.")
        LOG.info(s"$getId >> Time taken: ${Utils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results.")

        val fieldSchemas = if (hiveResponse.getSchema != null) hiveResponse.getSchema.getFieldSchemas
        else if (driver.getSchema != null) driver.getSchema.getFieldSchemas
        else throw HiveQueryFailedException(41005, "cannot get the field schemas.")

        LOG.debug("fieldSchemas are " + fieldSchemas)
        if (fieldSchemas == null || isNoResultSql(realCode)) {
          //IOUtils.closeQuietly(resultSetWriter)
          numberOfMRJobs = -1
          singleCodeCompleted.set(true)
          onComplete()
          singleSqlProgressMap.clear()
          return SuccessExecuteResponse()
        }
        //get column data
        val metaData: TableMetaData = getResultMetaData(fieldSchemas, engineExecutorContext.getEnableResultsetMetaWithTableName)
        //send result
        rows = sendResultSet(engineExecutorContext, driver, metaData)
        columnCount = if (fieldSchemas != null) fieldSchemas.size() else 0
        hasResult = true
      } catch {
        case e if HiveDriverProxy.isCommandNeedRetryException(e) => tryCount += 1
          needRetry = true
          HiveProgressHelper.clearHiveProgress()
          onComplete()
          singleSqlProgressMap.clear()
          clearCurrentProgress()
          HiveProgressHelper.storeSingleSQLProgress(0.0f)
          LOG.warn("Retry hive query with a different approach...")
        case t: Throwable => LOG.error(s"query failed, reason : ", t)
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

  private def sendResultSet(engineExecutorContext: EngineExecutionContext, driver: HiveDriverProxy, metaData: TableMetaData): Int = {
    val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    resultSetWriter.addMetaData(metaData)
    val colLength = metaData.columns.length
    val result = new util.ArrayList[String]()
    var rows = 0
    while (driver.getResults(result)) {
      val scalaResult: mutable.Buffer[String] = result
      scalaResult foreach { s =>
        val arr: Array[String] = s.split("\t")
        val arrAny: ArrayBuffer[Any] = new ArrayBuffer[Any]()
        if (arr.length > colLength) {
          logger.error(s"""hive code 查询的结果中有\t制表符，hive不能进行切割,请使用spark执行""")
          throw new ErrorException(60078, """您查询的结果中有\t制表符，hive不能进行切割,请使用spark执行""")
        }
        if (arr.length == colLength) arr foreach arrAny.add
        else if (arr.length == 0) for (i <- 1 to colLength) arrAny add ""
        else {
          val i = colLength - arr.length
          arr foreach arrAny.add
          for (i <- 1 to i) arrAny add ""
        }
        resultSetWriter.addRecord(new TableRecord(arrAny.toArray))
      }
      rows += result.size
      result.clear()
    }
    engineExecutorContext.sendResultSet(resultSetWriter)
    IOUtils.closeQuietly(resultSetWriter)
    rows
  }

  private def getResultMetaData(fieldSchemas: util.List[FieldSchema], useTableName: Boolean): TableMetaData = {
    var results: util.List[FieldSchema] = null
    val nameSet = new mutable.HashSet[String]()
    val cleanSchema = new util.ArrayList[FieldSchema]()
    fieldSchemas foreach {
      fieldSchema =>
        val name = fieldSchema.getName
        if (name.split('.').length == 2) {
          nameSet.add(name.split('.')(1))
          cleanSchema += new FieldSchema(name.split('.')(1), fieldSchema.getType, fieldSchema.getComment)
        }
    }
    if (nameSet.size < fieldSchemas.length) {
      results = fieldSchemas
    } else {
      if (useTableName) {
        results = fieldSchemas
      } else {
        results = cleanSchema
      }
    }

    val columns = results.map(result => Column(result.getName,
      DataType.toDataType(result.getType.toLowerCase()), result.getComment)).toArray[Column]
    val metaData = new TableMetaData(columns)
    metaData
  }


  private def isNoResultSql(sql: String): Boolean = {
    if (sql.trim.startsWith("create table") || sql.trim.startsWith("drop table")) true else false
  }


  /**
   * 在job完成之前，要将singleSqlProgressMap的剩余的内容全部变为成功
   */
  private def onComplete(): Unit = {

  }


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


  override def executeCompletely(engineExecutorContext: EngineExecutionContext, code: String, completedLine: String): ExecuteResponse = {
    val completeCode = code + completedLine
    executeLine(engineExecutorContext, completeCode)
  }

  override def close(): Unit = {
    singleSqlProgressMap.clear()
    Utils.tryAndWarnMsg(sessionState.close())("close session failed")
    super.close()
  }


  override def progress(taskID: String): Float = {
    if (engineExecutorContext != null) {
      val totalSQLs = engineExecutorContext.getTotalParagraph
      val currentSQL = engineExecutorContext.getCurrentParagraph
      val currentBegin = (currentSQL - 1) / totalSQLs.asInstanceOf[Float]
      HadoopJobExecHelper.runningJobs synchronized {
        HadoopJobExecHelper.runningJobs foreach {
          runningJob =>
            val name = runningJob.getID.toString
            val _progress = runningJob.reduceProgress() + runningJob.mapProgress()
            singleSqlProgressMap.put(name, _progress / 2)
        }
      }
      var totalProgress: Float = 0.0F
      val hiveRunJobs = if (numberOfMRJobs <= 0) 1 else numberOfMRJobs
      singleSqlProgressMap foreach {
        case (_name, _progress) => totalProgress += _progress
      }
      try {
        totalProgress = totalProgress / (hiveRunJobs * totalSQLs)
      } catch {
        case e: Exception => totalProgress = 0.0f
        case _ => totalProgress = 0.0f
      }

      logger.debug(s"hive progress is $totalProgress")
      if (totalProgress.isNaN || totalProgress.isInfinite) return currentBegin
      totalProgress + currentBegin
    } else 0.0f
  }

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = {
    val arrayBuffer: ArrayBuffer[JobProgressInfo] = new ArrayBuffer[JobProgressInfo]()
    singleSqlProgressMap synchronized {
      val set = singleSqlProgressMap.keySet()
      val tempSet = new util.HashSet[RunningJob](HadoopJobExecHelper.runningJobs)
      import scala.collection.JavaConverters._
      set.asScala foreach {
        key =>
          if (!tempSet.contains(key)) {
            arrayBuffer += JobProgressInfo(key, 200, 0, 0, 200)
          }
      }
    }

    HadoopJobExecHelper.runningJobs synchronized {
      HadoopJobExecHelper.runningJobs foreach {
        runningJob =>
          val succeedTask = ((runningJob.mapProgress() + runningJob.reduceProgress()) * 100).asInstanceOf[Int]
          if (succeedTask.equals(totalTask.asInstanceOf[Int]) || runningJob.isComplete || runningJob.isSuccessful) {
            arrayBuffer += JobProgressInfo(runningJob.getID.toString, totalTask.asInstanceOf[Int], 0, 0, totalTask.asInstanceOf[Int])
          } else {
            arrayBuffer += JobProgressInfo(runningJob.getID.toString, totalTask.asInstanceOf[Int], 1, 0, succeedTask)
          }
      }
    }
    arrayBuffer.toArray
  }


  override def killTask(taskID: String): Unit = {
    LOG.info(s"hive begins to kill job with id : ${taskID}")
    HadoopJobExecHelper.killRunningJobs()
    //Utils.tryQuietly(TezJobExecHelper.killRunningJobs())
    Utils.tryQuietly(HiveInterruptUtils.interrupt())
    if (null != thread) {
      Utils.tryAndWarn(thread.interrupt())
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
    val properties = EngineConnObject.getEngineCreationContext.getOptions
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      val settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
      if (!settingClientMemory.toLowerCase().endsWith("g")) {
        properties.put(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key, settingClientMemory + "g")
      }
    }
    val actualUsedResource = new LoadInstanceResource(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue(properties).toLong,
      EngineConnPluginConf.JAVA_ENGINE_REQUEST_CORES.getValue(properties), EngineConnPluginConf.JAVA_ENGINE_REQUEST_INSTANCE.getValue)
    val resource = new CommonNodeResource
    resource.setUsedResource(actualUsedResource)
    resource
  }

  override def getId(): String = namePrefix + id


}

class HiveDriverProxy(driver: Any) extends Logging {

  def getSchema(): Schema = {
    driver.getClass.getMethod("getSchema").invoke(driver).asInstanceOf[Schema]
  }

  def run(): CommandProcessorResponse = {
    driver.getClass.getMethod("run").invoke(driver).asInstanceOf[CommandProcessorResponse]
  }

  def run(command: String): CommandProcessorResponse = {
    driver.getClass.getMethod("run", classOf[String]).invoke(driver, command.asInstanceOf[AnyRef]).asInstanceOf[CommandProcessorResponse]
  }

  def setTryCount(retry: Int): Unit = {
    if (HiveDriverProxy.HAS_COMMAND_NEED_RETRY_EXCEPTION) {
      driver.getClass.getMethod("setTryCount", classOf[Int]).invoke(driver, retry.asInstanceOf[AnyRef])
    }
  }

  def getResults(res: util.List[_]): Boolean = {
    Utils.tryAndWarn {
      driver.getClass.getMethod("getResults", classOf[util.List[_]]).invoke(driver, res.asInstanceOf[AnyRef]).asInstanceOf[Boolean]
    }
  }

  def close(): Unit = {
    info("start to close driver")
    driver.getClass.getMethod("close").invoke(driver)
    driver.getClass.getMethod("destroy").invoke(driver)
    info("Finished to close driver")
  }

}


object HiveDriverProxy extends Logging {


  private val COMMAND_NEED_RETRY_EXCEPTION_CLASS_STR = "org.apache.hadoop.hive.ql.CommandNeedRetryException"
  private val IDRIVER_CLASS_STR = "org.apache.hadoop.hive.ql.IDriver"

  val COMMAND_NEED_RETRY_EXCEPTION_CLASS = Utils.tryQuietly {
    Thread.currentThread().getContextClassLoader
      .loadClass(COMMAND_NEED_RETRY_EXCEPTION_CLASS_STR)
  }
  val IDRIVER_CLASS = Utils.tryQuietly {
    Thread.currentThread().getContextClassLoader
      .loadClass(IDRIVER_CLASS_STR)
  }

  val HAS_COMMAND_NEED_RETRY_EXCEPTION: Boolean = COMMAND_NEED_RETRY_EXCEPTION_CLASS != null
  val HAS_IDRIVER: Boolean = IDRIVER_CLASS != null

  def isIDriver(any: Any): Boolean = HAS_IDRIVER && IDRIVER_CLASS.isInstance(any)

  def isDriver(any: Any): Boolean = isIDriver(any) || any.isInstanceOf[Driver]

  def isCommandNeedRetryException(any: Any): Boolean = HAS_COMMAND_NEED_RETRY_EXCEPTION && COMMAND_NEED_RETRY_EXCEPTION_CLASS.isInstance(any)

}
