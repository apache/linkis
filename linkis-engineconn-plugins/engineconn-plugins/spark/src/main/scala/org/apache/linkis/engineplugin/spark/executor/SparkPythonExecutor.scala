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
 
package org.apache.linkis.engineplugin.spark.executor

import java.io._
import java.util
import java.net.InetAddress

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.rs.RsOutputStream
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.engineplugin.spark.Interpreter.PythonInterpreter._
import org.apache.linkis.engineplugin.spark.common.{Kind, PySpark}
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration
import org.apache.linkis.engineplugin.spark.entity.SparkEngineSession
import org.apache.linkis.engineplugin.spark.exception.ExecuteError
import org.apache.linkis.engineplugin.spark.imexport.CsvRelation
import org.apache.linkis.engineplugin.spark.utils.EngineUtils
import org.apache.linkis.governance.common.paser.PythonCodeParser
import org.apache.linkis.scheduler.executer.{ExecuteResponse, SuccessExecuteResponse}
import org.apache.linkis.storage.resultset.ResultSetWriter
import org.apache.commons.exec.CommandLine
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.{RandomStringUtils, StringUtils}
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.execution.datasources.csv.UDF
import org.apache.spark.sql.{DataFrame, SparkSession}
import py4j.GatewayServer
import py4j.GatewayServer.GatewayServerBuilder

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future, Promise}

/**
 *
 */
class SparkPythonExecutor(val sparkEngineSession: SparkEngineSession, val id: Int) extends SparkEngineConnExecutor(sparkEngineSession.sparkContext, id) {


  private var gatewayServer: GatewayServer = _
  private var process: Process = _
  private var pid: Option[String] = None
  private var jobGroup: String = _
  private val queryLock = new Array[Byte](0)
  private var code: String = _
  private var pythonScriptInitialized = false
  private[this] var promise: Promise[String] = _
  private implicit val executor: ExecutionContextExecutor = ExecutionContext.global

  implicit var sparkSession: SparkSession = sparkEngineSession.sparkSession
  private[spark] var engineExecutionContext: EngineExecutionContext = _
  private val engineCreationContext = EngineConnServer.getEngineCreationContext
  private val lineOutputStream = new RsOutputStream
  val sqlContext = sparkEngineSession.sqlContext
  val SUCCESS = "success"
  /*@throws(classOf[IOException])
  override def open = {}*/
  private lazy val py4jToken: String = RandomStringUtils.randomAlphanumeric(256)

  private lazy val gwBuilder: GatewayServerBuilder = {
    val builder = new GatewayServerBuilder()
      .javaPort(0)
      .callbackClient(0, InetAddress.getByName(GatewayServer.DEFAULT_ADDRESS))
      .connectTimeout(GatewayServer.DEFAULT_CONNECT_TIMEOUT)
      .readTimeout(GatewayServer.DEFAULT_READ_TIMEOUT)
      .customCommands(null)

    try builder.authToken(py4jToken) catch {
      case err: Throwable => builder
    }
  }

  def getSparkConf = sc.getConf

  def getJavaSparkContext = new JavaSparkContext(sc)

  def getSparkSession = if (sparkSession != null) sparkSession else () => throw new IllegalAccessException("not supported keyword spark in spark1.x versions")


  override def init(): Unit = {
    setCodeParser(new PythonCodeParser)
    super.init()
    info("spark sql executor start")
  }

  override def killTask(taskID: String): Unit = {
    info(s"Start to kill python task $taskID")
    super.killTask(taskID)
    info(s"To close python cli task $taskID")
    Utils.tryAndError(close)
    info(s"To delete python executor task $taskID")
    Utils.tryAndError(ExecutorManager.getInstance.removeExecutor(getExecutorLabels().asScala.toArray))
    info(s"Finished to kill python task $taskID")
  }

  override def close = {
    info("python executor ready to close")
    if (process != null) {
      if (gatewayServer != null) {
        Utils.tryAndError(gatewayServer.shutdown())
        gatewayServer = null
      }
      IOUtils.closeQuietly(lineOutputStream)
      pid.foreach(p => Utils.exec(Array("kill", "-9", p), 3000l))
      process.destroy()
      process = null
    }
    info("python executor Finished to close")
  }

