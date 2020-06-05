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

package com.webank.wedatasphere.linkis.engine

/**
  * Created by allenlliu on 2019/4/8.
  */

import java.io.{File, FileFilter, FileOutputStream, InputStream}
import java.net.ServerSocket
import java.nio.file.Files
import java.util.{List => JList}

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.conf.PythonEngineConfiguration
import com.webank.wedatasphere.linkis.engine.exception.{ExecuteException, PythonExecuteError}
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.rs.RsOutputStream
import com.webank.wedatasphere.linkis.engine.util.PythonConfiguration._
import com.webank.wedatasphere.linkis.engine.util._
import com.webank.wedatasphere.linkis.storage.domain._
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import com.webank.wedatasphere.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.exec.CommandLine
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.scalactic.Pass
import py4j.GatewayServer

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.{ArrayBuffer, StringBuilder}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

class PythonSession extends Logging {
  private implicit val executor = Utils.newCachedExecutionContext(5, "Python-Session-Thread-")
  private var engineExecutorContext: EngineExecutorContext = _

  private var gatewayServer: GatewayServer = _
  private var process: Process = _
  private[this] var promise: Promise[String] = _
  private var pythonScriptInitialized = false
  private val outputStream = new RsOutputStream
  private val queryLock = new Array[Byte](0)
  private var code: String = _
  private var pid: Option[String] = None

  def setEngineExecutorContext(engineExecutorContext: EngineExecutorContext): Unit = {
    if (engineExecutorContext != this.engineExecutorContext){
      this.engineExecutorContext = engineExecutorContext
      outputStream.reset(this.engineExecutorContext)
      outputStream.ready()
    }
  }

  private def initGateway = {

    val pythonExec = PYTHON_SCRIPT.getValue
    val port = {
      val socket = new ServerSocket(0)
      val port = socket.getLocalPort
      socket.close()
      port
    }
    gatewayServer = new GatewayServer(this, port)
    gatewayServer.start()
    info("Python executor file path is: " + getClass.getClassLoader.getResource("python/python.py").toURI)
    val pythonClasspath = new StringBuilder(PythonSession.pythonPath)
    val pyFiles = PYTHON_PATH.getValue
    if (StringUtils.isNotEmpty(pyFiles)) {
      pythonClasspath ++= File.pathSeparator ++= pyFiles.split(",").mkString(File.pathSeparator)
    }
    val cmd = CommandLine.parse(pythonExec)
    cmd.addArgument(PythonSession.createFakeShell("python/python.py").getAbsolutePath, false)
    cmd.addArgument(port.toString, false)
    cmd.addArgument(pythonClasspath.toString(), false)

    val builder = new ProcessBuilder(cmd.toStrings.toList)


    val env = builder.environment()
    env.put("PYTHONPATH", pythonClasspath.toString())
    env.put("PYTHONUNBUFFERED", "YES")
    env.put("PYTHON_GATEWAY_PORT", "" + port)
    info(builder.command().mkString(" "))
    builder.redirectErrorStream(true)
    builder.redirectInput(ProcessBuilder.Redirect.PIPE)
    process = builder.start()
    Future {
      val exitCode = process.waitFor()
      info("PythonExecutor has stopped with exit code " + exitCode)
    }
  }

  initGateway

  def execute(code: String): ExecuteResponse = {

    promise = Promise[String]()
    this.code = Kind.getRealCode(code)
    queryLock synchronized queryLock.notify()
    Utils.tryFinally(ExecuteComplete(Await.result(promise.future, Duration.Inf)))({
      outputStream.flush()
      val outStr = outputStream.toString()
      //logger.info("outStr is {}", outStr)
      if (StringUtils.isEmpty(outStr) && ResultSetFactory.getInstance.isResultSet(outStr)){
        val output = Utils.tryQuietly(ResultSetWriter.getRecordByRes(outStr, PythonEngineConfiguration.PYTHON_CONSOLE_OUTPUT_LINE_LIMIT.getValue))
        val output1 = Utils.tryQuietly(ResultSetWriter.getLastRecordByRes(outStr))
        //val res = output.map(x => x.toString).toList.mkString("\n")
        //val res = output1.apply(output.length - 1).toString
        val res = output1.toString
        logger.info("result is {} ", res)
        if (StringUtils.isNotBlank(res)) engineExecutorContext.appendStdout(res)
      }
      Pass
    })
  }

  def onPythonScriptInitialized(pid: Int) = {
    this.pid = Some(pid.toString)
    pythonScriptInitialized = true
    info(s"Python executor has been initialized with pid($pid).")
  }

