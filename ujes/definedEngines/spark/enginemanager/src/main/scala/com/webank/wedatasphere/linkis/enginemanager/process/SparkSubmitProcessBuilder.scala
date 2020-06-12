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


package com.webank.wedatasphere.linkis.enginemanager.process

import java.lang.ProcessBuilder.Redirect

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.enginemanager.EngineResource
import com.webank.wedatasphere.linkis.enginemanager.conf.EnvConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.configuration.SparkConfiguration
import com.webank.wedatasphere.linkis.enginemanager.configuration.SparkConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.configuration.SparkResourceConfiguration._
import com.webank.wedatasphere.linkis.enginemanager.impl.UserEngineResource
import com.webank.wedatasphere.linkis.enginemanager.process.SparkSubmitProcessBuilder.{AbsolutePath, Path, RelativePath}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.resourcemanager.DriverAndYarnResource

import scala.collection.mutable.ArrayBuffer

/**
  * Created by allenlliu on 2019/4/8.
  */
class SparkSubmitProcessBuilder extends ProcessEngineBuilder with Logging {
  private[this] val fsRoot = "hdfs://"
  protected var port: Int = _
  protected var request: RequestEngine = _
  protected var userEngineResource: UserEngineResource = _
  private[this] var _executable: Path = AbsolutePath(SPARK_SUBMIT_CMD.getValue)
  private[this] var _master: Option[String] = None
  private[this] var _deployMode: Option[String] = None
  private[this] var _className: Option[String] = None
  private[this] var _name: Option[String] = None
  private[this] var _jars: ArrayBuffer[Path] = ArrayBuffer()
  private[this] var _pyFiles: ArrayBuffer[Path] = ArrayBuffer()
  private[this] var _files: ArrayBuffer[Path] = ArrayBuffer()
  private[this] var _conf: ArrayBuffer[(String, String)] = ArrayBuffer()
  private[this] var _driverMemory: Option[String] = None
  private[this] var _driverJavaOptions: Option[String] = None
  private[this] var _driverClassPath: ArrayBuffer[String] = ArrayBuffer()
  private[this] var _executorMemory: Option[String] = None
  private[this] var _proxyUser: Option[String] = None

  private[this] var _driverCores: Option[String] = None
  private[this] var _executorCores: Option[String] = None
  private[this] var _queue: Option[String] = None
  private[this] var _numExecutors: Option[String] = None
  private[this] var _archives: ArrayBuffer[Path] = ArrayBuffer()

  private[this] var _env: ArrayBuffer[(String, String)] = ArrayBuffer()
  private[this] var _redirectOutput: Option[ProcessBuilder.Redirect] = None
  private[this] var _redirectError: Option[ProcessBuilder.Redirect] = None
  private[this] var _redirectErrorStream: Option[Boolean] = None

  def executable(executable: Path): SparkSubmitProcessBuilder = {
    _executable = executable
    this
  }

  def jars(jars: Traversable[Path]): SparkSubmitProcessBuilder = {
    this._jars ++= jars
    this
  }

  def pyFile(pyFile: Path): SparkSubmitProcessBuilder = {
    this._pyFiles += pyFile
    this
  }

  def pyFiles(pyFiles: Traversable[Path]): SparkSubmitProcessBuilder = {
    this._pyFiles ++= pyFiles
    this
  }

  def files(files: Traversable[Path]): SparkSubmitProcessBuilder = {
    this._files ++= files
    this
  }

  def conf(conf: Traversable[(String, String)]): SparkSubmitProcessBuilder = {
    this._conf ++= conf
    this
  }

  def conf(conf: (String, String)): SparkSubmitProcessBuilder = this.conf(conf._1, conf._2)

  def driverJavaOptions(driverJavaOptions: String): SparkSubmitProcessBuilder = {
    _driverJavaOptions = Some(driverJavaOptions)
    this
  }

  def driverClassPaths(classPaths: Traversable[String]): SparkSubmitProcessBuilder = {
    _driverClassPath ++= classPaths
    this
  }

  def archives(archives: Traversable[Path]): SparkSubmitProcessBuilder = {
    archives.foreach(archive)
    this
  }

  def archive(archive: Path): SparkSubmitProcessBuilder = {
    _archives += archive
    this
  }

  def redirectError(redirect: ProcessBuilder.Redirect): SparkSubmitProcessBuilder = {
    _redirectError = Some(redirect)
    this
  }

  override def setPort(port: Int): Unit = this.port = port

