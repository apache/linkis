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

import org.apache.linkis.common.utils.JsonUtils
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration.{
  SPARK_CONF_DIR_ENV,
  SPARK_HOME_ENV
}
import org.apache.linkis.engineplugin.spark.config.SparkResourceConfiguration
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.manager.common.protocol.bml.BmlResource
import org.apache.linkis.manager.engineplugin.common.conf.EnvConfiguration
import org.apache.linkis.manager.engineplugin.common.launch.entity.EngineConnBuildRequest
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment.{variable, USER}
import org.apache.linkis.manager.engineplugin.common.launch.process.JavaProcessEngineConnLaunchBuilder
import org.apache.linkis.manager.engineplugin.common.launch.process.LaunchConstants.addPathToClassPath
import org.apache.linkis.manager.label.entity.engine.{
  EngineConnMode,
  EngineConnModeLabel,
  UserCreatorLabel
}
import org.apache.linkis.manager.label.utils.LabelUtil

import java.util

import scala.collection.JavaConverters.asScalaBufferConverter

import com.google.common.collect.Lists

class SparkEngineConnLaunchBuilder extends JavaProcessEngineConnLaunchBuilder {

  override protected def getCommands(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] = {
    if (isOnceMode) {
      val properties = engineConnBuildRequest.engineConnCreationDesc.properties
      properties.put(
        EnvConfiguration.ENGINE_CONN_MEMORY.key,
        SparkResourceConfiguration.LINKIS_SPARK_DRIVER_MEMORY.getValue(properties)
      )
      super.getCommands
    } else {
      new SparkSubmitProcessEngineConnLaunchBuilder(this).getCommands(
        engineConnBuildRequest,
        getMainClass,
        getGcLogDir(engineConnBuildRequest),
        getLogDir(engineConnBuildRequest)
      )
    }
  }

  def isOnceMode: Boolean = {
    val engineConnMode = LabelUtil.getEngineConnMode(engineConnBuildRequest.labels)
    EngineConnMode.toEngineConnMode(engineConnMode) == EngineConnMode.Once
  }

  override def getEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): util.Map[String, String] = {
    val environment = super.getEnvironment
    if (isOnceMode)
      addPathToClassPath(environment, s"$$$SPARK_HOME_ENV/jars/*")
    environment
  }

  override protected def getBmlResources(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): java.util.List[BmlResource] = {
    if (!isOnceMode) return super.getBmlResources
    val bmlResources = new java.util.ArrayList[BmlResource](super.getBmlResources)
    val properties = engineConnBuildRequest.engineConnCreationDesc.properties
    val userName = engineConnBuildRequest.labels.asScala
      .find(_.isInstanceOf[UserCreatorLabel])
      .map { case label: UserCreatorLabel => label.getUser }
      .get

    def getBmlString(bml: BmlResource): String = {
      s"BmlResource(${bml.getFileName}, ${bml.getResourceId}, ${bml.getVersion})"
    }

    val ticketId = engineConnBuildRequest.ticketId
    properties.get("spark.app.main.class.jar.bml.json") match {
      case mainClassJarContent: String =>
        val bml = contentToBmlResource(userName, mainClassJarContent)
        logger.info(s"Add a ${getBmlString(bml)} for user $userName and ticketId $ticketId")
        bmlResources.add(bml)
        properties.remove("spark.app.main.class.jar.bml.json")
      case _ =>
    }
    properties.get("spark.app.user.class.path.bml.json") match {
      case classpathContent: String =>
        val contentList = JsonUtils.jackson.readValue(
          classpathContent,
          classOf[java.util.List[java.util.Map[String, Object]]]
        )
        contentList.asScala.map(contentToBmlResource(userName, _)).foreach { bml =>
          logger.info(s"Add a ${getBmlString(bml)} for user $userName and ticketId $ticketId")
          bmlResources.add(bml)
        }
        properties.remove("spark.app.user.class.path.bml.json")
      case _ =>
    }
    bmlResources
  }

  private def contentToBmlResource(userName: String, content: String): BmlResource = {
    val contentMap = JsonUtils.jackson.readValue(content, classOf[java.util.Map[String, Object]])
    contentToBmlResource(userName, contentMap)
  }

  private def contentToBmlResource(
      userName: String,
      contentMap: java.util.Map[String, Object]
  ): BmlResource = {
    val bmlResource = new BmlResource
    bmlResource.setFileName(contentMap.get("fileName").asInstanceOf[String])
    bmlResource.setResourceId(contentMap.get("resourceId").asInstanceOf[String])
    bmlResource.setVersion(contentMap.get("version").asInstanceOf[String])
    bmlResource.setOwner(userName)
    bmlResource.setVisibility(BmlResource.BmlResourceVisibility.Private)
    bmlResource
  }

  override protected def getNecessaryEnvironment(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): Array[String] = if (isOnceMode) {
    Array(SPARK_HOME_ENV, SPARK_CONF_DIR_ENV) ++: super.getNecessaryEnvironment
  } else {
    super.getNecessaryEnvironment
  }

  override protected def getExtractJavaOpts: String = if (isOnceMode) {
    if (!HadoopConf.KEYTAB_PROXYUSER_ENABLED.getValue) super.getExtractJavaOpts
    else super.getExtractJavaOpts + s" -DHADOOP_PROXY_USER=${variable(USER)}".trim
  } else {
    super.getExtractJavaOpts
  }

  override protected def ifAddHiveConfigPath: Boolean = if (isOnceMode) {
    true
  } else {
    super.ifAddHiveConfigPath
  }

  override def enablePublicModule: Boolean = !isOnceMode

  override protected def getEngineConnManagerHooks(implicit
      engineConnBuildRequest: EngineConnBuildRequest
  ): java.util.List[String] = if (isOnceMode) {
    super.getEngineConnManagerHooks(engineConnBuildRequest)
  } else {
    Lists.newArrayList("JarUDFLoadECMHook")
  }

}