  def getStatements = {
    queryLock synchronized {
      while (code == null) queryLock.wait()
    }
    val request = PythonInterpretRequest(code)
    code = null
    request
  }

  def printStdout(out: String): Unit = println(out)

  def setStatementsFinished(out: String, error: Boolean) = {
    info(s"A python code finished, has some errors happened? $error.")
    Utils.tryAndError(Thread.sleep(10))
    if (!error) {
      promise.success(outputStream.toString)
    }else{
      promise.failure(new PythonExecuteError(41001, out))
    }
  }

  def appendOutput(message: String) = {
    info("output message: " + message)
    if (pythonScriptInitialized != true) {
      info(message)
    } else {
//      for (c <- message){
//        outputStream.write(c)
//      }
      message.getBytes("utf-8").foreach(x => outputStream.write(x))
     // outputStream.write(message.getBytes("utf-8"))
    }
  }

  def close: Unit = {
    if (process != null) {
      if (gatewayServer != null) {
        Utils.tryAndError(gatewayServer.shutdown())
        gatewayServer = null
      }
        IOUtils.closeQuietly(outputStream)
      pid.foreach(p => Utils.exec(Array("kill", "-9", p), 3000l))
      process.destroy()
      process = null
    }
  }
  def kind: Kind = Python()

  def onProcessFailed(e: ExecuteException): Unit = error("", e)

  def onProcessComplete(i: Int): Unit = info("Python executor has completed with exit code " + i)

  def changeDT(dt: String): DataType = dt match {
    case "int" | "int16" | "int32" | "int64" => IntType
    case "float"|"float16"|"float32"|"float64" => FloatType
    case "double" => DoubleType
    case "bool" => BooleanType
    case "datetime64[ns]" | "datetime64[ns,tz]" | "timedelta[ns]" => TimestampType
    case "category" | "object" => StringType
    case _ => StringType
  }

  /**
    * show table
    *
    * @param data
    * @param schema
    * @param header
    */
  def showDF(data: JList[JList[Any]], schema: JList[String], header: JList[String]): Unit = {
    val writer = engineExecutorContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
    val length = schema.size() - 1
    var list: List[Column] = List[Column]()
    for (i <- 0 to length) {
      val col = Column(header.get(i), changeDT(schema.get(i)), null)
      list = col :: list
    }
    val metaData = new TableMetaData(list.toArray[Column])
    writer.addMetaData(metaData)
    val size = data.size() - 1
    for (i <- 0 to size) {
      writer.addRecord(new TableRecord(data.get(i).asScala.toArray[Any]))
    }
    // generate table data
    engineExecutorContext.sendResultSet(writer)
  }

  def showHTML(htmlContent: Any): Unit = {
    val startTime = System.currentTimeMillis()
    val writer = engineExecutorContext.createResultSetWriter(ResultSetFactory.HTML_TYPE)
    val metaData = new LineMetaData(null)
    writer.addMetaData(metaData)
    writer.addRecord(new LineRecord(htmlContent.toString))
    warn(s"Time taken: ${System.currentTimeMillis() - startTime}, done with html")
    engineExecutorContext.sendResultSet(writer)
  }
}

object PythonSession {
  private[PythonSession] def createFakeShell(script: String, fileType: String = ".py"): File = {
    val source: InputStream = getClass.getClassLoader.getResourceAsStream(script)

    val file = Files.createTempFile("", fileType).toFile
    file.deleteOnExit()

    val sink = new FileOutputStream(file)
    val buf = new Array[Byte](1024)
    var n = source.read(buf)

    while (n > 0) {
      sink.write(buf, 0, n)
      n = source.read(buf)
    }

    source.close()
    sink.close()

    file
  }

  private[PythonSession] def pythonPath = {
    info("this is pythonPath")
    val pythonPath = new ArrayBuffer[String]
    //借用spark的py4j
    val pythonHomePath = new File(PythonEngineConfiguration.PY4J_HOME.getValue)
    info(s"pythonHomePath => $pythonHomePath" )
    //val pythonParentPath = new File(pythonHomePath, "lib")
    //info(s"pythonParentPath => $pythonParentPath" )
    pythonPath += pythonHomePath.getPath
    info(s"pythonPath => $pythonPath" )
    pythonHomePath.listFiles(new FileFilter {
      override def accept(pathname: File): Boolean = pathname.getName.endsWith(".zip")
    }).foreach(f => pythonPath += f.getPath)
    pythonPath.mkString(File.pathSeparator)
  }
}

case class PythonInterpretRequest(statements: String)

case class Python() extends Kind {
  override def toString = "python"
}

