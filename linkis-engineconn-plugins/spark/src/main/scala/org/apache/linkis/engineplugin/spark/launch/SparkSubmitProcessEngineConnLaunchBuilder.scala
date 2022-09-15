/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineplugin.spark.launch

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.engineplugin.spark.config.{SparkConfiguration, SparkResourceConfiguration}
import org.apache.linkis.engineplugin.spark.config.SparkResourceConfiguration.LINKIS_SPARK_DRIVER_MEMORY
import org.apache.linkis.engineplugin.spark.launch.SparkSubmitProcessEngineConnLaunchBuilder.{
  getValueAndRemove,
  AbsolutePath,
  Path,
  RelativePath
}
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.manager.common.entity.resource.{DriverAndYarnResource, NodeResource}
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment._
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel
import org.apache.linkis.protocol.UserWithCreator

import org.apache.commons.lang3.StringUtils

import java.lang.ProcessBuilder.Redirect
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import com.google.common.collect.Lists

class SparkSubmitProcessEngineConnLaunchBuilder private extends JavaProcessEngineConnLaunchBuilder {

  private[this] val fsRoot = "hdfs://"
  protected var port: Int = _
  protected var request: EngineConnBuildRequest = _
  protected var userEngineResource: NodeResource = _
  private[this] var _executable: Path = _
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

  private[this] var _userWithCreator: UserWithCreator =
    UserWithCreator("DefaultUser", "DefaultCreator")

  private[this] var _labels: ArrayBuffer[Label[_]] = ArrayBuffer()

  def executable(executable: Path): SparkSubmitProcessEngineConnLaunchBuilder = {
    _executable = executable
    this
  }

