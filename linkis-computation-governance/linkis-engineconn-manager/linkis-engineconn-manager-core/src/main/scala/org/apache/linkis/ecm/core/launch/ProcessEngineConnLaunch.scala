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
 
package org.apache.linkis.ecm.core.launch

import java.io.{File, InputStream, OutputStream}
import java.net.ServerSocket
import org.apache.linkis.common.conf.{CommonVars, Configuration}
import org.apache.linkis.common.exception.ErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.ecm.core.conf.ECMErrorCode
import org.apache.linkis.ecm.core.exception.ECMCoreException
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.utils.{EngineConnArgumentsBuilder, EngineConnArgumentsParser}
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnLaunchRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment._
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants._
import org.apache.linkis.manager.engineplugin.common.launch.process.{Environment, ProcessEngineConnLaunchRequest}
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConversions._


trait ProcessEngineConnLaunch extends EngineConnLaunch with Logging {

  private var request: ProcessEngineConnLaunchRequest = _
  private var engineConnManagerEnv: EngineConnManagerEnv = _
  private var discoveryMsgGenerator: DiscoveryMsgGenerator = _
  private var processBuilder: ProcessEngineCommandBuilder = _
  private var preparedExecFile: String = _
  private var process: Process = _
  private var randomPortNum = 1

  private var engineConnPort: String = _

  protected def newProcessEngineConnCommandBuilder(): ProcessEngineCommandBuilder = new UnixProcessEngineCommandBuilder

  protected def newProcessEngineConnCommandExec(command: Array[String], workDir: String): ProcessEngineCommandExec =
    new ShellProcessEngineCommandExec(command, workDir)

  override def setEngineConnLaunchRequest(request: EngineConnLaunchRequest): Unit = request match {
    case processEngineConnLaunchRequest: ProcessEngineConnLaunchRequest =>
      this.request = processEngineConnLaunchRequest
    case _ => //TODO exception
  }

  override def setEngineConnManagerEnv(engineConnManagerEnv: EngineConnManagerEnv): Unit = this.engineConnManagerEnv = engineConnManagerEnv

  override def getEngineConnManagerEnv(): EngineConnManagerEnv = this.engineConnManagerEnv

  def setDiscoveryMsgGenerator(discoveryMsgGenerator: DiscoveryMsgGenerator): Unit = this.discoveryMsgGenerator = discoveryMsgGenerator

  def getEngineConnLaunchRequest: EngineConnLaunchRequest = request

  private def initializeEnv(): Unit = {
    val environment = request.environment
    def putIfExists(enum: Environment): Unit = {
      val key = enum.toString
      val conf = CommonVars.apply(key, "")
      if(StringUtils.isNotBlank(conf.getValue)) environment.put(key, conf.getValue)
    }
    Environment.values foreach {
      case USER => environment.put(USER.toString, request.user)
      case ECM_HOME => environment.put(ECM_HOME.toString, engineConnManagerEnv.engineConnManagerHomeDir)
      case PWD => environment.put(PWD.toString, engineConnManagerEnv.engineConnWorkDir)
      case LOG_DIRS => environment.put(LOG_DIRS.toString, engineConnManagerEnv.engineConnLogDirs)
      case TEMP_DIRS => environment.put(TEMP_DIRS.toString, engineConnManagerEnv.engineConnTempDirs)
      case ECM_HOST => environment.put(ECM_HOST.toString, engineConnManagerEnv.engineConnManagerHost)
      case ECM_PORT => environment.put(ECM_PORT.toString, engineConnManagerEnv.engineConnManagerPort)
      case HADOOP_HOME => putIfExists(HADOOP_HOME)
      case HADOOP_CONF_DIR => putIfExists(HADOOP_CONF_DIR)
      case HIVE_CONF_DIR => putIfExists(HIVE_CONF_DIR)
      case JAVA_HOME => putIfExists(JAVA_HOME)
      case RANDOM_PORT => environment.put(RANDOM_PORT.toString, findAvailPort().toString)
      case EUREKA_PREFER_IP => environment.put(EUREKA_PREFER_IP.toString, Configuration.EUREKA_PREFER_IP.toString)
      case ENGINECONN_ENVKEYS => environment.put(ENGINECONN_ENVKEYS.toString, GovernanceCommonConf.ENGINECONN_ENVKEYS.toString)
      case _ =>
    }
  }

  private def findAvailPort(): Int = {
    val socket = new ServerSocket(0)
    Utils.tryFinally(socket.getLocalPort)(IOUtils.closeQuietly(socket))
  }

  private def setMoreAvailPort(value: String): Unit = {
    val key = RANDOM_PORT.toString + randomPortNum
    // TODO just replace it by sorted RANDOM_PORT, since only one RANDOM_PORT is used now.
    if(value.contains(key)) {
      processBuilder.setEnv(key, findAvailPort().toString)
      randomPortNum += 1
    }
  }

  override def launch(): Unit = {
    request.necessaryEnvironments.foreach{e =>
      val env = CommonVars(e, "")
      if(StringUtils.isEmpty(env.getValue))
        throw new ErrorException(30000, s"Necessary environment $e is not exists!(必须的环境变量 $e 不存在！)") //TODO exception
      else request.environment.put(e, env.getValue)
    }
    prepareCommand()
    val exec = newProcessEngineConnCommandExec(sudoCommand(request.user, execFile.mkString(" ")), engineConnManagerEnv.engineConnWorkDir)
    exec.execute()
    process = exec.getProcess
  }