  override def getKind: Kind = PySpark()

  private def initGateway = {
    //  如果从前端获取到用户所设置的Python版本为Python3 则取Python3的环境变量，否则默认为Python2
    logger.info(s"spark.python.version => ${engineCreationContext.getOptions.get("spark.python.version")}")
    val userDefinePythonVersion = engineCreationContext.getOptions.getOrDefault("spark.python.version", "python").toString.toLowerCase()
    val sparkPythonVersion = if(StringUtils.isNotBlank(userDefinePythonVersion)) userDefinePythonVersion else "python"
    val pythonExec = CommonVars("PYSPARK_DRIVER_PYTHON", sparkPythonVersion).getValue

    val pythonScriptPath = CommonVars("python.script.path", "python/mix_pyspark.py").getValue

    val port: Int = EngineUtils.findAvailPort
    gatewayServer = gwBuilder.entryPoint(this).javaPort(port).build()
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
    val pyFiles = sc.getConf.get("spark.submit.pyFiles", "")
    logger.info(s"spark.submit.pyFiles => ${pyFiles}")
    //add class path zip
    val classPath = CommonVars("java.class.path", "").getValue
    classPath.split(";").filter(_.endsWith(".zip")).foreach(pythonClasspath ++= File.pathSeparator ++= _)

    val cmd = CommandLine.parse(pythonExec)
    cmd.addArgument(createFakeShell(pythonScriptPath).getAbsolutePath, false)
    cmd.addArgument(port.toString, false)
    cmd.addArgument(EngineUtils.sparkSubmitVersion().replaceAll("\\.", ""), false)
    cmd.addArgument(py4jToken, false)
    cmd.addArgument(pythonClasspath.toString(), false)
    cmd.addArgument(pyFiles, false)

    val builder = new ProcessBuilder(cmd.toStrings.toSeq.toList.asJava)

    val env = builder.environment()
    if (StringUtils.isBlank(sc.getConf.get("spark.pyspark.python", ""))) {
      info("spark.pyspark.python is null")
      if (sparkPythonVersion.equals("python3")) {
        info("userDefinePythonVersion is python3 will be set to PYSPARK_PYTHON")
        env.put("PYSPARK_PYTHON", pythonExec)
      }
    } else {
      val executorPython = sc.getConf.get("spark.pyspark.python")
      info(s"set PYSPARK_PYTHON spark.pyspark.python is $executorPython")
      env.put("PYSPARK_PYTHON", executorPython)
    }
    env.put("PYTHONPATH", pythonClasspath.toString())
    env.put("PYTHONUNBUFFERED", "YES")
    env.put("PYSPARK_GATEWAY_PORT", "" + port)
    env.put("SPARK_HOME", SparkConfiguration.SPARK_HOME.getValue)
    //    builder.redirectError(Redirect.INHERIT)
    info("pyspark builder command:" + builder.command().asScala.mkString(" "))
    builder.redirectErrorStream(true)
    builder.redirectInput(ProcessBuilder.Redirect.PIPE)
    process = builder.start()
    //add hook to shutdown python
    Utils.addShutdownHook {
      close
      Utils.tryAndError(pid.foreach(p => Utils.exec(Array("kill", "-9", p), 3000l)))
    }

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
    Utils.waitUntil(() => pythonScriptInitialized, SparkConfiguration.SPARK_LANGUAGE_REPL_INIT_TIME.getValue.toDuration)
  }
  override protected def runCode(sparkEngineExecutor: SparkEngineConnExecutor, code: String, engineExecutionContext: EngineExecutionContext, jobGroup:String): ExecuteResponse = {
    if(engineExecutionContext != this.engineExecutionContext){
      this.engineExecutionContext = engineExecutionContext
      lineOutputStream.reset(engineExecutionContext)
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

  /*override protected def getInitLabels(): util.List[Label[_]] = {
    val runTypeLabel = new CodeLanguageLabel
    runTypeLabel.setRunType(RunType.PYSPARK.toString)
    val engineTypeLabel = getEngineTypeLabel
    val labels = new util.ArrayList[Label[_]](2)
    labels.add(runTypeLabel)
    labels.add(engineTypeLabel)
    labels
  }*/

  def executeLine(code: String): ExecuteResponse = {
    if (sc.isStopped) {
      throw new IllegalStateException("Application has been stopped, please relogin to try it.")
    }
    if (!pythonScriptInitialized) {
      throw new IllegalStateException("Pyspark process cannot be initialized, please ask administrator for help.")
    }
    promise = Promise[String]()
    this.code = code
    engineExecutionContext.appendStdout(s"${EngineUtils.getName} >> $code")
    queryLock synchronized queryLock.notify()
    Await.result(promise.future, Duration.Inf)
    lineOutputStream.flush()
    val outStr = lineOutputStream.toString()
    if(outStr.length >0) {
      val output = Utils.tryQuietly(ResultSetWriter.getRecordByRes(outStr, SparkConfiguration.SPARK_CONSOLE_OUTPUT_NUM.getValue))
      val res = if(output != null) output.map(x => x.toString).toList.mkString("\n") else ""
      if (res.length > 0) {
        engineExecutionContext.appendStdout(s"result is $res")
      }
    }
    SuccessExecuteResponse()
  }

  def onPythonScriptInitialized(pid: Int) = {
    this.pid = Some(pid.toString)
    pythonScriptInitialized = true
    info("Pyspark process has been initialized.")
  }

  def getStatements = {
    queryLock synchronized {while(code == null) queryLock.wait()}
    info("Prepare to deal python code, code: " + code.substring(0, if (code.indexOf("\n") > 0) code.indexOf("\n") else code.length))
    //    lineOutputStream.reset(this.engineExecutorContext)
    val request = PythonInterpretRequest(code, jobGroup)
    code = null
    request
  }

  def setStatementsFinished(out: String, error: Boolean) = {
    info(s"A python code finished, has some errors happened?  $error.")
    Utils.tryQuietly(Thread.sleep(10))
    if(! error) {
      promise.success(SUCCESS)
    } else {
      if (promise.isCompleted) {
        info("promise is completed and should start another python gateway")
        close
      }else{
        promise.failure(ExecuteError(40003,out))
      }
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
      engineExecutionContext.appendStdout(s"errorMessage is $message")
    }
  }

  def showDF(jobGroup: String, df: Any) = {
    SQLSession.showDF(sc, jobGroup, df.asInstanceOf[DataFrame], null, 5000, this.engineExecutionContext)
    info("Pyspark showDF execute success!")
  }

  def showAliasDF(jobGroup: String, df: Any, alias:String) = {
    SQLSession.showDF(sc, jobGroup, df.asInstanceOf[DataFrame], alias, 5000, this.engineExecutionContext)
    info("Pyspark showAliasDF execute success!")
  }

  def showHTML(jobGroup: String, htmlContent: Any)={
    SQLSession.showHTML(sc,jobGroup,htmlContent,this.engineExecutionContext)
    info("Pyspark showHTML execute success!")
  }
  def saveDFToCsv(df: Any, path: String, hasHeader: Boolean = true ,
                  isOverwrite: Boolean = false, option: util.Map[String, Any] = new util.HashMap()): Boolean ={
    CsvRelation.saveDFToCsv(sparkSession,df.asInstanceOf[DataFrame],path, hasHeader,isOverwrite, option.asScala.toMap)
  }
  def listUDFs() = UDF.listUDFs

  def existsUDF(name: String) = UDF.existsUDF(name)

  override protected def getExecutorIdPreFix: String = "SparkPythonExecutor_"

  def printLog(log:Any):Unit = {
    if(engineExecutionContext != null){
      engineExecutionContext.appendStdout("+++++++++++++++")
      engineExecutionContext.appendStdout(log.toString)
      engineExecutionContext.appendStdout("+++++++++++++++")
    }else{
      logger.warn("engine context is null can not send log")
    }
  }


}
case class PythonInterpretRequest(statements: String, jobGroup: String)
