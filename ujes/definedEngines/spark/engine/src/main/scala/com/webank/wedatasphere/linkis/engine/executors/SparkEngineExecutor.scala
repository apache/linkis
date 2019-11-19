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

package com.webank.wedatasphere.linkis.engine.executors

import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import com.webank.wedatasphere.linkis.common.utils.{ByteTimeUtils, Logging, Utils}
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.engine.exception.{NoSupportEngineException, SparkEngineException}
import com.webank.wedatasphere.linkis.engine.execute.{EngineExecutor, EngineExecutorContext}
import com.webank.wedatasphere.linkis.engine.extension.{SparkPostExecutionHook, SparkPreExecutionHook}
import com.webank.wedatasphere.linkis.engine.spark.common._
import com.webank.wedatasphere.linkis.engine.spark.utils.{EngineUtils, JobProgressUtil}
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo
import com.webank.wedatasphere.linkis.resourcemanager.{DriverAndYarnResource, LoadInstanceResource, YarnResource}
import com.webank.wedatasphere.linkis.scheduler.exception.DWCJobRetryException
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorState.{ExecutorState => _}
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.domain.{Column, DataType}
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.lang.StringUtils
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{StructField, StructType}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by allenlliu on 2019/4/8.
  */
class SparkEngineExecutor(val sc: SparkContext, id: Long, outputPrintLimit: Int, sparkExecutors: Seq[SparkExecutor])
  extends EngineExecutor(outputPrintLimit, false) with SingleTaskOperateSupport with SingleTaskInfoSupport with Logging {
  val queryNum = new AtomicLong(0)
  private var jobGroup: String = _

  private var oldprogress:Float = 0f

  override def init() = {
    info(s"Ready to change engine state!")
    //    this.setCodeParser(new SparkCombinedCodeParser())
    val heartbeat = Utils.defaultScheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        if (sc.isStopped) {
          error("Spark application has already stopped, please restart a new engine.")
          transition(ExecutorState.Error)
        }
      }
    }, 1000, 3000, TimeUnit.MILLISECONDS)
    super.init()
  }

  override def getName: String = sc.appName

  //Runtime resources, because of the time constraints, so take it directly in the conf（运行时资源 这里由于时间紧迫，所以先直接在conf里面拿）
  override def getActualUsedResources: DriverAndYarnResource = {
    info("Begin to get actual used resources!")
    Utils.tryCatch({
      //      val driverHost: String = sc.getConf.get("spark.driver.host")
      //      val executorMemList = sc.getExecutorMemoryStatus.filter(x => !x._1.split(":")(0).equals(driverHost)).map(x => x._2._1)
      val executorNum: Int = sc.getConf.get("spark.executor.instances").toInt
      val executorMem: Long = ByteTimeUtils.byteStringAsBytes(sc.getConf.get("spark.executor.memory")) * executorNum

      //      if(executorMemList.size>0) {
      //        executorMem = executorMemList.reduce((x, y) => x + y)
      //      }
      val driverMem: Long = ByteTimeUtils.byteStringAsBytes(sc.getConf.get("spark.driver.memory"))
      //      val driverMemList = sc.getExecutorMemoryStatus.filter(x => x._1.split(":")(0).equals(driverHost)).map(x => x._2._1)
      //      if(driverMemList.size > 0) {
      //          driverMem = driverMemList.reduce((x, y) => x + y)
      //      }
      val sparkExecutorCores = sc.getConf.get("spark.executor.cores").toInt * executorNum
      val sparkDriverCores = sc.getConf.get("spark.driver.cores", "1").toInt
      val queue = sc.getConf.get("spark.yarn.queue")
      info("Current actual used resources is driverMem:" + driverMem + ",driverCores:" + sparkDriverCores + ",executorMem:" + executorMem + ",executorCores:" + sparkExecutorCores + ",queue:" + queue)
      new DriverAndYarnResource(
        new LoadInstanceResource(driverMem, sparkDriverCores, 1),
        new YarnResource(executorMem, sparkExecutorCores, 0, queue, sc.applicationId)
      )
    })(t => {
      warn("Get actual used resource exception", t)
      null
    })
  }

  private[executors] var engineExecutorContext: EngineExecutorContext = _

  override protected def executeLine(engineExecutorContext: EngineExecutorContext, code: String): ExecuteResponse = Utils.tryFinally {
    if (sc.isStopped) {
      error("Spark application has already stopped, please restart it.")
      transition(ExecutorState.Error)
      throw new DWCJobRetryException("Spark application sc has already stopped, please restart it.")
    }
    this.engineExecutorContext = engineExecutorContext
    oldprogress = 0f
    val runType = engineExecutorContext.getProperties.get("runType").asInstanceOf[String]
    var kind: Kind = null
    if (StringUtils.isNotBlank(runType)) {
      runType.toLowerCase match {
        case "python" | "py" | "pyspark" => kind = PySpark()
        case "sql" | "sparksql" => kind = SparkSQL()
        case "scala" |SparkKind.FUNCTION_MDQ_TYPE => kind = SparkScala()
        case _ => kind = SparkSQL()
      }
    }
    var preCode = code
    //Pre-execution hook
    SparkPreExecutionHook.getSparkPreExecutionHooks().foreach(hook => preCode = hook.callPreExecutionHook(engineExecutorContext,preCode))
    val _code = Kind.getRealCode(preCode)
    info(s"Ready to run code with kind $kind.")
    jobGroup = String.valueOf("dwc-spark-mix-code-" + queryNum.incrementAndGet())
    //    val executeCount = queryNum.get().toInt - 1
    info("Set jobGroup to " + jobGroup)
    sc.setJobGroup(jobGroup, _code, true)

    val response = Utils.tryFinally(sparkExecutors.find(_.kind == kind).map(_.execute(this, _code, engineExecutorContext, jobGroup)).getOrElse(throw new NoSupportEngineException(40008, s"Not supported $kind executor!"))) {
      this.engineExecutorContext.pushProgress(1, getProgressInfo)
      jobGroup = null
      sc.clearJobGroup()
    }
    //Post-execution hook
    SparkPostExecutionHook.getSparkPostExecutionHooks().foreach(_.callPostExecutionHook(engineExecutorContext,response,code))
    response
  }{
    this.engineExecutorContext = null
    oldprogress = 0f
  }

  override protected def executeCompletely(engineExecutorContext: EngineExecutorContext, code: String, completedLine: String): ExecuteResponse = {
    val newcode = completedLine + code
    info("newcode is " + newcode)
    executeLine(engineExecutorContext, newcode)
  }


  override def createEngineExecutorContext(executeRequest: ExecuteRequest): EngineExecutorContext = {
    val engineExecutorContext = super.createEngineExecutorContext(executeRequest)
    executeRequest match {
      case runTypeExecuteRequest: RunTypeExecuteRequest => engineExecutorContext.addProperty("runType", runTypeExecuteRequest.runType)
      case _ =>
    }
    engineExecutorContext
  }

  override def pause(): Boolean = false

  override def kill(): Boolean = {
    if (!sc.isStopped) {
      sc.cancelAllJobs
      true
    } else {
      false
    }
  }

  override def resume(): Boolean = false

  override def progress(): Float = if (jobGroup == null || engineExecutorContext.getTotalParagraph == 0) 0
  else {
    debug("request new progress for jobGroup is " + jobGroup + "old progress:" + oldprogress)
    val newProgress = (engineExecutorContext.getCurrentParagraph * 1f - 1f )/ engineExecutorContext.getTotalParagraph + JobProgressUtil.progress(sc,jobGroup)/engineExecutorContext.getTotalParagraph - 0.01f
    if(newProgress < oldprogress && oldprogress < 0.98) oldprogress else {
      oldprogress = newProgress
      newProgress
    }
  }

  override def getProgressInfo: Array[JobProgressInfo] = if (jobGroup == null) Array.empty
  else {
    debug("request new progress info for jobGroup is " + jobGroup)
    val progressInfoArray = ArrayBuffer[JobProgressInfo]()
    progressInfoArray ++= JobProgressUtil.getActiveJobProgressInfo(sc,jobGroup)
    progressInfoArray ++= JobProgressUtil.getCompletedJobProgressInfo(sc,jobGroup)
    progressInfoArray.toArray
  }

  override def log(): String = {
    ""
  }

  override def close(): Unit = {
    sparkExecutors foreach {
      executor => Utils.tryCatch(executor.close){
        case e:Exception => logger.error("c;pse")
      }
    }
  }
}

