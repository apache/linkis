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

import java.io.{File, IOException}

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.engine.configuration.SparkConfiguration._
import com.webank.wedatasphere.linkis.engine.exception.{ApplicationAlreadyStoppedException, ExecuteError, SparkSessionNullException}
import com.webank.wedatasphere.linkis.engine.execute.EngineExecutorContext
import com.webank.wedatasphere.linkis.engine.rs.RsOutputStream
import com.webank.wedatasphere.linkis.engine.spark.common.SparkScala
import com.webank.wedatasphere.linkis.engine.spark.utils.EngineUtils
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.util.SparkUtils
import org.apache.spark.{SparkConf, SparkContext}

import _root_.scala.tools.nsc.GenericRunnerSettings
import scala.concurrent.Future
import scala.tools.nsc.interpreter.{ILoop, JPrintWriter, Results}


/**
  * Created by allenlliu on 2018/11/12.
  */
class SparkScalaExecutor(val sparkConf: SparkConf) extends SparkExecutor{

  var sparkContext : SparkContext = _
  var  _sqlContext:SQLContext =_
  var sparkSession: SparkSession =_
  var sparkILoop: ILoop = _
  private var bindFlag:Boolean = false
  private val engineExecutorContextFactory: EngineExecutorContextFactory = new EngineExecutorContextFactory

//  private val outputStream = new HDFSResultSetOutputStream(conf)
//  private val lineOutputStream = new HDFSByteArrayOutputStream(conf)

//  private val outputStream = EngineUtils.createOutputStream(EngineUtils.getTmpHDFSPath())
//  val lineOutputPath = EngineUtils.getTmpHDFSPath()
  private val lineOutputStream = new RsOutputStream

  private val jobGroup = new java.lang.StringBuilder
  private var executeCount = 0
  var sparkILoopInited = false
  private val sysProps = sys.env
  var outputDir: File = _
  protected implicit val executor = Utils.newCachedExecutionContext(5, "Spark-Scala-REPL-Thread-", true)
  @throws(classOf[IOException])
  override def open = {}



  def start() = {
    if(sparkILoop == null) {
      synchronized {
        if(sparkILoop == null) createSparkILoop
      }
    }
    if(sparkILoop != null) {
      if (!sparkILoopInited) {
        sparkILoop synchronized {
          if (!sparkILoopInited) {
            initSparkILoop //TODO When an exception is thrown, is there a shutdown here? I need to think about it again.（当抛出异常时，此处是否需要shutdown，需要再思考一下）

            //Wait up to 30 seconds（最多等待30秒）
            //Utils.waitUntil(() => future.isCompleted, SPARK_LANGUAGE_REPL_INIT_TIME.getValue.toDuration)
            //          info(lineOutputStream.toString)
            //          lineOutputStream.reset()
            sparkILoopInited = true
          }
        }
      }
    }else{
      throw new SparkSessionNullException(40006,"sparkILoop is null")
    }

  }