  def jars(jars: Traversable[Path]): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._jars ++= jars
    this
  }

  def pyFile(pyFile: Path): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._pyFiles += pyFile
    this
  }

  def pyFiles(pyFiles: Traversable[Path]): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._pyFiles ++= pyFiles
    this
  }

  def files(files: Traversable[Path]): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._files ++= files
    this
  }

  def conf(conf: Traversable[(String, String)]): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._conf ++= conf
    this
  }

  def conf(conf: (String, String)): SparkSubmitProcessEngineConnLaunchBuilder =
    this.conf(conf._1, conf._2)

  def driverJavaOptions(driverJavaOptions: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _driverJavaOptions = Some(driverJavaOptions)
    this
  }

  def driverClassPaths(
      classPaths: Traversable[String]
  ): SparkSubmitProcessEngineConnLaunchBuilder = {
    _driverClassPath ++= classPaths
    this
  }

  def archives(archives: Traversable[Path]): SparkSubmitProcessEngineConnLaunchBuilder = {
    archives.foreach(archive)
    this
  }

  def archive(archive: Path): SparkSubmitProcessEngineConnLaunchBuilder = {
    _archives += archive
    this
  }

  def redirectError(
      redirect: ProcessBuilder.Redirect
  ): SparkSubmitProcessEngineConnLaunchBuilder = {
    _redirectError = Some(redirect)
    this
  }

  def setPort(port: Int): Unit = this.port = port

  override protected def getCommands(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] = {
    build(engineConnBuildRequest)
    val commandLine: ArrayBuffer[String] = ArrayBuffer[String]()
    commandLine += SparkConfiguration.SPARK_SUBMIT_PATH.getValue

    def addOpt(option: String, value: Option[String]): Unit = {
      value.foreach { v =>
        commandLine += option
        commandLine += v
      }
    }

    def addList(option: String, values: Traversable[String]): Unit = {
      if (values.nonEmpty) {
        commandLine += option
        commandLine += values.mkString(",")
      }
    }

    def addClasspath(option: String, values: Traversable[String]): Unit = {
      if (values.nonEmpty) {
        commandLine += option
        commandLine += values.mkString(":")
      }
    }

    addOpt("--master", _master)
    addOpt("--deploy-mode", _deployMode)
    addOpt("--name", _name)

    if (HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue && _proxyUser.nonEmpty) {
      addOpt("--proxy-user", _proxyUser)
    }

    // addOpt("--jars",Some(ENGINEMANAGER_JAR.getValue))
    // info("No need to add jars for " + _jars.map(fromPath).exists(x => x.equals("hdfs:///")).toString())
    _jars = _jars.filter(_.isNotBlankPath())

    if (_jars.isEmpty) {
      _jars += AbsolutePath("")
    }
    _jars += AbsolutePath(variable(UDF_JARS))
    if (_jars.nonEmpty) {
      addList("--jars", _jars.map(fromPath))
    }

    _pyFiles = _pyFiles.filter(_.isNotBlankPath())
    if (_pyFiles.nonEmpty) {
      addList("--py-files", _pyFiles.map(fromPath))
    }

    _files = _files.filter(_.isNotBlankPath())
    if (_files.nonEmpty) {
      addList("--files", _files.map(fromPath))
    }

    _archives = _archives.filter(_.isNotBlankPath())
    if (_archives.nonEmpty) {
      addList("--archives", _archives.map(fromPath))
    }
    _conf.foreach { case (key, value) =>
      if (key.startsWith("spark.")) {
        // subcommand cannot be quoted by double quote, use single quote instead
        addOpt("--conf", Some(key + "=\"" + value + "\""))
      }
    }
    addOpt("--driver-memory", _driverMemory)
    addClasspath("--driver-class-path", _driverClassPath)
    addOpt("--driver-cores", _driverCores)
    addOpt("--executor-memory", _executorMemory)
    addOpt("--executor-cores", _executorCores)
    addOpt("--num-executors", _numExecutors)
    addOpt("--queue", _queue)

    addOpt("--class", _className)
    addOpt("1>", Some(s"${variable(LOG_DIRS)}/stdout"))
    addOpt("2>>", Some(s"${variable(LOG_DIRS)}/stderr"))

    addOpt("", Some(s" ${variable(PWD)}/lib/${SparkConfiguration.ENGINE_JAR.getValue}"))

    commandLine.toArray.filter(StringUtils.isNotEmpty)
  }

  override def enablePublicModule = true

  def master(masterUrl: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _master = Some(masterUrl)
    this
  }

  def deployMode(deployMode: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _deployMode = Some(deployMode)
    this
  }

  def className(className: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _className = Some(className)
    this
  }

  def name(name: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _name = Some(name)
    this
  }

  def jar(jar: Path): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._jars += jar
    this
  }

  def file(file: Path): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._files += file
    this
  }

  def conf(key: String, value: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    this._conf += ((key, value))
    this
  }

  def driverMemory(driverMemory: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _driverMemory = Some(driverMemory)
    this
  }

  def driverClassPath(classPath: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _driverClassPath += classPath
    this
  }

  def executorMemory(executorMemory: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _executorMemory = Some(executorMemory)
    this
  }

  def proxyUser(proxyUser: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _proxyUser = Some(proxyUser)
    this
  }

  def driverCores(driverCores: Int): SparkSubmitProcessEngineConnLaunchBuilder = {
    this.driverCores(driverCores.toString)
  }

  def driverCores(driverCores: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _driverCores = Some(driverCores)
    this
  }

  def executorCores(executorCores: Int): SparkSubmitProcessEngineConnLaunchBuilder = {
    this.executorCores(executorCores.toString)
  }

  def executorCores(executorCores: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _executorCores = Some(executorCores)
    this
  }

  def numExecutors(numExecutors: Int): SparkSubmitProcessEngineConnLaunchBuilder = {
    this.numExecutors(numExecutors.toString)
  }

  def numExecutors(numExecutors: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _numExecutors = Some(numExecutors)
    this
  }

  def queue(queue: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _queue = Some(queue)
    this
  }

  def env(key: String, value: String): SparkSubmitProcessEngineConnLaunchBuilder = {
    _env += ((key, value))
    this
  }

  def redirectOutput(
      redirect: ProcessBuilder.Redirect
  ): SparkSubmitProcessEngineConnLaunchBuilder = {
    _redirectOutput = Some(redirect)
    this
  }

  def redirectErrorStream(redirect: Boolean): SparkSubmitProcessEngineConnLaunchBuilder = {
    _redirectErrorStream = Some(redirect)
    this
  }

  def getEngineResource: NodeResource = userEngineResource

  def build(engineRequest: EngineConnBuildRequest): Unit = {
    this.request = engineRequest
    this.userEngineResource = engineRequest.engineResource
    val darResource: DriverAndYarnResource =
      userEngineResource.getLockedResource.asInstanceOf[DriverAndYarnResource]
    val properties = engineRequest.engineConnCreationDesc.properties
    this.master("yarn")
    this.deployMode("client")
    val driverJavaSet = new StringBuilder(" -server")

    if (StringUtils.isNotEmpty(EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue))
      EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue
        .format(getGcLogDir(engineRequest))
        .split("\\s+")
        .foreach(l => {
          driverJavaSet.append(" ").append(l)
        })
    getLogDir(engineRequest).trim
      .split(" ")
      .foreach(l => {
        driverJavaSet.append(" ").append(l)
      })
    driverJavaSet.append(" -Djava.io.tmpdir=" + variable(TEMP_DIRS))
    if (EnvConfiguration.ENGINE_CONN_DEBUG_ENABLE.getValue) {
      driverJavaSet.append(
        s" -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${variable(RANDOM_PORT)}"
      )
    }
    this.conf(SparkConfiguration.SPARK_DRIVER_EXTRA_JAVA_OPTIONS.key, driverJavaSet.toString())
    // this.conf("spark.sql.extensions", "org.apache.linkis.hook.spark.extension.SparkHistoryExtension")
    this.className(getValueAndRemove(properties, "className", getMainClass))

    getValueAndRemove(properties, "archives", "").toString
      .split(",")
      .map(AbsolutePath)
      .foreach(this.archive)
    this.driverCores(
      getValueAndRemove(properties, SparkResourceConfiguration.LINKIS_SPARK_DRIVER_CORES)
    )
    val driverMemory = getValueAndRemove(properties, LINKIS_SPARK_DRIVER_MEMORY)
    val driverMemoryWithUnit = if (StringUtils.isNumeric(driverMemory)) {
      driverMemory + "g"
    } else {
      driverMemory
    }
    this.driverMemory(driverMemoryWithUnit)
    this.executorCores(
      getValueAndRemove(properties, SparkResourceConfiguration.LINKIS_SPARK_EXECUTOR_CORES)
    )
    val executorMemory =
      getValueAndRemove(properties, SparkResourceConfiguration.LINKIS_SPARK_EXECUTOR_MEMORY)
    val executorMemoryWithUnit = if (StringUtils.isNumeric(executorMemory)) {
      executorMemory + "g"
    } else {
      executorMemory
    }
    this.executorMemory(executorMemoryWithUnit)
    this.numExecutors(
      getValueAndRemove(properties, SparkResourceConfiguration.LINKIS_SPARK_EXECUTOR_INSTANCES)
    )
    getValueAndRemove(properties, "files", "").split(",").map(AbsolutePath).foreach(file)
    getValueAndRemove(properties, "jars", "").split(",").map(AbsolutePath).foreach(jar)
    val defaultExternalJars =
      getValueAndRemove(properties, SparkConfiguration.SPARK_DEFAULT_EXTERNAL_JARS_PATH)
    defaultExternalJars
      .split(",")
      .map(AbsolutePath)
      .filter(x => {
        val file = new java.io.File(x.path)
        file.isFile
      })
      .foreach(jar)

    proxyUser(getValueAndRemove(properties, "proxyUser", ""))
    if (null != darResource) {
      this.queue(darResource.yarnResource.queueName)
    } else {
      this.queue("default")
    }

    this.driverClassPath(getValueAndRemove(properties, SparkConfiguration.SPARK_DRIVER_CLASSPATH))
    this.driverClassPath(variable(CLASSPATH))
    this.redirectOutput(Redirect.PIPE)
    this.redirectErrorStream(true)

    val labels = engineRequest.labels.asScala
    labels.foreach { l =>
      {
        this._labels += l
        l match {
          case label: UserCreatorLabel =>
            this._userWithCreator = UserWithCreator(label.getUser, label.getCreator)
          case _ =>
        }
      }
    }

    if (!HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) {
      this.proxyUser(getValueAndRemove(properties, "proxyUser", ""))
    } else {
      this.proxyUser(this._userWithCreator.user)
    }

    // deal spark conf and spark.hadoop.*
    val iterator = properties.entrySet().iterator()
    val sparkConfKeys = ArrayBuffer[String]()
    while (iterator.hasNext) {
      val keyValue = iterator.next()
      if (
          !SparkConfiguration.SPARK_PYTHON_VERSION.key.equals(keyValue.getKey) && keyValue.getKey
            .startsWith("spark.") && StringUtils.isNotBlank(keyValue.getValue)
      ) {
        conf(keyValue.getKey, keyValue.getValue)
        sparkConfKeys += keyValue.getKey
      }
    }
    this.name(
      getValueAndRemove(
        properties,
        SparkConfiguration.SPARK_APP_NAME
      ) + "_" + this._userWithCreator.creator
    )
    sparkConfKeys.foreach(properties.remove(_))
  }

  private def fromPath(path: Path): String = path match {
    case AbsolutePath(p) => p
    case RelativePath(p) => p
  }

  override protected def getEngineConnManagerHooks(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[String] = {
    Lists.newArrayList("JarUDFLoadECMHook")
  }

}

object SparkSubmitProcessEngineConnLaunchBuilder {

  def newBuilder(): SparkSubmitProcessEngineConnLaunchBuilder =
    new SparkSubmitProcessEngineConnLaunchBuilder

  sealed trait Path {

    def isNotBlankPath(): Boolean;

    protected def isNotBlankPath(path: String): Boolean = {
      StringUtils.isNotBlank(path) && !"/".equals(path.trim) && !"hdfs:///".equals(
        path.trim
      ) && !"file:///".equals(path.trim)
    }

  }

  case class AbsolutePath(path: String) extends Path {
    override def isNotBlankPath(): Boolean = isNotBlankPath(path)
  }

  case class RelativePath(path: String) extends Path {
    override def isNotBlankPath(): Boolean = isNotBlankPath(path)
  }

  def getValueAndRemove[T](
      properties: java.util.Map[String, String],
      commonVars: CommonVars[T]
  ): T = {
    val value = commonVars.getValue(properties)
    properties.remove(commonVars.key)
    value
  }

  def getValueAndRemove(
      properties: java.util.Map[String, String],
      key: String,
      defaultValue: String
  ): String = {
    if (properties.containsKey(key)) {
      properties.remove(key)
    } else {
      defaultValue
    }
  }

}
