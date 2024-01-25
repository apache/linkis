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

package org.apache.linkis.ecm.core.launch

import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.core.errorcode.LinkisECMErrorCodeSummary._
import org.apache.linkis.ecm.core.exception.ECMCoreException
import org.apache.linkis.ecm.core.utils.PortUtils
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.utils.{
  EngineConnArgumentsBuilder,
  EngineConnArgumentsParser
}
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.{
  Environment,
  ProcessEngineConnLaunchRequest
}
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment._
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants._

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import java.io.{File, InputStream, OutputStream}

import scala.collection.JavaConverters._
import scala.collection.mutable

trait ProcessEngineConnLaunch extends EngineConnLaunch with Logging {

  private var request: ProcessEngineConnLaunchRequest = _
  private var engineConnManagerEnv: EngineConnManagerEnv = _
  private var processBuilder: ProcessEngineCommandBuilder = _
  private var preparedExecFile: String = _
  private var process: Process = _
  private var randomPortNum = 1

  private var engineConnPort: String = _

  protected def newProcessEngineConnCommandBuilder(): ProcessEngineCommandBuilder =
    new UnixProcessEngineCommandBuilder

  protected def newProcessEngineConnCommandExec(
      command: Array[String],
      workDir: String
  ): ProcessEngineCommandExec =
    new ShellProcessEngineCommandExec(command, workDir)

  override def setEngineConnLaunchRequest(request: EngineConnLaunchRequest): Unit =
    request match {
      case processEngineConnLaunchRequest: ProcessEngineConnLaunchRequest =>
        this.request = processEngineConnLaunchRequest
      case _ => // TODO exception
    }

  override def setEngineConnManagerEnv(engineConnManagerEnv: EngineConnManagerEnv): Unit =
    this.engineConnManagerEnv = engineConnManagerEnv

  override def getEngineConnManagerEnv(): EngineConnManagerEnv = this.engineConnManagerEnv

  def getEngineConnLaunchRequest: EngineConnLaunchRequest = request

  private def initializeEnv(): Unit = {
    val environment = request.environment
    def putIfExists(enum: Environment): Unit = {
      val key = enum.toString
      val conf = CommonVars.apply(key, "")
      if (StringUtils.isNotBlank(conf.getValue)) environment.put(key, conf.getValue)
    }
    Environment.values foreach {
      case USER => environment.put(USER.toString, request.user)
      case ECM_HOME =>
        environment.put(ECM_HOME.toString, engineConnManagerEnv.engineConnManagerHomeDir)
      case PWD => environment.put(PWD.toString, engineConnManagerEnv.engineConnWorkDir)
      case LOG_DIRS => environment.put(LOG_DIRS.toString, engineConnManagerEnv.engineConnLogDirs)
      case TEMP_DIRS =>
        environment.put(TEMP_DIRS.toString, engineConnManagerEnv.engineConnTempDirs)
      case ECM_HOST =>
        environment.put(ECM_HOST.toString, engineConnManagerEnv.engineConnManagerHost)
      case ECM_PORT =>
        environment.put(ECM_PORT.toString, engineConnManagerEnv.engineConnManagerPort)
      case HADOOP_HOME => putIfExists(HADOOP_HOME)
      case HADOOP_CONF_DIR => putIfExists(HADOOP_CONF_DIR)
      case HIVE_CONF_DIR => putIfExists(HIVE_CONF_DIR)
      case JAVA_HOME => putIfExists(JAVA_HOME)
      case RANDOM_PORT =>
        environment.put(
          RANDOM_PORT.toString,
          PortUtils
            .findAvailPortByRange(GovernanceCommonConf.ENGINE_CONN_DEBUG_PORT_RANGE.getValue)
            .toString
        )
      case PREFER_IP_ADDRESS =>
        environment.put(PREFER_IP_ADDRESS.toString, Configuration.PREFER_IP_ADDRESS.toString)
      case ENGINECONN_ENVKEYS =>
        environment.put(ENGINECONN_ENVKEYS.toString, GovernanceCommonConf.ENGINECONN_ENVKEYS)
      case _ =>
    }
  }

  private def setMoreAvailPort(value: String): Unit = {
    val key = RANDOM_PORT.toString + randomPortNum
    // TODO just replace it by sorted RANDOM_PORT, since only one RANDOM_PORT is used now.
    if (value.contains(key)) {
      processBuilder.setEnv(key, PortUtils.findAvailPort().toString)
      randomPortNum += 1
    }
  }

  override def launch(): Unit = {
    request.necessaryEnvironments.foreach { e =>
      val env = CommonVars(e, "")
      if (StringUtils.isEmpty(env.getValue)) {
        throw new ErrorException(
          30000,
          s"Necessary environment $e does not exist!(必须的环境变量 $e 不存在！)"
        ) // TODO exception
      } else request.environment.put(e, env.getValue)
    }
    prepareCommand()
    val exec = newProcessEngineConnCommandExec(
      sudoCommand(request.user, execFile.mkString(" ")),
      engineConnManagerEnv.engineConnWorkDir
    )
    exec.execute()
    process = exec.getProcess
  }

  protected def execFile: Array[String]

  def getEngineConnPort: String = engineConnPort