object SQLSession extends Logging {
  val nf = NumberFormat.getInstance()
  nf.setGroupingUsed(false)
  nf.setMaximumFractionDigits(SparkConfiguration.SPARK_NF_FRACTION_LENGTH.getValue)

  def showDF(sc: SparkContext, jobGroup: String, df: Any, alias: String, maxResult: Int, engineExecutorContext: EngineExecutorContext): Unit = {
    //
    if (sc.isStopped) {
      error("Spark application has already stopped in showDF, please restart it.")
      throw new DWCJobRetryException("Spark application sc has already stopped, please restart it.")
    }
    val startTime = System.currentTimeMillis()
    //    sc.setJobGroup(jobGroup, "Get IDE-SQL Results.", false)
    val dataFrame = df.asInstanceOf[DataFrame]
    val iterator = Utils.tryThrow(dataFrame.toLocalIterator) { t =>
      throw new SparkEngineException(40002, s"dataFrame to local exception", t)
    }
    //var columns: List[Attribute] = null
    // get field names
    //logger.info("SCHEMA BEGIN")
    import java.util
    val colSet = new util.HashSet[String]()
    val schema = dataFrame.schema
    var columnsSet:StructType = null
    schema foreach (s => colSet.add(s.name))
    if (colSet.size() < schema.size){
      val arr:ArrayBuffer[StructField] = new ArrayBuffer[StructField]()
      dataFrame.queryExecution.analyzed.output foreach {
        attri => val tempAttri = StructField(attri.qualifiedName, attri.dataType, attri.nullable, attri.metadata)
          arr += tempAttri
      }
      columnsSet = StructType(arr.toArray)
    }else{
      columnsSet = schema
    }
    //val columnsSet = dataFrame.schema
    val columns = columnsSet.map(c =>
      Column(c.name, DataType.toDataType(c.dataType.typeName.toLowerCase), c.getComment().orNull)).toArray[Column]
    if (columns == null || columns.isEmpty) return
    val metaData = new TableMetaData(columns)
    val writer = if (StringUtils.isNotBlank(alias))
      engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE, alias)
    else engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    writer.addMetaData(metaData)
    var index = 0
    Utils.tryThrow({
      while (index < maxResult && iterator.hasNext) {
        val row = iterator.next()
        val r: Array[Any] = columns.indices.map { i =>
          val data = row(i) match {
            case value: String => value.replaceAll("\n|\t", " ")
            case value: Double => nf.format(value)
            case value: Any => value.toString
            case _ => null
          }
          data
        }.toArray
        writer.addRecord(new TableRecord(r))
        index += 1
      }
    }) { t =>
      throw new SparkEngineException(40001, s"read record  exception", t)
    }
    warn(s"Time taken: ${System.currentTimeMillis() - startTime}, Fetched $index row(s).")
    engineExecutorContext.appendStdout(s"${EngineUtils.getName} >> Time taken: ${System.currentTimeMillis() - startTime}, Fetched $index row(s).")
    engineExecutorContext.sendResultSet(writer)
  }

  def showHTML(sc: SparkContext, jobGroup: String, htmlContent: Any, engineExecutorContext: EngineExecutorContext): Unit = {
    val startTime = System.currentTimeMillis()
    val writer = engineExecutorContext.createResultSetWriter(ResultSetFactory.HTML_TYPE)
    val metaData = new LineMetaData(null)
    writer.addMetaData(metaData)
    writer.addRecord(new LineRecord(htmlContent.toString))
    warn(s"Time taken: ${System.currentTimeMillis() - startTime}")

    engineExecutorContext.sendResultSet(writer)
  }
}