  override def execute(sparkEngineExecutor: SparkEngineExecutor, code: String,engineExecutorContext: EngineExecutorContext,jobGroup:String):ExecuteResponse = {
    this.jobGroup.append(jobGroup)
    Thread.currentThread().setContextClassLoader(sparkILoop.intp.classLoader)
    if(engineExecutorContext != this.engineExecutorContextFactory.getEngineExecutorContext){
     lineOutputStream.reset(engineExecutorContext)
    }

    lazyLoadILoop
    lineOutputStream.ready()
    if(sparkILoopInited) {
      engineExecutorContextFactory.setEngineExecutorContext(engineExecutorContext)
//      info("Spark scala executor reset new engineExecutorContext!!!!!")
    }
//    jobGroup.setLength(0)
//    jobGroup.append("ide-alone-scala-").append(executeCount)
//    sparkContext.setJobGroup(jobGroup.toString, code, true)
        var res: ExecuteResponse = null

        try {
          res = executeLine(code,engineExecutorContext)
        } catch {
          case e: Exception =>
            sparkContext.clearJobGroup()
            error("Interpreter exception", e)
            // _state = Idle()
            return ErrorExecuteResponse("Interpreter exception",e)
        }
        res match {
          case SuccessExecuteResponse() =>
            //println("scala> " + Utils.codeTransfer(incomplete + s).trim)
            //println(output)
//            incomplete = ""
//                     printlnCode = ""
          case IncompleteExecuteResponse(_) =>
            //            printlnCode += incomplete + s + "\n"
//            incomplete += s + "\n"
          case _ =>
            sparkContext.clearJobGroup()
           // _state = Idle()
            return res
        }

      res
  }
    def executeLine(code: String,engineExecutorContext: EngineExecutorContext): ExecuteResponse = synchronized {
      if(sparkContext.isStopped) {
        error("Spark application has already stopped, please restart it.")
        throw new ApplicationAlreadyStoppedException(40004,"Spark application has already stopped, please restart it.")
      }
//      info("Spark Interpreter begin to run code:" +code)
      executeCount += 1
      //lineOutputStream.reset()
      val result = scala.Console.withOut(lineOutputStream) {
        Utils.tryCatch(sparkILoop.interpret(code)){ t => Results.Error} match {
          case Results.Success =>
            lineOutputStream.flush()
            engineExecutorContext.appendStdout("scala> " + code)
            val outStr = lineOutputStream.toString()
            if( StringUtils.isNotEmpty(outStr) && ResultSetFactory.getInstance.isResultSet(outStr)) {
              val output = Utils.tryQuietly(ResultSetWriter.getRecordByRes(outStr, SPARK_CONSOLE_OUTPUT_NUM.getValue))
              val res = output.map(x => x.toString).toList.mkString("\n")
              if (res.length > 0) {
                engineExecutorContext.appendStdout(res)
              }
            }
            SuccessExecuteResponse()
          case Results.Incomplete =>
            error("incomplete code.")
            IncompleteExecuteResponse(null)
          case Results.Error =>
            val output = lineOutputStream.toString
            lineOutputStream.close()
            val errorMsg = EngineUtils.getResultStrByDolphinTextContent(output)
            error("Execute code error for "+  errorMsg)
            IOUtils.closeQuietly(lineOutputStream)
            ErrorExecuteResponse("",new ExecuteError(40005, "execute sparkScala failed!"))
        }
      }
      result
    }

 def close = {
    {
      shutdown()
    }
  }

  def kind = SparkScala()

  private def createSparkILoop = {
//    outputDir = createOutputDir(sparkConf)
    info("outputDir====> " + outputDir)
    sparkILoop = new ILoop(None, new JPrintWriter(lineOutputStream, true))
  }

   private def lazyLoadILoop = {    //lazy loaded.
     if(!bindFlag) {
       bindSparkSession
     }

  }

   private def initSparkILoop = {
    val settings = new GenericRunnerSettings(error(_))
    val sparkJars = sparkConf.getOption("spark.jars")
    val jars = if (sparkConf.get("spark.master").contains("yarn")) {
      val yarnJars = sparkConf.getOption("spark.yarn.dist.jars")
      SparkUtils.unionFileLists(sparkJars, yarnJars).toSeq
    } else {
      sparkJars.map(_.split(",")).map(_.filter(_.nonEmpty)).toSeq.flatten
    }
    val classpathJars = System.getProperty("java.class.path").split(":").filter(_.endsWith(".jar"))
    //.filterNot(f=> f.contains("spark-") || f.contains("datanucleus"))
    val classpath = jars.mkString(File.pathSeparator) + File.pathSeparator +
      classpathJars.mkString(File.pathSeparator)
    debug("Spark shell add jars: " + classpath)
    settings.processArguments(List("-Yrepl-class-based",
      "-Yrepl-outdir", s"${outputDir.getAbsolutePath}","-classpath", classpath), true)
    settings.usejavacp.value = true
     settings.embeddedDefaults(Thread.currentThread().getContextClassLoader())
    Future {
      sparkILoop.process(settings)
      warn("spark repl has been finished, now stop it.")
//      if(_state != ShuttingDown() && _state != Error()) {
//        shutdown()
//      }
    }
  }