  protected def getProcess(): Process = this.process

  /**
   * Get the pid of the startup process
   * @return
   */
  def getPid(): Option[String] = None

  protected def getCommandArgs: Array[String] = {
    val recordMap: mutable.Map[String, String] = mutable.Map()
    request.creationDesc.properties.asScala.foreach { case (k, v) =>
      if (k.contains(" ") || (v != null && v.contains(" "))) recordMap.put(k, v)
    }
    if (recordMap.size > 0) {
      val keyAndValue = new StringBuilder
      for (kv <- recordMap) {
        keyAndValue.append(s"${kv._1}->${kv._2};")
      }
      throw new ErrorException(
        30000,
        s"Startup parameters contain spaces! The key and value values of all its parameters are(启动参数中包含空格！其所有参数的key和value值分别为)：${keyAndValue.toString()}"
      )
    }
    val arguments = EngineConnArgumentsBuilder.newBuilder()
    engineConnPort = PortUtils
      .findAvailPortByRange(GovernanceCommonConf.ENGINE_CONN_PORT_RANGE.getValue)
      .toString

    var springConf =
      Map[String, String]("server.port" -> engineConnPort, "spring.profiles.active" -> "engineconn")
    val properties =
      PortUtils.readFromProperties(Configuration.getLinkisHome + "/conf/version.properties")
    if (StringUtils.isNotBlank(properties.getProperty("version"))) {
      springConf += ("eureka.instance.metadata-map.linkis.app.version" -> properties.getProperty(
        "version"
      ))
    }
    request.creationDesc.properties.asScala.filter(_._1.startsWith("spring.")).foreach {
      case (k, v) =>
        springConf = springConf + (k -> v)
    }

    arguments.addSpringConf(springConf)
    var engineConnConf = Map("ticketId" -> request.ticketId, "user" -> request.user)
    engineConnConf = engineConnConf ++: request.labels.asScala
      .map(l => EngineConnArgumentsParser.LABEL_PREFIX + l.getLabelKey -> l.getStringValue)
      .toMap
    engineConnConf = engineConnConf ++: request.creationDesc.properties.asScala
      .filterNot(_._1.startsWith("spring."))
      .toMap
    arguments.addEngineConnConf(engineConnConf)
    EngineConnArgumentsParser.getEngineConnArgumentsParser.parseToArgs(arguments.build())
  }

  override def kill(): Unit = {
    if (process != null) {
      process.destroy()
    }
  }

  override def isAlive: Boolean = {
    if (process != null) {
      process.isAlive
    } else {
      false
    }
  }

  protected def prepareCommand(): Unit = {
    processBuilder = newProcessEngineConnCommandBuilder()
    initializeEnv()
    // TODO env需要考虑顺序问题
    val classPath = request.environment.remove(CLASSPATH.toString)
    request.environment.asScala.foreach { case (k, v) =>
      val value = v.replaceAll(CLASS_PATH_SEPARATOR, File.pathSeparator)
      setMoreAvailPort(value)
      processBuilder.setEnv(k, processBuilder.replaceExpansionMarker(value))
    }
    processBuilder.setEnv(
      CLASSPATH.toString,
      processBuilder.replaceExpansionMarker(
        classPath.replaceAll(CLASS_PATH_SEPARATOR, File.pathSeparator)
      )
    )

    val engineConnEnvKeys = request.environment.remove(ENGINECONN_ENVKEYS.toString)
    logger.debug(s"ENGINECONN_ENVKEYS: " + engineConnEnvKeys)
    // set other env
    val engineConnEnvKeyArray = engineConnEnvKeys.split(",")
    engineConnEnvKeyArray.foreach(envKey => {
      if (null != envKey && !"".equals(envKey.trim)) {
        processBuilder.setEnv(envKey, GovernanceCommonConf.getEngineEnvValue(envKey))
      }
    })

    engineConnManagerEnv.linkDirs.foreach { case (k, v) => processBuilder.link(k, v) }
    val execCommand =
      request.commands.map(processBuilder.replaceExpansionMarker(_)) ++ getCommandArgs
    // execCommand = sudoCommand(request.user, execCommand.mkString(" "))
    execCommand.foreach(setMoreAvailPort)
    processBuilder.setCommand(execCommand)
    preparedExecFile = new File(engineConnManagerEnv.engineConnWorkDir, "engineConnExec.sh").getPath
    val output = getFileOutputStream
    Utils.tryFinally(processBuilder.writeTo(output))(output.close())
  }

  protected def sudoCommand(user: String, command: String): Array[String]

  protected def getFileOutputStream: OutputStream =
    FileUtils.openOutputStream(new File(preparedExecFile))

  protected def getPreparedExecFile: String = preparedExecFile

  def getProcessInputStream: InputStream = {
    if (process != null) {
      process.getInputStream
    } else {
      throw new ECMCoreException(
        CAN_NOT_GET_INPUTSTREAM.getErrorCode,
        CAN_NOT_GET_INPUTSTREAM.getErrorDesc
      )
    }
  }

  /**
   * process exit code if process is null retur errorcode 10
   * @return
   */
  def processWaitFor: Int = {
    if (process != null) {
      process.waitFor
    } else {
      10
    }
  }

}
