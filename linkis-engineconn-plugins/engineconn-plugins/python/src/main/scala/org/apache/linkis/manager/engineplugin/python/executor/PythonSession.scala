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
 
package org.apache.linkis.manager.engineplugin.python.executor


import java.io.{File, FileFilter, FileOutputStream, InputStream}
import java.net.ServerSocket
import java.nio.file.Files
import java.util.{List => JList}

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.rs.RsOutputStream
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.manager.engineplugin.python.conf.PythonEngineConfiguration
import org.apache.linkis.manager.engineplugin.python.exception.{ExecuteException, PythonExecuteError}
import org.apache.linkis.manager.engineplugin.python.utils.Kind
import org.apache.linkis.storage.domain._
import org.apache.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import org.apache.linkis.storage.{LineMetaData, LineRecord}
import org.apache.commons.exec.CommandLine
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import py4j.GatewayServer

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

class PythonSession extends Logging {

  private implicit val executor = Utils.newCachedExecutionContext(5, "Python-Session-Thread-")
  private var engineExecutionContext: EngineExecutionContext = _

  private var gatewayServer: GatewayServer = _
  private var process: Process = _
  private[this] var promise: Promise[String] = _
  private var pythonScriptInitialized = false
  private val outputStream = new RsOutputStream
  private val queryLock = new Array[Byte](0)
  private var code: String = _
  private var pid: Option[String] = None
  private var gatewayInited = false
  private val pythonDefaultVersion: String = EngineConnServer.getEngineCreationContext.getOptions.getOrDefault("python.version", "python")


  def init(): Unit = {
    initGateway
  }

  def setEngineExecutionContext(engineExecutorContext: EngineExecutionContext): Unit = {
    if (engineExecutorContext != this.engineExecutionContext) {
      this.engineExecutionContext = engineExecutorContext
      outputStream.reset(this.engineExecutionContext)
      outputStream.ready()
    }
  }

  private def initGateway = {
    val userDefinePythonVersion = Some(pythonDefaultVersion).getOrElse("python")
    info(s"System userDefinePythonVersion => ${userDefinePythonVersion}")
    val pythonExec = if ("python3".equalsIgnoreCase(userDefinePythonVersion)) PythonEngineConfiguration.PYTHON_VERSION.getValue else "python"
    info(s"pythonExec => ${pythonExec}")
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
    val pyFiles = PythonEngineConfiguration.PYTHON_PATH.getValue(EngineConnServer.getEngineCreationContext.getOptions)
    info(s"pyFiles => ${pyFiles}")
    if (StringUtils.isNotEmpty(pyFiles)) {
      pythonClasspath ++= File.pathSeparator ++= pyFiles.split(",").mkString(File.pathSeparator)
    }
    val cmd = CommandLine.parse(pythonExec)
    cmd.addArgument(PythonSession.createFakeShell("python/python.py").getAbsolutePath, false)
    cmd.addArgument(port.toString, false)
    cmd.addArgument(pythonClasspath.toString(), false)
    val cmdList = cmd.toStrings.toList.asJava
    val builder = new ProcessBuilder(cmdList)


    val env = builder.environment()
    env.put("PYTHONPATH", pythonClasspath.toString())
    env.put("PYTHONUNBUFFERED", "YES")
    env.put("PYTHON_GATEWAY_PORT", "" + port)
    info(builder.command().asScala.mkString(" "))
    builder.redirectErrorStream(true)
    builder.redirectInput(ProcessBuilder.Redirect.PIPE)
    process = builder.start()
    Utils.addShutdownHook {
      close
      Utils.tryAndError(pid.foreach(p => Utils.exec(Array("kill", "-9", p), 3000l)))
    }
    gatewayInited = true
    Future {
      val exitCode = process.waitFor()
      info("PythonExecutor has stopped with exit code " + exitCode)
      if (!process.isAlive) {
        warn("Python shell process exited.")
      }
    }
  }

  def execute(code: String): Unit = {
    if (!gatewayInited) synchronized {
      if (!gatewayInited) {
        initGateway
      }
    }
    if (null == process || !process.isAlive) {
      val msg = "Python process is not alive."
      error(msg)
      throw new ExecuteException(desc = msg)
    }
    promise = Promise[String]()
    this.code = Kind.getRealCode(code)
    queryLock synchronized queryLock.notify()
    try {
      Await.result(promise.future, Duration.Inf)
    } catch {
      case t: Throwable =>
        val exception = new ExecuteException(desc = t.getMessage)
        exception.initCause(t)
        throw t
    } finally {
      outputStream.flush()
      val outStr = outputStream.toString()
      if (StringUtils.isNotBlank(outStr)) {
        val output = Utils.tryQuietly(ResultSetWriter.getRecordByRes(outStr, PythonEngineConfiguration.PYTHON_CONSOLE_OUTPUT_LINE_LIMIT.getValue))
        val res = if (output != null) output.toString else ""
        info(s"result is {$res} ")
        if (StringUtils.isNotBlank(res)) engineExecutionContext.appendStdout(res)
      }
    }
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
    } else {
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
    case "float" | "float16" | "float32" | "float64" => FloatType
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
    val writer = engineExecutionContext.createResultSetWriter(ResultSetFactory.TABLE_TYPE)
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
    engineExecutionContext.sendResultSet(writer)
  }

  def showHTML(htmlContent: Any): Unit = {
    val startTime = System.currentTimeMillis()
    val writer = engineExecutionContext.createResultSetWriter(ResultSetFactory.HTML_TYPE)
    val metaData = new LineMetaData(null)
    writer.addMetaData(metaData)
    writer.addRecord(new LineRecord(htmlContent.toString))
    warn(s"Time taken: ${System.currentTimeMillis() - startTime}, done with html")
    engineExecutionContext.sendResultSet(writer)
  }
}

object PythonSession extends Logging {
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
    val pythonHomePath = new File(PythonEngineConfiguration.PY4J_HOME.getValue).getPath
    info(s"pythonHomePath => $pythonHomePath")
    val pythonParentPath = new File(pythonHomePath)
    info(s"pythonParentPath => $pythonParentPath")
    pythonPath += pythonHomePath
    info(s"pythonPath => $pythonPath")
    pythonParentPath.listFiles(new FileFilter {
      override def accept(pathname: File): Boolean = pathname.getName.endsWith(".zip")
    }).foreach(f => pythonPath += f.getPath)
    pythonPath.mkString(File.pathSeparator)
  }
}

case class PythonInterpretRequest(statements: String)

case class Python() extends Kind {
  override def toString = "python"
}