  protected def execFile: Array[String]

  def getEngineConnPort: String = engineConnPort

  protected def getCommandArgs: Array[String] = {
    if (request.creationDesc.properties.exists { case (k, v) => k.contains(" ") || (v != null && v.contains(" ")) })
      throw new ErrorException(30000, "Startup parameters contain spaces!(启动参数中包含空格！)") //TODO exception
    val arguments = EngineConnArgumentsBuilder.newBuilder()
    engineConnPort = findAvailPort().toString
    var springConf = Map("spring.application.name" -> GovernanceCommonConf.ENGINE_CONN_SPRING_NAME.getValue,
      "server.port" -> engineConnPort, "spring.profiles.active" -> "engineconn",
      "logging.config" -> s"classpath:${EnvConfiguration.LOG4J2_XML_FILE.getValue}") ++: discoveryMsgGenerator.generate(engineConnManagerEnv)

    val eurekaPreferIp: Boolean = Configuration.EUREKA_PREFER_IP
    logger.info(s"EUREKA_PREFER_IP: " + eurekaPreferIp)
    if(eurekaPreferIp){
      springConf = springConf + ("eureka.instance.prefer-ip-address" -> "true")
      springConf = springConf + ("eureka.instance.instance-id" -> "\\${spring.cloud.client.ip-address}:\\${spring.application.name}:\\${server.port}")
    }

    request.creationDesc.properties.filter(_._1.startsWith("spring.")).foreach { case (k, v) =>
      springConf = springConf += (k -> v)
    }
    arguments.addSpringConf(springConf.toMap)
    var engineConnConf = Map("ticketId" -> request.ticketId, "user" -> request.user)
    engineConnConf = engineConnConf ++: request.labels.map(l => EngineConnArgumentsParser.LABEL_PREFIX + l.getLabelKey -> l.getStringValue).toMap
    engineConnConf = engineConnConf ++: request.creationDesc.properties.filterNot(_._1.startsWith("spring.")).toMap
    arguments.addEngineConnConf(engineConnConf)
    EngineConnArgumentsParser.getEngineConnArgumentsParser.parseToArgs(arguments.build())
  }

  override def kill(): Unit = {
    if(process != null){
      process.destroy()
    }
  }

  override def isAlive: Boolean = {
    if(process != null){
      process.isAlive
    }else{
      false
    }
  }

  protected def prepareCommand(): Unit = {
    processBuilder = newProcessEngineConnCommandBuilder()
    initializeEnv()
    //TODO env需要考虑顺序问题
    val classPath = request.environment.remove(CLASSPATH.toString)
    request.environment.foreach{ case (k, v) =>
      val value = v.replaceAll(CLASS_PATH_SEPARATOR, File.pathSeparator)
      setMoreAvailPort(value)
      processBuilder.setEnv(k, processBuilder.replaceExpansionMarker(value))
    }
    processBuilder.setEnv(CLASSPATH.toString, processBuilder.replaceExpansionMarker(classPath.replaceAll(CLASS_PATH_SEPARATOR, File.pathSeparator)))

    val engineConnEnvKeys = request.environment.remove(ENGINECONN_ENVKEYS.toString)
    logger.debug(s"ENGINECONN_ENVKEYS: " + engineConnEnvKeys)
    //set other env
    val engineConnEnvKeyArray = engineConnEnvKeys.split(",")
    engineConnEnvKeyArray.foreach(envKey => {
      if(null != envKey && !"".equals(envKey.trim)) {
        processBuilder.setEnv(envKey, GovernanceCommonConf.getEngineEnvValue(envKey))
      }
    })

    engineConnManagerEnv.linkDirs.foreach{case (k, v) => processBuilder.link(k, v)}
    val execCommand = request.commands.map(processBuilder.replaceExpansionMarker(_)) ++ getCommandArgs
    //execCommand = sudoCommand(request.user, execCommand.mkString(" "))
    execCommand.foreach(setMoreAvailPort)
    processBuilder.setCommand(execCommand)
    preparedExecFile = new File(engineConnManagerEnv.engineConnWorkDir, "engineConnExec.sh").getPath
    val output = getFileOutputStream
    Utils.tryFinally(processBuilder.writeTo(output))(output.close())
  }

  protected def sudoCommand(user: String, command: String): Array[String]

  protected def getFileOutputStream: OutputStream = FileUtils.openOutputStream(new File(preparedExecFile))

  protected def getPreparedExecFile: String = preparedExecFile

  def getProcessInputStream: InputStream = {
    if(process != null){
      process.getInputStream
    }else{
      throw new ECMCoreException(ECMErrorCode.PROCESS_WAITFOR_ERROR, "process is not be launch, can not get InputStream!")
    }
  }

  def processWaitFor:Int = {
    if(process != null){
      process.waitFor
    }else{
      throw new ECMCoreException(ECMErrorCode.PROCESS_WAITFOR_ERROR, "process is not be launch, can not get terminated code by wait!")
    }
  }

}