  override def build(engineRequest: EngineResource, request: RequestEngine): Unit = {
    this.request = request
    userEngineResource = engineRequest.asInstanceOf[UserEngineResource]
    val darResource: DriverAndYarnResource = engineRequest.getResource.asInstanceOf[DriverAndYarnResource]
    val properties = request.properties
    this.master("yarn")
    this.deployMode("client")
    val driverJavaSet = "\"-Dwds.linkis.configuration=linkis-engine.properties " + SparkConfiguration.getJavaRemotePort + "\""
    this.conf(SPARK_DRIVER_EXTRA_JAVA_OPTIONS.key, driverJavaSet)
    this.name(properties.getOrDefault("appName", "linkis"))
    this.className(properties.getOrDefault("className", "com.webank.wedatasphere.linkis.engine.DataWorkCloudEngineApplication"))
    properties.getOrDefault("archives", "").toString.split(",").map(RelativePath).foreach(this.archive)
    this.driverCores(DWC_SPARK_DRIVER_CORES)
    this.driverMemory(DWC_SPARK_DRIVER_MEMORY.getValue(properties) + "G")
    this.executorCores(DWC_SPARK_EXECUTOR_CORES.getValue(properties))
    this.executorMemory(DWC_SPARK_EXECUTOR_MEMORY.getValue(properties) + "G")
    this.numExecutors(DWC_SPARK_EXECUTOR_INSTANCES.getValue(properties))
    properties.getOrDefault("files", "").split(",").map(RelativePath).foreach(file)
    properties.getOrDefault("jars", "").split(",").map(RelativePath).foreach(jar)
    proxyUser(properties.getOrDefault("proxyUser", ""))
    this.queue(darResource.yarnResource.queueName)

    this.driverClassPath(SPARK_CONF_DIR.getValue)
    this.driverClassPath(HADOOP_CONF_DIR.getValue)
    this.driverClassPath(SPARK_DRIVER_CLASSPATH.getValue)
    this.redirectOutput(Redirect.PIPE)
    this.redirectErrorStream(true)
    this.env("spark.app.name", properties.getOrDefault("appName", "linkis" + request.creator))

  }

  def master(masterUrl: String): SparkSubmitProcessBuilder = {
    _master = Some(masterUrl)
    this
  }

  def deployMode(deployMode: String): SparkSubmitProcessBuilder = {
    _deployMode = Some(deployMode)
    this
  }

  def className(className: String): SparkSubmitProcessBuilder = {
    _className = Some(className)
    this
  }

  def name(name: String): SparkSubmitProcessBuilder = {
    _name = Some(name)
    this
  }

  def jar(jar: Path): SparkSubmitProcessBuilder = {
    this._jars += jar
    this
  }

  def file(file: Path): SparkSubmitProcessBuilder = {
    this._files += file
    this
  }

  def conf(key: String, value: String): SparkSubmitProcessBuilder = {
    this._conf += ((key, value))
    this
  }

  def driverMemory(driverMemory: String): SparkSubmitProcessBuilder = {
    _driverMemory = Some(driverMemory)
    this
  }

  def driverClassPath(classPath: String): SparkSubmitProcessBuilder = {
    _driverClassPath += classPath
    this
  }

  def executorMemory(executorMemory: String): SparkSubmitProcessBuilder = {
    _executorMemory = Some(executorMemory)
    this
  }

  def proxyUser(proxyUser: String): SparkSubmitProcessBuilder = {
    _proxyUser = Some(proxyUser)
    this
  }

  def driverCores(driverCores: Int): SparkSubmitProcessBuilder = {
    this.driverCores(driverCores.toString)
  }

  def driverCores(driverCores: String): SparkSubmitProcessBuilder = {
    _driverCores = Some(driverCores)
    this
  }

  def executorCores(executorCores: Int): SparkSubmitProcessBuilder = {
    this.executorCores(executorCores.toString)
  }

  def executorCores(executorCores: String): SparkSubmitProcessBuilder = {
    _executorCores = Some(executorCores)
    this
  }

  def numExecutors(numExecutors: Int): SparkSubmitProcessBuilder = {
    this.numExecutors(numExecutors.toString)
  }

  def numExecutors(numExecutors: String): SparkSubmitProcessBuilder = {
    _numExecutors = Some(numExecutors)
    this
  }

  def queue(queue: String): SparkSubmitProcessBuilder = {
    _queue = Some(queue)
    this
  }

  def env(key: String, value: String): SparkSubmitProcessBuilder = {
    _env += ((key, value))
    this
  }

