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

package org.apache.linkis.manager.engineplugin.common.launch.process

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.conf.{EngineConnPluginConf, EnvConfiguration}
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration.LINKIS_PUBLIC_MODULE_PATH
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import org.apache.linkis.manager.engineplugin.common.launch.entity.{
  EngineConnBuildRequest,
  RicherEngineConnBuildRequest
}
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment._
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants._
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary._
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.nio.file.Paths
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

abstract class JavaProcessEngineConnLaunchBuilder
    extends ProcessEngineConnLaunchBuilder
    with Logging {

  private var engineConnResourceGenerator: EngineConnResourceGenerator = _

  def setEngineConnResourceGenerator(
      engineConnResourceGenerator: EngineConnResourceGenerator
  ): Unit =
    this.engineConnResourceGenerator = engineConnResourceGenerator

  protected def getGcLogDir(engineConnBuildRequest: EngineConnBuildRequest): String =
    variable(LOG_DIRS) + "/gc"

  protected def getLogDir(engineConnBuildRequest: EngineConnBuildRequest): String =
    s" -Dlogging.file=${EnvConfiguration.LOG4J2_XML_FILE.getValue} " +
      s" -D$TICKET_ID_KEY=${engineConnBuildRequest.ticketId}"

  override protected def getCommands(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] = {
    val commandLine: ArrayBuffer[String] = ArrayBuffer[String]()
    commandLine += (variable(JAVA_HOME) + "/bin/java")
    commandLine += "-server"

    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    var settingClientMemory = ""
    if (properties.containsKey(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)) {
      settingClientMemory = properties.get(EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.key)
    }
    if (StringUtils.isNotBlank(settingClientMemory)) {
      commandLine += ("-Xmx" + NodeResourceUtils.formatJavaOptionMemoryWithDefaultUnitG(
        settingClientMemory
      ))
    } else {
      val engineConnMemory = EngineConnPluginConf.JAVA_ENGINE_REQUEST_MEMORY.getValue.toString
      commandLine += ("-Xmx" + engineConnMemory)
    }

    val javaOPTS = getExtractJavaOpts
    if (StringUtils.isNotEmpty(EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue)) {
      EnvConfiguration.ENGINE_CONN_DEFAULT_JAVA_OPTS.getValue
        .format(getGcLogDir(engineConnBuildRequest))
        .split("\\s+")
        .foreach(commandLine += _)
    }
    if (StringUtils.isNotEmpty(javaOPTS)) javaOPTS.split("\\s+").foreach(commandLine += _)
    getLogDir(engineConnBuildRequest).trim.split(" ").foreach(commandLine += _)
    commandLine += ("-Djava.io.tmpdir=" + variable(TEMP_DIRS))
    if (EnvConfiguration.ENGINE_CONN_DEBUG_ENABLE.getValue) {
      commandLine += s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${variable(RANDOM_PORT)}"
    }
    commandLine += "-cp"
    commandLine += variable(CLASSPATH)
    commandLine += getMainClass
    commandLine ++= Seq(
      "1>",
      s"${variable(LOG_DIRS)}/stdout",
      "2>>",
      s"${variable(LOG_DIRS)}/stderr"
    )
    commandLine.toArray
  }

  protected def getMainClass: String = EngineConnPluginConf.ENGINECONN_MAIN_CLASS.getValue

  override protected def getEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.Map[String, String] = {
    logger.info("Setting up the launch environment for engineconn.")
    val environment = new util.HashMap[String, String]
    if (ifAddHiveConfigPath) {
      addPathToClassPath(environment, variable(HADOOP_CONF_DIR))
      addPathToClassPath(environment, variable(HIVE_CONF_DIR))
    }
    // first, add engineconn conf dirs.
    addPathToClassPath(environment, buildPath(Seq(variable(PWD), ENGINE_CONN_CONF_DIR_NAME)))
    // then, add LINKIS_CONF_DIR conf dirs.
    addPathToClassPath(environment, buildPath(Seq(EnvConfiguration.LINKIS_CONF_DIR.getValue)))
    // then, add engineconn libs.
    addPathToClassPath(environment, buildPath(Seq(variable(PWD), ENGINE_CONN_LIB_DIR_NAME + "/*")))
    // then, add public modules.
    if (!enablePublicModule) {
      addPathToClassPath(environment, buildPath(Seq(LINKIS_PUBLIC_MODULE_PATH.getValue + "/*")))
    }
    // finally, add the suitable properties key to classpath
    val taskClassPathFiles = EnvConfiguration.ENGINE_CONN_CLASSPATH_FILES.getValue(
      engineConnBuildRequest.engineConnCreationDesc.properties
    )
    if (StringUtils.isNotBlank(taskClassPathFiles)) {
      taskClassPathFiles
        .split(",")
        .filter(StringUtils.isNotBlank(_))
        .foreach(file => addPathToClassPath(environment, buildPath(Seq(file))))
    }
    getExtraClassPathFile.filter(StringUtils.isNotBlank(_)).foreach { file: String =>
      addPathToClassPath(environment, buildPath(Seq(new File(file).getName)))
    }
    environment
  }

  override protected def getNecessaryEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] =
    if (!ifAddHiveConfigPath) Array.empty
    else Array(HADOOP_CONF_DIR.toString, HIVE_CONF_DIR.toString)

  protected def getExtractJavaOpts: String = EnvConfiguration.ENGINE_CONN_JAVA_EXTRA_OPTS.getValue

  protected def getExtraClassPathFile: Array[String] =
    EnvConfiguration.ENGINE_CONN_JAVA_EXTRA_CLASSPATH.getValue.split(",")

  protected def ifAddHiveConfigPath: Boolean = false

  protected def enablePublicModule: Boolean = false

  override protected def getBmlResources(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.List[BmlResource] = {
    val engineType = engineConnBuildRequest.labels.asScala
      .find(_.isInstanceOf[EngineTypeLabel])
      .map { case engineTypeLabel: EngineTypeLabel => engineTypeLabel }
      .getOrElse(
        throw new EngineConnBuildFailedException(
          ETL_NOT_EXISTS.getErrorCode,
          ETL_NOT_EXISTS.getErrorDesc
        )
      )
    val engineConnResource = engineConnResourceGenerator.getEngineConnBMLResources(engineType)
    Array(
      engineConnResource.getConfBmlResource,
      engineConnResource.getLibBmlResource
    ) ++: engineConnResource.getOtherBmlResources.toList
  }.asJava

  private def buildPath(paths: Seq[String]): String =
    Paths.get(paths.head, paths.tail: _*).toFile.getPath

}
