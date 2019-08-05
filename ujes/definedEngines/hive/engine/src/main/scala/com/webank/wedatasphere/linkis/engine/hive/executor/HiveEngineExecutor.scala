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

package com.webank.wedatasphere.linkis.engine.hive.executor

import java.security.PrivilegedExceptionAction
import java.util
import java.util.concurrent.atomic.AtomicBoolean

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext, SQLCodeParser}
import com.webank.wedatasphere.linkis.engine.hive.common.HiveUtils
import com.webank.wedatasphere.linkis.engine.hive.exception.HiveQueryFailedException
import com.webank.wedatasphere.linkis.engine.hive.progress.HiveProgressHelper
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.resourcemanager.{LoadInstanceResource, Resource}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.domain.{Column, DataType}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hadoop.hive.metastore.api.FieldSchema
import org.apache.hadoop.hive.ql.exec.Utilities
import org.apache.hadoop.hive.ql.exec.mr.HadoopJobExecHelper
import org.apache.hadoop.hive.ql.parse.{ParseDriver, ParseUtils, SemanticAnalyzerFactory}
import org.apache.hadoop.hive.ql.processors.{CommandProcessor, CommandProcessorFactory, CommandProcessorResponse}
import org.apache.hadoop.hive.ql.session.SessionState
import org.apache.hadoop.hive.ql.{CommandNeedRetryException, Context, Driver, QueryPlan}
import org.apache.hadoop.mapred.RunningJob
import org.apache.hadoop.security.UserGroupInformation
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * created by cooperyang on 2018/11/22
  * Description:
  */