    def bindSparkSession = {
    require(sparkContext != null)
    require(sparkSession != null)
    require(_sqlContext != null)
    //Wait up to 10 seconds（最多等待10秒）
    val startTime = System.currentTimeMillis()
    Utils.waitUntil(() => sparkILoop.intp != null && sparkILoop.intp.isInitializeComplete, SPARK_LANGUAGE_REPL_INIT_TIME.getValue.toDuration)
    warn(s"init sparkILoop0 cost ${System.currentTimeMillis() - startTime}.")
    sparkILoop.beSilentDuring {
      sparkILoop.processLine(":silent")
      sparkILoop.processLine("import org.apache.spark.SparkContext")
      sparkILoop.processLine("import org.apache.spark.sql.SparkSession")
      sparkILoop.processLine("import org.apache.spark.sql.SQLContext")
      sparkILoop.bind("sc", "org.apache.spark.SparkContext", sparkContext, List("""@transient"""))
      sparkILoop.bind("spark", "org.apache.spark.sql.SparkSession", sparkSession, List("""@transient"""))
      sparkILoop.bind("sqlContext", "org.apache.spark.sql.SQLContext", _sqlContext, List("""@transient"""))
      sparkILoop.bind("engineExecutorContext", "com.webank.wedatasphere.linkis.engine.executors.EngineExecutorContextFactory", engineExecutorContextFactory)
      sparkILoop.bind("jobGroup", "java.lang.StringBuilder", jobGroup)
//      sparkILoop.processLine("import com.webank.wedatasphere.linkis.wds.linkis.core.repl.spark.sql.SQLSession.showDF")
      sparkILoop.processLine("import com.webank.wedatasphere.linkis.engine.executors.SQLSession.showDF")
      sparkILoop.processLine("import com.webank.wedatasphere.linkis.engine.executors.SQLSession.showHTML")
      sparkILoop.processLine("import sqlContext.sql")
      sparkILoop.processLine("import sqlContext._")
      sparkILoop.processLine("import spark.implicits._")
      sparkILoop.processLine("import spark.sql")
      sparkILoop.processLine("import org.apache.spark.sql.functions._")
      sparkILoop.processLine("import com.webank.wedatasphere.linkis.engine.executors.EngineExecutorContextFactory")
      sparkILoop.processLine("def toEngineExecutorContext(engineExecutorContextFactory: EngineExecutorContextFactory): EngineExecutorContext = engineExecutorContextFactory.getEngineExecutorContext")
      sparkILoop.processLine("def showAlias(df: Any, alias:String): Unit = showDF(sparkContext, jobGroup.toString, df, alias,10000, engineExecutorContext.getEngineExecutorContext)")
      sparkILoop.processLine("def show(df: Any): Unit = showDF(sparkContext, jobGroup.toString, df,\"\",10000,engineExecutorContext.getEngineExecutorContext)")
      sparkILoop.processLine("def showHtml(content: Any): Unit = showHTML(sparkContext, jobGroup.toString, content, engineExecutorContext.getEngineExecutorContext)")
      sparkILoop.processLine("import org.apache.spark.sql.UDFRegistration")
      sparkILoop.processLine("implicit val sparkSession = spark")
      bindFlag = true
      warn(s"init sparkILoop cost ${System.currentTimeMillis() - startTime}.")
    }
  }
    def shutdown(): Unit = synchronized {
      //_state = ShuttingDown()

//      IOUtils.closeQuietly(lineOutputStream)
//      IOUtils.closeQuietly(outputStream)
      if (sparkILoop != null && sparkILoop.intp != null) {
        Utils.tryQuietly(sparkILoop.intp.close())
      }
      if (sparkContext != null && !sparkContext.isStopped) {
        Utils.tryQuietly(sparkContext.stop())
      }

      outputDir.delete()
      executor.shutdown()
      //_state = Error()
    }


//  val ResultTypes = List("%TABLE", "%HTML", "%IMG", "%TEXT", "%ANGULAR", "%SVG")
//
//  def isIDEResultSets(resultSet: String): Boolean = {
//    val _resultSet = trimBlank(resultSet)
//    if(StringUtils.isEmpty(_resultSet) || ! _resultSet.startsWith("%")) false
//    else if(_resultSet.length < 9 && ResultTypes.contains(_resultSet.toUpperCase)) true
//    else if(_resultSet.indexOf("\n") < 0) false
//    else if(ResultTypes.contains(_resultSet.substring(0, _resultSet.indexOf("\n")).toUpperCase)) true
//    else if(ResultTypes.contains(_resultSet.substring(0, _resultSet.indexOf(" ")).toUpperCase)) true //兼容老版本
//    else false
//  }
//  def trimBlank(str: String) = StringUtils.strip(str)
//
//  val HDFS_HEADER = "%HDFS "

//  def isHDFSPath(str: String) = StringUtils.isNotEmpty(str) && str.startsWith(HDFS_HEADER)

}
class EngineExecutorContextFactory {
  private var engineExecutorContext: EngineExecutorContext = _

  def setEngineExecutorContext(engineExecutorContext: EngineExecutorContext): Unit = this.engineExecutorContext = engineExecutorContext

  def getEngineExecutorContext = this.engineExecutorContext
}
