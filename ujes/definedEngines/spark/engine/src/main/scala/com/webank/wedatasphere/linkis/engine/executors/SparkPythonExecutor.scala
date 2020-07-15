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

import java.io._
import java.util
import java.util.concurrent.atomic.AtomicLong

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.Interpreter.PythonInterpreter._
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration._
import com.webank.wedatasphere.linkis.engine.exception.ExecuteError
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.imexport.CsvRelation
import com.webank.wedatasphere.linkis.engine.rs.RsOutputStream
import com.webank.wedatasphere.linkis.engine.spark.common.PySpark
import com.webank.wedatasphere.linkis.engine.spark.utils.EngineUtils
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import org.apache.commons.exec.CommandLine
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.spark.SparkContext
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}
import py4j.GatewayServer

import scala.collection.JavaConversions._
import scala.collection.mutable.StringBuilder
import scala.concurrent._
import scala.concurrent.duration.Duration

/**
  * Created by allenlliu on 2018/11/19.
  */
class SparkPythonExecutor(val sc: SparkContext,  val sqlContext: SQLContext,session: SparkSession) extends SparkExecutor {
  private var gatewayServer: GatewayServer = _
  private var process: Process = _
  private var jobGroup:String = _
  private val queryNum = new AtomicLong(0)
  private val queryLock = new Array[Byte](0)
  private var code: String = _
  private var pythonScriptInitialized = false
  private[this] var promise: Promise[String] = _
  private implicit val executor: ExecutionContextExecutor = ExecutionContext.global

  implicit var sparkSession: SparkSession = session
  private[executors] var engineExecutorContext: EngineExecutorContext = _
  private val lineOutputStream = new RsOutputStream

  val SUCCESS = "success"
  @throws(classOf[IOException])
  override def open = {}

  def getSparkConf = sc.getConf

  def getJavaSparkContext = new JavaSparkContext(sc)

  def getSparkSession = if(sparkSession != null) sparkSession else () => throw new IllegalAccessException("not supported keyword spark in spark1.x versions")


  override def close = {
    IOUtils.closeQuietly(lineOutputStream)
    if (gatewayServer != null) {
      gatewayServer = null
    }
    if (process != null) {
      process.destroy()
      process = null
    }
  }

  override def kind = PySpark()

  private def initGateway = {
    //    _queue = new LinkedBlockingQueue[ExecuteRequest]
    val pythonExec = CommonVars("PYSPARK_DRIVER_PYTHON", "python").getValue

    val pythonScriptPath = CommonVars("python.script.path", "python/mix_pyspark.py").getValue

    val port = EngineUtils.findAvailPort
    gatewayServer = new GatewayServer(this, port)
    gatewayServer.start()

    info("Pyspark process file path is: " + getClass.getClassLoader.getResource(pythonScriptPath).toURI)
    val pythonClasspath = new StringBuilder(pythonPath)

    //
     val files = sc.getConf.get("spark.files", "")
     info("output spark files "+ sc.getConf.get("spark.files", ""))
     if(StringUtils.isNotEmpty(files)) {
       pythonClasspath ++= File.pathSeparator ++= files.split(",").filter(_.endsWith(".zip")).mkString(File.pathSeparator)
     }
    //extra python package
    val pyFiles = sc.getConf.get("spark.application.pyFiles", "")

    //add class path zip
    val classPath = CommonVars("java.class.path", "").getValue
    classPath.split(";").filter(_.endsWith(".zip")).foreach(pythonClasspath ++= File.pathSeparator ++= _)

    val cmd = CommandLine.parse(pythonExec)
    cmd.addArgument(createFakeShell(pythonScriptPath).getAbsolutePath, false)
    cmd.addArgument(port.toString, false)
    cmd.addArgument(EngineUtils.sparkSubmitVersion().replaceAll("\\.", ""), false)
    cmd.addArgument(pythonClasspath.toString(), false)
    cmd.addArgument(pyFiles, false)

    val builder = new ProcessBuilder(cmd.toStrings.toSeq)

    val env = builder.environment()
    env.put("PYTHONPATH", pythonClasspath.toString())
    env.put("PYTHONUNBUFFERED", "YES")
    env.put("PYSPARK_GATEWAY_PORT", "" + port)
    env.put("SPARK_HOME", SparkConfiguration.SPARK_HOME.getValue)
    //    builder.redirectError(Redirect.INHERIT)
    info("pyspark builder command:" + builder.command().mkString(" "))
    builder.redirectErrorStream(true)
    builder.redirectInput(ProcessBuilder.Redirect.PIPE)
    process = builder.start()
    Future {
      val exitCode = process.waitFor()
      info("Pyspark process  has stopped with exit code " + exitCode)
      //      close
      Utils.tryFinally({
        if (promise != null && !promise.isCompleted) {
          /*val out = outputStream.toString
          if (StringUtils.isNotEmpty(out)) promise.failure(new ExecuteError(30034,out))
          else*/
          promise.failure(new ExecuteError(40007,"Pyspark process  has stopped, query failed!"))
        }
      }) {
        close
      }
    }
    // Wait up to 30 seconds（最多等待30秒）
    Utils.waitUntil(() => pythonScriptInitialized, SPARK_LANGUAGE_REPL_INIT_TIME.getValue.toDuration)
  }

