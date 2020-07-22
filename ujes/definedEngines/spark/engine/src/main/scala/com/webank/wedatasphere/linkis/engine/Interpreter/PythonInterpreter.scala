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

package com.webank.wedatasphere.linkis.engine.Interpreter

import java.io._
import java.nio.file.Files

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.engine.spark.common.LineBufferedStream
import com.webank.wedatasphere.linkis.engine.spark.utils.EngineUtils
import com.webank.wedatasphere.linkis.storage.FSFactory
import org.apache.commons.io.IOUtils
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.{SparkContext, SparkException}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.json4s.{DefaultFormats, JValue}
import py4j.GatewayServer

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
/**
  * Created by allenlliu on 2018/11/19.
  */

object PythonInterpreter {
  def create(): Interpreter = {
    val pythonExec = CommonVars("PYSPARK_DRIVER_PYTHON", "python").getValue

    val gatewayServer = new GatewayServer(SQLSession, 0)
    gatewayServer.start()

    val builder = new ProcessBuilder(Seq(
      pythonExec,
      createFakeShell().toString
    ))

    val env = builder.environment()
    env.put("PYTHONPATH", pythonPath)
    env.put("PYTHONUNBUFFERED", "YES")
    env.put("PYSPARK_GATEWAY_PORT", "" + gatewayServer.getListeningPort)
    env.put("SPARK_HOME", SparkConfiguration.SPARK_HOME.getValue)

    env.put("PYSPARK_ALLOW_INSECURE_GATEWAY" , "1")
    //    builder.redirectError(Redirect.INHERIT)

    val process = builder.start()

    new PythonInterpreter(process, gatewayServer)
  }

  def pythonPath = {
    val pythonPath = new ArrayBuffer[String]
    //    sys.env.get("SPARK_HOME").foreach { sparkHome =>
    //    }
    val pythonHomePath = new File(SparkConfiguration.SPARK_HOME.getValue, "python").getPath
    val pythonParentPath = new File(pythonHomePath, "lib")
    pythonPath += pythonHomePath
    pythonParentPath.listFiles(new FileFilter {
      override def accept(pathname: File): Boolean = pathname.getName.endsWith(".zip")
    }).foreach(f => pythonPath += f.getPath)
    EngineUtils.jarOfClass(classOf[SparkContext]).foreach(pythonPath += _)
    pythonPath.mkString(File.pathSeparator)
  }

  def createFakeShell(): File = createFakeShell("python/fake_shell.py")

  def createFakeShell(script: String, fileType: String = ".py"): File = {
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

  private def createFakePySpark(): File = {
    val source: InputStream = getClass.getClassLoader.getResourceAsStream("fake_pyspark.sh")

    val file = Files.createTempFile("", "").toFile
    file.deleteOnExit()

    file.setExecutable(true)

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
}

private class PythonInterpreter(process: Process, gatewayServer: GatewayServer)
  extends ProcessInterpreter(process)
    with Logging
{
  implicit val formats = DefaultFormats

  override def close(): Unit = {
    try {
      super.close()
    } finally {
      gatewayServer.shutdown()
    }
  }

  final override protected def waitUntilReady(): Unit = {
    var running = false
    val code = try process.exitValue catch { case t: IllegalThreadStateException => running = true;-1}
    if(!running) {
      throw new SparkException(s"Spark python application has already finished with exit code $code, now exit...")
    }
    var continue = true
    val initOut = new LineBufferedStream(process.getInputStream)
    val iterable = initOut.iterator
    while(continue && iterable.hasNext) {
      iterable.next match {
        case "READY" => println("Start python application succeed.");continue = false
        case str: String => println(str)
        case _ =>
      }
    }
    initOut.close
  }

  override protected def sendExecuteRequest(code: String): Option[JValue] = {
    val rep = sendRequest(Map("msg_type" -> "execute_request", "content" -> Map("code" -> code)))
    rep.map { rep =>
      assert((rep \ "msg_type").extract[String] == "execute_reply")

      val content: JValue = rep \ "content"

      content
    }
  }

  override protected def sendShutdownRequest(): Unit = {
    sendRequest(Map(
      "msg_type" -> "shutdown_request",
      "content" -> ()
    )).foreach { rep =>
      warn(f"process failed to shut down while returning $rep")
    }
  }

  private def sendRequest(request: Map[String, Any]): Option[JValue] = {
    stdin.println(write(request))
    stdin.flush()

    Option(stdout.readLine()).map { line => parse(line) }
  }
}

object SQLSession extends Logging {

//  def create = new SQLSession

  //  val maxResult = QueryConf.getInt("wds.linkis.query.maxResult", 1000) + 1

  def showDF(sc: SparkContext, jobGroup: String, df: Any, maxResult: Int = Int.MaxValue): String = {
    //    var rows: Array[AnyRef] = null
    //    var take: Method = null
    val startTime = System.currentTimeMillis()
    //    sc.setJobGroup(jobGroup, "Get IDE-SQL Results.", false)

    val iterator = Utils.tryThrow(df.asInstanceOf[DataFrame].toLocalIterator)(t => {
      sc.clearJobGroup()
      t
    }
    )

    var columns: List[Attribute] = null
    // get field names
    Utils.tryThrow({
      val qe = df.getClass.getMethod("queryExecution").invoke(df)
      val a = qe.getClass.getMethod("analyzed").invoke(qe)
      val seq = a.getClass.getMethod("output").invoke(a).asInstanceOf[Seq[Any]]
      columns = seq.toList.asInstanceOf[List[Attribute]]
    })(t => {
      sc.clearJobGroup()
      t
    })
    var schema = new StringBuilder
    schema ++= "%TABLE\n"
    for (col <- columns) {
      schema ++= col.name ++ "\t"
    }
    val trim = schema.toString.trim
    // val msg = new HDFSByteArrayOutputStream(sc.hadoopConfiguration)
    val msg = FSFactory.getFs("").write(new FsPath(""), true)
    msg.write(trim.getBytes("utf-8"))

    var index = 0
    Utils.tryThrow({
      while (index < maxResult && iterator.hasNext) {
        msg.write("\n".getBytes("utf-8"))
        val row = iterator.next()
        columns.indices.foreach { i =>
          if (row.isNullAt(i)) msg.write("NULL".getBytes("utf-8")) else msg.write(row.apply(i).asInstanceOf[Object].toString.getBytes("utf-8"))
          if (i != columns.size - 1) {
            msg.write("\t".getBytes("utf-8"))
          }
        }
        index += 1
      }
    })(t => {
      sc.clearJobGroup()
      t
    }
    )
    val colCount = if (columns != null) columns.size else 0
    warn(s"Fetched $colCount col(s) :  $index row(s).")
    sc.clearJobGroup()
    Utils.tryFinally({
      msg.flush();
      msg.toString
    }){ () => IOUtils.closeQuietly(msg)}
  }

}