class HiveEngineExecutor(outputPrintLimit:Int,
                         sessionState: SessionState,
                         ugi:UserGroupInformation,
                         hiveConf:HiveConf)
  extends EngineExecutor(outputPrintLimit, isSupportParallelism = false) with SingleTaskOperateSupport with SingleTaskInfoSupport {

  private val LOG = LoggerFactory.getLogger(getClass)

  private val nameSuffix:String = "_HiveEngineExecutor"

  private val name:String = Sender.getThisServiceInstance.getInstance

  private var initialized:Boolean = false

  private var proc:CommandProcessor = _

  private var totalProgress:Float = 0.0f

  private var map:Int = 0

  private var reduce:Int = 0

  private val totalTask = 200.0f

  private var singleLineProgress:Float = 0.0f

  private var stage:Int = 0

  private var engineExecutorContext:EngineExecutorContext = _

  private var singleCodeCompleted:AtomicBoolean = new AtomicBoolean(false)

  private var numberOfMRJobs:Int = -1

  private var currentSqlProgress:Float = 0.0f

  private val singleSqlProgressMap:util.Map[String, Float] = new util.HashMap[String, Float]()


  override def getName: String = name

  override def init():Unit = {
    transition(ExecutorState.Idle)
    LOG.info(s"Ready to change engine state!")
    setCodeParser(new SQLCodeParser)
    super.init()
  }

  override def getActualUsedResources: Resource = {
    new LoadInstanceResource(Runtime.getRuntime.totalMemory() - Runtime.getRuntime.freeMemory(), 2, 1)
  }


  override def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = {
    //Clear the data of singleSqlMap(清空singleSqlMap的数据)
    singleSqlProgressMap.clear()
    singleCodeCompleted.set(false)
    currentSqlProgress = 0.0f
    var realCode = code.trim()
    while(realCode.startsWith("\n")) realCode = StringUtils.substringAfter(realCode, "\n")
    LOG.info(s"hive client begins to run hql code:\n ${realCode.trim}")
    if (realCode.trim.length > 500){
      engineExecutorContext.appendStdout(s"$getName >> ${realCode.trim.substring(0, 500)} ...")
    } else engineExecutorContext.appendStdout(s"$getName >> ${realCode.trim}")
    val tokens = realCode.trim.split("""\s+""")
    //if(!initialized){
      SessionState.setCurrentSessionState(sessionState)
     // initialized = true
   // }
    this.engineExecutorContext = engineExecutorContext
    val proc = CommandProcessorFactory.get(tokens, hiveConf)
    this.proc = proc
    LOG.debug("ugi is " + ugi.getUserName)
    ugi.doAs(new PrivilegedExceptionAction[ExecuteResponse]() {
      override def run(): ExecuteResponse = {
        proc match {
          case driver:Driver =>
            var needRetry:Boolean = true
            var tryCount:Int = 0
            var hasResult:Boolean = false
            var rows:Int = 0
            var columnCount:Int = 0
            while(needRetry){
              needRetry = false
              driver.setTryCount(tryCount + 1)
              val startTime = System.currentTimeMillis()
              try{
                //to get hive query plan
//                val context = new Context(hiveConf)
//                val parseDriver = new ParseDriver
//                val tree = ParseUtils.findRootNonNullToken(parseDriver.parse(realCode, context))
//                val sem = SemanticAnalyzerFactory.get(hiveConf, tree)
//                sem.analyze(tree, context)
//                sem.validate()
//                val queryPlan = new QueryPlan(realCode, sem, 0L, null, "query")
//                val numberOfJobs = Utilities.getMRTasks(queryPlan.getRootTasks).size
                numberOfMRJobs = 1
//                queryPlan.getRootTasks foreach {task =>
//                  LOG.info(s"MR job jobID ${task.getJobID} and job is ${task.getMapWork.size()}")
//                }
//                LOG.info(s"$realCode has $numberOfJobs MR jobs to do")
//                if (numberOfJobs != 0) engineExecutorContext.appendStdout(s"Your hive sql has $numberOfJobs MR jobs to do")
                val hiveResponse:CommandProcessorResponse = driver.run(realCode)
                if (hiveResponse.getResponseCode != 0) {
                  LOG.error("Hive query failed, reason: ", hiveResponse.getException)
                  val errorException = HiveQueryFailedException(41004, "hive query failed:" + hiveResponse.getErrorMessage)
                  errorException.initCause(hiveResponse.getException)
                  throw errorException
                }
                engineExecutorContext.appendStdout(s"Time taken: ${HiveUtils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results.")
                LOG.info(s"$getName >> Time taken: ${HiveUtils.msDurationToString(System.currentTimeMillis() - startTime)}, begin to fetch results.")
                val fieldSchemas = if (hiveResponse.getSchema != null) hiveResponse.getSchema.getFieldSchemas
                else if (driver.getSchema != null) driver.getSchema.getFieldSchemas
                else throw HiveQueryFailedException(41005, "cannot get the field schemas.")
                LOG.debug("fieldSchemas are " + fieldSchemas)
                if (fieldSchemas == null) {
                  //IOUtils.closeQuietly(resultSetWriter)
                  numberOfMRJobs = -1
                  singleCodeCompleted.set(true)
                  singleSqlProgressMap.clear()
                  return SuccessExecuteResponse()
                }
                hasResult = true
                import scala.collection.JavaConversions._
                val scalaFieldSchemas:scala.collection.mutable.Buffer[FieldSchema] = fieldSchemas
                LOG.debug("Scala field Schemas are " + scalaFieldSchemas.mkString(" "))
                val columns = scalaFieldSchemas.map(fieldSchema => Column(justFieldName(fieldSchema.getName),
                  DataType.toDataType(fieldSchema.getType.toLowerCase()), fieldSchema.getComment)).toArray[Column]
                val metaData = new TableMetaData(columns)
                val resultSetWriter = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
                resultSetWriter.addMetaData(metaData)
                val result = new util.ArrayList[String]()
                while(driver.getResults(result)){
                  val scalaResult:scala.collection.mutable.Buffer[String] = result
                  scalaResult foreach { s =>
                    val arr:Array[String] = s.split("\t")
                    val arrAny:ArrayBuffer[Any] = new ArrayBuffer[Any]()
                    if (arr.length != 0) arr foreach arrAny.add else for(i <-1 to columns.length) arrAny add ""
                    resultSetWriter.addRecord(new TableRecord(arrAny.toArray))
                  }
                  rows += result.size
                  result.clear()
                  rows += result.size
                  result.clear()
                }
                columnCount = if (fieldSchemas != null) fieldSchemas.size() else 0
                engineExecutorContext.sendResultSet(resultSetWriter)
                IOUtils.closeQuietly(resultSetWriter)
              }catch{
                case e:CommandNeedRetryException =>  tryCount += 1
                  needRetry = true
                  HiveProgressHelper.clearHiveProgress()
                  singleSqlProgressMap.clear()
                  clearCurrentProgress()
                  HiveProgressHelper.storeSingleSQLProgress(0.0f)
                  LOG.warn("Retry hive query with a different approach...")
                case t:Throwable => LOG.error(s"query failed, reason:",t)
                  HiveProgressHelper.clearHiveProgress()
                  clearCurrentProgress()
                  HiveProgressHelper.storeSingleSQLProgress(0.0f)
                  singleCodeCompleted.set(true)
                  numberOfMRJobs = -1
                  singleSqlProgressMap.clear()
                  return ErrorExecuteResponse(t.getMessage, t)
              }
            }
            if (hasResult) {
              engineExecutorContext.appendStdout(s"Fetched  $columnCount col(s) : $rows row(s) in hive")
              LOG.info(s"$getName >> Fetched  $columnCount col(s) : $rows row(s) in hive")
            }
            clearCurrentProgress()
            HiveProgressHelper.clearHiveProgress()
            HiveProgressHelper.storeSingleSQLProgress(0.0f)
            singleCodeCompleted.set(true)
            numberOfMRJobs = -1
            singleSqlProgressMap.clear()
            SuccessExecuteResponse()
          case _ =>  val resp = proc.run(realCode.substring(tokens(0).length).trim)
            if(resp.getResponseCode != 0) {
              clearCurrentProgress()
              HiveProgressHelper.clearHiveProgress()
              singleSqlProgressMap.clear()
              HiveProgressHelper.storeSingleSQLProgress(0.0f)
              throw resp.getException
            }
            clearCurrentProgress()
            HiveProgressHelper.clearHiveProgress()
            HiveProgressHelper.storeSingleSQLProgress(0.0f)
            singleSqlProgressMap.clear()
            SuccessExecuteResponse()
        }
      }
    })
  }

  private def clearCurrentProgress():Unit = {
    reduce = 0
    map = 0
    singleLineProgress = 0.0f
  }


  private def justFieldName(schemaName:String):String = {
    LOG.debug("schemaName: " + schemaName)
    val arr = schemaName.split("\\.")
    //if (arr.length == 2) LOG.info("justFieldName: " + arr(1))
    if (arr.length == 2) arr(1) else schemaName
  }


  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = {
    val completeCode = code + completedLine
    executeLine(engineExecutorContext, completeCode)
  }

  override def close(): Unit = {
    singleSqlProgressMap.clear()
    Utils.tryAndWarnMsg(sessionState.close())("close session failed")
  }


  override def progress(): Float = {
    if (engineExecutorContext != null){
      val totalSQLs = engineExecutorContext.getTotalParagraph
      val currentSQL = engineExecutorContext.getCurrentParagraph
      val currentBegin = (currentSQL - 1) / totalSQLs.asInstanceOf[Float]
//      val number = HadoopJobExecHelper.runningJobs.length
//      //logger.info(s"number is $number" )
//      if (number == 0) {
//        logger.info(s"push progress is $currentBegin")
//        return currentBegin
//      }

      HadoopJobExecHelper.runningJobs synchronized {
        HadoopJobExecHelper.runningJobs foreach {
          runningJob => val name = runningJob.getID.toString
            val _progress = runningJob.reduceProgress() + runningJob.mapProgress()
            singleSqlProgressMap.put(name, _progress / 2)
        }
      }
      var totalProgress:Float = 0.0F
      singleSqlProgressMap foreach {
        case (_name, _progress) => totalProgress += _progress
      }
      try{
        totalProgress = totalProgress / (numberOfMRJobs * totalSQLs)
      }catch{
        case e:Exception => totalProgress = 0.0f
        case _ => totalProgress = 0.0f
      }

      //HadoopJobExecHelper.runningJobs.foreach(runningJob => totalProgress += (runningJob.mapProgress() + runningJob.reduceProgress()))
      logger.info(s"hive progress is $totalProgress")
      if (totalProgress.isNaN) return 0.0f
      totalProgress + currentBegin
    }else 0.0f
//    if (engineExecutorContext != null){
//      val totalSQLs = engineExecutorContext.getTotalParagraph
//      val currentSQL = engineExecutorContext.getCurrentParagraph
//      val currentBegin = (currentSQL - 1) / totalSQLs.asInstanceOf[Float]
//      val hiveProgressQueue =  HiveProgressHelper.getHiveProgress
//      var hiveProgress:HiveProgress = null
//      while(hiveProgressQueue.size() > 0){
//        hiveProgress = hiveProgressQueue.take()
//      }
//      if (hiveProgress != null){
//        this.map = hiveProgress.getMap
//        this.reduce = hiveProgress.getReduce
//      }
//      //todo 先进行第一个
//      val progress = (this.map + this.reduce) / totalTask
//      //LOG.info(s"end to get the progress in hive map is $map and reduce is $reduce, progress is $progress")
//      if (HiveProgressHelper.getSingleSQLProgress.equals(1.0f)) {
//        HiveProgressHelper.storeSingleSQLProgress(0.0f)
//        currentBegin + 1.0f / totalSQLs
//      } else {
//        currentBegin + (this.map + this.reduce) / totalTask / totalSQLs
//      }
//    }else 0.0f
  }

  override def getProgressInfo: Array[JobProgressInfo] = {
    //LOG.info("begin to get progressinfo")
    val arrayBuffer:ArrayBuffer[JobProgressInfo] = new ArrayBuffer[JobProgressInfo]()
    if (HadoopJobExecHelper.runningJobs.isEmpty) return arrayBuffer.toArray
    singleSqlProgressMap synchronized{
      val set = singleSqlProgressMap.keySet()
      val tempSet = new util.HashSet[RunningJob](HadoopJobExecHelper.runningJobs)
      import scala.collection.JavaConverters._
      set.asScala foreach {
        key => if (!tempSet.contains(key)){
          arrayBuffer += JobProgressInfo(key, 200, 0, 0, 200)
        }
      }
    }

    HadoopJobExecHelper.runningJobs synchronized {
      HadoopJobExecHelper.runningJobs foreach {
        runningJob => val succeedTask = ((runningJob.mapProgress() + runningJob.reduceProgress()) * 100).asInstanceOf[Int]
          if (succeedTask.equals(totalTask.asInstanceOf[Int]) || runningJob.isComplete || runningJob.isSuccessful){
            arrayBuffer += JobProgressInfo(runningJob.getID.toString, totalTask.asInstanceOf[Int], 0, 0, totalTask.asInstanceOf[Int])
          }else{
            arrayBuffer += JobProgressInfo(runningJob.getID.toString, totalTask.asInstanceOf[Int], 1, 0, succeedTask)
          }
      }
    }

//    val succeedTask = ((runningJob.mapProgress() + runningJob.reduceProgress()) * 100).asInstanceOf[Int]
//    if (succeedTask.equals(totalTask.asInstanceOf[Int])){
//      arrayBuffer += JobProgressInfo(runningJob.getJobName, totalTask.asInstanceOf[Int], 0, 0, succeedTask)
//    }else{
//      arrayBuffer += JobProgressInfo(runningJob.getJobName, totalTask.asInstanceOf[Int], 1, 0, succeedTask)
//    }
    arrayBuffer.toArray



//    if (StringUtils.isNotEmpty(HiveProgressHelper.getAppid)){
//      if (totalTask.asInstanceOf[Int] == reduce + map){
//        arrayBuffer += JobProgressInfo(HiveProgressHelper.getAppid, totalTask.asInstanceOf[Int], 0, 0, map+reduce)
//      } else{
//        arrayBuffer += JobProgressInfo(HiveProgressHelper.getAppid, totalTask.asInstanceOf[Int], 1, 0, map+reduce)
//      }
//    }
//    arrayBuffer.toArray
  }

  override def log(): String = ""

  override def kill(): Boolean = {
    LOG.info("hive begins to kill job")
    HadoopJobExecHelper.killRunningJobs()
    clearCurrentProgress()
    singleSqlProgressMap.clear()
    HiveProgressHelper.clearHiveProgress()
    LOG.info("hive killed job successfully")
    true
  }

  override def pause(): Boolean = {
    true
  }

  override def resume(): Boolean = {
    true
  }
}