  def redirectOutput(redirect: ProcessBuilder.Redirect): SparkSubmitProcessBuilder = {
    _redirectOutput = Some(redirect)
    this
  }

  def redirectErrorStream(redirect: Boolean): SparkSubmitProcessBuilder = {
    _redirectErrorStream = Some(redirect)
    this
  }

  override def getEngineResource: EngineResource = userEngineResource

  override def getRequestEngine: RequestEngine = request

  override def start(args: Array[String]): Process = {
    var args_ = ArrayBuffer(fromPath(_executable))

    def addOpt(option: String, value: Option[String]): Unit = {
      value.foreach { v =>
        args_ += option
        args_ += v
      }
    }

    def addList(option: String, values: Traversable[String]): Unit = {
      if (values.nonEmpty) {
        args_ += option
        args_ += values.mkString(",")
      }
    }

    def addClasspath(option: String, values: Traversable[String]): Unit = {
      if (values.nonEmpty) {
        args_ += option
        args_ += values.mkString(":")
      }
    }

    addOpt("--master", _master)
    addOpt("--deploy-mode", _deployMode)
    addOpt("--name", _name)
    //addOpt("--jars",Some(ENGINEMANAGER_JAR.getValue))
    info("No need to add jars for "+_jars.map(fromPath).exists(x => x.equals("hdfs:///")).toString())
    if(_jars.map(fromPath).exists(x => x.equals("hdfs:///")) != true) {
      addList("--jars", _jars.map(fromPath))
    }
    if(_pyFiles.map(fromPath).exists(x => x.equals("hdfs:///")) != true) {
      addList("--py-files", _pyFiles.map(fromPath))
    }
    if(_files.map(fromPath).exists(x => x.equals("hdfs:///")) != true) {
      addList("--files", _files.map(fromPath))
    }
    _conf.foreach { case (key, value) => if (key.startsWith("spark.")) addOpt("--conf", Option(f"""$key=$value"""))
    else if (key.startsWith("hive.")) addOpt("--hiveconf", Option(f"""$key=$value"""))
    }
    addOpt("--driver-memory", _driverMemory)
    //addOpt("--driver-java-options", _driverJavaOptions)
    addClasspath("--driver-class-path", _driverClassPath)
    addOpt("--driver-cores", _driverCores)
    addOpt("--executor-memory", _executorMemory)
    addOpt("--executor-cores", _executorCores)
    addOpt("--num-executors", _numExecutors)
    addOpt("--queue", _queue)
    //    if(!_archives.map(fromPath).equals("")) {
    //      addList("--archives", _archives.map(fromPath))
    //    }
    addOpt("--class", _className)
    addOpt("", Some(ENGINE_JAR.getValue))
    //    addOpt("--spring-conf", Some("ribbon.ReadTimeout=1200000"))
    //    addOpt("--spring-conf", Some("ribbon.ConnectTimeout=300000"))
    //    addOpt("--spring-conf", Some("feign.hystrix.enabled=false"))

    args_ ++= args

    var command = args_.mkString(" ")
    //Here are two reasons: 1. Space caused 2. Spark-submit has no source
    //这里是由于两个原因 1、空格引起  2、spark-submit 没有source
    //command="ipconfig"
    info(s"Running ${command}")

    val sudoCommand = Array(JavaProcessEngineBuilder.sudoUserScript.getValue, request.user, command)

    val pb = new ProcessBuilder(sudoCommand: _*)
    //val pb = new ProcessBuilder(command.split("\\s+").toList.asJava)
    val env = pb.environment()

    for ((key, value) <- _env) {
      env.put(key, value)
    }

    _redirectOutput.foreach(pb.redirectOutput)
    _redirectError.foreach(pb.redirectError)
    _redirectErrorStream.foreach(pb.redirectErrorStream)

    pb.redirectErrorStream(true)
    pb.redirectInput(ProcessBuilder.Redirect.PIPE)

    pb.start()
  }

  private def fromPath(path: Path) = path match {
    case AbsolutePath(p) => p
    case RelativePath(p) =>
      if (p.startsWith("hdfs://")) {
        p
      } else if (p.startsWith("file://")) {
        p
      } else {
        fsRoot + "/" + p
      }
  }
}

object SparkSubmitProcessBuilder {

  def apply(): SparkSubmitProcessBuilder = {
    new SparkSubmitProcessBuilder
  }

  sealed trait Path

  case class AbsolutePath(path: String) extends Path

  case class RelativePath(path: String) extends Path

}