  override def execute(sparkEngineExecutor: SparkEngineExecutor, code: String, engineExecutorContext: EngineExecutorContext,jobGroup:String): ExecuteResponse = {
    if(engineExecutorContext != this.engineExecutorContext){
      this.engineExecutorContext = engineExecutorContext
      lineOutputStream.reset(engineExecutorContext)
      lineOutputStream.ready()
//      info("Spark scala executor reset new engineExecutorContext!")
    }
    lazyInitGageWay()
    this.jobGroup= jobGroup
    executeLine(code)
  }

  def lazyInitGageWay(): Unit = {
    if (process == null) {
      Utils.tryThrow(initGateway) { t => {
        error("initialize python executor failed, please ask administrator for help!",t)
        throw t
      }
      }
    }
  }

  def executeLine(code: String): ExecuteResponse = {
    if (sc.isStopped) {
      throw new IllegalStateException("Application has been stopped, please relogin to try it.")
    }
    if (!pythonScriptInitialized) {
      throw new IllegalStateException("Pyspark process cannot be initialized, please ask administrator for help.")
    }
    promise = Promise[String]()
    this.code = code
    queryLock synchronized queryLock.notify()
    Await.result(promise.future, Duration.Inf)
    lineOutputStream.flush()
    engineExecutorContext.appendStdout(s"${EngineUtils.getName} >> $code")
    val outStr = lineOutputStream.toString()
    if(StringUtils.isEmpty(outStr) && ResultSetFactory.getInstance.isResultSet(outStr)) {
      val output = Utils.tryQuietly(ResultSetWriter.getRecordByRes(outStr, SPARK_CONSOLE_OUTPUT_NUM.getValue))
      val res = output.map(x => x.toString).toList.mkString("\n")
      if (res.length > 0) {
        engineExecutorContext.appendStdout(res)
      }
    }
    SuccessExecuteResponse()
  }

  def onPythonScriptInitialized = {
    pythonScriptInitialized = true
    info("Pyspark process has been initialized.")
  }

  def getStatements = {
    queryLock synchronized {while(code == null) queryLock.wait()}
    info("Prepare to deal python code, code: " + code)
//    lineOutputStream.reset(this.engineExecutorContext)
    val request = PythonInterpretRequest(code, jobGroup)
    code = null
    request
  }

  def setStatementsFinished(out: String, error: Boolean) = {
    info(s"A python code finished, has some errors happened?  $error.")
    Utils.tryQuietly(Thread.sleep(10))
    sc.clearJobGroup()
    if(! error) {
      promise.success(SUCCESS)
    } else {
      promise.failure(new ExecuteError(40003,out))
    }
  }
  def appendOutput(message: String) = {
    if(!pythonScriptInitialized) {
      info(message)
    } else {
      lineOutputStream.write(message.getBytes("utf-8"))
    }
  }

  def appendErrorOutput(message: String) = {
    if(!pythonScriptInitialized) {
      info(message)
    } else {
      error(message)
      engineExecutorContext.appendStdout(message)
    }
  }

  def showDF(jobGroup: String, df: Any) = {
    SQLSession.showDF(sc,jobGroup,df,null,5000,this.engineExecutorContext)
    info("Pyspark showDF execute success!")
  }

  def showAliasDF(jobGroup: String, df: Any, alias:String) = {
    SQLSession.showDF(sc,jobGroup,df,alias,5000,this.engineExecutorContext)
    info("Pyspark showDF execute success!")
  }

  def showHTML(jobGroup: String, htmlContent: Any)={
    SQLSession.showHTML(sc,jobGroup,htmlContent,this.engineExecutorContext)
    info("Pyspark showHTML execute success!")
  }

  import scala.collection.JavaConverters._
  def saveDFToCsv(df: Any, path: String, hasHeader: Boolean = true ,
                  isOverwrite: Boolean = false, option: util.Map[String, Any] = new util.HashMap()): Boolean ={
    CsvRelation.saveDFToCsv(sparkSession,df.asInstanceOf[DataFrame],path, hasHeader,isOverwrite, option.asScala.toMap)
  }


}
case class PythonInterpretRequest(statements: String, jobGroup: String)